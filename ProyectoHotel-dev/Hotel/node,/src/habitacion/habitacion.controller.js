const Habitacion = require('./habitacion.models');
const fs = require("fs");
const path = require("path");

const pickAllowed = (obj, allowed) => {
  const out = {};
  for (const key of allowed) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) out[key] = obj[key];
  }
  return out;
};

const handleMongoErrors = (error, res, fallbackMessage) => {
  // Validación de MongoDB a nivel de colección ($jsonSchema) -> code 121
  if (error && (error.code === 121 || error.codeName === 'DocumentValidationFailure')) {
    return res.status(400).json({
      message: 'Error de validación (MongoDB): el documento no cumple el esquema',
      errores: Object.values(error.errors).map(e => e.message)
    });
  }

  // Duplicado por índice unique (ej: numero) -> code 11000
  if (error && error.code === 11000) {
    return res.status(409).json({
      message: 'Conflicto: ya existe una habitación con ese número',
      details: error.keyValue
    });
  }

  // ID inválido / cast
  if (error && error.name === 'CastError') {
    return res.status(400).json({ message: 'ID inválido' });
  }

  return res.status(500).json({ message: fallbackMessage, error: error?.message });
};

const esRutaLocalUploads = (v) => typeof v === "string" && v.startsWith("/uploads/");

const tryParseArray = (value) => {
  if (value == null) return [];
  if (Array.isArray(value)) return value;

  // A veces form-data manda strings; si viene JSON en string lo parseamos
  if (typeof value === "string") {
    const t = value.trim();
    if (!t) return [];
    try {
      const parsed = JSON.parse(t);
      return Array.isArray(parsed) ? parsed : [parsed];
    } catch {
      // Si no es JSON, puede venir separado por comas
      if (t.includes(",")) return t.split(",").map(x => x.trim()).filter(Boolean);
      return [t];
    }
  }
  return [value];
};

const getUploadedPaths = (req) => {
  // Con upload.fields: req.files = { imagen: [..], imagenes: [..] }
  const out = [];

  if (req.files && req.files.imagen && req.files.imagen[0]) {
    out.push(`/uploads/${req.files.imagen[0].filename}`);
  }
  if (req.files && Array.isArray(req.files.imagenes)) {
    out.push(...req.files.imagenes.map(f => `/uploads/${f.filename}`));
  }

  // Por si en algún punto usas upload.single / upload.array
  if (!out.length && req.file) out.push(`/uploads/${req.file.filename}`);
  if (!out.length && Array.isArray(req.files)) out.push(...req.files.map(f => `/uploads/${f.filename}`));

  return out;
};

const borrarUploadsLocales = (imagenes) => {
  const arr = Array.isArray(imagenes) ? imagenes : (imagenes ? [imagenes] : []);
  for (const img of arr) {
    if (!esRutaLocalUploads(img)) continue;
    // "/uploads/xxx.jpg" -> "./uploads/xxx.jpg"
    const ruta = path.join(".", img);
    if (fs.existsSync(ruta)) fs.unlinkSync(ruta);
  }
};

exports.crearHabitacion = async (req, res) => {
  try {
    const allowedFields = [
      'numero',
      'tipo',
      'descripcion',
      'imagen',      
      'imagenes',    
      'precionoche',
      'rate',
      'max_ocupantes',
      'disponible',
      'oferta',
      'servicios'
    ];

    const datos = pickAllowed(req.body, allowedFields);
    
    const urlsExternas = tryParseArray(datos.imagenes).map(String).filter(Boolean);
    
    const rutasLocales = getUploadedPaths(req);

    const carrusel = [...rutasLocales, ...urlsExternas];

    const nuevaHabitacion = new Habitacion({
      ...datos,
      descripcion: datos.descripcion ?? '',
      imagen: (datos.imagen && String(datos.imagen)) ? String(datos.imagen) : (carrusel[0] ?? ''),
      imagenes: carrusel,
      rate: datos.rate ?? 0,
      disponible: datos.disponible ?? true,
      oferta: datos.oferta ?? false,
      servicios: datos.servicios ?? []
    });

    const habitacionGuardada = await nuevaHabitacion.save();
    return res.status(201).json(habitacionGuardada);
  } catch (error) {
    try { borrarUploadsLocales(getUploadedPaths(req)); } catch {}
    console.error(error);
    return handleMongoErrors(error, res, 'Error creando la habitación');
  }
};

exports.obtenerHabitaciones = async (req, res) => {
  try {
    const habitaciones = await Habitacion.find();
    return res.json(habitaciones);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: 'Error obteniendo las habitaciones', error: error.message });
  }
};

exports.obtenerHabitacion = async (req, res) => {
  try {
    const { id } = req.params;
    const habitacion = await Habitacion.findById(id);
    if (!habitacion) return res.status(404).json({ message: 'Habitación no encontrada' });
    return res.json(habitacion);
  } catch (error) {
    console.error(error);
    return handleMongoErrors(error, res, 'Error obteniendo la habitación');
  }
};

exports.actualizarHabitacion = async (req, res) => {
  try {
    const { id } = req.params;

    const allowedFields = [
      'numero',
      'tipo',
      'descripcion',
      'imagen',       
      'imagenes',     
      'precionoche',
      'rate',
      'max_ocupantes',
      'disponible',
      'oferta',
      'servicios',
      'replace'       
    ];

    const datos = pickAllowed(req.body, allowedFields);

    const habitacion = await Habitacion.findById(id);
    if (!habitacion) {
      try { borrarUploadsLocales(getUploadedPaths(req)); } catch {}
      return res.status(404).json({ message: 'Habitación no encontrada' });
    }

    const urlsExternas = tryParseArray(datos.imagenes).map(String).filter(Boolean);

    const rutasLocales = getUploadedPaths(req);

    const nuevas = [...rutasLocales, ...urlsExternas];

    const replace =
      req.query.replace === "true" ||
      datos.replace === true ||
      datos.replace === "true";

    if (nuevas.length) {
      if (replace) {
        borrarUploadsLocales(habitacion.imagenes);
        habitacion.imagenes = nuevas;
      } else {
        habitacion.imagenes = [...habitacion.imagenes, ...nuevas];
      }

      if (!datos.imagen) {
        habitacion.imagen = habitacion.imagenes[0] ?? habitacion.imagen ?? '';
      }
    }

    if (datos.numero !== undefined) habitacion.numero = datos.numero;
    if (datos.tipo !== undefined) habitacion.tipo = datos.tipo;
    if (datos.descripcion !== undefined) habitacion.descripcion = datos.descripcion;
    if (datos.imagen !== undefined) habitacion.imagen = String(datos.imagen);
    if (datos.precionoche !== undefined) habitacion.precionoche = datos.precionoche;
    if (datos.rate !== undefined) habitacion.rate = datos.rate;
    if (datos.max_ocupantes !== undefined) habitacion.max_ocupantes = datos.max_ocupantes;
    if (datos.disponible !== undefined) habitacion.disponible = datos.disponible;
    if (datos.oferta !== undefined) habitacion.oferta = datos.oferta;
    if (datos.servicios !== undefined) habitacion.servicios = datos.servicios;

    const guardada = await habitacion.save();
    return res.json(guardada);

  } catch (error) {
    try { borrarUploadsLocales(getUploadedPaths(req)); } catch {}
    console.error(error);
    return handleMongoErrors(error, res, 'Error actualizando la habitación');
  }
};

exports.eliminarHabitacion = async (req, res) => {
  try {
    const { id } = req.params;

    const habitacion = await Habitacion.findById(id);
    if (!habitacion) return res.status(404).json({ message: 'Habitación no encontrada' });
    borrarUploadsLocales(habitacion.imagenes);
    if (habitacion.imagen) borrarUploadsLocales(habitacion.imagen);

    await Habitacion.findByIdAndDelete(id);
    return res.json({ message: 'Habitación eliminada' });

  } catch (error) {
    console.error(error);
    return handleMongoErrors(error, res, 'Error eliminando la habitación');
  }
};