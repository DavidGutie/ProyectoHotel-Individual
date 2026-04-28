using HOTELINTERFAZ.Models;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Windows;

namespace HOTELINTERFAZ.Ventanas
{
    public partial class HistorialReservaWindow : Window
    {
        public ObservableCollection<ReservaAudit> Historial { get; } = new();

        public HistorialReservaWindow(IEnumerable<ReservaAudit> historial, string reservaId)
        {
            InitializeComponent();

            if (historial != null)
            {
                foreach (var item in historial)
                    Historial.Add(item);
            }

            TxtTitulo.Text = $"Historial de la reserva {reservaId}";
            DataContext = this;
        }
    }
}