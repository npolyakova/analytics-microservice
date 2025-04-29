package ru.hpclab.hl.module2.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.hpclab.hl.module2.dto.BookingDto;
import ru.hpclab.hl.module2.dto.HotelRoomDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<Long, HotelRoomDto> roomCache = new ConcurrentHashMap<>();

    public RestTemplate restTemplate(int connectionTimeout, int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);

        return new RestTemplate(factory);
    }

    public BookingDto[] getBookings() {
        return this.restTemplate(120000, 120000).getForObject(String.format("http://%s:%d/%s", baseUrl, port, bookingSource), BookingDto[].class);
    }

    public HotelRoomDto getRoom(Long id) {
        HotelRoomDto cachedRoom = roomCache.get(id);
        if (cachedRoom != null) {
            return cachedRoom;
        }
        HotelRoomDto room = this.restTemplate(60000, 60000).getForObject(String.format("http://%s:%d/%s/%d", baseUrl, port, roomSource, id), HotelRoomDto.class);
        if (room != null) {
            roomCache.put(id, room);
        }
        return room;
    }
}
