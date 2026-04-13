const mongoose = require('../db');
const bcrypt = require('bcrypt')

const usuarioSchema = new mongoose.Schema({

    nombre: {
    type: String,
    required: true
    },
    
    dni: {
    type: String,
    unique: true,
    required: true
    }
    ,

    email: {
    type: String,
    required: true,
    },

    password: {
    type: String,
    required: true},

    fechaNacimiento: {
    type: Date,
    required: true},

    sexo: {
    type: String,
    enum: ['M', 'F', 'X'],
    required: true
  },
  foto: {
    type: String,
    default: null,
    required: false
  }

}, 
{
    discriminatorKey: 'tipoUsuario',
    timestamps: true
})
usuarioSchema.pre('save', async function () {
  if (!this.isModified('password')) return;
  const salt = await bcrypt.genSalt(10);
  this.password = await bcrypt.hash(this.password, salt);
});


usuarioSchema.methods.comparePassword = async function (plain) {
  const res = await bcrypt.compare(plain, this.password);
  return res;
};

const Usuario = mongoose.model('usuarios', usuarioSchema, 'usuarios');
module.exports = Usuario;