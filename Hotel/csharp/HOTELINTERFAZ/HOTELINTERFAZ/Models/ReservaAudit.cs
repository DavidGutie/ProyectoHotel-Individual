using System;
using System.Collections.Generic;
using System.Linq;
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

        [JsonPropertyName("actorId")]
        public string ActorId { get; set; }

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
            "EXTRA" => "Extra anadido",
            _ => Action ?? "Accion"
        };

        [JsonIgnore]
        public string ActorTexto => ActorType switch
        {
            "user" => "Cliente",
            "employee" => "Empleado",
            "system" => "Sistema",
            _ => string.IsNullOrWhiteSpace(ActorType) ? "Sistema" : ActorType
        };

        [JsonIgnore]
        public string ResumenCambio
        {
            get
            {
                var diferencias = ObtenerDiferencias();
                if (!string.IsNullOrWhiteSpace(diferencias))
                    return diferencias;

                if (Action == "CREAR") return "Se creo la reserva.";
                if (Action == "CANCELAR") return "La reserva paso a estado cancelado.";
                if (Action == "ELIMINAR") return "La reserva fue eliminada.";
                if (Action == "PAGO") return "Se registro un pago.";
                if (Action == "EXTRA") return "Se anadio un extra.";
                if (Action == "MODIFICAR") return "Se modificaron datos de la reserva.";

                return "Cambio registrado.";
            }
        }

        private string ObtenerDiferencias()
        {
            if (Action == "CREAR") return "Reserva creada.";
            if (Action == "ELIMINAR") return "Reserva eliminada.";

            if (PreviousState.ValueKind != JsonValueKind.Object || NewState.ValueKind != JsonValueKind.Object)
                return "";

            var cambios = new List<string>();
            var campos = new Dictionary<string, string>
            {
                ["fechaEntrada"] = "Entrada",
                ["fechaSalida"] = "Salida",
                ["personas"] = "Personas",
                ["precioTotal"] = "Total",
                ["cancelacion"] = "Cancelada",
                ["invoiceNumber"] = "Factura",
                ["invoiceIssuedAt"] = "Fecha factura",
                ["empresaNombre"] = "Empresa",
                ["empresaCif"] = "CIF empresa",
                ["empresaDireccion"] = "Direccion empresa",
                ["descuento"] = "Descuento",
                ["impuestos"] = "Impuestos",
                ["extras"] = "Extras"
            };

            foreach (var campo in campos)
            {
                var anterior = ObtenerValor(PreviousState, campo.Key);
                var nuevo = ObtenerValor(NewState, campo.Key);

                if (anterior != nuevo)
                    cambios.Add($"{campo.Value}: {anterior} -> {nuevo}");
            }

            return cambios.Count == 0
                ? ""
                : string.Join("; ", cambios.Take(4)) + (cambios.Count > 4 ? "..." : "");
        }

        private static string ObtenerValor(JsonElement estado, string nombre)
        {
            if (!estado.TryGetProperty(nombre, out var valor) || valor.ValueKind == JsonValueKind.Null)
                return "-";

            return valor.ValueKind switch
            {
                JsonValueKind.String => FormatearString(valor.GetString()),
                JsonValueKind.Number => valor.GetRawText(),
                JsonValueKind.True => "Si",
                JsonValueKind.False => "No",
                JsonValueKind.Array => $"{valor.GetArrayLength()} elemento(s)",
                _ => valor.GetRawText()
            };
        }

        private static string FormatearString(string valor)
        {
            if (string.IsNullOrWhiteSpace(valor))
                return "-";

            return DateTime.TryParse(valor, out var fecha)
                ? fecha.ToString("dd/MM/yyyy HH:mm")
                : valor;
        }
    }
}
