using System.Globalization;
using System.Windows;

namespace HOTELINTERFAZ.Ventanas
{
    public partial class ExtraReservaWindow : Window
    {
        public string Concepto { get; private set; }
        public double Importe { get; private set; }

        public ExtraReservaWindow()
        {
            InitializeComponent();
        }

        private void Guardar_Click(object sender, RoutedEventArgs e)
        {
            if (string.IsNullOrWhiteSpace(TxtConcepto.Text))
            {
                MessageBox.Show("Indique el concepto del extra.");
                return;
            }

            var importeTexto = TxtImporte.Text.Replace(',', '.');
            if (!double.TryParse(importeTexto, NumberStyles.Number, CultureInfo.InvariantCulture, out var importe) || importe < 0)
            {
                MessageBox.Show("Indique un importe válido.");
                return;
            }

            Concepto = TxtConcepto.Text.Trim();
            Importe = importe;
            DialogResult = true;
        }

        private void Cancelar_Click(object sender, RoutedEventArgs e)
        {
            DialogResult = false;
        }
    }
}
