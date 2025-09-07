package org.schema.schine.graphicsengine.shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderPreprocessor implements ShaderModifyInterface {

	// possible states
	private static final int STATE_OUTSIDE_CONDITION = 0;
	private static final int STATE_TRUE_CONDITION = 1;
	private static final int STATE_FALSE_CONDITION = 2;
	private static final int STATE_POST_TRUE_CONDITION = 3;
	private Set<String> fSymbols = new HashSet<String>();
	// matchers
	private Matcher IF_DEF_MATCHER = Pattern.compile("#IFDEF\\s+\\w+").matcher("");
	private Matcher ELSE_IF_MATCHER = Pattern.compile("#ELSEIF\\s+\\w+").matcher("");
	private Matcher ELSE_MATCHER = Pattern.compile("#ELSE$|#ELSE\\W+").matcher("");
	private Matcher END_MATCHER = Pattern.compile("#ENDIF").matcher("");

	public ShaderPreprocessor(String... strings) {
		for (String s : strings) {
			if (s != null && s.length() > 0) {
				fSymbols.add(s);
			}
		}
	}

	/**
	 * Sets the symbols that are "on" for the preprocessing.
	 *
	 * @param symbols symbols that are "on" for the preprocessing
	 */
	public void setSymbols(String symbols) {
		String[] strings = symbols.split(",");
		for (int i = 0; i < strings.length; i++) {
			String string = strings[i].trim();
			if (string.length() > 0) {
				fSymbols.add(string);
			}
		}
	}

	/**
	 * Preprocesses a file
	 *
	 * @param srcFile the file to process
	 * @param strip   chars to stip off lines in a true condition, or <code>null
	 * @return
	 */
	public String preProcessFile(String input, String strip) {
		try {
			BufferedReader reader = new BufferedReader(new StringReader(input));
			StringBuffer buffer = new StringBuffer();
			String line = reader.readLine();
			String activeSymbol = null;
			int state = STATE_OUTSIDE_CONDITION;
			boolean changed = false;
			while (line != null) {
				boolean ifdef = IF_DEF_MATCHER.reset(line).find();
				boolean elseif = ELSE_IF_MATCHER.reset(line).find();
				boolean elze = ELSE_MATCHER.reset(line).find();
				boolean endif = END_MATCHER.reset(line).find();
				boolean commandLine = ifdef || elseif || elze || endif;
				boolean written = false;
				switch (state) {
					case STATE_OUTSIDE_CONDITION:
						if (ifdef) {
							String condition = line.substring(IF_DEF_MATCHER.start(), IF_DEF_MATCHER.end());
							String[] strings = condition.split("\\s+");
							activeSymbol = strings[1].trim();
							if (fSymbols.contains(activeSymbol)) {
								state = STATE_TRUE_CONDITION;
							} else {
								state = STATE_FALSE_CONDITION;
							}
						} else if (elseif) {
							throw new RuntimeException("#elseif encountered without corresponding #ifdef");
						} else if (elze) {
							throw new RuntimeException("#else encountered without corresponding #ifdef (" + ")");
						} else if (endif) {
							throw new RuntimeException("#endif encountered without corresponding #ifdef");
						}
						break;
					case STATE_TRUE_CONDITION:
						if (elze || elseif) {
							state = STATE_POST_TRUE_CONDITION;
							break;
						} else if (endif) {
							state = STATE_OUTSIDE_CONDITION;
							break;
						} else if (ifdef) {
							throw new RuntimeException("illegal nested #ifdef");
						}
					case STATE_FALSE_CONDITION:
						if (elseif) {
							String condition = line.substring(ELSE_IF_MATCHER.start(), ELSE_IF_MATCHER.end());
							String[] strings = condition.split("\\s+");
							activeSymbol = strings[1].trim();
							if (fSymbols.contains(activeSymbol)) {
								state = STATE_TRUE_CONDITION;
							} else {
								state = STATE_FALSE_CONDITION;
							}
						} else if (elze) {
							state = STATE_TRUE_CONDITION;
							break;
						} else if (endif) {
							state = STATE_OUTSIDE_CONDITION;
							break;
						} else if (ifdef) {
							throw new RuntimeException("illegal nested #ifdef");
						}
					case STATE_POST_TRUE_CONDITION:
						if (endif) {
							state = STATE_OUTSIDE_CONDITION;
							break;
						} else if (ifdef) {
							throw new RuntimeException("illegal nested #ifdef");
						}
				}
				if (!commandLine) {
					if (state == STATE_OUTSIDE_CONDITION || state == STATE_TRUE_CONDITION) {
						if (state == STATE_TRUE_CONDITION && strip != null) {
							if (line.startsWith(strip)) {
								line = line.substring(strip.length());
							}
						}
						buffer.append(line);
						buffer.append("\n");
						written = true;
					}
				}
				changed = changed || !written;
				line = reader.readLine();
			}
			if (!changed) {
				return input;
			}
			return buffer.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String handle(String vsrc) {
		return preProcessFile(vsrc, null);
	}
}
