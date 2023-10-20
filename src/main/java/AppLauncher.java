import javax.swing.*;

public class AppLauncher {
    public static void main(String[] args) {
        // Метод invokeLater используется для постановки кода в очередь на выполнение в потоке обработки событий Swing,
        // чтобы избежать проблем с многопоточностью во время работы Gui
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // отображаем окно нашего погодного приложения
              new WeatherAppGui().setVisible(true);

                //         System.out.println(WeatherApp.getLocationData("Tokyo")); // использую для проверки

                // System.out.println(WeatherApp.getCurrentTime());
            }
        });
    }
}
