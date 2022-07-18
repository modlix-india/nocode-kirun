export class ArrayUtil {
    public static removeAListFrom(source: any[], removeList: any[]): void {
        if (!removeList || !source || !source.length || !removeList.length) return;

        const e: Set<any> = new Set<any>(removeList);

        for (let i = 0; i < source.length; i++) {
            if (!e.has(source[i])) continue;
            source.splice(i, 1);
            i--;
        }
    }
}
