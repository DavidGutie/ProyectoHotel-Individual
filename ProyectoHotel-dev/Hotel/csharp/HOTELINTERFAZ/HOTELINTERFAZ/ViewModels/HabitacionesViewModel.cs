using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Net.Http;
using System.Net.Http.Json;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Threading.Tasks;
using System.Windows;
using HOTELINTERFAZ.Models;

namespace HOTELINTERFAZ.ViewModels
{
    public class HabitacionesViewModel
    {
        public ObservableCollection<Habitacion> Habitaciones { get; } = new();

        private readonly HttpClient _client;
        private static readonly JsonSerializerOptions JsonOptions = new()
        {
            DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull,
            PropertyNamingPolicy = null
        };

        public HabitacionesViewModel()
        {
            _client = new HttpClient
            {
                BaseAddress = new Uri("http://localhost:3000/")
            };

            _ = CargarHabitaciones();
        }

        public async Task CargarHabitaciones()
        {
            try
            {
                var lista = await _client.GetFromJsonAsync<List<Habitacion>>("habitaciones");

                Habitaciones.Clear();
                if (lista != null)
                    foreach (var h in lista)
                        Habitaciones.Add(h);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error de API (GET habitaciones): " + ex.Message);
            }
        }

        public async Task<Habitacion?> CrearHabitacionAsync(Habitacion h)
        {
            h.Id = null;

            var resp = await _client.PostAsJsonAsync("habitaciones", h, JsonOptions);
            if (!resp.IsSuccessStatusCode)
            {
                var err = await resp.Content.ReadAsStringAsync();
                throw new Exception($"POST /habitaciones falló: {(int)resp.StatusCode} - {err}");
            }
            try
            {
                var creada = await resp.Content.ReadFromJsonAsync<Habitacion>(JsonOptions);
                return creada;
            }
            catch
            {
                return null;
            }
        }

        public async Task ActualizarHabitacionAsync(Habitacion h)
        {
            if (string.IsNullOrWhiteSpace(h.Id))
                throw new Exception("No se puede actualizar una habitación sin Id.");

            var resp = await _client.PutAsJsonAsync($"habitaciones/{h.Id}", h, JsonOptions);
            if (!resp.IsSuccessStatusCode)
            {
                var err = await resp.Content.ReadAsStringAsync();
                throw new Exception($"PUT /habitaciones/{h.Id} falló: {(int)resp.StatusCode} - {err}");
            }
        }

        public async Task EliminarHabitacionAsync(Habitacion h)
        {
            if (string.IsNullOrWhiteSpace(h.Id))
                throw new Exception("No se puede borrar una habitación sin Id.");

            var resp = await _client.DeleteAsync($"habitaciones/{h.Id}");
            if (!resp.IsSuccessStatusCode)
            {
                var err = await resp.Content.ReadAsStringAsync();
                throw new Exception($"DELETE /habitaciones/{h.Id} falló: {(int)resp.StatusCode} - {err}");
            }
        }
    }
}