const express = require('express');
const router = express.Router();

const clienteController = require('./cliente.controller');
const upload = require("../Middleware/upload.middleware");

router.post('/', upload.single('foto'), clienteController.crearCliente);

router.get('/', clienteController.obtenerClientes);

router.get('/:id', clienteController.obtenerClientePorId);

router.put('/:id', upload.single('foto'), clienteController.actualizarCliente);

router.delete('/:id', clienteController.eliminarCliente);

module.exports = router;
