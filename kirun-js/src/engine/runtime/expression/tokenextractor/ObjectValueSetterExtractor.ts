import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { isNullValue } from '../../../util/NullCheck';
import { duplicate } from '../../../util/duplicate';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { Expression } from '../Expression';
import { ExpressionTokenValue } from '../ExpressionTokenValue';
import { Operation } from '../Operation';
import { TokenValueExtractor } from './TokenValueExtractor';

export class ObjectValueSetterExtractor extends TokenValueExtractor {
    private store: any;
    private prefix: string;
    constructor(store: any, prefix: string) {
        super();
        this.store = store;
        this.prefix = prefix;
    }
    protected getValueInternal(token: string) {
        let parts: string[] = token.split(TokenValueExtractor.REGEX_DOT);
        return this.retrieveElementFrom(token, parts, 1, this.store);
    }

    public getStore(): any {
        return this.store;
    }
    public setStore(store: any): ObjectValueSetterExtractor {
        this.store = store;
        return this;
    }

    public setValue(
        token: string,
        value: any,
        overwrite: boolean = true,
        deleteOnNull: boolean = false,
    ) {
        this.store = duplicate(this.store);
        this.modifyStore(token, value, overwrite, deleteOnNull);
    }

    private modifyStore(
        stringToken: string,
        value: any,
        overwrite: boolean,
        deleteOnNull: boolean,
    ) {
        const exp = new Expression(stringToken);
        const tokens = exp.getTokens();
        tokens.removeLast();
        const ops = exp.getOperations();

        let op = ops.removeLast();
        let token = tokens.removeLast();
        let mem =
            token instanceof ExpressionTokenValue
                ? (token as ExpressionTokenValue).getElement()
                : token.getExpression();

        let el = this.store;

        while (!ops.isEmpty()) {
            if (op == Operation.OBJECT_OPERATOR) {
                el = this.getDataFromObject(el, mem, ops.peekLast());
            } else {
                el = this.getDataFromArray(el, mem, ops.peekLast());
            }

            op = ops.removeLast();
            token = tokens.removeLast();
            mem =
                token instanceof ExpressionTokenValue
                    ? (token as ExpressionTokenValue).getElement()
                    : token.getExpression();
        }

        if (op == Operation.OBJECT_OPERATOR)
            this.putDataInObject(el, mem, value, overwrite, deleteOnNull);
        else this.putDataInArray(el, mem, value, overwrite, deleteOnNull);
    }

    private getDataFromArray(el: any, mem: string, nextOp: Operation): any {
        if (!Array.isArray(el))
            throw new KIRuntimeException(
                StringFormatter.format('Expected an array but found $', el),
            );

        const index = parseInt(mem);
        if (isNaN(index))
            throw new KIRuntimeException(
                StringFormatter.format('Expected an array index but found $', mem),
            );
        if (index < 0)
            throw new KIRuntimeException(
                StringFormatter.format('Array index is out of bound - $', mem),
            );

        let je = el[index];

        if (isNullValue(je)) {
            je = nextOp == Operation.OBJECT_OPERATOR ? {} : [];
            el[index] = je;
        }
        return je;
    }

    private getDataFromObject(el: any, mem: string, nextOp: Operation): any {
        if (Array.isArray(el) || typeof el !== 'object')
            throw new KIRuntimeException(
                StringFormatter.format('Expected an object but found $', el),
            );

        let je = el[mem];

        if (isNullValue(je)) {
            je = nextOp == Operation.OBJECT_OPERATOR ? {} : [];
            el[mem] = je;
        }
        return je;
    }

    private putDataInArray(
        el: any,
        mem: string,
        value: any,
        overwrite: boolean,
        deleteOnNull: boolean,
    ): void {
        if (!Array.isArray(el))
            throw new KIRuntimeException(
                StringFormatter.format('Expected an array but found $', el),
            );

        const index = parseInt(mem);
        if (isNaN(index))
            throw new KIRuntimeException(
                StringFormatter.format('Expected an array index but found $', mem),
            );
        if (index < 0)
            throw new KIRuntimeException(
                StringFormatter.format('Array index is out of bound - $', mem),
            );

        if (overwrite || isNullValue(el[index])) {
            if (deleteOnNull && isNullValue(value)) el.splice(index, 1);
            else el[index] = value;
        }
    }

    private putDataInObject(
        el: any,
        mem: string,
        value: any,
        overwrite: boolean,
        deleteOnNull: boolean,
    ): void {
        if (Array.isArray(el) || typeof el !== 'object')
            throw new KIRuntimeException(
                StringFormatter.format('Expected an object but found $', el),
            );

        if (overwrite || isNullValue(el[mem])) {
            if (deleteOnNull && isNullValue(value)) delete el[mem];
            else el[mem] = value;
        }
    }

    getPrefix(): string {
        return this.prefix;
    }
}
