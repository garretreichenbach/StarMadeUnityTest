package org.schema.game.client.view.buildhelper;

import org.schema.schine.graphicsengine.forms.Transformable;

public enum BuildHelpers implements BuildHelperFactory{
	CIRCLE(new BuildHelperFactory() {
		@Override
		public BuildHelper getInstance(Transformable trans) {
			return new CircleBuildHelper(trans);
		}

		@Override
		public Class<? extends BuildHelper> getBuildHelperClass() {
			return CircleBuildHelper.class;
		}
	}), 
	LINE(new BuildHelperFactory() {
		@Override
		public BuildHelper getInstance(Transformable trans) {
			return new LineBuildHelper(trans);
		}
		@Override
		public Class<? extends BuildHelper> getBuildHelperClass() {
			return LineBuildHelper.class;
		}
	}), 
	ELLIPSOID(new BuildHelperFactory() {
		@Override
		public BuildHelper getInstance(Transformable trans) {
			return new EllipsoidBuildHelper(trans);
		}
		@Override
		public Class<? extends BuildHelper> getBuildHelperClass() {
			return EllipsoidBuildHelper.class;
		}
	}), 
	TORUS(new BuildHelperFactory() {
		@Override
		public BuildHelper getInstance(Transformable trans) {
			return new TorusBuildHelper(trans);
		}
		@Override
		public Class<? extends BuildHelper> getBuildHelperClass() {
			return TorusBuildHelper.class;
		}
	}), 
	
	
	
	;
	private final BuildHelperFactory fac;
	private BuildHelpers(BuildHelperFactory p) {
		this.fac = p;
	}
	@Override
	public BuildHelper getInstance(Transformable trans) {
		return fac.getInstance(trans);
	}
	@Override
	public Class<? extends BuildHelper> getBuildHelperClass() {
		return fac.getBuildHelperClass();
	}
	

}
