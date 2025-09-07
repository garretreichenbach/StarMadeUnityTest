package org.schema.schine.graphicsengine.core.settings;

public enum Resolutions implements ResolutionInterface{

	SVGA(new Resolution("SVGA", 800, 600)),

	XGA(new Resolution("XGA", 1024, 768)),

	SVGA5(new Resolution("SVGA", 1152, 720)),

	WXGA(new Resolution("WXGA", 1280, 720)),

	WXGA2(new Resolution("WXGA", 1280, 800)),

	SXGA_UVGA(new Resolution("SXGA–(UVGA)", 1280, 960)),

	SXGA(new Resolution("SXGA", 1280, 1024)),

	HD(new Resolution("HD", 1360, 768)),

	HD2(new Resolution("HD", 1366, 768)),

	WXGA_PLUS(new Resolution("WXGA+", 1440, 900)),

	HD_PLUS(new Resolution("HD+", 1600, 900)),

	UXGA(new Resolution("UXGA", 1600, 1200)),

	FHD(new Resolution("FHD", 1920, 1080)),

	WUXGA(new Resolution("WUXGA", 1920, 1200)),

	WQHD(new Resolution("WQHD", 2560, 1440)),

	FOURK3(new Resolution("WQHD2", 2560, 1080)),

	WSXGA_PLUS(new Resolution("WSXGA+", 1680, 1050)),

	FOURK11(new Resolution("4k-1", 2560, 1600)),
	
	FOURK111(new Resolution("4k-6", 3440, 1440)),
	
	FOURK1(new Resolution("4k-1", 3840, 2160)),

	FOURK2(new Resolution("4k-2", 4096, 2160)),

	FOURK33(new Resolution("4k-3", 3440, 1440 )),
	
	FOURK4(new Resolution("4k-4", 3840, 1024)),

	FOURK5(new Resolution("4k-5", 3480, 2160)),

	FOURK12(new Resolution("4k-5",  5760, 1080)),;

	
	
	public final Resolution resolution;

	public float getAspect() {
		return resolution.getAspect();
	}

	public String getName() {
		return resolution.getName();
	}

	public int getWidth() {
		return resolution.getWidth();
	}

	public int getHeight() {
		return resolution.getHeight();
	}

	private Resolutions(Resolution resolution) {
		this.resolution = resolution;
	}

	@Override
	public String toString() {
		return resolution.toString();
	}
	public static Resolution[] getResolutions(){
		Resolution[] res = new Resolution[Resolutions.values().length];
		for(int i = 0; i < Resolutions.values().length; i++){
			Resolutions s = Resolutions.values()[i];
			
			res[i] = new Resolution(s.name(), s.resolution.width, s.resolution.height);
		}
		return res;
	}

	@Override
	public String getStringID() {
		return this.resolution.getStringID();
	}

}
