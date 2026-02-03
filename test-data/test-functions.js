#!/usr/bin/env node

/**
 * Test script to verify DSL compilation/decompilation with downloaded functions
 * Usage: node test-functions.js
 */

const fs = require('fs');
const path = require('path');

// Import DSL compiler from kirun-js (built version)
const kirunPath = path.join(__dirname, '../kirun-js/dist/index.js');

let DSLCompiler;
try {
    const kirun = require(kirunPath);
    DSLCompiler = kirun.DSLCompiler;
    console.log('Loaded DSLCompiler from kirun-js');
} catch (error) {
    console.error('Failed to load DSLCompiler. Make sure kirun-js is built.');
    console.error('Run: cd ../kirun-js && npm run build');
    process.exit(1);
}

const FUNCTIONS_DIR = path.join(__dirname, 'functions');
const RESULTS_DIR = path.join(__dirname, 'test-results');

/**
 * Load all function files
 */
function loadAllFunctions() {
    const allFunctions = [];

    const subdirs = ['ui-functions', 'page-event-functions', 'core-functions'];

    for (const subdir of subdirs) {
        const dir = path.join(FUNCTIONS_DIR, subdir);

        if (!fs.existsSync(dir)) {
            console.log(`Skipping ${subdir} (not found)`);
            continue;
        }

        const files = fs.readdirSync(dir).filter(f => f.endsWith('.json'));

        for (const file of files) {
            const filePath = path.join(dir, file);
            const content = fs.readFileSync(filePath, 'utf-8');
            const func = JSON.parse(content);

            allFunctions.push({
                file: path.join(subdir, file),
                data: func
            });
        }

        console.log(`Loaded ${files.length} functions from ${subdir}`);
    }

    return allFunctions;
}

/**
 * Extract the actual function definition from the stored format
 * All functions are wrapped in { _id, definition: {...} }
 */
function extractDefinition(func) {
    if (func.definition) {
        return func.definition;
    }
    return func;
}

/**
 * Compare two definitions and return the first difference found, or null if they match
 */
function compareDefinitions(original, recompiled) {
    // Compare name
    if (original.name !== recompiled.name) {
        return `Name mismatch: "${original.name}" !== "${recompiled.name}"`;
    }

    // Compare namespace
    if ((original.namespace || '') !== (recompiled.namespace || '')) {
        return `Namespace mismatch: "${original.namespace}" !== "${recompiled.namespace}"`;
    }

    // Compare parameters
    const origParams = original.parameters || {};
    const recompParams = recompiled.parameters || {};
    const origParamNames = Object.keys(origParams);
    const recompParamNames = Object.keys(recompParams);

    if (origParamNames.length !== recompParamNames.length) {
        return `Parameter count mismatch: ${origParamNames.length} !== ${recompParamNames.length}`;
    }

    for (const paramName of origParamNames) {
        if (!recompParams[paramName]) {
            return `Missing parameter: "${paramName}"`;
        }
    }

    // Compare events
    const origEvents = original.events || {};
    const recompEvents = recompiled.events || {};
    const origEventNames = Object.keys(origEvents);
    const recompEventNames = Object.keys(recompEvents);

    if (origEventNames.length !== recompEventNames.length) {
        return `Event count mismatch: ${origEventNames.length} !== ${recompEventNames.length}`;
    }

    for (const eventName of origEventNames) {
        if (!recompEvents[eventName]) {
            return `Missing event: "${eventName}"`;
        }
    }

    // Compare steps
    const origSteps = original.steps || {};
    const recompSteps = recompiled.steps || {};
    const origStepNames = Object.keys(origSteps);
    const recompStepNames = Object.keys(recompSteps);

    if (origStepNames.length !== recompStepNames.length) {
        return `Step count mismatch: ${origStepNames.length} !== ${recompStepNames.length}`;
    }

    for (const stepName of origStepNames) {
        const origStep = origSteps[stepName];
        const recompStep = recompSteps[stepName];

        if (!recompStep) {
            return `Missing step: "${stepName}"`;
        }

        // Compare statement name
        if (origStep.statementName !== recompStep.statementName) {
            return `Step "${stepName}" statementName mismatch: "${origStep.statementName}" !== "${recompStep.statementName}"`;
        }

        // Compare step function reference
        if (origStep.name !== recompStep.name) {
            return `Step "${stepName}" function name mismatch: "${origStep.name}" !== "${recompStep.name}"`;
        }

        if (origStep.namespace !== recompStep.namespace) {
            return `Step "${stepName}" namespace mismatch: "${origStep.namespace}" !== "${recompStep.namespace}"`;
        }

        // Compare parameter maps
        const origParamMap = origStep.parameterMap || {};
        const recompParamMap = recompStep.parameterMap || {};
        const origParamKeys = Object.keys(origParamMap);
        const recompParamKeys = Object.keys(recompParamMap);

        if (origParamKeys.length !== recompParamKeys.length) {
            return `Step "${stepName}" parameter count mismatch: ${origParamKeys.length} !== ${recompParamKeys.length}`;
        }

        for (const paramKey of origParamKeys) {
            if (!recompParamMap[paramKey]) {
                return `Step "${stepName}" missing parameter: "${paramKey}"`;
            }

            // Compare parameter values
            const origValues = origParamMap[paramKey];
            const recompValues = recompParamMap[paramKey];

            const origValueKeys = Object.keys(origValues || {});
            const recompValueKeys = Object.keys(recompValues || {});

            if (origValueKeys.length !== recompValueKeys.length) {
                return `Step "${stepName}" param "${paramKey}" value count mismatch: ${origValueKeys.length} !== ${recompValueKeys.length}`;
            }

            // Compare each value entry by index (keys are UUIDs and will differ)
            const origValuesArray = origValueKeys.map(k => origValues[k]);
            const recompValuesArray = recompValueKeys.map(k => recompValues[k]);

            for (let i = 0; i < origValuesArray.length; i++) {
                const origVal = origValuesArray[i];
                const recompVal = recompValuesArray[i];

                // Normalize the values for comparison
                const origContent = origVal.type === 'EXPRESSION' ? origVal.expression : origVal.value;
                const recompContent = recompVal.type === 'EXPRESSION' ? recompVal.expression : recompVal.value;

                // Compare content semantically
                // VALUE with simple string and EXPRESSION with same string are equivalent
                if (origVal.type === recompVal.type) {
                    // Same type - compare directly
                    if (origVal.type === 'EXPRESSION') {
                        if ((origVal.expression || '') !== (recompVal.expression || '')) {
                            return `Step "${stepName}" param "${paramKey}" expression mismatch: "${origVal.expression}" !== "${recompVal.expression}"`;
                        }
                    } else if (origVal.type === 'VALUE') {
                        if (JSON.stringify(origVal.value) !== JSON.stringify(recompVal.value)) {
                            return `Step "${stepName}" param "${paramKey}" value mismatch: ${JSON.stringify(origVal.value)} !== ${JSON.stringify(recompVal.value)}`;
                        }
                    }
                } else {
                    // Different types - check if semantically equivalent
                    // VALUE with primitive string vs EXPRESSION with same string
                    if (typeof origContent === 'string' && typeof recompContent === 'string') {
                        if (origContent !== recompContent) {
                            return `Step "${stepName}" param "${paramKey}" content mismatch: "${origContent}" !== "${recompContent}"`;
                        }
                    } else {
                        // Complex values need exact type match
                        return `Step "${stepName}" param "${paramKey}" type mismatch: "${origVal.type}" !== "${recompVal.type}"`;
                    }
                }
            }
        }
    }

    return null; // No differences found
}

/**
 * Test a single function
 */
async function testFunction(func) {
    const definition = extractDefinition(func.data);

    const result = {
        name: definition.name || 'unnamed',
        file: func.file,
        success: false,
        error: null,
        stepCount: definition.steps ? Object.keys(definition.steps).length : 0,
        hasParams: !!definition.parameters && Object.keys(definition.parameters).length > 0,
        hasEvents: !!definition.events && Object.keys(definition.events).length > 0,
        stages: {
            decompile: false,
            validate: false,
            compile: false,
            roundtrip: false
        }
    };

    try {
        // Stage 1: Decompile JSON to DSL text (async)
        const dslText = await DSLCompiler.decompile(definition);
        result.dslTextLength = dslText.length;
        result.stages.decompile = true;

        // Stage 2: Validate DSL text
        const validation = DSLCompiler.validate(dslText);
        result.stages.validate = validation.valid;

        if (!validation.valid) {
            result.error = `Validation failed: ${validation.errors[0]?.message}`;
            return result;
        }

        // Stage 3: Compile DSL text back to JSON
        const recompiled = DSLCompiler.compile(dslText);
        result.stages.compile = true;

        // Stage 4: Verify round-trip with comprehensive comparison
        const diff = compareDefinitions(definition, recompiled);
        if (diff === null) {
            result.stages.roundtrip = true;
            result.success = true;
        } else {
            result.error = `Round-trip: ${diff}`;
        }

    } catch (error) {
        result.error = error.message;
    }

    return result;
}

/**
 * Run all tests
 */
async function runAllTests() {
    console.log('\n=== Starting DSL Tests ===\n');

    const functions = loadAllFunctions();

    if (functions.length === 0) {
        console.error('No functions found!');
        console.error('Run: npm run download');
        process.exit(1);
    }

    console.log(`Testing ${functions.length} functions...\n`);

    const results = [];
    const stats = {
        total: functions.length,
        success: 0,
        failed: 0,
        byStage: {
            decompile: 0,
            validate: 0,
            compile: 0,
            roundtrip: 0
        }
    };

    for (let i = 0; i < functions.length; i++) {
        const func = functions[i];
        const result = await testFunction(func);
        results.push(result);

        if (result.success) {
            stats.success++;
            console.log(`✓ [${i + 1}/${functions.length}] ${result.name} (${result.stepCount} steps)`);
        } else {
            stats.failed++;
            console.log(`✗ [${i + 1}/${functions.length}] ${result.name} - ${result.error}`);
        }

        // Track which stage each function reached
        for (const [stage, passed] of Object.entries(result.stages)) {
            if (passed) stats.byStage[stage]++;
        }
    }

    // Save detailed results
    if (!fs.existsSync(RESULTS_DIR)) {
        fs.mkdirSync(RESULTS_DIR, { recursive: true });
    }

    const resultsPath = path.join(RESULTS_DIR, `results_${Date.now()}.json`);
    fs.writeFileSync(resultsPath, JSON.stringify(results, null, 2));

    // Print summary
    console.log('\n=== Test Summary ===');
    console.log(`Total functions: ${stats.total}`);
    console.log(`Success: ${stats.success} (${(stats.success / stats.total * 100).toFixed(1)}%)`);
    console.log(`Failed: ${stats.failed} (${(stats.failed / stats.total * 100).toFixed(1)}%)`);

    console.log('\nBy stage:');
    console.log(`  Decompile: ${stats.byStage.decompile}/${stats.total}`);
    console.log(`  Validate: ${stats.byStage.validate}/${stats.total}`);
    console.log(`  Compile: ${stats.byStage.compile}/${stats.total}`);
    console.log(`  Round-trip: ${stats.byStage.roundtrip}/${stats.total}`);

    console.log(`\nDetailed results saved to: ${resultsPath}`);

    // Print failed functions
    const failed = results.filter(r => !r.success);
    if (failed.length > 0 && failed.length <= 10) {
        console.log('\n=== Failed Functions ===');
        for (const result of failed) {
            console.log(`\n${result.name} (${result.file})`);
            console.log(`  Error: ${result.error}`);
            console.log(`  Stages: ${Object.entries(result.stages).filter(([, v]) => v).map(([k]) => k).join(', ')}`);
        }
    }

    return stats.success === stats.total ? 0 : 1;
}

// Run if executed directly
if (require.main === module) {
    runAllTests().then(exitCode => {
        process.exit(exitCode);
    }).catch(error => {
        console.error('Fatal error:', error);
        process.exit(1);
    });
}

module.exports = { testFunction, loadAllFunctions };
