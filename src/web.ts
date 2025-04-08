import { WebPlugin } from '@capacitor/core';

import type { PdfPrinterPlugin } from './definitions';

export class PdfPrinterWeb extends WebPlugin implements PdfPrinterPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
