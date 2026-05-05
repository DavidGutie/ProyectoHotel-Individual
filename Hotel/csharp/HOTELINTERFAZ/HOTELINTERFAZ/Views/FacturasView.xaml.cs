using HOTELINTERFAZ.ViewModels;
using Microsoft.Win32;
using System.IO;
using System.Windows;
using System.Windows.Controls;

namespace HOTELINTERFAZ.Views
{
    public partial class FacturasView : UserControl
    {
        private readonly FacturasViewModel _vm = new();

        public FacturasView()
        {
            InitializeComponent();
            DataContext = _vm;
        }

        private async void Actualizar_Click(object sender, RoutedEventArgs e)
        {
            await _vm.CargarFacturasAsync();
        }

        private async void Descargar_Click(object sender, RoutedEventArgs e)
        {
            if (_vm.FacturaSeleccionada == null)
            {
                MessageBox.Show("Seleccione una factura primero.");
                return;
            }

            var pdf = await _vm.ObtenerFacturaPdfAsync(_vm.FacturaSeleccionada.ReservaId);

            if (pdf == null || pdf.Length == 0)
            {
                MessageBox.Show("No se pudo obtener la factura PDF.");
                return;
            }

            var saveDialog = new SaveFileDialog
            {
                Filter = "Archivo PDF (*.pdf)|*.pdf",
                FileName = $"factura-{_vm.FacturaSeleccionada.InvoiceNumber}.pdf"
            };

            if (saveDialog.ShowDialog() == true)
            {
                File.WriteAllBytes(saveDialog.FileName, pdf);
                MessageBox.Show("Factura descargada correctamente.");
            }
        }

        private async void Reenviar_Click(object sender, RoutedEventArgs e)
        {
            if (_vm.FacturaSeleccionada == null)
            {
                MessageBox.Show("Seleccione una factura primero.");
                return;
            }

            var exito = await _vm.ReenviarFacturaAsync(_vm.FacturaSeleccionada.ReservaId);
            MessageBox.Show(exito
                ? "Factura marcada para reenvío por email."
                : "No se pudo reenviar la factura.");
        }

        private async void GuardarConfig_Click(object sender, RoutedEventArgs e)
        {
            var exito = await _vm.GuardarConfigAsync();
            MessageBox.Show(exito
                ? "Cabecera de factura guardada."
                : "No se pudo guardar la cabecera.");
        }
    }
}
