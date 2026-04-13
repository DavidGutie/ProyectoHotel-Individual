const Usuario = require('../usuario/usuario.models');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcrypt');

exports.login = async (req, res) => {
    try {
        const { email, password } = req.body;
        if (!email || !password) {
            return res.status(400).json({ message: 'Email y contraseña son requeridos' });
        }

        const usuario = await Usuario.findOne({ email });
        if (!usuario) {
            return res.status(401).json({ message: 'Este email no está registrado' });
        }

        const esPasswordValido = await usuario.comparePassword(password);
        if (!esPasswordValido) {
            return res.status(401).json({ message: 'Contraseña incorrecta' });
        }

        let esAdministrador = false;

        if (usuario.tipoUsuario === "Empleado") {
            esAdministrador = usuario.administrador;
        }
        
        const token = jwt.sign(
        {
            id: usuario._id,
            tipoUsuario: usuario.tipoUsuario,
            administrador: esAdministrador
        },
        process.env.JWT_SECRET || 'secreto_super_seguro',
        { expiresIn: '2h' }
        );

        res.status(200).json({
            token,
            usuario: {
                id: usuario._id,
                nombre: usuario.nombre,
                email: usuario.email,
                tipoUsuario: usuario.tipoUsuario,
                administrador: esAdministrador
        }
    });

    } catch (error) {
    res.status(500).json({ error: error.message });
  }
};