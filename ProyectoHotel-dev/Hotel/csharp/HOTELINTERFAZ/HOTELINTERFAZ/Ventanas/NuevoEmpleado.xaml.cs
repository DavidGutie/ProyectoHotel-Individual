using System.Windows;
using HOTELINTERFAZ.ViewModels;




namespace HOTELINTERFAZ.Ventanas;

public partial class NuevoEmpleado : Window
{
    private EmpleadosViewModel vm;

    public NuevoEmpleado()
    {
        InitializeComponent();
        vm = new EmpleadosViewModel();
        DataContext = vm;
    }
    
    private void SeleccionarFoto_Click(object sender, RoutedEventArgs e)
    {
        vm.SeleccionarImagen();
    }
    
    private async void CrearEmpleado_Click(object sender, RoutedEventArgs e)
    {
        vm.PasswordNuevo = PasswordBox.Password;

        await vm.CrearEmpleado();

        Close();
    }
    
}