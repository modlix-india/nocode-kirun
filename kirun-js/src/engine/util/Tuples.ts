export class Tuple2<F, S> {
    protected f: F;
    protected s: S;

    constructor(f: F, s: S) {
        this.f = f;
        this.s = s;
    }

    public getT1(): F {
        return this.f;
    }

    public getT2(): S {
        return this.s;
    }

    public setT1(f: F): Tuple2<F, S> {
        this.f = f;
        return this;
    }

    public setT2(s: S): Tuple2<F, S> {
        this.s = s;
        return this;
    }
}

export class Tuple3<F, S, T> extends Tuple2<F, S> {
    protected t: T;

    constructor(f: F, s: S, t: T) {
        super(f, s);
        this.t = t;
    }

    public getT3(): T {
        return this.t;
    }

    public setT1(f: F): Tuple3<F, S, T> {
        this.f = f;
        return this;
    }

    public setT2(s: S): Tuple3<F, S, T> {
        this.s = s;
        return this;
    }

    public setT3(t: T): Tuple3<F, S, T> {
        this.t = t;
        return this;
    }
}

export class Tuple4<F, S, T, FR> extends Tuple3<F, S, T> {
    protected fr: FR;

    constructor(f: F, s: S, t: T, fr: FR) {
        super(f, s, t);
        this.fr = fr;
    }

    public getT4(): FR {
        return this.fr;
    }

    public setT1(f: F): Tuple4<F, S, T, FR> {
        this.f = f;
        return this;
    }

    public setT2(s: S): Tuple4<F, S, T, FR> {
        this.s = s;
        return this;
    }

    public setT3(t: T): Tuple4<F, S, T, FR> {
        this.t = t;
        return this;
    }

    public setT4(fr: FR): Tuple4<F, S, T, FR> {
        this.fr = fr;
        return this;
    }
}
