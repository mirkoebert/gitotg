package com.mirkoebert.golfmetric.eclectic;

import com.mirkoebert.golfcourse.*;
import com.mirkoebert.handicap.HcpScoreDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EclecticService {

    private final GolfCourseCatalog catalog;
    private final PlayedRoundRepository playedRoundRepository;


    Double getEclecticHcpForYear(int year, final String userId){
        try {
            final String courseName = getHomeCourseName(userId);
            log.info("getEclecticHcpForYear {} {} {}", year, courseName, userId);
            final GolfCourse c = catalog.findByName(courseName).get();

            List<PlayedRoundEntity> allPlayedRounds = playedRoundRepository.findAllByUserIdAndCourseName(userId, courseName);
            List<PlayedRoundEntity> playedRoundsOfYear = allPlayedRounds
                    .stream()
                    .filter(x -> x.getDate().getYear() == year)
                    .toList();
            int countOfHoles = c.getHoles().size();
            int eclecticSum = 0;
            for (int i = 0; i < countOfHoles; i++) {
                eclecticSum = eclecticSum + getBestShotCountForHole(i, playedRoundsOfYear);
            }
            log.info("eclecticSum {}", eclecticSum);


            return (double) eclecticSum;
        } catch (Exception e) {
            log.info("Can't calc eclectic hcp {}", e.getMessage());
            return null;
        }
    }

    public Integer getEclecticHcpForLast10Rounds(final String userId){
        try {
            final String courseName = getHomeCourseName(userId);
            log.info("getEclecticHcpForLast10Rounds {} {}",  courseName, userId);
            final GolfCourse c = catalog.findByName(courseName).get();

            List<PlayedRoundEntity> allPlayedRounds = playedRoundRepository.findAllByUserIdAndCourseName(userId, courseName);
            List<PlayedRoundEntity> playedRoundsSelected = allPlayedRounds
                    .stream()
                    .limit(10)
                    .toList();
            int countOfHoles = c.getHoles().size();
            int eclecticSum = 0;
            for (int i = 0; i < countOfHoles; i++) {
                eclecticSum = eclecticSum + getBestShotCountForHole(i, playedRoundsSelected);
            }

            log.info("eclecticSum {}", eclecticSum);
            return eclecticSum;
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

    @NonNull List<HcpScoreDTO> getEclecticHcpTimeline(final String userId){
        try {
            log.info("getEclecticHcpTimeline {}", userId);
            List<HcpScoreDTO> eclecticHcpTimeline = new ArrayList<>();

            final String courseName = getHomeCourseName(userId);
            List<PlayedRoundEntity> allPlayedRounds = playedRoundRepository.findAllByUserIdAndCourseName(userId, courseName);
            log.info("Found played rounds {}", allPlayedRounds.size());
            int[] yearsWithPlayedRounds = allPlayedRounds.stream().mapToInt(x -> x.getDate().getYear()).toArray();
            for (int yearsWithPlayedRound : yearsWithPlayedRounds) {
                eclecticHcpTimeline.add(
                        new HcpScoreDTO(
                                LocalDate.of(yearsWithPlayedRound, 1, 1),
                                getEclecticHcpForYear(yearsWithPlayedRound, userId)
                        )
                );
            }

            return eclecticHcpTimeline;
        } catch (Exception e) {
            log.info("Can't calc eclectic hcp {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public String getHomeCourseName(final String userId){
        List<PlayedRoundEntity> p = playedRoundRepository.findByUserId(userId);
        Map<String, Long> x = p
                .stream()
                .map(PlayedRoundEntity::getCourseName)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Optional<Map.Entry<String, Long>> top = x.entrySet().stream().max(Map.Entry.comparingByValue());
        log.info("top entry: {}", top);
        return top.get().getKey();
    }

    public int getCoursePar(final String courseName){
        final GolfCourse c = catalog.findByName(courseName).get();
        int coursePar = c.getHoles().stream().mapToInt(Hole::getPar).sum();
        log.info("course name {} par {}", courseName, coursePar);
        return coursePar;
    }
}
