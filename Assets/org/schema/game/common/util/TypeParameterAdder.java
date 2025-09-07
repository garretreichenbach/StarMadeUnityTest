package org.schema.game.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TypeParameterAdder {
	public static void main(String[] args) throws IOException {
		TypeParameterAdder t = new TypeParameterAdder();
//		t.handl(new File("D:\\git\\StarMade\\src\\org\\schema\\game\\common\\data\\player\\inventory\\Inventory.java"));
		t.handl(new File("./src"));
		t.handl(new File("./schine/src"));
	}
	
	public void handl(File f) throws IOException {
		if(f.isDirectory()) {
			File fs[] = f.listFiles();
			for(File fa : fs) {
				handl(fa);
			}
		}else if(f.getName().endsWith(".java") && !f.getName().equals("TypeParameterAdder.java")){
			System.err.println("HANDLE: "+f);
			BufferedReader r = new BufferedReader(new FileReader(f));
			StringBuffer sb = new StringBuffer();
			String l = null;
			while((l = r.readLine()) != null) {
				sb.append(l);
				sb.append("\n");
			}
			r.close();
			
			String modified = modify(sb);
			
			if(modified != null) {
				System.err.println("MOD: "+f.getName());
				f.delete();
				BufferedWriter b = new BufferedWriter(new FileWriter(f));
				b.write(modified);
				b.close();
			}
			
			
		}
		
	}

	private String modify(StringBuffer sb) {
		boolean mod = false;
		int m = -1;
		String rr = "StaticStates<>(false, true)";
		while(sb.indexOf(rr) >= 0) {
			sb.replace(sb.indexOf(rr), sb.indexOf(rr)+rr.length(), "StaticStates<Boolean>(false, true)");
			mod = true;
		}
		while((m = sb.indexOf("<>") ) >= 0) {
			
			int endLine = sb.indexOf(";", m);
			int startLine = m;
			while(startLine > 0 && !sb.substring(startLine-1, startLine).equals(";") && !sb.substring(startLine-1, startLine).equals("{") && !sb.substring(startLine-1, startLine).equals("\n")) {
				startLine--;
			}
			String line = sb.substring(startLine, endLine);
			System.err.println("LINE: "+line.trim());
			if(line.trim().startsWith("//")) {
				sb.replace(m, m+2, "< . >");
			}else if(line.contains("=")) {
//				System.err.println("LINE: "+line);
				String[] split = line.split("=", 2);
//				System.err.println("PL "+split[0].trim());
				String src = split[0].trim();
				if(src.contains(".")) {
					src = src.substring(src.lastIndexOf(".")+1).trim();
				}
//				System.err.println("SOURCE: "+src);
				int a = src.indexOf("<");
				int b = src.lastIndexOf(">");
				if(a >= 0 && b >= 0) {
					String content = src.substring(a, b+1);
//					System.err.println("CONTENT: "+content);
					sb.replace(m, m+2, content);
					mod = true;
				}else {
					search(src, 0, sb, m);
					mod = true;
				}
			}else {
				sb.replace(m, m+2, "");
				mod = true;
			}
		}
		if(mod) {
			return sb.toString();
		}
		return null;
	}

	private void search(String src, int from, StringBuffer sb, final int m) {
		int fst = sb.indexOf(src.trim(), from);
		if(fst < 0) {
			sb.replace(m, m+2, "");
			return;
		}
		int fsNext = fst+src.trim().length();
		while(fst > 0  && !sb.substring(fst-1, fst).equals(";") && !sb.substring(fst-1, fst).equals("{") ) {
			fst--;
		}
		int snd = sb.indexOf(";", fst);
		if(snd < 0) {
			search(src, fsNext, sb, m);
		}
		String mm = sb.substring(fst, snd);
		
		if(mm.contains("=") || !(mm.contains("<") && mm.contains(">"))) {
			search(src, fsNext, sb, m);
		}else {
			int a = mm.indexOf("<");
			int b = mm.lastIndexOf(">");
			if(a >= 0 && b >= 0) {
				String content = mm.substring(a, b+1);
				System.err.println("CONTENT2: "+content);
				sb.replace(m, m+2, content);
			}	
		}
	}

}
