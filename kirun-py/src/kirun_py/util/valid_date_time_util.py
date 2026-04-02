import sys


class ValidDateTimeUtil:

    @staticmethod
    def is_leap_year(year: int) -> bool:
        return (year % 4 == 0 and year % 100 != 0) or year % 400 == 0

    @staticmethod
    def valid_year_range(year: int) -> bool:
        return year < 275761 and year > -271821

    @staticmethod
    def valid_month_range(month: int) -> bool:
        return month <= 12 and month > 0

    @staticmethod
    def valid_date(year: int, month: int, date: int) -> bool:
        if date <= 0 or month <= 0:
            return False

        if month == 2:
            return (
                ValidDateTimeUtil.valid_year_range(year)
                and ValidDateTimeUtil.valid_month_range(month)
                and (
                    (ValidDateTimeUtil.is_leap_year(year) and date < 30)
                    or (not ValidDateTimeUtil.is_leap_year(year) and date < 29)
                )
            )
        elif month in (4, 6, 9, 11):
            return (
                ValidDateTimeUtil.valid_year_range(year)
                and ValidDateTimeUtil.valid_month_range(month)
                and date <= 30
            )
        else:
            return (
                ValidDateTimeUtil.valid_year_range(year)
                and ValidDateTimeUtil.valid_month_range(month)
                and date <= 31
            )

    @staticmethod
    def valid_hour_range(hour: int) -> bool:
        return hour < 24 and hour >= 0

    @staticmethod
    def valid_minute_second_range(minute: int) -> bool:
        return minute < 60 and minute >= 0

    @staticmethod
    def valid_milli_second_range(millis: int) -> bool:
        return millis < 1000 and millis >= 0

    @staticmethod
    def validate_with_optional_millis(portion: str) -> bool:
        return (
            ValidDateTimeUtil.valid_milli_second_range(
                ValidDateTimeUtil._convert_to_int(portion[1:4])
            )
            and ValidDateTimeUtil.validate_local_time(portion[4:])
        )

    def get_year(self, date: str) -> int:
        first = date[0]
        if first in ('+', '-'):
            year = ValidDateTimeUtil._convert_to_int(date[1:7])
            return year * -1 if first == '-' else year
        return ValidDateTimeUtil._convert_to_int(date[:4])

    @staticmethod
    def validate_local_time(offset: str) -> bool:
        if not offset:
            return False
        if offset[0] == 'Z':
            return True
        if len(offset) != 6:
            return False
        return (
            (offset[0] == '+' or offset[0] == '-')
            and ValidDateTimeUtil.valid_hour_range(ValidDateTimeUtil._convert_to_int(offset[1:3]))
            and offset[3] == ':'
            and ValidDateTimeUtil.valid_minute_second_range(
                ValidDateTimeUtil._convert_to_int(offset[4:len(offset) - 1])
            )
        )

    @staticmethod
    def validate(date: str) -> bool:
        if len(date) < 20 or len(date) > 32:
            return False

        first = date[0]

        if first in ('+', '-'):
            a = (
                date[7] == '-' and date[10] == '-' and date[13] == 'T'
                and date[16] == ':' and date[19] == ':' and date[22] == '.'
            )
            return (
                a
                and ValidDateTimeUtil.valid_date(
                    ValidDateTimeUtil._convert_to_int(date[1:7]),
                    ValidDateTimeUtil._convert_to_int(date[8:10]),
                    ValidDateTimeUtil._convert_to_int(date[11:13]),
                )
                and ValidDateTimeUtil.valid_hour_range(ValidDateTimeUtil._convert_to_int(date[14:16]))
                and ValidDateTimeUtil.valid_minute_second_range(ValidDateTimeUtil._convert_to_int(date[17:19]))
                and ValidDateTimeUtil.valid_minute_second_range(ValidDateTimeUtil._convert_to_int(date[20:22]))
                and ValidDateTimeUtil.validate_with_optional_millis(date[22:])
            )
        else:
            a = (
                date[4] == '-' and date[7] == '-' and date[10] == 'T'
                and date[13] == ':' and date[16] == ':' and date[19] == '.'
            )
            return (
                a
                and ValidDateTimeUtil.valid_date(
                    ValidDateTimeUtil._convert_to_int(date[:4]),
                    ValidDateTimeUtil._convert_to_int(date[5:7]),
                    ValidDateTimeUtil._convert_to_int(date[8:10]),
                )
                and ValidDateTimeUtil.valid_hour_range(ValidDateTimeUtil._convert_to_int(date[11:13]))
                and ValidDateTimeUtil.valid_minute_second_range(ValidDateTimeUtil._convert_to_int(date[14:16]))
                and ValidDateTimeUtil.valid_minute_second_range(ValidDateTimeUtil._convert_to_int(date[17:19]))
                and ValidDateTimeUtil.validate_with_optional_millis(date[19:])
            )

    @staticmethod
    def _convert_to_int(num: str) -> int:
        try:
            return int(num)
        except (ValueError, TypeError):
            return -sys.maxsize
