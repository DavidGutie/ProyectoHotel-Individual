using System.ComponentModel;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using HOTELINTERFAZ.Models;
using HOTELINTERFAZ.Ventanas;
using HOTELINTERFAZ.ViewModels;

namespace HOTELINTERFAZ.Views;

public partial class ClienteView : UserControl
{
    private readonly ClientesViewModel _vm;
    private readonly ICollectionView _view;
    public ClienteView()
    {
        InitializeComponent();
        _vm = new ClientesViewModel();
        DataContext = _vm;

        _view = _vm.ClienteView;
    }
    private void Editar_Cliente_Click(object sender, RoutedEventArgs e)
    {
        if (DgClientes.SelectedItem is Cliente emp)
        {
            var ventana = new EditarCliente(_vm, emp);
            ventana.ShowDialog();
        }
    }


    private void Nuevo_Cliente_Click(object sender, RoutedEventArgs e)
    {
        NuevoCliente ventana = new NuevoCliente();
        ventana.ShowDialog();
    }

    private async void Eliminar_Cliente_Click(object sender, RoutedEventArgs e)
    {
        await _vm.EliminarCliente();
    }


    private void TxtBuscar_TextChanged(object sender, TextChangedEventArgs e)
    {
        _vm.BuscarPorDni(TxtBuscar.Text);
    }


    private void DgClientes_RowEditEnding(object sender, DataGridRowEditEndingEventArgs e)
    {

    }
}