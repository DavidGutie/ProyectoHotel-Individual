const mongoose = require('../db');

const extraSchema = new mongoose.Schema({
  concepto: {
    type: String,
    required: true,
    trim: true
  },
  importe: {
    type: Number,
    required: true,
    min: 0
  }
}, { _id: false });

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
  },

  // FACTURA
  invoiceNumber: {
    type: String,
    default: null,
    index: true
  },
  invoiceIssuedAt: {
    type: Date,
    default: null
  },
  empresaNombre: {
    type: String,
    default: null,
    trim: true
  },
  empresaCif: {
    type: String,
    default: null,
    trim: true
  },
  empresaDireccion: {
    type: String,
    default: null,
    trim: true
  },
  extras: {
    type: [extraSchema],
    default: []
  },
  descuento: {
    type: Number,
    default: 0,
    min: 0
  },
  impuestos: {
    type: Number,
    default: 0,
    min: 0
  }

}, {
  timestamps: true
});

module.exports = mongoose.model('Reserva', reservaSchema, 'Reserva');