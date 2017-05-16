package com.nosorogstudio.animations.util;

import android.graphics.Color;

public class Colors {

    public static int interpolateRgb(int from, int to, float progress) {
        int fromRed = Color.red(from);
        int fromGreen = Color.green(from);
        int fromBlue = Color.blue(from);
        int fromAlpha = Color.alpha(from);

        int toRed = Color.red(to);
        int toGreen = Color.green(to);
        int toBlue = Color.blue(to);
        int toAlpha = Color.alpha(to);

        int red = Math2.interpolate(fromRed, toRed, progress);
        int green = Math2.interpolate(fromGreen, toGreen, progress);
        int blue = Math2.interpolate(fromBlue, toBlue, progress);
        int alpha = Math2.interpolate(fromAlpha, toAlpha, progress);

        int result = Color.argb(alpha, red, green, blue);
        return result;
    }

    public static int interpolateHsv(int from, int to, float progress) {
        float[] fromHsv = { 0.0f, 0.0f, 0.0f };
        Color.colorToHSV(from, fromHsv);
        int fromAlpha = Color.alpha(from);

        float[] toHsv = { 0.0f, 0.0f, 0.0f };
        Color.colorToHSV(to, toHsv);
        int toAlpha = Color.alpha(to);

        float h = Math2.interpolateDegrees(fromHsv[0], toHsv[0], progress);
        float s = Math2.interpolate(fromHsv[1], toHsv[1], progress);
        float v = Math2.interpolate(fromHsv[2], toHsv[2], progress);
        int alpha = Math2.interpolate(fromAlpha, toAlpha, progress);

        int result = hsv(h, s, v, alpha);
        return result;
    }


    public static int hsv(float hue, float saturation, float value, int alpha) {
        float[] values = {hue, saturation, value};
        int color = Color.HSVToColor(alpha, values);
        return color;
    }

    public static int hsv(float hue, float saturation, float value) {
        float[] values = {hue, saturation, value};
        int color = Color.HSVToColor(values);
        return color;
    }

    public static int withAlpha(int color, float alpha) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb((int) (alpha * 255), r, g, b);
    }
}
