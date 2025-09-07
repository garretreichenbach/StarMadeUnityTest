package org.schema.schine;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.schema.common.ParseException;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.GraphicsFrame;
import org.schema.schine.graphicsengine.core.OpenGLWindowParams;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.xml.sax.SAXException;

/**
 * used for main menu, etc
 * @author schema
 *
 */
public abstract class GraphicsMainMenuController extends GUIObservable{

	public final GraphicsContext graphicsContext;

	
	public GraphicsMainMenuController(){
		graphicsContext = new GraphicsContext(new OpenGLWindowParams());
		
	}
	public void setFrame(GraphicsFrame frame, boolean grabMouse){
		graphicsContext.setFrame(frame, grabMouse);
	}
	
	
	public abstract void startGraphics() throws FileNotFoundException, ResourceException, ParseException, SAXException, IOException, ParserConfigurationException;
	
	
	
	public GraphicsContext getGraphicsContext() {
		return graphicsContext;
	}

}
