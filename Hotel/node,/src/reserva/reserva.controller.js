const mongoose = require('mongoose');
const Reserva = require('./reserva.models');
const Usuario = require('../usuario/usuario.models');
const Habitacion = require('../habitacion/habitacion.models');
const {
  registrarAuditoriaReserva,
  obtenerAuditoriaPorReserva
} = require('./reservaAudit.service');

function obtenerActorDesdeRequest(req) {
  return {
    actorId: req.body?.actorId || null,
    actorType: req.body?.actorType || 'system'
  };
}

exports.crearReserva = async (req, res) => {
  try {
    const {
      clienteId,
      habitacionId,
      fechaEntrada,
      fechaSalida,
      personas,
      precioTotal
    } = req.body;

    if (
      !clienteId ||
      !habitacionId ||
      !fechaEntrada ||
      !fechaSalida ||
      !personas ||
      precioTotal === undefined
    ) {
      return res.status(400).json({ msg: 'Faltan datos obligatorios' });
    }

    if (new Date(fechaSalida) <= new Date(fechaEntrada)) {
      return res.status(400).json({ msg: 'Fechas inválidas' });
    }

    if (personas < 1) {
      return res.status(400).json({ msg: 'Debe haber al menos una persona' });
    }

    if (precioTotal < 0) {
      return res.status(400).json({ msg: 'Precio inválido' });
    }

    const nuevaReserva = new Reserva({
      clienteId,
      habitacionId,
      fechaEntrada,
      fechaSalida,
      personas,
      precioTotal
    });

    const reservaGuardada = await nuevaReserva.save();

    const { actorId, actorType } = obtenerActorDesdeRequest(req);

    await registrarAuditoriaReserva({
      reservaId: reservaGuardada._id,
      action: 'CREAR',
      actorId,
      actorType,
      previousState: null,
      newState: reservaGuardada.toObject()
    });

    res.status(201).json(reservaGuardada);

  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.eliminarReserva = async (req, res) => {
  try {
    const reserva = await Reserva.findById(req.params.id);

    if (!reserva) {
      return res.status(404).json({ msg: 'Reserva no encontrada' });
    }

    if (!reserva.cancelacion) {
      return res.status(400).json({
        msg: 'Solo se pueden eliminar reservas canceladas'
      });
    }

    const previousState = reserva.toObject();
    const { actorId, actorType } = obtenerActorDesdeRequest(req);

    await Reserva.findByIdAndDelete(req.params.id);

    await registrarAuditoriaReserva({
      reservaId: reserva._id,
      action: 'ELIMINAR',
      actorId,
      actorType,
      previousState,
      newState: null
    });

    res.json({ msg: 'Reserva eliminada correctamente' });

  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.obtenerReservas = async (req, res) => {
  try {
    const reservas = await Reserva.find().lean();
    const clientes = await Usuario.find().lean();
    const habitaciones = await Habitacion.find().lean();

    const resultado = reservas.map(reserva => {
      const cliente = clientes.find(
        c => c._id.toString() === reserva.clienteId.toString()
      );

      const habitacion = habitaciones.find(
        h => h._id.toString() === reserva.habitacionId.toString()
      );

      return {
        ...reserva,
        cliente: cliente
          ? {
              dni: cliente.dni,
              nombre: cliente.nombre
            }
          : null,
        habitacion: habitacion
          ? {
              numero: habitacion.numero
            }
          : null
      };
    });

    res.json(resultado);

  } catch (error) {
    console.error('ERROR EN EL SERVIDOR:', error);
    res.status(500).json({ error: 'Error interno', detalle: error.message });
  }
};

exports.obtenerReservaPorId = async (req, res) => {
  try {
    const reserva = await Reserva.findById(req.params.id);

    if (!reserva) {
      return res.status(404).json({ msg: 'Reserva no encontrada' });
    }

    res.json(reserva);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.cancelarReserva = async (req, res) => {
  try {
    const reserva = await Reserva.findById(req.params.id);

    if (!reserva) {
      return res.status(404).json({ msg: 'Reserva no encontrada' });
    }

    if (reserva.cancelacion) {
      return res.status(400).json({ msg: 'La reserva ya está cancelada' });
    }

    const previousState = reserva.toObject();

    reserva.cancelacion = true;
    const reservaActualizada = await reserva.save();

    const { actorId, actorType } = obtenerActorDesdeRequest(req);

    await registrarAuditoriaReserva({
      reservaId: reservaActualizada._id,
      action: 'CANCELAR',
      actorId,
      actorType,
      previousState,
      newState: reservaActualizada.toObject()
    });

    res.json(reservaActualizada);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.obtenerHistorialReserva = async (req, res) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
      return res.status(400).json({ msg: 'Id de reserva no válido' });
    }

    const historial = await obtenerAuditoriaPorReserva(req.params.id);

    if (!historial || historial.length === 0) {
      return res.status(404).json({ msg: 'No hay historial para esta reserva' });
    }

    res.json(historial);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};