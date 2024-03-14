package ru.practicum.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import ru.practicum.exception.StatsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.StatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatClient {
    private final RestTemplate restTemplate;

    public static final String DATE = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    public StatClient(@Value("http://localhost:9090") String serverUrl, RestTemplateBuilder restTemplateBuilder) {
        restTemplate = restTemplateBuilder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public EndpointHitDto createEndpointHit(EndpointHitDto endpointHitDto) {
        HttpEntity<EndpointHitDto> requestHttpEntity = new HttpEntity<>(endpointHitDto);
        ResponseEntity<EndpointHitDto> response;

        try {
            response = restTemplate.exchange("/hit", HttpMethod.POST, requestHttpEntity, EndpointHitDto.class);
        } catch (Exception ex) {
            throw new StatsException("Ошибка во время сохранения данных", ex);
        }

        HttpStatus statusCode = response.getStatusCode();
        if (statusCode != HttpStatus.CREATED) {
            throw new StatsException("Ошибка во время сохранения данных. Ожидался код ответа 201, вместо ", statusCode);
        }

        return response.getBody();
    }

    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, @Nullable List<String> uris, @Nullable Boolean unique) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity requestEntity = new HttpEntity<>(headers);

        StringBuilder path = new StringBuilder();
        path.append("/stats?start={start}&end={end}");
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("start", start.format(DateTimeFormatter.ofPattern(DATE)));
        parameters.put("end", end.format(DateTimeFormatter.ofPattern(DATE)));

        if (uris != null && !uris.isEmpty()) {
            parameters.put("uris", uris);
            path.append("&uris={uris}");
        }

        if (unique != null) {
            parameters.put("unique", unique);
            path.append("&unique={unique}");
        }

        ResponseEntity<List<StatsDto>> response;

        try {
            response = restTemplate.exchange(path.toString(), HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<>() {}, parameters);
        } catch (HttpStatusCodeException e) {
            throw new StatsException("Произошла ошибка при обращении к ", e);
        }

        return response.getBody();
    }
}
