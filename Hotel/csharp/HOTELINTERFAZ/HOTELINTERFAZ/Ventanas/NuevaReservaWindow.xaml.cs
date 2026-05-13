using HOTELINTERFAZ.Models;
using HOTELINTERFAZ.ViewModels;
using System;
using System.Collections.ObjectModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;

namespace HOTELINTERFAZ.Ventanas
{
    public partial class NuevaReservaWindow : Window
    {
        private readonly ReservasViewModel _reservasVM;
        private readonly HabitacionesViewModel _habitacionesVM;
        private readonly ClientesViewModel _clientesVM;
        private readonly Reserva _reservaEditar;
        private readonly bool _modoEdicion;

        public ObservableCollection<Habitacion> HabitacionesDisponibles { get; set; } = new();
        public Habitacion HabitacionSeleccionada { get; set; }

        public NuevaReservaWindow(
            ReservasViewModel reservasVM,
            HabitacionesViewModel habitacionesVM,
            ClientesViewModel clientesVM)
            : this(reservasVM, habitacionesVM, clientesVM, null)
        {
        }

        public NuevaReservaWindow(
            ReservasViewModel reservasVM,
            HabitacionesViewModel habitacionesVM,
            ClientesViewModel clientesVM,
            Reserva reservaEditar)
        {
            InitializeComponent();

            _reservasVM = reservasVM;
            _habitacionesVM = habitacionesVM;
            _clientesVM = clientesVM;
            _reservaEditar = reservaEditar;
            _modoEdicion = reservaEditar != null;

            DataContext = this;

            if (_modoEdicion)
            {
                Title = "Modificar Reserva";
                GuardarButton.Content = "Guardar cambios";
                FechaEntradaPicker.DisplayDateStart = null;
                FechaSalidaPicker.DisplayDateStart = null;
                DniTextBox.IsEnabled = false;
                DniTextBox.Text = _reservaEditar.Cliente?.Dni ?? "";
                FechaEntradaPicker.SelectedDate = _reservaEditar.FechaEntrada.Date;
                FechaSalidaPicker.SelectedDate = _reservaEditar.FechaSalida.Date;
                TextBoxPersonas.Text = _reservaEditar.Personas.ToString();
            }

            FechaEntradaPicker.SelectedDateChanged += Fechas_SelectedDateChanged;
            FechaSalidaPicker.SelectedDateChanged += Fechas_SelectedDateChanged;

            CargarDatosAsync();
        }

        private async void CargarDatosAsync()
        {
            await _habitacionesVM.CargarHabitaciones();
            await _reservasVM.CargarReservasAsync();
            await _clientesVM.CargarClientesAsync();

            ActualizarHabitacionesDisponibles();

            if (_modoEdicion)
            {
                HabitacionSeleccionada = HabitacionesDisponibles
                    .FirstOrDefault(h => h.Id == _reservaEditar.HabitacionId);
                ComboBoxHabitacion.SelectedItem = HabitacionSeleccionada;
            }
        }

        private void Fechas_SelectedDateChanged(object sender, SelectionChangedEventArgs e)
        {
            ActualizarHabitacionesDisponibles();
        }

        private void ActualizarHabitacionesDisponibles()
        {
            HabitacionesDisponibles.Clear();

            if (!FechaEntradaPicker.SelectedDate.HasValue || !FechaSalidaPicker.SelectedDate.HasValue)
                return;

            DateTime entrada = FechaEntradaPicker.SelectedDate.Value.Date;
            DateTime salida = FechaSalidaPicker.SelectedDate.Value.Date;

            if (salida <= entrada)
                return;

            var disponibles = _habitacionesVM.Habitaciones
                .Where(h => !_reservasVM.Reservas.Any(r =>
                    (!_modoEdicion || r.Id != _reservaEditar.Id) &&
                    !r.Cancelacion &&
                    r.HabitacionId == h.Id &&
                    !(salida <= r.FechaEntrada || entrada >= r.FechaSalida)
                ));

            foreach (var h in disponibles)
                HabitacionesDisponibles.Add(h);
        }

        private async void Crear_Click(object sender, RoutedEventArgs e)
        {
            if (!FechaEntradaPicker.SelectedDate.HasValue || !FechaSalidaPicker.SelectedDate.HasValue)
            {
                MessageBox.Show("Selecciona las fechas");
                return;
            }

            if (!int.TryParse(TextBoxPersonas.Text, out int personas))
            {
                MessageBox.Show("Número de personas inválido");
                return;
            }

            if (ComboBoxHabitacion.SelectedItem is not Habitacion habitacion)
            {
                MessageBox.Show("Selecciona una habitación");
                return;
            }

            string dni = DniTextBox.Text.Trim();
            if (string.IsNullOrWhiteSpace(dni))
            {
                MessageBox.Show("Ingresa el DNI del cliente");
                return;
            }
            var clienteExistente = _clientesVM.ClientesNoReducido
                .FirstOrDefault(c => c.DNI.Trim().Equals(dni.ToUpper(), StringComparison.OrdinalIgnoreCase));

            if (clienteExistente == null)
            {
                MessageBox.Show("El cliente no está registrado");
                return;
            }

            int dias = (FechaSalidaPicker.SelectedDate.Value.Date - FechaEntradaPicker.SelectedDate.Value.Date).Days;
            if (dias <= 0)
            {
                MessageBox.Show("La fecha de salida debe ser posterior a la de entrada");
                return;
            }
            decimal precioTotal = Convert.ToDecimal(habitacion.PrecioNoche) * dias;

            var reserva = new Reserva
            {
                Id = _modoEdicion ? _reservaEditar.Id : Guid.NewGuid().ToString(),
                ClienteId = clienteExistente.Id,
                HabitacionId = habitacion.Id,
                FechaEntrada = FechaEntradaPicker.SelectedDate.Value.Date,
                FechaSalida = FechaSalidaPicker.SelectedDate.Value.Date,
                Personas = Math.Min(personas, habitacion.MaxOcupantes),
                PrecioTotal = (double)precioTotal, // tu modelo usa double
                Cancelacion = false
            };

            bool exito = _modoEdicion
                ? await _reservasVM.ActualizarReservaAsync(reserva)
                : await _reservasVM.AgregarReservaAsync(reserva);

            if (exito)
            {
                MessageBox.Show(_modoEdicion
                    ? "Reserva modificada correctamente"
                    : "Reserva creada correctamente");
                Close();
            }
            else
            {
                MessageBox.Show(_modoEdicion
                    ? "Error modificando la reserva"
                    : "Error creando la reserva");
            }
        }

        private void Cancelar_Click(object sender, RoutedEventArgs e)
        {
            Close();
        }
    }
}
