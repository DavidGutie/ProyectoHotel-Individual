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
        public ObservableCollection<Amenity> Amenities { get; } = new();

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
            _ = CargarAmenitiesAsync();
        }

        private static object CrearPayloadHabitacion(Habitacion h) => new
        {
            numero = h.Numero,
            tipo = h.Tipo,
            descripcion = h.Descripcion,
            imagen = h.Imagen,
            imagenes = string.IsNullOrWhiteSpace(h.Imagen) ? Array.Empty<string>() : new[] { h.Imagen },
            precionoche = h.PrecioNoche,
            rate = h.Rate,
            max_ocupantes = h.MaxOcupantes,
            disponible = h.Disponible,
            oferta = h.Oferta,
            servicios = h.Servicios,
            amenityIds = h.AmenityIds,
            pets_allowed = h.AdmiteMascotas,
            aceptaMascotas = h.AdmiteMascotas,
            pet_supplement_per_night = h.SuplementoMascotasNoche,
            suplementoMascota = h.SuplementoMascotasNoche,
            politicaMascotas = h.PoliticaMascotas,
            maxMascotas = h.MaxMascotas
        };

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

        public async Task CargarAmenitiesAsync()
        {
            try
            {
                var lista = await _client.GetFromJsonAsync<List<Amenity>>("amenities");

                Amenities.Clear();
                if (lista != null)
                    foreach (var a in lista)
                        Amenities.Add(a);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error de API (GET amenities): " + ex.Message);
            }
        }

        public async Task<Habitacion?> CrearHabitacionAsync(Habitacion h)
        {
            h.Id = null;

            var resp = await _client.PostAsJsonAsync("habitaciones", CrearPayloadHabitacion(h), JsonOptions);
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

            var resp = await _client.PutAsJsonAsync($"habitaciones/{h.Id}", CrearPayloadHabitacion(h), JsonOptions);
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

        public async Task<Amenity?> CrearAmenityAsync(Amenity amenity)
        {
            amenity.Id = "";
            var resp = await _client.PostAsJsonAsync("amenities", amenity, JsonOptions);
            if (!resp.IsSuccessStatusCode)
            {
                var err = await resp.Content.ReadAsStringAsync();
                throw new Exception($"POST /amenities falló: {(int)resp.StatusCode} - {err}");
            }

            return await resp.Content.ReadFromJsonAsync<Amenity>(JsonOptions);
        }

        public async Task ActualizarAmenityAsync(Amenity amenity)
        {
            if (string.IsNullOrWhiteSpace(amenity.Id))
                throw new Exception("No se puede actualizar un amenity sin Id.");

            var resp = await _client.PutAsJsonAsync($"amenities/{amenity.Id}", amenity, JsonOptions);
            if (!resp.IsSuccessStatusCode)
            {
                var err = await resp.Content.ReadAsStringAsync();
                throw new Exception($"PUT /amenities/{amenity.Id} falló: {(int)resp.StatusCode} - {err}");
            }
        }

        public async Task EliminarAmenityAsync(Amenity amenity)
        {
            if (string.IsNullOrWhiteSpace(amenity.Id))
                throw new Exception("No se puede borrar un amenity sin Id.");

            var resp = await _client.DeleteAsync($"amenities/{amenity.Id}");
            if (!resp.IsSuccessStatusCode)
            {
                var err = await resp.Content.ReadAsStringAsync();
                throw new Exception($"DELETE /amenities/{amenity.Id} falló: {(int)resp.StatusCode} - {err}");
            }
        }
    }
}
