const mongoose = require('../db');

const reservaAuditSchema = new mongoose.Schema({
  reservaId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Reserva',
    required: true,
    index: true
  },
  action: {
    type: String,
    required: true,
    enum: ['CREAR', 'MODIFICAR', 'CANCELAR', 'ELIMINAR', 'PAGO', 'EXTRA']
  },
  actorId: {
    type: mongoose.Schema.Types.ObjectId,
    default: null
  },
  actorType: {
    type: String,
    enum: ['user', 'employee', 'system'],
    default: 'system'
  },
  previousState: {
    type: mongoose.Schema.Types.Mixed,
    default: null
  },
  newState: {
    type: mongoose.Schema.Types.Mixed,
    default: null
  },
  timestamp: {
    type: Date,
    default: Date.now,
    immutable: true
  }
}, {
  versionKey: false
});

module.exports = mongoose.model('ReservaAudit', reservaAuditSchema, 'ReservaAudit');