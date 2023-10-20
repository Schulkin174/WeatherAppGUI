import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Scanner;

// получим данные о погоде из api
public class WeatherApp {
// получаем данные о погоде на конкретную локацию
    public static JSONObject  getWeatherData(String locationName){
        // получаем координаты локации используя апи для геолокации
        JSONArray locatoinData = getLocationData(locationName);

        // извлекаем данные о широте и долготе
        JSONObject location = (JSONObject) locatoinData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // создаем URL-адрес запроса API с координатами местоположения
        // установил время по Москве
        // в качестве шаблона использую апи https://api.open-meteo.com/v1/forecast?latitude=33.767&longitude=-118.1892&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&windspeed_unit=ms&timezone=Europe%2FMoscow
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&timezone=America%2FLos_Angeles";

        try {
        // вызываем апи и получам response
            HttpURLConnection connection = fetchApiResponce(urlString);
        // чекаем статус ответа: 200, ок?
            if(connection.getResponseCode() != 200){
                System.out.println("Error: could not connect to API");
                return null;
            }

        // сохраняем результат в JSON
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(connection.getInputStream());
            while (scanner.hasNext()){
                resultJson.append(scanner.nextLine());
            }

            scanner.close();
            connection.disconnect();

            // анализируем данные
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            // извлекать временные (почасовые) данные
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

            // мы хотим получить данные за текущий час
            // поэтому нам нужно получить индекс нашего текущего часа
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            // получаем температуру
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            // получаем "код погоды" из апи
            JSONArray weathercode = (JSONArray) hourly.get("weathercode");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            // влажность
            JSONArray relativeHumidity = (JSONArray) hourly.get("relativehumidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            // скорость ветра
            JSONArray windspeedData = (JSONArray) hourly.get("windspeed_10m");
            double windspeed = (double) windspeedData.get(index);

            // создаем наш собственный объект данных json, к которому мы собираемся получить доступ в нашем графическом интерфейсе
            // для того чтобы получить доступ ко всем данным и отобразить их в нашем GUI
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);

            return weatherData;

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    // извлекаем географические координаты для заданного названия местоположения
    public static JSONArray getLocationData(String locationName){
        // заменяем пробелы в названии мест на "+" чтобы соответствовать формату запроса API (например, "new+york")
        locationName = locationName.replaceAll(" ", "+");

        // создаем URL API с параметром местоположения
        // использую открытое апи с https://geocoding-api.open-meteo.com
        // за образец беру https://geocoding-api.open-meteo.com/v1/search?name=Berlin&count=10&language=en&format=json
        // и подстраиваю под свои нужды, заменяя Берлин на locationName
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
        locationName + "&count=10&language=en&format=json";

        try{
        // вызываем апи и ждем ответ
            HttpURLConnection connection = fetchApiResponce(urlString);

            // чекаем статус ответа от сервера
            if(connection.getResponseCode() !=200) {
                System.out.println("Error: could not connect to API");
                return null;
            }else{
                // запоминаем результат апи
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream()); // используем сканер для чтения данных из JSONки
                while (scanner.hasNext()){
                    resultJson.append((scanner.nextLine()));
                }
            scanner.close();
            connection.disconnect();

            // парсим JSON string в JSON obj
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                // получаем список локаций, сгенерированный из имен локаций
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static HttpURLConnection fetchApiResponce(String urlString){
        try {
            // попытка создать соединение
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // устанавливаем в качестве метода GET-запрос
            connection.setRequestMethod("GET");

            // коннектимся к нашему апи
            connection.connect();
            return connection;
        }catch (IOException e){
            e.printStackTrace();
        }

        // если соединение отсутствует:
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList){
        String currentTime = getCurrentTime();

    // пройдитесь по списку времени и посмотрите, какое из них соответствует нашему текущему времени
        for (int i =0; i < timeList.size(); i++){
            String time = (String) timeList.get(i);
            if(time.equalsIgnoreCase(currentTime));
            return i;
        }
        return 0;
    }
    public static String getCurrentTime(){
   // получаем актуальные дату и время
        LocalDateTime currentDateTime = LocalDateTime.now();

    // указываем формат даты-времени
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

    // форматируем и печатаем (выводим на экран) текущие дату-время
        String formattedDataTime = currentDateTime.format(formatter);
        return formattedDataTime;
    }
    // конфертируем погодый код во что-то более читабельное
    private static String convertWeatherCode(long weathercode){
        String weatherCondition = "";
        if(weathercode == 0L){
            weatherCondition = "Clear";
        }else if (weathercode <= 3L && weathercode > 0L){
            weatherCondition = "Cloudy";
        }else if ((weathercode >= 51L && weathercode <= 67L) || (weathercode >= 80L && weathercode <= 99L)){
            weatherCondition = "Rain";
        }else if (weathercode >= 71L && weathercode <= 77L){
            weatherCondition = "Snow";
        }
        return weatherCondition;
    }
}
