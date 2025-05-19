import * as React from 'react';
import { createRoot } from 'react-dom/client';
import { App } from './App'
//
// const root = createRoot(document.getElementById('main-div'))
// root.render(<App />)

const container = document.getElementById('main-div');

if (container) {
    const root = createRoot(container);
    root.render(<App />);
} else {
    console.error("element with ID 'main-div' not found in DOM.");
}