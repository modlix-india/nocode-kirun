export class Tuple2<F, S> {
    private f: F;
    private s: S;

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
}

export class Tuple3<F, S, T> extends Tuple2<F, S> {
    private t: T;

    constructor(f: F, s: S, t: T) {
        super(f, s);
        this.t = t;
    }

    public getT3(): T {
        return this.t;
    }
}

export class Tuple4<F, S, T, FR> extends Tuple3<F, S, T> {
    private fr: FR;

    constructor(f: F, s: S, t: T, fr: FR) {
        super(f, s, t);
        this.fr = fr;
    }

    public getT4(): FR {
        return this.fr;
    }
}
