import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import static java.awt.Cursor.HAND_CURSOR;

public class WeatherAppGui extends JFrame {

    private JSONObject weatherData;
    public WeatherAppGui(){
        // настраиваем Gui и добавляем заголовок
        super("Weather App");
        // программа завершит работу после закрытия интерфейса
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // устанавливаем размер окна приложения в пикселях
        setSize(450, 650);
        // выставляем окно программы по центру экрана
        setLocationRelativeTo(null);
        // отключаем менеджер компоновки (выставляем на ноль), чтобы установить графические элементы вручную
        // это позволит нам вручную задать все координаты и размеры компонентов (кнопки и пр)
        // setLayout - это метод, используемый для установки менеджера компоновки
        setLayout(null);
        // предотвращаем любое изменение нашего Gui
        setResizable(false);

        // добавляем элементы в главное окно нашего приложения
        addGuiComponents();
    }
    private void addGuiComponents(){
        // строка поиска
        JTextField searchTextField = new JTextField();
        searchTextField.setBounds(15, 15, 351, 45);
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));
        add(searchTextField);



        // картинка, отображающая текущую погоду
        // JLabel - метка для отображения статической информации, в данном случае картинки, с которой пользователь не может взаимодействовать
        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        // температура (текст)
        JLabel temperatureText = new JLabel("10 C");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        // описание погоды
        JLabel weatherConditionDescription = new JLabel("Cloudy");
        weatherConditionDescription.setBounds(0,405, 450, 36);
        weatherConditionDescription.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDescription.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDescription);

        // влажность воздуха
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        // текстовое описание влажности воздуха
        JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>"); // так тоже можно :)
        humidityText.setBounds(90, 500, 74, 66);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        // иконка скорости ветра
        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windspeedImage.setBounds(220, 500, 74, 66);
        add(windspeedImage);

        // описание скорости ветра
        JLabel windspeedText = new JLabel("<html><b>Windspeed</b> 15km/h</html>");
        windspeedText.setBounds(310, 500, 85, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);


        // кнопки меню
        JButton searchButton = new JButton(loadImage("src/assets/search.png"));
        // меняем курсор на ручной при наведении на эту кнопку "поиска"
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                // получаем локацию от пользователя (ввод)
                String userInput = searchTextField.getText();
                // убеждаемся что пользователь ввел текст, а не оставил пустое поле
                if (userInput.replaceAll("\\s", "").length() <= 0){
                    return;
                }
                // извлекаем погодные данные
                weatherData = WeatherApp.getWeatherData(userInput);

                // обновляем GUI
                // обновляем иконку погоды
                String weatherCondition = (String) weatherData.get("weather_condition");

                // в зависимости от условий мы обновим изображение погоды, соответствующее этим условиям
                switch (weatherCondition){
                    case "Clear":
                        weatherConditionImage.setIcon(loadImage("src/assets/clear.png"));
                        break;
                    case "Cloudy":
                        weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                        break;
                    case "Rain":
                        weatherConditionImage.setIcon(loadImage("src/assets/rain.png"));
                        break;
                    case "Snow":
                        weatherConditionImage.setIcon(loadImage("src/assets/snow.png"));
                        break;
                }
                // обновляем текст температуры
                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + " C");

                // обновляем текст погоды
                JLabel weatherConditionDesc; // проверить эту часть кода
                weatherConditionDesc = new JLabel(); // проверить эту часть кода
                weatherConditionDesc.setText(weatherCondition); // проверить эту часть кода


                // обновляем текст влажности
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");

                // обновляем текст скорости ветра
                double windspeed = (double) weatherData.get("windspeed");
                windspeedText.setText("<html><b>Windspeed</b> " + windspeed + "km/h</html>");
            }
        });
        add(searchButton);

    }
    // загружаю иконку "поиска"
    private ImageIcon loadImage(String resoursePath){
        try {
            // читаем image из указанного пути
            BufferedImage image = ImageIO.read(new File(resoursePath));
            // отображаем иконку если смогли прочитать её
            return new ImageIcon(image);
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Could not find resource");
        return null;
    }
}
