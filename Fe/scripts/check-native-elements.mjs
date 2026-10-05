import { readdirSync, readFileSync } from 'node:fs'
import { join, relative } from 'node:path'

const sourceRoot = join(process.cwd(), 'src')
const allowedRoot = join(sourceRoot, 'components', 'base')
const restricted = /<(button|label|input|select|textarea|table|dialog)(\s|>)/g
const violations = []

function walk(directory) {
  for (const entry of readdirSync(directory, { withFileTypes: true })) {
    const path = join(directory, entry.name)
    if (entry.isDirectory()) walk(path)
    if (!entry.isFile() || !path.endsWith('.vue') || path.startsWith(allowedRoot)) continue
    const source = readFileSync(path, 'utf8')
    for (const match of source.matchAll(restricted)) {
      const line = source.slice(0, match.index).split('\n').length
      violations.push(`${relative(process.cwd(), path)}:${line} uses <${match[1]}> outside components/base`)
    }
  }
}

walk(sourceRoot)
if (violations.length) {
  console.error(violations.join('\n'))
  process.exit(1)
}
console.log('UI native-element guard passed: restricted controls only live in components/base.')
