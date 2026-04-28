const ReservaAudit = require('./reservaAudit.models');

exports.registrarAuditoriaReserva = async ({
  reservaId,
  action,
  actorId = null,
  actorType = 'system',
  previousState = null,
  newState = null
}) => {
  try {
    const log = new ReservaAudit({
      reservaId,
      action,
      actorId,
      actorType,
      previousState,
      newState
    });

    return await log.save();
  } catch (error) {
    console.error('Error al registrar auditoría de reserva:', error.message);
    return null;
  }
};

exports.obtenerAuditoriaPorReserva = async (reservaId) => {
  return await ReservaAudit
    .find({ reservaId })
    .sort({ timestamp: 1 });
};