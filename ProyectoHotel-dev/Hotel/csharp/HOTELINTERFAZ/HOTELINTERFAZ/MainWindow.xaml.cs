using System.Windows;
using HOTELINTERFAZ.Views;
using HOTELINTERFAZ.Models;
using HOTELINTERFAZ.ViewModels;
using System.Text;
using System.Windows.Controls;
using System.Windows.Data;

namespace HOTELINTERFAZ
{
    public partial class Principal : Window
    {
        private Usuario usuario;
        private readonly HabitacionesViewModel _habitacionesVM = new();
        public Principal(Usuario _usuario)
        {
            InitializeComponent();
            usuario = _usuario;

            lblNombreUsuario.Content = usuario.Nombre;

            if (usuario.Administrador)
            {
                lblAdminEmp.Content = "Administrador";
            }
            else
            {
                lblAdminEmp.Content = "Empleado";
            }
        }

        // ================== CLIENTES ==================
        private void GestionUsuarios_Click(object sender, RoutedEventArgs e)
        {
            contentControl.Content = new ClienteView();
        }

        // ================== EMPLEADOS ==================
        private void GestionEmpleados_Click(object sender, RoutedEventArgs e)
        {
            if (usuario.Administrador)
            {
                contentControl.Content = new EmpleadosView();
            }
            else
            {
                MessageBox.Show("Solo los Administradores tinen acceso a los empleados.");
            }
            
        }

        // ================== HABITACIONES ==================
        private void GestionHabitaciones_Click(object sender, RoutedEventArgs e)
        {
            contentControl.Content = new HabitacionesView(_habitacionesVM);
        }

        // ================== RESERVAS ==================
        private void GestionReservas_Click(object sender, RoutedEventArgs e)
        {
            contentControl.Content = new ReservasView();
        }

        // ================== RESEÑAS ==================
        private void GestionResenas_Click(object sender, RoutedEventArgs e)
        {
            contentControl.Content = new ResenasView();
            
        }

        // ================== LOGOUT ==================
        private void CerrarSesion_Click(object sender, RoutedEventArgs e)
        {
            HOTELINTERFAZ.Views.LogIn login  =  new LogIn();
            login.Show();
            this.Close();
            
        }
    }
}
