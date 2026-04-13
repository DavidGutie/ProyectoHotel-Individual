const mongoose = require('mongoose');


//const URI = 'mongodb://localhost:27017/Hotel'; 
const URI = 'mongodb://localhost:27017/HotelPereMaria'; // tu base de datos
mongoose.connect(URI) 
  .then(() => console.log('Conectado a la base de datos'))
  .catch(err => console.log('Error de conexión:', err));

module.exports = mongoose;
// const mongoose = require('mongoose');

// const URI = process.env.MONGO_URI;

// if (!URI) {
//   throw new Error("Falta MONGO_URI en el archivo .env");
// }

// mongoose.connect(URI)
//   .then(() => console.log('Conectado a MongoDB Atlas'))
//   .catch(err => console.log('Error de conexión:', err));
//   mongoose.connection.once("open", () => {
//   console.log("DB name:", mongoose.connection.name);
// });

// module.exports = mongoose;