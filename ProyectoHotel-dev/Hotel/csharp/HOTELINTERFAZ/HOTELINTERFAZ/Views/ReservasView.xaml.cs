using HOTELINTERFAZ.Ventanas;
using HOTELINTERFAZ.ViewModels;
using System.ComponentModel;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;

namespace HOTELINTERFAZ.Views
{
    public partial class ReservasView : UserControl
    {
        private readonly ReservasViewModel _reservasVM = new ReservasViewModel();
        private readonly HabitacionesViewModel _habitacionesVM = new HabitacionesViewModel();
        private readonly ClientesViewModel _clientesVM = new ClientesViewModel();
        private readonly ICollectionView _view;

        public ReservasView()
        {
            InitializeComponent();
            DataContext = _reservasVM;

            _view = CollectionViewSource.GetDefaultView(_reservasVM.Reservas);
            _view.Filter = FiltrarPorDni;
        }

        private bool FiltrarPorDni(object obj)
        {
            if (obj is not Models.Reserva r) return false;

            var q = TxtBuscar?.Text?.Trim().ToLower() ?? "";
            if (string.IsNullOrWhiteSpace(q)) return true;

            var dni = r.Cliente?.Dni?.Trim().ToLower() ?? "";
            return dni.Contains(q);
        }

        private void TxtBuscar_TextChanged(object sender, TextChangedEventArgs e)
        {
            _view.Refresh();
        }

        private void Buscar_Click(object sender, RoutedEventArgs e)
        {
            _view.Refresh();
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

            var confirm = MessageBox.Show(
                "¿Desea cancelar esta reserva?",
                "Confirmar cancelación",
                MessageBoxButton.YesNo,
                MessageBoxImage.Warning
            );

            if (confirm != MessageBoxResult.Yes)
                return;

            bool exito = await _reservasVM.CancelarReservaAsync(_reservasVM.ReservaSeleccionada.Id);

            MessageBox.Show(exito ? "Reserva cancelada correctamente."
                                  : "Error al cancelar la reserva.");
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

            var confirm = MessageBox.Show(
                "¿Eliminar definitivamente la reserva?",
                "Confirmar",
                MessageBoxButton.YesNo
            );

            if (confirm == MessageBoxResult.Yes)
            {
                await _reservasVM.EliminarReservaAsync(_reservasVM.ReservaSeleccionada.Id);
            }
        }
    }
}