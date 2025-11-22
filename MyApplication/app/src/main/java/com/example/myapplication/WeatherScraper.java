package com.example.myapplication;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WeatherScraper {

    public static class WeatherData {
        private String date;
        private String weather;
        private String temperature;
        private String humidity;
        private String wind;

        public WeatherData(String date, String weather, String temperature, String humidity, String wind) {
            this.date = date;
            this.weather = weather;
            this.temperature = temperature;
            this.humidity = humidity;
            this.wind = wind;
        }

        // Getter方法
        public String getDate() { return date; }
        public String getWeather() { return weather; }
        public String getTemperature() { return temperature; }
        public String getHumidity() { return humidity; }
        public String getWind() { return wind; }

        @Override
        public String toString() {
            return String.format("%s: %s, %s, 湿度:%s, %s",
                    date, weather, temperature, humidity, wind);
        }
    }

    public interface WeatherCallback {
        void onSuccess(List<WeatherData> data);
        void onError(String error);
    }

    public static void getWeatherData(final WeatherCallback callback) {
        new Thread(() -> {
            try {
                String url = "https://tianqi.moji.com/forecast15/china/guangdong/guangming-district";

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(10000)
                        .get();

                List<WeatherData> weatherList = new ArrayList<>();

                // 这里需要根据实际网页结构调整选择器
                // 以下是示例选择器，需要你根据实际网页调整
                Elements weatherItems = doc.select("div.forecast_item, li.weather-item, div.weather-box");

                for (Element item : weatherItems) {
                    String date = item.select("div.date, span.date").text();
                    String weather = item.select("div.weather, span.weather").text();
                    String temp = item.select("div.temp, span.temp").text();
                    String humidity = item.select("div.humidity, span.humidity").text();
                    String wind = item.select("div.wind, span.wind").text();

                    if (!date.isEmpty() || !weather.isEmpty()) {
                        weatherList.add(new WeatherData(date, weather, temp, humidity, wind));
                    }
                }

                // 回调成功结果
                callback.onSuccess(weatherList);

            } catch (IOException e) {
                callback.onError("网络请求失败: " + e.getMessage());
            } catch (Exception e) {
                callback.onError("解析失败: " + e.getMessage());
            }
        }).start();
    }
}
