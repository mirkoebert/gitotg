package com.mirkoebert.sgi.calc;

import org.springframework.stereotype.Service;

import java.util.function.IntUnaryOperator;

@Service
class SgiTest5HcpFunction implements IntUnaryOperator {

        @Override
        public int applyAsInt(int s) {
                return switch (s) {
                        case 0 -> 52;
                        case 1 -> 44;
                        case 2 -> 40;
                        case 3 -> 38;
                        case 4 -> 35;
                        case 5 -> 32;
                        case 6 -> 30;
                        case 7 -> 27;
                        case 8 -> 24;
                        case 9 -> 22;
                        case 10 -> 17;
                        case 11 -> 12;
                        case 12 -> 10;
                        case 13 -> 8;
                        case 14 -> 5;
                        case 15 -> 3;
                        case 16 -> 0;
                        default -> -1; // Tour
                };
        }

}
