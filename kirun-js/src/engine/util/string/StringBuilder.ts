import { KIRunConstants } from '../../constant/KIRunConstants';
import { KIRuntimeException } from '../../exception/KIRuntimeException';

export class StringBuilder {
    private str: string;

    public constructor(str?: string) {
        this.str = str ?? '';
    }

    public append(x: any): StringBuilder {
        this.str += x;
        return this;
    }

    public toString(): string {
        return '' + this.str;
    }

    public trim(): StringBuilder {
        this.str = this.str.trim();
        return this;
    }

    public setLength(num: number): StringBuilder {
        this.str = this.str.substring(0, num);
        return this;
    }

    public length(): number {
        return this.str.length;
    }

    public charAt(index: number): string {
        return this.str.charAt(index);
    }

    public deleteCharAt(index: number): StringBuilder {
        this.checkIndex(index);
        this.str = this.str.substring(0, index) + this.str.substring(index + 1);
        return this;
    }

    public insert(index: number, str: string): StringBuilder {
        this.str = this.str.substring(0, index) + str + this.str.substring(index);
        return this;
    }

    private checkIndex(index: number): void {
        if (index >= this.str.length)
            throw new KIRuntimeException(
                `Index ${index} is greater than or equal to ${this.str.length}`,
            );
    }

    public substring(start: number, end: number): string {
        return this.str.substring(start, end);
    }
}
