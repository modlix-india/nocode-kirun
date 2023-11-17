export class DurationUtils {
    public static gettingDifference(
        helperArray: Array<number>,
        key: string,
        prefix: string,
        suffix: string,
    ) {
        console.log(helperArray);

        let exactValue = false;

        key == 'E' ? (exactValue = true) : (exactValue = false);

        let i;
        for (i = 0; i < helperArray.length; i++) {
            if (helperArray[i] != 0) {
                break;
            }
        }

        if (i == 0) {
            return helperArray[i] == 1
                ? exactValue
                    ? `${prefix}1 year${suffix}`
                    : `${prefix}a year${suffix}`
                : `${prefix}${helperArray[i]} years${suffix}`;
        }
        if (i == 1) {
            return helperArray[i] == 1
                ? exactValue
                    ? `${prefix}1 month${suffix}`
                    : `${prefix}a month${suffix}`
                : `${prefix}${helperArray[i]} months${suffix}`;
        }
        if (i == 2) {
            return helperArray[i] == 1
                ? exactValue
                    ? `${prefix}1 day${suffix}`
                    : `${prefix}a day${suffix}`
                : `${prefix}${helperArray[i]} days${suffix}`;
        }
        if (i == 3) {
            return helperArray[i] >= 1 && helperArray[i] <= 3
                ? `${prefix}few hours${suffix}`
                : `${prefix}${helperArray[i]} minutes${suffix}`;
        }
        if (i == 4) {
            return helperArray[i] >= 1 && helperArray[i] <= 15 && !exactValue
                ? `${prefix}few minutes${suffix}`
                : `${prefix}${helperArray[i]} minutes${suffix}`;
        }
        if (i == 5) {
            return helperArray[i] <= 44 && !exactValue
                ? helperArray[i] <= 2
                    ? `now`
                    : `${prefix}few seconds${suffix}`
                : `${prefix}${helperArray[i]} seconds${suffix}`;
        }
    }

    public static getDuration(firstDate: Date, secondDate: Date, key: string) {
        let prefix = '';
        let suffix = '';
        let helperArray = [];

        const diffInMilli = firstDate.getTime() - secondDate.getTime();

        helperArray.push(firstDate.getFullYear() - secondDate.getFullYear());
        helperArray.push(firstDate.getMonth() - secondDate.getMonth());
        helperArray.push(firstDate.getDate() - secondDate.getDate());
        helperArray.push(firstDate.getHours() - secondDate.getHours());
        helperArray.push(Math.abs(firstDate.getMinutes() - secondDate.getMinutes()));
        helperArray.push(Math.abs(firstDate.getSeconds() - secondDate.getSeconds()));

        if (key == 'A' || key == 'EA') {
            diffInMilli > 0 ? (prefix = 'after ') : (suffix = ' ago');
        }
        if (key == 'I' || key == 'EI') {
            diffInMilli > 0 ? (prefix = 'in ') : (suffix = ' ago');
        }

        return this.gettingDifference(helperArray, key, prefix, suffix);
    }
}
