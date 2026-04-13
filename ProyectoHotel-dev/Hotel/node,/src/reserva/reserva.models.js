const mongoose = require('../db');

const reservaSchema = new mongoose.Schema({
  clienteId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Usuario',
    required: true
  },
  habitacionId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Habitacion',
    required: true
  },
  fechaEntrada: {
    type: Date,
    required: true
  },
  fechaSalida: {
    type: Date,
    required: true
  },
  personas: {
    type: Number,
    required: true,
    min: 1
  },
  precioTotal: {
    type: Number,
    required: true,
    min: 0
  },
  cancelacion: {
    type: Boolean,
    default: false
  }
}, {
  timestamps: true
});

module.exports = mongoose.model('Reserva', reservaSchema, 'Reserva');
