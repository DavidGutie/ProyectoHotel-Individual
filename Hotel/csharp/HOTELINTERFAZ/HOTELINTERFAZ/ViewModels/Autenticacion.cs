using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Net.Http.Json;
using HOTELINTERFAZ.Models;

namespace HOTELINTERFAZ.ViewModels
{
    internal class Autenticacion
    {
        private readonly HttpClient _httpClient = new()
        {
            BaseAddress = new Uri("http://localhost:3000/")
        };

        public async Task<LoginResponse> Login(string email, string password)
        {
            var response = await _httpClient.PostAsJsonAsync("auth/login", new
            {
                email,
                password
            });

            if (!response.IsSuccessStatusCode)
                throw new Exception("Credenciales incorrectas");

            return await response.Content.ReadFromJsonAsync<LoginResponse>();
        }
    }
}
