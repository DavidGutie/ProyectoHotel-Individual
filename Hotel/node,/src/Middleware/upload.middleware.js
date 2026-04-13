const multer = require('multer');
const path = require('path');
const fs = require("fs");


const uploadRuta = "uploads/"; 

if (!fs.existsSync(uploadRuta)) {
  fs.mkdirSync(uploadRuta);
}

const storage = multer.diskStorage({

    destination: (req, file, cb) => {
        cb(null, uploadRuta);
    },

    filename:(req, file, cb) => {
        const nombreUnico = Date.now() + path.extname(file.originalname);
        cb(null, nombreUnico);
    }

});

const fileFilter = (req, file, cb) => {

    const formatosPermitidos = /jpg|jpeg|png|webp/;

    const extension = formatosPermitidos.test(
        path.extname(file.originalname).toLowerCase()
    );

    const mime = formatosPermitidos.test(file.mimetype);

    if (extension && mime) {
        cb(null, true);
    } else {
        cb(new Error("Formato de imagen no permitido"));
    }
};

const upload = multer({
    storage,
    fileFilter
});

module.exports = upload ;

