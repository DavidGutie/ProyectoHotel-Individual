const express = require('express');
const router = express.Router();
const resenaController = require('./reseña.controller');

router.get('/', resenaController.obtenerResenas);
router.post('/', resenaController.crearResena);
router.delete('/:id', resenaController.eliminarResena);

module.exports = router;