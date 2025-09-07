package org.schema.game.common.data.element.quarters;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.quarters.crew.CrewMember;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.TagSerializable;

public class QuarterManager implements TagSerializable {

	private static final byte VERSION = 0;
	private final ObjectArrayList<CrewMember> currentCrew = new ObjectArrayList<>();
	private final Long2ObjectOpenHashMap<Quarter> quartersById = new Long2ObjectOpenHashMap<>();
	private final ObjectArrayList<Quarter> quartersChanged = new ObjectArrayList<>();
	private final SegmentController segmentController;

	public QuarterManager(SegmentController segmentController) {
		this.segmentController = segmentController;
	}

	public void update(Timer timer) {
		updateQuarters(timer);
		handleChangedQuarters();
		handleCrew(timer);
	}

	private void updateQuarters(Timer timer) {
		for(Quarter next : quartersById.values()) {
			for(CrewMember member : next.getCrew()) {
				if(!currentCrew.contains(member)) currentCrew.add(member);
			}
			if(next.isChanged()) {
				next.setChanged(false);
				quartersChanged.add(next);
			}
			next.update(timer);
		}
	}

	private void handleCrew(Timer timer) {
		for(CrewMember crewMember : currentCrew) crewMember.update(timer);
	}

	private void handleChangedQuarters() {
		for(int i = 0; i < quartersChanged.size(); i++) {
			Quarter quarter = quartersChanged.get(i);
			if(!quarter.isReinitialing()) quarter.reinitialize();
			if(quarter.reinitializingDone()) {
				quartersById.put(quarter.getIndex(), quarter);
				quartersChanged.remove(i);
				i--;
			}
		}
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] v = (Tag[]) tag.getValue();
		byte version = (Byte) v[0].getValue();
		Tag[] qt = (Tag[]) v[1].getValue();
		for(int i = 0; i < qt.length - 1; i++) {
			Quarter loadFromTag = Quarter.loadFromTag(segmentController, qt[i]);
			quartersById.put(loadFromTag.getIndex(), loadFromTag);
		}
	}

	@Override
	public Tag toTagStructure() {
		Tag versionTag = new Tag(Tag.Type.BYTE, null, VERSION);
		Tag[] qTags = new Tag[quartersById.size() + 1];
		qTags[qTags.length - 1] = FinishTag.INST;
		int i = 0;
		for(Quarter q : quartersById.values()) {
			qTags[i] = q.toTagStructure();
			i++;
		}
		Tag quarterTag = new Tag(Tag.Type.STRUCT, null, qTags);
		return new Tag(Tag.Type.STRUCT, null, new Tag[]{versionTag, quarterTag, FinishTag.INST});
	}

	public ObjectArrayList<CrewMember> getCurrentCrew() {
		return currentCrew;
	}

	public Long2ObjectOpenHashMap<Quarter> getQuartersById() {
		return quartersById;
	}

	public void addCrewStation(SegmentPiece segmentPiece, ElementInformation info) {
		assert Quarter.QuarterType.getFromBlock(info.getId()) != null;
		Quarter quarter = Quarter.QuarterType.getFromBlock(info.getId()).getQuarter(segmentController);
		int id = quartersById.size();
		quarter.setIndex(segmentPiece.getAbsoluteIndex());
		Vector3i pos = new Vector3i();
		segmentPiece.getAbsolutePos(pos);
		quarter.getArea().min.set(pos);
		quarter.getArea().max.set(pos);
		quartersById.put(id, quarter);
	}

	public Quarter getCrewStation(SegmentPiece segmentPiece) {
		for(Quarter quarter : quartersById.values()) {
			if(segmentPiece.getAbsoluteIndex() == quarter.getIndex()) return quarter;
		}
		return null;
	}
}