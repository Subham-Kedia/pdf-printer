export interface PdfPrinterPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
