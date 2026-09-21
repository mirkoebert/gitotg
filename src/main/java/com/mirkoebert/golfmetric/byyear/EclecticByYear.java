package com.mirkoebert.golfmetric.byyear;

import com.mirkoebert.golfcourse.*;
import com.mirkoebert.handicap.HcpScoreDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EclecticByYear {

    private final GolfCourseCatalog catalog;
    private final PlayedRoundRepository playedRoundRepository;


    // TODO tech debt two string parameter
    Double getEclecticHcpForYear(int year, final String courseName, final String userId){
        try {
            log.info("getEclecticHcpForYear {} {} {}", year, courseName, userId);
            final GolfCourse c = catalog.findByName(courseName).get();
            int courseHcp = c.getHoles().stream().mapToInt(Hole::getPar).sum();
            log.info("courseHcp {}", courseHcp);

            List<PlayedRoundEntity> allPlayedRounds = playedRoundRepository.findByUserId(userId);
            List<PlayedRoundEntity> playedRoundsOfYear = allPlayedRounds
                    .stream()
                    .filter(x -> x.getCourseName().equals(courseName))
                    .filter(x -> x.getDate().getYear() == year)
                    .toList();
            int countOfHoles = c.getHoles().size();
            int eclecticSum = 0;
            for (int i = 0; i < countOfHoles; i++) {
                eclecticSum = eclecticSum + getBestShotCountForHole(i, playedRoundsOfYear);
            }
            log.info("eclecticSum {}", eclecticSum);


            return (double) (eclecticSum - courseHcp);
        } catch (Exception e) {
            log.info("Can't calc eclectic hcp {}", e.getMessage());
            return null;
        }
    }

    private int getBestShotCountForHole(int i, final List<PlayedRoundEntity> playedRoundsOfYear) {
        return playedRoundsOfYear
                .stream()
                .mapToInt(x -> x.getHoleStrokes().get(i))
                .min()
                .getAsInt();
    }

    @NonNull List<HcpScoreDTO> getEclecticHcpTimeline(final String courseName, final String userId){
        log.info("getEclecticHcpTimeline {} {}", courseName, userId);
        List<HcpScoreDTO> eclecticHcpTimeline = new ArrayList<>();

        List<PlayedRoundEntity> allPlayedRounds = playedRoundRepository.findAllByUserIdAndCourseName(userId, courseName);
        log.info("Found played rounds {}", allPlayedRounds.size());
        int[] yearsWithPlayedRounds = allPlayedRounds.stream().mapToInt(x -> x.getDate().getYear()).toArray();
        for (int yearsWithPlayedRound : yearsWithPlayedRounds) {
            eclecticHcpTimeline.add(
                    new HcpScoreDTO(
                            LocalDate.of(yearsWithPlayedRound, 1, 1),
                            getEclecticHcpForYear(yearsWithPlayedRound, courseName, userId)
                    )
            );
        }


        return eclecticHcpTimeline;
    }
}
