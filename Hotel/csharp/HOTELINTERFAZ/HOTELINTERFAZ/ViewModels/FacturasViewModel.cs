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
    public class FacturasViewModel : INotifyPropertyChanged
    {
        private readonly HttpClient _client;

        public ObservableCollection<FacturaResumen> Facturas { get; } = new();
        public ICollectionView FacturasView { get; }
        public FacturaConfig Config { get; } = new();

        private FacturaResumen _facturaSeleccionada;
        public FacturaResumen FacturaSeleccionada
        {
            get => _facturaSeleccionada;
            set
            {
                _facturaSeleccionada = value;
                OnPropertyChanged(nameof(FacturaSeleccionada));
            }
        }

        private string _filtroTexto = "";
        public string FiltroTexto
        {
            get => _filtroTexto;
            set
            {
                if (_filtroTexto == value) return;
                _filtroTexto = value;
                OnPropertyChanged(nameof(FiltroTexto));
                FacturasView.Refresh();
            }
        }

        public FacturasViewModel()
        {
            _client = new HttpClient
            {
                BaseAddress = new Uri("http://localhost:3000/")
            };

            FacturasView = CollectionViewSource.GetDefaultView(Facturas);
            FacturasView.Filter = FiltrarFactura;

            _ = CargarFacturasAsync();
            _ = CargarConfigAsync();
        }

        private bool FiltrarFactura(object item)
        {
            if (item is not FacturaResumen factura)
                return false;

            var filtro = FiltroTexto?.Trim().ToLower() ?? "";
            if (string.IsNullOrWhiteSpace(filtro))
                return true;

            return Contiene(factura.InvoiceNumber, filtro)
                || Contiene(factura.Cliente?.Nombre, filtro)
                || Contiene(factura.Cliente?.Dni, filtro)
                || Contiene(factura.Cliente?.Email, filtro)
                || Contiene(factura.Habitacion?.Numero.ToString(), filtro)
                || Contiene(factura.Habitacion?.Tipo, filtro);
        }

        private static bool Contiene(string valor, string filtro)
        {
            return !string.IsNullOrWhiteSpace(valor)
                && valor.ToLower().Contains(filtro);
        }

        public async Task CargarFacturasAsync()
        {
            try
            {
                var facturas = await _client.GetFromJsonAsync<List<FacturaResumen>>("facturas");

                Facturas.Clear();

                if (facturas != null)
                {
                    foreach (var factura in facturas)
                        Facturas.Add(factura);
                }

                FacturasView.Refresh();
            }
            catch
            {
            }
        }

        public async Task CargarConfigAsync()
        {
            try
            {
                var config = await _client.GetFromJsonAsync<FacturaConfig>("facturas/config");
                if (config == null) return;

                Config.NombreHotel = config.NombreHotel;
                Config.Cif = config.Cif;
                Config.Direccion = config.Direccion;
                Config.Email = config.Email;
                Config.Telefono = config.Telefono;
            }
            catch
            {
            }
        }

        public async Task<bool> GuardarConfigAsync()
        {
            try
            {
                var response = await _client.PutAsJsonAsync("facturas/config", Config);
                return response.IsSuccessStatusCode;
            }
            catch
            {
                return false;
            }
        }

        public async Task<byte[]> ObtenerFacturaPdfAsync(string reservaId)
        {
            try
            {
                var response = await _client.GetAsync($"reservas/{reservaId}/factura");

                if (!response.IsSuccessStatusCode)
                    return Array.Empty<byte>();

                return await response.Content.ReadAsByteArrayAsync();
            }
            catch
            {
                return Array.Empty<byte>();
            }
        }

        public async Task<bool> ReenviarFacturaAsync(string reservaId)
        {
            try
            {
                var response = await _client.PostAsync($"facturas/{reservaId}/reenviar", null);
                return response.IsSuccessStatusCode;
            }
            catch
            {
                return false;
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        private void OnPropertyChanged(string name)
            => PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));
    }
}
