package com.mirkoebert.sgi;

import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
class SgiTestSuiteHcpFunction implements Function<Integer, Integer> {

        @Override
        public Integer apply(Integer points) {
                if (points < 12) {
                        return 40;
                }
                return switch (points) {
                        case 12, 13 -> 39;
                        case 14, 15 -> 38;
                        case 16, 17 -> 37;
                        case 18, 19 -> 36;
                        case 20, 21 -> 35;
                        case 22, 23 -> 34;
                        case 24, 25 -> 33;
                        case 26, 27 -> 32;
                        case 28, 29 -> 31;
                        case 30, 31 -> 30;
                        case 32, 33 -> 29;
                        case 34, 35 -> 28;
                        case 36, 37 -> 27;
                        case 38, 39 -> 26;
                        case 40, 41 -> 25;
                        case 42, 43 -> 24;
                        case 44, 45 -> 23;
                        case 46, 47 -> 22;
                        case 48, 49 -> 21;
                        case 50, 51 -> 20;
                        case 52, 53 -> 19;
                        case 54, 55 -> 18;
                        case 56, 57 -> 17;
                        case 58, 59 -> 16;
                        case 60, 61, 62 -> 15;
                        case 63, 64, 65, 66 -> 14;
                        case 67, 68, 69 -> 13;
                        case 70, 71, 72 -> 12;
                        case 73, 74, 75, 76 -> 11;
                        case 77, 78, 79 -> 10;
                        case 80, 81, 82 -> 9;
                        case 83, 84, 85, 86 -> 8;
                        case 87, 88, 89 -> 7;
                        case 90, 91, 92 -> 6;
                        case 93, 94, 95, 96 -> 5;
                        case 97, 98, 99 -> 4;
                        case 100, 101, 102 -> 3;
                        case 103, 104, 105, 106 -> 2;
                        case 107, 108, 109 -> 1;
                        case 110, 111, 112, 113, 114 -> 0;
                        case 115, 116, 117, 118, 119 -> -1;
                        case 120, 121, 122, 123, 124 -> -2;
                        case 125, 126, 127, 128, 129 -> -3;
                        case 130, 131, 132, 133, 134 -> -4;
                        case 135, 136, 137, 138, 139 -> -5;
                        case 140, 141, 142, 143, 144 -> -6;
                        case 145, 146, 147, 148, 149 -> -7;
                        default -> -8;
                };
        }

}
