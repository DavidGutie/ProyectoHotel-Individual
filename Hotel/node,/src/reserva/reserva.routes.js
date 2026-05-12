const express = require('express');
const router = express.Router();
const reservaController = require('./reserva.controller');

router.post('/', reservaController.crearReserva);

router.get('/', reservaController.obtenerReservas);

router.get('/:id/factura', reservaController.descargarFacturaReserva);

router.get('/:id/audit', reservaController.obtenerHistorialReserva);

router.get('/:id', reservaController.obtenerReservaPorId);

router.patch('/:id', reservaController.actualizarReserva);

router.put('/:id/cancelar', reservaController.cancelarReserva);

router.post('/:id/pago', reservaController.registrarPagoReserva);

router.post('/:id/extras', reservaController.agregarExtraReserva);

router.delete('/:id', reservaController.eliminarReserva);

module.exports = router;
