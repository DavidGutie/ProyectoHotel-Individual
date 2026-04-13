using HOTELINTERFAZ.Models;
using HOTELINTERFAZ.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;

namespace HOTELINTERFAZ.Views
{
    /// <summary>
    /// Lógica de interacción para LogIn.xaml
    /// </summary>
    public partial class LogIn : Window
    {
        public LogIn()
        {
            InitializeComponent();
        }

        private async void LogIn_Button(object sender, RoutedEventArgs e)
        {
            String email = txtEmailLogIn.Text;
            String password = txtPassLogIn.Password;
            try
            {
                Autenticacion auth = new Autenticacion();

                var resultado = await auth.Login(
                    email,
                    password
                );

                if (resultado.Usuario.TipoUsuario != "Empleado")
                {
                    MessageBox.Show("Solo empleados pueden acceder");
                    return;
                }

                SessionManager.Token = resultado.Token;
                SessionManager.UsuarioActual = resultado.Usuario;

                HOTELINTERFAZ.Principal main = new HOTELINTERFAZ.Principal(SessionManager.UsuarioActual);
                main.Show();
                this.Close();
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
        }
        
        private void CBMostrarContra_Checked(object sender, RoutedEventArgs e)
        {
            txtPassVisible.Text = txtPassLogIn.Password;

            txtPassVisible.Visibility = Visibility.Visible;
            txtPassLogIn.Visibility = Visibility.Collapsed;
        }

        private void CBMostrarContra_Unchecked(object sender, RoutedEventArgs e)
        {
            txtPassLogIn.Password = txtPassVisible.Text;

            txtPassVisible.Visibility = Visibility.Collapsed;
            txtPassLogIn.Visibility = Visibility.Visible;
        }
    }
}
