package com.servicelens.platform.detection;

import java.time.Instant;

import com.servicelens.platform.domain.ComparisonOperator;
import com.servicelens.platform.domain.DetectionRule;

public final class DetectionMath {

    private DetectionMath() {
    }

    public static boolean isBreach(double observed, DetectionRule rule) {
        double t = rule.getThreshold();
        return switch (rule.getComparison()) {
            case GT -> observed > t;
            case GTE -> observed >= t;
            case LT -> observed < t;
            case LTE -> observed <= t;
        };
    }

    /** Bucket de ventana fija alineado a epoch (misma lógica que deduplicación). */
    public static long windowBucketId(Instant now, int windowMinutes) {
        long sec = now.getEpochSecond();
        long w = windowMinutes * 60L;
        if (w <= 0) {
            return sec;
        }
        return sec / w;
    }

    public static String fingerprint(DetectionRule rule, long bucketId) {
        return rule.getId() + ":" + bucketId;
    }

    public static Instant windowStart(Instant now, int windowMinutes) {
        long w = windowMinutes * 60L;
        long sec = (now.getEpochSecond() / w) * w;
        return Instant.ofEpochSecond(sec);
    }

    public static Instant windowEnd(Instant windowStart, int windowMinutes) {
        return windowStart.plusSeconds(windowMinutes * 60L);
    }
}
