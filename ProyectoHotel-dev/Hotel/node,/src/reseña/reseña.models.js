const mongoose = require('../db');

const resenaSchema = new mongoose.Schema({
  clienteId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Cliente',
    required: true
  },
  habitacionId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Habitacion',
    required: true
  },
  reservaId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Reserva',
    required: false
  },
  puntuacion: {
    type: Number,
    required: true,
    min: 1,
    max: 5
  },
  comentario: {
    type: String,
    default: ''
  }
}, {
  timestamps: true
});
module.exports = mongoose.model('Resena', resenaSchema, 'Resena');