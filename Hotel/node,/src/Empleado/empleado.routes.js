const express = require('express');
const router = express.Router();


const empleadoController = require('./empleado.controller');
const upload = require("../Middleware/upload.middleware");

router.post('/', upload.single('foto'), empleadoController.crearEmpleado);

router.get('/', empleadoController.obtenerEmpleados);

router.get('/:id', empleadoController.obtenerEmpleadoPorId);

router.put('/:id', upload.single('foto'), empleadoController.actualizarEmpleado);

router.delete('/:id', empleadoController.eliminarEmpleado);
module.exports = router;
