package com.mirkoebert.sgi.calc;

import org.springframework.stereotype.Service;

import java.util.function.IntUnaryOperator;

@Service
class SgiTest7HcpFunction implements IntUnaryOperator {

        @Override
        public int applyAsInt(int s) {
                return switch (s) {
                        case 0 -> 52;
                        case 1 -> 44;
                        case 2 -> 40;
                        case 3 -> 33;
                        case 4 -> 27;
                        case 5 -> 20;
                        case 6 -> 16;
                        case 7 -> 12;
                        case 8 -> 10;
                        case 9 -> 7;
                        case 10 -> 6;
                        case 11 -> 5;
                        case 12 -> 4;
                        case 13 -> 2;
                        case 14 -> 0;
                        default -> -1; // Tour
                };
        }

}
