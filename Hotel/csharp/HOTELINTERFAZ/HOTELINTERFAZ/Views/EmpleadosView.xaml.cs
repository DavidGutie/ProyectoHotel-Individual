using HOTELINTERFAZ.ViewModels;
using System;
using System.Collections.Generic;
using System.ComponentModel;
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
using HOTELINTERFAZ.Models;
using HOTELINTERFAZ.Ventanas;
using System.Windows.Controls;


namespace HOTELINTERFAZ.Views
{
    /// <summary>
    /// Lógica de interacción para EmpleadosView.xaml
    /// </summary>
    public partial class EmpleadosView : UserControl
    {
        private readonly EmpleadosViewModel _vm;
        private readonly ICollectionView _view;
        public EmpleadosView()
        {
            InitializeComponent();

            _vm = new EmpleadosViewModel();
            DataContext = _vm;

            _view = CollectionViewSource.GetDefaultView(DgEmpleados.ItemsSource);
            
        }

        private void Editar_Empleado_Click(object sender, RoutedEventArgs e)
        {
            if (DgEmpleados.SelectedItem is Empleado emp)
            {
                var ventana = new EditarEmpleado(_vm, emp);
                ventana.ShowDialog();
            }
        }


        private void Nuevo_Empleado_Click(object sender, RoutedEventArgs e)
        {
            NuevoEmpleado ventana = new NuevoEmpleado();
            ventana.ShowDialog();
        }

        private async void Eliminar_Empleado_Click(object sender, RoutedEventArgs e)
        {
            await _vm.EliminarEmpleado();
        }
        
        private void TxtBuscar_TextChanged(object sender, TextChangedEventArgs e)
        {
            _vm.BuscarPorDni(TxtBuscar.Text);
        }


        private void DgEmpleados_RowEditEnding(object sender, DataGridRowEditEndingEventArgs e)
        {

        }
    }
}
