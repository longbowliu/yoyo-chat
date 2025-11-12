package com.xc.voicechat.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xc.voicechat.config.WeatherProperties;
import com.xc.voicechat.util.WeatherUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class WeatherTools {

    private static final Logger logger = LoggerFactory.getLogger(WeatherTools.class);

    // 知心天气API地址
    private static final String SENIVERSE_API_URL = "https://api.seniverse.com/v3/weather/daily.json";

    private final WeatherProperties weatherProperties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public WeatherTools(WeatherProperties weatherProperties) {
        this.weatherProperties = weatherProperties;
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }

    @Tool(description = "获取天气信息")
    public SeniverseResponse getWeatherServiceMethod(
            @ToolParam(description = "城市名称，如：北京、上海、广州") String city,
            @ToolParam(description = "天气预报天数，范围从1到5") int days) {

        if (!StringUtils.hasText(city)) {
            logger.error("无效请求：城市名称不能为空");
            return null;
        }

        if (days < 1 || days > 5) {
            logger.warn("天数参数超出范围(1-5)，使用默认值3");
            days = 3;
        }

        try {
            // 预处理城市名称
            String location = WeatherUtils.preprocessLocation(city);

            // 构建API请求URL
            String url = UriComponentsBuilder.fromUriString(SENIVERSE_API_URL)
                    .queryParam("key", weatherProperties.getKey())
                    .queryParam("location", location)
                    .queryParam("language", "zh-Hans") // 中文返回
                    .queryParam("unit", "c")          // 摄氏度单位
                    .queryParam("start", 0)           // 从今天开始
                    .queryParam("days", days)
                    .toUriString();

            logger.info("调用知心天气API: {}", url);

            // 发送请求并获取响应
            Mono<String> responseMono = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class);

            String jsonResponse = responseMono.block();
            
            if (jsonResponse == null) {
                logger.error("API响应为空");
                return null;
            }

            // 解析JSON响应
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse,
                    new TypeReference<>() {
                    });
            
            SeniverseResponse response = fromJson(responseMap);
            logger.info("成功获取 {} 的天气数据", response.location().name());
            
            return response;

        } catch (Exception e) {
            logger.error("获取天气数据失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从JSON映射转换为响应对象
     */
    public static SeniverseResponse fromJson(Map<String, Object> json) {
        List<Map<String, Object>> results = (List<Map<String, Object>>) json.get("results");
        
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException("API响应中未包含有效结果");
        }

        Map<String, Object> result = results.get(0);
        Map<String, Object> location = (Map<String, Object>) result.get("location");
        List<Map<String, Object>> daily = (List<Map<String, Object>>) result.get("daily");
        String lastUpdate = (String) result.get("last_update");

        return new SeniverseResponse(
            new Location(
                (String) location.get("id"),
                (String) location.get("name"),
                (String) location.get("country"),
                (String) location.get("path"),
                (String) location.get("timezone"),
                (String) location.get("timezone_offset")
            ),
            daily,
            lastUpdate
        );
    }

    /**
     * 响应记录类
     */
    public record SeniverseResponse(Location location, List<Map<String, Object>> daily, String lastUpdate) {}

    /**
     * 位置信息记录类
     */
    public record Location(
        String id,
        String name,
        String country,
        String path,
        String timezone,
        String timezoneOffset
    ) {}
}