const Usuario = require('../usuario/usuario.models')
const mongoose = require('mongoose')

const ClienteSchema = new mongoose.Schema({
    ciudad: {
    type: String,
    required: true
  },
  vip: {
    type: Boolean,
    default: false
  }
})

module.exports = Usuario.discriminator('Cliente', ClienteSchema)