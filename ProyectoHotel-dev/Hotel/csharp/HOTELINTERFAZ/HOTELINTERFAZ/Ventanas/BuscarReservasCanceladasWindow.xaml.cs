using HOTELINTERFAZ.Models;
using HOTELINTERFAZ.ViewModels;
using System.Collections.ObjectModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;

namespace HOTELINTERFAZ.Ventanas
{
    public partial class BuscarReservasCanceladasWindow : Window
    {
        private readonly ReservasViewModel _vm;
        private ObservableCollection<Reserva> _reservasFiltradas = new();

        public BuscarReservasCanceladasWindow(ReservasViewModel vm)
        {
            InitializeComponent();
            _vm = vm;
            dgReservas.ItemsSource = _reservasFiltradas;
        }

        private async void Buscar_Click(object sender, RoutedEventArgs e)
        {
            string dni = txtDni.Text.Trim();

            var reservas = _vm.Reservas
                .Where(r => r.Cancelacion &&
                            r.Cliente != null &&
                            r.Cliente.Dni == dni);

            _reservasFiltradas.Clear();
            foreach (var r in reservas)
                _reservasFiltradas.Add(r);

            if (!_reservasFiltradas.Any())
                MessageBox.Show("No hay reservas canceladas para ese DNI.");
        }


        private async void Eliminar_Click(object sender, RoutedEventArgs e)
        {
            if (sender is Button btn && btn.DataContext is Reserva reserva)
            {
                var confirm = MessageBox.Show(
                    "¿Eliminar definitivamente esta reserva?",
                    "Confirmar",
                    MessageBoxButton.YesNo);

                if (confirm == MessageBoxResult.Yes)
                {
                    await _vm.EliminarReservaAsync(id: reserva.Id);
                    _reservasFiltradas.Remove(reserva);
                }
            }
        }

    }
}
