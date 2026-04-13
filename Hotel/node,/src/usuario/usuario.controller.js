const Usuario = require('./usuario.models.js')

exports.verTodosUsuarios = async (req, res) => {
  try {

    const usuarios = await Usuario.find();
    const usuarioJSON = usuarios.map(usuario => {
      const usuarioObj = usuario.toObject();
      delete usuarioObj.password;
      return usuarioObj;
    });
    res.status(200).json(usuarioJSON);

    } catch (error) {
    res.status(500).json({ error: error.message });
  } 
};