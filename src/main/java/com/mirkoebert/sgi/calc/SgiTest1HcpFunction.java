package com.mirkoebert.sgi.calc;

import org.springframework.stereotype.Service;

import java.util.function.IntUnaryOperator;

@Service
class SgiTest1HcpFunction implements IntUnaryOperator {

        @Override
        public int applyAsInt(int s) {
                return switch (s) {
                        case 0 -> 36;
                        case 1 -> 31;
                        case 2 -> 25;
                        case 3 -> 20;
                        case 4 -> 15;
                        case 5 -> 12;
                        case 6 -> 8;
                        case 7 -> 6;
                        case 8 -> 3;
                        case 9 -> 0;
                        default -> -1; // Tour
                };
        }

}
