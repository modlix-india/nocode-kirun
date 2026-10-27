<p align="center">
  <a href="https://kirun.modlix.com">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/modlix-india/nocode-kirun/master/.github/assets/logo-dark.png">
      <img src="https://raw.githubusercontent.com/modlix-india/nocode-kirun/master/.github/assets/logo-light.png" alt="kirun" width="280">
    </picture>
  </a>
</p>

<h3 align="center">kirun-ui: the visual and text editor for KIRun functions</h3>

<p align="center">
  <a href="https://kirun.modlix.com/playground"><strong>See it in the playground</strong></a> ·
  <a href="https://kirun.modlix.com/docs/visual-editor">Docs</a> ·
  <a href="https://github.com/modlix-india/nocode-kirun">GitHub</a>
</p>

<p align="center">
  <a href="https://www.npmjs.com/package/@fincity/kirun-ui"><img src="https://img.shields.io/npm/v/@fincity/kirun-ui?style=flat-square&color=7AA2F7&labelColor=0A0B11" alt="npm"></a>
  <a href="https://github.com/modlix-india/nocode-kirun/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-MIT-9ECE6A?style=flat-square&labelColor=0A0B11" alt="MIT License"></a>
</p>

---

<p align="center">
  <picture>
    <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/modlix-india/nocode-kirun/master/.github/assets/visual-editor-light.png">
    <img src="https://raw.githubusercontent.com/modlix-india/nocode-kirun/master/.github/assets/visual-editor-dark.png" alt="A KIRun function as a graph in the visual editor, with the run's output beside it" width="760">
  </picture>
</p>

A React component library for editing KIRun function definitions: a node graph you can drag steps
around in, a DSL text editor (Monaco) with completions, and a debug view that draws an execution
trace onto the graph. Both modes edit the same definition. The editors on the
[KIRun playground](https://kirun.modlix.com/playground) are this package.

## Install

```bash
npm install @fincity/kirun-ui @fincity/kirun-js monaco-editor react react-dom
```

| Peer dependency | Version |
| --- | --- |
| `@fincity/kirun-js` | `>=3.17.2` |
| `monaco-editor` | `>=0.40.0` |
| `react`, `react-dom` | `>=17.0.0` |

## Use it

```tsx
import { useState } from 'react';
import { KIRunEditor } from '@fincity/kirun-ui';
import { KIRunFunctionRepository, KIRunSchemaRepository } from '@fincity/kirun-js';
import '@fincity/kirun-ui/dist/kirun-ui.css';

function MyEditor() {
    const [definition, setDefinition] = useState(initialDefinition);

    return (
        <KIRunEditor
            functionDefinition={definition}
            onChange={setDefinition}
            functionRepository={new KIRunFunctionRepository()}
            schemaRepository={new KIRunSchemaRepository()}
        />
    );
}
```

Every prop, the editor themes, app themes (the host styling the editor), personalization and the
debug view are in the [visual editor docs](https://kirun.modlix.com/docs/visual-editor).

## Build

```bash
npm install
npm run build
npm test
```

## License

MIT. Built by [Modlix](https://modlix.com).
