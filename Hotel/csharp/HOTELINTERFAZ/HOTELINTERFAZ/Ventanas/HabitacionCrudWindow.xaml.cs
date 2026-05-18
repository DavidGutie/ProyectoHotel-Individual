using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using HOTELINTERFAZ.Models;

namespace HOTELINTERFAZ.Ventanas
{
    public partial class HabitacionCrudWindow : Window
    {
        public Habitacion Habitacion { get; }
        private readonly List<SelectableAmenity> _selectableAmenities = new();

        public HabitacionCrudWindow(Habitacion habitacion)
            : this(habitacion, Enumerable.Empty<Amenity>())
        {
        }

        public HabitacionCrudWindow(Habitacion habitacion, IEnumerable<Amenity> amenities)
        {
            InitializeComponent();
            Habitacion = habitacion;
            DataContext = Habitacion;
            CargarAmenities(amenities);
        }

        private void CargarAmenities(IEnumerable<Amenity> amenities)
        {
            var seleccionados = Habitacion.AmenityIds.ToHashSet();
            _selectableAmenities.Clear();

            foreach (var amenity in amenities)
            {
                _selectableAmenities.Add(new SelectableAmenity
                {
                    Amenity = amenity,
                    IsSelected = seleccionados.Contains(amenity.Id)
                });
            }

            AmenityCategoriesItems.ItemsSource = _selectableAmenities
                .GroupBy(a => string.IsNullOrWhiteSpace(a.Amenity.Category) ? "Sin categoría" : a.Amenity.Category)
                .OrderBy(g => g.Key)
                .Select(g => new AmenityCategoryGroup
                {
                    Category = g.Key,
                    Items = new ObservableCollection<SelectableAmenity>(g.OrderBy(a => a.Amenity.Name))
                })
                .ToList();
        }

        private void Guardar_Click(object sender, RoutedEventArgs e)
        {
            Habitacion.Amenities.Clear();
            foreach (var item in _selectableAmenities.Where(a => a.IsSelected))
                Habitacion.Amenities.Add(item.Amenity);

            DialogResult = true;
        }

        private void Cancelar_Click(object sender, RoutedEventArgs e)
        {
            DialogResult = false;
        }
    }

    public class SelectableAmenity
    {
        public Amenity Amenity { get; set; }
        public bool IsSelected { get; set; }
        public string DisplayName => Amenity?.DisplayName ?? "";
    }

    public class AmenityCategoryGroup
    {
        public string Category { get; set; } = "";
        public ObservableCollection<SelectableAmenity> Items { get; set; } = new();
    }
}
