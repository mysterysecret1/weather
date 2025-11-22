import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UniversalWeatherScraper {
    
    public static class WeatherData {
        private String date;
        private String dayOfWeek;
        private String weather;
        private String temperature;
        private String highTemp;
        private String lowTemp;
        private String humidity;
        private String wind;
        private String airQuality;
        private String uvIndex;
        
        public WeatherData() {}
        
        // Getter and Setter 方法
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public String getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        
        public String getWeather() { return weather; }
        public void setWeather(String weather) { this.weather = weather; }
        
        public String getTemperature() { return temperature; }
        public void setTemperature(String temperature) { this.temperature = temperature; }
        
        public String getHighTemp() { return highTemp; }
        public void setHighTemp(String highTemp) { this.highTemp = highTemp; }
        
        public String getLowTemp() { return lowTemp; }
        public void setLowTemp(String lowTemp) { this.lowTemp = lowTemp; }
        
        public String getHumidity() { return humidity; }
        public void setHumidity(String humidity) { this.humidity = humidity; }
        
        public String getWind() { return wind; }
        public void setWind(String wind) { this.wind = wind; }
        
        public String getAirQuality() { return airQuality; }
        public void setAirQuality(String airQuality) { this.airQuality = airQuality; }
        
        public String getUvIndex() { return uvIndex; }
        public void setUvIndex(String uvIndex) { this.uvIndex = uvIndex; }
        
        @Override
        public String toString() {
            return String.format("日期: %s(%s) | 天气: %s | 温度: %s | 湿度: %s | 风力: %s", 
                    date, dayOfWeek, weather, temperature, humidity, wind);
        }
    }
    
    /**
     * 通用天气爬取方法 - 需要根据实际网页结构调整选择器
     */
    public static List<WeatherData> scrapeWeather(String url) {
        List<WeatherData> result = new ArrayList<>();
        
        try {
            // 连接并获取网页
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();
            
            // 首先分析页面结构
            analyzePageStructure(doc);
            
            // 尝试多种可能的选择器模式
            result = tryMultipleSelectors(doc);
            
            // 如果仍然没有数据，使用调试模式
            if (result.isEmpty()) {
                System.out.println("使用调试模式分析页面...");
                debugPageAnalysis(doc);
            }
            
        } catch (IOException e) {
            System.err.println("网络请求失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("解析失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 分析页面结构
     */
    private static void analyzePageStructure(Document doc) {
        System.out.println("=== 页面结构分析 ===");
        System.out.println("页面标题: " + doc.title());
        
        // 查找可能包含天气信息的容器
        String[] possibleContainers = {
            "div.forecast", "div.weather", "div.wea_list", "ul.weather", 
            "table", "div.days", "div.forecast-box", "div.week"
        };
        
        for (String container : possibleContainers) {
            Elements elements = doc.select(container);
            if (!elements.isEmpty()) {
                System.out.println("找到容器: " + container + " - 数量: " + elements.size());
            }
        }
    }
    
    /**
     * 尝试多种选择器策略
     */
    private static List<WeatherData> tryMultipleSelectors(Document doc) {
        List<WeatherData> result = new ArrayList<>();
        
        // 策略1: 基于类的选择器
        result = tryClassBasedSelectors(doc);
        if (!result.isEmpty()) return result;
        
        // 策略2: 基于表格的选择器
        result = tryTableBasedSelectors(doc);
        if (!result.isEmpty()) return result;
        
        // 策略3: 基于列表的选择器
        result = tryListBasedSelectors(doc);
        
        return result;
    }
    
    /**
     * 基于类的选择器
     */
    private static List<WeatherData> tryClassBasedSelectors(Document doc) {
        List<WeatherData> result = new ArrayList<>();
        
        // 常见的天气网站类名
        String[][] selectors = {
            {"div.forecast_item", "div.date", "div.weather", "div.temp"},
            {"li.weather-item", "span.date", "span.weather", "span.temp"},
            {"div.day-item", "div.day_date", "div.wea", "div.tem"},
            {"div.weather-box", "div.dates", "div.condition", "div.temperature"}
        };
        
        for (String[] selector : selectors) {
            Elements items = doc.select(selector[0]);
            if (!items.isEmpty()) {
                System.out.println("找到匹配的选择器: " + selector[0]);
                
                for (Element item : items) {
                    WeatherData data = new WeatherData();
                    data.setDate(item.select(selector[1]).text());
                    data.setWeather(item.select(selector[2]).text());
                    data.setTemperature(item.select(selector[3]).text());
                    
                    if (!data.getDate().isEmpty() || !data.getWeather().isEmpty()) {
                        result.add(data);
                    }
                }
                break;
            }
        }
        
        return result;
    }
    
    /**
     * 基于表格的选择器
     */
    private static List<WeatherData> tryTableBasedSelectors(Document doc) {
        List<WeatherData> result = new ArrayList<>();
        
        Elements tables = doc.select("table");
        for (Element table : tables) {
            Elements rows = table.select("tr");
            
            // 跳过表头，从第二行开始
            for (int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cells = row.select("td");
                
                if (cells.size() >= 3) {
                    WeatherData data = new WeatherData();
                    data.setDate(cells.get(0).text());
                    data.setWeather(cells.get(1).text());
                    data.setTemperature(cells.get(2).text());
                    
                    if (cells.size() > 3) data.setWind(cells.get(3).text());
                    
                    result.add(data);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 基于列表的选择器
     */
    private static List<WeatherData> tryListBasedSelectors(Document doc) {
        List<WeatherData> result = new ArrayList<>();
        
        // 查找所有可能的列表项
        Elements listItems = doc.select("li");
        for (Element item : listItems) {
            String text = item.text();
            
            // 如果包含天气关键词，认为是天气数据
            if (text.matches(".*(晴|雨|阴|云|雪|风|℃|°C|温度).*")) {
                WeatherData data = new WeatherData();
                data.setTemperature(extractTemperature(text));
                data.setWeather(extractWeather(text));
                data.setDate(extractDate(text));
                result.add(data);
            }
        }
        
        return result;
    }
    
    /**
     * 调试模式：详细分析页面
     */
    private static void debugPageAnalysis(Document doc) {
        System.out.println("\n=== 详细页面分析 ===");
        
        // 显示所有div的类名
        Elements divs = doc.select("div[class]");
        System.out.println("页面中的div类名:");
        divs.forEach(div -> {
            String className = div.className();
            if (className.matches(".*(weather|forecast|day|date|temp).*")) {
                System.out.println("相关div: " + className + " - 内容: " + div.text().substring(0, Math.min(50, div.text().length())));
            }
        });
        
        // 显示页面中所有图片的alt属性（可能包含天气信息）
        Elements images = doc.select("img[alt]");
        System.out.println("\n图片alt属性:");
        images.forEach(img -> {
            String alt = img.attr("alt");
            if (alt.matches(".*(晴|雨|阴|云|雪).*")) {
                System.out.println("天气图片: " + alt);
            }
        });
    }
    
    // 辅助方法：从文本中提取信息
    private static String extractTemperature(String text) {
        if (text.matches(".*\\d+~\\d+°C.*")) {
            return text.replaceAll(".*?(\\d+~\\d+°C).*", "$1");
        } else if (text.matches(".*\\d+°C.*")) {
            return text.replaceAll(".*?(\\d+°C).*", "$1");
        }
        return "";
    }
    
    private static String extractWeather(String text) {
        if (text.matches(".*(晴|多云|阴|雨|雪).*")) {
            return text.replaceAll(".*?((晴|多云|阴|雨|雪)).*", "$1");
        }
        return "";
    }
    
    private static String extractDate(String text) {
        if (text.matches(".*\\d+月\\d+日.*")) {
            return text.replaceAll(".*?(\\d+月\\d+日).*", "$1");
        } else if (text.matches(".*周[一二三四五六日].*")) {
            return text.replaceAll(".*?(周[一二三四五六日]).*", "$1");
        }
        return "";
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        String url = "https://tianqi.moji.com/forecast15/china/guangdong/guangming-district";
        
        System.out.println("开始爬取天气数据...");
        List<WeatherData> weatherData = scrapeWeather(url);
        
        System.out.println("\n=== 爬取结果 ===");
        System.out.println("获取到 " + weatherData.size() + " 条天气数据");
        
        for (int i = 0; i < weatherData.size(); i++) {
            System.out.println((i + 1) + ". " + weatherData.get(i));
        }
        
        // 如果没有获取到数据，提供调试建议
        if (weatherData.isEmpty()) {
            System.out.println("\n没有获取到数据，请执行以下步骤:");
            System.out.println("1. 手动访问该网址，确认页面正常显示");
            System.out.println("2. 右键查看页面源代码，找到天气数据的HTML结构");
            System.out.println("3. 根据实际HTML结构调整代码中的选择器");
            System.out.println("4. 检查是否需要处理动态加载的内容");
        }
    }
}