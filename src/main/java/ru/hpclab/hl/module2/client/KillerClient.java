package ru.hpclab.hl.module1.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class KillerClient {

    private final RestTemplate restTemplate;

    @Value("${core.service.host}")
    private String coreServiceHost;

    @Value("${core.service.port}")
    private String coreServicePort;

    public void crashCoreService() {
        String url = "http://" + coreServiceHost + ":" + coreServicePort + "/api/crash";
        restTemplate.postForEntity(url, null, Void.class);
    }
}