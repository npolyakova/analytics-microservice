package ru.hpclab.hl.module2.client;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.hpclab.hl.module2.dto.BookingDto;
import ru.hpclab.hl.module2.dto.HotelRoomDto;

@Component
public class Client {

    @Value("${baseUrl}")
    private String baseUrl;

    @Value("${port}")
    private Long port;

    @Value("${bookingSource}")
    private String bookingSource;

    @Value("${roomSource}")
    private String roomSource;

    public RestTemplate restTemplate(int connectionTimeout, int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);

        return new RestTemplate(factory);
    }

    @Retry(name = "ANALYTIC_SERVICE")
    public BookingDto[] getBookings() {
        return this.restTemplate(120000, 120000).getForObject(String.format("http://%s:%d/%s", baseUrl, port, bookingSource), BookingDto[].class);
    }

    @Retry(name = "ANALYTIC_SERVICE")
    public HotelRoomDto getRoom(Long id) {
        return this.restTemplate(60000, 60000).getForObject(String.format("http://%s:%d/%s/%d", baseUrl, port, roomSource, id), HotelRoomDto.class);
    }
}
