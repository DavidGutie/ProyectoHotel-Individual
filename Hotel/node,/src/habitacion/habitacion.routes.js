const express = require('express');
const router = express.Router();
const controller = require('./habitacion.controller');
const upload = require("../Middleware/upload.middleware");

router.post('/', upload.array('imagenes', 10), controller.crearHabitacion);

router.get('/', controller.obtenerHabitaciones);
router.get('/:id/amenities', controller.obtenerAmenitiesHabitacion);
router.put('/:id/amenities', controller.asignarAmenitiesHabitacion);
router.patch('/:id/pet-policy', controller.actualizarPoliticaMascotasHabitacion);
router.get('/:id/mascotas', controller.obtenerPoliticaMascotasHabitacion);
router.get('/:id/pets', controller.obtenerPoliticaMascotasHabitacion);
router.put('/:id/mascotas', controller.actualizarPoliticaMascotasHabitacion);
router.put('/:id/pets', controller.actualizarPoliticaMascotasHabitacion);
router.patch('/:id/mascotas', controller.actualizarPoliticaMascotasHabitacion);
router.patch('/:id/pets', controller.actualizarPoliticaMascotasHabitacion);
router.get('/:id/servicios', controller.obtenerServiciosHabitacion);
router.get('/:id/amenidades', controller.obtenerServiciosHabitacion);
router.put('/:id/servicios', controller.reemplazarServiciosHabitacion);
router.put('/:id/amenidades', controller.reemplazarServiciosHabitacion);
router.post('/:id/servicios', controller.agregarServiciosHabitacion);
router.post('/:id/amenidades', controller.agregarServiciosHabitacion);
router.delete('/:id/servicios', controller.eliminarServiciosHabitacion);
router.delete('/:id/amenidades', controller.eliminarServiciosHabitacion);
router.delete('/:id/servicios/:servicio', controller.eliminarServicioHabitacion);
router.delete('/:id/amenidades/:servicio', controller.eliminarServicioHabitacion);
router.get('/:id', controller.obtenerHabitacion);

router.put('/:id', upload.array('imagenes', 10), controller.actualizarHabitacion);

router.delete('/:id', controller.eliminarHabitacion);

module.exports = router;
