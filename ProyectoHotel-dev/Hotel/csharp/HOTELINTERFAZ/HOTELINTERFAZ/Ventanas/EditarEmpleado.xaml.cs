using System.Windows;
using HOTELINTERFAZ.Models;
using HOTELINTERFAZ.ViewModels;

namespace HOTELINTERFAZ.Ventanas;

public partial class EditarEmpleado : Window
{
    private EmpleadosViewModel vm;

    public EditarEmpleado(EmpleadosViewModel viewModel, Empleado empleado)
    {
        InitializeComponent();

        vm = viewModel;
        vm.CargarEmpleadoParaEditar(empleado);

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

        await vm.ActualizarEmpleado();
        Close();
    }
}
