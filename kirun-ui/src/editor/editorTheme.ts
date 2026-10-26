import type * as MonacoEditor from 'monaco-editor';
import { kirunDarkTheme, kirunLightTheme } from '../texteditor/syntax/kirunTheme';

/** Themes the editor ships with. Any other name is an "app theme": the host styles the editor. */
export const BUILT_IN_EDITOR_THEMES = [
    'light',
    'dark',
    'high-contrast',
    'easy-on-eyes',
    'flared-up',
] as const;

export type EditorTheme = (typeof BUILT_IN_EDITOR_THEMES)[number];

export const BUILT_IN_EDITOR_THEME_LABELS: Record<EditorTheme, string> = {
    light: 'Light',
    dark: 'Dark',
    'high-contrast': 'High Contrast',
    'easy-on-eyes': 'Easy on Eyes',
    'flared-up': 'Flared Up',
};

/** Monaco theme name used for every app theme. */
export const APP_MONACO_THEME = 'kirun-app';

export function isBuiltInEditorTheme(theme: string | undefined): theme is EditorTheme {
    return !!theme && (BUILT_IN_EDITOR_THEMES as readonly string[]).includes(theme);
}

/**
 * The theme the editor uses.
 *
 * With the selector shown, the user's saved choice wins over `defaultTheme`. With the
 * selector hidden the host decides alone, so a saved choice is ignored.
 */
export function resolveEditorTheme(
    showThemeSelector: boolean | undefined,
    defaultTheme: string | undefined,
    savedTheme: string | undefined,
): string {
    const fallback = defaultTheme?.trim() || 'light';
    if (showThemeSelector === false) return fallback;
    return savedTheme?.trim() || fallback;
}

/**
 * Class names for the editor root. A theme outside the built-in list gets `_theme-app`, the hook
 * a host uses to set the `--kirun-*` variables (see KIRunEditorThemes.css).
 */
export function editorThemeClassName(theme: string): string {
    const safe = theme.replace(/[^a-zA-Z0-9_-]/g, '-');
    return isBuiltInEditorTheme(theme) ? `_theme-${safe}` : `_theme-app _theme-${safe}`;
}

/** Label for an app theme in the selector. */
export function appThemeLabel(theme: string): string {
    return theme === 'app' ? 'App' : theme;
}

/**
 * Reads one CSS colour variable and returns it as a Monaco colour (`RRGGBB` or `RRGGBBAA`, no #),
 * or undefined when the variable is not set.
 */
export type ColorReader = (variable: string) => string | undefined;

/** Monaco token -> the CSS variable that colours it. Unset variables keep the base theme's colour. */
const TOKEN_VARIABLES: Array<[string, string]> = [
    ['keyword', '--kirun-code-keyword'],
    ['keyword.control', '--kirun-code-control'],
    ['type', '--kirun-code-type'],
    ['type.identifier', '--kirun-code-type'],
    ['entity.name.function', '--kirun-code-function'],
    ['entity.name.tag', '--kirun-code-tag'],
    ['attribute.name', '--kirun-code-variable'],
    ['variable.value', '--kirun-code-value'],
    ['variable.parameter', '--kirun-code-variable'],
    ['variable.predefined', '--kirun-code-predefined'],
    ['variable', '--kirun-code-variable'],
    ['identifier', '--kirun-code-fg'],
    ['number', '--kirun-code-number'],
    ['number.float', '--kirun-code-number'],
    ['string.value', '--kirun-code-string'],
    ['string.expression', '--kirun-code-expression'],
    ['string', '--kirun-code-string'],
    ['string.escape', '--kirun-code-value'],
    ['string.invalid', '--kirun-code-error'],
    ['comment', '--kirun-code-comment'],
    ['operator', '--kirun-code-fg'],
    ['delimiter', '--kirun-code-fg'],
    ['delimiter.bracket.embed', '--kirun-code-value'],
];

function luminance(hex: string): number {
    const [r, g, b] = [0, 2, 4].map((i) => {
        const c = parseInt(hex.substring(i, i + 2), 16) / 255;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    });
    return 0.2126 * r + 0.7152 * g + 0.0722 * b;
}

/**
 * Builds the Monaco theme for an app theme from the host's CSS variables. It starts from the
 * built-in light or dark theme, chosen by how dark the code background is, so a host only has to
 * set the colours it cares about.
 */
export function buildAppMonacoTheme(read: ColorReader): MonacoEditor.editor.IStandaloneThemeData {
    const background =
        read('--kirun-code-bg') ?? read('--kirun-bg-secondary') ?? read('--kirun-bg-primary');
    const dark = background ? luminance(background.substring(0, 6)) < 0.25 : false;
    const base = dark ? kirunDarkTheme : kirunLightTheme;

    const rules = base.rules.map((rule) => {
        const variable = TOKEN_VARIABLES.find(([token]) => token === rule.token)?.[1];
        const colour = variable ? read(variable) : undefined;
        return colour ? { ...rule, foreground: colour.substring(0, 6) } : { ...rule };
    });

    const colors: Record<string, string> = { ...base.colors };
    const setColor = (key: string, ...variables: string[]) => {
        for (const v of variables) {
            const c = read(v);
            if (c) {
                colors[key] = `#${c}`;
                return;
            }
        }
    };
    if (background) colors['editor.background'] = `#${background}`;
    setColor('editor.foreground', '--kirun-code-fg', '--kirun-text-primary');
    setColor('editor.lineHighlightBackground', '--kirun-code-line-highlight', '--kirun-bg-tertiary');
    setColor('editorCursor.foreground', '--kirun-code-cursor', '--kirun-accent');
    setColor('editor.selectionBackground', '--kirun-code-selection');
    setColor('editorLineNumber.foreground', '--kirun-code-comment', '--kirun-text-secondary');
    setColor('editorGutter.background', '--kirun-code-bg', '--kirun-bg-secondary');

    return { base: dark ? 'vs-dark' : 'vs', inherit: true, rules, colors };
}

/** `rgb(1, 2, 3)`, `rgba(1, 2, 3, 0.5)` or `color(srgb 0.1 0.2 0.3 / 0.5)` -> `010203` / `01020380`. */
export function cssColorToMonaco(value: string): string | undefined {
    const v = value.trim();
    if (!v || v === 'transparent') return undefined;
    const hex = (n: number) =>
        Math.max(0, Math.min(255, Math.round(n)))
            .toString(16)
            .padStart(2, '0');
    let m = /^rgba?\(\s*([\d.]+)[\s,]+([\d.]+)[\s,]+([\d.]+)(?:\s*[,/]\s*([\d.]+%?))?\s*\)$/i.exec(v);
    let r: number, g: number, b: number, a: number;
    if (m) {
        [r, g, b] = [m[1], m[2], m[3]].map(Number);
        a = alpha(m[4]);
    } else {
        m = /^color\(\s*srgb\s+([\d.]+)\s+([\d.]+)\s+([\d.]+)(?:\s*\/\s*([\d.]+%?))?\s*\)$/i.exec(v);
        if (!m) {
            const h = /^#([0-9a-f]{3,8})$/i.exec(v)?.[1];
            if (!h) return undefined;
            if (h.length === 3 || h.length === 4)
                return h
                    .split('')
                    .map((c) => c + c)
                    .join('')
                    .toUpperCase();
            return h.toUpperCase();
        }
        [r, g, b] = [m[1], m[2], m[3]].map((x) => Number(x) * 255);
        a = alpha(m[4]);
    }
    if (a <= 0) return undefined;
    return (hex(r) + hex(g) + hex(b) + (a < 1 ? hex(a * 255) : '')).toUpperCase();
}

function alpha(a: string | undefined): number {
    if (a === undefined) return 1;
    return a.endsWith('%') ? Number(a.slice(0, -1)) / 100 : Number(a);
}

/**
 * A ColorReader over the live DOM. Custom properties come back unresolved from
 * getComputedStyle, so each one is resolved through a hidden probe's `color`.
 */
export function domColorReader(root: HTMLElement): { read: ColorReader; dispose: () => void } {
    const probe = document.createElement('span');
    probe.style.display = 'none';
    root.appendChild(probe);
    const rootStyle = getComputedStyle(root);
    return {
        read: (variable) => {
            if (!rootStyle.getPropertyValue(variable).trim()) return undefined;
            probe.style.color = `var(${variable})`;
            return cssColorToMonaco(getComputedStyle(probe).color);
        },
        dispose: () => probe.remove(),
    };
}

/** The host's code font, when it sets `--kirun-code-font-family`. */
export function readCodeFont(root: HTMLElement): string | undefined {
    return getComputedStyle(root).getPropertyValue('--kirun-code-font-family').trim() || undefined;
}

/**
 * Calls `onChange` (debounced) when the page's styles may have changed: a <style> element added,
 * removed or rewritten anywhere, or a class / data-theme / data-mlx-theme (Modlix's applied theme) /
 * style change on <html> or <body>.
 * That is how a host's light/dark switch reaches Monaco, which cannot follow CSS variables itself.
 */
export function watchHostStyles(onChange: () => void, delay = 120): () => void {
    if (typeof MutationObserver === 'undefined' || typeof document === 'undefined') return () => {};
    let timer: ReturnType<typeof setTimeout> | undefined;
    const fire = () => {
        clearTimeout(timer);
        timer = setTimeout(onChange, delay);
    };
    const isStyle = (n: Node | null) => !!n && (n.nodeName === 'STYLE' || n.nodeName === 'LINK');
    const styleObserver = new MutationObserver((mutations) => {
        for (const m of mutations) {
            if (
                isStyle(m.target) ||
                isStyle(m.target.parentNode) ||
                Array.from(m.addedNodes).some(isStyle) ||
                Array.from(m.removedNodes).some(isStyle)
            ) {
                fire();
                return;
            }
        }
    });
    styleObserver.observe(document.documentElement, {
        childList: true,
        subtree: true,
        characterData: true,
    });
    const attrObserver = new MutationObserver(fire);
    const attributeFilter = ['class', 'style', 'data-theme', 'data-mlx-theme'];
    attrObserver.observe(document.documentElement, { attributes: true, attributeFilter });
    if (document.body) attrObserver.observe(document.body, { attributes: true, attributeFilter });
    return () => {
        clearTimeout(timer);
        styleObserver.disconnect();
        attrObserver.disconnect();
    };
}
