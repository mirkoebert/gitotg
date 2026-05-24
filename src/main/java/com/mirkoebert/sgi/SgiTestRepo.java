package com.mirkoebert.sgi;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
class SgiTestRepo {

        private final Map<Integer, SgiTest> allTests = new HashMap<>(8);

        SgiTestRepo() {
                allTests.put(1, new SgiTest("Test 1: 50-yard wedge", 45, 1));
                allTests.put(2, new SgiTest("Test 2: 30-yard wedge", 27, 2));
                allTests.put(3, new SgiTest("Test 3: 8-yard sand shot", 7, 3));
                allTests.put(4, new SgiTest("Test 4: 15-yard sand blast shot", 13, 4));
                allTests.put(5, new SgiTest("Test 5: 10-yard chipping from the light fringe", 9, 5));
                allTests.put(6, new SgiTest("Test 6: 20-yard chip from the light rough", 18, 6));
                allTests.put(7, new SgiTest("Test 7: 15-yard short pitch", 13, 7));
                allTests.put(8, new SgiTest("Test 8: 15-yard pitch over the bunker", 13, 8));
        }

        public SgiTest getTestById(int id) {
                return allTests.get(id);
        }

        public int count() {
                return allTests.size();
        }
}
