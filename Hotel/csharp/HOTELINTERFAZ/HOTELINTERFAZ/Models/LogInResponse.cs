using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json.Serialization;
using System.Threading.Tasks;


namespace HOTELINTERFAZ.Models
{
    
        public class LoginResponse
        {
            [JsonPropertyName("Token")]
            public string Token { get; set; }

            [JsonPropertyName("Usuario")]
            public Usuario Usuario { get; set; }
        }
    
}
