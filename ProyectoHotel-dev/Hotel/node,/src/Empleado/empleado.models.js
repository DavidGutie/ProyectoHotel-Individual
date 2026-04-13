const Usuario = require('../usuario/usuario.models')
const mongoose = require('mongoose')

const EmpleadoSchema = new mongoose.Schema({
  administrador: {
    type: Boolean,
    default: false
  },
  
})

module.exports = Usuario.discriminator('Empleado', EmpleadoSchema)