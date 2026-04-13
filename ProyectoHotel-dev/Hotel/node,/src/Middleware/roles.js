exports.soloClientes = (req, res, next) => {
  if (req.usuario.tipoUsuario !== 'Cliente') {
    return res.status(403).json({ msg: 'Solo clientes' });
  }
  next();
};

exports.soloEmpleados = (req, res, next) => {
  if (req.usuario.tipoUsuario !== 'Empleado') {
    return res.status(403).json({ msg: 'Solo empleados' });
  }
  next();
};