export interface PdfPrinterPlugin {
  printPDF(options: { url: string; paper_type?: 'ISO_A4' | 'ISO_A5' }): Promise<null>;
}
