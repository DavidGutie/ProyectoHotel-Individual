using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HOTELINTERFAZ.Models
{
    internal class SessionManager
    {
        public static string Token { get; set; }
        public static Usuario UsuarioActual { get; set; }
    }
}
