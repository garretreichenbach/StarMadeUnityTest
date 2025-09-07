package org.schema.schine.graphicsengine.forms.gui.newgui;

import java.awt.Polygon;
import java.util.List;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector4f;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Keyboard;
import org.schema.schine.input.Mouse;

import com.bulletphysics.util.ObjectArrayList;

import PolygonStatsInterface.PolygonStatsEditableInterface;

public class GUIPolygonStats extends GUIElement  {

	private static final float minVal = 0.05f;
	private final PolygonStatsInterface p;
	private final List<GUITextOverlay> texts = new ObjectArrayList<GUITextOverlay>();
	private int height;
	private int width;
	private boolean recreate = true;
	private boolean init;
	private float innerWidthSub = 10;
	private float innerHeightSub = 10;
	private Vector4f bgPolyColor = new Vector4f(0.4f, 0.4f, 0.4f, 1.0f);
	private Vector4f bgPolyOutlineColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
	private Vector4f bgPolyInnerColor = new Vector4f(0.4f, 0.8f, 0.4f, 0.5f);
	private Vector4f bgPolyInnerOutlineColor = new Vector4f(0.1f, 0.1f, 0.1f, 0.7f);
	private int diaplayListIndex;
	private PolygonStatsEditableInterface editable;
	private boolean wasDown;
	private boolean pIns;
	public GUIPolygonStats(InputState state, PolygonStatsInterface p) {
		this(state, p, 128, 128);
	}

	public GUIPolygonStats(InputState state, PolygonStatsInterface p, int width, int height) {
		super(state);
		this.width = width;
		this.height = height;
		this.p = p;
		
		
		
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)){
			recreate = true;
		}
		
		if (recreate) {
			recreate();
		}

		GlUtil.glPushMatrix();
		transform();
		if(isEditable()){
			checkMouseInside();
		}
		GlUtil.glPushMatrix();
		GlUtil.translateModelview((int)(getWidth() / 2), (int)(getHeight() / 2), 0);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // default

		//draw polygon with stats
		GL11.glCallList(diaplayListIndex);

		GlUtil.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glDisable(GL11.GL_BLEND);

		udpateTextsPos();
		for (int i = 0; i < texts.size(); i++) {
			texts.get(i).draw();
		}

		GlUtil.glPopMatrix();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		if (init) {
			return;
		}
		if (recreate) {
			recreate();
		}

		init = true;
	}

	private void recreate() {

		int num = p.getDataPointsNum();

		float steps = FastMath.TWO_PI / num;

		if (diaplayListIndex != 0) {
			GL11.glDeleteLists(this.diaplayListIndex, 1);
		}
		this.diaplayListIndex = GL11.glGenLists(1);

		GL11.glNewList(diaplayListIndex, GL11.GL_COMPILE);

		GL11.glBegin(GL11.GL_POLYGON);
		GlUtil.glColor4f(bgPolyColor);
		if(isEditable()){
			doOutlineCycle(false);
		}else{
			doOutline(false);
		}
		GL11.glEnd();

		GL11.glBegin(GL11.GL_LINE_LOOP);
		GlUtil.glColor4f(bgPolyOutlineColor);
		if(isEditable()){
			doOutlineCycle(false);
		}else{
			doOutline(false);
		}
		GL11.glEnd();

		GL11.glBegin(GL11.GL_LINES);
		GlUtil.glColor4f(bgPolyInnerOutlineColor);
//		if(isEditable()){
//			GlUtil.glColor4f(1,1,1,1);
//			doEditableCross(false);
//		}else{
			doCross(false);
//		}
		GL11.glEnd();

//		if(!isEditable()){
			GL11.glBegin(GL11.GL_POLYGON);
			GlUtil.glColor4f(bgPolyInnerColor);
			doOutline(true);
			GL11.glEnd();
	
			GL11.glBegin(GL11.GL_LINE_LOOP);
			GlUtil.glColor4f(bgPolyInnerOutlineColor);
			doOutline(true);
			GL11.glEnd();
//		}

		recreate = false;

		GL11.glEndList();

		updateTextOutline();
	}

	public void updateTextOutline() {
		texts.clear();
		int num = p.getDataPointsNum();
		for (int i = 0; i < num; i++) {

			GUITextOverlay t = new GUITextOverlay(FontSize.SMALL_13, getState());
			t.setTextSimple(p.getValueName(i));
			t.onInit();
//			t.draw();
			texts.add(t);
		}
		udpateTextsPos();
		
		
	}

	private void udpateTextsPos() {
		int num = p.getDataPointsNum();
		float steps = FastMath.TWO_PI / num;
		int i = 0;
		for (float rad = 0; rad < FastMath.TWO_PI; rad += steps) {
			texts.get(i).setColor(1, 1, 1, 0);
			texts.get(i).draw();
			float x = -FastMath.cos(rad) * ((getWidth()/2f - innerWidthSub) * getP(i, false));
			float y = FastMath.sin(rad) * ((getHeight()/2f - innerHeightSub) * getP(i, false));
			if (Math.abs(x) < 5) {
				x = (int) (x - texts.get(i).getMaxLineWidth() / 2);
			} else if (x < 0) {
				x = (int) (x - texts.get(i).getMaxLineWidth());
			} else {

			}
			texts.get(i).setPos((int)x, (int) (y - texts.get(i).getTextHeight() / 2), 0);
			texts.get(i).draw();
			texts.get(i).setColor(1, 1, 1, 1);
			i++;
		}
	}

	public void doOutline(boolean percental) {
		int num = p.getDataPointsNum();
		float steps = FastMath.TWO_PI / num;
		int i = 0;
		for (float rad = 0; rad < FastMath.TWO_PI; rad += steps) {
			float x = -FastMath.cos(rad) * ((getWidth()/2f - innerWidthSub) * getP(i, percental));
			float y = FastMath.sin(rad) * ((getHeight()/2f - innerHeightSub) * getP(i, percental));
			GL11.glVertex2f((int) x, (int) y);
			i++;
		}
	}
	public void doOutlineCycle(boolean percental) {
		int num = p.getDataPointsNum();
		float steps = FastMath.TWO_PI / 32;
		int i = 0;
		for (float rad = 0; rad < FastMath.TWO_PI; rad += steps) {
			float x = -FastMath.cos(rad) * ((getWidth()/2f - innerWidthSub) * getP(i, percental));
			float y = FastMath.sin(rad) * ((getHeight()/2f - innerHeightSub) * getP(i, percental));
			GL11.glVertex2f((int) x, (int) y);
			i++;
		}
	}

	public void doEditableCross(boolean percental) {
		int dataPointsNum = p.getDataPointsNum();
		assert(dataPointsNum == 3);
		
		float hwidth = getWidth() / 2;
		float hheight = getHeight() / 2;
		
		Vector2d[] centers = new Vector2d[dataPointsNum];
		float[] radis = new float[dataPointsNum];
		Vector2d tc = new Vector2d(hwidth, hheight);
		double baselen = tc.length();
		int num = p.getDataPointsNum();
		float steps = FastMath.TWO_PI / num;
		int i = 0;
//		float sideLen = 0;
//		for (float rad = 0; rad < FastMath.TWO_PI; rad += steps) {
//			float x = -FastMath.cos(rad) * ((getWidth()/2f - innerWidthSub) * getP(i, false));
//			float y = FastMath.sin(rad) * ((getHeight()/2f - innerHeightSub) * getP(i, false));
//			centers[i] = new Vector2d(hwidth + x, hheight + y);
//			
////			x -= -FastMath.cos(rad+steps) * ((getWidth()/2f - innerWidthSub) * getP(i, false));
////			y -= FastMath.sin(rad+steps) * ((getHeight()/2f - innerHeightSub) * getP(i, false));
//			sideLen += Math.sqrt(x*x+y*y);
//			
//			
//			
//			i++;
//		}
//		i = 0;
//		for (float rad = 0; rad < FastMath.TWO_PI; rad += steps) {
//			float x = -FastMath.cos(rad) * ((getWidth()/2f - innerWidthSub) * getP(i, false));
//			float y = FastMath.sin(rad) * ((getHeight()/2f - innerHeightSub) * getP(i, false));
//			x -= -FastMath.cos(rad+steps) * ((getWidth()/2f - innerWidthSub) * getP(i, false));
//			y -= FastMath.sin(rad+steps) * ((getHeight()/2f - innerHeightSub) * getP(i, false));
//			
//			float pc = 1.0f-getP(i, true);
//			System.err.println("III "+pc);
//			radis[i] = (float) (sideLen/3f  * pc)*1.76f;
//			i++;
//		}
//		
//		for(int j = 0; j < centers.length; j++){
//			for (float rad = 0; rad < FastMath.TWO_PI+FastMath.TWO_PI/18d; rad += FastMath.TWO_PI/18d) {
//				
//				Vector2d cen = centers[j];
//				float radius = radis[j];
//				
//				float x = (float) (-FastMath.cos(rad) * radius + cen.x - hwidth);
//				float y = (float) (FastMath.sin(rad) * radius + cen.y - hheight);
//				
//				GL11.glVertex2f((int) x, (int) y);
//				
//				x = (float) (-FastMath.cos((float) (rad+FastMath.TWO_PI/18d)) * radius + cen.x - hwidth);
//				y = (float) (FastMath.sin((float) (rad+FastMath.TWO_PI/18d)) * radius + cen.y - hheight);
//				GL11.glVertex2f((int) x, (int) y);
//			}
//		}
//		
			
			
			
			
//			i = 0;
//			for (float rad = 0; rad < FastMath.TWO_PI; rad += steps) {
//				GL11.glVertex2f((int)(0), (int)(0));
//				float x = -FastMath.cos(rad) * ((getWidth()/2f - innerWidthSub) * getP(i, percental));
//				float y = FastMath.sin(rad) * ((getHeight()/2f - innerHeightSub) * getP(i, percental));
//				Vector2f b = new Vector2f()
//				GL11.glVertex2f((int) x, (int) y);
//				i++;
//			}
		
		
		
	}
	public void doCross(boolean percental) {
		int num = p.getDataPointsNum();
		float steps = FastMath.TWO_PI / num;
		int i = 0;
		for (float rad = 0; rad < FastMath.TWO_PI; rad += steps) {
			GL11.glVertex2f(0, 0);
			float x = -FastMath.cos(rad) * ((getWidth()/2f - innerWidthSub) * getP(i, percental));
			float y = FastMath.sin(rad) * ((getHeight()/2f - innerHeightSub) * getP(i, percental));
			GL11.glVertex2f((int) x, (int) y);
			i++;
		}
	}

	private float getP(int i, boolean percental) {
		return (percental ? Math.max(minVal, (float) p.getPercent(i)) : 1);
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public float getWidth() {
		return width;
	}

	public void setWidth(int width) {
		recreate = true;
		this.width = width;
	}

	public void setHeight(int height) {
		recreate = true;
		this.height = height;
	}

	public boolean isEditable() {
		return editable != null;
	}

	public void setEditable(PolygonStatsEditableInterface editable) {
		this.editable = editable;
		
		if(editable != null){
			setCallback(new EditableCallBack());
		}
	}
	
	
	public void update(){
		if(isEditable() && isInside() && pIns){
			float hwidth = getWidth() / 2;
			float hheight = getHeight() / 2;
//			System.err.println("___ "+(getRelMousePos().x - hwidth)+"; "+(getRelMousePos().y - hheight)+" :::: "+Math.abs(getRelMousePos().x - hwidth)+" :x: "+Math.abs(getRelMousePos().y- hheight)+" ;;; "+hwidth+", "+hheight); 
			float a = Math.abs(getRelMousePos().x - hwidth);
			float b = Math.abs(getRelMousePos().y- hheight);
			
			float aM = ((getWidth()/2f - innerWidthSub));
			float ll = FastMath.carmackSqrt((a*a)+(b*b));
//			System.err.println("LEN:: "+ll+"/"+aM);
			if(ll< aM+3){
				Polygon pol = new Polygon();
				int num = p.getDataPointsNum();
				float steps = FastMath.TWO_PI / num;
				int i = 0;
				float distTotal = 0;
				for (float rad = 0; rad < FastMath.TWO_PI; rad += steps) {
					float x = -FastMath.cos(rad) * ((getWidth()/2f - innerWidthSub) * getP(i, false));
					float y = FastMath.sin(rad) * ((getHeight()/2f - innerHeightSub) * getP(i, false));
					
					float x2 = -FastMath.cos(rad+steps) * ((getWidth()/2f - innerWidthSub) * getP(i, false));
					float y2 = FastMath.sin(rad+steps) * ((getHeight()/2f - innerHeightSub) * getP(i, false));
					
					float d1 = x - x2;
					float d2 = y - y2;
					
					distTotal = FastMath.sqrt(d1*d1 + d2*d2);
					
					pol.addPoint((int)(hwidth + x), (int)(hheight + y));
					
					
					float xDist = x - getRelMousePos().x;
					float yDist = y - getRelMousePos().y;
					i++;
				}
				i = 0;
				float totLen = 0;
				for (float rad = 0; rad < FastMath.TWO_PI; rad += steps) {
					float x = -FastMath.cos(rad) * ((getWidth()/2f - innerWidthSub) * getP(i, false))+hwidth;
					float y = FastMath.sin(rad) * ((getHeight()/2f - innerHeightSub) * getP(i, false))+hheight;
					
					float xDist = x - getRelMousePos().x;
					float yDist = y - getRelMousePos().y;
					float len = FastMath.sqrt(xDist*xDist + yDist*yDist);
					totLen += len;
					i++;
				}
				totLen /= 3f;
				totLen += 0.5f * totLen;
				i = 0;
				float percTot = 0;
				for (float rad = 0; rad < FastMath.TWO_PI; rad += steps) {
					float x = -FastMath.cos(rad) * ((getWidth()/2f - innerWidthSub) * getP(i, false))+hwidth;
					float y = FastMath.sin(rad) * ((getHeight()/2f - innerHeightSub) * getP(i, false))+hheight;
					
					float xDist = x - getRelMousePos().x;
					float yDist = y - getRelMousePos().y;
					
					float mX = getRelMousePos().x-hwidth; 
					float mY = getRelMousePos().y-hheight;
					if(Mouse.isPrimaryMouseDownUtility() ){
						
						
						if(Math.sqrt(mX*mX+mY*mY) <= Math.sqrt(x*x+y*y)){
							wasDown = true;
						}
						
						if(wasDown && Math.sqrt(mX*mX+mY*mY) > Math.sqrt(x*x+y*y)){
							mX /= Math.sqrt(mX*mX+mY*mY); 
							mY /= Math.sqrt(mX*mX+mY*mY);
							
							mX*=Math.sqrt(x*x+y*y);
							mY*=Math.sqrt(x*x+y*y);
							
							xDist = x - (mX + hwidth);
							yDist = y - (mY + hheight);
						}
						
						if(Math.sqrt(mX*mX+mY*mY) <= Math.sqrt(x*x+y*y)){
							float len = FastMath.sqrt(xDist*xDist + yDist*yDist);
							
							float perc = ( len / totLen);
							
	//						System.err.println("L LL LL: "+i+" :: "+len+"; "+totLen+";; "+perc);
							
							editable.setPercent(i, perc);
							
							percTot += perc;
							
							recreate = true;
						}
						
					}else{
						wasDown = false;
					}
					i++;
				}
			}
		}else if(!isInside() && !Mouse.isPrimaryMouseDownUtility()){
			pIns = false;
		}
	}
	
	private class EditableCallBack implements GUICallback{

		

		@Override
		public void callback(GUIElement callingGuiElement, MouseEvent event) {
			if(event.pressedLeftMouse()){
				pIns = true;
			}else if(event.releasedLeftMouse()){
				pIns = false;
			}
			
		}

		@Override
		public boolean isOccluded() {
			return false;
		}
		
	}
}
