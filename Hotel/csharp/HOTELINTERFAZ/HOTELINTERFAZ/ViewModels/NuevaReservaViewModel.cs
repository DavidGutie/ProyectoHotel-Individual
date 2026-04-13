using HOTELINTERFAZ.Models;
using System;
using System.Collections.ObjectModel;
using System.Net.Http;
using System.Net.Http.Json;
using System.Windows;
using System.Windows.Input;
using HOTELINTERFAZ.Utils;

public class NuevaReservaViewModel
{
    private readonly HttpClient _client =
        new() { BaseAddress = new Uri("http://localhost:3000/") };

    public DateTime? FechaEntrada { get; set; }
    public DateTime? FechaSalida { get; set; }

    public ObservableCollection<ClienteReducido> Clientes { get; } = new();
    public ObservableCollection<Habitacion> Habitaciones { get; } = new();

    public ClienteReducido ClienteSeleccionado { get; set; }
    public Habitacion HabitacionSeleccionada { get; set; }

    public ICommand GuardarCommand { get; }

    public NuevaReservaViewModel()
    {
        GuardarCommand = new RelayCommand(async _ => await Guardar());
        _ = CargarDatos();
    }

    private async Task CargarDatos()
    {
        var clientes = await _client.GetFromJsonAsync<List<ClienteReducido>>("cliente");
        var habitaciones = await _client.GetFromJsonAsync<List<Habitacion>>("habitaciones");

        clientes.ForEach(c => Clientes.Add(c));
        habitaciones.ForEach(h => Habitaciones.Add(h));
    }

    private async Task Guardar()
    {
        if (FechaEntrada == null || FechaSalida == null ||
            ClienteSeleccionado == null || HabitacionSeleccionada == null)
        {
            MessageBox.Show("Completa todos los campos");
            return;
        }

        if (FechaSalida <= FechaEntrada)
        {
            MessageBox.Show("La fecha de salida debe ser posterior");
            return;
        }

        var reserva = new
        {
            clienteId = ClienteSeleccionado.Id,
            habitacionId = HabitacionSeleccionada.Id,
            fechaEntrada = FechaEntrada,
            fechaSalida = FechaSalida,
            personas = 1,
            precioTotal = 0
        };

        var res = await _client.PostAsJsonAsync("reservas", reserva);

        if (res.IsSuccessStatusCode)
        {
            MessageBox.Show("Reserva creada correctamente");
            Application.Current.Windows[Application.Current.Windows.Count - 1].Close();
        }
        else
        {
            var error = await res.Content.ReadAsStringAsync();
            MessageBox.Show(error);
        }
    }
}
