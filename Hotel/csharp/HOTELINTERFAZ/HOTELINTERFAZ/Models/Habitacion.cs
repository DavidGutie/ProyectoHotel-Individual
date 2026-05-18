using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

namespace HOTELINTERFAZ.Models
{
    public class Habitacion
    {
        [JsonPropertyName("_id")]
        public string Id { get; set; }

        [JsonPropertyName("numero")]
        public int Numero { get; set; }

        [JsonPropertyName("tipo")]
        public string Tipo { get; set; } = "";

        [JsonPropertyName("descripcion")]
        public string Descripcion { get; set; } = "";

        [JsonPropertyName("imagen")]
        public string Imagen { get; set; } = "";

        [JsonPropertyName("precionoche")]
        public double PrecioNoche { get; set; }

        [JsonPropertyName("rate")]
        public double Rate { get; set; }

        [JsonPropertyName("max_ocupantes")]
        public int MaxOcupantes { get; set; }

        [JsonPropertyName("disponible")]
        public bool Disponible { get; set; }

        [JsonPropertyName("oferta")]
        public bool Oferta { get; set; }

        [JsonPropertyName("servicios")]
        public ObservableCollection<string> Servicios { get; set; } = new();

        [JsonPropertyName("amenities")]
        public ObservableCollection<Amenity> Amenities { get; set; } = new();

        [JsonPropertyName("pets_allowed")]
        public bool PetsAllowed { get; set; }

        [JsonPropertyName("pet_supplement_per_night")]
        public double PetSupplementPerNight { get; set; }

        [JsonPropertyName("aceptaMascotas")]
        public bool AceptaMascotas { get; set; }

        [JsonPropertyName("suplementoMascota")]
        public double SuplementoMascota { get; set; }

        [JsonPropertyName("politicaMascotas")]
        public string PoliticaMascotas { get; set; } = "";

        [JsonPropertyName("maxMascotas")]
        public int MaxMascotas { get; set; }

        [JsonIgnore]
        public bool AdmiteMascotas
        {
            get => PetsAllowed || AceptaMascotas;
            set
            {
                PetsAllowed = value;
                AceptaMascotas = value;
            }
        }

        [JsonIgnore]
        public double SuplementoMascotasNoche
        {
            get => PetSupplementPerNight > 0 ? PetSupplementPerNight : SuplementoMascota;
            set
            {
                PetSupplementPerNight = value;
                SuplementoMascota = value;
            }
        }

        [JsonIgnore]
        public string AmenitiesTexto => Amenities == null || Amenities.Count == 0
            ? ""
            : string.Join(", ", Amenities.Select(a => a.Name));

        [JsonIgnore]
        public List<string> AmenityIds => Amenities?
            .Where(a => !string.IsNullOrWhiteSpace(a.Id))
            .Select(a => a.Id)
            .ToList() ?? new List<string>();
        
        [JsonIgnore]
        public string ServiciosTexto
        {
            get => Servicios == null || Servicios.Count == 0
                ? ""
                : string.Join(", ", Servicios);
            set
            {
                Servicios ??= new ObservableCollection<string>();
                Servicios.Clear();

                if (string.IsNullOrWhiteSpace(value))
                    return;

                var tokens = value
                    .Split(new[] { ',', ';' }, StringSplitOptions.RemoveEmptyEntries)
                    .Select(s => s.Trim())
                    .Where(s => !string.IsNullOrWhiteSpace(s));

                foreach (var t in tokens)
                    Servicios.Add(t);
            }
        }

    }
}
