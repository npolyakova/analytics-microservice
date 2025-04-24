package ru.hpclab.hl.module2.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.hpclab.hl.module2.dto.BookingDto;
import ru.hpclab.hl.module2.dto.HotelRoomDto;
import ru.hpclab.hl.module2.dto.RoomType;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    final String baseUrl = "http://192.168.1.62:8080/api";
//    final String baseUrl = "http://100.71.107.143:8080/api";
    final String bookingSource = "/bookings";
    final String roomSource = "/rooms";

    public Map<String, Map<RoomType, Double>> getStat() {
        RestTemplate restTemplate = new RestTemplate();

        HotelRoomDto[] getRoomsResponse = restTemplate.getForObject(baseUrl + roomSource, HotelRoomDto[].class);
        BookingDto[] getBookingsResponse = restTemplate.getForObject(baseUrl + bookingSource, BookingDto[].class);

        assert getRoomsResponse != null;
        Map<Long, RoomType> roomTypeMap = Arrays.stream(getRoomsResponse)
                .collect(Collectors.toMap(HotelRoomDto::getId, HotelRoomDto::getType));

        Map<YearMonth, Map<RoomType, List<Long>>> occupancyByMonthAndType = new HashMap<>();

        for (BookingDto booking : getBookingsResponse) {
            LocalDate startDate = booking.getDateArr().toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate endDate = booking.getDateLeave().toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();

            RoomType roomType = roomTypeMap.get(booking.getRoomId());
            if (roomType == null) continue;

            startDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
                YearMonth yearMonth = YearMonth.from(date);
                occupancyByMonthAndType
                        .computeIfAbsent(yearMonth, k -> new HashMap<>())
                        .computeIfAbsent(roomType, k -> new ArrayList<>())
                        .add(booking.getRoomId());
            });
        }

        Map<String, Map<RoomType, Double>> result = new TreeMap<>();

        Set<RoomType> roomTypes = Arrays.stream(getRoomsResponse)
                .map(HotelRoomDto::getType)
                .collect(Collectors.toSet());

        occupancyByMonthAndType.forEach((yearMonth, typeCounts) -> {
            String monthKey = yearMonth.toString();
            Map<RoomType, Double> monthData = new HashMap<>();
            roomTypes.forEach(type -> {
                List<Long> occupiedRoomsOfType = typeCounts.getOrDefault(type, Collections.emptyList());
                long uniqueOccupiedRooms = occupiedRoomsOfType.stream().distinct().count();
                long totalRoomsOfType = Arrays.stream(getRoomsResponse)
                        .filter(r -> r.getType().equals(type))
                        .count();

                double occupancyRate = totalRoomsOfType == 0 ? 0 :
                        (double) uniqueOccupiedRooms / totalRoomsOfType;

                monthData.put(type, occupancyRate);
            });

            result.put(monthKey, monthData);
        });

        return result;
    }
}
