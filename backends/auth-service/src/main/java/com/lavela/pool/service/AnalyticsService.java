package com.lavela.pool.service;

import com.lavela.pool.domain.enums.BookingStatus;
import com.lavela.pool.dto.response.RevenueStatsResponse;
import com.lavela.pool.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Cung cấp thống kê doanh thu và số vé bán theo thời gian thực.
 * Chỉ tính các booking đã hoàn thành: CONFIRMED và CHECKED_IN.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final List<BookingStatus> COMPLETED_STATUSES =
            List.of(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN);

    private final BookingRepository bookingRepository;

    /**
     * Thống kê doanh thu + số vé theo ngày trong khoảng [from, to].
     *
     * @param from        ngày bắt đầu (inclusive)
     * @param to          ngày kết thúc (inclusive)
     * @param granularity DAY | WEEK | MONTH
     */
    @Transactional(readOnly = true)
    public List<RevenueStatsResponse> getRevenueSummary(
            LocalDate from, LocalDate to, String granularity) {

        LocalDateTime dtFrom = from.atStartOfDay();
        LocalDateTime dtTo   = to.plusDays(1).atStartOfDay();   // exclusive

        // Query aggregates by day từ DB rồi re-group ở service theo granularity
        List<Object[]> rows = bookingRepository.aggregateRevenueByDay(
                COMPLETED_STATUSES, dtFrom, dtTo);

        // Group by granularity
        return switch (granularity.toUpperCase()) {
            case "WEEK"  -> groupByWeek(rows);
            case "MONTH" -> groupByMonth(rows);
            default      -> rows.stream()
                    .map(r -> toRevenueStats(formatDay(r[0]), r))
                    .toList();
        };
    }

    /**
     * Thống kê doanh thu phân tách theo từng hồ bơi trong khoảng [from, to].
     */
    @Transactional(readOnly = true)
    public List<RevenueStatsResponse> getRevenueByPool(LocalDate from, LocalDate to) {
        LocalDateTime dtFrom = from.atStartOfDay();
        LocalDateTime dtTo   = to.plusDays(1).atStartOfDay();

        return bookingRepository.aggregateRevenueByPool(COMPLETED_STATUSES, dtFrom, dtTo)
                .stream()
                .map(r -> RevenueStatsResponse.builder()
                        .period(r[1] != null ? r[1].toString() : "Unknown")
                        .totalRevenue(toBigDecimal(r[2]))
                        .ticketsSold(toInt(r[3]))
                        .bookingCount(toLong(r[4]))
                        .poolId(r[0] != null ? ((Number) r[0]).longValue() : null)
                        .poolName(r[1] != null ? r[1].toString() : null)
                        .build())
                .toList();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Group các dòng daily thành weekly (ISO Week: yyyy-Www)
     */
    private List<RevenueStatsResponse> groupByWeek(List<Object[]> rows) {
        java.util.Map<String, Object[]> grouped = new java.util.LinkedHashMap<>();
        for (Object[] r : rows) {
            LocalDate d   = toLocalDate(r[0]);
            String weekKey = d.getYear() + "-W" +
                    String.format("%02d", d.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()));
            grouped.merge(weekKey, r.clone(), (a, b) -> {
                a[1] = toBigDecimal(a[1]).add(toBigDecimal(b[1]));
                a[2] = toInt(a[2]) + toInt(b[2]);
                a[3] = toLong(a[3]) + toLong(b[3]);
                return a;
            });
        }
        return grouped.entrySet().stream()
                .map(e -> toRevenueStats(e.getKey(), e.getValue()))
                .toList();
    }

    /**
     * Group các dòng daily thành monthly (yyyy-MM)
     */
    private List<RevenueStatsResponse> groupByMonth(List<Object[]> rows) {
        java.util.Map<String, Object[]> grouped = new java.util.LinkedHashMap<>();
        for (Object[] r : rows) {
            LocalDate d      = toLocalDate(r[0]);
            String monthKey  = d.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            grouped.merge(monthKey, r.clone(), (a, b) -> {
                a[1] = toBigDecimal(a[1]).add(toBigDecimal(b[1]));
                a[2] = toInt(a[2]) + toInt(b[2]);
                a[3] = toLong(a[3]) + toLong(b[3]);
                return a;
            });
        }
        return grouped.entrySet().stream()
                .map(e -> toRevenueStats(e.getKey(), e.getValue()))
                .toList();
    }

    private RevenueStatsResponse toRevenueStats(String period, Object[] r) {
        return RevenueStatsResponse.builder()
                .period(period)
                .totalRevenue(toBigDecimal(r[1]))
                .ticketsSold(toInt(r[2]))
                .bookingCount(toLong(r[3]))
                .build();
    }

    private String formatDay(Object raw) {
        LocalDate d = toLocalDate(raw);
        return d.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private LocalDate toLocalDate(Object raw) {
        if (raw instanceof LocalDate ld) return ld;
        if (raw instanceof java.sql.Date sd) return sd.toLocalDate();
        return LocalDate.parse(raw.toString());
    }

    private BigDecimal toBigDecimal(Object raw) {
        if (raw instanceof BigDecimal bd) return bd;
        if (raw instanceof Number n)       return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }

    private Integer toInt(Object raw) {
        if (raw instanceof Number n) return n.intValue();
        return 0;
    }

    private Long toLong(Object raw) {
        if (raw instanceof Number n) return n.longValue();
        return 0L;
    }
}
