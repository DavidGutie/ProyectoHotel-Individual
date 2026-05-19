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
        public Cliente ClienteSeleccionado { get; set; }
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
                DniComboBox.IsEnabled = false;
                DniComboBox.Text = _reservaEditar.Cliente?.Dni ?? "";
                FechaEntradaPicker.SelectedDate = _reservaEditar.FechaEntrada.Date;
                FechaSalidaPicker.SelectedDate = _reservaEditar.FechaSalida.Date;
                TextBoxPersonas.Text = _reservaEditar.Personas.ToString();
                CheckMascota.IsChecked = _reservaEditar.IncluyeMascota;
                TextBoxMascotas.Text = Math.Max(0, _reservaEditar.Mascotas).ToString();
            }

            FechaEntradaPicker.SelectedDateChanged += Fechas_SelectedDateChanged;
            FechaSalidaPicker.SelectedDateChanged += Fechas_SelectedDateChanged;

            CargarDatosAsync();
        }

        private async void CargarDatosAsync()
        {
            try
            {
                await _clientesVM.CargarClientesAsync();
                DniComboBox.ItemsSource = _clientesVM.ClientesNoReducido;

                await _habitacionesVM.CargarHabitaciones();
                await _reservasVM.CargarReservasAsync();

                ActualizarHabitacionesDisponibles();

                if (_modoEdicion)
                {
                    ClienteSeleccionado = _clientesVM.ClientesNoReducido
                        .FirstOrDefault(c => string.Equals(c.Id, _reservaEditar.ClienteId, StringComparison.OrdinalIgnoreCase));
                    DniComboBox.SelectedItem = ClienteSeleccionado;
                    if (ClienteSeleccionado != null)
                        DniComboBox.Text = ClienteSeleccionado.DNI;

                    HabitacionSeleccionada = HabitacionesDisponibles
                        .FirstOrDefault(h => h.Id == _reservaEditar.HabitacionId);
                    ComboBoxHabitacion.SelectedItem = HabitacionSeleccionada;
                    ActualizarEstadoMascotas();
                }

                if (_clientesVM.ClientesNoReducido.Count == 0)
                {
                    MessageBox.Show("No hay clientes registrados para seleccionar un DNI.");
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("No se pudieron cargar los datos de la reserva: " + ex.Message);
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
            {
                ComboBoxHabitacion.SelectedItem = null;
                ActualizarEstadoMascotas();
                return;
            }

            var disponibles = _habitacionesVM.Habitaciones
                .Where(h => !_reservasVM.Reservas.Any(r =>
                    (!_modoEdicion || r.Id != _reservaEditar.Id) &&
                    !r.Cancelacion &&
                    r.HabitacionId == h.Id &&
                    !(salida <= r.FechaEntrada || entrada >= r.FechaSalida)
                ));

            foreach (var h in disponibles)
                HabitacionesDisponibles.Add(h);

            ActualizarEstadoMascotas();
        }

        private void ComboBoxHabitacion_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            ActualizarEstadoMascotas();
        }

        private void Mascota_Changed(object sender, RoutedEventArgs e)
        {
            ActualizarEstadoMascotas();
        }

        private void ActualizarEstadoMascotas()
        {
            if (ComboBoxHabitacion?.SelectedItem is not Habitacion habitacion)
            {
                if (TextBoxMascotas != null) TextBoxMascotas.IsEnabled = false;
                if (TxtSuplementoMascotas != null) TxtSuplementoMascotas.Text = "";
                return;
            }

            var admiteMascotas = habitacion.AdmiteMascotas;
            if (CheckMascota != null)
                CheckMascota.IsEnabled = admiteMascotas;

            if (!admiteMascotas && CheckMascota != null)
                CheckMascota.IsChecked = false;

            var incluirMascota = CheckMascota?.IsChecked == true && admiteMascotas;
            if (TextBoxMascotas != null)
            {
                TextBoxMascotas.IsEnabled = incluirMascota;
                if (!incluirMascota) TextBoxMascotas.Text = "0";
                else if (TextBoxMascotas.Text == "0") TextBoxMascotas.Text = "1";
            }

            if (TxtSuplementoMascotas != null)
            {
                TxtSuplementoMascotas.Text = admiteMascotas
                    ? $"Suplemento: {habitacion.SuplementoMascotasNoche:F2} €/noche"
                    : "Esta habitación no admite mascotas";
            }
        }

        private async void Crear_Click(object sender, RoutedEventArgs e)
        {
            if (!FechaEntradaPicker.SelectedDate.HasValue || !FechaSalidaPicker.SelectedDate.HasValue)
            {
                MessageBox.Show("Selecciona las fechas");
                return;
            }

            DateTime entrada = FechaEntradaPicker.SelectedDate.Value.Date;
            DateTime salida = FechaSalidaPicker.SelectedDate.Value.Date;
            if (salida <= entrada)
            {
                MessageBox.Show("La fecha de salida debe ser posterior a la fecha de entrada.");
                return;
            }

            if (!int.TryParse(TextBoxPersonas.Text, out int personas) || personas <= 0)
            {
                MessageBox.Show("Número de personas inválido");
                return;
            }

            var clienteExistente = ObtenerClienteSeleccionado();
            if (clienteExistente == null)
            {
                MessageBox.Show("Selecciona un DNI registrado del desplegable.");
                return;
            }

            if (ComboBoxHabitacion.SelectedItem is not Habitacion habitacion)
            {
                MessageBox.Show("Selecciona una habitación");
                return;
            }

            int mascotas = 0;
            if (CheckMascota.IsChecked == true)
            {
                if (!habitacion.AdmiteMascotas)
                {
                    MessageBox.Show("La habitación seleccionada no admite mascotas");
                    return;
                }

                if (!int.TryParse(TextBoxMascotas.Text, out mascotas) || mascotas <= 0)
                {
                    MessageBox.Show("Número de mascotas inválido");
                    return;
                }

                if (habitacion.MaxMascotas > 0 && mascotas > habitacion.MaxMascotas)
                {
                    MessageBox.Show($"La habitación admite un máximo de {habitacion.MaxMascotas} mascota(s)");
                    return;
                }
            }

            int dias = (salida - entrada).Days;
            decimal precioTotal = Convert.ToDecimal(habitacion.PrecioNoche) * dias;

            var reserva = new Reserva
            {
                Id = _modoEdicion ? _reservaEditar.Id : Guid.NewGuid().ToString(),
                ClienteId = clienteExistente.Id,
                HabitacionId = habitacion.Id,
                FechaEntrada = entrada,
                FechaSalida = salida,
                Personas = Math.Min(personas, habitacion.MaxOcupantes),
                Mascotas = mascotas,
                WithPet = mascotas > 0,
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

        private Cliente ObtenerClienteSeleccionado()
        {
            if (DniComboBox.SelectedItem is Cliente cliente)
                return cliente;

            var dniEscrito = DniComboBox.Text?.Trim();
            if (string.IsNullOrWhiteSpace(dniEscrito))
                return null;

            return _clientesVM.ClientesNoReducido.FirstOrDefault(c =>
                string.Equals(c.DNI?.Trim(), dniEscrito, StringComparison.OrdinalIgnoreCase));
        }
    }
}
