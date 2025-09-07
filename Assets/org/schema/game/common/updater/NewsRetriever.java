package org.schema.game.common.updater;

import org.schema.common.util.StringTools;
import org.schema.common.util.security.OperatingSystem;
import org.schema.schine.resource.FileExt;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.GregorianCalendar;

public class NewsRetriever extends HTMLEditorKit.ParserCallback {

	private static final long HOURINMILLISECS = 1000 * 60 * 60;
	static DateFormat df = StringTools.getSimpleDateFormat("M/d/yyyy - h:mm a", "M/d/yyyy - h:mm a");
	StringBuffer s;
	private Tag currentTag;

	public static String getNe(boolean forced) throws IOException {
		NewsRetriever nw = new NewsRetriever();
		if (!forced) {
			try {
				return nw.getFromFile();
			} catch (Exception e) {
				//			e.printStackTrace();
			}
		}

		// Create a URL for the desired page
		URL url = new URL("http://star-made.org/news");

		URLConnection openConnection = url.openConnection();
		openConnection.setConnectTimeout(10000);

		// Read all the text returned by the server
		try(BufferedReader in = new BufferedReader(new InputStreamReader(openConnection.getInputStream()))) {
			nw.parse(in);
			in.close();
			nw.writeToFile(nw.getText());
		} catch(Exception e) {
			e.printStackTrace();
		}
		return nw.getText();

	}

	private String getFromFile() throws UpdateNeededException, NumberFormatException, IOException {
		File f = new FileExt(OperatingSystem.getAppDir("StarMade"), "news.txt");
		if (!f.exists()) {
			throw new FileNotFoundException();
		}
		BufferedReader b = new BufferedReader(new FileReader(f));
		long timeUpdate = Long.parseLong(b.readLine());

		if (System.currentTimeMillis() - timeUpdate < HOURINMILLISECS) {
			String str;
			StringBuffer s = new StringBuffer();
			GregorianCalendar c = new GregorianCalendar();
			c.setTimeInMillis(timeUpdate);
			s.append("last news refresh: ").append(df.format(c.getTime())).append("\n\n");
			while((str = b.readLine()) != null) s.append(str).append("\n");
			b.close();
			return s.toString();
		} else {
			b.close();
			throw new UpdateNeededException(timeUpdate);
		}
	}

	public String getText() {
		return s.toString();
	}

	@Override
	public void handleText(char[] text, int pos) {
		s.append(text);
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.html.HTMLEditorKit.ParserCallback#handleStartTag(javax.swing.text.html.HTML.Tag, javax.swing.text.MutableAttributeSet, int)
	 */
	@Override
	public void handleStartTag(Tag t, MutableAttributeSet a, int pos) {
		String string = a.toString();

		if (string.contains("class=meta submitted")) {
			currentTag = t;
			s.append("\n");
		}
		if (string.contains("node node-news-entry")) {
			s.append("\n\n\n");
			currentTag = t;
		}
		super.handleStartTag(t, a, pos);
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.html.HTMLEditorKit.ParserCallback#handleEndTag(javax.swing.text.html.HTML.Tag, int)
	 */
	@Override
	public void handleEndTag(Tag t, int pos) {
		if (currentTag == t) {
			s.append("\n\n");
			currentTag = null;
		}

		super.handleEndTag(t, pos);
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.html.HTMLEditorKit.ParserCallback#handleEndOfLineString(java.lang.String)
	 */
	@Override
	public void handleEndOfLineString(String eol) {
		super.handleEndOfLineString(eol);
		s.delete(0, s.indexOf("\n") + 3);
		String toDel = "Log in or register to post comments";
		int ined = 0;
		while ((ined = s.indexOf(toDel)) >= 0) {
			s.delete(ined, ined + toDel.length());
		}

	}

	public void parse(Reader in) throws IOException {
		s = new StringBuffer();
		ParserDelegator delegator = new ParserDelegator();
		// the third parameter is TRUE to ignore charset directive
		delegator.parse(in, this, true);
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(System.currentTimeMillis());
		s.insert(0, "last news refresh: " + df.format(c.getTime()) + "\n\n");
	}

	private void writeToFile(String t) throws IOException {
		File f = new FileExt(OperatingSystem.getAppDir(), "news.txt");
		if (!f.exists()) {
			f.createNewFile();
		}
		BufferedWriter w = new BufferedWriter(new FileWriter(f));
		w.append(System.currentTimeMillis() + "\n");
		w.append(t, t.indexOf("\n"), t.length());
		w.flush();
		w.close();
	}

	private class UpdateNeededException extends Exception {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		/**
		 *
		 */


		public UpdateNeededException(long lastUpdate) {
			super("LAST UPDATE " + lastUpdate);
		}

	}

}