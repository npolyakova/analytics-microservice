package ru.hpclab.hl.module2.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.hpclab.hl.module2.client.KillerClient;


@Component
@RequiredArgsConstructor
public class CrashScheduler {

    private final KillerClient webApiKillerClient;

    @Async(value = "applicationTaskExecutor")
    @Scheduled(fixedDelayString = "${service.scheduler.core.crash.delay}")
    public void callCrashEndpoint() {
        try {
            webApiKillerClient.crashCoreService();
            System.out.println("Crash request sent to Core Service");
        } catch (Exception e) {
            System.err.println("Failed to call crash endpoint: " + e.getMessage());
        }
    }
}