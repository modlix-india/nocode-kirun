import {
    ExecutionGraph,
    Function,
    FunctionSignature,
    GraphVertex,
    LinkedList,
    Parameter,
    ParameterReferenceType,
    Repository,
    StatementExecution,
    isNullValue,
} from '@fincity/kirun-js';
import React, { ReactNode, useEffect, useMemo } from 'react';
import { UIError } from '../util/errorHandling';
import { generateColor } from '../colors';
import { shortUUID } from '../util/shortUUID';

interface ExecutionGraphLinesProps {
    executionPlan: ExecutionGraph<string, StatementExecution> | UIError | undefined;
    designerRef: React.RefObject<HTMLDivElement>;
    rawDef: any;
    selectedStatements: Map<string, boolean>;
    menu: any;
    setSelectedStatements: React.Dispatch<React.SetStateAction<Map<string, boolean>>>;
    functionRepository: Repository<Function>;
    showMenu: React.Dispatch<any>;
    stores?: Array<string>;
    hideArguments?: boolean;
    showStores?: boolean;
    showParamValues?: boolean;
}

const STEP_REGEX =
    /Steps\.([a-zA-Z0-9\-]{1,})\.([a-zA-Z0-9\-]{1,})([\.]{0,}[a-zA-Z0-9\-]{1,})+/g;

export default function ExecutionGraphLines({
    executionPlan,
    designerRef,
    rawDef,
    selectedStatements,
    menu,
    setSelectedStatements,
    functionRepository,
    showMenu,
    stores,
    hideArguments,
    showStores,
    showParamValues,
}: ExecutionGraphLinesProps) {
    const [sid, setSid] = React.useState(shortUUID());
    useEffect(() => {
        setTimeout(() => setSid(shortUUID()), 10);
    }, [showStores, showParamValues]);

    const nodeMap =
        executionPlan instanceof ExecutionGraph
            ? executionPlan?.getNodeMap()
            : new Map<string, GraphVertex<string, StatementExecution>>();

    const regexMap = useMemo(() => {
        const rMap = new Map<string, RegExp>([['Steps', STEP_REGEX]]);
        if (!stores?.length || !showStores) return rMap;
        stores.forEach((s) => rMap.set(s, new RegExp(`${s}\\.[a-zA-Z0-9\-]{1,}`, 'g')));
        return rMap;
    }, [stores, showStores]);

    const [signatures, setSignatures] = React.useState<Map<string, FunctionSignature>>(new Map());

    useEffect(() => {
        if (!nodeMap.size) return;

        (async () => {
            const signatures = new Map<string, FunctionSignature>();

            for (const vertex of Array.from(nodeMap.values())) {
                const statement = vertex.getData().getStatement();
                const func = await functionRepository.find(
                    statement.getNamespace(),
                    statement.getName(),
                );
                if (!func) continue;
                signatures.set(statement.getStatementName(), func.getSignature());
            }
            setSignatures(signatures);
        })();
    }, [setSignatures, functionRepository, nodeMap]);

    if (!(executionPlan instanceof ExecutionGraph) || !designerRef.current) return <></>;

    let gradients: Map<string, ReactNode> = new Map();
    const lines = Array.from(nodeMap.values()).flatMap((v) => {
        const statement = v.getData().getStatement();
        const depNode = document.getElementById(
            `eventNode_dependentNode_${statement.getStatementName()}`,
        );
        if (!depNode) return [];
        const toColor = generateColor(statement.getNamespace(), statement.getName());
        const array: Array<ReactNode> = Array.from(statement.getDependentStatements().entries())
            .filter((e) => e[1])
            .map((e) => e[0])
            .map((e) => {
                const names = e.split('.');
                if (names.length < 3) return;
                const fromNode = document.getElementById(
                    names.length > 3
                        ? `eventParameter_${names[1]}_${names[2]}_${names[3]}`
                        : `eventNode_${names[1]}_${names[2]}`,
                );
                if (!rawDef.steps) return;
                if (!fromNode || !rawDef.steps[names[1]]) return;
                const fromColor = generateColor(
                    rawDef.steps[names[1]].namespace,
                    rawDef.steps[names[1]].name,
                );
                makeGradients(fromColor, toColor, gradients);
                return lineFrom(
                    'dependent' + statement.getStatementName(),
                    fromNode,
                    depNode,
                    designerRef.current!,
                    fromColor,
                    toColor,
                    {
                        className: `_connector ${
                            selectedStatements.get(statement.getStatementName()) ||
                            selectedStatements.get(names[1]) ||
                            (menu?.type === 'dependent' &&
                                menu.value.statementName === statement.getStatementName() &&
                                menu.value.dependency === e)
                                ? '_selected'
                                : ''
                        }`,
                        onClick: () => {
                            setSelectedStatements(
                                new Map<string, boolean>([
                                    [statement.getStatementName(), true],
                                    [names[1], true],
                                ]),
                            );
                        },
                        onContextMenu: (ev: MouseEvent) => {
                            ev.preventDefault();
                            ev.stopPropagation();
                            const parentRect = designerRef.current!.getBoundingClientRect();
                            const magnification =
                                parentRect.width / (designerRef.current!.offsetWidth ?? 1);
                            showMenu({
                                position: {
                                    left: (ev.clientX - parentRect.left) / magnification,
                                    top: (ev.clientY - parentRect.top) / magnification,
                                },
                                type: 'dependent',
                                value: {
                                    statementName: statement.getStatementName(),
                                    dependency: e,
                                },
                            });
                        },
                    },
                    true,
                );
            });

        const functionSignature = signatures.get(statement.getStatementName());
        if (!functionSignature) return array;

        const redundancyCheck = new Set<string>();
        const inLines = Array.from(functionSignature.getParameters().values() ?? []).map(
            (p: Parameter) => {
                const paramValue = statement.getParameterMap()?.get(p.getParameterName());
                if (!paramValue) return undefined;

                const toNode = document.getElementById(
                    `paramNode_${statement.getStatementName()}_${p.getParameterName()}`,
                );
                if (!toNode) return undefined;

                const toColor = generateColor(statement.getNamespace(), statement.getName());

                return Array.from(paramValue.values() ?? []).flatMap((pr) => {
                    if (!pr) return undefined;
                    if (pr.getType() === ParameterReferenceType.EXPRESSION) {
                        let expression = pr.getExpression();
                        if (!expression) return undefined;

                        return makeLineFromExpression(
                            expression,
                            rawDef,
                            toColor,
                            gradients,
                            toNode,
                            statement.getStatementName(),
                            designerRef,
                            selectedStatements,
                            setSelectedStatements,
                            regexMap,
                            redundancyCheck,
                        );
                    } else if (!isNullValue(pr.getValue())) {
                        const value = pr.getValue();

                        if (typeof value === 'string') {
                            return makeLineFromExpression(
                                value,
                                rawDef,
                                toColor,
                                gradients,
                                toNode,
                                statement.getStatementName(),
                                designerRef,
                                selectedStatements,
                                setSelectedStatements,
                                regexMap,
                                redundancyCheck,
                            );
                        } else {
                            const ll = new LinkedList<any>();
                            const set = new Set<string>();
                            ll.push(value);
                            let v: any;
                            const lines = [];
                            while (ll.size() > 0) {
                                v = ll.pop();
                                if (Array.isArray(v)) {
                                    ll.addAll(v);
                                } else if (typeof v === 'object' && v !== null) {
                                    ll.addAll(Array.from(Object.values(v)));
                                } else if (typeof v === 'string' && !set.has(v)) {
                                    set.add(v);
                                    lines.push(
                                        ...makeLineFromExpression(
                                            v,
                                            rawDef,
                                            toColor,
                                            gradients,
                                            toNode,
                                            statement.getStatementName(),
                                            designerRef,
                                            selectedStatements,
                                            setSelectedStatements,
                                            regexMap,
                                            redundancyCheck,
                                        ),
                                    );
                                }
                            }
                            return lines;
                        }
                    }
                    return undefined;
                });
            },
        );
        array.push(...inLines);

        return array;
    });

    return <svg className="_linesSvg">{lines}</svg>;
}

function makeLineFromExpression(
    expression: string,
    rawDef: any,
    toColor: string,
    gradients: Map<string, React.ReactNode>,
    toNode: HTMLElement,
    statementName: string,
    designerRef: React.RefObject<HTMLDivElement>,
    selectedStatements: Map<string, boolean>,
    setSelectedStatements: (statements: Map<string, boolean>) => void,
    regexMap: Map<string, RegExp>,
    redundancyCheck: Set<string>,
    props: any = {},
): ReactNode[] {
    const lines: ReactNode[] = Array.from(regexMap)
        .flatMap((e) => {
            let arr = new Array<[string, string[]]>();

            let matches = expression.match(e[1]);
            if (matches?.length) {
                arr.push([e[0], Array.from(matches)]);
            }

            return arr;
        })
        .flatMap(([type, exprs]) =>
            exprs.map((e) => {
                const names = e.split('.');
                if (names.length < 2) return undefined;
                if (!rawDef.steps) return undefined;
                const fromId =
                    type === 'Steps'
                        ? names.length > 3
                            ? `eventParameter_${names[1]}_${names[2]}_${names[3]}`
                            : `eventNode_${names[1]}_${names[2]}`
                        : `_storeNode_${type}`;
                if (redundancyCheck.has(fromId + '-' + toNode.id)) return undefined;
                redundancyCheck.add(fromId + '-' + toNode.id);
                const fromNode = document.getElementById(fromId);

                if (!fromNode || (type === 'Steps' && !rawDef.steps[names[1]])) return undefined;

                const fromColor =
                    type === 'Steps'
                        ? generateColor(
                              rawDef.steps[names[1]].namespace,
                              rawDef.steps[names[1]].name,
                          )
                        : generateColor('stor', type);
                makeGradients(fromColor, toColor, gradients);
                return lineFrom(
                    e + ' ' + statementName,
                    fromNode,
                    toNode,
                    designerRef.current!,
                    fromColor,
                    toColor,
                    {
                        ...props,

                        className: `_connector ${
                            selectedStatements.get(statementName) ||
                            selectedStatements.get(names[1])
                                ? '_selected'
                                : ''
                        }`,

                        onClick: () => {
                            setSelectedStatements(
                                new Map(
                                    type === 'Steps'
                                        ? [
                                              [statementName, true],
                                              [names[1], true],
                                          ]
                                        : [[statementName, true]],
                                ),
                            );
                        },
                    },
                    false,
                );
            }),
        );

    return lines;
}

function makeGradients(fromColor: string, toColor: string, gradients: Map<string, ReactNode>) {
    const gradName = `grad_${fromColor}_${toColor}`;
    const revGradNam = `grad_${toColor}_${fromColor}`;
    if (gradients.has(gradName)) return;

    gradients.set(
        gradName,
        <linearGradient key={gradName} id={gradName} x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stopColor={`#${fromColor}`} />
            <stop offset="100%" stopColor={`#${toColor}`} />
        </linearGradient>,
    );

    gradients.set(
        revGradNam,
        <linearGradient key={revGradNam} id={revGradNam} x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stopColor={`#${toColor}`} />
            <stop offset="100%" stopColor={`#${fromColor}`} />
        </linearGradient>,
    );
}

function lineFrom(
    name: string,
    fromNode: HTMLElement,
    toNode: HTMLElement,
    parentElement: HTMLElement,
    fromColor: string,
    toColor: string,
    props: any = {},
    isNodeOnTop = false,
): ReactNode {
    if (!fromNode || !toNode || !fromNode.parentElement || !toNode.parentElement || !parentElement)
        return <></>;
    const fromRect = fromNode.getBoundingClientRect();
    const toRect = toNode.getBoundingClientRect();
    const parentRect = parentElement.getBoundingClientRect();

    const magnification = parentRect.width / parentElement.offsetWidth;
    const sx = (fromRect.left - parentRect.left) / magnification;
    const sy = (fromRect.top - parentRect.top) / magnification;
    const ex = (toRect.left - parentRect.left) / magnification;
    const ey = (toRect.top - parentRect.top) / magnification;

    let dPath = `M ${sx} ${sy} `;
    if (isNodeOnTop)
        dPath += `C ${sx + Math.abs(ex - sx) / 1.5} ${sy} ${ex} ${
            ey - Math.abs(ey - sy) / 1.2
        } ${ex} ${ey}`;
    else {
        if (Math.abs(sy - ey) < 0.4) {
            props = { ...props, className: `${props.className ?? ''} _straight` };
            dPath += `L ${ex} ${sy} L ${ex} ${ey + 1} L ${sx} ${ey + 1} Z`;
        } else if (Math.abs(sx - ex) < 0.4) {
            props = { ...props, className: `${props.className ?? ''} _straight` };
            dPath += `L ${ex + 1} ${sy} L ${ex + 1} ${ey} L ${sx} ${ey} Z`;
        } else {
            const xdiff = ex - sx;
            const ydiff = ey - sy;
            dPath += `Q ${sx + (xdiff * (ex - sx < 0 ? -1 : 1)) / 3} ${sy} ${sx + xdiff / 2} ${
                sy + ydiff / 2
            } T ${ex} ${ey}`;
        }
    }
    return (
        <path
            key={`line_${sx}_${sy}_${ex}_${ey}_${name}`}
            d={dPath}
            role="button"
            {...props}
        />
    );
}
