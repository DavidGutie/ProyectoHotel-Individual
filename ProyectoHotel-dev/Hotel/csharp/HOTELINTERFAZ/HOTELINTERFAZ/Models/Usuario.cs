using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

namespace HOTELINTERFAZ.Models
{
    public class Usuario
    {
        [JsonPropertyName("Id")]
        public string Id { get; set; }

        [JsonPropertyName("Nombre")]
        public string Nombre { get; set; }

        [JsonPropertyName("Email")]
        public string Email { get; set; }

        [JsonPropertyName("TipoUsuario")]
        public string TipoUsuario { get; set; }

        [JsonPropertyName("Administrador")]
        public bool Administrador { get; set; }
    }
}
