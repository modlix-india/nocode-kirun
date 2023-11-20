import { KIRuntimeException } from '../../exception/KIRuntimeException';

export class DurationUtils {
    static weeks: number = 0;

    static stringArray = ['years', 'months', 'days', 'hours', 'minutes', 'seconds'];

    public static getDifference(
        helperArray: Array<number>,
        key: string,
        prefix: string,
        suffix: string,
    ) {
        let i;
        for (i = 0; i < helperArray.length; i++) {
            if (helperArray[i] != 0) {
                break;
            }
        }

        if (i == 0) {
            return helperArray[i] == 1
                ? `${prefix}a year${suffix}`
                : `${prefix}${helperArray[i]} years${suffix}`;
        }
        if (i == 1) {
            return helperArray[i] == 1
                ? `${prefix}a month${suffix}`
                : `${prefix}${helperArray[i]} months${suffix}`;
        }
        if (i == 2) {
            return helperArray[i] == 1
                ? `${prefix}a day${suffix}`
                : `${prefix}${helperArray[i]} days${suffix}`;
        }
        if (i == 3) {
            return helperArray[i] >= 1 && helperArray[i] <= 3
                ? `${prefix}few hours${suffix}`
                : `${prefix}${helperArray[i]} minutes${suffix}`;
        }
        if (i == 4) {
            return helperArray[i] >= 1 && helperArray[i] <= 15
                ? `${prefix}few minutes${suffix}`
                : `${prefix}${helperArray[i]} minutes${suffix}`;
        }
        if (i == 5) {
            return helperArray[i] <= 44
                ? helperArray[i] <= 2
                    ? `now`
                    : `${prefix}few seconds${suffix}`
                : `${prefix}${helperArray[i]} seconds${suffix}`;
        }
    }

    public static getExactDifference(
        helperArray: Array<number>,
        key: string,
        prefix: string,
        suffix: string,
    ) {
        let i;
        for (i = 0; i < helperArray.length; i++) {
            if (helperArray[i] != 0) {
                break;
            }
        }

        let end = key.slice(2) != 'A' ? i + parseInt(key.slice(2)) : helperArray.length;

        let finalString = `${helperArray[i]} ${
            helperArray[i] == 1 ? this.stringArray[i].slice(0, -1) : this.stringArray[i]
        }`;

        if (end <= helperArray.length) {
            while (i + 1 < end) {
                finalString =
                    finalString +
                    ' ' +
                    `${helperArray[i + 1]} ${
                        helperArray[i + 1] == 1
                            ? this.stringArray[i + 1].slice(0, -1)
                            : this.stringArray[i + 1]
                    }`;
                i++;
            }
            return `${prefix}${finalString}${suffix}`;
        } else {
            throw new KIRuntimeException(`Please provide a valid key.`);
        }
    }

    public static getDuration(firstDate: Date, secondDate: Date, key: string) {
        let prefix = '';
        let suffix = '';
        let helperArray = [];

        const diffInMilli = firstDate.getTime() - secondDate.getTime();

        this.weeks = Math.floor(Math.abs(diffInMilli / (1000 * 60 * 60 * 24 * 7)));

        helperArray.push(Math.abs(firstDate.getFullYear() - secondDate.getFullYear()));
        helperArray.push(Math.abs(firstDate.getMonth() - secondDate.getMonth()));
        helperArray.push(Math.abs(firstDate.getDate() - secondDate.getDate()));
        helperArray.push(Math.abs(firstDate.getHours() - secondDate.getHours()));
        helperArray.push(Math.abs(firstDate.getMinutes() - secondDate.getMinutes()));
        helperArray.push(Math.abs(firstDate.getSeconds() - secondDate.getSeconds()));

        if (key == 'EY') {
            return helperArray[0] == 1 ? '1 year' : helperArray[0] + ' years';
        }
        if (key == 'EM') {
            return helperArray[1] == 1 ? '1 month' : helperArray[1] + ' months';
        }
        if (key == 'EW') {
            return this.weeks == 1 ? '1 week' : this.weeks + ' weeks';
        }
        if (key == 'ED') {
            return helperArray[2] == 1 ? '1 day' : helperArray[2] + ' days';
        }
        if (key == 'EH') {
            return helperArray[3] == 1 ? '1 hour' : helperArray[3] + ' hours';
        }
        if (key == 'ES') {
            return helperArray[4] == 1 ? '1 second' : helperArray[4] + ' seconds';
        }
        if (key == 'A' || key[1] == 'A') {
            diffInMilli > 0 ? (prefix = 'after ') : (suffix = ' ago');
        }
        if (key == 'I' || key[1] == 'I') {
            diffInMilli > 0 ? (prefix = 'in ') : (suffix = ' ago');
        }
        if (key == 'EN' || key == 'EA' || key == 'EI') {
            key = key + '1';
        }

        if (key.length <= 2) {
            return this.getDifference(helperArray, key, prefix, suffix);
        } else {
            return this.getExactDifference(helperArray, key, prefix, suffix);
        }
    }
}
