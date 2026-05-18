const express = require('express');
const router = express.Router();
const amenityController = require('./amenity.controller');

router.get('/', amenityController.obtenerAmenities);
router.post('/', amenityController.crearAmenity);
router.get('/:id', amenityController.obtenerAmenityPorId);
router.put('/:id', amenityController.actualizarAmenity);
router.delete('/:id', amenityController.eliminarAmenity);

module.exports = router;
