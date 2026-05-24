package com.mirkoebert.sgi.calc;

import org.springframework.stereotype.Service;

import java.util.function.IntUnaryOperator;

@Service
class SgiTest4HcpFunction implements IntUnaryOperator {

        @Override
        public int applyAsInt(int s) {
                return switch (s) {
                        case 0 -> 36;
                        case 1 -> 31;
                        case 2 -> 16;
                        case 3 -> 13;
                        case 4 -> 10;
                        case 5 -> 6;
                        case 6 -> 5;
                        case 7 -> 3;
                        case 8 -> 2;
                        case 9, 10 -> 1;
                        case 11 -> 0;
                        default -> -1; // Tour
                };
        }

}
