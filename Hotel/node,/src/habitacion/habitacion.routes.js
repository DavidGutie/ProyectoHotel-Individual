const express = require('express');
const router = express.Router();
const controller = require('./habitacion.controller');
const upload = require("../Middleware/upload.middleware");

router.post('/', upload.array('imagenes', 10), controller.crearHabitacion);

router.get('/', controller.obtenerHabitaciones);
router.get('/:id/servicios', controller.obtenerServiciosHabitacion);
router.put('/:id/servicios', controller.reemplazarServiciosHabitacion);
router.post('/:id/servicios', controller.agregarServiciosHabitacion);
router.delete('/:id/servicios', controller.eliminarServiciosHabitacion);
router.delete('/:id/servicios/:servicio', controller.eliminarServicioHabitacion);
router.get('/:id', controller.obtenerHabitacion);

router.put('/:id', upload.array('imagenes', 10), controller.actualizarHabitacion);

router.delete('/:id', controller.eliminarHabitacion);

module.exports = router;
