import { WebPlugin } from '@capacitor/core';

import type { PdfPrinterPlugin } from './definitions';

export class PdfPrinterWeb extends WebPlugin implements PdfPrinterPlugin {
  async printPDF(options: { url: string }): Promise<null> {
    console.log('PDF URL:', options.url);
    console.log('Use android to print PDF');
    return null;
  }
}
