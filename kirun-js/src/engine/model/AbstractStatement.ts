import { Position } from './Position';

export class AbstractStatement {
    private comment?: string;
    private description?: string;
    private position?: Position;
    private override: boolean = false;

    public constructor(ast?: AbstractStatement) {
        if (!ast) return;
        this.comment = ast.comment;
        this.description = ast.description;
        this.position = ast.position
            ? new Position(ast.position.getLeft(), ast.position.getTop())
            : undefined;
        this.override = ast.override;
    }

    public getComment(): string | undefined {
        return this.comment;
    }
    public setComment(comment: string): AbstractStatement {
        this.comment = comment;
        return this;
    }
    public isOverride(): boolean {
        return this.override;
    }

    public setOverride(override: boolean): AbstractStatement {
        this.override = override;
        return this;
    }

    public getDescription(): string | undefined {
        return this.description;
    }
    public setDescription(description: string): AbstractStatement {
        this.description = description;
        return this;
    }
    public getPosition(): Position | undefined {
        return this.position;
    }
    public setPosition(position: Position | undefined): AbstractStatement {
        this.position = position;
        return this;
    }
}
