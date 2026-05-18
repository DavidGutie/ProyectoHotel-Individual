const Amenity = require('./amenity.models');
const Habitacion = require('../habitacion/habitacion.models');
const RoomAmenity = require('./roomAmenity.models');

const normalizarAmenity = (body) => ({
  name: body.name !== undefined ? String(body.name).trim() : undefined,
  icon: body.icon !== undefined ? String(body.icon).trim() : undefined,
  category: body.category !== undefined ? String(body.category).trim() : undefined
});

const quitarUndefined = (obj) => Object.fromEntries(
  Object.entries(obj).filter(([, value]) => value !== undefined)
);

const validarAmenity = (amenity, parcial = false) => {
  if ((!parcial || amenity.name !== undefined) && !amenity.name) {
    return 'name es obligatorio';
  }

  if ((!parcial || amenity.category !== undefined) && !amenity.category) {
    return 'category es obligatorio';
  }

  return null;
};

const manejarError = (error, res, mensaje) => {
  if (error.code === 11000) {
    return res.status(409).json({ msg: 'Ya existe un amenity con ese name' });
  }

  if (error.name === 'CastError') {
    return res.status(400).json({ msg: 'ID invalido' });
  }

  return res.status(500).json({ msg: mensaje, error: error.message });
};

exports.obtenerAmenities = async (req, res) => {
  try {
    const filtro = {};
    if (req.query.category) filtro.category = String(req.query.category).trim();

    const amenities = await Amenity.find(filtro).sort({ category: 1, name: 1 });
    return res.json(amenities);
  } catch (error) {
    return manejarError(error, res, 'Error obteniendo amenities');
  }
};

exports.obtenerAmenityPorId = async (req, res) => {
  try {
    const amenity = await Amenity.findById(req.params.id);

    if (!amenity) {
      return res.status(404).json({ msg: 'Amenity no encontrado' });
    }

    return res.json(amenity);
  } catch (error) {
    return manejarError(error, res, 'Error obteniendo amenity');
  }
};

exports.crearAmenity = async (req, res) => {
  try {
    const amenity = normalizarAmenity(req.body);
    const errorValidacion = validarAmenity(amenity);

    if (errorValidacion) {
      return res.status(400).json({ msg: errorValidacion });
    }

    const guardado = await new Amenity(amenity).save();
    return res.status(201).json(guardado);
  } catch (error) {
    return manejarError(error, res, 'Error creando amenity');
  }
};

exports.actualizarAmenity = async (req, res) => {
  try {
    const amenity = quitarUndefined(normalizarAmenity(req.body));
    const errorValidacion = validarAmenity(amenity, true);

    if (errorValidacion) {
      return res.status(400).json({ msg: errorValidacion });
    }

    const actualizado = await Amenity.findByIdAndUpdate(
      req.params.id,
      { $set: amenity },
      { new: true, runValidators: true }
    );

    if (!actualizado) {
      return res.status(404).json({ msg: 'Amenity no encontrado' });
    }

    return res.json(actualizado);
  } catch (error) {
    return manejarError(error, res, 'Error actualizando amenity');
  }
};

exports.eliminarAmenity = async (req, res) => {
  try {
    const eliminado = await Amenity.findByIdAndDelete(req.params.id);

    if (!eliminado) {
      return res.status(404).json({ msg: 'Amenity no encontrado' });
    }

    await RoomAmenity.deleteMany({ amenityId: eliminado._id });
    await Habitacion.updateMany(
      { amenities: eliminado._id },
      { $pull: { amenities: eliminado._id } }
    );

    return res.json({ msg: 'Amenity eliminado correctamente' });
  } catch (error) {
    return manejarError(error, res, 'Error eliminando amenity');
  }
};
