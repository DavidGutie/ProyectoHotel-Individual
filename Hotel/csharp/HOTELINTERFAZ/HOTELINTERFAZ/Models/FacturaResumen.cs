using System;
using System.Text.Json.Serialization;

namespace HOTELINTERFAZ.Models
{
    public class FacturaResumen
    {
        [JsonPropertyName("reservaId")]
        public string ReservaId { get; set; }

        [JsonPropertyName("clienteId")]
        public string ClienteId { get; set; }

        [JsonPropertyName("invoiceNumber")]
        public string InvoiceNumber { get; set; }

        [JsonPropertyName("invoiceIssuedAt")]
        public DateTime? InvoiceIssuedAt { get; set; }

        [JsonPropertyName("fechaEntrada")]
        public DateTime FechaEntrada { get; set; }

        [JsonPropertyName("fechaSalida")]
        public DateTime FechaSalida { get; set; }

        [JsonPropertyName("precioTotal")]
        public double PrecioTotal { get; set; }

        [JsonPropertyName("cancelacion")]
        public bool Cancelacion { get; set; }

        [JsonPropertyName("cliente")]
        public ClienteFactura Cliente { get; set; }

        [JsonPropertyName("habitacion")]
        public HabitacionFactura Habitacion { get; set; }
    }

    public class ClienteFactura
    {
        [JsonPropertyName("nombre")]
        public string Nombre { get; set; }

        [JsonPropertyName("dni")]
        public string Dni { get; set; }

        [JsonPropertyName("email")]
        public string Email { get; set; }
    }

    public class HabitacionFactura
    {
        [JsonPropertyName("numero")]
        public int Numero { get; set; }

        [JsonPropertyName("tipo")]
        public string Tipo { get; set; }
    }
}
