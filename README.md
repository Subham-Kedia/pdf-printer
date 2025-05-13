# pdf-printer

print in android

## Install

```bash
npm install pdf-printer
npx cap sync
```

## API

<docgen-index>

* [`printPDF(...)`](#printpdf)
* [`printPDFviaIPP(...)`](#printpdfviaipp)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### printPDF(...)

```typescript
printPDF(options: { contentType: 'html' | 'pdf'; content: string; paperType: 'ISO_A4' | 'ISO_A5'; layout: 'portrait' | 'landscape'; }) => Promise<null>
```

| Param         | Type                                                                                                                                |
| ------------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ contentType: 'html' \| 'pdf'; content: string; paperType: 'ISO_A4' \| 'ISO_A5'; layout: 'portrait' \| 'landscape'; }</code> |

**Returns:** <code>Promise&lt;null&gt;</code>

--------------------


### printPDFviaIPP(...)

```typescript
printPDFviaIPP(options: { printerUrl: string; content: string; paperType: 'ISO_A4' | 'ISO_A5'; layout: 'portrait' | 'landscape'; }) => Promise<null>
```

| Param         | Type                                                                                                                      |
| ------------- | ------------------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ printerUrl: string; content: string; paperType: 'ISO_A4' \| 'ISO_A5'; layout: 'portrait' \| 'landscape'; }</code> |

**Returns:** <code>Promise&lt;null&gt;</code>

--------------------

</docgen-api>
