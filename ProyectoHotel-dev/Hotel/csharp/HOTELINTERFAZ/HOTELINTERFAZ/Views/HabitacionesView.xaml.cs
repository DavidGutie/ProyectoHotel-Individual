using System;
using System.ComponentModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using HOTELINTERFAZ.Models;
using HOTELINTERFAZ.Ventanas;
using HOTELINTERFAZ.ViewModels;

namespace HOTELINTERFAZ.Views
{
    public partial class HabitacionesView : UserControl
    {
        private readonly HabitacionesViewModel _vm;
        private readonly ICollectionView _view;

        public HabitacionesView(HabitacionesViewModel vm)
        {
            InitializeComponent();

            _vm = vm;
            DataContext = _vm;

            DgHabitaciones.ItemsSource = _vm.Habitaciones;

            _view = CollectionViewSource.GetDefaultView(DgHabitaciones.ItemsSource);
            _view.Filter = FilterHabitaciones;
        }

        private bool FilterHabitaciones(object obj)
        {
            if (obj is not Habitacion h) return false;

            var q = TxtBuscar?.Text?.Trim().ToLower() ?? "";
            if (string.IsNullOrWhiteSpace(q)) return true;

            return h.Numero.ToString().Contains(q)
                   || (h.Tipo?.ToLower().Contains(q) ?? false);
        }


        private async void Nueva_Click(object sender, RoutedEventArgs e)
        {
            var nueva = new Habitacion
            {
                Numero = GetNextNumeroDisponible(),
                Tipo = "",
                Descripcion = "",
                MaxOcupantes = 1,
                PrecioNoche = 0,
                Disponible = true,
                Imagen = "https://commons.wikimedia.org/wiki/Special:FilePath/Hotel-room-renaissance-columbus-ohio.jpg"
            };

            var win = new HabitacionCrudWindow(nueva)
            {
                Owner = Window.GetWindow(this)
            };

            if (win.ShowDialog() != true)
                return;

            try
            {
                ValidarHabitacion(nueva, esNueva: true);

                var creada = await _vm.CrearHabitacionAsync(nueva);
                if (creada != null && !string.IsNullOrWhiteSpace(creada.Id))
                    nueva.Id = creada.Id;

                await _vm.CargarHabitaciones();
                _view.Refresh();
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, "Error creando", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private async void Editar_Click(object sender, RoutedEventArgs e)
        {
            if (DgHabitaciones.SelectedItem is not Habitacion selected)
            {
                MessageBox.Show("Selecciona una habitación para editar.", "Info",
                    MessageBoxButton.OK, MessageBoxImage.Information);
                return;
            }

            var copia = new Habitacion
            {
                Id = selected.Id,
                Numero = selected.Numero,
                Tipo = selected.Tipo,
                Descripcion = selected.Descripcion,
                MaxOcupantes = selected.MaxOcupantes,
                PrecioNoche = selected.PrecioNoche,
                Rate = selected.Rate,
                Disponible = selected.Disponible,
                Oferta = selected.Oferta,
                Imagen = selected.Imagen,
                ServiciosTexto = selected.ServiciosTexto
            };

            var win = new HabitacionCrudWindow(copia)
            {
                Owner = Window.GetWindow(this)
            };

            if (win.ShowDialog() != true)
                return;

            try
            {
                ValidarHabitacion(copia, esNueva: false);

                await _vm.ActualizarHabitacionAsync(copia);
                await _vm.CargarHabitaciones();
                _view.Refresh();
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, "Error actualizando", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private async void Eliminar_Click(object sender, RoutedEventArgs e)
        {
            if (DgHabitaciones.SelectedItem is not Habitacion selected)
            {
                MessageBox.Show("Selecciona una habitación para eliminar.", "Info",
                    MessageBoxButton.OK, MessageBoxImage.Information);
                return;
            }

            var res = MessageBox.Show(
                $"¿Eliminar la habitación {selected.Numero}?",
                "Confirmación",
                MessageBoxButton.YesNo,
                MessageBoxImage.Warning);

            if (res == MessageBoxResult.Yes)
            {
                try
                {
                    await _vm.EliminarHabitacionAsync(selected);
                    await _vm.CargarHabitaciones();
                    _view.Refresh();
                }
                catch (Exception ex)
                {
                    MessageBox.Show(ex.Message, "Error eliminando", MessageBoxButton.OK, MessageBoxImage.Error);
                }
            }
        }

        private void Buscar_Click(object sender, RoutedEventArgs e)
        {
            _view.Refresh();
        }


        private void ValidarHabitacion(Habitacion h, bool esNueva)
        {
            if (h.Numero <= 0)
                throw new Exception("El número debe ser mayor que 0.");

            if (string.IsNullOrWhiteSpace(h.Tipo))
                throw new Exception("El tipo es obligatorio.");

            if (h.MaxOcupantes <= 0)
                throw new Exception("La capacidad debe ser mayor que 0.");

            if (h.PrecioNoche < 0)
                throw new Exception("El precio/noche no puede ser negativo.");

            var repes = _vm.Habitaciones.Count(x => x.Numero == h.Numero);
            if (esNueva)
            {
                if (repes >= 1)
                    throw new Exception("Ya existe una habitación con ese número.");
            }
            else
            {
                if (repes > 1)
                    throw new Exception("Ya existe una habitación con ese número.");
            }

            if (string.IsNullOrWhiteSpace(h.Imagen))
                throw new Exception("La habitación debe tener una imagen asociada.");
        }

        private int GetNextNumeroDisponible()
        {
            if (_vm.Habitaciones.Count == 0) return 1;
            return _vm.Habitaciones.Max(h => h.Numero) + 1;
        }
    }
}