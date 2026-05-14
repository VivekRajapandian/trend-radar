package com.trendradar.api;

import com.trendradar.application.SystemStatusResponse;
import com.trendradar.application.SystemStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemStatusController {

    private final SystemStatusService systemStatusService;

    public SystemStatusController(SystemStatusService systemStatusService) {
        this.systemStatusService = systemStatusService;
    }

    @GetMapping("/api/system/status")
    public SystemStatusResponse getSystemStatus() {
        return systemStatusService.getStatus();
    }
}
