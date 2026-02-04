#!/usr/bin/env node

/**
 * Standalone script to download KIRun functions from MongoDB for testing
 * Usage: node download-functions.js
 */

const { MongoClient } = require('mongodb');
const fs = require('fs');
const path = require('path');

// MongoDB connection strings
const UI_DB_URI = 'mongodb://admin:xxxxxx@dev-mongo:27017/ui?retryWrites=true&w=majority&authSource=admin';
const CORE_DB_URI = 'mongodb://admin:xxxxx@dev-mongo:27017/core?retryWrites=true&w=majority&authSource=admin';

// Output directory
const OUTPUT_DIR = path.join(__dirname, 'functions');

/**
 * Download all functions from a collection
 */
async function downloadFunctions(uri, dbName, collectionName) {
    console.log(`\nConnecting to ${dbName}.${collectionName}...`);

    const client = new MongoClient(uri);
    await client.connect();

    const db = client.db(dbName);
    const collection = db.collection(collectionName);

    // Get all documents
    const docs = await collection.find({}).toArray();
    console.log(`Found ${docs.length} documents`);

    await client.close();

    return docs;
}

/**
 * Download event functions from pages collection
 */
async function downloadPageEventFunctions(uri, dbName) {
    console.log(`\nConnecting to ${dbName}.pages for event functions...`);

    const client = new MongoClient(uri);
    await client.connect();

    const db = client.db(dbName);
    const collection = db.collection('page');

    // Get pages with eventFunctions
    const pages = await collection.find({
        eventFunctions: { $exists: true, $ne: {} }
    }).toArray();

    console.log(`Found ${pages.length} pages with event functions`);

    // Extract all event functions
    const allFunctions = [];
    for (const page of pages) {
        if (page.eventFunctions && typeof page.eventFunctions === 'object') {
            for (const [funcName, funcDef] of Object.entries(page.eventFunctions)) {
                allFunctions.push({
                    _id: `page_${page._id}_${funcName}`,
                    name: funcName,
                    pageId: page._id?.toString(),
                    pageName: page.name,
                    source: 'page_event',
                    definition: {...funcDef}
                });
            }
        }
    }

    console.log(`Extracted ${allFunctions.length} event functions`);

    await client.close();

    return allFunctions;
}

/**
 * Convert MongoDB ObjectId buffer to hex string
 */
function objectIdToString(obj) {
    if (obj && typeof obj === 'object' && obj.buffer && typeof obj.buffer === 'object') {
        // Check if it looks like an ObjectId buffer (12 bytes)
        const keys = Object.keys(obj.buffer);
        if (keys.length === 12 && keys.every(k => !isNaN(k))) {
            // Convert buffer to hex string
            const bytes = [];
            for (let i = 0; i < 12; i++) {
                bytes.push(obj.buffer[i].toString(16).padStart(2, '0'));
            }
            return bytes.join('');
        }
    }
    return null;
}

/**
 * Recursively decode __d-o-t__ back to . in object keys and string values
 * Also converts MongoDB ObjectId buffers to readable hex strings
 */
function decodeDots(obj) {
    if (obj === null || obj === undefined) {
        return obj;
    }

    // Check if this is an ObjectId buffer
    const objectIdString = objectIdToString(obj);
    if (objectIdString) {
        return objectIdString;
    }

    if (typeof obj === 'string') {
        return obj.replace(/__d-o-t__/g, '.');
    }

    if (Array.isArray(obj)) {
        return obj.map(item => decodeDots(item));
    }

    if (typeof obj === 'object') {
        const decoded = {};
        for (const [key, value] of Object.entries(obj)) {
            const decodedKey = key.replace(/__d-o-t__/g, '.');
            decoded[decodedKey] = decodeDots(value);
        }
        return decoded;
    }

    return obj;
}

/**
 * Save functions to individual files
 */
function saveFunctionsToFiles(functions, subdir) {
    const dir = path.join(OUTPUT_DIR, subdir);

    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
    }

    let savedCount = 0;

    for (let i = 0; i < functions.length; i++) {
        const func = functions[i];

        // Decode __d-o-t__ back to . before saving
        const decodedFunc = decodeDots(func);

        const fileName = `func_${i + 1}_${(func.name || 'unnamed').replace(/[^a-zA-Z0-9]/g, '_')}.json`;
        const filePath = path.join(dir, fileName);

        fs.writeFileSync(filePath, JSON.stringify(decodedFunc, null, 2));
        savedCount++;
    }

    console.log(`Saved ${savedCount} functions to ${dir}`);
    return savedCount;
}

/**
 * Create index file with metadata
 */
function createIndexFile(allFunctions) {
    const index = {
        totalFunctions: allFunctions.length,
        sources: {},
        timestamp: new Date().toISOString(),
        functions: []
    };

    for (const func of allFunctions) {
        const source = func.source || 'unknown';

        if (!index.sources[source]) {
            index.sources[source] = 0;
        }
        index.sources[source]++;

        const stepCount = func.definition.steps ? Object.keys(func.definition.steps).length : 0;
        const hasParams = func.definition.parameters && Object.keys(func.definition.parameters).length > 0;
        const hasEvents = func.definition.events && Object.keys(func.definition.events).length > 0;

        index.functions.push({
            id: func._id?.toString(),
            name: func.definition.name || 'unnamed',
            namespace: func.definition.namespace,
            source: source,
            stepCount: stepCount,
            hasParams: hasParams,
            hasEvents: hasEvents,
            complexity: stepCount > 5 ? 'complex' : stepCount > 2 ? 'medium' : 'simple'
        });
    }

    const indexPath = path.join(OUTPUT_DIR, 'index.json');
    fs.writeFileSync(indexPath, JSON.stringify(index, null, 2));
    console.log(`\nCreated index file: ${indexPath}`);

    return index;
}

async function main() {
    console.log('=== KIRun Function Download Script ===\n');

    // Create output directory
    if (!fs.existsSync(OUTPUT_DIR)) {
        fs.mkdirSync(OUTPUT_DIR, { recursive: true });
        console.log(`Created output directory: ${OUTPUT_DIR}`);
    }

    try {
        const allFunctions = [];

        // 1. Download from UI database - functions collection
        console.log('\n--- UI Database: functions collection ---');
        const uiFunctions = await downloadFunctions(UI_DB_URI, 'ui', 'function');
        const uiFuncsWithSource = uiFunctions.map(f => ({ ...f, source: 'ui_function' }));
        allFunctions.push(...uiFuncsWithSource);
        saveFunctionsToFiles(uiFuncsWithSource, 'ui-functions');

        // 2. Download from UI database - pages collection (eventFunctions)
        console.log('\n--- UI Database: page.eventFunctions ---');
        const pageEventFunctions = await downloadPageEventFunctions(UI_DB_URI, 'ui');
        allFunctions.push(...pageEventFunctions);
        saveFunctionsToFiles(pageEventFunctions, 'page-event-functions');

        // 3. Download from Core database - functions collection
        console.log('\n--- Core Database: functions collection ---');
        const coreFunctions = await downloadFunctions(CORE_DB_URI, 'core', 'function');
        const coreFuncsWithSource = coreFunctions.map(f => ({ ...f, source: 'core_function' }));
        allFunctions.push(...coreFuncsWithSource);
        saveFunctionsToFiles(coreFuncsWithSource, 'core-functions');

        // Create index
        const index = createIndexFile(allFunctions);

        // Print summary
        console.log('\n=== Download Complete ===');
        console.log(`Total functions: ${allFunctions.length}`);
        console.log('By source:');
        for (const [source, count] of Object.entries(index.sources)) {
            console.log(`  ${source}: ${count}`);
        }

        console.log('\nBy complexity:');
        const complexity = {
            simple: index.functions.filter(f => f.complexity === 'simple').length,
            medium: index.functions.filter(f => f.complexity === 'medium').length,
            complex: index.functions.filter(f => f.complexity === 'complex').length
        };
        console.log(`  Simple (1-2 steps): ${complexity.simple}`);
        console.log(`  Medium (3-5 steps): ${complexity.medium}`);
        console.log(`  Complex (6+ steps): ${complexity.complex}`);

        console.log(`\nAll functions saved to: ${OUTPUT_DIR}`);

    } catch (error) {
        console.error('\n!!! Error !!!');
        console.error(error.message);
        console.error(error.stack);
        process.exit(1);
    }
}

// Run if executed directly
if (require.main === module) {
    main();
}

module.exports = { downloadFunctions, downloadPageEventFunctions };
