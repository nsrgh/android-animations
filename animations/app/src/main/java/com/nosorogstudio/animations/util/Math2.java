package com.nosorogstudio.animations.util;

public class Math2 {

    public static float interpolate(float from, float to, float progress) {
        return from + (to - from) * progress;
    }

    public static int interpolate(int from, int to, float progress) {
        return Math.round(from + (to - from) * progress);
    }

    public static float interpolateDegrees(float from, float to, float progress) {
        from = normalizeAngle360(from);
        to = normalizeAngle360(to);

        float difference = Math.abs(from - to);
        if (difference > 180) {
            if (to > from) {
                from += 360;
            } else {
                to += 360;
            }
        }

        float result = interpolate(from, to, progress);
        result = normalizeAngle360(result);
        return result;
    }

    public static float normalizeAngle360(float degrees) {
        while (degrees < 0.0f) {
            degrees += 360.0f;
        }
        while (degrees >= 360.0f) {
            degrees -= 360.0f;
        }
        return degrees;
    }

    public static float normalizeAngle180(float degrees) {
        while (degrees < -180.0f) {
            degrees += 360.0f;
        }
        while (degrees >= 180.0f) {
            degrees -= 360.0f;
        }
        return degrees;
    }

    public static double normalizeAngle2Pi(double radians) {
        while (radians < 0.0) {
            radians += Math.PI * 2;
        }
        while (radians >= Math.PI * 2) {
            radians -= Math.PI * 2;
        }
        return radians;
    }

    public static float clamp(float value, float start, float end) {
        if (value < start) {
            return start;
        }
        if (value > end) {
            return end;
        }
        return value;
    }

    public static float clamp01(float value) {
        return clamp(value, 0, 1);
    }

    public static float subProgressClamp(float progress, float subStart, float subEnd) {
        float sub = (progress - subStart) / (subEnd - subStart);
        sub = clamp01(sub);
        return sub;
    }

    public static float subProgressOrZero(float progress, float subStart, float subEnd) {
        float sub = (progress - subStart) / (subEnd - subStart);
        if (sub < 0.0f || sub > 1.0f) {
            return 0.0f;
        }
        return sub;
    }

    public static Float subProgressOrNull(float progress, float subStart, float subEnd) {
        float sub = (progress - subStart) / (subEnd - subStart);
        if (sub < 0.0f || sub > 1.0f) {
            return null;
        }
        return sub;
    }

    public static boolean insideRange(float value, float rangeStart, float rangeEnd) {
        return value >= rangeStart && value < rangeEnd;
    }

}
