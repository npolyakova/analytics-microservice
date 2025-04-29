package ru.hpclab.hl.module2.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.hpclab.hl.module2.dto.HotelRoomDto;
import ru.hpclab.hl.module2.service.AnalyticsService;

import java.util.HashMap;
import java.util.Map;

@Component
public class RoomCache {

    public Map<Long, HotelRoomDto> roomCacheMap = new HashMap<>();

    private static final Logger logger = LogManager.getLogger(AnalyticsService.class);

    @Scheduled(fixedDelayString = "${cacheStatsInterval}")
    public void scheduleFixedDelayTask() {
        logger.info("Cache size is {}", roomCacheMap.size());
    }
}
