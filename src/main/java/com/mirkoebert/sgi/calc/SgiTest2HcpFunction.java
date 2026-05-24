package com.mirkoebert.sgi.calc;

import org.springframework.stereotype.Service;

import java.util.function.IntUnaryOperator;

@Service
class SgiTest2HcpFunction implements IntUnaryOperator {

        @Override
        public int applyAsInt(int s) {
                return switch (s) {
                        case 0 -> 36;
                        case 1 -> 31;
                        case 2 -> 25;
                        case 3 -> 27;
                        case 4 -> 21;
                        case 5 -> 17;
                        case 6 -> 15;
                        case 7 -> 11;
                        case 8 -> 8;
                        case 9 -> 7;
                        case 10 -> 5;
                        case 11 -> 3;
                        case 12 -> 0;
                        default -> -1; // Tour
                };
        }

}
