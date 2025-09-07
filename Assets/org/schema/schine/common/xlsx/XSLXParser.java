package org.schema.schine.common.xlsx;

public class XSLXParser {
//	public Workbook getTemplateData(String xlsxFile) {
//	    Workbook workbook = new Workbook();
//	    parseSharedStrings(xlsxFile);
//	    parseWorkesheet(xlsxFile, workbook);
//	    parseComments(xlsxFile, workbook);
//	    for (Worksheet worksheet : workbook.sheets) {
//	        worksheet.dimension = manager.getDimension(worksheet);
//	    }
//
//	    return workbook;
//	}
//
//	private void parseComments(String tmpFile, Workbook workbook) {
//	    try {
//	        FileInputStream fin = new FileInputStream(tmpFile);
//	        final ZipInputStream zin = new ZipInputStream(fin);
//	        InputStream in = getInputStream(zin);
//	        while (true) {
//	            ZipEntry entry = zin.getNextEntry();
//	            if (entry == null)
//	                break;
//
//	            String name = entry.getName();
//	            if (name.endsWith(".xml")) { //$NON-NLS-1$
//	                if (name.contains(COMMENTS)) {
//	                    parseComments(in, workbook);
//	                }
//	            }
//	            zin.closeEntry();
//	        }
//	        in.close();
//	        zin.close();
//	        fin.close();
//	    } catch (FileNotFoundException e) {
//	        System.out.println(e);
//	    } catch (IOException e) {
//	        e.printStackTrace();
//	    }
//	}
//
//	private void parseComments(InputStream in, Workbook workbook) {
//	    try {
//	        DefaultHandler handler = getCommentHandler(workbook);
//	        SAXParser saxParser = getSAXParser();
//	        saxParser.parse(in, handler);
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	    }
//	}
//
//	private DefaultHandler getCommentHandler(Workbook workbook) {
//	    final Worksheet ws = workbook.sheets.get(0);
//	    return new DefaultHandler() {
//	        String lastTag = "";
//	        private Cell ccell;
//
//	        @Override
//	        public void startElement(String uri, String localName,
//	                String qName, Attributes attributes) throws SAXException {
//	            lastTag = qName;
//	            if (lastTag.equals("comment")) {
//	                String cellName = attributes.getValue("ref");
//	                int r = manager.getRowIndex(cellName);
//	                int c = manager.getColumnIndex(cellName);
//	                Row row = ws.rows.get(r);
//	                if (row == null) {
//	                    row = new Row();
//	                    row.index = r;
//	                    ws.rows.put(r, row);
//	                }
//	                ccell = row.cells.get(c);
//	                if (ccell == null) {
//	                    ccell = new Cell();
//	                    ccell.cellName = cellName;
//	                    row.cells.put(c, ccell);
//	                }
//	            }
//	        }
//
//	        @Override
//	        public void characters(char[] ch, int start, int length)
//	                throws SAXException {
//	            String val = "";
//	            if (ccell != null && lastTag.equals("t")) {
//	                for (int i = start; i < start + length; i++) {
//	                    val += ch[i];
//	                }
//	                if (ccell.comment == null)
//	                    ccell.comment = val;
//	                else {
//	                    ccell.comment += val;
//	                }
//	            }
//	        }
//	    };
//	}
//
//	private void parseSharedStrings(String tmpFile) {
//	    try {
//	        FileInputStream fin = new FileInputStream(tmpFile);
//	        final ZipInputStream zin = new ZipInputStream(fin);
//	        InputStream in = getInputStream(zin);
//	        while (true) {
//	            ZipEntry entry = zin.getNextEntry();
//	            if (entry == null)
//	                break;
//	            String name = entry.getName();
//	            if (name.endsWith(".xml")) { //$NON-NLS-1$
//	                if (name.startsWith(SHARED_STRINGS)) {
//	                    parseStrings(in);
//	                }
//	            }
//	            zin.closeEntry();
//	        }
//	        in.close();
//	        zin.close();
//	        fin.close();
//	    } catch (FileNotFoundException e) {
//	        System.out.println(e);
//	    } catch (IOException e) {
//	        e.printStackTrace();
//	    }
//	}
//
//	public void parseWorkesheet(String tmpFile, Workbook workbook) {
//	    try {
//	        FileInputStream fin = new FileInputStream(tmpFile);
//	        final ZipInputStream zin = new ZipInputStream(fin);
//	        InputStream in = getInputStream(zin);
//	        while (true) {
//	            ZipEntry entry = zin.getNextEntry();
//	            if (entry == null)
//	                break;
//
//	            String name = entry.getName();
//	            if (name.endsWith(".xml")) { //$NON-NLS-1$
//	                if (name.contains("worksheets")) {
//	                    Worksheet worksheet = new Worksheet();
//	                    worksheet.name = name;
//	                    parseWorksheet(in, worksheet);
//	                    workbook.sheets.add(worksheet);
//	                }
//	            }
//	            zin.closeEntry();
//	        }
//	        in.close();
//	        zin.close();
//	        fin.close();
//	    } catch (FileNotFoundException e) {
//	        System.out.println(e);
//	    } catch (IOException e) {
//	        e.printStackTrace();
//	    }
//	}
//
//	public void parseWorksheet(InputStream in, Worksheet worksheet)
//	        throws IOException {
//	    // read sheet1 sharedStrings
//	    // styles, strings, formulas ...
//	    try {
//	        DefaultHandler handler = getDefaultHandler(worksheet);
//	        SAXParser saxParser = getSAXParser();
//	        saxParser.parse(in, handler);
//	    } catch (SAXException e) {
//	        e.printStackTrace();
//	    } catch (ParserConfigurationException e) {
//	        e.printStackTrace();
//	    }
//	}
}
