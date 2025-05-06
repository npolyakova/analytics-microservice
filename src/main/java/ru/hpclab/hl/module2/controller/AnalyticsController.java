package ru.hpclab.hl.module2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hpclab.hl.module2.dto.RoomType;
import ru.hpclab.hl.module2.service.AnalyticsService;
import ru.hpclab.hl.module2.service.ObservabilityService;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    AnalyticsService analyticsService;

    @Autowired
    private ObservabilityService observabilityService;

    @GetMapping("")
    public Map<String, Map<RoomType, Double>> getStatistics(){
        observabilityService.start();
        Map<String, Map<RoomType, Double>> stat = analyticsService.getStat();
        observabilityService.stop();
        return stat;
    }

}
