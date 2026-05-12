using HOTELINTERFAZ.Models;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Net.Http;
using System.Net.Http.Json;
using System.Threading.Tasks;
using System.Windows.Data;

namespace HOTELINTERFAZ.ViewModels
{
    public class ReservasViewModel : INotifyPropertyChanged
    {
        private readonly HttpClient _client;

        public ObservableCollection<Reserva> Reservas { get; } = new();

        public ICollectionView ReservasView { get; }

        private Reserva _reservaSeleccionada;
        public Reserva ReservaSeleccionada
        {
            get => _reservaSeleccionada;
            set
            {
                _reservaSeleccionada = value;
                OnPropertyChanged(nameof(ReservaSeleccionada));
            }
        }

        private bool _mostrarSoloActivas = true;
        public bool MostrarSoloActivas
        {
            get => _mostrarSoloActivas;
            set
            {
                if (_mostrarSoloActivas == value) return;
                _mostrarSoloActivas = value;
                OnPropertyChanged(nameof(MostrarSoloActivas));
                ReservasView.Refresh();
            }
        }

        private string _filtroDni = "";
        public string FiltroDni
        {
            get => _filtroDni;
            set
            {
                if (_filtroDni == value) return;
                _filtroDni = value;
                OnPropertyChanged(nameof(FiltroDni));
                ReservasView.Refresh();
            }
        }
        private string _reservaVisibleTemporalmenteId;
        public string ReservaVisibleTemporalmenteId
        {
            get => _reservaVisibleTemporalmenteId;
            set
            {
                if (_reservaVisibleTemporalmenteId == value) return;
                _reservaVisibleTemporalmenteId = value;
                OnPropertyChanged(nameof(ReservaVisibleTemporalmenteId));
                ReservasView.Refresh();
            }
        }

        public ReservasViewModel()
        {
            _client = new HttpClient
            {
                BaseAddress = new Uri("http://localhost:3000/")
            };

            if (SessionManager.UsuarioActual != null)
            {
                _client.DefaultRequestHeaders.Add("x-actor-id", SessionManager.UsuarioActual.Id);
                _client.DefaultRequestHeaders.Add("x-actor-type", "employee");
            }

            ReservasView = CollectionViewSource.GetDefaultView(Reservas);
            ReservasView.Filter = FiltrarReserva;

            _ = CargarReservasAsync();
        }

        private bool FiltrarReserva(object obj)
        {
            if (obj is not Reserva r) return false;

            if (MostrarSoloActivas && r.Cancelacion && r.Id != ReservaVisibleTemporalmenteId)
                return false;

            var filtro = FiltroDni?.Trim().ToLower() ?? "";
            if (string.IsNullOrWhiteSpace(filtro))
                return true;

            var dni = r.Cliente?.Dni?.Trim().ToLower() ?? "";
            return dni.Contains(filtro);
        }

        public async Task CargarReservasAsync()
        {
            try
            {
                var reservas = await _client.GetFromJsonAsync<List<Reserva>>("reservas");

                Reservas.Clear();

                if (reservas != null)
                {
                    foreach (var r in reservas)
                        Reservas.Add(r);
                }

                ReservasView.Refresh();
            }
            catch
            {
            }
        }

        public async Task<bool> AgregarReservaAsync(Reserva reserva)
        {
            var response = await _client.PostAsJsonAsync("reservas", reserva);
            if (response.IsSuccessStatusCode)
            {
                await CargarReservasAsync();
                return true;
            }
            return false;
        }

        public async Task<bool> CancelarReservaAsync(string id)
        {
            var response = await _client.PutAsync($"reservas/{id}/cancelar", null);
            if (response.IsSuccessStatusCode)
            {
                await CargarReservasAsync();
                return true;
            }
            return false;
        }

        public async Task<bool> EliminarReservaAsync(string id)
        {
            var response = await _client.DeleteAsync($"reservas/{id}");
            if (response.IsSuccessStatusCode)
            {
                await CargarReservasAsync();
                return true;
            }
            return false;
        }

        public async Task<bool> RegistrarPagoAsync(string id)
        {
            var response = await _client.PostAsync($"reservas/{id}/pago", null);
            if (response.IsSuccessStatusCode)
            {
                await CargarReservasAsync();
                return true;
            }
            return false;
        }

        public async Task<bool> AgregarExtraAsync(string id, string concepto, double importe)
        {
            var response = await _client.PostAsJsonAsync($"reservas/{id}/extras", new
            {
                concepto,
                importe
            });

            if (response.IsSuccessStatusCode)
            {
                await CargarReservasAsync();
                return true;
            }
            return false;
        }

        public async Task<List<ReservaAudit>> ObtenerHistorialReservaAsync(string id)
        {
            try
            {
                var response = await _client.GetAsync($"reservas/{id}/audit");

                if (!response.IsSuccessStatusCode)
                    return new List<ReservaAudit>();

                var historial = await response.Content.ReadFromJsonAsync<List<ReservaAudit>>();
                return historial ?? new List<ReservaAudit>();
            }
            catch
            {
                return new List<ReservaAudit>();
            }
        }

        public async Task<byte[]> ObtenerFacturaPdfAsync(string id)
        {
            try
            {
                var response = await _client.GetAsync($"reservas/{id}/factura");

                if (!response.IsSuccessStatusCode)
                    return Array.Empty<byte>();

                return await response.Content.ReadAsByteArrayAsync();
            }
            catch
            {
                return Array.Empty<byte>();
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        protected void OnPropertyChanged(string name)
            => PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));

        public void MostrarReservaCanceladaTemporalmente(string id)
        {
            ReservaVisibleTemporalmenteId = id;
        }

        public void LimpiarReservaVisibleTemporalmente()
        {
            ReservaVisibleTemporalmenteId = null;
        }
    }

}
