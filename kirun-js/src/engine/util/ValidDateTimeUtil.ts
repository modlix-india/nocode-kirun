
export class ValidDateTimeUtil{

   private constructor(){

   }
   public static  isLeapYear( year : number) : boolean{
		return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
	}

    public static  validYearRange( year : number) :boolean {
		return year < 275761 && year > -271821;
	}

	public static validMonthRange( month: number) : boolean{
		return month <= 12 && month > 0;
	}

    public static  validDate( year : number,  month: number,  date: number) : boolean{

		if (date <= 0 || month <= 0)
			return false;

		switch (month) {
		case 2:
			return ValidDateTimeUtil.validYearRange(year) && ValidDateTimeUtil.validMonthRange(month) && (ValidDateTimeUtil.isLeapYear(year) && date < 30)
			        || (!ValidDateTimeUtil.isLeapYear(year) && date < 29);

		case 4:
        case 6:
        case 9:
        case 11:
			return ValidDateTimeUtil.validYearRange(year) && ValidDateTimeUtil.validMonthRange(month) && date <= 30;

		default:
			return ValidDateTimeUtil.validYearRange(year) && ValidDateTimeUtil.validMonthRange(month) && date <= 31;
		}

	}


    public static  validHourRange( hour: number) : boolean{
		return hour < 24 && hour >= 0;
	}

	public static  validMinuteSecondRange( minute : number) : boolean{
		return minute < 60 && minute >= 0;
	}

	public static validMilliSecondRange( millis: number) : boolean {
		return millis < 1000 && millis >= 0;
	}

    public static validateWithOptionalMillis( portion: string): boolean {

		return ValidDateTimeUtil.validMilliSecondRange(ValidDateTimeUtil.convertToInt(portion.substring(1, 4))) && ValidDateTimeUtil.validateLocalTime(portion.substring(4));
	}

    public  getYear( date: string) : number{

		const first: string = date.charAt(0);
		if (first == '+' || first == '-') {
			const year : number = ValidDateTimeUtil.convertToInt(date.substring(1, 7));
			return first == '-' ? year * -1 : year;
		}

		return ValidDateTimeUtil.convertToInt(date.substring(0, 4));
	}

	public static  validateLocalTime( offset: string )  : boolean{

		if (offset.charAt(0) == 'Z')
			return true;

		if (offset.length != 6)
			return false;

		return (offset.charAt(0) == '+' || offset.charAt(0) == '-')
		        && (ValidDateTimeUtil.validHourRange(ValidDateTimeUtil.convertToInt(offset.substring(1, 3))) && offset.charAt(3) == ':'
		                && ValidDateTimeUtil.validMinuteSecondRange(ValidDateTimeUtil.convertToInt(offset.substring(4, offset.length - 1))));
	}

   public static validate( date: string) : boolean {

    if (date.length < 20 || date.length > 32)
        return false; // as date time should have minimum of 20 characters and maximum of 32 characters

    const first: string = date.charAt(0); // checking first index whether '+' or '-' or any digit

    if (first == '+' || first == '-') {

       const  a : boolean = date.charAt(7) == '-' && date.charAt(10) == '-' && date.charAt(13) == 'T'
                && date.charAt(16) == ':' && date.charAt(19) == ':' && date.charAt(22) == '.';

        return a && ValidDateTimeUtil.validDate(ValidDateTimeUtil.convertToInt(date.substring(1, 7)), ValidDateTimeUtil.convertToInt(date.substring(8, 10)),
                ValidDateTimeUtil.convertToInt(date.substring(11, 13))) && ValidDateTimeUtil.validHourRange(ValidDateTimeUtil.convertToInt(date.substring(14, 16)))
                && ValidDateTimeUtil.validMinuteSecondRange(ValidDateTimeUtil.convertToInt(date.substring(17, 19)))
                && ValidDateTimeUtil.validMinuteSecondRange(ValidDateTimeUtil.convertToInt(date.substring(20, 22)))
                && ValidDateTimeUtil.validateWithOptionalMillis(date.substring(22));
    } else {

        const  a : boolean = date.charAt(4) == '-' && date.charAt(7) == '-' && date.charAt(10) == 'T'
                && date.charAt(13) == ':' && date.charAt(16) == ':' && date.charAt(19) == '.';

        return a && ValidDateTimeUtil.validDate(ValidDateTimeUtil.convertToInt(date.substring(0, 4)), ValidDateTimeUtil.convertToInt(date.substring(5, 7)),
                ValidDateTimeUtil.convertToInt(date.substring(8, 10))) && ValidDateTimeUtil.validHourRange(ValidDateTimeUtil.convertToInt(date.substring(11, 13)))
                && ValidDateTimeUtil.validMinuteSecondRange(ValidDateTimeUtil.convertToInt(date.substring(14, 16)))
                && ValidDateTimeUtil.validMinuteSecondRange(ValidDateTimeUtil.convertToInt(date.substring(17, 19)))
                && ValidDateTimeUtil.validateWithOptionalMillis(date.substring(19));
    }

    }

    private static  convertToInt( num : string )  : number{ // adding exception
		var parsed = parseInt(num);		
		return isNaN(parsed) ? Number.MIN_VALUE : parsed;
	}
}

