// TypeScript 6 with "moduleResolution": "bundler" requires a type declaration
// for side-effect stylesheet imports (TS2882). Parcel handles the actual CSS at
// build time; this only tells tsc the imports are legitimate.
declare module '*.css';
