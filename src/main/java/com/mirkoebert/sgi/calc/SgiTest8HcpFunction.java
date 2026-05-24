package com.mirkoebert.sgi.calc;

import org.springframework.stereotype.Service;

import java.util.function.IntUnaryOperator;

@Service
class SgiTest8HcpFunction implements IntUnaryOperator {

        @Override
        public int applyAsInt(int s) {
                return switch (s) {
                        case 0 -> 52;
                        case 1 -> 44;
                        case 2 -> 40;
                        case 3 -> 30;
                        case 4 -> 26;
                        case 5 -> 22;
                        case 6 -> 17;
                        case 7 -> 14;
                        case 8 -> 10;
                        case 9 -> 8;
                        case 10 -> 5;
                        case 11 -> 4;
                        case 12 -> 2;
                        case 13 -> 0;
                        default -> -1; // Tour
                };
        }

}
