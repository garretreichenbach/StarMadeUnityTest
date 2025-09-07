package org.schema.game.common.data.element.mediawiki;

import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.data.element.ElementCategory;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.resource.FileExt;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MediawikiExport {
	private Wiki wiki;

	public static void main(String[] args) {
		ElementKeyMap.initializeData(GameResourceLoader.getConfigInputFile());

//		System.err.println(printCat(ElementKeyMap.getCategoryHirarchy(), new StringBuffer(), 1));

		MediawikiExport p = new MediawikiExport();
		p.execute(args);

//		8D3tT{NbEeC@5)!-/zKn

	}

	public static void printTitle(String title, int lvl, StringBuffer sb) {
		for (int i = 0; i < lvl; i++) {
			sb.append("=");
		}
		sb.append(title);
		for (int i = 0; i < lvl; i++) {
			sb.append("=");
		}
		sb.append("\n");
	}

	public static String printCat(ElementCategory categoryHirarchy, StringBuffer sb, int lvl) {

		printTitle(categoryHirarchy.getCategory(), lvl, sb);

		sb.append("{| class=\"wikitable sortable alternance\"\n");
		sb.append("!ID!!Block Name!!class=\"unsortable\"|Picture\n");
		sb.append("|-\n");

		ArrayList<ElementInformation> s = new ArrayList<ElementInformation>(categoryHirarchy.getInfoElements());

		Collections.sort(s, (o1, o2) -> o1.getName().compareTo(o2.getName()));
		boolean first = true;
		for (ElementInformation info : s) {
			if (!first) {
				sb.append("|-\n");
			} else {
				first = false;
			}
			sb.append("|");
			sb.append(String.valueOf(info.getId()));
			sb.append("||");
			sb.append("[[").append(info.getName()).append("]]");
			sb.append("||");
			sb.append("[[File:").append(info.getName().replaceAll(" ", "_")).append(".png").append("|").append(info.getName()).append("|center|50px]]\n");
		}
		sb.append("|}\n");
		sb.append("\n");
		for (ElementCategory c : categoryHirarchy.getChildren()) {
			printCat(c, sb, lvl + 1);
		}

		return sb.toString();
	}

	public ArrayList<Page> create() {
		ArrayList<Page> p = new ArrayList<Page>();
		for (short e : ElementKeyMap.keySet) {
			ElementInformation info = ElementKeyMap.getInfo(e);

			String title = info.getName().replaceAll(" ", "_");

			p.add(new Page(title, info.createWikiStub()));
		}
		return p;
	}

	private void execute(String[] args) {
		try {
			wiki = new Wiki();
			wiki.login(args[0], args[1]);

			putIndividualSites();

			putBlockIdSize();

			wiki.logout();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LoginException e) {
			e.printStackTrace();
		}

	}

	private void putBlockIdSize() throws IOException, LoginException {

		StringBuffer sb = new StringBuffer();
		String title = "ID_list";

		ElementCategory categoryHirarchy = ElementKeyMap.getCategoryHirarchy();
		String pageText = printCat(categoryHirarchy, sb, 1);

		try {
			wiki.edit(title, pageText, "");
		} catch (FileNotFoundException e) {
			System.err.println("Page does not exit yet: " + title);
			wiki.edit(title, pageText, "");
		}

	}

	private void putIndividualSites() throws IOException, LoginException {
		ArrayList<Page> create = create();
		for (int i = 0; i < create.size(); i++) {
			Page page = create.get(i);
			System.err.println("---------------------: Handling " + (i + 1) + "/" + create.size() + ": " + page.title);
			try {
				String pageText = wiki.getPageText(page.title);
				
				
				
				System.err.println("Handling existing page");

				StringBuffer b = new StringBuffer(pageText);

				int startBlock = b.indexOf("{{infobox block");
				if (startBlock >= 0) {
					int endBlock = b.indexOf("}}", startBlock);

					if (endBlock >= startBlock) {
						b.delete(startBlock, endBlock + 2);
						System.err.println("Removed main block");
					}
				}
//				int startDesc = b.indexOf("==Description==");
//				if(startDesc >= 0){
//					int endDesc = b.indexOf("-----", startDesc);
//
//					if(endDesc >= startDesc){
//						b.delete(startDesc, endDesc+5);
//						System.err.println("Removed description block");
//					}
//				}

				pageText = page.content + b.toString();

				wiki.edit(page.title, pageText, "");

			} catch (FileNotFoundException e) {
				System.err.println("Page does not exit yet: " + page.title);
				wiki.edit(page.title, "{{Stub}}\n" + page.content, "");
			}

			File f = new FileExt("./iconbakery/150x150/" + page.title + ".png");
			if (f.exists()) {
				System.err.println("Uploading file: " + f.getName());
				wiki.upload(f, f.getName(), "", "");
			} else {
				System.err.println("File not found: " + f.getAbsolutePath());
			}
		}
	}

	private class Page {
		final String title;
		final String content;

		public Page(String title, String content) {
			super();
			this.title = title;
			this.content = content;
		}

	}

}
