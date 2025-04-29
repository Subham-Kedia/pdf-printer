export interface PdfPrinterPlugin {
  printPDF(options: { contentType: 'html' | 'pdf'; content: string; paperType: 'ISO_A4' | 'ISO_A5' }): Promise<null>;
}
