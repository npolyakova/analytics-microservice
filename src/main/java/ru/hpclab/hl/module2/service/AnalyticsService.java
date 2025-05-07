package ru.hpclab.hl.module2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import ru.hpclab.hl.module2.cache.RoomCache;
import ru.hpclab.hl.module2.client.Client;
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
@EnableScheduling
public class AnalyticsService {

    @Autowired
    private Client client;

    @Autowired
    private RoomCache roomCache;

    @Autowired
    private ObservabilityService observabilityService;

    private HotelRoomDto getRoomById(Long id) {
        if (!roomCache.getRoomCache().containsKey(id)) {
            var room = client.getRoom(id);

            roomCache.getRoomCache().put(room.getId(), room);

            return room;
        }

        return roomCache.getRoomCache().get(id);
    }

    public Map<String, Map<RoomType, Double>> getStat() {
        BookingDto[] getBookingsResponse = client.getBookings();

        Map<YearMonth, Map<RoomType, List<Long>>> occupancyByMonthAndType = new HashMap<>();

        for (BookingDto booking : getBookingsResponse) {
            LocalDate startDate = booking.getDateArr().toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate endDate = booking.getDateLeave().toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();

            RoomType roomType = getRoomById(booking.getRoomId()).getType();
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

        Set<RoomType> roomTypes = roomCache.getRoomCache().values().stream()
                .map(HotelRoomDto::getType)
                .collect(Collectors.toSet());

        occupancyByMonthAndType.forEach((yearMonth, typeCounts) -> {
            String monthKey = yearMonth.toString();
            Map<RoomType, Double> monthData = new HashMap<>();
            roomTypes.forEach(type -> {
                List<Long> occupiedRoomsOfType = typeCounts.getOrDefault(type, Collections.emptyList());
                long uniqueOccupiedRooms = occupiedRoomsOfType.stream().distinct().count();
                long totalRoomsOfType = roomCache.getRoomCache().values().stream()
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
