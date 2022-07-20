import { StatementMessageType } from './StatementMessageType';

export class StatementMessage {
    private messageType: StatementMessageType;
    private message: string;

    public constructor(messageType?: StatementMessageType, message?: string) {
        this.message = message;
        this.messageType = messageType;
    }

    public getMessageType(): StatementMessageType {
        return this.messageType;
    }
    public setMessageType(messageType: StatementMessageType): StatementMessage {
        this.messageType = messageType;
        return this;
    }
    public getMessage(): string {
        return this.message;
    }
    public setMessage(message: string): StatementMessage {
        this.message = message;
        return this;
    }

    public toString(): string {
        return `${this.messageType} : ${this.message}`;
    }
}
