import { registerPlugin } from '@capacitor/core';

import type { PdfPrinterPlugin } from './definitions';

const PdfPrinter = registerPlugin<PdfPrinterPlugin>('PdfPrinter', {
  web: () => import('./web').then((m) => new m.PdfPrinterWeb()),
});

export * from './definitions';
export { PdfPrinter };
