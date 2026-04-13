using System.Windows;
using HOTELINTERFAZ.ViewModels;

namespace HOTELINTERFAZ.Ventanas;

public partial class NuevoCliente : Window
{
    
    private ClientesViewModel vm;
    
    public NuevoCliente()
    {
        InitializeComponent();
        
        vm = new ClientesViewModel();
        DataContext = vm;
    }
    
    private void SeleccionarFoto_Click(object sender, RoutedEventArgs e)
    {
        vm.SeleccionarImagen();
    }
    
    private async void CrearCliente_Click(object sender, RoutedEventArgs e)
    {
        vm.PasswordNuevo = PasswordBox.Password;

        await vm.CrearCliente();

        Close();
    }
}