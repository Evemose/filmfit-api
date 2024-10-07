package com.filmfit.core.film.vector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;
import lombok.AccessLevel;
import lombok.Locked;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class Normalizer {

    private final double mean;
    private final double std;

    public static Normalizer forSource(DoubleStream values) {
        var stats = values.summaryStatistics();
        var mean = stats.getAverage();
        var std = Math.sqrt(stats.getSum() / stats.getCount() - mean * mean);

        return new Normalizer(mean, std);
    }

    public double normalize(double value) {
        return (value - mean) / std;
    }

    static class Builder {
        private final List<Double> elements = new ArrayList<>();

        @Locked
        public void consume(double value) {
            elements.add(value);
        }

        public Normalizer build() {
            var mean = elements.stream().map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(elements.size()), RoundingMode.HALF_UP).doubleValue();
            var sumOfSquareDiffs = elements.stream().map(BigDecimal::new)
                .map(value -> value.subtract(BigDecimal.valueOf(mean)).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            var std = Math.sqrt(sumOfSquareDiffs.divide(BigDecimal.valueOf(elements.size()), RoundingMode.HALF_UP).doubleValue());
            return new Normalizer(mean, std);
        }
    }

}
