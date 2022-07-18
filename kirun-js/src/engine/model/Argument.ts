export class Argument {
    private argumentIndex: number = 0;
    private name: string;
    private value: any;

    public constructor(argumentIndex?: number, name?: string, value?: any) {
        this.argumentIndex = argumentIndex;
        this.name = name;
        this.value = value;
    }
    public getArgumentIndex(): number {
        return this.argumentIndex;
    }
    public setArgumentIndex(argumentIndex: number): Argument {
        this.argumentIndex = argumentIndex;
        return this;
    }
    public getName(): string {
        return this.name;
    }
    public setName(name: string): Argument {
        this.name = name;
        return this;
    }
    public getValue(): any {
        return this.value;
    }
    public setValue(value: any): Argument {
        this.value = value;
        return this;
    }

    public static of(name: string, value: any): Argument {
        return new Argument(0, name, value);
    }
}
