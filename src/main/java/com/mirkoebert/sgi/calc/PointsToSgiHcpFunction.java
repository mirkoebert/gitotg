package com.mirkoebert.sgi.calc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointsToSgiHcpFunction implements BiFunction<Integer, Integer, Integer> {

        private final SgiTest1HcpFunction sgiTest1HcpFunction;
        private final SgiTest2HcpFunction sgiTest2HcpFunction;
        private final SgiTest3HcpFunction sgiTest3HcpFunction;
        private final SgiTest4HcpFunction sgiTest4HcpFunction;
        private final SgiTest5HcpFunction sgiTest5HcpFunction;
        private final SgiTest7HcpFunction sgiTest7HcpFunction;
        private final SgiTest8HcpFunction sgiTest8HcpFunction;

        @Override
        public Integer apply(final Integer testId, final Integer points) {
                return switch (testId) {
                        case 1 -> sgiTest1HcpFunction.applyAsInt(points);
                        case 2 -> sgiTest2HcpFunction.applyAsInt(points);
                        case 3 -> sgiTest3HcpFunction.applyAsInt(points);
                        case 4 -> sgiTest4HcpFunction.applyAsInt(points);
                        case 5, 6 -> sgiTest5HcpFunction.applyAsInt(points);
                        case 7 -> sgiTest7HcpFunction.applyAsInt(points);
                        case 8 -> sgiTest8HcpFunction.applyAsInt(points);
                        default -> 99;
                };
        }

}
