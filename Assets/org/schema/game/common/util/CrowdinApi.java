package org.schema.game.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.schema.schine.common.language.LanguageReader;
import org.schema.schine.common.language.Translation;
import org.schema.schine.resource.FileExt;
import org.xml.sax.SAXException;


public class CrowdinApi {

	
	public static final String URL = "https://api.crowdin.com/api/project/";
	public static final String FOLDER = "./crowdin/";
	public static final String DATA_FOLDER = "./data/language/";
	
	public static Properties loadProperties() throws IOException{
		Properties p = new Properties();
		
		
		InputStream input = null;

		try {

			input = new FileInputStream("./crowdin.properties");
			// load a properties file
			p.load(input);
			return p;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static String getGETUrl(Properties p, String endpoint) throws IOException{
		String pID = p.getProperty("PROJECT_ID");
		if(pID == null){
			throw new IOException("No project ID in properties");
		}
		return URL + pID +"/"+endpoint;
	}
	
	public static String getApiKeyEncode(Properties p) throws IOException{
		String apiKey = p.getProperty("API_KEY");
		if(apiKey == null){
			throw new IOException("No API_KEY in properties");
		}
		return URLEncoder.encode("key", "UTF-8") + "=" + URLEncoder.encode(apiKey, "UTF-8");
	}

	public static void main(String[] args) throws IOException, SAXException,
			ParserConfigurationException, TransformerException {
		export();

		try {
			updateLanguage("German", "de");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			updateLanguage("Polish", "pl");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			updateLanguage("Japanese", "ja",
					"data/font/NotoSansCJKtc-Regular.ttf");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			updateLanguage("Russian", "ru",
					"data/font/NotoSansCJKtc-Regular.ttf");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			updateLanguage("Chinese Traditional", "zh-TW",
					"data/font/NotoSansCJKtc-Regular.ttf");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			updateLanguage("French", "fr");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			updateLanguage("Spanish", "es-ES");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			updateLanguage("Portuguese Brazilian", "pt-BR");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			updateLanguage("Chinese Simplified", "zh-CN",
					"data/font/NotoSansCJKtc-Regular.ttf");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			updateLanguage("Czech", "cs");
		} catch (Exception e) {
			e.printStackTrace();
		}
		FileUtils.deleteDirectory(new FileExt(FOLDER));

	}
	public static void updateLanguage(String language, String crowdinCode) throws IOException, SAXException, ParserConfigurationException, TransformerException{
		updateLanguage(language, crowdinCode, null);
	}
	public static void updateLanguage(String language, String crowdinCode, String font) throws IOException, SAXException, ParserConfigurationException, TransformerException{
		
		download(crowdinCode);
		List<Translation> list = new ArrayList<Translation>();
		File cFile = new FileExt(FOLDER+crowdinCode+".zip");
		
		Map<String, Translation > param = new HashMap<String, Translation >();
		LanguageReader.loadLangFile( new FileExt("./data/language/defaultPack.xml"), param );
		for(Translation a : param.values()){
			list.add(a);
		}
		
		LanguageReader.importFileXML(cFile, list);
		
		if(list.isEmpty()){
			throw new IOException("Nothing imported from "+cFile.getName());
		}
		(new FileExt(DATA_FOLDER+language+"/")).mkdirs();
		File file = new FileExt(DATA_FOLDER+language+"/pack.xml");
		System.err.println("EXPORTING TO SMLANG FORMAT: "+file.getAbsolutePath()+" ...");
		LanguageReader.save(file, language, font, list);
		
		
		LanguageReader.importFileTutorial(cFile, DATA_FOLDER+language+"/");
	}
	public static void download(String lang) throws IOException{
		Properties p = loadProperties();
		
		String getUrl = getGETUrl(p, "download")+"/"+URLEncoder.encode(lang+".zip", "UTF-8")+"?"+getApiKeyEncode(p);
		
		
		System.err.println("DOWNLOADING URL: "+getUrl);
		new FileExt(FOLDER).mkdir();
		FileUtils.copyURLToFile(new URL(getUrl), new FileExt(FOLDER+lang+".zip"));
	}
	public static void export() throws IOException{
		Properties p = loadProperties();
		
		String getUrl = getGETUrl(p, "export")+"?"+getApiKeyEncode(p);
		
		
		
		
		System.err.println("CALLING URL: "+getUrl);
		
		URL url = new URL(getUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setReadTimeout(520000);

		conn.setRequestMethod("GET");

		long t = System.currentTimeMillis();


		int responseCode = conn.getResponseCode();
		// Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;

		StringBuffer b = new StringBuffer();
		while ((line = rd.readLine()) != null) {
			System.err.println(line);
			b.append(line);
		}
		long took = System.currentTimeMillis() - t;
		rd.close();
	}
}
