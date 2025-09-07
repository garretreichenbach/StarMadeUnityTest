package org.schema.game.client.controller.manager.ingame;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.buildhelper.BuildHelper;
import org.schema.game.client.view.buildhelper.BuildHelperFactory;
import org.schema.game.client.view.buildhelper.BuildHelpers;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

import com.bulletphysics.util.ObjectArrayList;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.schema.schine.resource.FileExt;

public class BuildToolsManager extends AbstractControlManager implements GUICallback {

	private final ObjectArrayList<BuildHelperFactory> buildHelperClasses = new ObjectArrayList<>();
	public String user = "";
	public Object2ObjectOpenHashMap<String, SizeSetting[]> savedMap = new Object2ObjectOpenHashMap<>();
	public final SizeSetting width;
	public final SizeSetting height;
	public final SizeSetting depth;
	public final SizeSetting orientation;
	public final FillSetting fill;
	private final SymmetryPlanes symmetryPlanes = new SymmetryPlanes();
	public boolean add = true;
	public boolean lighten = false;
	public boolean buildHelperReplace;
	public boolean showCenterOfMass;
	public boolean buildInfo;
	public boolean structureInfo = true;
	private BuildSelection selectMode;
	private boolean copyMode;
	private boolean pasteMode;
	private CopyArea copyArea;
	private short removeFilter;
	private boolean replaceRemoveFilter;
	private BuildHelper buildHelper;
	private BuildToolCreateDocking buildToolCreateDocking;
	public final SlabSetting slabSize;
	public boolean selectionPlaced;
	private FillTool fillTool;
	public boolean reactorHull;

	public BuildToolsManager(GameClientState state) {
		super(state);
		width = new SizeSetting(state);
		height = new SizeSetting(state);
		depth = new SizeSetting(state);
		orientation = new SizeSetting(state);
		slabSize = new SlabSetting(state);
		fill = new FillSetting(state);
		readBuildHelperClasses();
	}

	private void readBuildHelperClasses() {
		try {

			for(BuildHelpers c : BuildHelpers.values()) {
				buildHelperClasses.add(c);
			}
			Collections.sort(buildHelperClasses, (o1, o2) -> o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName()));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
	}

	public int getWidth() {
		return width.setting;
	}

	public int getHeight() {
		return height.setting;
	}

	public int getDepth() {
		return depth.setting;
	}

	public Vector3i getSize() {
		if(copyArea != null && pasteMode) {
			return copyArea.getSize();
		} else {
			return new Vector3i(getWidth(), getHeight(), getDepth());
		}
	}

	public Vector3f getSizef() {
		if(copyArea != null && pasteMode) {
			return copyArea.getSizef();
		} else {
			return new Vector3f(getWidth(), getHeight(), getDepth());
		}
	}

	public void load(String name) {
		SizeSetting[] sizeSettings = savedMap.get(name);

		if(sizeSettings != null) {

			width.set(sizeSettings[0].setting);
			height.set(sizeSettings[1].setting);
			depth.set(sizeSettings[2].setting);
		}

	}

	public void save(String name) {

		SizeSetting[] s = new SizeSetting[3];
		s[0] = new SizeSetting(getState());
		s[1] = new SizeSetting(getState());
		s[2] = new SizeSetting(getState());

		s[0].set(getWidth());
		s[1].set(getHeight());
		s[2].set(getDepth());

		savedMap.put(name, s);
	}

	public void reset() {
		System.err.println("[CLIENT][BUILDTOOLS] Reset area placement");
		width.reset();
		height.reset();
		depth.reset();
		copyMode = false;
		pasteMode = false;
		cancelCreateDockingMode();

	}

	public PlayerInteractionControlManager getInteractionControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager();
	}

	public boolean canUndo() {
		return getInteractionControlManager().canUndo();
	}

	public void undo() {
		getInteractionControlManager().undo();
	}

	public boolean isUndoRedoOnCooldown() {
		return getInteractionControlManager().isUndoRedoOnCooldown();
	}

	public void redo() {
		getInteractionControlManager().redo();
	}

	public boolean canRedo() {
		return getInteractionControlManager().canRedo();
	}

	public boolean isAddMode() {
		return add && !copyMode && !(buildHelper != null && !buildHelper.placed);
	}

	/**
	 * @return the copyMode
	 */
	public boolean isCopyMode() {
		return copyMode;
	}

	/**
	 * @param copyMode the copyMode to set
	 */
	public void setCopyMode(boolean copyMode) {
		this.copyMode = copyMode;
	}

	/**
	 * @return the pasteMode
	 */
	public boolean isPasteMode() {
		return pasteMode;
	}

	/**
	 * @param pasteMode the pasteMode to set
	 */
	public void setPasteMode(boolean pasteMode) {
		this.pasteMode = pasteMode;
	}

	public boolean canPaste() {
		return copyArea != null && copyArea.getPieces().size() > 0;
	}

	public void saveCopyArea(String name) throws IOException {
		if(copyArea != null) {
			copyArea.save(name);
		}
	}

	public void loadCopyArea(String namewithoutSuffix) throws IOException {
		this.copyArea = new CopyArea();
		copyArea.load(namewithoutSuffix);
	}

	public void loadCopyArea(File name) throws IOException {
		this.copyArea = new CopyArea();
		copyArea.load(name);
	}

	public void setCopyArea2vectors(SegmentController con, Vector3i cornerA, Vector3i cornerB, int maxSize) {
		if(maxSize > 0) {
			//shrinking maxSize as selection includes borders
			maxSize -= 1;
			//shrinking 2nd vector coordinates to fit within the maxSize
			cornerB.x = cornerA.x > cornerB.x ? Math.max(cornerB.x, cornerA.x - maxSize) : Math.min(cornerB.x, cornerA.x + maxSize);
			cornerB.y = cornerA.y > cornerB.y ? Math.max(cornerB.y, cornerA.y - maxSize) : Math.min(cornerB.y, cornerA.y + maxSize);
			cornerB.z = cornerA.z > cornerB.z ? Math.max(cornerB.z, cornerA.z - maxSize) : Math.min(cornerB.z, cornerA.z + maxSize);
		}

		int minX = Math.min(cornerA.x, cornerB.x);
		int minY = Math.min(cornerA.y, cornerB.y);
		int minZ = Math.min(cornerA.z, cornerB.z);

		int maxX = Math.max(cornerA.x, cornerB.x);
		int maxY = Math.max(cornerA.y, cornerB.y);
		int maxZ = Math.max(cornerA.z, cornerB.z);

		System.out.println("Copy Area, cornerA " + cornerA + " cornerB " + cornerB);

		this.copyArea = new CopyArea();
		copyArea.copyArea(con, new Vector3i(minX, minY, minZ), new Vector3i(maxX, maxY, maxZ));
	}

	public void setCopyArea(SegmentController con, Vector3i origPosToBuild, Vector3i size) {
		//Shrinking area 
		size.x -= Math.signum(size.x);
		size.y -= Math.signum(size.y);
		size.z -= Math.signum(size.z);

		int minX = Math.min(origPosToBuild.x, origPosToBuild.x + size.x);
		int minY = Math.min(origPosToBuild.y, origPosToBuild.y + size.y);
		int minZ = Math.min(origPosToBuild.z, origPosToBuild.z + size.z);

		int maxX = Math.max(origPosToBuild.x, origPosToBuild.x + size.x);
		int maxY = Math.max(origPosToBuild.y, origPosToBuild.y + size.y);
		int maxZ = Math.max(origPosToBuild.z, origPosToBuild.z + size.z);

		this.copyArea = new CopyArea();
		copyArea.copyArea(con, new Vector3i(minX, minY, minZ), new Vector3i(maxX, maxY, maxZ));
	}

	/**
	 * @return the copyArea
	 */
	public CopyArea getCopyArea() {
		return copyArea;
	}

	/**
	 * @param copyArea the copyArea to set
	 */
	public void setCopyArea(CopyArea copyArea) {
		this.copyArea = copyArea;
	}

	public boolean isSelectMode() {
		return selectMode != null;
	}

	public BuildSelection getSelectMode() {
		return selectMode;
	}

	public void setSelectMode(BuildSelection selectMode) {
		this.selectMode = selectMode;
	}

	/**
	 * @return the removeFilter
	 */
	public short getRemoveFilter() {
		return removeFilter;
	}

	/**
	 * @param removeFilter the removeFilter to set
	 */
	public void setRemoveFilter(short removeFilter) {
		this.removeFilter = removeFilter;
	}

	/**
	 * @return the replaceRemoveFilter
	 */
	public boolean isReplaceRemoveFilter() {
		return replaceRemoveFilter;
	}

	/**
	 * @param replaceRemoveFilter the replaceRemoveFilter to set
	 */
	public void setReplaceRemoveFilter(boolean replaceRemoveFilter) {
		this.replaceRemoveFilter = replaceRemoveFilter;
	}

	public ObjectArrayList<BuildHelperFactory> getBuildHelperClasses() {
		return buildHelperClasses;
	}

	public BuildHelper getBuildHelper() {
		return buildHelper;
	}

	public void setBuildHelper(BuildHelper buildHelper) {
		this.buildHelper = buildHelper;
	}

	public boolean isInCreateDockingMode() {
		return buildToolCreateDocking != null;
	}

	public void startCreateDockingMode() {
		if(getState().getShip() != null && getState().getShip().getDockingController().isInAnyDockingRelation()) {
			getState().getController().popupAlertTextMessage(Lng.str("Can't mix old and new docking"), 0);
		} else {
			this.buildToolCreateDocking = new BuildToolCreateDocking();
		}
	}

	public void cancelCreateDockingMode() {
		this.buildToolCreateDocking = null;
	}

	public String getCreateDockingModeMsg() {
		if(!isInCreateDockingMode()) {
			return Lng.str("Create Docking");
		} else {
			return this.buildToolCreateDocking.getButtonMsg();
		}
	}

	public BuildToolCreateDocking getBuildToolCreateDocking() {
		return buildToolCreateDocking;
	}

	public FillTool getFillTool() {
		return fillTool;
	}

	public void setFillTool(FillTool fillTool) {
		this.fillTool = fillTool;
	}

	public SymmetryPlanes getSymmetryPlanes() {
		return symmetryPlanes;
	}

	public boolean isCameraDroneDisplayName() {
		return EngineSettings.CAMERA_DRONE_DISPLAY_NAMES.isOn();
	}

	public void setCameraDroneDisplayName(boolean b) {
		EngineSettings.CAMERA_DRONE_DISPLAY_NAMES.setOn(b);
		try {
			EngineSettings.write();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public boolean copyAreaExistsAlready(String name) {
		return (new FileExt("./templates/" + name + ".smtpl")).exists();
	}
}
