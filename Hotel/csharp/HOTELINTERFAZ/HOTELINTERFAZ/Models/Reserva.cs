using System;
using System.Text.Json.Serialization;

namespace HOTELINTERFAZ.Models
{
    public class Reserva
    {
        [JsonPropertyName("_id")]
        public string Id { get; set; }

        [JsonPropertyName("clienteId")]
        public string ClienteId { get; set; }

        [JsonPropertyName("habitacionId")]
        public string HabitacionId { get; set; }

        [JsonPropertyName("fechaEntrada")]
        public DateTime FechaEntrada { get; set; }

        [JsonPropertyName("fechaSalida")]
        public DateTime FechaSalida { get; set; }

        [JsonPropertyName("personas")]
        public int Personas { get; set; }

        [JsonPropertyName("precioTotal")]
        public double PrecioTotal { get; set; }  

        [JsonPropertyName("cancelacion")]
        public bool Cancelacion { get; set; }

        [JsonPropertyName("cliente")]
        public ClienteReducido Cliente { get; set; } 


        [JsonPropertyName("habitacion")]
        public HabitacionReducida Habitacion { get; set; }

        public override string ToString() => $"Cliente: {Cliente}";
    }
}
