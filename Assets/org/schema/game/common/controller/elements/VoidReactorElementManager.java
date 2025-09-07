package org.schema.game.common.controller.elements;

import java.lang.reflect.Constructor;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;

public class VoidReactorElementManager<
		E extends ElementCollection<E, CM, VoidElementManager<E, CM>>,
		CM extends ElementCollectionManager<E, CM, VoidElementManager<E, CM>>>
		extends VoidElementManager<E, CM> {

	private final short reactorType;

	public VoidReactorElementManager(short type, SegmentController segmentController, Class<CM> clazz) {
		super(segmentController, clazz);
		this.reactorType = type;
		assert(ElementKeyMap.isChamber(type)):type;
	}

	@Override
	public CM getNewCollectionManager(SegmentPiece position, Class<CM> clazz) {
		try {
			Constructor<CM> constructor = clazz.getConstructor(Short.TYPE, SegmentController.class, VoidReactorElementManager.class);
			return constructor.newInstance(reactorType, getSegmentController(), this);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(clazz.getName(), e);
		}
	}


	

}
