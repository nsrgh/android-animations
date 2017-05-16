package com.nosorogstudio.animations;

import android.animation.TimeInterpolator;

import com.nosorogstudio.animations.util.Math2;

import java.util.ArrayList;
import java.util.List;

public class Timeline {
    @lombok.Value
    private static class Value {
        private float value;
        private float begin;
        private float end;
        private TimeInterpolator interpolator;
    }

    @lombok.Value
    private static class Param {
        private String name;
        private float initialValue;
        private List<Value> values = new ArrayList<>();
    }

    private List<Param> params = new ArrayList<>();

    private Param paramBy(String name) {
        for (Param param : params) {
            if (param.name.equals(name)) {
                return param;
            }
        }
        throw new IllegalArgumentException("name");
    }

    public void createParam(String name, float initialValue) {
        params.add(new Param(name, initialValue));
    }

    public void addValue(String name, float value, float begin, float end, TimeInterpolator interpolator) {
        paramBy(name).values.add(new Value(value, begin, end, interpolator));
    }

    public void addValue(String name, float value, long beginMillis, long endMillis, TimeInterpolator interpolator, long timelineDuration) {
        addValue(name, value, (float) beginMillis / (float) timelineDuration, (float) endMillis / (float) timelineDuration, interpolator);
    }

    public float getValue(String name, float factor) {
        Param param = paramBy(name);
        float result = param.initialValue;
        for (Value value : param.values) {
            if (factor >= value.end) {
                result = value.value;
            } else if (factor >= value.begin) {
                float valueFactor = Math2.subProgressClamp(factor, value.begin, value.end);
                valueFactor = value.interpolator.getInterpolation(valueFactor);
                result = Math2.interpolate(result, value.value, valueFactor);
            }
        }
        return result;
    }
}
