import fs from 'node:fs';
import path from 'node:path';
import vm from 'node:vm';
import { fileURLToPath } from 'node:url';
import ts from 'typescript';
import React from 'react';
import * as jsxRuntime from 'react/jsx-runtime';

export const webRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
export function loadModule(relative, mocks = {}) {
  const file = path.join(webRoot, relative);
  const output = ts.transpileModule(fs.readFileSync(file, 'utf8'), {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020, jsx: ts.JsxEmit.ReactJSX, esModuleInterop: true },
  }).outputText;
  const loaded = { exports: {} };
  vm.runInNewContext(output, {
    module: loaded, exports: loaded.exports,
    require: (name) => {
      if (name in mocks) return mocks[name];
      if (name === 'react') return React;
      if (name === 'react/jsx-runtime') return jsxRuntime;
      if (name.startsWith('@/')) {
        const base = `src/${name.slice(2)}`;
        const extension = fs.existsSync(path.join(webRoot, base + '.ts')) ? '.ts' : '.tsx';
        return loadModule(base + extension, mocks);
      }
      throw new Error(`Unexpected test dependency: ${name}`);
    },
  }, { filename: file });
  return loaded.exports;
}
