using System;
using System.Windows;
using System.Windows.Controls;
using HOTELINTERFAZ.Models;
using HOTELINTERFAZ.ViewModels;

namespace HOTELINTERFAZ.Ventanas
{
    public partial class AmenitiesCatalogWindow : Window
    {
        private readonly HabitacionesViewModel _vm;

        public AmenitiesCatalogWindow(HabitacionesViewModel vm)
        {
            InitializeComponent();
            _vm = vm;
            DgAmenities.ItemsSource = _vm.Amenities;
        }

        private void DgAmenities_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (DgAmenities.SelectedItem is not Amenity amenity)
                return;

            TxtIcono.Text = amenity.Icon;
            TxtNombre.Text = amenity.Name;
            TxtCategoria.Text = amenity.Category;
        }

        private void Nuevo_Click(object sender, RoutedEventArgs e)
        {
            DgAmenities.SelectedItem = null;
            TxtIcono.Clear();
            TxtNombre.Clear();
            TxtCategoria.Clear();
            TxtNombre.Focus();
        }

        private async void Guardar_Click(object sender, RoutedEventArgs e)
        {
            var nombre = TxtNombre.Text.Trim();
            var categoria = TxtCategoria.Text.Trim();

            if (string.IsNullOrWhiteSpace(nombre) || string.IsNullOrWhiteSpace(categoria))
            {
                MessageBox.Show("Nombre y categoría son obligatorios.", "Validación",
                    MessageBoxButton.OK, MessageBoxImage.Warning);
                return;
            }

            try
            {
                if (DgAmenities.SelectedItem is Amenity selected)
                {
                    selected.Icon = TxtIcono.Text.Trim();
                    selected.Name = nombre;
                    selected.Category = categoria;
                    await _vm.ActualizarAmenityAsync(selected);
                }
                else
                {
                    await _vm.CrearAmenityAsync(new Amenity
                    {
                        Icon = TxtIcono.Text.Trim(),
                        Name = nombre,
                        Category = categoria
                    });
                }

                await _vm.CargarAmenitiesAsync();
                Nuevo_Click(sender, e);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, "Error guardando amenity", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private async void Eliminar_Click(object sender, RoutedEventArgs e)
        {
            if (DgAmenities.SelectedItem is not Amenity amenity)
            {
                MessageBox.Show("Selecciona un amenity para eliminar.", "Info",
                    MessageBoxButton.OK, MessageBoxImage.Information);
                return;
            }

            var res = MessageBox.Show(
                $"¿Eliminar el amenity {amenity.Name}?",
                "Confirmación",
                MessageBoxButton.YesNo,
                MessageBoxImage.Warning);

            if (res != MessageBoxResult.Yes)
                return;

            try
            {
                await _vm.EliminarAmenityAsync(amenity);
                await _vm.CargarAmenitiesAsync();
                await _vm.CargarHabitaciones();
                Nuevo_Click(sender, e);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, "Error eliminando amenity", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }
    }
}
