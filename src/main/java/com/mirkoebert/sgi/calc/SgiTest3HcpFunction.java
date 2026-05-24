package com.mirkoebert.sgi.calc;

import org.springframework.stereotype.Service;

import java.util.function.IntUnaryOperator;

@Service
class SgiTest3HcpFunction implements IntUnaryOperator {

        @Override
        public int applyAsInt(int s) {
                return switch (s) {
                        case 0 -> 36;
                        case 1 -> 31;
                        case 2 -> 22;
                        case 3 -> 19;
                        case 4 -> 15;
                        case 5 -> 12;
                        case 6 -> 9;
                        case 7 -> 6;
                        case 8 -> 5;
                        case 9 -> 4;
                        case 10 -> 2;
                        case 11 -> 1;
                        case 12 -> 0;
                        default -> -1; // Tour
                };
        }

}
