const Reserva = require('./reserva.models');
const Usuario = require('../usuario/usuario.models');
const Habitacion = require('../habitacion/habitacion.models');
const FACTURA_CONFIG = require('../config/factura.config');

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
    hotel: FACTURA_CONFIG,
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
    totalFinal,
    formatearFecha,
    formatearImporte
  } = datos;

  const colorPrincipal = '#1f3c88';
  const colorSuave = '#e9eef9';
  const margenIzq = 50;
  const margenDer = 545;

  const linea = () => {
    const y = doc.y;
    doc
      .strokeColor('#d9d9d9')
      .lineWidth(1)
      .moveTo(margenIzq, y)
      .lineTo(margenDer, y)
      .stroke();
    doc.moveDown(0.7);
  };

  const filaConcepto = (concepto, importe, negrita = false) => {
    doc
      .font(negrita ? 'Helvetica-Bold' : 'Helvetica')
      .fontSize(11)
      .fillColor('#000000')
      .text(concepto, margenIzq, doc.y, { width: 360 });

    doc
      .font(negrita ? 'Helvetica-Bold' : 'Helvetica')
      .fontSize(11)
      .text(importe, 430, doc.y - 14, {
        width: 110,
        align: 'right'
      });

    doc.moveDown(0.5);
  };

  // CABECERA
  doc
    .rect(margenIzq, 40, 495, 55)
    .fill(colorPrincipal);

  doc
    .fillColor('#ffffff')
    .font('Helvetica-Bold')
    .fontSize(21)
    .text(hotel.nombreHotel, 65, 58);

  doc
    .fillColor('#000000')
    .moveDown(3);

  doc
    .font('Helvetica-Bold')
    .fontSize(18)
    .fillColor(colorPrincipal)
    .text('FACTURA', margenIzq, 120);

  doc
    .font('Helvetica')
    .fontSize(10)
    .fillColor('#000000')
    .text(`Número: ${invoiceNumber}`, 380, 122, { width: 160, align: 'right' })
    .text(`Fecha: ${formatearFecha(fechaFactura)}`, 380, 137, { width: 160, align: 'right' });

  doc.moveDown(1.5);

  // DATOS HOTEL
  doc
    .rect(margenIzq, doc.y, 230, 75)
    .fill(colorSuave);

  const yHotel = doc.y - 68;

  doc
    .fillColor(colorPrincipal)
    .font('Helvetica-Bold')
    .fontSize(11)
    .text('Datos del hotel', margenIzq + 12, yHotel);

  doc
    .fillColor('#000000')
    .font('Helvetica')
    .fontSize(10)
    .text(`CIF: ${hotel.cif}`, margenIzq + 12, yHotel + 18)
    .text(`Dirección: ${hotel.direccion}`, margenIzq + 12, yHotel + 34, { width: 200 })
    .text(`Email: ${hotel.email}`, margenIzq + 12, yHotel + 52);

  // DATOS CLIENTE
  doc
    .rect(315, yHotel - 7, 230, 90)
    .fill(colorSuave);

  doc
    .fillColor(colorPrincipal)
    .font('Helvetica-Bold')
    .fontSize(11)
    .text('Datos del cliente', 327, yHotel);

  doc
    .fillColor('#000000')
    .font('Helvetica')
    .fontSize(10)
    .text(`Nombre: ${cliente.nombre}`, 327, yHotel + 18)
    .text(`DNI: ${cliente.dni}`, 327, yHotel + 34)
    .text(`Email: ${cliente.email}`, 327, yHotel + 50, { width: 200 });

  if (reserva.empresaNombre || reserva.empresaCif || reserva.empresaDireccion) {
    doc.moveDown(2.5);

    doc
      .font('Helvetica-Bold')
      .fontSize(11)
      .fillColor(colorPrincipal)
      .text('Datos de empresa', margenIzq);

    doc
      .font('Helvetica')
      .fontSize(10)
      .fillColor('#000000')
      .text(`Empresa: ${reserva.empresaNombre || '-'}`)
      .text(`CIF: ${reserva.empresaCif || '-'}`)
      .text(`Dirección: ${reserva.empresaDireccion || '-'}`);

    doc.moveDown(0.8);
  } else {
    doc.moveDown(2.8);
  }

  linea();

  doc
    .font('Helvetica-Bold')
    .fontSize(11)
    .fillColor(colorPrincipal)
    .text('Detalle de la estancia');

  doc.moveDown(0.4);

  doc
    .font('Helvetica')
    .fontSize(10)
    .fillColor('#000000')
    .text(`Habitación: ${habitacion.numero} - ${habitacion.tipo}`)
    .text(`Entrada: ${formatearFecha(reserva.fechaEntrada)}`)
    .text(`Salida: ${formatearFecha(reserva.fechaSalida)}`)
    .text(`Personas: ${reserva.personas}`)
    .text(`Noches: ${noches}`);

  doc.moveDown(0.8);
  linea();

  doc
    .font('Helvetica-Bold')
    .fontSize(11)
    .fillColor(colorPrincipal)
    .text('Desglose económico');

  doc.moveDown(0.5);

  filaConcepto(
    `Alojamiento (${noches} noche(s) x ${formatearImporte(precioNoche)})`,
    formatearImporte(subtotalNoches)
  );

  extras.forEach(extra => {
    filaConcepto(`Extra - ${extra.concepto}`, formatearImporte(extra.importe));
  });

  if (descuento > 0) {
    filaConcepto('Descuento', `- ${formatearImporte(descuento)}`);
  }

  if (impuestos > 0) {
    filaConcepto('Impuestos', formatearImporte(impuestos));
  }

  doc.moveDown(0.4);
  linea();

  filaConcepto('TOTAL', formatearImporte(totalFinal), true);

  doc.moveDown(1.2);

  doc
    .font('Helvetica')
    .fontSize(9)
    .fillColor('#555555')
    .text(
      'Documento generado automáticamente por el sistema del hotel.',
      margenIzq,
      doc.y,
      { align: 'left' }
    );
};

exports.listarFacturasPorUsuario = async (userId) => {
  const reservas = await Reserva.find({
    clienteId: userId,
    invoiceNumber: { $ne: null }
  })
    .sort({ invoiceIssuedAt: -1, createdAt: -1 })
    .lean();

  const habitacionIds = reservas.map(r => r.habitacionId);
  const habitaciones = await Habitacion.find({
    _id: { $in: habitacionIds }
  }).lean();

  return reservas.map(reserva => {
    const habitacion = habitaciones.find(
      h => h._id.toString() === reserva.habitacionId.toString()
    );

    return {
      reservaId: reserva._id,
      invoiceNumber: reserva.invoiceNumber,
      invoiceIssuedAt: reserva.invoiceIssuedAt,
      fechaEntrada: reserva.fechaEntrada,
      fechaSalida: reserva.fechaSalida,
      precioTotal: reserva.precioTotal,
      cancelacion: reserva.cancelacion,
      habitacion: habitacion
        ? {
            numero: habitacion.numero,
            tipo: habitacion.tipo
          }
        : null
    };
  });
};