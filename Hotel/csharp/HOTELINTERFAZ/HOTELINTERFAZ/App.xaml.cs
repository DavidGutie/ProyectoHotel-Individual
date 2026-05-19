using System;
using System.IO;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Threading;

namespace HOTELINTERFAZ
{
    /// <summary>
    /// Interaction logic for App.xaml
    /// </summary>
    public partial class App : Application
    {
        protected override void OnStartup(StartupEventArgs e)
        {
            base.OnStartup(e);

            DispatcherUnhandledException += App_DispatcherUnhandledException;
            TaskScheduler.UnobservedTaskException += TaskScheduler_UnobservedTaskException;
            AppDomain.CurrentDomain.UnhandledException += CurrentDomain_UnhandledException;
        }

        private void App_DispatcherUnhandledException(object sender, DispatcherUnhandledExceptionEventArgs e)
        {
            MostrarError("Error no controlado en la interfaz", e.Exception);
            e.Handled = true;
        }

        private void TaskScheduler_UnobservedTaskException(object? sender, UnobservedTaskExceptionEventArgs e)
        {
            MostrarError("Error no observado en una tarea", e.Exception);
            e.SetObserved();
        }

        private void CurrentDomain_UnhandledException(object sender, UnhandledExceptionEventArgs e)
        {
            if (e.ExceptionObject is Exception ex)
            {
                GuardarError("Error fatal", ex);
            }
        }

        private static void MostrarError(string titulo, Exception ex)
        {
            GuardarError(titulo, ex);
            MessageBox.Show(
                $"{titulo}:\n{ex.Message}\n\nDetalle guardado en hotel-wpf-error.log",
                "Error",
                MessageBoxButton.OK,
                MessageBoxImage.Error);
        }

        private static void GuardarError(string titulo, Exception ex)
        {
            try
            {
                var ruta = Path.Combine(AppContext.BaseDirectory, "hotel-wpf-error.log");
                File.AppendAllText(
                    ruta,
                    $"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] {titulo}{Environment.NewLine}{ex}{Environment.NewLine}{Environment.NewLine}");
            }
            catch
            {
                // Evita que el logger provoque otro cierre si no hay permisos de escritura.
            }
        }
    }
}
