export class DurationUtils {
    public static getDuration(
        diffDays: number,
        diffHours: number,
        diffMins: number,
        diffSecs: number,
    ) {
        if (diffDays >= 548) return Math.floor(diffDays / 365) + ' years';

        if (diffDays <= 547 && diffDays >= 320) return 'a year';

        if (diffDays <= 319 && diffDays >= 45) return Math.floor(diffDays / 31) + ' month';

        if (diffDays <= 45 && diffDays >= 26) return 'a month';

        if (diffDays <= 25 && diffHours >= 36) return diffDays + ' days';

        if (diffHours <= 35 && diffHours >= 22) return 'a day';

        if (diffHours <= 21 && diffMins >= 90) return diffHours + ' hours';

        if (diffMins <= 89 && diffMins >= 45) return 'an hour';

        if (diffMins <= 44 && diffSecs >= 90) return diffMins + ' minutes';

        if (diffSecs <= 89 && diffSecs >= 45) return 'a minute';

        return 'a few seconds';
    }
}
