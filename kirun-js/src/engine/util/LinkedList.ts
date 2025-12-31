import { KIRuntimeException } from '../exception/KIRuntimeException';
import { deepEqual } from './deepEqual';
import { StringFormatter } from './string/StringFormatter';

export class LinkedList<T> {
    private head?: Node<T> = undefined;
    private tail?: Node<T> = undefined;
    public length: number = 0;

    public constructor(list?: T[]) {
        if (list?.length) {
            for (const t of list) {
                if (!this.head) {
                    this.tail = this.head = new Node(t);
                } else {
                    const node = new Node(t, this.tail);
                    this.tail!.next = node;
                    this.tail = node;
                }
            }
            this.length = list.length;
        }
    }

    public push(value: T) {
        const node = new Node(value, undefined, this.head);
        if (!this.head) {
            this.tail = this.head = node;
        } else {
            this.head.previous = node;
            this.head = node;
        }
        this.length++;
    }

    public pop(): T {
        if (!this.head) {
            throw Error('List is empty and cannot pop further.');
        }
        const value: T = this.head!.value;
        this.length--;

        if (this.head == this.tail) {
            this.head = this.tail = undefined;
            return value;
        }

        const node: Node<T> = this.head!;

        this.head = node.next;
        node.next = undefined;
        node.previous = undefined;
        this.head!.previous = undefined;
        return value;
    }

    public isEmpty(): boolean {
        return !this.length;
    }

    public size(): number {
        return this.length;
    }

    public get(index: number): T {
        if (index < 0 || index >= this.length) {
            throw new Error(`${index} is out of bounds [0,${this.length}]`);
        }

        let x = this.head;
        while (index > 0) {
            x = this.head!.next;
            --index;
        }

        return x!.value;
    }

    public set(index: number, value: T): LinkedList<T> {
        if (index < 0 || index >= this.length)
            throw new KIRuntimeException(
                StringFormatter.format(
                    'Index $ out of bound to set the value in linked list.',
                    index,
                ),
            );

        let x = this.head;
        while (index > 0) {
            x = this.head!.next;
            --index;
        }
        x!.value = value;
        return this;
    }

    public toString(): string {
        let x = this.head;
        let str: string = '';

        while (x) {
            str += x.value;
            x = x.next;
            if (x) str += ', ';
        }

        return `[${str}]`;
    }

    public toArray(): T[] {
        let arr: T[] = [];

        let x = this.head;

        while (x) {
            arr.push(x.value);
            x = x.next;
        }

        return arr;
    }

    public clone(): LinkedList<T> {
        return new LinkedList(this.toArray());
    }

    public peek(): T {
        if (!this.head) {
            throw new Error('List is empty so cannot peak');
        }

        return this.head.value;
    }

    public peekLast(): T {
        if (!this.tail) {
            throw new Error('List is empty so cannot peak');
        }
        return this.tail.value;
    }

    public getFirst(): T {
        if (!this.head) {
            throw new Error('List is empty so cannot get first');
        }
        return this.head.value;
    }

    public removeFirst(): T {
        return this.pop();
    }

    public removeLast(): T {
        if (!this.tail) {
            throw new Error('List is empty so cannot remove');
        }
        --this.length;
        const v: T = this.tail.value;
        if (this.length == 0) {
            this.head = this.tail = undefined;
        } else {
            const n = this.tail.previous;
            n!.next = undefined;
            this.tail.previous = undefined;
            this.tail = n;
        }

        return v;
    }

    public addAll(list: T[]): LinkedList<T> {
        if (!list || !list.length) return this;
        list.forEach(this.add.bind(this));
        return this;
    }

    public add(t: T): LinkedList<T> {
        ++this.length;
        if (!this.tail && !this.head) {
            this.head = this.tail = new Node(t);
        } else if (this.head === this.tail) {
            this.tail = new Node(t, this.head);
            this.head!.next = this.tail;
        } else {
            this.tail = new Node(t, this.tail);
            this.tail.previous!.next = this.tail;
        }
        return this;
    }

    public map<U>(callbackfn: (value: T, index: number) => U, thisArg?: any): LinkedList<U> {
        let newList: LinkedList<U> = new LinkedList();

        let x = this.head;

        let index: number = 0;
        while (x) {
            newList.add(callbackfn(x.value, index));
            x = x.next;
            ++index;
        }

        return newList;
    }

    public indexOf(value: T): number {
        let x = this.head;

        let index: number = 0;
        while (x) {
            if (deepEqual(x.value, value)) return index;
            x = x.next;
            ++index;
        }

        return -1;
    }

    public forEach(callbackfn: (value: T, index: number) => void, thisArg?: any): void {
        let x = this.head;
        let index: number = 0;
        while (x) {
            callbackfn(x.value, index);
            x = x.next;
            ++index;
        }
    }
}

class Node<T> {
    public value: T;
    public next?: Node<T>;
    public previous?: Node<T>;

    constructor(t: T, previous?: Node<T>, next?: Node<T>) {
        this.value = t;
        this.next = next;
        this.previous = previous;
    }

    public toString(): string {
        return '' + this.value;
    }
}
