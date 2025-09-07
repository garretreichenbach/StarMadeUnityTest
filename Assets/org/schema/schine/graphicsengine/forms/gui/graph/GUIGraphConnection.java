package org.schema.schine.graphicsengine.forms.gui.graph;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.texture.Texture;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

public class GUIGraphConnection {

	public final GUIGraphElement a;
	public final GUIGraphElement b;
	private final Vector4f lineColor;
	private LineStyle lineStyle = LineStyle.SOLID;
	private Texture tex;
	private float texCordXEnd;
	private float texCordXStart;
	public float correctVertical;
	private static boolean debug = false;
	static int currentTexture;
	private static Vector2f dir = new Vector2f();
	public enum LineStyle{
		SOLID,
		DOTTED,
		TEXTURED,
	}

	public GUIGraphConnection(GUIGraphElement a, GUIGraphElement b) {
		this(a, b, new Vector4f(1, 1, 1, 1));
	}

	public GUIGraphConnection(GUIGraphElement a, GUIGraphElement b, Vector4f lineColor) {
		super();
		this.a = a;
		this.b = b;
		this.lineColor = new Vector4f(lineColor);
	}

	/**
	 * @return the color
	 */
	public Vector4f getLineColor() {
		return lineColor;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return a.hashCode() + b.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return a.equals(((GUIGraphConnection) obj).a) && b.equals(((GUIGraphConnection) obj).b);
	}

	public LineStyle getLineStyle() {
		return lineStyle;
	}

	public void setLineStyle(LineStyle lineStyle) {
		this.lineStyle = lineStyle;
	}
	private void switchMode(){
		err();
		int oldTexture = currentTexture;
		err();
		if(lineStyle == LineStyle.TEXTURED) currentTexture = getTextureID();
		else currentTexture = -1;
		err();
		if(oldTexture != currentTexture ){
			if(currentTexture != -1){
				err();
				GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
				GlUtil.glEnable(GL11.GL_TEXTURE_2D);
				GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
				GlUtil.glDisable(GL11.GL_LIGHTING);
				GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture);
				
				ShaderLibrary.graphConnectionShader.loadWithoutUpdate();
				GlUtil.updateShaderInt(ShaderLibrary.graphConnectionShader, "barTex", 0);
				err();
			}else{
				ShaderLibrary.graphConnectionShader.unloadWithoutExit();
				GlUtil.glDisable(GL11.GL_TEXTURE_2D);
				GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
				GlUtil.glDisable(GL11.GL_LIGHTING);
				GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			}
		}
		
		err();
		
		
		
	}
	public static void err() {
		if(debug){
			System.err.println("DEBUG");
			GlUtil.printGlErrorCritical();
		}
	}

	public void setTextured(Texture tex, int yIndex, int yMax){
		if(tex != null){
			this.tex = tex;
			float one = 1f/yMax;
			texCordXStart = one * yIndex;
			texCordXEnd = one * (yIndex + 1);
			lineStyle = LineStyle.TEXTURED;
		}else{
			lineStyle = LineStyle.SOLID;
		}
	}
	private int getTextureID() {
		assert(this.tex != null);
		return this.tex.getTextureId();
	}

	public void draw(float arrowSize) {
		Vector2f pA;
		Vector2f pB;
		err();
		GlUtil.glColor4f(lineColor);
		err();
		if(lineStyle == LineStyle.TEXTURED){
			pA = a.getCenter();
			pB = b.getCenter();
			if(correctVertical != 0){
				dir.sub(pB, pA);
				dir.normalize();
				
				float angle = dir.angle(new Vector2f(0,1));
				if(angle > 0.000001f && Math.abs(angle) < correctVertical){
					pA.x = pB.x;
				}
			}
		}else{
			pA = a.getBoxIntersectionTo(b.getCenter());
			if(pA == null){
				System.err.println("NO INTER::: "+a.getCenter()+" -> "+b.getCenter()+"; "+a+"; "+b+"; "+a.getPos()+"; "+b.getPos());
				return;
			}
			pB = b.getBoxIntersectionTo(a.getCenter());
			if(pB == null){
				System.err.println("NO INTER::: "+b.getCenter()+" -> "+a.getCenter()+"; "+b+"; "+a+"; "+b.getPos()+"; "+a.getPos());
				return;
			}
		}

		dir.sub(pB, pA);
		Vector2f normA = new Vector2f(-dir.y, dir.x);
		Vector2f normB = new Vector2f(dir.y, -dir.x);
		normA.normalize();
		normB.normalize();

		normA.scale(arrowSize);
		normB.scale(arrowSize);

		dir.negate();
		dir.normalize();
		dir.scale(arrowSize);

		normA.add(dir);
		normB.add(dir);

		normA.normalize();
		normB.normalize();

		normA.scale(arrowSize);
		normB.scale(arrowSize);
		
		switchMode();
		err();
		if (pA != null && pB != null) {
			switch(lineStyle) {
				case DOTTED -> {
					GL11.glBegin(GL11.GL_LINES);
					dir.sub(pB, pA);
					float length = dir.length();
					float dotLen = 5;
					dir.normalize();
					for(float f = 0; f < length; f += dotLen * 2) {
						GL11.glVertex2f(pA.x + dir.x * f, pA.y + dir.y * f);
						float fEnd = Math.min(length, f + dotLen);
						GL11.glVertex2f(pA.x + dir.x * fEnd, pA.y + dir.y * fEnd);
					}
					doArrowTip(pB, normA, normB);
					GL11.glEnd();
				}
				case SOLID -> {
					GL11.glBegin(GL11.GL_LINES);
					GL11.glVertex2f(pA.x, pA.y);
					GL11.glVertex2f(pB.x, pB.y);
					doArrowTip(pB, normA, normB);
					GL11.glEnd();
				}
				case TEXTURED -> {
					err();
					dir.sub(pB, pA);
					normA.set(-dir.y, dir.x);
					normB.set(dir.y, -dir.x);
					normA.normalize();
					normB.normalize();
					normA.scale(arrowSize);
					normB.scale(arrowSize);
					float yCoord = dir.length() / tex.getHeight();
					float x0 = texCordXStart;
					float x1 = texCordXEnd;
					float y0 = 0;
					float y1 = yCoord;
					err();
					GlUtil.updateShaderVector4f(ShaderLibrary.graphConnectionShader, "boxA", a.getAsClipPaneScreen());
					GlUtil.updateShaderVector4f(ShaderLibrary.graphConnectionShader, "boxB", b.getAsClipPaneScreen());
					GlUtil.updateShaderVector4f(ShaderLibrary.graphConnectionShader, "clipPlane", GlUtil.getClip());
					GlUtil.updateShaderVector2f(ShaderLibrary.graphConnectionShader, "scrollPos", GlUtil.scrollX, GlUtil.scrollY);
					GL11.glBegin(GL11.GL_QUADS);
					GL11.glTexCoord2f(x0, y1);
					GL11.glVertex2f(pA.x + normB.x, pA.y + normB.y);
					GL11.glTexCoord2f(x1, y1);
					GL11.glVertex2f(pA.x + normA.x, pA.y + normA.y);
					GL11.glTexCoord2f(x1, y0);
					GL11.glVertex2f(pB.x + normA.x, pB.y + normA.y);
					GL11.glTexCoord2f(x0, y0);
					GL11.glVertex2f(pB.x + normB.x, pB.y + normB.y);
					GL11.glEnd();
					err();
				}
				default -> throw new RuntimeException("Unknown Style");
			}
			
			err();
			
		} else {
			System.err.println("Graph no intersection " + a + ", " + b + "" + "; " + a.getCenter() + "; " + b.getCenter());
		}
		err();
	}		
	
	private void doArrowTip(Vector2f pB, Vector2f normA, Vector2f normB){
		GL11.glVertex2f(pB.x, pB.y);
		GL11.glVertex2f(pB.x + normA.x, pB.y + normA.y);

		GL11.glVertex2f(pB.x, pB.y);
		GL11.glVertex2f(pB.x + normB.x, pB.y + normB.y);
	}

	public static void afterDraw() {
		err();
		if(currentTexture != -1){
			err();
			ShaderLibrary.graphConnectionShader.unloadWithoutExit();
			err();
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			err();
			GUIGraphConnection.currentTexture = -1;
		}
		err();
	}

	public void cleanUp() {
	}

}
