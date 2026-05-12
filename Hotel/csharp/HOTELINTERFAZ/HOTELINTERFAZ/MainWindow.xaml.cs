using System.Windows;
using HOTELINTERFAZ.Models;
using HOTELINTERFAZ.ViewModels;
using HOTELINTERFAZ.Views;

namespace HOTELINTERFAZ
{
    public partial class Principal : Window
    {
        private readonly Usuario usuario;
        private readonly HabitacionesViewModel _habitacionesVM = new();

        public Principal(Usuario _usuario)
        {
            InitializeComponent();
            usuario = _usuario;

            lblNombreUsuario.Content = usuario.Nombre;
            lblAdminEmp.Content = usuario.Administrador ? "Administrador" : "Empleado";
        }

        private void GestionUsuarios_Click(object sender, RoutedEventArgs e)
        {
            contentControl.Content = new ClienteView();
        }

        private void GestionEmpleados_Click(object sender, RoutedEventArgs e)
        {
            if (usuario.Administrador)
            {
                contentControl.Content = new EmpleadosView();
            }
            else
            {
                MessageBox.Show("Solo los administradores tienen acceso a los empleados.");
            }
        }

        private void GestionHabitaciones_Click(object sender, RoutedEventArgs e)
        {
            contentControl.Content = new HabitacionesView(_habitacionesVM);
        }

        private void GestionReservas_Click(object sender, RoutedEventArgs e)
        {
            contentControl.Content = new ReservasView();
        }

        private void GestionFacturas_Click(object sender, RoutedEventArgs e)
        {
            contentControl.Content = new FacturasView();
        }

        private void GestionResenas_Click(object sender, RoutedEventArgs e)
        {
            contentControl.Content = new ResenasView();
        }

        private void CerrarSesion_Click(object sender, RoutedEventArgs e)
        {
            var login = new LogIn();
            login.Show();
            Close();
        }
    }
}
