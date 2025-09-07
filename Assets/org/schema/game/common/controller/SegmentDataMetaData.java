package org.schema.game.common.controller;


import api.listener.events.block.SegmentPieceAddByMetadataEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.armorhp.ArmorHPCollection;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementInformation.ResourceInjectionType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataInterface;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.util.Arrays;

public class SegmentDataMetaData {
	private int pointer;
	private final long[] absIndex = new long[SegmentData.BLOCK_COUNT];
	private final short[] types = new short[SegmentData.BLOCK_COUNT];
	private final Vector3f centerOfMassUnweighted = new Vector3f();
	private float totalPhysicalMass;
	private int[] counts = new int[ElementKeyMap.highestType + 1];
	private final int[] oreCounts = new int[32];

	private int totalElements;
	private final LongArrayList textBlocks = new LongArrayList();
	private final Matrix3f tensor = new Matrix3f();
	private final Matrix3f j = new Matrix3f();
	private final Vector3f bPos = new Vector3f();
	public final Vector3i segPos = new Vector3i();
	/**
	 * physical body that can move (ship etc)
	 */
	private boolean staticElement;


	public void reset(boolean staticElement) {
		pointer = 0;

		this.staticElement = staticElement;
		tensor.setZero();
		totalElements = 0;
		totalPhysicalMass = 0;

		Arrays.fill(counts, 0);
		Arrays.fill(oreCounts, 0);

		textBlocks.clear();

		centerOfMassUnweighted.set(0, 0, 0);


		//no need to reset arrays because we are resetting the pointer
	}

	public void check() {
		if(counts.length != ElementKeyMap.highestType + 1) {
			counts = new int[ElementKeyMap.highestType + 1];
		}

	}

	public void apply(Segment segment, SegmentController segmentController) {
		long time = System.currentTimeMillis();
		if(segmentController instanceof ManagedSegmentController<?>) {
			ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) segmentController).getManagerContainer();
			final int size = pointer;
			for(int i = 0; i < size; i++) {


				managerContainer.onAddedElementSynched(types[i], segment, absIndex[i], time, true);
			}
		}

		segmentController.addFromMeta(totalPhysicalMass, centerOfMassUnweighted, totalElements, counts, oreCounts, textBlocks, tensor);
	}


	public void onAddedElementSynched(final short newType,
	                                  final byte x, final byte y, final byte z, SegmentDataInterface data, int index,
	                                  long absIndex) {

		ElementInformation info = ElementKeyMap.infoArray[newType];
		float massOfBlock = info.getMass();
		totalPhysicalMass += massOfBlock;

		if(!staticElement) {
			bPos.set(
					((segPos.x + x) - SegmentData.SEG_HALF),
					((segPos.y + y) - SegmentData.SEG_HALF),
					((segPos.z + z) - SegmentData.SEG_HALF));

			centerOfMassUnweighted.x += bPos.x * massOfBlock;
			centerOfMassUnweighted.y += bPos.y * massOfBlock;
			centerOfMassUnweighted.z += bPos.z * massOfBlock;


			// compute inertia tensor of pointmass at o
			float o2 = bPos.lengthSquared();
			j.m00 = o2;
			j.m01 = 0;
			j.m02 = 0;
			j.m10 = 0;
			j.m11 = o2;
			j.m12 = 0;
			j.m20 = 0;
			j.m21 = 0;
			j.m22 = o2;

			j.m00 += bPos.x * -bPos.x;
			j.m01 += bPos.y * -bPos.x;
			j.m02 += bPos.z * -bPos.x;
			j.m10 += bPos.x * -bPos.y;
			j.m11 += bPos.y * -bPos.y;
			j.m12 += bPos.z * -bPos.y;
			j.m20 += bPos.x * -bPos.z;
			j.m21 += bPos.y * -bPos.z;
			j.m22 += bPos.z * -bPos.z;

			// add inertia tensor of pointmass
			tensor.m00 += massOfBlock * j.m00;
			tensor.m01 += massOfBlock * j.m01;
			tensor.m02 += massOfBlock * j.m02;
			tensor.m10 += massOfBlock * j.m10;
			tensor.m11 += massOfBlock * j.m11;
			tensor.m12 += massOfBlock * j.m12;
			tensor.m20 += massOfBlock * j.m20;
			tensor.m21 += massOfBlock * j.m21;
			tensor.m22 += massOfBlock * j.m22;
		}

		//INSERTED CODE
		byte orientation = data.getOrientation(index);
		SegmentPieceAddByMetadataEvent event = new SegmentPieceAddByMetadataEvent(newType, x, y, z, orientation, data.getSegment(), absIndex);
		StarLoader.fireEvent(event, data.getSegmentController().isOnServer());
		///
		long indexAndOrientation = ElementCollection.getIndex4(absIndex, data.getOrientation(index));
		if(ElementKeyMap.isTextBox(newType)) textBlocks.add(indexAndOrientation);
		else if(ElementKeyMap.getInfoFast(newType).isArmor()) {
			if(data.getSegmentController() != null && ArmorHPCollection.getCollection(data.getSegmentController()) != null) {
				ArmorHPCollection.getCollection(data.getSegmentController()).addBlock(indexAndOrientation, newType);
			}
		}

		counts[newType]++;
		int newOrientation;
		if(info.resourceInjection == ResourceInjectionType.ORE && (newOrientation = data.getOrientation(index)) > 0 && newOrientation <= 32) {
			int resource = newOrientation - 1;
			oreCounts[resource]++;
		}
		totalElements++;

		this.absIndex[pointer] = absIndex;
		this.types[pointer] = newType;

		pointer++;
	}
}
