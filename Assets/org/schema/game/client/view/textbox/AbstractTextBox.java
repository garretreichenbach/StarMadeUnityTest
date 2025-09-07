package org.schema.game.client.view.textbox;

import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.TextBoxDrawListener;
import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.client.controller.element.world.ClientSegmentProvider;
import org.schema.game.client.view.SegmentDrawer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.TransformableSubSprite;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.network.client.ClientState;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbstractTextBox implements Shaderable, Drawable {

	private static final float SCALE = -0.00395f;
	private final ClientState state;
	Matrix3f mY = new Matrix3f();
	Matrix3f mYB = new Matrix3f();
	Matrix3f mYC = new Matrix3f();
	Matrix3f mX = new Matrix3f();
	Matrix3f mXB = new Matrix3f();
	Matrix3f mXC = new Matrix3f();
	private float t;
	private boolean init;
	private int maxTextDistance;

	public AbstractTextBox(ClientState state) {
		this.state = state;

//		bg.setPos(125, -3, 0);

//		bg.attach(text);

		mY.setIdentity();
		mY.rotY(FastMath.HALF_PI);
		mYB.setIdentity();
		mYB.rotY(-FastMath.HALF_PI);
		mYC.setIdentity();
		mYC.rotY(FastMath.PI);

		mX.setIdentity();
		mX.rotX(FastMath.HALF_PI);
		mXB.setIdentity();
		mXB.rotX(-FastMath.HALF_PI);
		mXC.setIdentity();
		mXC.rotX(FastMath.PI);
	}

	public void update(Timer timer) {
		t += timer.getDelta() * 2.0f;

	}

	private void draw(SegmentDrawer.TextBoxSeg.TextBoxElement cont) {

		if(!Controller.getCamera().isBoundingSphereInFrustrum(cont.worldpos.origin, 2.0f)) {
			return;
		}
		//INSERTED CODE @97
		for(TextBoxDrawListener listener : FastListenerCommon.textBoxListeners) {
			listener.preDraw(cont, this);
		}
		///

		float len = Vector3fTools.diffLength(Controller.getCamera().getPos(), cont.worldpos.origin);
		if(len > maxTextDistance) {
			return;
		}

		GlUtil.glPushMatrix();

		GlUtil.glMultMatrix(cont.worldpos);

		GlUtil.scaleModelview(SCALE, SCALE, SCALE);

//		cont.text.setPos(8, 5, 0.1f);
		cont.text.setPos(8, 8, 0.1f);
//		bg.getSprite().draw();
		//INSERTED CODE @97
		for(TextBoxDrawListener listener : FastListenerCommon.textBoxListeners) {
			listener.draw(cont, this);
		}
		///
		try {
			cont.text.draw();
		} catch(NullPointerException e) {
			e.printStackTrace();
			for(Object o : cont.text.getText()) {
				try {
					System.err.println("PRINTING TEXT: " + o);
				} catch(Exception exx) {
					exx.printStackTrace();
				}
			}
		}

		GlUtil.glPopMatrix();
	}

	@Override
	public void onExit() {

	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {
		//		uniform float uTime;
		//		uniform vec2 uResolution;
		//		uniform sampler2D uDiffuseTexture;

		GlUtil.updateShaderFloat(shader, "uTime", t);
		GlUtil.updateShaderVector2f(shader, "uResolution", 20, 1000);
		GlUtil.updateShaderInt(shader, "uDiffuseTexture", 0);

	}

	public void draw(SegmentDrawer.TextBoxSeg textBoxes) {
		maxTextDistance = ((Integer) EngineSettings.MAX_DISPLAY_MODULE_TEXT_DRAW_DISTANCE.getInt()).intValue();
		for(int i = 0; i < textBoxes.pointer; i++) {
			SegmentDrawer.TextBoxSeg.TextBoxElement cont = textBoxes.v[i];

			SegmentController c = cont.c;

			long index = cont.v;

			String string = c.getTextMap().get(index);

			if(string == null) {
				//enter defaults
				cont.color.set(1, 1, 1, 1);
				cont.font = FontLibrary.FontSize.MEDIUM_15;
				cont.replacements.clear();

				cont.rawText = "loading...";
				cont.realText = "loading...";
			} else {
				if(!string.equals(cont.rawText)) {

					cont.rawText = string;
					cont.realText = string;
					String t = string;

					//enter defaults
					cont.color.set(1, 1, 1, 1);
					cont.font = FontLibrary.FontSize.MEDIUM_15;
					cont.replacements.clear();
					cont.offset.set(0, 0, 0);
					cont.holographic = true;
					cont.drawBG = true;
					cont.bgColorName = "blue";
					cont.text.setScale(new Vector3f(1.0f, 1.0f, 1.0f));

					try {
						if(t.startsWith("<style>")) {
							int end = t.indexOf("</style>");
							if(end > 0) {
								cont.realText = t.substring(end + 8);
								if(cont.realText.startsWith("\n")) {
									cont.realText = t.substring(end + 9);
								}
								String substring = t.substring(7, end);
								String[] split = substring.split(",");

								for(String s : split) {
									String[] vals = s.split("=");
									if(vals.length == 2) {
										switch(vals[0].toLowerCase(Locale.ENGLISH)) {
											case "c":
											case "color":
												Color decode = Color.decode(vals[1]);
												cont.color.set(decode.getRed() / 255.0f, decode.getGreen() / 255.0f, decode.getBlue() / 255.0f, decode.getAlpha() / 255.0f);
												break;
											case "f":
											case "font":
												int font = Integer.parseInt(vals[1]);
												switch(font) {
													case 0 -> cont.font = FontLibrary.FontSize.MEDIUM_15.getUnscaled();
													case 1 -> cont.font = FontLibrary.FontSize.MEDIUM_18.getUnscaled();
													case 2 -> cont.font = FontLibrary.FontSize.BIG_20.getUnscaled();
													case 3 -> cont.font = FontLibrary.FontSize.BIG_24.getUnscaled();
													case 4 -> cont.font = FontLibrary.FontSize.BIG_30.getUnscaled();
													case 5 -> cont.font = FontLibrary.FontSize.BIG_40.getUnscaled();
													case 6 -> cont.font = FontLibrary.FontSize.BIG_60.getUnscaled();
													case 7 -> cont.font = FontLibrary.FontSize.BIG_100.getUnscaled();
												}
												break;
											case "o":
											case "offset":
											case "p":
											case "pos":
											case "position":
												String[] split1 = vals[1].split(":");
												if(split1.length == 3) {
													float x = Float.parseFloat(split1[0]);
													float y = Float.parseFloat(split1[1]);
													float z = Float.parseFloat(split1[2]);
													x = Math.max(-10, Math.min(10, x));
													y = Math.max(-10, Math.min(10, y));
													z = Math.max(-10, Math.min(10, z));
													Vector3f offset = new Vector3f(x, y, z);
													cont.offset.set(offset);
												}
												break;
											case "h":
											case "holo":
											case "holographic":
												cont.holographic = Boolean.parseBoolean(vals[1]);
												break;
											case "bg":
											case "background":
												if(vals[1] != null && !vals[1].isEmpty()) {
													switch(vals[1].toLowerCase(Locale.ENGLISH)) {
														case "false":
															cont.drawBG = false;
															break;
														case "true":
															cont.drawBG = true;
															break;
														case "red":
															cont.setBGColor("red");
															break;
														case "green":
															cont.setBGColor("green");
															break;
														case "yellow":
															cont.setBGColor("yellow");
															break;
														case "purple":
															cont.setBGColor("purple");
															break;
														default:
															cont.setBGColor("blue");
															break;
													}
												} else cont.setBGColor("blue");
												break;
										}
									}
								}
							}
						}

						List<String> tokens = StringTools.tokenize(cont.realText, "[", "]");
						StringBuffer b = new StringBuffer(cont.realText);
						for(String s : tokens) {
//							System.err.println("TOKEN "+s);
							for(Replacements.Type type : Replacements.Type.values()) {
								check(s, b, type, cont, c);
							}

						}
						cont.buffer = b;
						cont.realText = b.toString();
					} catch(Exception e) {
						e.printStackTrace();
						cont.realText = "style error!";
					}
				}
			}

			cont.text.setFont(cont.font);
			cont.text.setColor(cont.color);

			if(!cont.replacements.isEmpty()) {
				for(int j = 0; j < cont.replacements.size(); j++) {
					Replacement replacement = cont.replacements.get(j);
					String a = replacement.get();
					cont.realText = cont.realText.replaceFirst(Pattern.quote("%&$"), a);
				}
			}

			cont.text.getText().set(0, cont.realText != null ? cont.realText : "<ERROR>");

			if(string == null) {
				((ClientSegmentProvider) c.getSegmentProvider()).getSendableSegmentProvider().clientTextBlockRequest(index);
			}

			ElementCollection.getPosFromIndex(index, cont.posBuffer);
			cont.posBuffer.x -= SegmentData.SEG_HALF;
			cont.posBuffer.y -= SegmentData.SEG_HALF;
			cont.posBuffer.z -= SegmentData.SEG_HALF;

			cont.worldpos.basis.setIdentity();
			cont.worldpos.basis.set(c.getWorldTransformOnClient().basis);
			int orientation = ElementCollection.getType(index);

			Vector3f baseOffset = new Vector3f(cont.offset);
			float sNormalDir = 0.51f + baseOffset.x;
			float sVertical = 0.51f + baseOffset.y;
			float sHorizontal = 0.5f + baseOffset.z;
			switch(orientation) {
				case Element.FRONT:
					cont.worldpos.basis.mul(mYC);
					cont.posBuffer.x -= sHorizontal;
					cont.posBuffer.y += sVertical;
					cont.posBuffer.z += sNormalDir;
					break;
				case Element.BACK:
					cont.posBuffer.x += sHorizontal;
					cont.posBuffer.y += sVertical;
					cont.posBuffer.z -= sNormalDir;
					break;
				case Element.TOP:
					cont.worldpos.basis.mul(mX);
					cont.posBuffer.x += sHorizontal;
					cont.posBuffer.y += sNormalDir;
					cont.posBuffer.z += sVertical;
					break;
				case Element.BOTTOM:
					cont.worldpos.basis.mul(mYC);
					cont.worldpos.basis.mul(mXB);
					cont.posBuffer.x -= sHorizontal;
					cont.posBuffer.y -= sNormalDir;
					cont.posBuffer.z += sVertical;
					break;
				case Element.RIGHT:
					cont.worldpos.basis.mul(mY);
					cont.posBuffer.x -= sNormalDir;
					cont.posBuffer.y += sVertical;
					cont.posBuffer.z -= sHorizontal;
					break;
				case Element.LEFT:
					cont.worldpos.basis.mul(mYB);
					cont.posBuffer.x += sNormalDir;
					cont.posBuffer.y += sVertical;
					cont.posBuffer.z += sHorizontal;
					break;
			}

			c.getWorldTransformOnClient().transform(cont.posBuffer);
			cont.worldpos.origin.set(cont.posBuffer);

			if(cont.holographic) {
				ShaderLibrary.scanlineShader.setShaderInterface(this);
				ShaderLibrary.scanlineShader.load();
			}

			if(cont.changedBgColor) {
				cont.bg.setSprite(Controller.getResLoader().getSprite("screen-gui-" + cont.bgColorName));
				cont.changedBgColor = false;
			}

			//Moved the actual drawing code into the loop to allow for stuff like different backgrounds and offsets
			//Not as fast as the old way, but it allows for more flexibility
			if(cont.drawBG) {
				for(TextBoxDrawListener listener : FastListenerCommon.textBoxListeners) {
					listener.preDrawBackground(textBoxes, this);
				}
				TransformableSubSprite[] pose = {textBoxes.v[i]};
				Sprite.draw3D(cont.bg.getSprite(), pose, 1, Controller.getCamera());
			}

			draw(textBoxes.v[i]);
			if(cont.holographic) ShaderLibrary.scanlineShader.unload();
			if(!cont.replacements.isEmpty() && cont.buffer != null) cont.realText = cont.buffer.toString();
		}
	}

	private void check(String token, StringBuffer b, Replacements.Type type, SegmentDrawer.TextBoxSeg.TextBoxElement cont, SegmentController c) {
		if("password".equals(token.toLowerCase(Locale.ENGLISH))) {
			b.delete(0, b.length());
		} else if(type.takesIndex == 0 && token.toLowerCase(Locale.ENGLISH).equals(type.var.toLowerCase(Locale.ENGLISH))) {
			int in = b.indexOf("[" + token + "]");
			b.delete(in, in + ("[" + token + "]").length());
			b.insert(in, "%&$");

			addRepl(cont, type, c, in);
		} else if(type.takesIndex > 0 && token.toLowerCase(Locale.ENGLISH).matches(type.var.toLowerCase(Locale.ENGLISH) + "[0-9]+")) {
			Pattern pattern = Pattern.compile("\\[" + type.var.toLowerCase(Locale.ENGLISH) + "[0-9]+" + "\\]");
			Matcher matcher = pattern.matcher(b);
			// Check all occurrences
			if(matcher.find()) {
//		        System.out.print("Start index: " + matcher.start());
//		        System.out.print(" End index: " + matcher.end());
//		        System.out.println(" Found: " + matcher.group());

				int in = matcher.start();
				String tStr = b.substring(in, matcher.end());
				int startIndex = tStr.toLowerCase(Locale.ENGLISH).indexOf(type.var.toLowerCase(Locale.ENGLISH)) + type.var.length();
				int endIndex = tStr.indexOf(']');
				try {
					int index = Integer.parseInt(tStr.substring(startIndex, endIndex));

					b.delete(in, matcher.end());
					b.insert(in, "%&$");

					addRepl(cont, type, c, in, index);
				} catch(NumberFormatException e) {
					System.err.println("VAR: " + type.name() + "; " + type.var);
					e.printStackTrace();
				}
			}

		}
	}

	private void addRepl(SegmentDrawer.TextBoxSeg.TextBoxElement cont, Replacements.Type t, SegmentController c, int in) {
		addRepl(cont, t, c, in, 0);
	}

	private void addRepl(SegmentDrawer.TextBoxSeg.TextBoxElement cont, Replacements.Type t, SegmentController c, int in, int index) {
		if(t.fac.ok(c)) {
			cont.replacements.add(new Replacement(in, index) {
				@Override
				public String get() {
					try {
						return t.fac.getValue(c, super.index);
					} catch(Exception ed) {
						ed.printStackTrace();
						return "ERROR(" + ed.getClass().getSimpleName() + ")";
					}
				}
			});
		}
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {

	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {
		if(init) return;
		init = true;
	}
}
