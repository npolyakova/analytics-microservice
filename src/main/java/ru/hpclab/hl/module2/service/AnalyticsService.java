package ru.hpclab.hl.module2.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.hpclab.hl.module2.client.Client;
import ru.hpclab.hl.module2.dto.BookingDto;
import ru.hpclab.hl.module2.dto.HotelRoomDto;
import ru.hpclab.hl.module2.dto.RoomType;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Service
@EnableScheduling
public class AnalyticsService {
    @Autowired
    private Client client;

    private static final Logger logger = LogManager.getLogger(AnalyticsService.class);

    private final Map<Long, RoomType> roomTypeMap = new HashMap<>();

    @Scheduled(fixedDelayString = "${cacheStatsInterval}")
    public void scheduleFixedDelayTask() {
        logger.info("Cache size is {}", roomTypeMap.size());
    }

    public Map<String, Map<RoomType, Double>> getStat() {
        BookingDto[] getBookingsResponse = client.getBookings();

        Map<YearMonth, Map<RoomType, List<Long>>> occupancyByMonthAndType = new HashMap<>();

        assert getBookingsResponse != null;
        for (BookingDto booking : getBookingsResponse) {
            LocalDate startDate = booking.getDateArr().toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate endDate = booking.getDateLeave().toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();

            HotelRoomDto bookedRoom = client.getRoom(booking.getRoomId());
            RoomType roomType = bookedRoom.getType();
            if (roomType == null) continue;
            else {
                roomTypeMap.put(bookedRoom.getId(), bookedRoom.getType());
            }

            startDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
                YearMonth yearMonth = YearMonth.from(date);
                occupancyByMonthAndType
                        .computeIfAbsent(yearMonth, k -> new HashMap<>())
                        .computeIfAbsent(roomType, k -> new ArrayList<>())
                        .add(booking.getRoomId());
            });
        }

        Map<String, Map<RoomType, Double>> result = new TreeMap<>();

        Set<RoomType> roomTypes = Set.of(RoomType.LUX, RoomType.ECONOM, RoomType.STANDART);

        occupancyByMonthAndType.forEach((yearMonth, typeCounts) -> {
            String monthKey = yearMonth.toString();
            Map<RoomType, Double> monthData = new HashMap<>();
            roomTypes.forEach(type -> {
                List<Long> occupiedRoomsOfType = typeCounts.getOrDefault(type, Collections.emptyList());
                long uniqueOccupiedRooms = occupiedRoomsOfType.stream().distinct().count();
                long totalRoomsOfType = roomTypeMap.values().stream()
                        .filter(r -> r.equals(type))
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
