import {
    Event,
    Function,
    LogEntry,
    Repository,
    Schema,
    TokenValueExtractor,
} from '@fincity/kirun-js';
import React, { RefObject, useEffect, useState } from 'react';
import ReactDOM from 'react-dom';
import {
    Pencil,
    ListTree,
    ChevronUp,
    ChevronDown,
    Cpu,
    Box,
    HardDrive,
    Repeat,
    Calculator,
    Type,
    Layers,
    CircleDot,
    Sparkles,
} from 'lucide-react';
import { duplicate } from '@fincity/kirun-js';
import { generateColor } from '../colors';
import { stringValue } from '../util/stringValue';
import Search from './Search';
import StatementButtons from './StatementButtons';
import ParamEditor from './ParamEditor';

interface StatementNodeProps {
    position?: { left: number; top: number };
    statement: any;
    functionRepository: Repository<Function>;
    schemaRepository: Repository<Schema>;
    tokenValueExtractors: Map<string, TokenValueExtractor>;
    onDragStart?: (
        append: boolean,
        statementName: string,
        startPosition: { left: number; top: number } | undefined,
    ) => void;
    selected: boolean;
    onClick?: (append: boolean, statementName: string) => void;
    container: RefObject<HTMLDivElement>;
    dragNode: any;
    executionPlanMessage?: string[];
    onChange: (statement: any) => void;
    functionNames: string[];
    onDelete: (statementName: string) => void;
    onDependencyDragStart?: (ddPos: any) => void;
    onDependencyDrop?: (statement: string) => void;
    showComment: boolean;
    onEditParameters?: (statementName: string) => void;
    editParameters?: boolean;
    showParamValues: boolean;
    onRemoveAllDependencies: () => void;
    selectedStatements: Map<string, boolean>;
    onCopy: (statementName: string) => void;
    // Debug mode props
    debugViewMode?: boolean;
    debugLogs?: LogEntry[];
}

const DEFAULT_POSITION = { left: 0, top: 0 };

const VARIBALE_NAME_REGEX = /^[A-Za-z_]{1,1}[_A-Za-z0-9]+$/;

export default function StatementNode({
    position = DEFAULT_POSITION,
    statement,
    functionRepository,
    schemaRepository,
    onDragStart,
    onClick,
    selected = false,
    container,
    dragNode,
    executionPlanMessage,
    onChange,
    functionNames,
    onDelete,
    onDependencyDragStart,
    onDependencyDrop,
    showComment,
    onEditParameters,
    editParameters,
    showParamValues,
    onRemoveAllDependencies,
    selectedStatements,
    onCopy,
    debugViewMode = false,
    debugLogs,
}: StatementNodeProps) {
    const [statementName, setStatementName] = useState(statement.statementName);
    const [editStatementName, setEditStatementName] = useState(false);
    const [editNameNamespace, setEditNameNamespace] = useState(false);
    const [validationMessages, setValidationMessages] = useState<Map<string, string>>(new Map());
    const [name, setName] = useState(
        ((statement.namespace ?? '_') === '_' ? '' : statement.namespace + '.') + statement.name,
    );
    const [debugExpanded, setDebugExpanded] = useState(false);

    // Reset debug expanded state when logs change (e.g., switching between functions)
    useEffect(() => {
        setDebugExpanded(false);
    }, [debugLogs]);

    useEffect(() => {
        setStatementName(statement.statementName);
        setName(
            ((statement.namespace ?? '_') === '_' ? '' : statement.namespace + '.') +
                statement.name,
        );
    }, [statement]);

    useEffect(() => {
        const map = new Map();
        if (!VARIBALE_NAME_REGEX.test(statementName)) {
            map.set(
                'statementName',
                'Step name cannot have spaces or special characters and should be atleast one character.',
            );
        }
        if (!functionRepository.find(statement.namespace, statement.name)) {
            map.set('function', 'Function does not exist.');
        }
        setValidationMessages(map);
    }, [statementName, name, statement]);

    const [mouseMove, setMouseMove] = useState(false);
    const alwaysColor =
        validationMessages.size > 0 || executionPlanMessage?.length
            ? '#DC2626'
            : `#${generateColor(statement.namespace, statement.name)}`;

    const [editComment, setEditComment] = useState(false);

    const [repoFunction, setRepoFunction] = useState<Function | undefined>(undefined);

    useEffect(() => {
        (async () =>
            setRepoFunction(
                await functionRepository.find(statement.namespace, statement.name),
            ))();
    }, [statement, functionRepository]);
    const repoSignature = repoFunction?.getSignature();

    const parameters = repoSignature?.getParameters()
        ? Array.from(repoSignature?.getParameters().values())
        : [];

    let eventsMap = repoFunction?.getSignature()?.getEvents();
    if (!eventsMap || !eventsMap.get(Event.OUTPUT)) {
        if (!eventsMap) eventsMap = new Map();
        eventsMap.set(Event.OUTPUT, new Event(Event.OUTPUT, new Map()));
    }

    const events = Array.from(eventsMap.values());

    // Filter parameters to only show filled ones when not in edit mode
    const filledParameters = editParameters
        ? parameters
        : parameters.filter((e) => {
              const paramValue = statement.parameterMap?.[e.getParameterName()];
              return paramValue && Object.values(paramValue).length > 0;
          });

    const params = filledParameters.length ? (
        <div
            className="_paramsContainer"
            onDoubleClick={(ev) => {
                ev.preventDefault();
                ev.stopPropagation();
                if (editParameters) return;
                onClick?.(false, statement.statementName);
                onEditParameters?.(statement.statementName);
            }}
        >
            <div className="_paramHeader">Parameters</div>
            {filledParameters
                .sort((a, b) => a.getParameterName().localeCompare(b.getParameterName()))
                .map((e) => {
                    const paramValue = statement.parameterMap?.[e.getParameterName()];
                    const hasValue = paramValue && Object.values(paramValue).length;
                    const title = stringValue(paramValue);
                    let paramDiv = undefined;
                    if (editParameters)
                        paramDiv = (
                            <ParamEditor
                                parameter={e}
                                schemaRepository={schemaRepository}
                                value={paramValue}
                                onChange={(v) => {
                                    const newStatement = duplicate(statement);
                                    if (!newStatement.parameterMap)
                                        newStatement.parameterMap = {};
                                    newStatement.parameterMap[e.getParameterName()] = v;
                                    onChange(newStatement);
                                }}
                            />
                        );
                    else if (showParamValues && title?.string)
                        paramDiv = <div className="_paramValue">{title?.string}</div>;
                    else paramDiv = <></>;
                    return (
                        <div className="_param" key={e.getParameterName()}>
                            <div
                                id={`paramNode_${statement.statementName}_${e.getParameterName()}`}
                                className="_paramNode _hideInEdit"
                            ></div>
                            <div
                                className={`_paramName ${hasValue ? '_hasValue' : ''}`}
                                title={'' + (title?.string ?? '')}
                            >
                                {e.getParameterName()}
                            </div>
                            {paramDiv}
                        </div>
                    );
                })}
        </div>
    ) : (
        <></>
    );

    const dependencyNode = (
        <div
            className="_dependencyNode _hideInEdit"
            id={`eventNode_dependentNode_${statement.statementName}`}
            title="Depends on"
        ></div>
    );

    // State for copy feedback toast
    const [copiedPath, setCopiedPath] = useState<string | null>(null);

    // Helper function to copy path to clipboard with visual feedback
    const copyToClipboard = (path: string) => {
        navigator.clipboard.writeText(path);
        setCopiedPath(path);
        setTimeout(() => setCopiedPath(null), 1800);
    };

    const eventsDiv = events.length ? (
        events
            .sort((a, b) => {
                // Put OUTPUT at the top, then sort alphabetically
                const aName = a.getName();
                const bName = b.getName();
                if (aName === 'output') return -1;
                if (bName === 'output') return 1;
                return aName.localeCompare(bName);
            })
            .map((e) => {
                const eventParams = Array.from(e.getParameters()?.entries() ?? []);
                const eventPath = `Steps.${statement.statementName}.${e.getName()}`;
                return (
                    <div className="_paramsContainer _event" key={e.getName()}>
                        <div
                            className="_paramHeader"
                            title={`Click to copy: ${eventPath}`}
                            onClick={(ev) => {
                                ev.stopPropagation();
                                copyToClipboard(eventPath);
                            }}
                        >
                            {e.getName()}
                        </div>
                        <div
                            id={`eventNode_${statement.statementName}_${e.getName()}`}
                            className="_paramNode _eventNode"
                            onMouseDown={(ev) => {
                                ev.stopPropagation();
                                ev.preventDefault();
                                const rect = container.current!.getBoundingClientRect();
                                const tRect = ev.currentTarget.getBoundingClientRect();
                                const left = Math.round(
                                    tRect.left - rect.left + container.current!.scrollLeft,
                                );
                                const top = Math.round(
                                    tRect.top - rect.top + container.current!.scrollTop,
                                );
                                onDependencyDragStart?.({
                                    left,
                                    top,
                                    dependency: eventPath,
                                });
                            }}
                        ></div>
                        {eventParams
                            .sort((a, b) => a[0].localeCompare(b[0]))
                            .map(([pname]) => {
                                const paramPath = `Steps.${statement.statementName}.${e.getName()}.${pname}`;
                                return (
                                    <div className="_param" key={pname}>
                                        <div
                                            id={`eventParameter_${statement.statementName}_${e.getName()}_${pname}`}
                                            className="_paramNode _paramNameNode"
                                            onMouseDown={(ev) => {
                                                ev.stopPropagation();
                                                ev.preventDefault();
                                                const rect =
                                                    container.current!.getBoundingClientRect();
                                                const tRect =
                                                    ev.currentTarget.getBoundingClientRect();
                                                const left = Math.round(
                                                    tRect.left -
                                                        rect.left +
                                                        container.current!.scrollLeft,
                                                );
                                                const top = Math.round(
                                                    tRect.top -
                                                        rect.top +
                                                        container.current!.scrollTop,
                                                );
                                                onDependencyDragStart?.({
                                                    left,
                                                    top,
                                                    dependency: paramPath,
                                                });
                                            }}
                                        ></div>
                                        <div
                                            className="_paramName"
                                            title={`Click to copy: ${paramPath}`}
                                            onClick={(ev) => {
                                                ev.stopPropagation();
                                                copyToClipboard(paramPath);
                                            }}
                                        >
                                            {pname}
                                        </div>
                                    </div>
                                );
                            })}
                    </div>
                );
            })
    ) : (
        <></>
    );

    const [changeComment, setChangeComment] = useState<string>(statement.comment ?? '');
    useEffect(() => setChangeComment(statement.comment), [statement.comment]);

    const comments =
        (showComment && statement.comment) || editComment ? (
            <div
                className="_commentContainer"
                onDoubleClick={(e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    setEditComment(true);
                }}
            >
                <span
                    className="_comment"
                    onMouseDown={(e) => {
                        e.preventDefault();
                        e.stopPropagation();
                    }}
                >
                    {statement.comment ?? ''}
                </span>
                {editComment ? (
                    <textarea
                        className="_commentEditor"
                        value={changeComment}
                        placeholder="Add a comment"
                        onKeyUp={(e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            if (e.key === 'Escape') {
                                setEditComment(false);
                                setChangeComment(statement.comment);
                            }
                        }}
                        onBlur={() => {
                            setEditComment(false);
                            onChange({ ...duplicate(statement), comment: changeComment });
                        }}
                        onChange={(e) => setChangeComment(e.target.value)}
                        onMouseDown={(e) => {
                            e.stopPropagation();
                        }}
                        autoFocus
                    />
                ) : (
                    <></>
                )}
            </div>
        ) : (
            <></>
        );

    const categoryLabel = getCategoryLabel(statement.namespace ?? '_');

    // Build debug info section when in debug view mode
    const debugInfoSection =
        debugViewMode && debugLogs && debugLogs.length > 0 ? (
            <DebugInfoSection logs={debugLogs} onExpandChange={setDebugExpanded} />
        ) : null;

    // Determine debug status for styling
    const hasDebugError = debugLogs?.some((log) => log.error);
    const wasExecuted = debugViewMode && debugLogs && debugLogs.length > 0;
    const debugStatusClass = debugViewMode
        ? wasExecuted
            ? hasDebugError
                ? '_executedWithError'
                : '_executed'
            : '_notExecuted'
        : '';

    return (
        <div
            className={`_statement ${selected ? '_selected' : ''} ${
                editParameters ? '_editParameters' : ''
            } ${debugStatusClass}`}
            style={{
                left: position.left + (selected && dragNode ? dragNode.dLeft : 0) + 'px',
                top: position.top + (selected && dragNode ? dragNode.dTop : 0) + 'px',
                zIndex: selected || debugExpanded ? '3' : '',
            }}
            id={`statement_${statement.statementName}`}
            onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
            }}
            onMouseUp={() => {
                onDependencyDrop?.(statement.statementName);
            }}
            onContextMenu={(e) => {
                e.preventDefault();
                e.stopPropagation();
            }}
            onDoubleClick={(ev) => {
                ev.preventDefault();
                ev.stopPropagation();
            }}
        >
            {comments}
            <div className="_namesContainer">
                <div className="_categoryLabel">{categoryLabel}</div>
                <div
                    className="_nameContainer"
                    onMouseDown={(e) => {
                        e.preventDefault();
                        e.stopPropagation();

                        if (e.button !== 0 || debugViewMode) return;

                        const rect = container.current!.getBoundingClientRect();
                        const left = Math.round(
                            e.clientX - rect.left + container.current!.scrollLeft,
                        );
                        const top = Math.round(
                            e.clientY - rect.top + container.current!.scrollTop,
                        );
                        onDragStart?.(e.ctrlKey || e.metaKey, statement.statementName, {
                            left,
                            top,
                        });
                    }}
                    onMouseMove={() => {
                        if (!mouseMove && dragNode) setMouseMove(true);
                    }}
                    onMouseUp={(e) => {
                        if (e.button !== 0) return;

                        if (e.target === e.currentTarget && !mouseMove) {
                            e.preventDefault();
                            e.stopPropagation();
                            onClick?.(e.ctrlKey || e.metaKey, statement.statementName);
                        }
                        onDependencyDrop?.(statement.statementName);

                        setMouseMove(false);
                    }}
                    onDoubleClick={(e) => {
                        e.stopPropagation();
                        e.preventDefault();
                    }}
                >
                    {(() => {
                        const StepIcon = ICONS_GROUPS.get(statement.namespace) ?? Cpu;
                        return (
                            <span className="_icon" style={{ backgroundColor: `#${alwaysColor.replace('#', '')}` }}>
                                <StepIcon size={16} />
                            </span>
                        );
                    })()}
                    <div className="_statementContanier">
                        <div
                            className={`_statementName`}
                            onDoubleClick={(e) => {
                                e.stopPropagation();
                                e.preventDefault();
                                if (editParameters || debugViewMode) return;
                                setEditStatementName(true);
                            }}
                        >
                            {editStatementName ? (
                                <>
                                    <input
                                        type="text"
                                        value={statementName}
                                        onChange={(e) => setStatementName(e.target.value)}
                                        autoFocus={true}
                                        onBlur={() => {
                                            setEditStatementName(false);
                                            onChange({
                                                ...duplicate(statement),
                                                statementName,
                                            });
                                        }}
                                        onKeyUp={(e) => {
                                            if (
                                                e.key === 'Delete' ||
                                                e.key === 'Backspace'
                                            ) {
                                                e.stopPropagation();
                                                e.preventDefault();
                                            } else if (e.key === 'Escape') {
                                                setStatementName(statement.statementName);
                                                setEditStatementName(false);
                                            } else if (e.key === 'Enter') {
                                                setEditStatementName(false);
                                                onChange({
                                                    ...duplicate(statement),
                                                    statementName,
                                                });
                                                onEditParameters?.(statementName);
                                            }
                                        }}
                                    />
                                </>
                            ) : (
                                <>
                                    {statementName}
                                    {!debugViewMode && (
                                        <Pencil
                                            className="_editIcon _hideInEdit"
                                            size={12}
                                            style={{
                                                opacity: editStatementName ? 1 : undefined,
                                            }}
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                setEditStatementName(true);
                                            }}
                                        />
                                    )}
                                </>
                            )}
                        </div>
                    </div>
                </div>
                <div className={`_nameNamespaceContainer`}>
                    <div
                        className={`_nameNamespace`}
                        onDoubleClick={(e) => {
                            e.stopPropagation();
                            e.preventDefault();
                            if (editParameters || debugViewMode) return;
                            setEditNameNamespace(true);
                            onClick?.(false, statement.statementName);
                        }}
                    >
                        {editNameNamespace ? (
                            <Search
                                value={name}
                                options={functionNames.map((e) => ({
                                    value: e,
                                }))}
                                onChange={(value) => {
                                    const index = value.lastIndexOf('.');

                                    const name =
                                        index === -1 ? value : value.substring(index + 1);
                                    const namespace =
                                        index === -1 ? '_' : value.substring(0, index);
                                    onChange({ ...duplicate(statement), name, namespace });
                                    setEditNameNamespace(false);
                                }}
                                onClose={() => setEditNameNamespace(false)}
                            />
                        ) : (
                            name
                        )}
                    </div>
                    {!debugViewMode && (
                        <ListTree
                            className="_editIcon _hideInEdit"
                            size={12}
                            style={{
                                visibility: editNameNamespace ? 'visible' : undefined,
                            }}
                            onClick={() => {
                                setEditNameNamespace(true);
                                onClick?.(false, statement.statementName);
                            }}
                        />
                    )}
                </div>
            </div>
            <div className="_otherContainer">
                {params}
                <div className="_eventsContainer _hideInEdit">{eventsDiv}</div>
            </div>
            {debugInfoSection}
            <div className="_messages">
                {executionPlanMessage &&
                    executionPlanMessage.map((value) => (
                        <div key={value} className="_message">
                            {value}
                        </div>
                    ))}
                {validationMessages.size > 0 &&
                    Array.from(validationMessages.entries()).map(([key, value]) => (
                        <div key={key} className="_message">
                            {value}
                        </div>
                    ))}
            </div>
            {debugViewMode ? (
                <></>
            ) : (
                <StatementButtons
                    selected={selected}
                    onEditParameters={onEditParameters}
                    onEditComment={() => setEditComment(true)}
                    statementName={statement.statementName}
                    onDelete={onDelete}
                    statement={statement}
                    showEditParameters={!!parameters.length}
                    editParameters={editParameters}
                    onRemoveAllDependencies={onRemoveAllDependencies}
                    onCopy={onCopy}
                />
            )}
            {dependencyNode}
            {copiedPath &&
                ReactDOM.createPortal(
                    <div className="_copiedToast">Copied to clipboard</div>,
                    document.body,
                )}
        </div>
    );
}

const ICONS_GROUPS = new Map<string, React.ComponentType<any>>([
    ['System', Box],
    ['System.Context', HardDrive],
    ['System.Loop', Repeat],
    ['System.Math', Calculator],
    ['System.String', Type],
    ['System.Array', Layers],
    ['System.Object', CircleDot],
    ['UIEngine', Sparkles],
]);

const CATEGORY_LABELS = new Map<string, string>([
    ['System', 'SYSTEM'],
    ['System.Context', 'CONTEXT'],
    ['System.Loop', 'LOOP'],
    ['System.Math', 'MATH'],
    ['System.String', 'STRING'],
    ['System.Array', 'ARRAY'],
    ['System.Object', 'OBJECT'],
    ['UIEngine', 'UI ENGINE'],
    ['_', 'FUNCTION'],
]);

function getCategoryLabel(namespace: string): string {
    return CATEGORY_LABELS.get(namespace) ?? CATEGORY_LABELS.get('_') ?? 'FUNCTION';
}

// Helper to format duration for display
function formatDuration(ms: number | undefined): string {
    if (ms === undefined) return '';
    if (ms < 1) return '<1ms';
    if (ms < 1000) return `${Math.round(ms)}ms`;
    return `${(ms / 1000).toFixed(2)}s`;
}

// Component to display debug information for a statement
function DebugInfoSection({
    logs,
    onExpandChange,
}: {
    logs: LogEntry[];
    onExpandChange?: (expanded: boolean) => void;
}) {
    const [expanded, setExpanded] = useState(false);

    // Reset expanded state when logs change (e.g., switching between functions)
    useEffect(() => {
        setExpanded(false);
        onExpandChange?.(false);
    }, [logs]);

    const handleExpandToggle = () => {
        const newExpanded = !expanded;
        setExpanded(newExpanded);
        onExpandChange?.(newExpanded);
    };

    // Calculate total duration from all logs
    const totalDuration = logs.reduce((sum, log) => sum + (log.duration ?? 0), 0);
    const hasError = logs.some((log) => log.error);
    const executionCount = logs.length;

    // Determine badge class based on duration
    let badgeClass = '';
    if (hasError) {
        badgeClass = '_errored';
    } else if (totalDuration > 1000) {
        badgeClass = '_verySlow';
    } else if (totalDuration > 100) {
        badgeClass = '_slow';
    }

    // Check if there's content to expand
    const hasExpandableContent = logs.some(
        (log) =>
            (log.arguments && Object.keys(log.arguments).length > 0) ||
            (log.result && Object.keys(log.result).length > 0),
    );

    return (
        <div className={`_statementDebugInfo ${hasError ? '_errored' : ''}`}>
            <div
                className="_debugHeader"
                role={hasExpandableContent ? 'button' : undefined}
                tabIndex={hasExpandableContent ? 0 : undefined}
                onClick={(e) => {
                    e.stopPropagation();
                    if (hasExpandableContent) handleExpandToggle();
                }}
                onKeyDown={(e) => {
                    if (hasExpandableContent && (e.key === 'Enter' || e.key === ' ')) {
                        e.stopPropagation();
                        handleExpandToggle();
                    }
                }}
            >
                <span className={`_debugBadge ${badgeClass}`}>
                    {formatDuration(totalDuration)}
                </span>
                {executionCount > 1 && (
                    <span className="_debugExecutionCount">×{executionCount}</span>
                )}
                {logs.at(-1)?.eventName && (
                    <span className="_debugEvent">{logs.at(-1)?.eventName}</span>
                )}
                {hasExpandableContent && (
                    expanded ? <ChevronUp className="_expandIcon" size={12} /> : <ChevronDown className="_expandIcon" size={12} />
                )}
            </div>

            {hasError &&
                logs
                    .filter((log) => log.error)
                    .map((log, idx) => (
                        <div
                            key={log.stepId || `error-${idx}`}
                            className="_debugInfoRow _errorRow"
                        >
                            <span className="_debugInfoLabel">
                                Error{executionCount > 1 ? ` #${idx + 1}` : ''}:
                            </span>
                            <span className="_debugInfoValue _error">{log.error}</span>
                        </div>
                    ))}

            {expanded && (
                <div className="_debugExpandedContent">
                    {logs.map((log, idx) => (
                        <div key={log.stepId || idx} className="_debugLogEntry">
                            {executionCount > 1 && (
                                <div className="_debugLogEntryHeader">
                                    <span className="_debugLogEntryIndex">
                                        Execution #{idx + 1}
                                    </span>
                                    {log.duration !== undefined && (
                                        <span className="_debugLogEntryDuration">
                                            {formatDuration(log.duration)}
                                        </span>
                                    )}
                                    {log.eventName && (
                                        <span className="_debugLogEntryEvent">
                                            {log.eventName}
                                        </span>
                                    )}
                                </div>
                            )}
                            {log.arguments && Object.keys(log.arguments).length > 0 && (
                                <div className="_debugInfoRow">
                                    <span className="_debugInfoLabel">Arguments:</span>
                                    <pre className="_debugInfoValue _json">
                                        {JSON.stringify(log.arguments, null, 2)}
                                    </pre>
                                </div>
                            )}
                            {log.result && Object.keys(log.result).length > 0 && (
                                <div className="_debugInfoRow">
                                    <span className="_debugInfoLabel">Result:</span>
                                    <pre className="_debugInfoValue _json">
                                        {JSON.stringify(log.result, null, 2)}
                                    </pre>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
