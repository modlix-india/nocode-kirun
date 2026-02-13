/**
 * This file is a TEMPLATE/EXAMPLE showing how nocode-ui should wrap @fincity/kirun-ui.
 *
 * Place a version of this file at:
 *   nocode-ui/ui-app/client/src/components/KIRunEditor/LazyKIRunEditor.tsx
 *
 * It replaces the current 1629-line LazyKIRunEditor.tsx with a thin wrapper
 * that bridges StoreContext to kirun-ui's controlled component API.
 */

import {
    duplicate,
    ExecutionLog,
    Function,
    FunctionDefinition,
    HybridRepository,
    isNullValue,
    Repository,
    Schema,
    TokenValueExtractor,
} from '@fincity/kirun-js';
import React, { useCallback, useEffect, useMemo, useState } from 'react';

// --- kirun-ui import ---
import { KIRunEditor, PersonalizationData } from '@fincity/kirun-ui';

// --- nocode-ui imports (these stay in nocode-ui) ---
// import { usedComponents } from '../../App/usedComponents';
// import { RemoteRepository, REPO_SERVER } from '../../Engine/RemoteRepository';
// import {
//     addListenerAndCallImmediatelyWithChildrenActivity,
//     getPathFromLocation,
//     PageStoreExtractor,
//     setData,
//     UrlDetailsExtractor,
// } from '../../context/StoreContext';
// import { UIFunctionRepository } from '../../functions';
// import { UISchemaRepository } from '../../schemas/common';
// import { ComponentProps } from '../../types/common';
// import useDefinition from '../util/useDefinition';
// import { propertiesDefinition, stylePropertiesDefinition } from './KIRunEditorProperties';
// import { correctStatementNames, savePersonalizationCurry } from './utils';
// import { runEvent } from '../util/runEvent';

/**
 * Example wrapper — bridges nocode-ui's StoreContext to kirun-ui's props-based API.
 *
 * Key responsibilities:
 * 1. Subscribe to bindingPath for function definition (via addListenerAndCallImmediately)
 * 2. Subscribe to bindingPath2 for personalization preferences
 * 3. Build repositories (HybridRepository / RemoteRepository) based on editorType
 * 4. Map setData calls to KIRunEditor's onChange callback
 * 5. Map personalization writes to onPersonalizationChange callback
 */

// export default function LazyKIRunEditor(
//     props: ComponentProps & {
//         functionRepository?: Repository<Function>;
//         schemaRepository?: Repository<Schema>;
//         tokenValueExtractors?: Map<string, TokenValueExtractor>;
//         stores?: Array<string>;
//         storePaths?: Set<string>;
//         hideArguments?: boolean;
//         functionKey?: string;
//         debugViewMode?: boolean;
//         executionLog?: ExecutionLog;
//         functionDefinition?: FunctionDefinition;
//         onChangePersonalizationFunction?: () => void;
//     },
// ) {
//     const {
//         definition: { bindingPath, bindingPath2 },
//         definition,
//         context,
//         locationHistory,
//         functionRepository: actualFunctionRepository,
//         schemaRepository: actualSchemaRepository,
//         tokenValueExtractors = new Map(),
//         storePaths = new Set(),
//         pageDefinition,
//         debugViewMode = false,
//         executionLog,
//         functionDefinition,
//         onChangePersonalizationFunction,
//     } = props;
//
//     const pageExtractor = PageStoreExtractor.getForContext(context.pageName);
//     const urlExtractor = UrlDetailsExtractor.getForContext(context.pageName);
//     const {
//         key,
//         properties: { readOnly, editorType, onChangePersonalization, clientCode, appCode } = {},
//     } = useDefinition(definition, propertiesDefinition, stylePropertiesDefinition, locationHistory, pageExtractor, urlExtractor);
//
//     const bindingPathPath = bindingPath
//         ? getPathFromLocation(bindingPath!, locationHistory, pageExtractor)
//         : undefined;
//
//     // Subscribe to function definition from store
//     const [rawDef, setRawDef] = useState<any>();
//     useEffect(() => {
//         if (functionDefinition) {
//             setRawDef(functionDefinition);
//             return;
//         }
//         if (!bindingPathPath) return;
//         return addListenerAndCallImmediatelyWithChildrenActivity(
//             pageExtractor.getPageName(),
//             (_, v) => setRawDef(v),
//             bindingPathPath,
//         );
//     }, [bindingPathPath, functionDefinition, pageExtractor]);
//
//     // Subscribe to personalization preferences from store
//     const personalizationPath = bindingPath2
//         ? getPathFromLocation(bindingPath2!, locationHistory, pageExtractor)
//         : undefined;
//     const [personalization, setPersonalization] = useState<PersonalizationData>({});
//     useEffect(() => {
//         if (!personalizationPath) return;
//         return addListenerAndCallImmediatelyWithChildrenActivity(
//             pageExtractor.getPageName(),
//             (_, v) => setPersonalization({ ...(v ?? {}) }),
//             personalizationPath,
//         );
//     }, [personalizationPath, pageExtractor]);
//
//     // Build repositories based on editorType
//     const functionRepository: Repository<Function> = useMemo(() => {
//         if (actualFunctionRepository) return actualFunctionRepository;
//         // ... same HybridRepository/RemoteRepository logic as current LazyKIRunEditor ...
//         return new UIFunctionRepository();
//     }, [actualFunctionRepository, appCode, clientCode, editorType]);
//
//     const schemaRepository: Repository<Schema> = useMemo(() => {
//         if (actualSchemaRepository) return actualSchemaRepository;
//         // ... same HybridRepository/RemoteRepository logic as current LazyKIRunEditor ...
//         return new UISchemaRepository();
//     }, [actualSchemaRepository, appCode, clientCode, editorType]);
//
//     // Handle function definition changes — write back to store
//     const handleChange = useCallback(
//         (def: any) => {
//             if (!bindingPathPath) return;
//             setData(bindingPathPath, def, context.pageName);
//         },
//         [bindingPathPath, context.pageName],
//     );
//
//     // Handle personalization changes — write to store + fire event
//     const handlePersonalizationChange = useCallback(
//         (data: PersonalizationData) => {
//             if (!personalizationPath) return;
//             // Write each changed key
//             for (const [key, value] of Object.entries(data)) {
//                 setData(`${personalizationPath}.${key}`, value, context.pageName);
//             }
//             // Fire the onChangePersonalization event if configured
//             if (onChangePersonalizationFunction) {
//                 onChangePersonalizationFunction();
//             } else if (onChangePersonalization) {
//                 runEvent(onChangePersonalization, key, context.pageName, locationHistory, pageDefinition);
//             }
//         },
//         [personalizationPath, context.pageName, onChangePersonalization, onChangePersonalizationFunction],
//     );
//
//     return (
//         <KIRunEditor
//             functionDefinition={rawDef}
//             onChange={handleChange}
//             functionRepository={functionRepository}
//             schemaRepository={schemaRepository}
//             tokenValueExtractors={tokenValueExtractors}
//             readOnly={readOnly || !bindingPathPath || debugViewMode}
//             functionKey={props.functionKey}
//             stores={props.stores}
//             storePaths={storePaths}
//             hideArguments={props.hideArguments}
//             debugViewMode={debugViewMode}
//             executionLog={executionLog}
//             personalization={personalization}
//             onPersonalizationChange={handlePersonalizationChange}
//         />
//     );
// }
