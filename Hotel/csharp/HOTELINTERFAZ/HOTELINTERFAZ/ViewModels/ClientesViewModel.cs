using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using HOTELINTERFAZ.Models;
using System.Windows;
using System.Net.Http.Json;
using Microsoft.Win32;
using System.ComponentModel;
using System.Windows.Data;

namespace HOTELINTERFAZ.ViewModels
{
    public class ClientesViewModel
    {
        public Cliente ClienteSeleccionado { get; set; }
        
        public ICollectionView ClienteView { get; set; }

        
        public string FotoUrlCompleta { get; set; }

        private readonly HttpClient _client;
        
        public string NombreNuevo { get; set; }
        public string DniNuevo { get; set; }
        public string EmailNuevo { get; set; }
        public string PasswordNuevo { get; set; }
        public DateTime FechaNacimientoNuevo { get; set; } = DateTime.Now;
        public string SexoNuevo { get; set; }
        public bool vipNuevo { get; set; }
        public string ciudadNuevo { get; set; }

        public string RutaImagenSeleccionada { get; set; }
        public ObservableCollection<ClienteReducido> Clientes { get; } = new ObservableCollection<ClienteReducido>();
        public ObservableCollection<Cliente> ClientesNoReducido { get; } = new ObservableCollection<Cliente>();
        

        public ClientesViewModel()
        {
            _client = new HttpClient
            {
                BaseAddress = new Uri("http://localhost:3000/")
            };
            
            ClienteView = CollectionViewSource.GetDefaultView(ClientesNoReducido);

            _ = CargarClientesAsync();
        }
        
        public async Task CargarClientesAsync()
        {
            try
            {
                var lista = await _client.GetFromJsonAsync<List<Cliente>>("cliente");


                ClientesNoReducido.Clear();

                foreach (var cli in lista)
                    ClientesNoReducido.Add(cli);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
        }
        
        public void CargarClienteParaEditar(Cliente cli)
        {
            ClienteSeleccionado = cli;
            

            NombreNuevo = cli.Nombre;
            DniNuevo = cli.DNI;
            EmailNuevo = cli.Email;
            FechaNacimientoNuevo = cli.FechaNacimiento;
            SexoNuevo = cli.Sexo;
            vipNuevo = cli.Vip;
            ciudadNuevo = cli.Ciudad;

            RutaImagenSeleccionada = null;
            
            if (!string.IsNullOrEmpty(cli.Foto))
            {
                FotoUrlCompleta = "http://localhost:3000" + cli.Foto;
            }
        }
        public void SeleccionarImagen()
        {
            OpenFileDialog ofd = new OpenFileDialog();
            ofd.Filter = "Imagenes|*.jpg;*.png;*.jpeg";

            if (ofd.ShowDialog() == true)
            {
                RutaImagenSeleccionada = ofd.FileName;
            }
        }
        
        public async Task ActualizarCliente()
        {
            try
            {
                var form = new MultipartFormDataContent();

                form.Add(new StringContent(NombreNuevo), "nombre");
                form.Add(new StringContent(DniNuevo), "dni");
                form.Add(new StringContent(EmailNuevo), "email");
                if (!string.IsNullOrWhiteSpace(PasswordNuevo))
                {
                    form.Add(new StringContent(PasswordNuevo), "password");
                }
                form.Add(new StringContent(FechaNacimientoNuevo.ToString("dd/MM/yyyy")), "fechaNacimiento");
                form.Add(new StringContent(SexoNuevo), "sexo");
                form.Add(new StringContent(vipNuevo.ToString().ToLower()), "vip");
                form.Add(new StringContent(ciudadNuevo), "ciudad");

                if (!string.IsNullOrEmpty(RutaImagenSeleccionada))
                {
                    var bytes = File.ReadAllBytes(RutaImagenSeleccionada);

                    var fileContent = new ByteArrayContent(bytes);
                    var extension = Path.GetExtension(RutaImagenSeleccionada).ToLower();
                    
                    string mime = extension switch
                    {
                        ".png" => "image/png",
                        ".jpg" => "image/jpeg",
                        ".jpeg" => "image/jpeg",
                        _ => "application/octet-stream"
                    };
                    
                    fileContent.Headers.ContentType =
                        new System.Net.Http.Headers.MediaTypeHeaderValue(mime);

                    form.Add(fileContent, "foto", Path.GetFileName(RutaImagenSeleccionada));
                }

                var response = await _client.PutAsync($"cliente/{ClienteSeleccionado.Id}", form);

                if (response.IsSuccessStatusCode)
                {
                    MessageBox.Show("Cliente actualizado");
                    await CargarClientesAsync();
                }
                else
                {
                    MessageBox.Show(await response.Content.ReadAsStringAsync());
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
        }
        
        public async Task CrearCliente()
        {
            try
            {
                var form = new MultipartFormDataContent();

                form.Add(new StringContent(NombreNuevo), "nombre");
                form.Add(new StringContent(DniNuevo), "dni");
                form.Add(new StringContent(EmailNuevo), "email");
                form.Add(new StringContent(PasswordNuevo), "password");
                form.Add(new StringContent(FechaNacimientoNuevo.ToString("dd/MM/yyyy")), "fechaNacimiento");
                form.Add(new StringContent(SexoNuevo), "sexo");
                form.Add(new StringContent(vipNuevo.ToString().ToLower()), "vip");
                form.Add(new StringContent(ciudadNuevo), "ciudad");
                if (!string.IsNullOrEmpty(RutaImagenSeleccionada))
                {
                    var bytes = File.ReadAllBytes(RutaImagenSeleccionada);

                    var fileContent = new ByteArrayContent(bytes);
                    var extension = Path.GetExtension(RutaImagenSeleccionada).ToLower();

                    string mime = extension switch
                    {
                        ".png" => "image/png",
                        ".jpg" => "image/jpeg",
                        ".jpeg" => "image/jpeg",
                        _ => "application/octet-stream"
                    };
                    fileContent.Headers.ContentType = new System.Net.Http.Headers.MediaTypeHeaderValue(mime);

                    form.Add(fileContent, "foto", Path.GetFileName(RutaImagenSeleccionada));
                }

                var response = await _client.PostAsync("cliente", form);

                if (response.IsSuccessStatusCode)
                {
                    MessageBox.Show("Cliente creado correctamente");
                    await CargarClientesAsync();
                }
                else
                {
                    MessageBox.Show(await response.Content.ReadAsStringAsync());
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
        }
        
        public async Task EliminarCliente()
        {
            if (ClienteSeleccionado == null)
            {
                MessageBox.Show("Seleccione un empleado primero.");
                return;
            }

            var resultado = MessageBox.Show(
                $"¿Está seguro de eliminar a {ClienteSeleccionado.Nombre}?",
                "Eliminar Cliente",
                MessageBoxButton.YesNo,
                MessageBoxImage.Question);

            if (resultado != MessageBoxResult.Yes)
            {
                return;
            }
            else
            {
                try
                {
                    var response = await _client.DeleteAsync($"cliente/{ClienteSeleccionado.Id}");

                    if (response.IsSuccessStatusCode)
                    {
                        MessageBox.Show("Cliente eliminado correctamente");

                        await CargarClientesAsync(); 
                    }
                    else
                    {
                        MessageBox.Show(await response.Content.ReadAsStringAsync());
                    }
                }
                catch (Exception ex)
                {
                    MessageBox.Show(ex.Message);
                }
            }
        }
        public void BuscarPorDni(string dni)
        {
            if (string.IsNullOrWhiteSpace(dni))
            {
                ClienteView.Filter = null;
            }
            else
            {
                ClienteView.Filter = e =>
                {
                    var cli = e as Cliente;
                    return cli.DNI.Contains(dni, StringComparison.OrdinalIgnoreCase);
                };
            }

            ClienteView.Refresh();
        }
    }
}
