const mongoose = require('../db');

const amenitySchema = new mongoose.Schema({
  name: {
    type: String,
    required: true,
    unique: true,
    trim: true
  },
  icon: {
    type: String,
    default: '',
    trim: true
  },
  category: {
    type: String,
    required: true,
    trim: true
  }
}, {
  timestamps: true
});

module.exports = mongoose.model('Amenity', amenitySchema, 'Amenities');
