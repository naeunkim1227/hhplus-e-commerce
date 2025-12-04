package io.hhplus.ecommerce.product.scheduler;

import io.hhplus.ecommerce.common.constants.RedisKeyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingScheduler {

    private final RedisTemplate<String, String> redisTemplate;

    private String saveDayRankingData(LocalDate date){
        String dailyKey = getDailyKey(date);
        String targetKey = RedisKeyType.RANKING_DAILY.getKey(dailyKey);
        Set<ZSetOperations.TypedTuple<String>> baseData = redisTemplate.opsForZSet().rangeWithScores( RedisKeyType.RANKING_POPULAR.getKey(dailyKey), 0, -1);

        if(baseData == null || baseData.isEmpty()){
               redisTemplate.opsForZSet().add(targetKey, "EMPTY", 0);
               log.warn("No ranking data found for date: {}. Stored EMPTY marker.", dailyKey);
        }else{
            for(ZSetOperations.TypedTuple<String> tuple : baseData){
                redisTemplate.opsForZSet().add(targetKey, tuple.getValue(), tuple.getScore());
            }
            log.info("Saved {} ranking entries for date: {}", baseData.size(), dailyKey);
        }

        // TTL 설정 (7일)
        redisTemplate.expire(targetKey, RedisKeyType.RANKING_DAILY.getTtl());

        return targetKey;
    }

    /**
     * @return 합산에 사용된 일간 키 합치기
     */
    private int aggregateDailyRankings(List<String> dailyKeys, String targetKey) {
        int validKeyCount = 0;
        List<String> existingKeys = new ArrayList<>();

        for (String dailyKey : dailyKeys) {
            Boolean exists = redisTemplate.hasKey(dailyKey);
            if (Boolean.TRUE.equals(exists)) {
                existingKeys.add(dailyKey);
                validKeyCount++;
            } else {
                log.warn("Daily ranking key not found: {}", dailyKey);
            }
        }

        if (existingKeys.isEmpty()) {
            log.error("No valid daily ranking data found for aggregation");
            return 0;
        }

        // 2. Redis ZUNIONSTORE를 사용하여 한 번에 합산
        String firstKey = existingKeys.get(0);
        List<String> otherKeys = existingKeys.subList(1, existingKeys.size());
        Long resultCount = redisTemplate.opsForZSet().unionAndStore(firstKey, otherKeys, targetKey);

        // 3. EMPTY 마커 제거
        Long removedCount = redisTemplate.opsForZSet().remove(targetKey, "EMPTY");
        if (removedCount != null && removedCount > 0) {
            log.info("Removed EMPTY marker from aggregated ranking: {}", targetKey);
        }

        log.info("Aggregated {} daily rankings into {}. Result count: {}", validKeyCount, targetKey, resultCount);

        return validKeyCount;
    }

    public static String getWeekKey(LocalDate date) {
        int year = date.get(IsoFields.WEEK_BASED_YEAR);  // ISO 주차 연도
        int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);  // ISO 주차
        return String.format("%d-W%02d", year, week);
    }

    public static String getMonthKey(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    public static String getDailyKey(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * 일간 랭킹 업데이트: 매일 자정에 실행
     * 전날의 인기 랭킹 데이터를 일간 랭킹으로 저장
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void updateDailyRanking() {
        System.out.println("---일간 데이터 합산---");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        saveDayRankingData(yesterday);
    }

    /**
     * 주간 랭킹 업데이트: 매주 월요일 자정에 실행
     * 지난 7일의 일간 랭킹 데이터를 합산하여 주간 랭킹 생성
     */
    @Scheduled(cron = "0 0 0 * * MON")
    public void updateWeeklyRanking() {
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
        String weekKey = getWeekKey(lastWeek);
        String targetKey = RedisKeyType.RANKING_WEEKLY.getKey(weekKey);

        log.info("Starting weekly ranking update for week {}", weekKey);

        // 지난 7일의 일간 키 수집
        List<String> dailyKeys = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            LocalDate targetDay = LocalDate.now().minusDays(i);
            String dailyKey = RedisKeyType.RANKING_DAILY.getKey(getDailyKey(targetDay));

            //일간데이터가 없을 경우 다시 수집처리
            if(!redisTemplate.hasKey(dailyKey)){
                dailyKey = saveDayRankingData(targetDay);
            }
            dailyKeys.add(dailyKey);
        }

        // 일간 데이터 합산
        int validCount = aggregateDailyRankings(dailyKeys, targetKey);

        if (validCount == 0) {
            log.error("Failed to create weekly ranking for week {}: No valid daily data", weekKey);
            return;
        }

        // 데이터 검증
        Long resultSize = redisTemplate.opsForZSet().zCard(targetKey);
        if (resultSize == null || resultSize == 0) {
            log.error("Weekly ranking for week {} is empty after aggregation", weekKey);
            return;
        }

        // TTL 설정
        redisTemplate.expire(targetKey, RedisKeyType.RANKING_WEEKLY.getTtl());

        log.info("Completed weekly ranking update for week {}. Used {}/7 daily rankings. Result size: {}",
                weekKey, validCount, resultSize);
    }

    /**
     * 월간 랭킹 업데이트: 매월 1일 자정에 실행
     * 지난 달의 모든 일간 랭킹 데이터를 합산하여 월간 랭킹 생성
     */
    @Scheduled(cron = "0 0 0 1 * *")
    public void updateMonthlyRanking() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        String monthKey = getMonthKey(lastMonth);
        String targetKey = RedisKeyType.RANKING_MONTHLY.getKey(monthKey);

        log.info("Starting monthly ranking update for month {}", monthKey);

        // 지난 달의 모든 일간 키 수집
        LocalDate startOfLastMonth = lastMonth.withDayOfMonth(1);
        LocalDate endOfLastMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());

        List<String> dailyKeys = new ArrayList<>();
        LocalDate currentDate = startOfLastMonth;

        while (!currentDate.isAfter(endOfLastMonth)) {
            String dailyKey = RedisKeyType.RANKING_DAILY.getKey(getDailyKey(currentDate));
            if(!redisTemplate.hasKey(dailyKey)){
               dailyKey =  saveDayRankingData(currentDate);
            }

            dailyKeys.add(dailyKey);
            currentDate = currentDate.plusDays(1);
        }

        // 일간 데이터 합산
        int validCount = aggregateDailyRankings(dailyKeys, targetKey);

        if (validCount == 0) {
            return;
        }

        // TTL 설정
        redisTemplate.expire(targetKey, RedisKeyType.RANKING_MONTHLY.getTtl());
    }

}
