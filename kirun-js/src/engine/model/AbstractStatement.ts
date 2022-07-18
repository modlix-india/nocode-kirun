import { Position } from './Position';

export class AbstractStatement {
    private comment: string;
    private description: string;
    private position: Position;
    public getComment(): string {
        return this.comment;
    }
    public setComment(comment: string): AbstractStatement {
        this.comment = comment;
        return this;
    }
    public getDescription(): string {
        return this.description;
    }
    public setDescription(description: string): AbstractStatement {
        this.description = description;
        return this;
    }
    public getPosition(): Position {
        return this.position;
    }
    public setPosition(position: Position): AbstractStatement {
        this.position = position;
        return this;
    }
}
