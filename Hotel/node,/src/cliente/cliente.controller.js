const Cliente = require('./cliente.models');
const fs = require("fs");


exports.crearCliente = async (req, res) => {
  try {
    const { nombre, dni, email, password, fechaNacimiento, sexo, ciudad, vip } = req.body;  
    if (!nombre || !dni || !email || !password || !fechaNacimiento || !sexo || !ciudad || vip === undefined) {
        return res.status(400).json({ msg: 'Faltan datos obligatorios' });  
    }

    if (!validarDNI(dni)) {
      return res.status(400).json({ msg: 'DNI inválido' });
    }
    const dniExiste = await Cliente.findOne({ dni });
    if (dniExiste) {
      return res.status(409).json({ msg: 'El DNI ya está registrado' });
    }

    const existe = await Cliente.findOne({ email });
    if (existe) {
       return res.status(409).json({ msg: 'El email ya está registrado' });
    }
    

    if (!esEmailValido(email)) {
      return res.status(400).json({ msg: 'Email inválido' });
    }

    if (!esPasswordValida(password)) {
      return res.status(400).json({ msg: 'La contraseña debe tener al menos 6 caracteres, una mayúscula, una minúscula, un número y un carácter especial' });
    }
    let foto = null;
    if (req.file) {
      foto = `/uploads/${req.file.filename}`;
    }

    const fecha = validarFechaNacimiento(fechaNacimiento);

    if(!fecha) {
      return res.status(400).json({ msg: 'Fecha de nacimiento inválida. Formato correcto DD/MM/YYYY' });
    }

    if (!['M', 'F', 'X'].includes(sexo)) {
      return res.status(400).json({ msg: 'Sexo inválido. Debe ser M, F o X' });
    }



    const nuevoCliente = new Cliente({ nombre, dni, email, password, fechaNacimiento : fecha, sexo, foto, ciudad, vip });
    const clienteGuardado = await nuevoCliente.save();

    const clienteJSON = clienteGuardado.toObject();
    delete clienteJSON.password;

    res.status(201).json(clienteJSON);
  } catch (error) {
    borrarArchivo(req.file);
    res.status(500).json({ error: error.message });
  }
};

exports.obtenerClientes = async (req, res) => {
  try {
    const clientes = await Cliente.find();

    const clienteJSON = clientes.map(cliente => {
      const clienteObj = cliente.toObject();
      delete clienteObj.password;
      return clienteObj;
    });

    res.status(200).json(clienteJSON);
  } catch (error) {
    res.status(500).json({ error: error.message });
  } 
};

exports.obtenerClientePorId = async (req, res) => {
  try {
    const cliente = await Cliente.findById(req.params.id); 
    if (!cliente) {
      return res.status(404).json({ msg: 'Cliente no encontrado' });
    }

    const clienteJSON = cliente.toObject();
    delete clienteJSON.password;

    res.status(200).json(clienteJSON);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.actualizarCliente = async (req, res) => {
  try {

    const { nombre, dni, email, password, fechaNacimiento, sexo, ciudad, vip } = req.body;

    if (!nombre || !dni || !email || !fechaNacimiento || !sexo || !ciudad || vip === undefined) {
      borrarArchivo(req.file);
      return res.status(400).json({ msg: 'Faltan datos obligatorios' });
    }

    if (!validarDNI(dni)) {
      borrarArchivo(req.file);
      return res.status(400).json({ msg: 'DNI inválido' });
    }

    const cliente = await Cliente.findById(req.params.id);

    if (!cliente) {
      borrarArchivo(req.file);
      return res.status(404).json({ msg: 'Cliente no encontrado' });
    }

    const dniExiste = await Cliente.findOne({
      dni,
      _id: { $ne: req.params.id }
    });

    if (dniExiste) {
      borrarArchivo(req.file);
      return res.status(409).json({ msg: 'El DNI ya está registrado' });
    }

    const emailEnUso = await Cliente.findOne({
      email,
      _id: { $ne: req.params.id }
    });

    if (emailEnUso) {
      borrarArchivo(req.file);
      return res.status(409).json({ msg: 'El email ya está registrado' });
    }

    if (!esEmailValido(email)) {
      borrarArchivo(req.file);
      return res.status(400).json({ msg: 'Email inválido' });
    }


    const fecha = validarFechaNacimiento(fechaNacimiento);

    if (!fecha) {
      borrarArchivo(req.file);
      return res.status(400).json({ msg: 'Fecha inválida' });
    }

    if (!['M', 'F', 'X'].includes(sexo)) {
      borrarArchivo(req.file);
      return res.status(400).json({ msg: 'Sexo inválido' });
    }

    // PASSWORD OPCIONAL
    if (password && password.trim() !== "") {
      if (!esPasswordValida(password)) {
        return res.status(400).json({ msg: 'Password inválida' });
      }

      cliente.password = password;
    }

    let foto = cliente.foto;

    if (req.file) {

      // borrar foto antigua
      if (cliente.foto) {
        const rutaVieja = "." + cliente.foto;

        if (fs.existsSync(rutaVieja)) {
          fs.unlinkSync(rutaVieja);
        }
      }

      foto = `/uploads/${req.file.filename}`;
    }

    
    cliente.nombre = nombre;
    cliente.dni = dni;
    cliente.email = email;
    cliente.fechaNacimiento = fecha;
    cliente.sexo = sexo;
    cliente.foto = foto;
    cliente.ciudad = ciudad;
    cliente.vip = vip;

    await cliente.save();

    const clienteJSON = cliente.toObject();
    delete clienteJSON.password;

    res.status(200).json(clienteJSON);

  } catch (error) {
    borrarArchivo(req.file);
    res.status(500).json({ error: error.message });
  }
};


exports.eliminarCliente = async (req, res) => {
  try {
    const clienteEliminado = await Cliente.findByIdAndDelete(req.params.id);
    if (!clienteEliminado) {
      return res.status(404).json({ msg: 'Cliente no encontrado' });
    }
    res.status(200).json({ msg: 'Cliente eliminado correctamente' });
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