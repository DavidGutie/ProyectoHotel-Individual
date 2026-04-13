const Resena = require('./reseña.models');

exports.crearResena = async (req, res) => {
  try {
    const { clienteId, habitacionId, reservaId, puntuacion, comentario } = req.body;

    if (!clienteId || !habitacionId || !puntuacion) {
      return res.status(400).json({ msg: 'Faltan datos obligatorios' });
    }

    if (puntuacion < 1 || puntuacion > 5) {
      return res.status(400).json({ msg: 'La puntuación debe estar entre 1 y 5' });
    }

    const nuevaResena = new Resena({
      clienteId,
      habitacionId,
      reservaId,
      puntuacion,
      comentario
    });

    const resenaGuardada = await nuevaResena.save();
    res.status(201).json(resenaGuardada);

  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

// 1. Asegúrate de tener el modelo de Cliente importado arriba
const Cliente = require('../cliente/cliente.models'); // Ajusta la ruta a tu proyecto

// ... (otros métodos)

exports.obtenerResenas = async (req, res) => {
  try {
    // Traemos las reseñas
    const resenas = await Resena.find().lean(); 
    
    // IMPORTANTE: Buscamos en la colección de Clientes, NO en Usuarios
    const clientes = await Cliente.find().lean();

    const resultado = resenas.map(resena => {
      // Buscamos el cliente que coincide con el ID de la reseña
      const clienteEncontrado = clientes.find(c => 
        c._id.toString() === resena.clienteId.toString()
      );

      return {
        ...resena,
        // Construimos el objeto cliente para que C# lo entienda
        cliente: clienteEncontrado ? {
          _id: clienteEncontrado._id,
          dni: clienteEncontrado.dni, // C# lo leerá gracias a [JsonPropertyName("dni")]
          nombre: clienteEncontrado.nombre
        } : null
      };
    });

    res.json(resultado);

  } catch (error) {
    console.error("Error en obtenerResenas:", error);
    res.status(500).json({ error: error.message });
  }
};

exports.obtenerResenasPorHabitacion = async (req, res) => {
  try {
    const resenas = await Resena.find({ habitacionId: req.params.id });
    res.json(resenas);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.eliminarResena = async (req, res) => {
  try {
    const resena = await Resena.findByIdAndDelete(req.params.id);

    if (!resena) {
      return res.status(404).json({ msg: 'Reseña no encontrada' });
    }

    res.json({ msg: 'Reseña eliminada' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};