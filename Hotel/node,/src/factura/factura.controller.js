const mongoose = require('mongoose');
const { listarFacturas } = require('../reserva/reservaInvoice.service');
const {
  obtenerConfigFactura,
  guardarConfigFactura
} = require('../config/facturaConfig.service');

exports.obtenerFacturasUsuario = async (req, res) => {
  try {
    const { userId, filtro } = req.query;

    if (userId && !mongoose.Types.ObjectId.isValid(userId)) {
      return res.status(400).json({ msg: 'userId no valido' });
    }

    const facturas = await listarFacturas({ userId, filtro });
    res.json(facturas);

  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.obtenerConfigFactura = async (req, res) => {
  res.json(obtenerConfigFactura());
};

exports.actualizarConfigFactura = async (req, res) => {
  try {
    const config = guardarConfigFactura(req.body || {});
    res.json(config);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.reenviarFactura = async (req, res) => {
  try {
    const { id } = req.params;

    if (!mongoose.Types.ObjectId.isValid(id)) {
      return res.status(400).json({ msg: 'Id de reserva no valido' });
    }

    res.json({
      msg: 'Factura marcada para reenvio por email',
      reservaId: id
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};
