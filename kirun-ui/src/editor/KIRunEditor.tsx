import {
    duplicate,
    ExecutionGraph,
    ExecutionLog,
    Function,
    FunctionDefinition,
    FunctionExecutionParameters,
    isNullValue,
    KIRuntime,
    LinkedList,
    LogEntry,
    Repository,
    Schema,
    StatementExecution,
    TokenValueExtractor,
    DSLCompiler,
} from '@fincity/kirun-js';
import React, {
    CSSProperties,
    ReactNode,
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from 'react';
import { toUIError, UIError } from '../util/errorHandling';
import { generateColor } from '../colors';
import ExecutionGraphLines from '../components/ExecutionGraphLines';
import FunctionDetailsEditor from '../components/FunctionDetailsEditor';
import KIRunContextMenu from '../components/KIRunContextMenu';
import Search from '../components/Search';
import StatementNode from '../components/StatementNode';
import StatementParameters from '../components/StatementParameters';
import { StoreNode } from '../components/StoreNode';
import { autoLayoutFunctionDefinition } from '../util/autoLayout';
import { correctStatementNames } from '../util/stringValue';
import { COPY_STMT_KEY } from '../constants';
import { KIRunTextEditor, EditorTheme } from '../texteditor/KIRunTextEditor';
import { DSLHelpWindow } from '../texteditor/DSLHelpWindow';
import { KIRunEditorProps, PersonalizationData } from '../types';
import {
    BoxSelect,
    SquarePlus,
    Trash2,
    Pencil,
    Workflow,
    Variable,
    MessageCircle,
    Database,
    ArrowLeft,
    CircleHelp,
    WrapText,
    AlignLeft,
    Search as SearchIcon,
    Replace,
    ZoomIn,
    ZoomOut,
    Code,
} from 'lucide-react';

const gridSize = 20;

function makePositions(
    executionPlan: ExecutionGraph<string, StatementExecution> | UIError | undefined,
    setPositions: (p: Map<string, { left: number; top: number }>) => void,
) {
    if (!executionPlan) return;
    if ('message' in executionPlan) return;

    const positions: Map<string, { left: number; top: number }> = new Map();
    const list = new LinkedList(executionPlan.getVerticesWithNoIncomingEdges());
    const finishedSet = new Set<string>();
    let firstLeft = 20;
    let firstTop = 20;
    while (!list.isEmpty()) {
        const v = list.removeFirst();
        const s = v.getData().getStatement();
        finishedSet.add(s.getStatementName());
        if (v.getOutVertices().size) {
            list.addAll(
                Array.from(v.getOutVertices().values())
                    .flatMap((e) => Array.from(e))
                    .filter(
                        (e) => !finishedSet.has(e.getData().getStatement().getStatementName()),
                    ),
            );
        }

        if (
            isNullValue(s.getPosition()) ||
            ((s.getPosition()?.getLeft() ?? 0) <= 0 && (s.getPosition()?.getTop() ?? 0) <= 0)
        ) {
            if (!v.getInVertices() || !v.getInVertices().size) {
                positions.set(s.getStatementName(), { left: firstLeft, top: firstTop });
                firstTop += 100;
            }
        }
    }

    setPositions(positions);
}

function makeUpdates(
    inFunDef: FunctionDefinition | undefined,
    setExecutionPlan: (
        p: ExecutionGraph<string, StatementExecution> | UIError | undefined,
    ) => void,
    setKirunMessages: (p: Map<string, string[]>) => void,
    functionRepository: Repository<Function>,
    schemaRepository: Repository<Schema>,
    key: string,
    tokenValueExtractors: Map<string, TokenValueExtractor>,
    setPositions: (p: Map<string, { left: number; top: number }>) => void,
) {
    if (isNullValue(inFunDef)) {
        setExecutionPlan(undefined);
        return;
    }

    (async () => {
        const fep = new FunctionExecutionParameters(functionRepository, schemaRepository, key);

        if (tokenValueExtractors.size) {
            fep.setValuesMap(tokenValueExtractors);
        }

        try {
            const ep = await new KIRuntime(inFunDef!, true).getExecutionPlan(
                functionRepository,
                schemaRepository,
            );
            setExecutionPlan(ep);
            makePositions(ep, setPositions);

            const map = new Map();
            Array.from(ep.getNodeMap().values()).forEach((e) => {
                map.set(
                    e.getData().getStatement().getStatementName(),
                    e
                        .getData()
                        .getMessages()
                        .map((m) => m.getMessage()),
                );
            });

            setKirunMessages(map);
        } catch (err) {
            setExecutionPlan(toUIError(err));
        }
    })();
}

function buildDebugInfoMap(
    executionLog: ExecutionLog | undefined,
    currentFunctionName: string | undefined,
): Map<string, LogEntry[]> {
    const map = new Map<string, LogEntry[]>();
    if (!executionLog || !currentFunctionName) return map;

    const flattenLogs = (logs: LogEntry[]): LogEntry[] => {
        const result: LogEntry[] = [];
        for (const log of logs) {
            result.push(log);
            if (log.children?.length) {
                result.push(...flattenLogs(log.children));
            }
        }
        return result;
    };

    const allLogs = flattenLogs(executionLog.logs);
    for (const log of allLogs) {
        if (!log.statementName) continue;
        if (log.kirunFunctionName !== currentFunctionName) continue;
        const existing = map.get(log.statementName) || [];
        existing.push(log);
        map.set(log.statementName, existing);
    }
    return map;
}

export default function KIRunEditor({
    functionDefinition: propFunctionDefinition,
    onChange,
    functionRepository,
    schemaRepository,
    tokenValueExtractors = new Map(),
    readOnly = false,
    functionKey = '_',
    stores,
    storePaths = new Set(),
    hideArguments,
    debugViewMode = false,
    executionLog,
    personalization = {},
    onPersonalizationChange,
    onCopy: onCopyProp,
    onPaste: onPasteProp,
    className,
    style,
}: KIRunEditorProps) {
    const currentFunctionName = useMemo(() => {
        if (!propFunctionDefinition) return undefined;
        const ns =
            typeof propFunctionDefinition.getNamespace === 'function'
                ? propFunctionDefinition.getNamespace()
                : (propFunctionDefinition as any)?.namespace;
        const name =
            typeof propFunctionDefinition.getName === 'function'
                ? propFunctionDefinition.getName()
                : (propFunctionDefinition as any)?.name;
        return ns ? `${ns}.${name}` : name;
    }, [propFunctionDefinition]);

    const debugInfoMap = useMemo(
        () =>
            debugViewMode
                ? buildDebugInfoMap(executionLog, currentFunctionName)
                : new Map<string, LogEntry[]>(),
        [debugViewMode, executionLog, currentFunctionName],
    );

    const isReadonly = readOnly || debugViewMode;

    const [rawDef, setRawDef] = useState<any>();
    const [name, setName] = useState<string>();
    const [selectedStatements, setSelectedStatements] = useState<Map<string, boolean>>(
        new Map(),
    );
    const [editParameters, setEditParameters] = useState<string>('');
    const [error, setError] = useState<any>();
    const [funDef, setFunDef] = useState<FunctionDefinition | undefined>();

    const [editorMode, setEditorMode] = useState<'visual' | 'text' | 'help'>(
        personalization?.editorMode ?? 'visual',
    );
    const [textContent, setTextContent] = useState<string>('');
    const [syncError, setSyncError] = useState<string>();
    const textChangeOriginRef = useRef(false);
    const textCompileTimeoutRef = useRef<ReturnType<typeof setTimeout>>();
    const editorModeInitRef = useRef(false);

    useEffect(() => {
        if (!editorModeInitRef.current) {
            editorModeInitRef.current = true;
            return;
        }
        const savedMode = personalization?.editorMode;
        if (savedMode && savedMode !== editorMode) {
            setEditorMode(savedMode);
        }
    }, [personalization?.editorMode]);

    const measureStatementNodeHeights = useCallback(
        (stepNames: string[]): Map<string, number> => {
            const heights = new Map<string, number>();
            const defaultHeight = 180;

            for (const name of stepNames) {
                const element = document.getElementById(`statement_${name}`);
                if (element) {
                    const height = element.offsetHeight;
                    heights.set(name, height > 0 ? height : defaultHeight);
                } else {
                    heights.set(name, defaultHeight);
                }
            }

            return heights;
        },
        [],
    );

    const emitChange = useCallback(
        (def: any) => {
            onChange?.(def);
        },
        [onChange],
    );

    const preference = personalization;
    const savePersonalization = useCallback(
        (key: string, value: any) => {
            onPersonalizationChange?.({ ...preference, [key]: value });
        },
        [preference, onPersonalizationChange],
    );

    const handleModeToggle = useCallback(async () => {
        if (editorMode === 'visual') {
            try {
                if (!rawDef) {
                    setTextContent('');
                    setEditorMode('text');
                    savePersonalization('editorMode', 'text');
                    return;
                }
                const dslText = await DSLCompiler.decompile(rawDef);
                setTextContent(dslText);
                setEditorMode('text');
                savePersonalization('editorMode', 'text');
                setSyncError(undefined);
            } catch (err: any) {
                setSyncError(`Failed to convert to text: ${err.message || err}`);
            }
        } else {
            try {
                if (!textContent.trim()) {
                    setEditorMode('visual');
                    savePersonalization('editorMode', 'visual');
                    return;
                }
                const json = DSLCompiler.compile(textContent);

                emitChange(json);
                setEditorMode('visual');
                savePersonalization('editorMode', 'visual');
                setSyncError(undefined);

                setTimeout(() => {
                    const funcDef = FunctionDefinition.from(json);
                    const stepNames = Array.from(funcDef.getSteps().keys());
                    const nodeHeights = measureStatementNodeHeights(stepNames);
                    const newPositions = autoLayoutFunctionDefinition(
                        funcDef,
                        280,
                        nodeHeights,
                        100,
                    );

                    const updatedJson = duplicate(json);
                    if (updatedJson.steps) {
                        for (const [name, pos] of Array.from(newPositions.entries())) {
                            if (updatedJson.steps[name]) {
                                updatedJson.steps[name].position = pos;
                            }
                        }
                    }

                    emitChange(updatedJson);
                }, 100);
            } catch (err: any) {
                setSyncError(`Failed to parse text: ${err.message || err}`);
            }
        }
    }, [editorMode, rawDef, textContent, emitChange, measureStatementNodeHeights, savePersonalization]);

    const handleTextChange = useCallback(
        (newText: string) => {
            setTextContent(newText);
            if (syncError) setSyncError(undefined);

            if (textCompileTimeoutRef.current) {
                clearTimeout(textCompileTimeoutRef.current);
            }
            textCompileTimeoutRef.current = setTimeout(() => {
                try {
                    if (!newText.trim()) return;
                    const json = DSLCompiler.compile(newText);

                    const funcDef = FunctionDefinition.from(json);
                    const newPositions = autoLayoutFunctionDefinition(
                        funcDef,
                        280,
                        180,
                        100,
                    );
                    if (json.steps) {
                        for (const [name, pos] of Array.from(newPositions.entries())) {
                            if (json.steps[name]) {
                                json.steps[name].position = pos;
                            }
                        }
                    }

                    textChangeOriginRef.current = true;
                    emitChange(json);
                } catch (_err) {
                    // Text is not valid DSL yet, don't update
                }
            }, 500);
        },
        [syncError, emitChange],
    );

    const handleFormatCode = useCallback(async () => {
        try {
            const json = DSLCompiler.compile(textContent);
            const formatted = await DSLCompiler.decompile(json);
            setTextContent(formatted);
            setSyncError(undefined);
        } catch (err: any) {
            setSyncError(`Format error: ${err.message}`);
        }
    }, [textContent]);

    useEffect(() => {
        if (editorMode === 'text' && rawDef) {
            if (textChangeOriginRef.current) {
                textChangeOriginRef.current = false;
                return;
            }
            (async () => {
                try {
                    const dslText = await DSLCompiler.decompile(rawDef);
                    setTextContent(dslText);
                    setSyncError(undefined);
                } catch (err: any) {
                    setSyncError(`Failed to convert to text: ${err.message || err}`);
                }
            })();
        }
    }, [rawDef, editorMode]);

    const [executionPlan, setExecutionPlan] = useState<
        ExecutionGraph<string, StatementExecution> | UIError | undefined
    >();
    const [kirunMessages, setKirunMessages] = useState<Map<string, string[]>>(new Map());
    const [positions, setPositions] = useState<Map<string, { left: number; top: number }>>(
        new Map(),
    );

    // Process incoming function definition
    useEffect(() => {
        if (!propFunctionDefinition) {
            setRawDef(undefined);
            setFunDef(undefined);
            setExecutionPlan(undefined);
            return;
        }

        const hereDef = correctStatementNames(propFunctionDefinition);
        setRawDef(hereDef);

        try {
            const inFunDef = isNullValue(hereDef) ? undefined : FunctionDefinition.from(hereDef);
            setFunDef(inFunDef);
            makeUpdates(
                inFunDef,
                setExecutionPlan,
                setKirunMessages,
                functionRepository,
                schemaRepository,
                functionKey,
                tokenValueExtractors,
                setPositions,
            );
            setError(undefined);
        } catch (err) {
            setError(err);
        }
        const finName = `${hereDef?.namespace ?? '_'}.${hereDef?.name}`;
        if (name !== finName) {
            setName(finName);
            setEditParameters('');
            setSelectedStatements(new Map());
        }
    }, [propFunctionDefinition, functionRepository, schemaRepository, functionKey]);

    const textEditorTheme: EditorTheme =
        (preference?.textEditorTheme as EditorTheme) ?? 'light';
    const textEditorWordWrap = preference?.textEditorWordWrap ?? 'on';

    const [textEditorRef, setTextEditorRef] = useState<any>(null);

    const [functionNames, setFunctionNames] = useState<string[]>([]);
    useEffect(() => {
        (async () => {
            const filterNames = await functionRepository.filter('');
            setFunctionNames(filterNames);
        })();
    }, [functionRepository]);

    const selectStatement = useCallback(
        (append: boolean, statementName: string, selectOverride: boolean = false) => {
            if (!append) {
                setSelectedStatements(new Map([[statementName, true]]));
            } else {
                const newSelectedStatements = new Map(selectedStatements);
                if (!selectOverride && newSelectedStatements.has(statementName))
                    newSelectedStatements.delete(statementName);
                else newSelectedStatements.set(statementName, true);
                setSelectedStatements(newSelectedStatements);
            }
        },
        [selectedStatements],
    );

    const container = useRef<HTMLDivElement>(null);
    const [dragNode, setDragNode] = useState<
        | {
              left: number;
              top: number;
              dLeft?: number;
              dTop?: number;
          }
        | undefined
    >();

    const [dragDependencyNode, setDragDependencyNode] = useState<any>();
    const [dragDependencyTo, setDragDependencyTo] = useState<any>();

    const deleteStatements = useCallback(
        (stmts: string[]) => {
            if (isReadonly || !stmts.length) return;
            const def = duplicate(rawDef);
            const newSelectedStatements = new Map(selectedStatements);
            for (let name of stmts) {
                delete def.steps[name];
                if (selectedStatements.has(name)) newSelectedStatements.delete(name);
                Object.values(def.steps).forEach((s: any) => {
                    if (!s.dependentStatements) return;
                    const keysToDelete = Object.keys(s.dependentStatements).filter((e) =>
                        e.startsWith(`Steps.${name}.`),
                    );
                    keysToDelete.forEach((e) => delete s.dependentStatements[e]);
                });
            }

            setSelectedStatements(newSelectedStatements);
            emitChange(def);
        },
        [rawDef, selectedStatements, isReadonly, emitChange],
    );

    const removeAllDependencies = useCallback(
        (stmt: string) => {
            if (isReadonly || !stmt) return;
            const def = duplicate(rawDef);
            delete def.steps[stmt].dependentStatements;
            emitChange(def);
        },
        [rawDef, isReadonly, emitChange],
    );

    const copyStatement = useCallback(
        (statementName: string) => {
            let str: string = '';
            if (!selectedStatements.get(statementName)) {
                if (!rawDef?.steps[statementName]) return;
                str = COPY_STMT_KEY + JSON.stringify(rawDef?.steps[statementName]);
            } else {
                str =
                    COPY_STMT_KEY +
                    Array.from(selectedStatements.entries())
                        .filter(([name, selected]) => selected && rawDef?.steps[name])
                        .map(([name]) => JSON.stringify(rawDef?.steps[name]))
                        .join(COPY_STMT_KEY);
            }
            if (!str || str === COPY_STMT_KEY) return;

            if (onCopyProp) {
                onCopyProp(str);
            } else {
                navigator.clipboard.write([
                    new ClipboardItem({
                        'text/plain': new Blob([str], { type: 'text/plain' }),
                    }),
                ]);
            }
        },
        [selectedStatements, rawDef, onCopyProp],
    );

    let statements: Array<ReactNode> = [];

    if (executionPlan && !('message' in executionPlan) && rawDef?.steps) {
        statements = Object.keys(rawDef.steps ?? {})
            .map((k) => rawDef.steps[k])
            .map((s: any) => (
                <StatementNode
                    statement={s}
                    position={s.position ?? positions.get(s.statementName)}
                    key={s.statementName}
                    functionRepository={functionRepository}
                    schemaRepository={schemaRepository}
                    tokenValueExtractors={tokenValueExtractors}
                    onClick={(append, statementName) => {
                        selectStatement(append, statementName);
                        setDragNode(undefined);
                    }}
                    selected={selectedStatements.has(s.statementName)}
                    selectedStatements={selectedStatements}
                    onDragStart={(append, statementName, startPosition) => {
                        if (!selectedStatements.get(statementName))
                            selectStatement(append, statementName, true);
                        setDragNode(startPosition);
                    }}
                    dragNode={dragNode}
                    container={container}
                    executionPlanMessage={kirunMessages.get(s.statementName)}
                    onChange={(stmt) => {
                        if (isReadonly) return;
                        const def = duplicate(rawDef);
                        delete def.steps[s.statementName];
                        def.steps[stmt.statementName] = stmt;
                        emitChange(def);
                    }}
                    functionNames={functionNames}
                    onDelete={(stmt) => deleteStatements([stmt])}
                    onDependencyDragStart={(pos: any) => setDragDependencyNode(pos)}
                    onDependencyDrop={(stmt) => {
                        if (!dragDependencyNode) return;
                        if (isReadonly) return;
                        if (dragDependencyNode.dependency?.startsWith(`Steps.${stmt}.`))
                            return;

                        const newRawDef = duplicate(rawDef);

                        if (!newRawDef.steps[stmt].dependentStatements)
                            newRawDef.steps[stmt].dependentStatements = {};

                        newRawDef.steps[stmt].dependentStatements[
                            dragDependencyNode.dependency
                        ] = true;

                        setDragDependencyNode(undefined);
                        setDragDependencyTo(undefined);

                        emitChange(newRawDef);
                    }}
                    showComment={!preference.showComments}
                    onEditParameters={() => setEditParameters(s.statementName)}
                    showParamValues={!!preference.showParamValues}
                    onRemoveAllDependencies={() => removeAllDependencies(s.statementName)}
                    onCopy={copyStatement}
                    debugViewMode={debugViewMode}
                    debugLogs={debugInfoMap.get(s.statementName)}
                />
            ));
    }

    let storesDiv: ReactNode = <></>;

    const magnification = preference.magnification ?? 1;

    if (!preference?.showStores && stores && stores.length) {
        storesDiv = (
            <div className="_storeContainer">
                {stores.map((storeName) => (
                    <StoreNode name={storeName} key={storeName} />
                ))}
            </div>
        );
    }

    const [selectionBox, setSelectionBox] = useState<any>({});
    const [scrMove, setScrMove] = useState<any>({});
    const [primedToClick, setPrimedToClick] = useState(false);
    const [showAddSearch, setShowAddSearch] = useState<{ left: number; top: number }>();

    const designerMouseDown = useCallback(
        (e: any) => {
            if (e.target === e.currentTarget) setPrimedToClick(true);
            if (!container.current) return;

            if (e.buttons !== 1) return;
            if (e.altKey) {
                setScrMove({
                    ...scrMove,
                    dragStart: true,
                    startLeft: e.screenX,
                    startTop: e.screenY,
                    oldLeft: container.current!.scrollLeft,
                    oldTop: container.current!.scrollTop,
                });
            } else if (!debugViewMode) {
                const rect = container.current!.getBoundingClientRect();
                const left = e.clientX - rect.left + container.current!.scrollLeft;
                const top = e.clientY - rect.top + container.current!.scrollTop;
                setSelectionBox({ selectionStart: true, left, top });
            }
        },
        [scrMove, debugViewMode],
    );

    const designerMouseMove = useCallback(
        (e: any) => {
            if (!container.current) return;

            setPrimedToClick(false);
            const rect = container.current.getBoundingClientRect();
            const { startLeft, startTop, oldLeft, oldTop } = scrMove;
            if (selectionBox.selectionStart || dragNode) {
                if (e.clientY - rect.top < gridSize * 1.5)
                    container.current.scrollTop -= gridSize / 2;
                else if (e.clientY - rect.top + gridSize * 1.5 > rect.height)
                    container.current.scrollTop += gridSize / 2;
                if (e.clientX - rect.left < gridSize * 1.5)
                    container.current.scrollLeft -= gridSize / 2;
                else if (e.clientX - rect.left + gridSize * 1.5 > rect.width)
                    container.current.scrollLeft += gridSize / 2;
            }

            if (selectionBox.selectionStart) {
                e.preventDefault();
                let { left, top } = selectionBox;
                let right = e.clientX - rect.left + container.current.scrollLeft;
                let bottom = e.clientY - rect.top + container.current.scrollTop;

                setSelectionBox({ ...selectionBox, left, top, right, bottom });
            } else if (scrMove.dragStart) {
                e.preventDefault();
                container.current.scrollLeft = oldLeft + (startLeft - e.screenX);
                container.current.scrollTop = oldTop + (startTop - e.screenY);
            } else if (dragNode) {
                e.preventDefault();
                const dLeft = Math.round(
                    e.clientX - rect.left + container.current.scrollLeft - dragNode.left,
                );
                const dTop = Math.round(
                    e.clientY - rect.top + container.current.scrollTop - dragNode.top,
                );
                setDragNode({ ...dragNode, dLeft, dTop });
            } else if (dragDependencyNode) {
                e.preventDefault();
                const dLeft = Math.round(
                    e.clientX - rect.left + container.current.scrollLeft,
                );
                const dTop = Math.round(
                    e.clientY - rect.top + container.current.scrollTop,
                );
                setDragDependencyTo({ left: dLeft, top: dTop });
            }
        },
        [scrMove, selectionBox, dragNode, dragDependencyNode],
    );

    const designerMouseUp = useCallback(
        (e: any) => {
            if (e.target === e.currentTarget && primedToClick) {
                setSelectedStatements(new Map());
                setPrimedToClick(false);
            }

            if (!dragNode && !scrMove && !selectionBox) {
                setSelectedStatements(new Map());
            }

            if (selectionBox.selectionStart) {
                let { left, top, right, bottom } = selectionBox;
                if (right < left) {
                    const t = left;
                    left = right;
                    right = t;
                }
                if (bottom < top) {
                    const t = top;
                    top = bottom;
                    bottom = t;
                }
                const boxRect = new DOMRect(left, top, right - left, bottom - top);
                const containerRect = container.current?.getBoundingClientRect();
                if (!isNaN(boxRect.width) && !isNaN(boxRect.height)) {
                    const nodes = Object.keys(rawDef?.steps || {})
                        .filter((k) => {
                            const el = document.getElementById(`statement_${k}`);
                            const rect = el?.getBoundingClientRect();
                            if (!rect) return false;

                            let { left, top, right, bottom } = rect;

                            left +=
                                (container.current?.scrollLeft || 0) -
                                (containerRect?.left ?? 0);
                            top +=
                                (container.current?.scrollTop || 0) -
                                (containerRect?.top ?? 0);
                            right +=
                                (container.current?.scrollLeft || 0) -
                                (containerRect?.left ?? 0);
                            bottom +=
                                (container.current?.scrollTop || 0) -
                                (containerRect?.top ?? 0);

                            if (boxRect.left > right || left > boxRect.right) return false;
                            return !(boxRect.top > bottom || top > boxRect.bottom);
                        })
                        .reduce((a, k) => {
                            a.set(k, true);
                            return a;
                        }, new Map<string, boolean>());

                    setSelectedStatements(nodes);
                } else {
                    setSelectedStatements(new Map());
                }
            }

            setSelectionBox({ ...selectionBox, selectionStart: false });
            setScrMove({ ...scrMove, dragStart: false });

            setDragDependencyNode(undefined);
            setDragDependencyTo(undefined);

            if (dragNode) {
                const { dLeft, dTop } = dragNode;
                const def = duplicate(rawDef);
                if (def.steps) {
                    for (const [name] of Array.from(selectedStatements)) {
                        const step = def.steps[name];
                        if (!step) continue;
                        step.position = step.position ?? {};
                        step.position.left =
                            (!isNaN(step.position.left) ? step.position.left : 0) +
                            (dLeft ?? 0);
                        step.position.top =
                            (!isNaN(step.position.top) ? step.position.top : 0) +
                            (dTop ?? 0);
                    }
                }

                setRawDef(def);
                if (!isReadonly) {
                    emitChange(def);
                }
                setDragNode(undefined);
            }
        },
        [
            dragNode,
            scrMove,
            selectionBox,
            rawDef,
            selectedStatements,
            isReadonly,
            primedToClick,
            emitChange,
        ],
    );

    let selector = undefined;
    if (selectionBox.selectionStart) {
        let { left, top, right, bottom } = selectionBox;
        selector = (
            <div
                className="_selectionBox"
                style={{
                    left: `${Math.min(left, right) / magnification}px`,
                    top: `${Math.min(top, bottom) / magnification}px`,
                    width: `${Math.abs(left - right) / magnification}px`,
                    height: `${Math.abs(top - bottom) / magnification}px`,
                }}
            />
        );
    }

    const designerRef = useRef<HTMLDivElement>(null);
    const [menu, showMenu] = useState<any>(undefined);

    let overLine = undefined;
    if (dragDependencyTo) {
        const sx = dragDependencyNode.left / magnification;
        const sy = dragDependencyNode.top / magnification;
        const ex = dragDependencyTo.left / magnification;
        const ey = dragDependencyTo.top / magnification;

        let dPath = `M ${sx} ${sy} Q ${sx + (ex - sx) / 3} ${sy} ${sx + (ex - sx) / 2} ${
            sy + (ey - sy) / 2
        } T ${ex} ${ey}`;

        const stepName = dragDependencyNode.dependency?.split('.')?.[1];
        const fromColor = stepName
            ? generateColor(rawDef.steps[stepName].namespace, rawDef.steps[stepName].name)
            : '000000';

        overLine = (
            <svg className="_linesSvg _overLine">
                <path
                    key="line_drag_path"
                    d={dPath}
                    role="button"
                    className="_connector _selected"
                    stroke={'#' + fromColor}
                />
            </svg>
        );
    }

    const searchBox = showAddSearch ? (
        <div className="_statement _forAdd" style={{ ...showAddSearch }}>
            <Search
                options={functionNames.map((e) => ({ value: e }))}
                onChange={(value) => {
                    if (isReadonly) return;

                    const index = value.lastIndexOf('.');
                    const name = index === -1 ? value : value.substring(index + 1);
                    const namespace = index === -1 ? '_' : value.substring(0, index);

                    const def = duplicate(rawDef);
                    let sName = name.substring(0, 1).toLowerCase() + name.substring(1);

                    if (!def.steps) def.steps = {};

                    let i = '';
                    let num = 0;
                    while (def.steps[`${sName}${i}`]) i = `${++num}`;

                    sName = `${sName}${i}`;
                    def.steps[sName] = {
                        statementName: sName,
                        name,
                        namespace,
                        position: showAddSearch,
                    };
                    setShowAddSearch(undefined);
                    emitChange(def);
                }}
                onClose={() => setShowAddSearch(undefined)}
            />
        </div>
    ) : (
        <></>
    );

    let paramEditor = <></>;
    if (editParameters && rawDef?.steps?.[editParameters]) {
        const s = rawDef.steps[editParameters];
        paramEditor = (
            <StatementParameters
                position={
                    rawDef?.steps?.[editParameters].position ??
                    positions.get(editParameters)
                }
                statement={rawDef?.steps?.[editParameters]}
                functionRepository={functionRepository}
                schemaRepository={schemaRepository}
                storePaths={storePaths}
                onEditParametersClose={() => setEditParameters('')}
            >
                <StatementNode
                    statement={s}
                    position={s.position ?? positions.get(s.statementName)}
                    key={s.statementName}
                    functionRepository={functionRepository}
                    schemaRepository={schemaRepository}
                    tokenValueExtractors={tokenValueExtractors}
                    selected={selectedStatements.has(s.statementName)}
                    selectedStatements={selectedStatements}
                    dragNode={dragNode}
                    container={container}
                    executionPlanMessage={
                        debugViewMode ? undefined : kirunMessages.get(s.statementName)
                    }
                    onChange={(stmt) => {
                        if (isReadonly) return;
                        const def = duplicate(rawDef);
                        delete def.steps[s.statementName];
                        def.steps[stmt.statementName] = stmt;
                        if (s.statementName === editParameters)
                            setEditParameters(stmt.statementName);
                        emitChange(def);
                    }}
                    functionNames={functionNames}
                    onDelete={(stmt) => deleteStatements([stmt])}
                    showComment={true}
                    onEditParameters={(name) => setEditParameters(name)}
                    editParameters={true}
                    showParamValues={true}
                    onRemoveAllDependencies={() => removeAllDependencies(s.statementName)}
                    onCopy={copyStatement}
                />
            </StatementParameters>
        );
    }

    const [editFunction, setEditFunction] = useState<boolean>(false);

    let functionEditor = editFunction ? (
        <FunctionDetailsEditor
            functionKey={functionKey}
            rawDef={rawDef}
            onChange={(def: any) => {
                if (isReadonly) return;
                emitChange(def);
            }}
            onEditFunctionClose={() => setEditFunction(false)}
        />
    ) : (
        <></>
    );

    const editableIcons = isReadonly ? (
        <></>
    ) : (
        <>
            <BoxSelect
                size={16}
                role="button"
                onClick={() => {
                    const entries = Object.entries(rawDef.steps);
                    setSelectedStatements(
                        entries.length === selectedStatements.size
                            ? new Map()
                            : new Map<string, boolean>(entries.map(([k]) => [k, true])),
                    );
                }}
            >
                <title>Select all</title>
            </BoxSelect>
            <div className="_separator" />
            <SquarePlus
                size={16}
                role="button"
                onClick={() => {
                    if (isReadonly) return;
                    setShowAddSearch({ left: 20, top: 20 });
                }}
            >
                <title>Add Step</title>
            </SquarePlus>
            <Trash2
                size={16}
                role="button"
                onClick={() => {
                    if (isReadonly || !selectedStatements.size || !rawDef.steps) return;
                    const def = duplicate(rawDef);
                    for (const [name] of Array.from(selectedStatements)) {
                        delete def.steps[name];
                    }
                    emitChange(def);
                }}
            >
                <title>Delete selected Steps</title>
            </Trash2>
            <div className="_separator" />
        </>
    );

    const editPencilIcon = isReadonly ? (
        <></>
    ) : (
        <>
            <div className="_separator" />
            <Pencil
                size={16}
                role="button"
                onClick={() => setEditFunction(true)}
            >
                <title>Edit Function</title>
            </Pencil>
        </>
    );

    let containerContents: React.JSX.Element;

    const designerStyle: CSSProperties = { transform: `scale(${magnification})` };
    let width = 3000;
    let height = 3000;

    if (executionPlan) {
        const steps = rawDef?.steps ? Object.values(rawDef.steps) : [];
        const maxX = Math.max(...steps.map((e: any) => (e.position?.left ?? 0) as number));
        const maxY = Math.max(...steps.map((e: any) => (e.position?.top ?? 0) as number));
        width = maxX < 2500 ? width : maxX + 1000;
        height = maxY < 2500 ? height : maxY + 1000;
    }

    designerStyle.minWidth = `${width}px`;
    designerStyle.minHeight = `${height}px`;

    if (editorMode === 'help') {
        containerContents = (
            <DSLHelpWindow
                isVisible={true}
                onClose={() => setEditorMode('text')}
                theme={textEditorTheme}
            />
        );
    } else if (editorMode === 'text') {
        containerContents = (
            <div
                style={{
                    width: '100%',
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                }}
            >
                {syncError && (
                    <div
                        style={{
                            padding: '10px',
                            backgroundColor: '#f44336',
                            color: 'white',
                            margin: '10px',
                            borderRadius: '4px',
                            flexShrink: 0,
                        }}
                    >
                        {syncError}
                    </div>
                )}
                <div style={{ flex: 1, overflow: 'hidden' }}>
                    <KIRunTextEditor
                        value={textContent}
                        onChange={handleTextChange}
                        theme={textEditorTheme}
                        wordWrap={textEditorWordWrap}
                        readOnly={isReadonly}
                        functionRepository={functionRepository}
                        schemaRepository={schemaRepository}
                        onEditorReady={setTextEditorRef}
                    />
                </div>
            </div>
        );
    } else if (!error) {
        containerContents = (
            <>
                <div
                    className={`_designer ${scrMove.dragStart ? '_moving' : ''} _theme-${textEditorTheme}`}
                    style={designerStyle}
                    data-theme={textEditorTheme}
                    onMouseDown={designerMouseDown}
                    onMouseMove={designerMouseMove}
                    onMouseUp={designerMouseUp}
                    onMouseLeave={designerMouseUp}
                    onDoubleClick={(ev) => {
                        ev.preventDefault();
                        ev.stopPropagation();
                        const parentRect = designerRef.current!.getBoundingClientRect();
                        setShowAddSearch({
                            left:
                                (ev.clientX - parentRect.left - 5) / magnification,
                            top:
                                (ev.clientY - parentRect.top - 5) / magnification,
                        });
                    }}
                    ref={designerRef}
                    tabIndex={0}
                    onKeyUp={(ev) => {
                        if (ev.key === 'Delete' || ev.key === 'Backspace') {
                            if (selectedStatements.size > 0)
                                deleteStatements(Array.from(selectedStatements.keys()));
                        } else if (
                            (ev.key === 'a' || ev.key === 'A') &&
                            (ev.ctrlKey || ev.metaKey)
                        ) {
                            ev.stopPropagation();
                            ev.preventDefault();
                            const entries = Object.entries(rawDef.steps);
                            setSelectedStatements(
                                entries.length === selectedStatements.size
                                    ? new Map()
                                    : new Map<string, boolean>(
                                          entries.map(([k]) => [k, true]),
                                      ),
                            );
                        } else if (ev.key === 'Escape') {
                            setSelectedStatements(new Map());
                        } else if (
                            (ev.key === '+' || ev.key === '=' || ev.key === '-') &&
                            (ev.ctrlKey || ev.metaKey)
                        ) {
                            savePersonalization(
                                'magnification',
                                magnification + (ev.key === '-' ? -0.1 : 0.1),
                            );
                        }
                    }}
                    onContextMenu={(ev) => {
                        ev.preventDefault();
                        ev.stopPropagation();
                        if (debugViewMode) return;
                        const parentRect = designerRef.current!.getBoundingClientRect();
                        showMenu({
                            position: {
                                left:
                                    (ev.clientX - parentRect.left) / magnification,
                                top:
                                    (ev.clientY - parentRect.top) / magnification,
                            },
                            type: 'designer',
                            value: {},
                        });
                    }}
                >
                    <ExecutionGraphLines
                        executionPlan={executionPlan}
                        designerRef={designerRef}
                        rawDef={rawDef}
                        selectedStatements={selectedStatements}
                        menu={menu}
                        setSelectedStatements={setSelectedStatements}
                        functionRepository={functionRepository}
                        showMenu={showMenu}
                        stores={stores}
                        showStores={!preference?.showStores}
                        showParamValues={!preference?.showParamValues}
                        hideArguments={hideArguments}
                    />
                    {statements}
                    {storesDiv}
                    {selector}
                    <KIRunContextMenu
                        menu={menu}
                        showMenu={showMenu}
                        isReadonly={isReadonly}
                        rawDef={rawDef}
                        onChange={emitChange}
                        setShowAddSearch={setShowAddSearch}
                        onPaste={onPasteProp}
                    />
                    {overLine}
                    {searchBox}
                </div>
                {paramEditor}
                {functionEditor}
            </>
        );
    } else {
        containerContents = <div className="_error">{error?.message ?? error}</div>;
    }

    const autoLayoutIcon = isReadonly ? (
        <></>
    ) : (
        <>
            <div className="_separator" />
            <Workflow
                size={16}
                role="button"
                onClick={() => {
                    if (isReadonly) return;
                    if (!rawDef?.steps) return;

                    const def = duplicate(rawDef);
                    const funcDef = FunctionDefinition.from(def);
                    const stepNames = Array.from(funcDef.getSteps().keys());
                    const nodeHeights = measureStatementNodeHeights(stepNames);
                    const newPositions = autoLayoutFunctionDefinition(
                        funcDef,
                        280,
                        nodeHeights,
                        100,
                    );

                    for (const [name, pos] of Array.from(newPositions.entries())) {
                        if (def.steps[name]) def.steps[name].position = pos;
                    }

                    emitChange(def);
                }}
            >
                <title>Auto Layout</title>
            </Workflow>
        </>
    );

    const iconStyle = { cursor: 'pointer', padding: '8px' };

    return (
        <div
            className={`_kirun-editor ${debugViewMode ? '_debugView' : ''} _theme-${textEditorTheme} ${className ?? ''}`}
            style={{
                ...(style ?? {}),
                display: 'flex',
                flexDirection: 'column',
                height: '100%',
                overflow: 'hidden',
            }}
            data-theme={textEditorTheme}
        >
            <div
                className="_header"
                style={{
                    minHeight: '40px',
                    display: 'flex',
                    alignItems: 'center',
                    flexShrink: 0,
                    zIndex: 10,
                }}
            >
                <div className="_left">
                    {editorMode === 'visual' && (
                        <>
                            {editableIcons}
                            <Variable
                                size={16}
                                role="button"
                                onClick={() => {
                                    savePersonalization(
                                        'showParamValues',
                                        !preference?.showParamValues,
                                    );
                                }}
                            >
                                <title>{!preference?.showParamValues ? 'Show Parameter Values' : 'Hide Parameter Values'}</title>
                            </Variable>
                            <MessageCircle
                                size={16}
                                role="button"
                                onClick={() => {
                                    savePersonalization(
                                        'showComments',
                                        preference?.showComments === undefined
                                            ? true
                                            : !preference.showComments,
                                    );
                                }}
                            >
                                <title>{preference?.showComments ? 'Show Comments' : 'Hide Comments'}</title>
                            </MessageCircle>
                            <Database
                                size={16}
                                role="button"
                                onClick={() => {
                                    savePersonalization(
                                        'showStores',
                                        preference?.showStores === undefined
                                            ? true
                                            : !preference.showStores,
                                    );
                                }}
                            >
                                <title>{preference?.showStores ? 'Show Stores' : 'Hide Stores'}</title>
                            </Database>
                            {editPencilIcon}
                            {autoLayoutIcon}
                        </>
                    )}
                    {editorMode === 'help' && (
                        <ArrowLeft
                            size={18}
                            role="button"
                            onClick={() => setEditorMode('text')}
                            style={iconStyle}
                        >
                            <title>Back to Text Editor</title>
                        </ArrowLeft>
                    )}
                    {editorMode === 'text' && (
                        <>
                            <CircleHelp
                                size={18}
                                role="button"
                                onClick={() => setEditorMode('help')}
                                style={iconStyle}
                            >
                                <title>Show Language Help</title>
                            </CircleHelp>
                            <div className="_separator" />
                            <WrapText
                                size={16}
                                role="button"
                                onClick={() => {
                                    savePersonalization(
                                        'textEditorWordWrap',
                                        textEditorWordWrap === 'on' ? 'off' : 'on',
                                    );
                                }}
                                style={iconStyle}
                            >
                                <title>{textEditorWordWrap === 'on' ? 'Disable Word Wrap' : 'Enable Word Wrap'}</title>
                            </WrapText>
                            <AlignLeft
                                size={16}
                                role="button"
                                onClick={handleFormatCode}
                                style={iconStyle}
                            >
                                <title>Format Code</title>
                            </AlignLeft>
                            <SearchIcon
                                size={16}
                                role="button"
                                onClick={() => {
                                    if (textEditorRef) {
                                        textEditorRef
                                            .getAction('actions.find')
                                            ?.run();
                                    }
                                }}
                                style={iconStyle}
                            >
                                <title>Find</title>
                            </SearchIcon>
                            <Replace
                                size={16}
                                role="button"
                                onClick={() => {
                                    if (textEditorRef) {
                                        textEditorRef
                                            .getAction(
                                                'editor.action.startFindReplaceAction',
                                            )
                                            ?.run();
                                    }
                                }}
                                style={iconStyle}
                            >
                                <title>Find and Replace</title>
                            </Replace>
                        </>
                    )}
                    <div className="_separator" />
                    <select
                        value={textEditorTheme}
                        onChange={(e) =>
                            savePersonalization('textEditorTheme', e.target.value)
                        }
                        title="Editor Theme"
                        style={{
                            padding: '4px 8px',
                            marginLeft: '8px',
                            borderRadius: '4px',
                            border: '1px solid #ccc',
                            backgroundColor: 'var(--background-color, white)',
                            color: 'var(--font-color, black)',
                            cursor: 'pointer',
                        }}
                    >
                        <option value="light">Light</option>
                        <option value="dark">Dark</option>
                        <option value="high-contrast">High Contrast</option>
                        <option value="easy-on-eyes">Easy on Eyes</option>
                        <option value="flared-up">Flared Up</option>
                    </select>
                    {!debugViewMode && editorMode !== 'help' && (
                        <>
                            <div className="_separator" />
                            {editorMode === 'text' ? (
                                <Workflow
                                    size={18}
                                    role="button"
                                    onClick={handleModeToggle}
                                    style={iconStyle}
                                >
                                    <title>Switch to Visual Mode</title>
                                </Workflow>
                            ) : (
                                <Code
                                    size={18}
                                    role="button"
                                    onClick={handleModeToggle}
                                    style={iconStyle}
                                >
                                    <title>Switch to Text Mode</title>
                                </Code>
                            )}
                        </>
                    )}
                </div>
                <div className="_right">
                    {editorMode === 'visual' && (
                        <>
                            <ZoomIn
                                size={16}
                                role="button"
                                onClick={() =>
                                    savePersonalization(
                                        'magnification',
                                        magnification + 0.1,
                                    )
                                }
                            >
                                <title>Zoom in</title>
                            </ZoomIn>
                            <SearchIcon
                                size={16}
                                role="button"
                                onClick={() => savePersonalization('magnification', 1)}
                            >
                                <title>Reset zoom</title>
                            </SearchIcon>
                            <ZoomOut
                                size={16}
                                role="button"
                                onClick={() =>
                                    savePersonalization(
                                        'magnification',
                                        magnification - 0.1,
                                    )
                                }
                            >
                                <title>Zoom out</title>
                            </ZoomOut>
                        </>
                    )}
                </div>
            </div>
            <div className="_container" ref={container}>
                {containerContents}
            </div>
        </div>
    );
}
