import { Namespaces } from "../../../namespaces/Namespaces";
import { Repository } from "../../../Repository";
import { MapUtil } from "../../../util/MapUtil";
import { AbstractDateFunction } from "./AbstractDateFunction";
import { Function } from '../../Function';


export class DateFunctionRepository implements Repository<Function> {

    private static readonly repoMap: Map<string, Function> = MapUtil.ofArrayEntries(

        AbstractDateFunction.ofEntryDateAndBooleanOutput("IsLeapYear", date => {
           const year = new Date(date).getUTCFullYear();
           return (year % 4 === 0 && year % 100 !== 0) || (year % 400 === 0);
        }),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetDate", date => new Date(date).getUTCDate()),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetDay", date => new Date(date).getUTCDay()),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetFullYear", date => new Date(date).getUTCFullYear()),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetMonth", date => new Date(date).getUTCMonth() + 1),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetHours", date => new Date(date).getUTCHours()),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetMinutes", date => new Date(date).getUTCMinutes()),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetSeconds", date => new Date(date).getUTCSeconds()),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetMilliSeconds", date => new Date(date).getUTCMilliseconds()),

        AbstractDateFunction.ofEntryDateAndIntegerOutput("GetTime" , date => new Date(date).getTime()),
    
    );

    private static readonly filterableNames = Array.from(
        DateFunctionRepository.repoMap.values(),
    ).map((e) => e.getSignature().getFullName());
    
    
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