const express = require('express');
const router = express.Router();
const facturaController = require('./factura.controller');

router.get('/', facturaController.obtenerFacturasUsuario);

module.exports = router;