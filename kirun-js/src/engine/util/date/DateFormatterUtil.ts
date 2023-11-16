const DAYS_OF_WEEK = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
const MONTHS_OF_YEAR = [
    'January',
    'February',
    'March',
    'April',
    'May',
    'June',
    'July',
    'August',
    'September',
    'October',
    'November',
    'December',
];
const DAYS_OF_MONTH_SUFFIX = ['th', 'st', 'nd', 'rd'];

export interface TimeStampObject {
    year: number;
    monthIndex: number;
    date: number;
    hours?: number;
    minutes?: number;
    seconds?: number;
    millis?: number;
}

export function formattedStringFromDate(date: Date, pattern: string): string {
    let patterns: Array<string> = patternSplitting(pattern);
    const formattedDate = patterns
        .map((pattern, ind) => {
            if (ind % 2 === 1) return pattern;

            let str = '';
            while (pattern.length) {
                if (TOKEN_FUNCTION[pattern[0]]) {
                    const nextValues = TOKEN_FUNCTION[pattern[0]](str, pattern, date);
                    str = nextValues.str;
                    pattern = nextValues.pattern;
                } else {
                    str += pattern[0];
                    pattern = pattern.slice(1);
                }
            }
            return str;
        })
        .join('');

    return formattedDate.replace(/''/g, "'");
}

const TOKEN_FUNCTION: any = {
    M: (str: string, pattern: string, date: Date) => {
        if (pattern.startsWith('MMMM')) {
            str += MONTHS_OF_YEAR[date.getMonth()];
            return { str, pattern: pattern.slice(4) };
        } else if (pattern.startsWith('MMM')) {
            str += MONTHS_OF_YEAR[date.getMonth()].slice(0, 3);
            return { str, pattern: pattern.slice(3) };
        } else if (pattern.startsWith('Mth')) {
            let num = date.getMonth() + 1;
            str += numberToOrdinal(num);
            return { str , pattern: pattern.slice(3) }
        } else if (pattern.startsWith('MTH')) {
            let num = date.getMonth() + 1;
            str += numberToOrdinal(num).toUpperCase();
            return { str , pattern: pattern.slice(3) }
        } else if (pattern.startsWith('MM')) {
            str += (date.getMonth() + 1).toString().padStart(2, '0');
            return { str, pattern: pattern.slice(2) };
        }

        str += date.getMonth() + 1;
        return { str, pattern: pattern.slice(1) };
    },

    Q: (str: string, pattern: string, date: Date) => {
        const quarter = Math.floor(date.getMonth() / 3) + 1;
        if (pattern.startsWith('QQ')) {
            str += quarter.toString().padStart(2, '0');
            return { str, pattern: pattern.slice(2) };
        } else if (pattern.startsWith('Qth')) {
            str += numberToOrdinal(quarter);
            return { str, pattern: pattern.slice(3) };
        } else if (pattern.startsWith('QTH')) {
            str += numberToOrdinal(quarter).toUpperCase();
            return { str, pattern: pattern.slice(3) };
        }
        str += quarter;
        return { str, pattern: pattern.slice(1) };
    },

    D: (str: string, pattern: string, date: Date) => {
        if (pattern.startsWith('DDD')) {
            let dayOfTheYear = 0;
            for (let i = 0; i < date.getMonth(); i++) {
                dayOfTheYear += new Date(date.getFullYear(), i + 1, 0).getDate();
            }
            dayOfTheYear += date.getDate();

            if (pattern.startsWith('DDDD')) {
                str += dayOfTheYear.toString().padStart(3, '0');
                return { str, pattern: pattern.slice(4) };
            } else if (pattern.startsWith('DDDth')) {
                str += numberToOrdinal(dayOfTheYear);
                return { str, pattern: pattern.slice(5) };
            } else if (pattern.startsWith('DDDTH')) {
                str += numberToOrdinal(dayOfTheYear).toUpperCase();
                return { str, pattern: pattern.slice(5) };
            }
            str += dayOfTheYear;
            return { str, pattern: pattern.slice(3) };
        }

        let dayOfTheMonth = date.getDate();
        if (pattern.startsWith('DD')) {
            str += dayOfTheMonth.toString().padStart(2, '0');
            return { str, pattern: pattern.slice(2) };
        } else if (pattern.startsWith('Dth')) {
            str += numberToOrdinal(dayOfTheMonth);
            return { str, pattern: pattern.slice(3) };
        } else if (pattern.startsWith('DTH')) {
            str += numberToOrdinal(dayOfTheMonth).toUpperCase();
            return { str, pattern: pattern.slice(3) };
        }

        str += dayOfTheMonth;
        return { str, pattern: pattern.slice(1) };
    },

    d: (str: string, pattern: string, date: Date) => {
        let dayOfTheWeek = date.getDay();
        if (pattern.startsWith('dddd')) {
            return {
                str: str + DAYS_OF_WEEK[dayOfTheWeek],
                pattern: pattern.slice(4),
            };
        } else if (pattern.startsWith('ddd')) {
            return {
                str: str + DAYS_OF_WEEK[dayOfTheWeek].slice(0, 3),
                pattern: pattern.slice(3),
            };
        } else if (pattern.startsWith('dd')) {
            return {
                str: str + DAYS_OF_WEEK[dayOfTheWeek].slice(0, 2),
                pattern: pattern.slice(2),
            };
        } else if (pattern.startsWith('dth')) {
            return {
                str: str + numberToOrdinal(dayOfTheWeek + 1),
                pattern: pattern.slice(3),
            };
        } else if (pattern.startsWith('dTH')) {
            return {
                str: str + numberToOrdinal(dayOfTheWeek + 1).toUpperCase(),
                pattern: pattern.slice(3),
            };
        }
        return { str: str + (dayOfTheWeek + 1), pattern: pattern.slice(1) };
    },

    W: weekOfTheYear,
    w: weekOfTheYear,
    Y: justYear,
    y: justYear,
    N: (str: string, pattern: string, date: Date) => {
        if (pattern.startsWith('NNNN')) {
            str += date.getFullYear() < 0 ? 'Before Common Era' : 'After Common Era';
            return { str, pattern: pattern.slice(4) };
        }
        if (pattern.startsWith('NNN')) {
            str += date.getFullYear() < 0 ? 'BCE' : 'CE';
            return { str, pattern: pattern.slice(3) };
        }
        if (pattern.startsWith('NN')) {
            str += date.getFullYear() < 0 ? 'BCE' : 'CE';
            return { str, pattern: pattern.slice(2) };
        }
        str += date.getFullYear() < 0 ? 'BCE' : 'CE';
        return { str, pattern: pattern.slice(1) };
    },
    A: (str: string, pattern: string, date: Date) => {
        str += date.getHours() < 12 ? 'AM' : 'PM';
        return { str, pattern: pattern.slice(1) };
    },
    a: (str: string, pattern: string, date: Date) => {
        str += date.getHours() < 12 ? 'am' : 'pm';
        return { str, pattern: pattern.slice(1) };
    },
    h: (str: string, pattern: string, date: Date) => {
        let hours = date.getHours();
        if (hours > 12) hours -= 12;
        if (hours === 0) hours = 12;

        if (pattern.startsWith('hh')) {
            str += hours.toString().padStart(2, '0');
            return { str, pattern: pattern.slice(2) };
        } else if (pattern.startsWith('hth')) {
            str += numberToOrdinal(hours);
            return { str, pattern: pattern.slice(3) };
        } else if (pattern.startsWith('hTH')) {
            str += numberToOrdinal(hours).toUpperCase();
            return { str, pattern: pattern.slice(3) };
        }
        str += hours;
        return { str, pattern: pattern.slice(1) };
    },
    H: (str: string, pattern: string, date: Date) => {
        if (pattern.startsWith('HH')) {
            str += date.getHours().toString().padStart(2, '0');
            return { str, pattern: pattern.slice(2) };
        } else if (pattern.startsWith('Hth')) {
            str += numberToOrdinal(date.getHours());
            return { str, pattern: pattern.slice(3) };
        } else if (pattern.startsWith('HTH')) {
            str += numberToOrdinal(date.getHours()).toUpperCase();
            return { str, pattern: pattern.slice(3) };
        }
        str += date.getHours();
        return { str, pattern: pattern.slice(1) };
    },
    k: (str: string, pattern: string, date: Date) => {
        let hours = date.getHours();
        if (hours === 0) hours = 24;
        if (pattern.startsWith('kk')) {
            str += hours.toString().padStart(2, '0');
            return { str, pattern: pattern.slice(2) };
        } else if (pattern.startsWith('kth')) {
            str += numberToOrdinal(hours);
            return { str, pattern: pattern.slice(3) };
        } else if (pattern.startsWith('kTH')) {
            str += numberToOrdinal(hours).toUpperCase();
            return { str, pattern: pattern.slice(3) };
        }
        str += hours;
        return { str, pattern: pattern.slice(1) };
    },
    m: (str: string, pattern: string, date: Date) => {
        if (pattern.startsWith('mm')) {
            str += date.getMinutes().toString().padStart(2, '0');
            return { str, pattern: pattern.slice(2) };
        } else if (pattern.startsWith('mth') || pattern.startsWith('mTH')) {
            const hasUpperCase = pattern.startsWith('mTH');
            str += hasUpperCase
                ? numberToOrdinal(date.getMinutes()).toUpperCase()
                : numberToOrdinal(date.getMinutes());
            return { str, pattern: pattern.slice(3) };
        }
        str += date.getMinutes();
        return { str, pattern: pattern.slice(1) };
    },
    s: (str: string, pattern: string, date: Date) => {
        if (pattern.startsWith('ss')) {
            str += date.getSeconds().toString().padStart(2, '0');
            return { str, pattern: pattern.slice(2) };
        } else if (pattern.startsWith('sth')) {
            str += numberToOrdinal(date.getSeconds());
            return { str, pattern: pattern.slice(3) };
        }
        str += date.getSeconds();
        return { str, pattern: pattern.slice(1) };
    },
    S: (str: string, pattern: string, date: Date) => {
        if (pattern.startsWith('Sth') || pattern.startsWith('STH')) {
            const hasUpperCase = pattern.startsWith('STH');
            str += hasUpperCase
                ? numberToOrdinal(date.getMilliseconds()).toUpperCase()
                : numberToOrdinal(date.getMilliseconds());
            return { str, pattern: pattern.slice(3) };
        }
        let i = 0;
        while (i < pattern.length && pattern[i] === 'S') i++;

        if (i <= 3) {
            let x = date.getMilliseconds().toString().padStart(3, '0');
            str += x.substring(0, i).padStart(i, '0');
        } else
            str += date
                .getMilliseconds()
                .toString()
                .padStart(i, '0')
                .padEnd(i - 3, '0');
        return { str, pattern: pattern.slice(i) };
    },
    Z: (str: string, pattern: string, date: Date) => {
        let offset = date.getTimezoneOffset() * -1;
        const isNegative = offset < 0;
        offset = Math.abs(offset);
        const hours = Math.floor(offset / 60);
        const minutes = offset % 60;
        if (pattern.startsWith('ZZ')) {
            str +=
                (isNegative ? '-' : '+') +
                hours.toString().padStart(2, '0') +
                minutes.toString().padStart(2, '0');
            return { str, pattern: pattern.slice(2) };
        }
        str +=
            (isNegative ? '-' : '+') +
            hours.toString().padStart(2, '0') +
            ':' +
            minutes.toString().padStart(2, '0');
        return { str, pattern: pattern.slice(1) };
    },
    x: (str: string, pattern: string, date: Date) => {
        return {
            str: str + date.getTime() / 1000,
            pattern: pattern.slice(1),
        };
    },
    X: (str: string, pattern: string, date: Date) => {
        return {
            str: str + date.getTime(),
            pattern: pattern.slice(1),
        };
    },
};

function justYear(str: string, pattern: string, date: Date) {
    if (pattern.toUpperCase().startsWith('YYYY')) {
        str += date.getFullYear().toString().padStart(4, '0');
        return { str, pattern: pattern.slice(4) };
    }

    if (pattern.toUpperCase().startsWith('YY')) {
        let year = date.getFullYear();
        str += year < 0 ? '-' + year.toString().slice(-2) : year.toString().slice(-2) ;
        return { str, pattern: pattern.slice(2)};
    }

    str += date.getFullYear();
    return {str, pattern: pattern.slice(1)};
}

function weekOfTheYear(str: string, pattern: string, date: Date) {
    let weekOfTheYear = 0;
    let dayOfTheYear = 0;
    for (let i = 0; i < date.getMonth(); i++) {
        dayOfTheYear += new Date(date.getFullYear(), i + 1, 0).getDate();
    }
    dayOfTheYear += date.getDate();
    weekOfTheYear = Math.floor(dayOfTheYear / 7) + 1;

    if (pattern.toUpperCase().startsWith('WW')) {
        str += weekOfTheYear.toString().padStart(2, '0');
        return { str, pattern: pattern.slice(2) };
    }

    if (pattern.toUpperCase().startsWith('WTH')) {
        str +=
            pattern.substring(1, 3) === 'TH'
                ? numberToOrdinal(weekOfTheYear).toUpperCase()
                : numberToOrdinal(weekOfTheYear);
        return { str, pattern: pattern.slice(3) };
    }

    return { str: str + weekOfTheYear, pattern: pattern.slice(1) };
}


function numberToOrdinal(num: number): string {
    if( (num % 10 > 3) || num % 100 >= 11 && num % 100 <= 13) 
        return num + DAYS_OF_MONTH_SUFFIX[0];

    return num + DAYS_OF_MONTH_SUFFIX[num % 10];
}

export function patternSplitting(str: string): string[] {
    let strs: string[] = [];

    let from = 0;

    for (let i = 0; i < str.length; i++) {
        if (str[i] !== "'") continue;

        if (i < str.length - 1 && str[i + 1] === "'") {
            i++;
            continue;
        }

        if (from === 0 && i === 0) {
            if (str.length === 1) return [str];
            from = 1;
            continue;
        }
        strs.push(str.slice(strs.length === 0 ? from : from + 1, i));
        from = i;
    }

    if (strs.length === 0) return [str];

    if (from !== str.length - 1) strs.push(str.slice(from + 1));

    return strs;
}

const VALUE_TOKEN_RULES: any = {
    M: [
        {
            key: 'MMMM',
            values: MONTHS_OF_YEAR.map((e) => e.toUpperCase()),
            caseInsensitive: true,
            notInteger: true,
            resultKey: 'monthIndex',
        },
        {
            key: 'MMM',
            length: 3,
            values: MONTHS_OF_YEAR.map((e) => e.substring(0, 3).toUpperCase()),
            caseInsensitive: true,
            notInteger: true,
            resultKey: 'monthIndex',
        },
        {
            key: 'Mth',
            hasTh: true,
            range: [1, 12],
            subtract: 1,
            resultKey: 'monthIndex',
        },
        {
            key: 'MTH',
            hasTh: true,
            range: [1, 12],
            subtract: 1,
            resultKey: 'monthIndex',
        },
        {
            key: 'MM',
            range: [1, 12],
            subtract: 1,
            resultKey: 'monthIndex',
        },
        {
            key: 'M',
            range: [1, 12],
            subtract: 1,
            resultKey: 'monthIndex',
        },
    ],
    Q: [
        {
            key: 'QQ',
            range: [1, 4],
            subtract: 1,
            resultKey: 'quarterIndex',
            length: 2,
        },
        {
            key: 'Qth',
            hasTh: true,
            range: [1, 4],
            subtract: 1,
            resultKey: 'quarterIndex',
        },
        {
            key: 'QTH',
            hasTh: true,
            range: [1, 4],
            subtract: 1,
            resultKey: 'quarterIndex',
        },
        {
            key: 'Q',
            range: [1, 4],
            subtract: 1,
            resultKey: 'quarterIndex',
        },
    ],
    D: [
        {
            key: 'DDDD',
            length: 3,
            range: [1, 366],
            resultKey: 'dateOfTheYear',
        },
        {
            key: 'DDDth',
            hasTh: true,
            range: [1, 366],
            resultKey: 'dateOfTheYear',
        },
        {
            key: 'DDDTH',
            hasTh: true,
            range: [1, 366],
            resultKey: 'dateOfTheYear',
        },
        {
            key: 'DDD',
            range: [1, 366],
            resultKey: 'dateOfTheYear',
        },
        {
            key: 'DD',
            range: [1, 31],
            resultKey: 'date',
            length: 2,
        },
        {
            key: 'Dth',
            hasTh: true,
            range: [1, 31],
            resultKey: 'date',
        },
        {
            key: 'DTH',
            hasTh: true,
            range: [1, 31],
            resultKey: 'date',
        },
        {
            key: 'D',
            range: [1, 31],
            resultKey: 'date',
        },
    ],
    d: [
        {
            key: 'dddd',
            values: DAYS_OF_WEEK.map((e) => e.toUpperCase()),
            caseInsensitive: true,
            notInteger: true,
            resultKey: 'dayOfTheWeek',
        },
        {
            key: 'ddd',
            length: 3,
            values: DAYS_OF_WEEK.map((e) => e.substring(0, 3).toUpperCase()),
            caseInsensitive: true,
            notInteger: true,
            resultKey: 'dayOfTheWeek',
        },
        {
            key: 'dd',
            length: 2,
            values: DAYS_OF_WEEK.map((e) => e.substring(0, 2).toUpperCase()),
            caseInsensitive: true,
            notInteger: true,
            resultKey: 'dayOfTheWeek',
        },
        {
            key: 'dth',
            hasTh: true,
            range: [1, 7],
            subtract: 1,
            resultKey: 'dayOfTheWeek',
        },
        {
            key: 'dTH',
            hasTh: true,
            range: [1, 7],
            subtract: 1,
            resultKey: 'dayOfTheWeek',
        },
        {
            key: 'd',
            range: [1, 7],
            subtract: 1,
            resultKey: 'dayOfTheWeek',
        },
    ],
    W: [
        {
            key: 'WW',
            range: [1, 53],
            resultKey: 'weekOfTheYear',
            length: 2,
        },
        {
            key: 'Wth',
            hasTh: true,
            range: [1, 53],
            resultKey: 'weekOfTheYear',
        },
        {
            key: 'WTH',
            hasTh: true,
            range: [1, 53],
            resultKey: 'weekOfTheYear',
        },
        {
            key: 'W',
            range: [1, 53],
            resultKey: 'weekOfTheYear',
        },
    ],
    w: [
        {
            key: 'ww',
            range: [1, 53],
            resultKey: 'weekOfTheYear',
            length: 2,
        },
        {
            key: 'wth',
            hasTh: true,
            range: [1, 53],
            resultKey: 'weekOfTheYear',
        },
        {
            key: 'wTH',
            hasTh: true,
            range: [1, 53],
            resultKey: 'weekOfTheYear',
        },
        {
            key: 'w',
            range: [1, 53],
            resultKey: 'weekOfTheYear',
        },
    ],
    Y: [
        {
            key: 'YYYY',
            length: 4,
            resultKey: 'year',
        },
        {
            key: 'YY',
            length: 2,
            resultKey: 'year',
            logic: (num: number) => (num >= 70 ? 1900 + num : 2000 + num),
        },
        {
            key: 'Y',
            resultKey: 'year',
        },
    ],
    y: [
        {
            key: 'yyyy',
            length: 4,
            resultKey: 'year',
        },
        {
            key: 'yy',
            length: 2,
            resultKey: 'year',
            logic: (num: number) => (num >= 70 ? 1900 + num : 2000 + num),
        },
        {
            key: 'y',
            resultKey: 'year',
        },
    ],
    N: [
        {
            key: 'NNNN',
            values: ['BEFORE COMMON ERA', 'AFTER COMMON ERA'],
            caseInsensitive: true,
            notInteger: true,
            resultKey: 'era',
            logic: (num: number) => (num === 0 ? -1 : 1),
        },
        {
            key: 'NNN',
            values: ['BCE', 'CE'],
            caseInsensitive: true,
            notInteger: true,
            resultKey: 'era',
            logic: (num: number) => (num === 0 ? -1 : 1),
        },
        {
            key: 'NN',
            values: ['BCE', 'CE'],
            caseInsensitive: true,
            notInteger: true,
            resultKey: 'era',
            logic: (num: number) => (num === 0 ? -1 : 1),
        },
        {
            key: 'N',
            values: ['BCE', 'CE'],
            caseInsensitive: true,
            notInteger: true,
            resultKey: 'era',
            logic: (num: number) => (num === 0 ? -1 : 1),
        },
    ],
    A: [
        {
            key: 'A',
            values: ['AM', 'PM'],
            caseInsensitive: false,
            notInteger: true,
            resultKey: 'amOrPm',
        },
    ],
    a: [
        {
            key: 'a',
            values: ['am', 'pm'],
            caseInsensitive: false,
            notInteger: true,
            resultKey: 'amOrPm',
        },
    ],
    h: [
        {
            key: 'hh',
            range: [1, 12],
            resultKey: 'hours',
            length: 2,
        },
        {
            key: 'hth',
            hasTh: true,
            range: [1, 12],
            resultKey: 'hours',
        },
        {
            key: 'hTH',
            hasTh: true,
            range: [1, 12],
            resultKey: 'hours',
        },
        {
            key: 'h',
            range: [1, 12],
            resultKey: 'hours',
        },
    ],
    H: [
        {
            key: 'HH',
            range: [0, 23],
            resultKey: 'hours',
            length: 2,
        },
        {
            key: 'Hth',
            hasTh: true,
            range: [0, 23],
            resultKey: 'hours',
        },
        {
            key: 'HTH',
            hasTh: true,
            range: [0, 23],
            resultKey: 'hours',
        },
        {
            key: 'H',
            range: [0, 23],
            resultKey: 'hours',
        },
    ],
    k: [
        {
            key: 'kk',
            range: [1, 24],
            resultKey: 'hours',
            length: 2,
        },
        {
            key: 'kth',
            hasTh: true,
            range: [1, 24],

            resultKey: 'hours',
        },
        {
            key: 'kTH',
            hasTh: true,
            range: [1, 24],

            resultKey: 'hours',
        },
        {
            key: 'k',
            range: [1, 24],
            resultKey: 'hours',
        },
    ],
    m: [
        {
            key: 'mm',
            range: [0, 59],
            resultKey: 'minutes',
            length: 2,
        },
        {
            key: 'mth',
            hasTh: true,
            range: [0, 59],
            resultKey: 'minutes',
        },
        {
            key: 'mTH',
            hasTh: true,
            range: [0, 59],
            resultKey: 'minutes',
        },
        {
            key: 'm',
            range: [0, 59],
            resultKey: 'minutes',
        },
    ],
    s: [
        {
            key: 'ss',
            range: [0, 59],
            resultKey: 'seconds',
            length: 2,
        },
        {
            key: 'sth',
            hasTh: true,
            range: [0, 59],
            resultKey: 'seconds',
        },
        {
            key: 'sTH',
            hasTh: true,
            range: [0, 59],
            resultKey: 'seconds',
        },
        {
            key: 's',
            range: [0, 59],
            resultKey: 'seconds',
        },
    ],
    S: [
        ...['SSSS', 'SSSSS', 'SSSSSS', 'SSSSSSS', 'SSSSSSSS', 'SSSSSSSSS'].reverse().map((e) => ({
            key: e,
            range: [0, 999],
            resultKey: 'millis',
            length: e.length,
            logic: (num: number) => Math.round(num / Math.pow(10, 3 - e.length)),
        })),
        {
            key: 'SSS',
            range: [0, 999],
            resultKey: 'millis',
            length: 3,
        },
        {
            key: 'SS',
            range: [0, 999],
            resultKey: 'millis',
            length: 2,
            logic: (num: number) => num * 10,
        },
        {
            key: 'Sth',
            hasTh: true,
            range: [0, 999],
            resultKey: 'millis',
        },
        {
            key: 'STH',
            hasTh: true,
            range: [0, 999],
            resultKey: 'millis',
        },
        {
            key: 'S',
            range: [0, 999],
            resultKey: 'millis',
            length: 1,
            logic: (num: number) => num * 100,
        },
    ],
    Z: (dateString: string, pattern: string, date: any) => {
        let x: RegExpMatchArray | null;

        if (pattern.startsWith('ZZ')) {
            x = dateString.match(/^([+-])(\d{2})(\d{2})/);
            pattern = pattern.slice(2);
        } else {
            x = dateString.match(/^([+-])(\d{2}):(\d{2})/);
            pattern = pattern.slice(1);
        }
        if (x == null || x?.length === 0)
            return {
                date,
                pattern: pattern.slice(1),
                dateString,
            };
        const isNegative = x[1] === '-';
        const hours = parseInt(x[2]);
        const minutes = parseInt(x[3]);

        let offset = hours * 60 + minutes;
        if (isNegative) offset *= -1;
        return {
            date: { ...date, offset },
            pattern: pattern.slice(1),
            dateString: dateString.slice(x[0].length),
        };
    },
    x: (dateString: string, pattern: string, date: any) => {
        let x = dateString.match(/^-?\d+/);
        if (x == null || x?.length === 0)
            return {
                date,
                pattern: pattern.slice(1),
                dateString,
            };

        return {
            date: { ...date, epoch: parseInt(x[0]) * 1000 },
            pattern: pattern.slice(1),
            dateString: dateString.slice(x[0].length),
        };
    },
    X: (dateString: string, pattern: string, date: any) => {
        let x = dateString.match(/^-?\d+/);
        if (x == null || x?.length === 0)
            return {
                date,
                pattern: pattern.slice(1),
                dateString,
            };

        return {
            date: { ...date, epoch: parseInt(x[0]) },
            pattern: pattern.slice(1),
            dateString: dateString.slice(x[0].length),
        };
    },
};

export function timeStampObjectToDate(obj: TimeStampObject): Date {
    return new Date(
        obj.year,
        obj.monthIndex,
        obj.date,
        obj.hours ?? 0,
        obj.minutes ?? 0,
        obj.seconds ?? 0,
        obj.millis ?? 0,
    );
}

function processParsedDate(date: any) {
    if (date.epoch !== undefined) return new Date(date.epoch);

    const now = new Date();

    if (date.year === undefined) date.year = now.getFullYear();
    if (date.monthIndex === undefined) {
        if (date.quarterIndex !== undefined) date.monthIndex = date.quarterIndex * 3;
        else date.monthIndex = now.getMonth();
    }
    if (date.date === undefined) {
        if (date.dateOfTheYear !== undefined || date.weekOfTheYear !== undefined) {
            let x = date.dateOfTheYear !== undefined ? date.dateOfTheYear : date.weekOfTheYear * 7;
            for (let i = 0; i < 12; i++) {
                const days = new Date(date.year, i + 1, 0).getDate();
                if (x <= days) {
                    date.monthIndex = i;
                    date.date = x;
                    break;
                }
                x -= days;
            }
        }
        if (date.quarterIndex !== undefined) {
            date.date = 1;
        } else date.date = now.getDate();
    }

    if (date.era === -1 && date.year > 0) date.year *= -1;

    if (date.hours === undefined) date.hours = 0;

    if (date.k && date.hours === 24) date.hours = 0;
    if (date.h) {
        if (date.amOrPm === 1) date.hours += 12;
        if (date.amOrPm === 0 && date.hours === 12) date.hours = 0;
    }

    const dobj = timeStampObjectToDate(date);

    if (date.offset === undefined) return dobj;

    if (date.offset * -1 === dobj.getTimezoneOffset()) return dobj;

    const has = date.offset * 60 * 1000 * -1;

    return new Date(dobj.getTime() - dobj.getTimezoneOffset() * 60 * 1000 + has);
}

function parseWithRules(
    dateString: string,
    pattern: string,
    date: any,
    rules: {
        key: string;
        values: string[];
        caseInsensitive: boolean;
        notInteger: boolean;
        resultKey: string;
        length?: number;
        hasTh?: boolean;
        range?: [number, number];
        subtract?: number;
        logic?: (num: number) => number;
    }[],
) {
    if (!pattern.length)
        return {
            date,
            pattern,
            dateString,
        };

    const rule = rules.find((e) => pattern.startsWith(e.key));

    if (!rule)
        return {
            date,
            pattern,
            dateString,
        };

    if (rule.key[0] === 'h') date.h = true;

    pattern = pattern.slice(rule.key.length);

    if (rule.notInteger) {
        let x = rule.values.findIndex((e) =>
            rule.caseInsensitive
                ? dateString.toUpperCase().startsWith(e)
                : dateString.startsWith(e),
        );
        if (x === -1)
            return {
                date,
                pattern,
                dateString,
            };

        let value = x;
        if (rule.logic !== undefined) value = rule.logic(x);

        return {
            date: { ...date, [rule.resultKey]: value },
            pattern,
            dateString: dateString.slice(rule.values[x].length),
        };
    }

    let stringValue: string;

    if (rule.length) {
        stringValue = dateString.substring(0, rule.length);
        if (stringValue.length !== rule.length)
            return {
                date,
                pattern,
                dateString: '',
            };
    } else if (rule.hasTh) {
        const match = dateString.match(/^(\d+)(th|st|nd|rd)/i);
        if (!match?.length)
            return {
                date,
                pattern,
                dateString,
            };
        stringValue = match[1];
    } else {
        const match = dateString.match(/^\d+/);
        if (!match?.length)
            return {
                date,
                pattern,
                dateString,
            };
        stringValue = match[0];
    }

    dateString = dateString.slice(stringValue.length);
    let value = parseInt(stringValue);

    if (isNaN(value))
        return {
            date,
            pattern,
            dateString,
        };

    if (rule.subtract !== undefined) value -= rule.subtract;

    if (rule.range !== undefined && (value < rule.range[0] || value > rule.range[1]))
        return {
            date,
            pattern,
            dateString,
        };

    if (rule.logic !== undefined) value = rule.logic(value);
    return {
        date: { ...date, [rule.resultKey]: value },
        pattern,
        dateString,
    };
}

export function dateFromFormatttedString(dateString: string, formatString: string): Date {
    const patterns = patternSplitting(formatString);

    let date: any = {};

    for (let i = 0; i < patterns.length; i++) {
        let pattern = patterns[i].replace(/''/g, "'");

        if (i % 2 === 1) {
            dateString = dateString.slice(pattern.length);
            continue;
        }

        while (pattern.length) {
            const funArray = VALUE_TOKEN_RULES[pattern[0]];    
            if (!funArray) {
                pattern = pattern.slice(1);
                dateString = dateString.slice(1);
                continue;
            }

            ({ date, pattern, dateString } =
                typeof funArray === 'function'
                    ? funArray(dateString, pattern, date)
                    : parseWithRules(dateString, pattern, date, VALUE_TOKEN_RULES[pattern[0]]));
        }
       
    }

    return processParsedDate(date);
}
