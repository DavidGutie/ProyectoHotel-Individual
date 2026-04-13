using HOTELINTERFAZ.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;

namespace HOTELINTERFAZ.Views
{
    public partial class ResenasView : UserControl
    {
        public ResenasView()
        {
            InitializeComponent();
        }

        private async void Eliminar_Click(object sender, RoutedEventArgs e)
        {
            if (DataContext is ResenasViewModel vm)
            {
                if (vm.ResenaSeleccionada == null)
                {
                    MessageBox.Show("Por favor, seleccione una reseña para eliminar.");
                    return;
                }

                var confirm = MessageBox.Show(
                    "¿Estás seguro de que deseas eliminar esta reseña de forma permanente?",
                    "Confirmar Eliminación",
                    MessageBoxButton.YesNo,
                    MessageBoxImage.Warning);

                if (confirm == MessageBoxResult.Yes)
                {
                    bool exito = await vm.EliminarResenaAsync(vm.ResenaSeleccionada.Id);

                    if (exito)
                    {
                        MessageBox.Show("Reseña eliminada correctamente.");
                    }
                    else
                    {
                        MessageBox.Show("Error al intentar eliminar la reseña del servidor.");
                    }
                }
            }
        }
    }
}