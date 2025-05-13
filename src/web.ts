import { WebPlugin } from '@capacitor/core';

import type { PdfPrinterPlugin } from './definitions';

export class PdfPrinterWeb extends WebPlugin implements PdfPrinterPlugin {
  async printPDF(options: { content: string; contentType: string; paperType: string; layout: string }): Promise<null> {
    console.log('This feature is specifically implemented for Android platform');
    console.log('contentType', options.contentType);
    console.log('content', options.content);
    console.log('paperType', options.paperType);
    console.log('layout', options.layout);
    return null;
  }
  async printPDFviaIPP(options: {
    printerUrl: string;
    content: string;
    paperType: string;
    layout: string;
  }): Promise<null> {
    console.log('This feature is specifically implemented for Android platform');
    console.log('printerUrl', options.printerUrl);
    console.log('content', options.content);
    console.log('paperType', options.paperType);
    console.log('layout', options.layout);
    return null;
  }
}
