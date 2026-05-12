using HOTELINTERFAZ.Models;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Windows;
using System.Windows.Data;

namespace HOTELINTERFAZ.Ventanas
{
    public partial class HistorialReservaWindow : Window
    {
        public ObservableCollection<ReservaAudit> Historial { get; } = new();
        public ICollectionView HistorialView { get; }
        public ObservableCollection<string> FiltrosAccion { get; } = new()
        {
            "Todas",
            "CREAR",
            "MODIFICAR",
            "CANCELAR",
            "PAGO",
            "EXTRA",
            "ELIMINAR"
        };

        private string _filtroAccion = "Todas";
        public string FiltroAccion
        {
            get => _filtroAccion;
            set
            {
                _filtroAccion = value;
                HistorialView.Refresh();
            }
        }

        public HistorialReservaWindow(IEnumerable<ReservaAudit> historial, string reservaId)
        {
            InitializeComponent();

            if (historial != null)
            {
                foreach (var item in historial)
                    Historial.Add(item);
            }

            HistorialView = CollectionViewSource.GetDefaultView(Historial);
            HistorialView.Filter = FiltrarHistorial;

            TxtTitulo.Text = $"Historial de la reserva {reservaId}";
            DataContext = this;
        }

        private bool FiltrarHistorial(object item)
        {
            if (item is not ReservaAudit audit)
                return false;

            return FiltroAccion == "Todas" || audit.Action == FiltroAccion;
        }
    }
}
