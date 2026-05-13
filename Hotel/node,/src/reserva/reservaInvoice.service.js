const Reserva = require('./reserva.models');
const Usuario = require('../usuario/usuario.models');
const Habitacion = require('../habitacion/habitacion.models');
const { obtenerConfigFactura } = require('../config/facturaConfig.service');

function crearError(status, message) {
  const error = new Error(message);
  error.status = status;
  return error;
}

function redondear(valor) {
  return Number((Number(valor) || 0).toFixed(2));
}

function formatearImporte(valor) {
  return `${redondear(valor).toFixed(2)} EUR`;
}

function formatearFecha(fecha) {
  return new Date(fecha).toLocaleDateString('es-ES');
}

function calcularNoches(fechaEntrada, fechaSalida) {
  const entrada = new Date(fechaEntrada);
  const salida = new Date(fechaSalida);
  const diferenciaMs = salida - entrada;
  const noches = Math.ceil(diferenciaMs / (1000 * 60 * 60 * 24));
  return noches > 0 ? noches : 1;
}

async function generarNumeroFactura() {
  const totalFacturas = await Reserva.countDocuments({
    invoiceNumber: { $ne: null }
  });

  const anio = new Date().getFullYear();
  return `FAC-${anio}-${String(totalFacturas + 1).padStart(6, '0')}`;
}

async function asignarNumeroFacturaSiNoExiste(reserva) {
  if (reserva.invoiceNumber) {
    return reserva.invoiceNumber;
  }

  const nuevoNumero = await generarNumeroFactura();
  reserva.invoiceNumber = nuevoNumero;
  reserva.invoiceIssuedAt = new Date();
  await reserva.save();

  return nuevoNumero;
}

exports.obtenerDatosFactura = async (reservaId) => {
  const reserva = await Reserva.findById(reservaId);

  if (!reserva) {
    throw crearError(404, 'Reserva no encontrada');
  }

  if (reserva.cancelacion && !reserva.invoiceNumber) {
    throw crearError(400, 'No se puede generar una factura nueva de una reserva cancelada');
  }

  const cliente = await Usuario.findById(reserva.clienteId).lean();
  const habitacion = await Habitacion.findById(reserva.habitacionId).lean();

  if (!cliente) {
    throw crearError(404, 'Cliente no encontrado');
  }

  if (!habitacion) {
    throw crearError(404, 'Habitación no encontrada');
  }

  const invoiceNumber = await asignarNumeroFacturaSiNoExiste(reserva);

  const noches = calcularNoches(reserva.fechaEntrada, reserva.fechaSalida);
  const precioNoche = habitacion.precionoche != null
    ? redondear(habitacion.precionoche)
    : redondear(reserva.precioTotal / noches);

  const subtotalNoches = redondear(noches * precioNoche);

  const extras = Array.isArray(reserva.extras)
    ? reserva.extras.map(extra => ({
        concepto: extra.concepto,
        importe: redondear(extra.importe)
      }))
    : [];

  const totalExtras = redondear(
    extras.reduce((acc, extra) => acc + extra.importe, 0)
  );

  const descuento = redondear(reserva.descuento);
  const impuestos = redondear(reserva.impuestos);

  const totalCalculado = redondear(
    subtotalNoches + totalExtras - descuento + impuestos
  );

  const totalFinal = redondear(
    reserva.precioTotal != null ? reserva.precioTotal : totalCalculado
  );

  return {
    hotel: obtenerConfigFactura(),
    reserva: reserva.toObject(),
    cliente,
    habitacion,
    invoiceNumber,
    fechaFactura: reserva.invoiceIssuedAt || new Date(),
    noches,
    precioNoche,
    subtotalNoches,
    extras,
    totalExtras,
    descuento,
    impuestos,
    totalFinal,
    formatearFecha,
    formatearImporte
  };
};

exports.renderFacturaPdf = (doc, datos) => {
  const {
    hotel,
    cliente,
    habitacion,
    reserva,
    invoiceNumber,
    fechaFactura,
    noches,
    precioNoche,
    subtotalNoches,
    extras,
    descuento,
    impuestos,
    formatearFecha,
    formatearImporte
  } = datos;

  const margenIzq = 50;
  const margenDer = 545;
  const anchoPagina = margenDer - margenIzq;
  const colorBorde = '#222222';
  const colorFondo = '#f4f4f4';
  const colorTexto = '#111111';
  const ivaPorDefecto = 21;
  const texto = (valor) => valor || '-';

  const dibujarCaja = (x, y, ancho, alto, titulo, lineas) => {
    doc
      .lineWidth(1)
      .strokeColor(colorBorde)
      .rect(x, y, ancho, alto)
      .stroke();

    doc
      .font('Helvetica-Bold')
      .fontSize(12)
      .fillColor(colorTexto)
      .text(titulo, x + 12, y - 18, { width: ancho - 24, align: 'center' });

    doc
      .font('Helvetica')
      .fontSize(9)
      .fillColor(colorTexto);

    lineas.forEach((linea, index) => {
      doc.text(linea, x + 12, y + 14 + (index * 15), { width: ancho - 24 });
    });
  };

  const filas = [
    {
      detalle: `Habitacion ${habitacion.numero} - ${habitacion.tipo}`,
      precio: precioNoche,
      cantidad: noches,
      importe: subtotalNoches
    },
    ...extras.map(extra => ({
      detalle: `Extra - ${extra.concepto}`,
      precio: extra.importe,
      cantidad: 1,
      importe: extra.importe
    }))
  ];

  if (descuento > 0) {
    filas.push({
      detalle: 'Descuento',
      precio: -descuento,
      cantidad: 1,
      importe: -descuento
    });
  }

  const subtotal = redondear(filas.reduce((acc, fila) => acc + fila.importe, 0));
  const ivaImporte = impuestos > 0
    ? impuestos
    : redondear(subtotal * (ivaPorDefecto / 100));
  const ivaPorcentaje = subtotal > 0
    ? redondear((ivaImporte / subtotal) * 100)
    : ivaPorDefecto;
  const totalConIva = redondear(subtotal + ivaImporte);

  doc
    .font('Helvetica-Bold')
    .fontSize(22)
    .fillColor(colorTexto)
    .text('FACTURA', margenIzq, 36, { width: anchoPagina, align: 'center' });

  dibujarCaja(margenIzq, 88, 225, 120, 'Datos empresa', [
    texto(hotel.nombreHotel),
    `CIF: ${texto(hotel.cif)}`,
    `Direccion: ${texto(hotel.direccion)}`,
    `Email: ${texto(hotel.email)}`,
    `Telefono: ${texto(hotel.telefono)}`
  ]);

  dibujarCaja(320, 88, 225, 120, 'Datos cliente', [
    `Nombre: ${texto(cliente.nombre)}`,
    `DNI: ${texto(cliente.dni)}`,
    `Email: ${texto(cliente.email)}`,
    reserva.empresaNombre ? `Empresa: ${reserva.empresaNombre}` : ''
  ].filter(Boolean));

  doc
    .font('Helvetica-Bold')
    .fontSize(11)
    .fillColor(colorTexto)
    .text(`No Factura: ${invoiceNumber}`, margenIzq, 235)
    .text(`Fecha factura: ${formatearFecha(fechaFactura)}`, 320, 235);

  const tablaY = 290;
  const detalleX = margenIzq;
  const precioX = 355;
  const cantidadX = 430;
  const importeX = 485;
  const cabeceraAlto = 26;
  const filaAlto = 25;

  doc
    .rect(detalleX, tablaY, anchoPagina, cabeceraAlto)
    .fill(colorFondo)
    .strokeColor(colorBorde)
    .rect(detalleX, tablaY, anchoPagina, cabeceraAlto)
    .stroke();

  doc
    .font('Helvetica-Bold')
    .fontSize(10)
    .fillColor(colorTexto)
    .text('Detalle factura', detalleX + 8, tablaY + 8, { width: 290 })
    .text('Precio', precioX, tablaY + 8, { width: 65, align: 'right' })
    .text('Cant.', cantidadX, tablaY + 8, { width: 45, align: 'right' })
    .text('Importe', importeX, tablaY + 8, { width: 55, align: 'right' });

  let y = tablaY + cabeceraAlto;

  filas.forEach(fila => {
    doc
      .strokeColor('#cccccc')
      .rect(detalleX, y, anchoPagina, filaAlto)
      .stroke();

    doc
      .font('Helvetica')
      .fontSize(9)
      .fillColor(colorTexto)
      .text(fila.detalle, detalleX + 8, y + 8, { width: 285 })
      .text(formatearImporte(fila.precio), precioX, y + 8, { width: 65, align: 'right' })
      .text(String(fila.cantidad), cantidadX, y + 8, { width: 45, align: 'right' })
      .text(formatearImporte(fila.importe), importeX, y + 8, { width: 55, align: 'right' });

    doc
      .strokeColor(colorBorde)
      .moveTo(precioX - 8, y)
      .lineTo(precioX - 8, y + filaAlto)
      .moveTo(cantidadX - 8, y)
      .lineTo(cantidadX - 8, y + filaAlto)
      .moveTo(importeX - 8, y)
      .lineTo(importeX - 8, y + filaAlto)
      .stroke();

    y += filaAlto;
  });

  y += 38;

  const resumenX = 340;
  const filaResumen = (label, valor, bold = false) => {
    doc
      .font(bold ? 'Helvetica-Bold' : 'Helvetica')
      .fontSize(bold ? 12 : 10)
      .fillColor(colorTexto)
      .text(label, resumenX, y, { width: 110 })
      .text(valor, resumenX + 110, y, { width: 95, align: 'right' });
    y += bold ? 22 : 18;
  };

  filaResumen('Subtotal', formatearImporte(subtotal));
  filaResumen(`IVA (${ivaPorcentaje.toFixed(2)}%)`, formatearImporte(ivaImporte));

  doc
    .strokeColor(colorBorde)
    .moveTo(resumenX, y - 4)
    .lineTo(margenDer, y - 4)
    .stroke();

  filaResumen('Total', formatearImporte(totalConIva), true);

  doc
    .font('Helvetica')
    .fontSize(9)
    .fillColor('#555555')
    .text(
      `Estancia: ${formatearFecha(reserva.fechaEntrada)} - ${formatearFecha(reserva.fechaSalida)} | Personas: ${reserva.personas}`,
      margenIzq,
      760,
      { width: anchoPagina, align: 'center' }
    );
};

exports.listarFacturas = async ({ userId, filtro } = {}) => {
  const consulta = {
    invoiceNumber: { $ne: null }
  };

  if (userId) {
    consulta.clienteId = userId;
  }

  const reservas = await Reserva.find(consulta)
    .sort({ invoiceIssuedAt: -1, createdAt: -1 })
    .lean();

  const habitacionIds = reservas.map(r => r.habitacionId);
  const clienteIds = reservas.map(r => r.clienteId);

  const habitaciones = await Habitacion.find({
    _id: { $in: habitacionIds }
  }).lean();

  const clientes = await Usuario.find({
    _id: { $in: clienteIds }
  }).lean();

  const resultado = reservas.map(reserva => {
    const habitacion = habitaciones.find(
      h => h._id.toString() === reserva.habitacionId.toString()
    );

    const cliente = clientes.find(
      c => c._id.toString() === reserva.clienteId.toString()
    );

    return {
      reservaId: reserva._id,
      clienteId: reserva.clienteId,
      invoiceNumber: reserva.invoiceNumber,
      invoiceIssuedAt: reserva.invoiceIssuedAt,
      fechaEntrada: reserva.fechaEntrada,
      fechaSalida: reserva.fechaSalida,
      precioTotal: reserva.precioTotal,
      cancelacion: reserva.cancelacion,
      cliente: cliente
        ? {
            nombre: cliente.nombre,
            dni: cliente.dni,
            email: cliente.email
          }
        : null,
      habitacion: habitacion
        ? {
            numero: habitacion.numero,
            tipo: habitacion.tipo
          }
        : null
    };
  });

  const filtroNormalizado = (filtro || '').trim().toLowerCase();

  if (!filtroNormalizado) {
    return resultado;
  }

  return resultado.filter(factura => {
    return [
      factura.invoiceNumber,
      factura.cliente?.nombre,
      factura.cliente?.dni,
      factura.cliente?.email,
      factura.habitacion?.numero?.toString(),
      factura.habitacion?.tipo
    ]
      .filter(Boolean)
      .some(valor => valor.toString().toLowerCase().includes(filtroNormalizado));
  });
};

exports.listarFacturasPorUsuario = async (userId) => {
  return exports.listarFacturas({ userId });
};
