package ru.hpclab.hl.module2.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class KillerClient {

    @Value("${baseUrl}")
    private String baseUrl;

    @Value("${port}")
    private Long port;

    public RestTemplate restTemplate(int connectionTimeout, int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);

        return new RestTemplate(factory);
    }

    public void crashCoreService() {
        String url = "http://" + baseUrl + ":" + port + "/api/crash";
        restTemplate(3000, 3000).getForEntity(url, null, Void.class);
    }
}