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

test('LinkedList Test - peekLastTest', () => {
    let x = new LinkedList();
    x.push(230);
    x.push(231);
    x.push(233);

    expect(x.peekLast()).toBe(230);
});
