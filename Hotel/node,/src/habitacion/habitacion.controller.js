const mongoose = require('mongoose');
const Habitacion = require('./habitacion.models');
const Amenity = require('../amenity/amenity.models');
const RoomAmenity = require('../amenity/roomAmenity.models');
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

const normalizarServicios = (value) => {
  const servicios = tryParseArray(value)
    .flatMap(item => {
      if (typeof item !== "string") return [item];
      return item.split(/[;,]/);
    })
    .map(item => String(item).trim())
    .filter(Boolean);

  const vistos = new Set();
  return servicios.filter(servicio => {
    const key = servicio.toLowerCase();
    if (vistos.has(key)) return false;
    vistos.add(key);
    return true;
  });
};

const resolverServiciosBody = (body) => {
  if (Object.prototype.hasOwnProperty.call(body, "servicios")) return body.servicios;
  if (Object.prototype.hasOwnProperty.call(body, "amenidades")) return body.amenidades;
  if (Object.prototype.hasOwnProperty.call(body, "servicio")) return body.servicio;
  return undefined;
};

const parseBoolean = (value) => {
  if (typeof value === "boolean") return value;
  if (typeof value === "string") return value.toLowerCase() === "true";
  return Boolean(value);
};

const resolverPoliticaMascotas = (body) => {
  const out = {};

  const aceptaMascotas = body.pets_allowed ?? body.aceptaMascotas ?? body.permiteMascotas ?? body.mascotasPermitidas ?? body.petFriendly ?? body.petsAllowed;
  const politicaMascotas = body.politicaMascotas ?? body.politica_mascotas ?? body.petPolicy;
  const suplementoMascota = body.pet_supplement_per_night ?? body.suplementoMascota ?? body.suplementoMascotas ?? body.suplemento_mascota ?? body.petSupplement;
  const maxMascotas = body.maxMascotas ?? body.max_mascotas ?? body.maxPets;

  if (aceptaMascotas !== undefined) {
    out.pets_allowed = parseBoolean(aceptaMascotas);
    out.aceptaMascotas = out.pets_allowed;
  }
  if (politicaMascotas !== undefined) out.politicaMascotas = String(politicaMascotas).trim();
  if (suplementoMascota !== undefined) {
    out.pet_supplement_per_night = Number(suplementoMascota);
    out.suplementoMascota = out.pet_supplement_per_night;
  }
  if (maxMascotas !== undefined) out.maxMascotas = Number(maxMascotas);

  return out;
};

const resolverAmenitiesBody = (body) => {
  if (Object.prototype.hasOwnProperty.call(body, "amenities")) return body.amenities;
  if (Object.prototype.hasOwnProperty.call(body, "amenityIds")) return body.amenityIds;
  if (Object.prototype.hasOwnProperty.call(body, "amenitiesIds")) return body.amenitiesIds;
  return undefined;
};

const normalizarAmenityIds = (value) => tryParseArray(value)
  .map(item => String(item).trim())
  .filter(Boolean);

const escapeRegex = (value) => value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

const resolverAmenitiesPorNombres = async (nombres) => {
  const names = normalizarAmenityIds(nombres);
  if (!names.length) return [];

  return Amenity.find({
    $or: names.map(name => ({
      name: new RegExp(`^${escapeRegex(name)}$`, 'i')
    }))
  });
};

const resolverAmenityIdsExistentes = async (ids) => {
  const amenityIds = normalizarAmenityIds(ids);

  if (!amenityIds.length) return [];

  if (amenityIds.some(id => !mongoose.Types.ObjectId.isValid(id))) {
    const error = new Error('Todos los amenities deben ser ObjectId validos');
    error.status = 400;
    throw error;
  }

  const encontrados = await Amenity.find({ _id: { $in: amenityIds } }).select('_id');

  if (encontrados.length !== amenityIds.length) {
    const error = new Error('Uno o mas amenities no existen');
    error.status = 400;
    throw error;
  }

  return encontrados.map(amenity => amenity._id);
};

const sincronizarRoomAmenities = async (roomId, amenityIds) => {
  await RoomAmenity.deleteMany({ roomId });

  if (!amenityIds.length) return;

  await RoomAmenity.insertMany(
    amenityIds.map(amenityId => ({
      roomId,
      amenityId
    }))
  );
};

const validarPoliticaMascotas = (politica) => {
  if (politica.suplementoMascota !== undefined && (Number.isNaN(politica.suplementoMascota) || politica.suplementoMascota < 0)) {
    return 'El suplemento de mascota debe ser un numero mayor o igual que 0';
  }

  if (politica.maxMascotas !== undefined && (!Number.isInteger(politica.maxMascotas) || politica.maxMascotas < 0)) {
    return 'El maximo de mascotas debe ser un entero mayor o igual que 0';
  }

  return null;
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
      'amenities',
      'amenityIds',
      'amenitiesIds',
      'pets_allowed',
      'pet_supplement_per_night',
      'aceptaMascotas',
      'permiteMascotas',
      'mascotasPermitidas',
      'petFriendly',
      'petsAllowed',
      'politicaMascotas',
      'politica_mascotas',
      'petPolicy',
      'suplementoMascota',
      'suplementoMascotas',
      'suplemento_mascota',
      'petSupplement',
      'maxMascotas',
      'max_mascotas',
      'maxPets',
      'servicios',
      'amenities',
      'amenidades'
    ];

    const datos = pickAllowed(req.body, allowedFields);
    const servicios = resolverServiciosBody(datos);
    const amenities = resolverAmenitiesBody(datos);
    const politicaMascotas = resolverPoliticaMascotas(datos);
    const errorPolitica = validarPoliticaMascotas(politicaMascotas);

    if (errorPolitica) {
      return res.status(400).json({ message: errorPolitica });
    }
    
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
      ...politicaMascotas,
      amenities: amenities !== undefined ? await resolverAmenityIdsExistentes(amenities) : [],
      servicios: servicios !== undefined ? normalizarServicios(servicios) : []
    });

    const habitacionGuardada = await nuevaHabitacion.save();
    if (amenities !== undefined) {
      await sincronizarRoomAmenities(habitacionGuardada._id, habitacionGuardada.amenities);
    }

    return res.status(201).json(habitacionGuardada);
  } catch (error) {
    try { borrarUploadsLocales(getUploadedPaths(req)); } catch {}
    console.error(error);
    return handleMongoErrors(error, res, 'Error creando la habitación');
  }
};

exports.obtenerHabitaciones = async (req, res) => {
  try {
    const filtro = {};

    if (req.query.pets === 'true') {
      filtro.pets_allowed = true;
    }

    if (req.query.amenities) {
      const amenities = await resolverAmenitiesPorNombres(req.query.amenities);
      const nombresSolicitados = normalizarAmenityIds(req.query.amenities);

      if (amenities.length !== nombresSolicitados.length) {
        return res.json([]);
      }

      const amenityIds = amenities.map(amenity => amenity._id.toString());
      const relaciones = await RoomAmenity.find({ amenityId: { $in: amenityIds } }).lean();
      const contadorPorHabitacion = new Map();

      for (const relacion of relaciones) {
        const roomId = relacion.roomId.toString();
        const amenityId = relacion.amenityId.toString();
        const set = contadorPorHabitacion.get(roomId) ?? new Set();
        set.add(amenityId);
        contadorPorHabitacion.set(roomId, set);
      }

      const habitacionesConTodos = [...contadorPorHabitacion.entries()]
        .filter(([, ids]) => amenityIds.every(id => ids.has(id)))
        .map(([roomId]) => roomId);

      if (!habitacionesConTodos.length) {
        return res.json([]);
      }

      filtro._id = { $in: habitacionesConTodos };
    }

    const habitaciones = await Habitacion.find(filtro).populate('amenities');
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
      'amenities',
      'amenityIds',
      'amenitiesIds',
      'pets_allowed',
      'pet_supplement_per_night',
      'aceptaMascotas',
      'permiteMascotas',
      'mascotasPermitidas',
      'petFriendly',
      'petsAllowed',
      'politicaMascotas',
      'politica_mascotas',
      'petPolicy',
      'suplementoMascota',
      'suplementoMascotas',
      'suplemento_mascota',
      'petSupplement',
      'maxMascotas',
      'max_mascotas',
      'maxPets',
      'servicios',
      'amenities',
      'amenidades',
      'replace'       
    ];

    const datos = pickAllowed(req.body, allowedFields);
    const servicios = resolverServiciosBody(datos);
    const amenities = resolverAmenitiesBody(datos);
    const politicaMascotas = resolverPoliticaMascotas(datos);
    const errorPolitica = validarPoliticaMascotas(politicaMascotas);

    if (errorPolitica) {
      return res.status(400).json({ message: errorPolitica });
    }

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
    if (amenities !== undefined) habitacion.amenities = await resolverAmenityIdsExistentes(amenities);
    if (politicaMascotas.pets_allowed !== undefined) habitacion.pets_allowed = politicaMascotas.pets_allowed;
    if (politicaMascotas.pet_supplement_per_night !== undefined) habitacion.pet_supplement_per_night = politicaMascotas.pet_supplement_per_night;
    if (politicaMascotas.aceptaMascotas !== undefined) habitacion.aceptaMascotas = politicaMascotas.aceptaMascotas;
    if (politicaMascotas.politicaMascotas !== undefined) habitacion.politicaMascotas = politicaMascotas.politicaMascotas;
    if (politicaMascotas.suplementoMascota !== undefined) habitacion.suplementoMascota = politicaMascotas.suplementoMascota;
    if (politicaMascotas.maxMascotas !== undefined) habitacion.maxMascotas = politicaMascotas.maxMascotas;
    if (servicios !== undefined) habitacion.servicios = normalizarServicios(servicios);

    const guardada = await habitacion.save();
    if (amenities !== undefined) {
      await sincronizarRoomAmenities(guardada._id, guardada.amenities);
    }

    return res.json(guardada);

  } catch (error) {
    try { borrarUploadsLocales(getUploadedPaths(req)); } catch {}
    console.error(error);
    return handleMongoErrors(error, res, 'Error actualizando la habitación');
  }
};

exports.obtenerAmenitiesHabitacion = async (req, res) => {
  try {
    const habitacion = await Habitacion.findById(req.params.id)
      .select('numero tipo amenities');

    if (!habitacion) return res.status(404).json({ message: 'Habitacion no encontrada' });

    const relaciones = await RoomAmenity.find({ roomId: habitacion._id }).populate('amenityId');
    return res.json(relaciones.map(relacion => relacion.amenityId));
  } catch (error) {
    console.error(error);
    return handleMongoErrors(error, res, 'Error obteniendo los amenities de la habitacion');
  }
};

exports.asignarAmenitiesHabitacion = async (req, res) => {
  try {
    const amenities = resolverAmenitiesBody(req.body);

    if (amenities === undefined) {
      return res.status(400).json({ message: 'Debes enviar amenities como lista de IDs' });
    }

    const habitacion = await Habitacion.findById(req.params.id);
    if (!habitacion) return res.status(404).json({ message: 'Habitacion no encontrada' });

    habitacion.amenities = await resolverAmenityIdsExistentes(amenities);
    const guardada = await habitacion.save();
    await sincronizarRoomAmenities(guardada._id, guardada.amenities);

    const relaciones = await RoomAmenity.find({ roomId: guardada._id }).populate('amenityId');

    return res.json({
      roomId: guardada._id,
      amenities: relaciones.map(relacion => relacion.amenityId)
    });
  } catch (error) {
    console.error(error);
    return res.status(error.status || 500).json({ message: error.message });
  }
};

exports.obtenerPoliticaMascotasHabitacion = async (req, res) => {
  try {
    const habitacion = await Habitacion.findById(req.params.id)
      .select('numero tipo pets_allowed pet_supplement_per_night aceptaMascotas politicaMascotas suplementoMascota maxMascotas');

    if (!habitacion) return res.status(404).json({ message: 'Habitacion no encontrada' });

    return res.json({
      habitacionId: habitacion._id,
      numero: habitacion.numero,
      tipo: habitacion.tipo,
      pets_allowed: habitacion.pets_allowed,
      pet_supplement_per_night: habitacion.pet_supplement_per_night,
      aceptaMascotas: habitacion.aceptaMascotas,
      politicaMascotas: habitacion.politicaMascotas,
      suplementoMascota: habitacion.suplementoMascota,
      maxMascotas: habitacion.maxMascotas
    });
  } catch (error) {
    console.error(error);
    return handleMongoErrors(error, res, 'Error obteniendo la politica de mascotas');
  }
};

exports.actualizarPoliticaMascotasHabitacion = async (req, res) => {
  try {
    const politicaMascotas = resolverPoliticaMascotas(req.body);
    const errorPolitica = validarPoliticaMascotas(politicaMascotas);

    if (errorPolitica) {
      return res.status(400).json({ message: errorPolitica });
    }

    if (!Object.keys(politicaMascotas).length) {
      return res.status(400).json({ message: 'Debes enviar datos de politica de mascotas' });
    }

    const habitacion = await Habitacion.findById(req.params.id);
    if (!habitacion) return res.status(404).json({ message: 'Habitacion no encontrada' });

    if (politicaMascotas.aceptaMascotas !== undefined) habitacion.aceptaMascotas = politicaMascotas.aceptaMascotas;
    if (politicaMascotas.politicaMascotas !== undefined) habitacion.politicaMascotas = politicaMascotas.politicaMascotas;
    if (politicaMascotas.suplementoMascota !== undefined) habitacion.suplementoMascota = politicaMascotas.suplementoMascota;
    if (politicaMascotas.maxMascotas !== undefined) habitacion.maxMascotas = politicaMascotas.maxMascotas;
    if (politicaMascotas.pets_allowed !== undefined) habitacion.pets_allowed = politicaMascotas.pets_allowed;
    if (politicaMascotas.pet_supplement_per_night !== undefined) habitacion.pet_supplement_per_night = politicaMascotas.pet_supplement_per_night;

    const guardada = await habitacion.save();

    return res.json({
      habitacionId: guardada._id,
      numero: guardada.numero,
      tipo: guardada.tipo,
      pets_allowed: guardada.pets_allowed,
      pet_supplement_per_night: guardada.pet_supplement_per_night,
      aceptaMascotas: guardada.aceptaMascotas,
      politicaMascotas: guardada.politicaMascotas,
      suplementoMascota: guardada.suplementoMascota,
      maxMascotas: guardada.maxMascotas
    });
  } catch (error) {
    console.error(error);
    return handleMongoErrors(error, res, 'Error actualizando la politica de mascotas');
  }
};

exports.obtenerServiciosHabitacion = async (req, res) => {
  try {
    const habitacion = await Habitacion.findById(req.params.id).select('numero tipo servicios');
    if (!habitacion) return res.status(404).json({ message: 'Habitación no encontrada' });

    return res.json({
      habitacionId: habitacion._id,
      numero: habitacion.numero,
      tipo: habitacion.tipo,
      servicios: habitacion.servicios ?? []
    });
  } catch (error) {
    console.error(error);
    return handleMongoErrors(error, res, 'Error obteniendo los servicios de la habitación');
  }
};

exports.reemplazarServiciosHabitacion = async (req, res) => {
  try {
    const servicios = resolverServiciosBody(req.body);
    if (servicios === undefined) {
      return res.status(400).json({ message: 'Debes enviar servicios, amenities o amenidades' });
    }

    const habitacion = await Habitacion.findById(req.params.id);
    if (!habitacion) return res.status(404).json({ message: 'Habitación no encontrada' });

    habitacion.servicios = normalizarServicios(servicios);
    const guardada = await habitacion.save();

    return res.json({
      habitacionId: guardada._id,
      numero: guardada.numero,
      tipo: guardada.tipo,
      servicios: guardada.servicios
    });
  } catch (error) {
    console.error(error);
    return handleMongoErrors(error, res, 'Error actualizando los servicios de la habitación');
  }
};

exports.agregarServiciosHabitacion = async (req, res) => {
  try {
    const servicios = resolverServiciosBody(req.body);
    if (servicios === undefined) {
      return res.status(400).json({ message: 'Debes enviar servicios, amenities o amenidades' });
    }

    const habitacion = await Habitacion.findById(req.params.id);
    if (!habitacion) return res.status(404).json({ message: 'Habitación no encontrada' });

    habitacion.servicios = normalizarServicios([
      ...(habitacion.servicios ?? []),
      ...normalizarServicios(servicios)
    ]);
    const guardada = await habitacion.save();

    return res.json({
      habitacionId: guardada._id,
      numero: guardada.numero,
      tipo: guardada.tipo,
      servicios: guardada.servicios
    });
  } catch (error) {
    console.error(error);
    return handleMongoErrors(error, res, 'Error agregando servicios a la habitación');
  }
};

exports.eliminarServicioHabitacion = async (req, res) => {
  try {
    const servicio = String(req.params.servicio ?? '').trim();
    if (!servicio) return res.status(400).json({ message: 'Servicio inválido' });

    const habitacion = await Habitacion.findById(req.params.id);
    if (!habitacion) return res.status(404).json({ message: 'Habitación no encontrada' });

    const servicioLower = servicio.toLowerCase();
    habitacion.servicios = (habitacion.servicios ?? []).filter(
      item => String(item).trim().toLowerCase() !== servicioLower
    );
    const guardada = await habitacion.save();

    return res.json({
      habitacionId: guardada._id,
      numero: guardada.numero,
      tipo: guardada.tipo,
      servicios: guardada.servicios
    });
  } catch (error) {
    console.error(error);
    return handleMongoErrors(error, res, 'Error eliminando el servicio de la habitación');
  }
};

exports.eliminarServiciosHabitacion = async (req, res) => {
  try {
    const servicios = resolverServiciosBody(req.body);
    if (servicios === undefined) {
      return res.status(400).json({ message: 'Debes enviar servicios, amenities o amenidades' });
    }

    const serviciosAEliminar = normalizarServicios(servicios).map(servicio => servicio.toLowerCase());
    if (!serviciosAEliminar.length) {
      return res.status(400).json({ message: 'Debes enviar al menos un servicio' });
    }

    const habitacion = await Habitacion.findById(req.params.id);
    if (!habitacion) return res.status(404).json({ message: 'Habitación no encontrada' });

    habitacion.servicios = (habitacion.servicios ?? []).filter(
      item => !serviciosAEliminar.includes(String(item).trim().toLowerCase())
    );
    const guardada = await habitacion.save();

    return res.json({
      habitacionId: guardada._id,
      numero: guardada.numero,
      tipo: guardada.tipo,
      servicios: guardada.servicios
    });
  } catch (error) {
    console.error(error);
    return handleMongoErrors(error, res, 'Error eliminando servicios de la habitación');
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
    await RoomAmenity.deleteMany({ roomId: id });
    return res.json({ message: 'Habitación eliminada' });

  } catch (error) {
    console.error(error);
    return handleMongoErrors(error, res, 'Error eliminando la habitación');
  }
};
