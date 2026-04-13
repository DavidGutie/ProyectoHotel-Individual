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
    public class EmpleadosViewModel
    {
        public ObservableCollection<Empleado> Empleados { get; set; } = new();
        public Empleado EmpleadoSeleccionado { get; set; }
        
        public ICollectionView EmpleadosView { get; set; }

        
        public string FotoUrlCompleta { get; set; }

        private readonly HttpClient _client;
        
        public string NombreNuevo { get; set; }
        public string DniNuevo { get; set; }
        public string EmailNuevo { get; set; }
        public string PasswordNuevo { get; set; }
        public DateTime FechaNacimientoNuevo { get; set; } = DateTime.Now;
        public string SexoNuevo { get; set; }
        public bool AdministradorNuevo { get; set; }

        public string RutaImagenSeleccionada { get; set; }

        public EmpleadosViewModel()
        {
            _client = new HttpClient
            {
                BaseAddress = new Uri("http://localhost:3000/")
            };
            
            EmpleadosView = CollectionViewSource.GetDefaultView(Empleados);
            
            _ = CargarEmpleados();
            
           
        }
        
        

        public async Task CargarEmpleados()
        {
            try
            {
                var lista = await _client.GetFromJsonAsync<List<Empleado>>("empleado");


                Empleados.Clear();

                foreach (var emp in lista)
                    Empleados.Add(emp);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
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
        
        public async Task CrearEmpleado()
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
                form.Add(new StringContent(AdministradorNuevo.ToString().ToLower()), "administrador");

                // FOTO
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

                var response = await _client.PostAsync("empleado", form);

                if (response.IsSuccessStatusCode)
                {
                    MessageBox.Show("Empleado creado correctamente");
                    await CargarEmpleados();
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
        
        public void CargarEmpleadoParaEditar(Empleado emp)
        {
            EmpleadoSeleccionado = emp;
            

            NombreNuevo = emp.Nombre;
            DniNuevo = emp.DNI;
            EmailNuevo = emp.Email;
            FechaNacimientoNuevo = emp.FechaNacimiento;
            SexoNuevo = emp.Sexo;
            AdministradorNuevo = emp.Administrador;

            RutaImagenSeleccionada = null;
            
            if (!string.IsNullOrEmpty(emp.Foto))
            {
                FotoUrlCompleta = "http://localhost:3000" + emp.Foto;
            }
        }
        
        public async Task ActualizarEmpleado()
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
                form.Add(new StringContent(AdministradorNuevo.ToString().ToLower()), "administrador");

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

                var response = await _client.PutAsync($"empleado/{EmpleadoSeleccionado.Id}", form);

                if (response.IsSuccessStatusCode)
                {
                    MessageBox.Show("Empleado actualizado");
                    await CargarEmpleados();
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

        public async Task EliminarEmpleado()
        {
            if (EmpleadoSeleccionado == null)
            {
                MessageBox.Show("Seleccione un empleado primero.");
                return;
            }

            var resultado = MessageBox.Show(
                $"¿Está seguro de eliminar a {EmpleadoSeleccionado.Nombre}?",
                "Eliminar Empleado",
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
                    var response = await _client.DeleteAsync($"empleado/{EmpleadoSeleccionado.Id}");

                    if (response.IsSuccessStatusCode)
                    {
                        MessageBox.Show("Empleado eliminado correctamente");

                        await CargarEmpleados(); 
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
                EmpleadosView.Filter = null;
            }
            else
            {
                EmpleadosView.Filter = e =>
                {
                    var emp = e as Empleado;
                    return emp.DNI.Contains(dni, StringComparison.OrdinalIgnoreCase);
                };
            }

            EmpleadosView.Refresh();
        }

    }
}

