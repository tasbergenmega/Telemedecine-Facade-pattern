package com.telemed.subsystemB;

import com.telemed.exception.VideoCallFailedException;

import java.util.Locale;

public class NetworkQualityDecider {

    public void ensureCallPossible(int qualityLevel) {
        if (qualityLevel < 2) {
            throw new VideoCallFailedException(
                    "Network quality is too low for a video call (level=" + qualityLevel + ")");
        }
    }

    public int estimateQuality(String networkId) {
        if (networkId == null || networkId.isBlank()) {
            return 3;
        }

        String normalized = networkId.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "0", "offline", "none" -> 0;
            case "1", "weak" -> 1;
            case "2", "medium" -> 2;
            case "3", "good" -> 3;
            default -> throw new VideoCallFailedException(
                    "Unknown networkId: " + networkId);
        };
    }
}