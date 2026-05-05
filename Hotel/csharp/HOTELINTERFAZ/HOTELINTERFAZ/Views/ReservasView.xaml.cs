using HOTELINTERFAZ.Models;
using HOTELINTERFAZ.Ventanas;
using HOTELINTERFAZ.ViewModels;
using Microsoft.Win32;
using System.Collections.Generic;
using System.IO;
using System.Windows;
using System.Windows.Controls;
using System.Linq;

namespace HOTELINTERFAZ.Views
{
    public partial class ReservasView : UserControl
    {
        private readonly ReservasViewModel _reservasVM = new ReservasViewModel();
        private readonly HabitacionesViewModel _habitacionesVM = new HabitacionesViewModel();
        private readonly ClientesViewModel _clientesVM = new ClientesViewModel();

        public ReservasView()
        {
            InitializeComponent();
            DataContext = _reservasVM;
        }

        private void Buscar_Click(object sender, RoutedEventArgs e)
        {
            _reservasVM.ReservasView.Refresh();
        }

        private void NuevaReserva_Click(object sender, RoutedEventArgs e)
        {
            var nuevaReservaWindow = new NuevaReservaWindow(_reservasVM, _habitacionesVM, _clientesVM);
            nuevaReservaWindow.ShowDialog();
        }

        private async void Cancelar_Click(object sender, RoutedEventArgs e)
        {
            if (_reservasVM.ReservaSeleccionada == null)
            {
                var ventanaCancelar = new BuscarReservasParaCancelarWindow(_reservasVM);
                ventanaCancelar.ShowDialog();
                return;
            }

            if (_reservasVM.ReservaSeleccionada.Cancelacion)
            {
                MessageBox.Show("La reserva ya está cancelada.");
                return;
            }

            var idReserva = _reservasVM.ReservaSeleccionada.Id;

            var confirm = MessageBox.Show(
                "¿Desea cancelar esta reserva?",
                "Confirmar cancelación",
                MessageBoxButton.YesNo,
                MessageBoxImage.Warning
            );

            if (confirm != MessageBoxResult.Yes)
                return;

            bool exito = await _reservasVM.CancelarReservaAsync(idReserva);

            if (exito)
            {
                _reservasVM.MostrarReservaCanceladaTemporalmente(idReserva);
                _reservasVM.ReservaSeleccionada = _reservasVM.Reservas.FirstOrDefault(r => r.Id == idReserva);

                MessageBox.Show("Reserva cancelada correctamente.");
            }
            else
            {
                MessageBox.Show("Error al cancelar la reserva.");
            }
        }

        private async void Eliminar_Click(object sender, RoutedEventArgs e)
        {
            if (_reservasVM.ReservaSeleccionada == null)
            {
                var buscarReservasCanceladasWindow = new BuscarReservasCanceladasWindow(_reservasVM);
                buscarReservasCanceladasWindow.ShowDialog();
                return;
            }

            if (!_reservasVM.ReservaSeleccionada.Cancelacion)
            {
                MessageBox.Show("Solo se pueden eliminar reservas canceladas.");
                return;
            }

            var idReserva = _reservasVM.ReservaSeleccionada.Id;

            var confirm = MessageBox.Show(
                "¿Eliminar definitivamente la reserva?",
                "Confirmar",
                MessageBoxButton.YesNo,
                MessageBoxImage.Warning
            );

            if (confirm == MessageBoxResult.Yes)
            {
                bool exito = await _reservasVM.EliminarReservaAsync(idReserva);

                if (exito)
                {
                    if (_reservasVM.ReservaVisibleTemporalmenteId == idReserva)
                        _reservasVM.LimpiarReservaVisibleTemporalmente();

                    MessageBox.Show("Reserva eliminada correctamente.");
                }
                else
                {
                    MessageBox.Show("Error al eliminar la reserva.");
                }
            }
        }

        private async void Historial_Click(object sender, RoutedEventArgs e)
        {
            if (_reservasVM.ReservaSeleccionada == null)
            {
                MessageBox.Show("Seleccione una reserva primero.");
                return;
            }

            List<ReservaAudit> historial = await _reservasVM.ObtenerHistorialReservaAsync(_reservasVM.ReservaSeleccionada.Id);

            if (historial == null || historial.Count == 0)
            {
                MessageBox.Show("No se encontró historial para esta reserva.");
                return;
            }

            var ventana = new HistorialReservaWindow(historial, _reservasVM.ReservaSeleccionada.Id);
            ventana.ShowDialog();
        }

        private async void DescargarFactura_Click(object sender, RoutedEventArgs e)
        {
            if (_reservasVM.ReservaSeleccionada == null)
            {
                MessageBox.Show("Seleccione una reserva primero.");
                return;
            }

            byte[] pdf = await _reservasVM.ObtenerFacturaPdfAsync(_reservasVM.ReservaSeleccionada.Id);

            if (pdf == null || pdf.Length == 0)
            {
                MessageBox.Show("No se pudo obtener la factura PDF.");
                return;
            }

            string numeroFactura = string.IsNullOrWhiteSpace(_reservasVM.ReservaSeleccionada.InvoiceNumber)
                ? _reservasVM.ReservaSeleccionada.Id
                : _reservasVM.ReservaSeleccionada.InvoiceNumber;

            var saveDialog = new SaveFileDialog
            {
                Filter = "Archivo PDF (*.pdf)|*.pdf",
                FileName = $"factura-{numeroFactura}.pdf"
            };

            if (saveDialog.ShowDialog() == true)
            {
                File.WriteAllBytes(saveDialog.FileName, pdf);
                MessageBox.Show("Factura descargada correctamente.");

                await _reservasVM.CargarReservasAsync();
            }
        }
    }
}