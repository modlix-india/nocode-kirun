import { FunctionDefinition } from '@fincity/kirun-js';

// Regex to find Steps references in expressions
const STEPS_REGEX = /Steps\.([a-zA-Z0-9_-]+)\./g;

function extractStepDependencies(value: any, stepNames: Set<string>, deps: Set<string>): void {
    if (typeof value === 'string') {
        let match;
        STEPS_REGEX.lastIndex = 0;
        while ((match = STEPS_REGEX.exec(value)) !== null) {
            const depName = match[1];
            if (stepNames.has(depName)) {
                deps.add(depName);
            }
        }
    } else if (Array.isArray(value)) {
        for (const item of value) {
            extractStepDependencies(item, stepNames, deps);
        }
    } else if (value && typeof value === 'object') {
        for (const key of Object.keys(value)) {
            extractStepDependencies(value[key], stepNames, deps);
        }
    }
}

export function autoLayoutFunctionDefinition(
    funcDef: FunctionDefinition,
    nodeWidth: number,
    nodeHeights: Map<string, number> | number,
    gap: number,
): Map<string, { left: number; top: number }> {
    const getNodeHeight = (name: string): number => {
        if (typeof nodeHeights === 'number') return nodeHeights;
        return nodeHeights.get(name) ?? 180;
    };
    const steps = funcDef.getSteps();
    const positions = new Map<string, { left: number; top: number }>();

    if (!steps.size) return positions;

    const stepNames = new Set(steps.keys());

    const dependsOn = new Map<string, Set<string>>();
    const dependedBy = new Map<string, Set<string>>();

    for (const [name] of steps) {
        dependsOn.set(name, new Set());
        dependedBy.set(name, new Set());
    }

    for (const [name, statement] of steps) {
        const deps = dependsOn.get(name)!;

        for (const [depPath] of statement.getDependentStatements()) {
            const parts = depPath.split('.');
            if (parts.length >= 2 && parts[0] === 'Steps') {
                const depName = parts[1];
                if (stepNames.has(depName) && depName !== name) {
                    deps.add(depName);
                }
            }
        }

        const paramMap = statement.getParameterMap();
        for (const [, paramRefs] of paramMap) {
            for (const [, paramRef] of paramRefs) {
                const expr = paramRef.getExpression();
                if (expr) {
                    extractStepDependencies(expr, stepNames, deps);
                }
                const val = paramRef.getValue();
                if (val !== undefined && val !== null) {
                    extractStepDependencies(val, stepNames, deps);
                }
            }
        }

        deps.delete(name);

        for (const depName of deps) {
            dependedBy.get(depName)?.add(name);
        }
    }

    const layers: string[][] = [];
    const inDegree = new Map<string, number>();
    const assigned = new Set<string>();

    for (const [name] of steps) {
        inDegree.set(name, dependsOn.get(name)?.size ?? 0);
    }

    while (assigned.size < steps.size) {
        const layer: string[] = [];
        for (const [name] of steps) {
            if (!assigned.has(name) && (inDegree.get(name) ?? 0) === 0) {
                layer.push(name);
            }
        }

        if (layer.length === 0) {
            let minDep = Infinity;
            let minNode = '';
            for (const [name] of steps) {
                if (!assigned.has(name)) {
                    const deg = inDegree.get(name) ?? 0;
                    if (deg < minDep) {
                        minDep = deg;
                        minNode = name;
                    }
                }
            }
            if (minNode) layer.push(minNode);
        }

        if (layer.length === 0) break;

        layer.sort((a, b) => a.localeCompare(b));
        layers.push(layer);

        for (const name of layer) {
            assigned.add(name);
            for (const dependent of dependedBy.get(name) ?? []) {
                inDegree.set(dependent, (inDegree.get(dependent) ?? 1) - 1);
            }
        }
    }

    const startX = 50;
    const startY = 50;

    for (let layerIdx = 0; layerIdx < layers.length; layerIdx++) {
        const layer = layers[layerIdx];
        const x = startX + layerIdx * (nodeWidth + gap);

        if (layerIdx === 0) {
            let currentY = startY;
            for (let nodeIdx = 0; nodeIdx < layer.length; nodeIdx++) {
                const name = layer[nodeIdx];
                positions.set(name, { left: x, top: currentY });
                currentY += getNodeHeight(name) + gap;
            }
        } else {
            const nodeYPositions: { name: string; targetY: number }[] = [];

            for (const name of layer) {
                const deps = dependsOn.get(name) ?? new Set();
                let targetY = startY;

                if (deps.size > 0) {
                    let sumY = 0;
                    let count = 0;
                    for (const depName of deps) {
                        const depPos = positions.get(depName);
                        if (depPos) {
                            sumY += depPos.top;
                            count++;
                        }
                    }
                    if (count > 0) {
                        targetY = sumY / count;
                    }
                }

                nodeYPositions.push({ name, targetY });
            }

            nodeYPositions.sort((a, b) => a.targetY - b.targetY);

            let lastY = startY;
            let lastNodeName: string | null = null;
            for (const { name, targetY } of nodeYPositions) {
                const minY = lastNodeName ? lastY + getNodeHeight(lastNodeName) + gap : lastY;
                const y = Math.max(targetY, minY);
                positions.set(name, { left: x, top: y });
                lastY = y;
                lastNodeName = name;
            }
        }
    }

    return positions;
}
