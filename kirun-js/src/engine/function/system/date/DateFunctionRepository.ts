import { Namespaces } from "../../../namespaces/Namespaces";
import { Repository } from "../../../Repository";
import { MapUtil } from "../../../util/MapUtil";
import { AbstractDateFunction } from "./AbstractDateFunction";
import { Function } from '../../Function';
import { KIRuntimeException } from "../../../exception/KIRuntimeException";
import mapEntry from '../../../util/mapEntry';
import { DateToEpoch } from "./DateToEpoch";
import { DifferenceOfTimestamps } from "./DifferenceOfTimestamps";
import { EpochToDate } from "./EpochToDate";
import { GetCurrentTimeStamp } from "./GetCurrentTimeStamp";
import { GetTimeAsArray } from "./GetTimeAsArray";
import { GetTimeAsObject } from "./GetTimeAsObject";
import { IsValidISODate } from "./IsValidISODate";
import { MaximumTimestamp } from "./MaximumTimestamp";
import { MinimumTimestamp } from "./MinimumTimestamp";


export class DateFunctionRepository implements Repository<Function> {

    private static readonly repoMap: Map<string, Function> = MapUtil.ofArrayEntries(

        mapEntry(new DateToEpoch()),
        mapEntry(new EpochToDate()),
        mapEntry(new GetCurrentTimeStamp()),
        mapEntry(new DifferenceOfTimestamps()),
        mapEntry(new IsValidISODate()),
        mapEntry(new GetTimeAsArray()),
        mapEntry(new GetTimeAsObject()),
        mapEntry(new MaximumTimestamp()),
        mapEntry(new MinimumTimestamp()),

        AbstractDateFunction.ofEntryDateAndBooleanOutput("IsLeapYear", date => {
           const year = new Date(date).getUTCFullYear();
           return (year % 4 === 0 && year % 100 !== 0) || (year % 400 === 0);
        }),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetDate", date => new Date(date).getUTCDate()),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetDay", date => new Date(date).getUTCDay()),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetFullYear", date => new Date(date).getUTCFullYear()),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetMonth", date => new Date(date).getUTCMonth()),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetHours", date => new Date(date).getUTCHours()),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetMinutes", date => new Date(date).getUTCMinutes()),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetSeconds", date => new Date(date).getUTCSeconds()),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetMilliSeconds", date => new Date(date).getUTCMilliseconds()),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetTime" , date => new Date(date).getTime()),

        AbstractDateFunction.ofEntryDateWithIntegerUnitWithOutputName("AddTime", 
            (date , value , unit) => this.changeAmountToUnit(date, unit, value, true)
        ),

        AbstractDateFunction.ofEntryDateWithIntegerUnitWithOutputName("SubtractTime",
            (date , value , unit) => this.changeAmountToUnit(date, unit, value, false)
        )
    );

    private static readonly filterableNames = Array.from(
        DateFunctionRepository.repoMap.values(),
    ).map((e) => e.getSignature().getFullName());

    private static changeAmountToUnit(inputDate: string , unit: string, value: number, isAdd: boolean): string{

        const amount = isAdd ? value : -1 * value;

        const date = this.getFullUTCDate(inputDate);
        const originalOffset = this.getTimezoneOffset(inputDate);
    
        switch(unit){

            case "MILLISECOND" : date.setMilliseconds(date.getMilliseconds() + amount);
                                break;
            case "SECOND" :  date.setSeconds(date.getSeconds() + amount);
                                break;
            case "MINUTE" : date.setMinutes(date.getMinutes() + amount);
                                break;
            case "HOUR" : date.setHours(date.getHours() + amount);
                                break;
            case "DAY" : date.setDate(date.getDate() + amount);
                                break;
            case "MONTH" : date.setMonth(date.getMonth() + amount);
                                break;
            case "YEAR" : date.setFullYear(date.getFullYear() + amount);
                                break;
            default :
                throw new KIRuntimeException("No such unit: " + unit)

        }

        return this.formatDate(date, originalOffset);
    }
    
    private static getTimezoneOffset(dateString: string): string {
        const lastChar = dateString.charAt(dateString.length - 1);
        if (lastChar === 'Z') return '+00:00';
    
        const offsetStart = dateString.indexOf('+') !== -1 ? dateString.lastIndexOf('+') : dateString.lastIndexOf('-');
        if (offsetStart === -1) return '+00:00';
    
        let offset = dateString.substring(offsetStart);
        if (offset.length === 3) {
            offset = offset.substring(0, 1) + '0' + offset.substring(1) + ':00';
        } else if (offset.length === 5) {
            offset = offset.substring(0, 3) + ':' + offset.substring(3);
        }
    
        return offset;
    }

    private static getFullUTCDate(inputDate:string): Date {

        if(inputDate.lastIndexOf('+') !== -1)
            inputDate = inputDate.substring(0, inputDate.lastIndexOf('+')) + 'Z';
        else if(inputDate.lastIndexOf('-') !== -1)
            inputDate = inputDate.substring(0, inputDate.lastIndexOf('-')) + 'Z';

        const date: Date = new Date(inputDate);
    
        return new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth() + 1, 
                        date.getUTCDate(), date.getUTCHours(), date.getUTCMinutes(), 
                        date.getUTCSeconds(), date.getUTCMilliseconds()));
    }

    private static formatDate(date: Date, offset: string): string {
        const pad = (num: number) => num.toString().padStart(2, '0');
        
        const year = date.getUTCMonth() === 0 ? date.getUTCFullYear() - 1 : date.getUTCFullYear();
        const month = pad(date.getUTCMonth() === 0 ? 12 : date.getUTCMonth());
        const day = pad(date.getUTCDate());
        const hours = pad(date.getUTCHours());
        const minutes = pad(date.getUTCMinutes());
        const seconds = pad(date.getUTCSeconds());
        const milliseconds = pad(date.getUTCMilliseconds());
        
        return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}.${milliseconds}${offset}`;
    }
    
    find(namespace: string, name: string): Promise<Function | undefined> {
        if (namespace != Namespaces.DATE) {
            return Promise.resolve(undefined);
        }
        return Promise.resolve(DateFunctionRepository.repoMap.get(name));
    }
    filter(name: string): Promise<string[]> {

        return Promise.resolve(
            DateFunctionRepository.filterableNames.filter(
                (e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1,
            ),
        );
    }
}