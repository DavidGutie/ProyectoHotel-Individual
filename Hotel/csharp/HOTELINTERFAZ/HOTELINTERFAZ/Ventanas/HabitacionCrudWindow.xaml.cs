using System;
using System.Collections.Generic;
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

        public HabitacionCrudWindow(Habitacion habitacion)
        {
            InitializeComponent();
            Habitacion = habitacion;
            DataContext = Habitacion;
        }

        private void Guardar_Click(object sender, RoutedEventArgs e)
        {
            DialogResult = true;
        }

        private void Cancelar_Click(object sender, RoutedEventArgs e)
        {
            DialogResult = false;
        }
    }
}
