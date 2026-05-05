using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Text.Json.Serialization;

namespace HOTELINTERFAZ.Models
{
    public class FacturaConfig : INotifyPropertyChanged
    {
        private string _nombreHotel;
        private string _cif;
        private string _direccion;
        private string _email;
        private string _telefono;

        [JsonPropertyName("nombreHotel")]
        public string NombreHotel
        {
            get => _nombreHotel;
            set => SetProperty(ref _nombreHotel, value);
        }

        [JsonPropertyName("cif")]
        public string Cif
        {
            get => _cif;
            set => SetProperty(ref _cif, value);
        }

        [JsonPropertyName("direccion")]
        public string Direccion
        {
            get => _direccion;
            set => SetProperty(ref _direccion, value);
        }

        [JsonPropertyName("email")]
        public string Email
        {
            get => _email;
            set => SetProperty(ref _email, value);
        }

        [JsonPropertyName("telefono")]
        public string Telefono
        {
            get => _telefono;
            set => SetProperty(ref _telefono, value);
        }

        public event PropertyChangedEventHandler PropertyChanged;

        private void SetProperty(ref string field, string value, [CallerMemberName] string name = null)
        {
            if (field == value) return;
            field = value;
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));
        }
    }
}
