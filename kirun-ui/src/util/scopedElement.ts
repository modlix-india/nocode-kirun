/**
 * Finds an element by id inside one editor. Step, port and store node ids are built from step
 * names, so two editors showing the same function on one page carry the same ids, and
 * document.getElementById would hand every editor the first editor's nodes: its lines were then
 * drawn from the other editor's positions. Searching from the editor's own root keeps each editor
 * to its own nodes. Without a root it falls back to the whole document.
 */
export function elementById(scope: Element | null | undefined, id: string): HTMLElement | null {
    if (!scope) return document.getElementById(id);
    const root = scope.closest('._kirun-editor') ?? scope;
    return root.querySelector<HTMLElement>(`[id="${id.replace(/["\\]/g, '\\$&')}"]`);
}
