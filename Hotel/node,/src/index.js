//require('dotenv').config();
const express = require('express');
const cors = require('cors'); 
const app = express();
const path = require('path');
const multer = require("multer");
const PORT = 3000;

//app.set('views', path.join(__dirname, 'views'));

// Conexión a DB (usa tu archivo db.js)
require('./db'); 

// Middlewares - ¡IMPORTANTE!
app.use(cors()); 
app.use(express.json()); 
//app.use("/uploads", express.static("uploads"));

// //Error que salta en caso de que el formato de imagen no sea permitido o haya un error con multer
// app.use((err, req, res, next) => {
//     if (err instanceof multer.MulterError || err.message.includes("Formato")) {
//         return res.status(400).json({ msg: err.message });
//     }

//     next(err);
// });

// Rutas
const reservaRoutes = require('./reserva/reserva.routes');
const clienteRoutes = require('./cliente/cliente.routes');
const empleadoRoutes = require('./empleado/empleado.routes');
const usuarioRoutes = require('./usuario/usuario.routes');
const authRoutes = require('./auth/auth.routes');
const resenaRoutes = require('./reseña/reseña.routes');
const habitacionRoutes = require('./habitacion/habitacion.routes');
const facturaRoutes = require('./factura/factura.routes');

app.use('/reservas', reservaRoutes);
app.use('/cliente', clienteRoutes);
app.use('/empleado', empleadoRoutes);
app.use('/usuario', usuarioRoutes);
app.use('/auth', authRoutes);
app.use('/resenas', resenaRoutes);
app.use('/habitaciones', habitacionRoutes);
app.use('/facturas', facturaRoutes);