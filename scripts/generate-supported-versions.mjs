#!/usr/bin/env node
import { readFileSync, readdirSync, writeFileSync, existsSync } from 'node:fs';
import { join } from 'node:path';

const inputDir = process.argv[2] ?? 'matrix-results';
const outputFile = process.argv[3] ?? 'SUPPORTED_VERSIONS.md';

if (!existsSync(inputDir)) {
    console.error(`input dir not found: ${inputDir}`);
    process.exit(1);
}

const results = [];
for (const file of readdirSync(inputDir)) {
    if (!file.endsWith('.json')) continue;
    try {
        const data = JSON.parse(readFileSync(join(inputDir, file), 'utf8'));
        results.push(data);
    } catch (e) {
        console.warn(`Skipping ${file}: ${e.message}`);
    }
}

results.sort((a, b) =>
    a.loader.localeCompare(b.loader) ||
    a.mcVersion.localeCompare(b.mcVersion, undefined, { numeric: true })
);

const loaders = [...new Set(results.map(r => r.loader))];
const versions = [...new Set(results.map(r => r.mcVersion))].sort(
    (a, b) => a.localeCompare(b, undefined, { numeric: true })
);

const cell = (loader, version) => {
    const r = results.find(x => x.loader === loader && x.mcVersion === version);
    if (!r) return '?';
    return r.status === 'pass' ? '✓' : r.status === 'skipped' ? '⚠︎' : '✗';
};

const lines = [];
lines.push('# Supported Versions');
lines.push('');
lines.push('| Loader \\ MC | ' + versions.join(' | ') + ' |');
lines.push('|' + ['---', ...versions.map(() => '---')].join('|') + '|');
for (const loader of loaders) {
    lines.push(`| **${loader}** | ` + versions.map(v => cell(loader, v)).join(' | ') + ' |');
}
lines.push('');
lines.push('Legend: ✓ pass · ✗ fail · ⚠︎ skipped · ? not tested');
lines.push('');
lines.push('Automatically Generated, do not edit')
lines.push(`Last updated: ${new Date().toISOString()}`);

writeFileSync(outputFile, lines.join('\n'));
console.log(`Wrote ${outputFile} (${results.length} results)`);