const mongoose = require('mongoose');
const { listarFacturasPorUsuario } = require('../reserva/reservaInvoice.service');

exports.obtenerFacturasUsuario = async (req, res) => {
  try {
    const { userId } = req.query;

    if (!userId) {
      return res.status(400).json({ msg: 'El parámetro userId es obligatorio' });
    }

    if (!mongoose.Types.ObjectId.isValid(userId)) {
      return res.status(400).json({ msg: 'userId no válido' });
    }

    const facturas = await listarFacturasPorUsuario(userId);
    res.json(facturas);

  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};