const fs = require('fs');
const path = require('path');
const DEFAULT_CONFIG = require('./factura.config');

const CONFIG_PATH = path.join(__dirname, 'factura.config.json');

function normalizarConfig(config) {
  return {
    nombreHotel: config.nombreHotel || DEFAULT_CONFIG.nombreHotel,
    cif: config.cif || DEFAULT_CONFIG.cif,
    direccion: config.direccion || DEFAULT_CONFIG.direccion,
    email: config.email || DEFAULT_CONFIG.email,
    telefono: config.telefono || DEFAULT_CONFIG.telefono
  };
}

function obtenerConfigFactura() {
  if (!fs.existsSync(CONFIG_PATH)) {
    return normalizarConfig(DEFAULT_CONFIG);
  }

  try {
    const contenido = fs.readFileSync(CONFIG_PATH, 'utf8');
    return normalizarConfig(JSON.parse(contenido));
  } catch {
    return normalizarConfig(DEFAULT_CONFIG);
  }
}

function guardarConfigFactura(config) {
  const nuevaConfig = normalizarConfig({
    ...obtenerConfigFactura(),
    ...config
  });

  fs.writeFileSync(CONFIG_PATH, JSON.stringify(nuevaConfig, null, 2));
  return nuevaConfig;
}

module.exports = {
  obtenerConfigFactura,
  guardarConfigFactura
};
