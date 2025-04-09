export interface PdfPrinterPlugin {
  printPDF(options: { url: string }): Promise<null>;
}
