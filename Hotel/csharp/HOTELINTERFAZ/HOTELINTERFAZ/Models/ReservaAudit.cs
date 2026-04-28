using System;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace HOTELINTERFAZ.Models
{
    public class ReservaAudit
    {
        [JsonPropertyName("_id")]
        public string Id { get; set; }

        [JsonPropertyName("reservaId")]
        public string ReservaId { get; set; }

        [JsonPropertyName("action")]
        public string Action { get; set; }

        [JsonPropertyName("actorType")]
        public string ActorType { get; set; }

        [JsonPropertyName("timestamp")]
        public DateTime Timestamp { get; set; }

        [JsonPropertyName("previousState")]
        public JsonElement PreviousState { get; set; }

        [JsonPropertyName("newState")]
        public JsonElement NewState { get; set; }

        [JsonIgnore]
        public string AccionTexto => Action switch
        {
            "CREAR" => "Reserva creada",
            "CANCELAR" => "Reserva cancelada",
            "ELIMINAR" => "Reserva eliminada",
            "MODIFICAR" => "Reserva modificada",
            "PAGO" => "Pago registrado",
            "EXTRA" => "Extra añadido",
            _ => Action ?? "Acción"
        };

        [JsonIgnore]
        public string ActorTexto => string.IsNullOrWhiteSpace(ActorType)
            ? "system"
            : ActorType;

        [JsonIgnore]
        public string ResumenCambio
        {
            get
            {
                if (Action == "CREAR") return "Se creó la reserva.";
                if (Action == "CANCELAR") return "La reserva pasó a estado cancelado.";
                if (Action == "ELIMINAR") return "La reserva fue eliminada.";
                if (Action == "PAGO") return "Se registró un pago.";
                if (Action == "EXTRA") return "Se añadió un extra.";

                if (Action == "MODIFICAR")
                    return "Se modificaron datos de la reserva.";

                return "Cambio registrado.";
            }
        }
    }
}