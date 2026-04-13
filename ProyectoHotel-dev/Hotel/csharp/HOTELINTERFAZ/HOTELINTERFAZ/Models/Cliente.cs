using System;
using System.Text.Json.Serialization;

namespace HOTELINTERFAZ.Models
{
    public class Cliente 
    {
        [JsonPropertyName("_id")]
        public string Id { get; set; }
        
        [JsonPropertyName("nombre")]
        public string Nombre { get; set; }
        
        [JsonPropertyName("dni")]
        public string DNI { get; set; }
        
        [JsonPropertyName("email")]
        public string Email { get; set; }
        
        [JsonPropertyName("fechaNacimiento")]
        public DateTime FechaNacimiento { get; set; }
        
        [JsonPropertyName("sexo")]
        public string Sexo { get; set; }
        
        [JsonPropertyName("foto")]
        public string Foto { get; set; }
        
        [JsonPropertyName("ciudad")]
        public string Ciudad { get; set; }
        
        [JsonPropertyName("vip")]
        public bool Vip { get; set; }
    }
}
