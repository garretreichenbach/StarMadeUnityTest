package org.schema.schine.common.language;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.schema.common.XMLTools;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.forms.font.FontPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class LanguageReader {

	
	
	public static void copyFile(File sourceLocation, File targetLocation) throws IOException {
//		System.err.println("[IO][COPY] FILE FROM " + sourceLocation.getAbsolutePath() + " to " + targetLocation.getAbsolutePath());
		InputStream in = new BufferedInputStream(new FileInputStream(sourceLocation));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(targetLocation));

		// Copy the bits from instream to outstream
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.flush();
		in.close();
		out.close();
	}

	public static void loadLangFile(File file, Map<String, Translation> params) throws IOException, SAXException, ParserConfigurationException {
		loadLangFile(file, file, params);
	}
	public static void save(File output, String language, String font, List<Translation> list) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		Document doc = docBuilder.newDocument();

		org.w3c.dom.Element root = doc.createElement("Language");
		root.setAttribute("Language", language);
		if(font != null){
			root.setAttribute("fontnameorpath", font);
		}
		root.setAttribute("Version", "0");
		
		doc.appendChild(root);
		int i = 0;
		for(Translation v : list){
			Element element = doc.createElement("Element");
			
			element.setAttribute("var", v.var);
			element.setAttribute("translator", v.translator);
			Element originalElement = doc.createElement("Original");
			Element translationElement = doc.createElement("Translation");
			Element argumentElement = doc.createElement("Arguments");
			
			originalElement.setTextContent(v.original);
			translationElement.setTextContent(v.translation);
			
			for(String s : v.args){
				Element arg = doc.createElement("Argument");
				arg.setTextContent(s);
				argumentElement.appendChild(arg);
			}
			
			element.appendChild(translationElement);
			element.appendChild(originalElement);
			element.appendChild(argumentElement);
			
			
			root.appendChild(element);
			
			i++;
		}
		
		output.getParentFile().mkdirs();
		
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		// create string from xml tree
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(output);
		DOMSource source = new DOMSource(doc);
		trans.transform(source, result);
	}
	public static void importFileTutorial(File importfile, String dir) throws IOException, SAXException, ParserConfigurationException {
		
		InputStream s;
		if(importfile.getName().endsWith(".zip")){
			s = new ZipInputStream(new BufferedInputStream(new FileInputStream(importfile)), StandardCharsets.UTF_8);
			ZipEntry nm;
			boolean found = false;
			while((nm = ((ZipInputStream)s).getNextEntry()) != null){
				System.err.println("ZIP ENTRY: "+nm.getName());
				if(nm.getName().toLowerCase(Locale.ENGLISH).equals("Tutorial Subtitles.xlsx".toLowerCase(Locale.ENGLISH))){
					System.err.println("USING: "+nm.getName());
					found = true;
					break;
				}
			}
			if(!found){
				s.close();
				throw new IOException("Tutorial File not found in Zip");
			}
//			s.close();
//			s = new ZipInputStream(new BufferedInputStream(new FileInputStream(importfile)));
//			ZipEntry ze = ((ZipInputStream)s).getNextEntry();
			
		}else{
			s = new BufferedInputStream(new FileInputStream(importfile));
		}
//		File out = new File("tmp.xlsx");
//		BufferedOutputStream b = new BufferedOutputStream(new FileOutputStream(out));
//		int read = 0;
//		byte[] bytes = new byte[4096];
//
//		while ((read = s.read(bytes)) != -1) {
//			b.write(bytes, 0, read);
//		}tmp
//		b.close();
//		
		
		int minColumns = -1;
//		OPCPackage p;
//		try {
//			p = OPCPackage.open(s);
//			
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			PrintStream ps = new PrintStream(baos);
//			
//			
//			XLSX2CSV xlsx2csv = new XLSX2CSV(p, ps, minColumns);
//			xlsx2csv.process();
//			p.close();
//			
//			String content = new String(baos.toByteArray(), "UTF-8");//StandardCharsets.UTF_8
//			
//			processTutorialFile(content, dir);
//		} catch (Exception e) {
//			throw new IOException("Tutorial Parsing Failed (XLSX)", e);
//		}finally{
////			out.delete();
//		}
	}
	private static class SubtitleElem{
//		00:00:12.200,00:00:16.200
//		You will start small, learning how to harvest resources for crafting.
		public String context;
		public String start;
		public String end;
		public String translated;
		public String original;
		public boolean keybind;
		@Override
		public String toString() {
			return "SubtitleElem [context=" + context + ", start=" + start + ", end=" + end + ", translated="
					+ translated + ", original=" + original + ", keybind=" + keybind + "]";
		}
		
		
	}
	private static class Index{
		String name;
		int index = -1;
		
		public Index(String name){
			this.name = name;
		}
		
		public String get(String line){
			String[] split = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
			if(index < 0 || index > split.length-1){
				throw new IndexOutOfBoundsException("INVALID INDEX : "+index+"::: "+line+"; "+Arrays.toString(split)+"; len "+split.length );
			}
			String string = split[index].trim();
//			System.err.println("TRIMMING "+name+"::: "+string+"; "+line);
			if(string.startsWith("\"")){
				string = string.substring(1, string.length()).trim();
			}
			if(string.endsWith("\"")){
				string = string.substring(0, string.length()-1).trim();
			}
			return string;
		}
		public void find(String line){
			String[] split = line.split(",");
			for(int i = 0; i <split.length; i++){
				if(split[i].toLowerCase(Locale.ENGLISH).equals("\""+name.toLowerCase(Locale.ENGLISH)+"\"")){
					index = i;
					return;
				}
			}
		}
	}
	private static void processTutorialFile(String content, String dir) throws IOException {
		Map<String, List<SubtitleElem>> subtitles = new Object2ObjectOpenHashMap<String, List<SubtitleElem>>();
		
		BufferedReader r = new BufferedReader(new StringReader(content));
		Index context = new Index("CONTEXT");
		Index start = new Index("S. TIMESTAMP");
		Index end = new Index("E. TIMESTAMP");
		Index translated = new Index("TRANSLATED");
		Index original = new Index("ORIGINAL");
		String line;
		while((line = r.readLine()) != null){
			if(line.trim().length() == 0){
				continue;
			}
			//"ID","Ch.Lmt","CONTEXT","S. TIMESTAMP","E. TIMESTAMP","TRANSLATED","ORIGINAL"
			if(line.startsWith("\"ID\"")){
				context.find(line);
				start.find(line);
				end.find(line);
				translated.find(line);
				original.find(line);
			}else if(line.contains(",")){
				SubtitleElem e = new SubtitleElem();
				if(line.startsWith("\"key_")){
					StringBuffer lm = new StringBuffer();
					lm.append(line);
					while(!line.trim().endsWith("\"")){
						line = r.readLine();
						lm.append("\n"+line);
					}
					String res = lm.toString();
					e.keybind = true;
					e.context = context.get(res).split("\n")[0].trim();
					e.translated = translated.get(res);
					e.original = original.get(res);
					
				}else{
					StringBuffer lm = new StringBuffer();
					lm.append(line);
					while(!line.trim().endsWith("\"")){
						line = r.readLine();
						lm.append("\n"+line);
					}
					String res = lm.toString();
					
					
					e.context = context.get(res);
					e.start = start.get(res);
					e.end = end.get(res);
					e.translated = translated.get(res);
					e.original = original.get(res);
				}
				List<SubtitleElem> list = subtitles.get(e.context.toLowerCase(Locale.ENGLISH));
				if(list == null){
					list = new ObjectArrayList<SubtitleElem>();
					subtitles.put(e.context.toLowerCase(Locale.ENGLISH), list);
				}
				list.add(e);
			}
		}
	

		r.close();
		writeSubtitles(subtitles, dir);
		
	}

	private static void writeSubtitles(Map<String, List<SubtitleElem>> subtitles, String dir) throws IOException {
		for(List<SubtitleElem> list : subtitles.values()){
			String context = list.get(0).context;
			System.err.println("PROCESSING: "+context);
			File vidDir = new File("./data/video/tutorial/");
			String fileNameSBV = null;
			String fileNameXML = null;
			for(File f : vidDir.listFiles()){
				if(f.getName().toLowerCase(Locale.ENGLISH).contains(context.toLowerCase(Locale.ENGLISH))){
					if(f.getName().toLowerCase(Locale.ENGLISH).endsWith(".sbv")){
						fileNameSBV = f.getName();
					}
					if(f.getName().toLowerCase(Locale.ENGLISH).endsWith(".xml")){
						fileNameXML = f.getName();
					}
				}
			}
			if(fileNameSBV == null || fileNameXML == null){
				throw new IOException("FILES NOT FOUND "+context+": "+Arrays.toString(vidDir.listFiles()));
			}
			File sbv = new File(dir+fileNameSBV);
			File xml = new File(dir+fileNameXML);
			sbv.delete();
			xml.delete();
			
			BufferedWriter wSbv = new BufferedWriter(new FileWriter(sbv));
			for(SubtitleElem e : list){
				if(!e.keybind){
					//00:00:00.240,00:00:03.800
//					Greetings citizen! Welcome to StarMade! 
					wSbv.write(e.start+","+e.end+"\n");
					wSbv.write(e.translated+"\n");
					wSbv.write("\n");
				}
			}
			wSbv.close();
			try{
				for(SubtitleElem e : list){
					if(e.keybind){
						DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
						DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
						Document doc = docBuilder.newDocument();
			
						org.w3c.dom.Element root = doc.createElement("Meta");
						org.w3c.dom.Element keybinds = doc.createElement("Keybinds");
						org.w3c.dom.Element description = doc.createElement("Description");
						
						keybinds.setTextContent(e.translated);
						description.setTextContent(e.context);
						
						root.appendChild(keybinds);
						root.appendChild(description);
						doc.appendChild(root);
						XMLTools.writeDocument(xml, doc);
						break;
					}
				}
			}catch(Exception e){
				throw new IOException(e);
			}
		}		
	}

	public static void importFileXML(File importfile, List<Translation> list) throws IOException, SAXException, ParserConfigurationException {
		
		InputStream s;
		if(importfile.getName().endsWith(".zip")){
			s = new ZipInputStream(new BufferedInputStream(new FileInputStream(importfile)));
			ZipEntry nm;
			while((nm = ((ZipInputStream)s).getNextEntry()) != null){
				System.err.println("ZIP ENTRY: "+nm.getName());
				if(nm.getName().toLowerCase(Locale.ENGLISH).equals("pack-crowdin.xml".toLowerCase(Locale.ENGLISH))){
					break;
				}
			}
//			s.close();
//			s = new ZipInputStream(new BufferedInputStream(new FileInputStream(importfile)));
//			ZipEntry ze = ((ZipInputStream)s).getNextEntry();
			
		}else{
			s = new BufferedInputStream(new FileInputStream(importfile));
		}
		
		
		Map<String, Translation> m = new Object2ObjectOpenHashMap<String, Translation>(list.size());
		
		for(Translation t : list){
			m.put(t.var, t);
		}
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		{
			Document doc = dBuilder.parse(s);
			s.close();
			
			Node root = doc.getDocumentElement();
			NodeList elements = root.getChildNodes();
			
			for(int x = 0; x < elements.getLength(); x++){
				Node element = elements.item(x);
				
				if(element.getNodeType() == Node.ELEMENT_NODE && element.getNodeName().equals("string")){
					String var = element.getAttributes().getNamedItem("name").getNodeValue();
					String value = element.getTextContent();
					Translation translation = m.get(var);
					if(translation != null){
						translation.translation = value;
						translation.translator = "CrowdinCommunity";
						translation.changed = true;
					}
				}
			}
			
		}
	}
	public static void loadLangFile(File file, File defaultLanguage, Map<String, Translation> params) throws IOException, SAXException, ParserConfigurationException {
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		{
			Document doc = dBuilder.parse(defaultLanguage);
			
			Node root = doc.getDocumentElement();
			
			String language = root.getAttributes().getNamedItem("Language").getNodeValue();
			String version = root.getAttributes().getNamedItem("Version").getNodeValue();
			
			System.err.println("[LANGUAGE] Loading "+language+"; Version: "+version);
			
			
			try{
				Node fontPathNode = root.getAttributes().getNamedItem("fontnameorpath");
				Node offsetStartSizeNode = root.getAttributes().getNamedItem("yOffsetStartSize");
				Node offsetDivideByNode = root.getAttributes().getNamedItem("yOffsetDividedBy");
				Node offsetFixedNode = root.getAttributes().getNamedItem("yOffsetFixed");
				
				if(fontPathNode != null){
					
					FontPath.font_Path = fontPathNode.getNodeValue();
					System.err.println("[LANGUAGE] LOADED FONT PATH/NAME: "+FontPath.font_Path);
				}
				if(offsetStartSizeNode != null){
					FontPath.offsetStartSize = Integer.parseInt(offsetStartSizeNode.getNodeValue());
				}
				if(offsetDivideByNode != null){
					FontPath.offsetDividedBy = Integer.parseInt(offsetDivideByNode.getNodeValue());
				}
				if(offsetFixedNode != null){
					FontPath.offsetFixed = Integer.parseInt(offsetFixedNode.getNodeValue());
				}
			}catch(Exception e){
				e.printStackTrace();
				GLFrame.processErrorDialogExceptionWithoutReport(e, null);
			}
			NodeList elements = root.getChildNodes();
			
			for(int x = 0; x < elements.getLength(); x++){
				Node element = elements.item(x);
				
				if(element.getNodeType() == Node.ELEMENT_NODE && element.getNodeName().equals("Element")){
					parseElement(element, params, false);
				}
			}
		}
		
		if(file != defaultLanguage){
			Document doc = dBuilder.parse(file);
			
			Node root = doc.getDocumentElement();
			
			String language = root.getAttributes().getNamedItem("Language").getNodeValue();
			String version = root.getAttributes().getNamedItem("Version").getNodeValue();
			
			System.err.println("[LANGUAGE] Loading "+language+"; Version: "+version);
			
			NodeList elements = root.getChildNodes();
			
			for(int x = 0; x < elements.getLength(); x++){
				Node element = elements.item(x);
				if(element.getNodeType() == Node.ELEMENT_NODE && element.getNodeName().equals("Element")){
					parseElement(element, params, true);
				}
			}
		}
		
	}
//	<Element translator="CrowdinCommunity" var="ORG_SCHEMA_GAME_CLIENT_VIEW_MAINMENU_TOOLSANDMODSDIALOG_13">
//	<Translation/>
//	<Original>Effect Config Editor</Original>
//	<Arguments/>
//	</Element>
	private static void parseElement(Node element,
			Map<String, Translation> params, boolean translationOnly) {
		
		
		String varName = element.getAttributes().getNamedItem("var").getNodeValue();
		String translator = element.getAttributes().getNamedItem("translator").getNodeValue();
		NodeList childs = element.getChildNodes();
		List<String> argsList = null;//
		String translation = null;
		String original = null;
		for(int x = 0; x < childs.getLength(); x++){
			Node item = childs.item(x);
			
			if (item.getNodeType() == Node.ELEMENT_NODE){
				if(item.getNodeName().equals("Original")){
					original = item.getTextContent();
				}else if(item.getNodeName().equals("Translation")){
					translation = item.getTextContent();
					
				}else if(item.getNodeName().equals("Arguments")){
					NodeList args = item.getChildNodes();
					for(int i = 0; i < args.getLength(); i++){
						Node arg = args.item(i);
						if(arg.getNodeType() == Node.ELEMENT_NODE){
							if(argsList== null){
								argsList = new ArrayList();
							}
							argsList.add(arg.getTextContent());
						}
					}
				}
			}
		}
//		if(varName.equals("ORG_SCHEMA_GAME_CLIENT_VIEW_MAINMENU_TOOLSANDMODSDIALOG_13")){
//			System.err.println("����� ::: "+varName+";;; '"+original+"'; '"+translation+"'");
//		}
		
		String[] args = new String[argsList != null ? argsList.size() : 0];
		for(int i = 0; i < args.length; i++){
			args[i] = argsList.get(i);
		}
		if(translation.length() == 0){
			//replace empty translations with original english
			translation = original;
			
//			System.err.println("USING ORIGINAL:: '"+translation+"' FOR "+varName);
		}
		if(translationOnly){
			Translation t = params.get(varName);
			if(t != null){
				if(t.original.equals(original)){
					//only fill in what didn't change
					t.translation = translation;
					t.translator = translator;
				}else{
					t.oldTranslation = translation;
				}
			}
		}else{
			Translation t = new Translation(varName, original, translation, null, translator, args);
			params.put(varName, t);
		}
	}

}
