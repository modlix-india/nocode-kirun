export class DurationUtils {
    public static getDuration(
        differenceInMilliseconds: number,
        key: string,
        diffDays: number,
        diffHours: number,
        diffMins: number,
        diffSecs: number,
    ) {
        let prefix = '';
        let suffix = '';
        if (key == 'N' || key == 'A' || key == 'I') {
            if (key == 'A') {
                differenceInMilliseconds > 0 ? (suffix = ' ago') : (prefix = 'after ');
            }
            if (key == 'I') {
                differenceInMilliseconds > 0 ? (suffix = ' ago') : (prefix = 'in ');
            }
            if (diffDays >= 3651) {
                return prefix + `more than 10 years` + suffix;
            }
            if (diffDays >= 1825 && diffDays <= 3650) {
                return prefix + `5 to 10 years` + suffix;
            }
            if (diffDays >= 1096 && diffDays <= 1824) {
                return prefix + `3 to 5 years` + suffix;
            }
            if (diffDays >= 731 && diffDays <= 195) {
                return prefix + `2 to 3 years` + suffix;
            }
            if (diffDays >= 365 && diffDays <= 730) {
                return prefix + `a year to 2 years` + suffix;
            }
            if (diffDays >= 180 && diffDays <= 364) {
                return prefix + `6 months to a year` + suffix;
            }
            if (diffDays >= 120 && diffDays <= 179) {
                return prefix + `4 to 6 months` + suffix;
            }
            if (diffDays >= 90 && diffDays <= 119) {
                return prefix + `3 months` + suffix;
            }
            if (diffDays >= 60 && diffDays <= 89) {
                return prefix + `2 months` + suffix;
            }
            if (diffDays >= 30 && diffDays <= 59) {
                return prefix + `a month` + suffix;
            }
            if (diffDays >= 14 && diffDays <= 29) {
                return prefix + `2 weeks` + suffix;
            }
            if (diffDays >= 7 && diffDays <= 13) {
                return prefix + `a week` + suffix;
            }
            if (diffHours >= 31 && diffDays <= 6) {
                return prefix + `2 days` + suffix;
            }
            if (diffHours >= 24 && diffHours <= 30) {
                return prefix + `a day` + suffix;
            }
            if (diffHours >= 3 && diffHours <= 23) {
                return prefix + `4 hours` + suffix;
            }
            if (diffHours >= 1 && diffHours <= 3) {
                return prefix + `few hour` + suffix;
            }
            if (diffMins >= 16 && diffMins <= 59) {
                return prefix + `16 minutes` + suffix;
            }
            if (diffMins >= 2 && diffMins <= 15) {
                return prefix + `few minutes` + suffix;
            }
            if (diffSecs >= 45 && diffSecs <= 59) {
                return prefix + `45 seconds` + suffix;
            }
            if (diffSecs >= 2 && diffSecs <= 44) {
                return prefix + `few seconds` + suffix;
            }
            return 'now';
        } else {
            if (key == 'EA') {
                differenceInMilliseconds > 0 ? (suffix = ' ago') : (prefix = 'after ');
            }
            if (key == 'EI') {
                differenceInMilliseconds > 0 ? (suffix = ' ago') : (prefix = 'in ');
            }
            if (diffDays >= 3651) {
                return prefix + `more than 10 years` + suffix;
            }
            if (diffDays >= 1825 && diffDays <= 3650) {
                return prefix + `5 to 10 years` + suffix;
            }
            if (diffDays >= 1096 && diffDays <= 1824) {
                return prefix + `3 to 5 years` + suffix;
            }
            if (diffDays >= 731 && diffDays <= 195) {
                return prefix + `2 to 3 years` + suffix;
            }
            if (diffDays >= 365 && diffDays <= 730) {
                return prefix + `1 year to 2 years` + suffix;
            }
            if (diffDays >= 180 && diffDays <= 364) {
                return prefix + `6 months to a year` + suffix;
            }
            if (diffDays >= 120 && diffDays <= 179) {
                return prefix + `4 to 6 months` + suffix;
            }
            if (diffDays >= 90 && diffDays <= 119) {
                return prefix + `3 months` + suffix;
            }
            if (diffDays >= 60 && diffDays <= 89) {
                return prefix + `2 months` + suffix;
            }
            if (diffDays >= 30 && diffDays <= 59) {
                return prefix + `1 month` + suffix;
            }
            if (diffDays >= 14 && diffDays <= 29) {
                return prefix + `2 weeks` + suffix;
            }
            if (diffDays >= 7 && diffDays <= 13) {
                return prefix + `1 week` + suffix;
            }
            if (diffHours >= 31 && diffDays <= 6) {
                return prefix + `2 days` + suffix;
            }
            if (diffHours >= 24 && diffHours <= 30) {
                return prefix + `1 day` + suffix;
            }
            if (diffHours >= 3 && diffHours <= 23) {
                return prefix + `4 hours` + suffix;
            }
            if (diffHours >= 1 && diffHours <= 3) {
                return prefix + `1 hour` + suffix;
            }
            if (diffMins >= 16 && diffMins <= 59) {
                return prefix + `16 minutes` + suffix;
            }
            if (diffMins >= 2 && diffMins <= 15) {
                return prefix + `3 minutes` + suffix;
            }
            if (diffSecs >= 45 && diffSecs <= 59) {
                return prefix + `45 seconds` + suffix;
            }
            if (diffSecs >= 2 && diffSecs <= 44) {
                return prefix + `2 seconds` + suffix;
            }
            return '1 second';
        }
    }
}
