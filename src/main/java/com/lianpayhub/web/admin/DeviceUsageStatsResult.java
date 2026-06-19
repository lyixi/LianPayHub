package com.lianpayhub.web.admin;

public class DeviceUsageStatsResult {
    private final long totalDurationSeconds;
    private final long averageDurationSeconds;

    public DeviceUsageStatsResult(long totalDurationSeconds, long averageDurationSeconds) {
        this.totalDurationSeconds = totalDurationSeconds;
        this.averageDurationSeconds = averageDurationSeconds;
    }

    public long getTotalDurationSeconds() { return totalDurationSeconds; }
    public long getAverageDurationSeconds() { return averageDurationSeconds; }
}
