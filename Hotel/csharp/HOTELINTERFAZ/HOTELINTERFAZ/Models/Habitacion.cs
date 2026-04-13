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