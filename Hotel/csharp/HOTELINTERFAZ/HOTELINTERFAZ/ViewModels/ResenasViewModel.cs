using HOTELINTERFAZ.Models;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Json;
using System.Text;
using System.Threading.Tasks;
using System.Windows;

namespace HOTELINTERFAZ.ViewModels
{
    public class ResenasViewModel : INotifyPropertyChanged
    {
        public ObservableCollection<Resena> Resenas { get; } = new();
        private readonly HttpClient _client = new() { BaseAddress = new Uri("http://localhost:3000/") };
        private Resena _resenaSeleccionada;
        public Resena ResenaSeleccionada
        {
            get => _resenaSeleccionada;
            set
            {
                _resenaSeleccionada = value;
                OnPropertyChanged(nameof(ResenaSeleccionada));
            }
        }

        public ResenasViewModel()
        {
            _ = CargarResenas();
        }

        public async Task CargarResenas()
        {
            try
            {
                var lista = await _client.GetFromJsonAsync<List<Resena>>("resenas");
                Resenas.Clear();
                if (lista != null)
                {
                    foreach (var r in lista) Resenas.Add(r);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error de API: " + ex.Message);
            }
        }
        public async Task<bool> EliminarResenaAsync(string id)
        {
            try
            {
                var response = await _client.DeleteAsync($"resenas/{id}");
                if (response.IsSuccessStatusCode)
                {
                    await CargarResenas(); 
                    return true;
                }
                return false;
            }
            catch
            {
                return false;
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;
        protected void OnPropertyChanged(string propertyName)
            => PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
    }
}
