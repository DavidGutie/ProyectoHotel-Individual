const mongoose = require('../db');

const roomAmenitySchema = new mongoose.Schema({
  roomId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Habitacion',
    required: true,
    index: true
  },
  amenityId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Amenity',
    required: true,
    index: true
  }
}, {
  timestamps: true
});

roomAmenitySchema.index({ roomId: 1, amenityId: 1 }, { unique: true });

module.exports = mongoose.model('RoomAmenity', roomAmenitySchema, 'room_amenities');
