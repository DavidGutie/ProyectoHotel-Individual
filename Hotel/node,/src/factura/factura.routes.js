const express = require('express');
const router = express.Router();
const facturaController = require('./factura.controller');

router.get('/', facturaController.obtenerFacturasUsuario);
router.get('/config', facturaController.obtenerConfigFactura);
router.put('/config', facturaController.actualizarConfigFactura);
router.post('/:id/reenviar', facturaController.reenviarFactura);

module.exports = router;
