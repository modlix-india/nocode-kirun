import {
    APP_MONACO_THEME,
    BUILT_IN_EDITOR_THEMES,
    buildAppMonacoTheme,
    cssColorToMonaco,
    editorThemeClassName,
    isBuiltInEditorTheme,
    resolveEditorTheme,
} from '../src/editor/editorTheme';
import { kirunDarkTheme, kirunLightTheme } from '../src/texteditor/syntax/kirunTheme';

/**
 * A host can hand the editor a theme name outside the built-in list ("app", "kirunDark", ...).
 * The editor then takes its look from the host's --kirun-* CSS variables instead of its own
 * theme classes, and builds a Monaco theme from the same variables.
 */
describe('resolveEditorTheme', () => {
    test('selector shown: the saved choice wins over the default', () => {
        expect(resolveEditorTheme(true, 'app', 'dark')).toBe('dark');
        expect(resolveEditorTheme(undefined, 'app', 'dark')).toBe('dark');
    });

    test('selector shown, nothing saved: the default applies', () => {
        expect(resolveEditorTheme(true, 'app', undefined)).toBe('app');
        expect(resolveEditorTheme(true, 'flared-up', '')).toBe('flared-up');
    });

    test('selector hidden: the saved choice is ignored', () => {
        expect(resolveEditorTheme(false, 'app', 'dark')).toBe('app');
        expect(resolveEditorTheme(false, undefined, 'dark')).toBe('light');
    });

    test('nothing set at all: light, as before', () => {
        expect(resolveEditorTheme(undefined, undefined, undefined)).toBe('light');
        expect(resolveEditorTheme(true, '  ', undefined)).toBe('light');
    });
});

describe('theme class names', () => {
    test('built-in themes keep their single class', () => {
        for (const t of BUILT_IN_EDITOR_THEMES) {
            expect(isBuiltInEditorTheme(t)).toBe(true);
            expect(editorThemeClassName(t)).toBe(`_theme-${t}`);
        }
    });

    test('any other name is an app theme', () => {
        expect(isBuiltInEditorTheme('app')).toBe(false);
        expect(isBuiltInEditorTheme(undefined)).toBe(false);
        expect(editorThemeClassName('app')).toBe('_theme-app _theme-app');
        expect(editorThemeClassName('kirun Dark')).toBe('_theme-app _theme-kirun-Dark');
    });
});

describe('cssColorToMonaco', () => {
    test('reads the forms getComputedStyle returns', () => {
        expect(cssColorToMonaco('rgb(122, 162, 247)')).toBe('7AA2F7');
        expect(cssColorToMonaco('rgba(251, 191, 36, 0.5)')).toBe('FBBF2480');
        expect(cssColorToMonaco('rgb(6 7 11 / 50%)')).toBe('06070B80');
        expect(cssColorToMonaco('color(srgb 1 0 0.5)')).toBe('FF0080');
        expect(cssColorToMonaco('#abc')).toBe('AABBCC');
        expect(cssColorToMonaco('#7aa2f7')).toBe('7AA2F7');
    });

    test('nothing to paint is undefined', () => {
        expect(cssColorToMonaco('')).toBeUndefined();
        expect(cssColorToMonaco('transparent')).toBeUndefined();
        expect(cssColorToMonaco('rgba(0, 0, 0, 0)')).toBeUndefined();
        expect(cssColorToMonaco('papayawhip')).toBeUndefined();
    });
});

describe('buildAppMonacoTheme', () => {
    const reader = (vars: Record<string, string>) => (v: string) => vars[v];

    test('no variables: the light theme, unchanged', () => {
        const theme = buildAppMonacoTheme(reader({}));
        expect(theme.base).toBe('vs');
        expect(theme.rules).toEqual(kirunLightTheme.rules);
        expect(theme.colors).toEqual(kirunLightTheme.colors);
    });

    test('a dark code background starts from the dark theme', () => {
        const theme = buildAppMonacoTheme(reader({ '--kirun-code-bg': '0E111A' }));
        expect(theme.base).toBe('vs-dark');
        expect(theme.colors['editor.background']).toBe('#0E111A');
        const keyword = theme.rules.find((r) => r.token === 'keyword');
        expect(keyword?.foreground).toBe(
            kirunDarkTheme.rules.find((r) => r.token === 'keyword')?.foreground,
        );
    });

    test('code variables colour their tokens; alpha is dropped for tokens', () => {
        const theme = buildAppMonacoTheme(
            reader({
                '--kirun-code-bg': '0E111A',
                '--kirun-code-keyword': 'BB9AF7',
                '--kirun-code-string': '9ECE6A80',
                '--kirun-code-function': '7AA2F7',
                '--kirun-code-cursor': 'FBBF24',
            }),
        );
        const fg = (token: string) => theme.rules.find((r) => r.token === token)?.foreground;
        expect(fg('keyword')).toBe('BB9AF7');
        expect(fg('string')).toBe('9ECE6A');
        expect(fg('string.value')).toBe('9ECE6A');
        expect(fg('entity.name.function')).toBe('7AA2F7');
        expect(theme.colors['editorCursor.foreground']).toBe('#FBBF24');
    });

    test('falls back to the core palette when code variables are missing', () => {
        const theme = buildAppMonacoTheme(
            reader({
                '--kirun-bg-secondary': 'F4F5F8',
                '--kirun-text-primary': '0B0D14',
                '--kirun-accent': 'B45309',
            }),
        );
        expect(theme.base).toBe('vs');
        expect(theme.colors['editor.background']).toBe('#F4F5F8');
        expect(theme.colors['editor.foreground']).toBe('#0B0D14');
        expect(theme.colors['editorCursor.foreground']).toBe('#B45309');
    });

    test('the built-in themes are never mutated', () => {
        const before = JSON.stringify(kirunLightTheme);
        buildAppMonacoTheme(reader({ '--kirun-code-keyword': '000000' }));
        expect(JSON.stringify(kirunLightTheme)).toBe(before);
        expect(APP_MONACO_THEME).toBe('kirun-app');
    });
});
