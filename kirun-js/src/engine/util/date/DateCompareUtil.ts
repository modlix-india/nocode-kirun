import { KIRuntimeException } from '../../exception/KIRuntimeException';

const iso8601Pattern =
    /^([+-]?\d{6}|\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\d|3[0-1])T([0-1]\d|2[0-3]):([0-5]\d):([0-5]\d)(\.\d+)?(Z|([+-]\d{2}:\d{2}))?$/;

export class DateCompareUtil {
    public static readonly YEAR: string = 'year';

    public static readonly MONTH: string = 'month';

    public static readonly DAY: string = 'day';

    public static readonly HOUR: string = 'hour';

    public static readonly MINUTE: string = 'minute';

    public static readonly SECOND: string = 'second';

    private LOOPDURATION = 0;

    public inBetween(firstDate: string, secondDate: string, thirdDate: string, fields: []) {
        return (
            this.compareFields(firstDate, thirdDate, 'before', fields) &&
            this.compareFields(secondDate, thirdDate, 'after', fields)
        );
    }

    public compareFields(
        firstDate: string,
        secondDate: string,
        operationName: string,
        fields: [],
    ): boolean {
        let equal = true;

        let firstDateTime;
        let secondDateTime;

        for (let i = 0; i < fields.length; i++) {
            let currentAnswer = false;
            this.loopDuration(fields[i]);
            firstDateTime = this.getTimeInMilliSeconds(firstDate);
            secondDateTime = this.getTimeInMilliSeconds(secondDate);
            if (firstDateTime && secondDateTime) {
                if (operationName == 'same') {
                    currentAnswer = firstDateTime == secondDateTime;
                }
                if (operationName == 'after') {
                    currentAnswer = firstDateTime >= secondDateTime;
                }
                if (operationName == 'before') {
                    currentAnswer = firstDateTime <= secondDateTime;
                }
            }
            equal = equal && currentAnswer;
        }

        return equal;
    }

    private getTimeInMilliSeconds(date: string): number | null {
        const match = date.match(iso8601Pattern);
        let arr = [1970, 0, 0, 0, 0, 0];
        if (match) {
            for (let i = 1; i < this.LOOPDURATION + 1; i++) {
                arr[i - 1] = parseInt(match[i]);
            }
            const timeInMilliSeconds = new Date(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]);
            return timeInMilliSeconds.getTime();
        }
        return null;
    }

    private loopDuration(field: string) {
        if (field == DateCompareUtil.YEAR) {
            this.LOOPDURATION = 1;
        }
        if (field == DateCompareUtil.MONTH) {
            this.LOOPDURATION = 2;
        }
        if (field == DateCompareUtil.DAY) {
            this.LOOPDURATION = 3;
        }
        if (field == DateCompareUtil.HOUR) {
            this.LOOPDURATION = 4;
        }
        if (field == DateCompareUtil.MINUTE) {
            this.LOOPDURATION = 5;
        }
        if (field == DateCompareUtil.SECOND) {
            this.LOOPDURATION = 6;
        }
    }
}
