import { LinkedList } from '../../../src/engine/util/LinkedList';

test('LinkedList Test', () => {
    let x: LinkedList<number> = new LinkedList();
    x.push(10);
    x.push(20);
    expect(x.isEmpty()).toBe(false);
    expect(x.size()).toBe(2);
    expect(x.pop()).toBe(20);
    expect(x.isEmpty()).toBe(false);
    expect(x.pop()).toBe(10);
    expect(x.isEmpty()).toBe(true);

    x = new LinkedList();
    x.push(230);
    x.push(231);
    x.push(233);

    expect(x.toArray()).toStrictEqual([233, 231, 230]);

    x = new LinkedList([5, 6, 7]);
    expect(x.toArray()).toStrictEqual([5, 6, 7]);

    x = new LinkedList();
    x.addAll([1, 2, 3]);
    expect(x.toArray()).toStrictEqual([1, 2, 3]);
    x.add(4);
    expect(x.toArray()).toStrictEqual([1, 2, 3, 4]);
});

test('LinkedList Test - set at every index', () => {
    // the walk in set() used to re-read from head each iteration, so any index
    // of 2 or more wrote over element 1 instead of the requested one
    for (let i = 0; i < 5; i++) {
        const x = new LinkedList([10, 20, 30, 40, 50]);
        x.set(i, 999);
        const expected = [10, 20, 30, 40, 50];
        expected[i] = 999;
        expect(x.toArray()).toStrictEqual(expected);
    }
    const y = new LinkedList([1, 2, 3]);
    expect(() => y.set(-1, 0)).toThrow();
    expect(() => y.set(3, 0)).toThrow();
});

test('LinkedList Test - peekLastTest', () => {
    let x = new LinkedList();
    x.push(230);
    x.push(231);
    x.push(233);

    expect(x.peekLast()).toBe(230);
});
