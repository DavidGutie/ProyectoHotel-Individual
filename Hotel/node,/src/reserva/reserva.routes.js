const express = require('express');
const router = express.Router();
const reservaController = require('./reserva.controller');

router.post('/', reservaController.crearReserva);

router.get('/', reservaController.obtenerReservas);

router.get('/:id/factura', reservaController.descargarFacturaReserva);

router.get('/:id', reservaController.obtenerReservaPorId);

router.get('/:id/audit', reservaController.obtenerHistorialReserva);

router.put('/:id/cancelar', reservaController.cancelarReserva);

router.delete('/:id', reservaController.eliminarReserva);


module.exports = router;
