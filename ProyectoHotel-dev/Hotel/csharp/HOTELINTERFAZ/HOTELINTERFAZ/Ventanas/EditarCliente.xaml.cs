using System.Windows;
using HOTELINTERFAZ.Models;
using HOTELINTERFAZ.ViewModels;

namespace HOTELINTERFAZ.Ventanas;

public partial class EditarCliente : Window
{
    private ClientesViewModel vm;
    public EditarCliente(ClientesViewModel viewModel, Cliente cliente)
    {
        InitializeComponent();

        vm = viewModel;
        vm.CargarClienteParaEditar(cliente);

        DataContext = vm;
    }
    private void SeleccionarFoto_Click(object sender, RoutedEventArgs e)
    {
        vm.SeleccionarImagen();
    }
    
    private async void GuardarCambios_Click(object sender, RoutedEventArgs e)
    {
        if (!string.IsNullOrWhiteSpace(PasswordBox.Password))
            vm.PasswordNuevo = PasswordBox.Password;
        else
            vm.PasswordNuevo = null;

        await vm.ActualizarCliente();
        Close();
    }
}