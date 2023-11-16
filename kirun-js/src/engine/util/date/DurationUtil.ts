export class DurationUtils {
    public static getDuration(
        diffDays: number,
        diffHours: number,
        diffMins: number,
        diffSecs: number,
    ) {
        if (diffDays >= 3651) {
            return `more than 10 years`
        }
        if (diffDays >= 1825 && diffDays <= 3650) {
            return `5 to 10 years`
        }
        if (diffDays >= 1096 && diffDays <= 1824) {
            return `3 to 5 years`
        }
        if (diffDays >= 731 && diffDays <= 195) {
            return `2 to 3 years`
        }
        if (diffDays >= 365 && diffDays <= 730) {
            return `a year to 2 years`
        }
        if (diffDays >= 180 && diffDays <= 364) {
            return `6 months to a year`
        }
        if (diffDays >= 120 && diffDays <= 179) {
            return `4 to 6 months`
        }
        if (diffDays >= 90 && diffDays <= 119) {
            return `3 months`
        }
        if (diffDays >= 60 && diffDays <= 89) {
            return `2 months`
        }
        if (diffDays >= 30 && diffDays <= 59) {
            return `a month`
        }
        if (diffDays >= 14 && diffDays <= 29) {
            return `2 weeks`
        }
        if (diffDays >= 7 && diffDays <= 13) {
            return `a week`
        }
        if (diffHours >= 31 && diffDays <= 6) {
            return `2 days`
        }
        if (diffHours >= 24 && diffHours <= 30) {
            return `a day`
        }
        if (diffHours >= 3 && diffHours <= 23) {
            return `4 hours`
        }
        if (diffHours >= 1 && diffHours <= 3) {
            return `few hour`
        }
        if (diffMins >= 16 && diffMins <= 59) {
            return `16 minutes`
        }
        if (diffMins >= 2 && diffMins <= 15) {
            return `few minutes`
        }
        if (diffSecs >= 45 && diffSecs <= 59) {
            return `45 seconds`
        }
        if (diffSecs >= 2 && diffSecs <= 44) {
            return `few seconds`
        }
        return 'now';
    }
}
