const Empleado = require('./empleado.models');
const fs = require("fs");


exports.crearEmpleado = async (req, res) => {
  try {
    const { nombre, dni, email, password, fechaNacimiento, sexo, administrador } = req.body;  
    if (!nombre || !dni || !email || !password || !fechaNacimiento || !sexo || administrador === undefined) {
        return res.status(400).json({ msg: 'Faltan datos obligatorios' });  
    }
    if (!validarDNI(dni)) {
      return res.status(400).json({ msg: 'DNI inválido' });
    }
    const dniExiste = await Empleado.findOne({ dni });
      if (dniExiste) {
        return res.status(409).json({ msg: 'El DNI ya está registrado' });
      }
    
    const existe = await Empleado.findOne({ email });
    if (existe) {
       return res.status(409).json({ msg: 'El email ya está registrado' });
    }

    if (!esEmailValido(email)) {
        return res.status(400).json({ msg: 'Email inválido' });
    }
    if (!esPasswordValida(password)) {
      return res.status(400).json({ msg: 'La contraseña debe tener al menos 6 caracteres, una mayúscula, una minúscula, un número y un carácter especial' });
    }

    const fecha = validarFechaNacimiento(fechaNacimiento);

    if(!fecha) {
      return res.status(400).json({ msg: 'Fecha de nacimiento inválida. Formato correcto DD/MM/YYYY' });
    }

    if (!['M', 'F', 'X'].includes(sexo)) {
      return res.status(400).json({ msg: 'Sexo inválido. Debe ser M, F o X' });
    }

    let foto = null;
    if (req.file) {
      foto = `/uploads/${req.file.filename}`;
    }

    const nuevoEmpleado  = new Empleado({ nombre, dni, email, password, fechaNacimiento : fecha, sexo, foto, administrador });
    const empleadoGuardado = await nuevoEmpleado.save();

    const empleadoJSON = empleadoGuardado.toObject();
    delete empleadoJSON.password;
    
    res.status(201).json(empleadoJSON);
    } catch (error) {
      borrarArchivo(req.file);
      res.status(500).json({ error: error.message });
    }
};
exports.obtenerEmpleados = async (req, res) => {
  try {
    const empleados = await Empleado.find();

    const empleadoJSON = empleados.map(empleado => {
      const empleadoObj = empleado.toObject();
      delete empleadoObj.password;
      return empleadoObj;
    });

    res.status(200).json(empleadoJSON);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.obtenerEmpleadoPorId = async (req, res) => {
  try {
    const empleado = await Empleado.findById(req.params.id);
    if (!empleado) {
      return res.status(404).json({ msg: 'Empleado no encontrado' });
    }
    const empleadoJSON = empleado.toObject();
    delete empleadoJSON.password;
    res.status(200).json(empleadoJSON);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.actualizarEmpleado = async (req, res) => {
  try {

    const { nombre, dni, email, password, fechaNacimiento, sexo, administrador } = req.body;

    if (!nombre || !dni || !email || !fechaNacimiento || !sexo || administrador === undefined) {
      return res.status(400).json({ msg: 'Faltan datos obligatorios' });
    }

    if (!validarDNI(dni)) {
      return res.status(400).json({ msg: 'DNI inválido' });
    }

    const empleado = await Empleado.findById(req.params.id);
    if (!empleado) {
      return res.status(404).json({ msg: 'Empleado no encontrado' });
    }

    const dniExiste = await Empleado.findOne({ dni, _id: { $ne: req.params.id } });
    if (dniExiste) {
      return res.status(409).json({ msg: 'El DNI ya está registrado' });
    }

    const emailEnUso = await Empleado.findOne({ email, _id: { $ne: req.params.id } });
    if (emailEnUso) {
      return res.status(409).json({ msg: 'El email ya está registrado' });
    }

    if (!esEmailValido(email)) {
      return res.status(400).json({ msg: 'Email inválido' });
    }

    const fecha = validarFechaNacimiento(fechaNacimiento);
    if (!fecha) {
      return res.status(400).json({ msg: 'Fecha inválida' });
    }

    if (!['M', 'F', 'X'].includes(sexo)) {
      return res.status(400).json({ msg: 'Sexo inválido' });
    }

    // PASSWORD OPCIONAL
    if (password && password.trim() !== "") {
      if (!esPasswordValida(password)) {
        return res.status(400).json({ msg: 'Password inválida' });
      }

      empleado.password = password;
    }

    // FOTO
    let foto = empleado.foto;

    if (req.file) {
      if (empleado.foto) {
        const rutaVieja = "." + empleado.foto;
        if (fs.existsSync(rutaVieja)) fs.unlinkSync(rutaVieja);
      }

      foto = `/uploads/${req.file.filename}`;
    }

    // ACTUALIZAR CAMPOS
    empleado.nombre = nombre;
    empleado.dni = dni;
    empleado.email = email;
    empleado.fechaNacimiento = fecha;
    empleado.sexo = sexo;
    empleado.foto = foto;
    empleado.administrador = administrador;

    const actualizado = await empleado.save();

    const json = actualizado.toObject();
    delete json.password;

    res.status(200).json(json);

  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.eliminarEmpleado = async (req, res) => {
  try {
    const empleado = await Empleado.findByIdAndDelete(req.params.id);
    if (!empleado) {
      return res.status(404).json({ msg: 'Empleado no encontrado' });
    }
    res.status(200).json({ msg: 'Empleado eliminado correctamente' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

function validarDNI(dni) {
    const letras = "TRWAGMYFPDXBNJZSQVHLCKE";
    
    
    const regex = /^\d{8}[A-Z]$/; // Formato: 8 números + 1 letra

    dni = dni.toUpperCase();

    if (!regex.test(dni)) {
        return false;
    }

    const numero = parseInt(dni.substring(0, 8), 10);
    const letra = dni.charAt(8);
    const letraCorrecta = letras[numero % 23];

    return letra === letraCorrecta;
}


function esEmailValido(email) {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // Formato basico de email, @, dominio, .com.
  return regex.test(email)
}

function esPasswordValida(password) {
  const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,}$/;// Al menos 6 caracteres, una mayúscula, una minúscula, un número y un carácter especial
  return regex.test(password);
}

function validarFechaNacimiento(fechaNacimiento) {
  const regex = /^(\d{2})\/(\d{2})\/(\d{4})$/ // Formato DD/MM/YYYY

  if (!regex.test(fechaNacimiento)) {
    return null
  }

  const [, dia, mes, anio] = fechaNacimiento.match(regex)

  const day = parseInt(dia, 10)
  const month = parseInt(mes, 10)
  const year = parseInt(anio, 10)

  const date = new Date(year, month - 1, day)

  return date
}

function borrarArchivo(file) {
  if (!file) return;

  const ruta = file.path;

  if (fs.existsSync(ruta)) {
    fs.unlinkSync(ruta);
  }
}