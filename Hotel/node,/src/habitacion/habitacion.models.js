
const mongoose = require('../db');

const habitacionSchema = new mongoose.Schema({

  numero: {
    type: Number,
    required: true,
    unique: true,
    min: 1
  },

  tipo: {
    type: String,
    required: true,
    enum: ['individual', 'doble', 'triple', 'suite']
  },

  descripcion: {
    type: String,
    default: ''
  },

  imagenes: {
    type: [String],
    default: []
  },

  precionoche: {
    type: Number,
    required: true,
    min: 0
  },

  rate: {
    type: Number,
    default: 0,
    min: 0,
    max: 5
  },

  max_ocupantes: {
    type: Number,
    required: true,
    min: 1
  },

  disponible: {
    type: Boolean,
    default: true
  },

  oferta: {
    type: Boolean,
    default: false
  },

  amenities: {
    type: [{
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Amenity'
    }],
    default: []
  },

  pets_allowed: {
    type: Boolean,
    default: false
  },

  pet_supplement_per_night: {
    type: Number,
    default: 0,
    min: 0
  },

  aceptaMascotas: {
    type: Boolean,
    default: false
  },

  politicaMascotas: {
    type: String,
    default: '',
    trim: true
  },

  suplementoMascota: {
    type: Number,
    default: 0,
    min: 0
  },

  maxMascotas: {
    type: Number,
    default: 0,
    min: 0
  },

  servicios: {
    type: [String],
    default: []
  }

}, {

  timestamps: true

});

module.exports = mongoose.model('Habitacion', habitacionSchema, 'Habitacion');
