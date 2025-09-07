package org.schema.game.common.controller;

import java.util.ArrayList;
import java.util.Collection;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.Segment;

public class SegmentProviderQueue<E> extends ArrayList<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	
	private SegmentController con;

	private Vector3i tmpPos = new Vector3i();

	public SegmentProviderQueue(Collection<? extends E> c, SegmentController con) {
		super(c);
		this.con = con;
		// TODO Auto-generated constructor stub
	}

	public SegmentProviderQueue(int initialCapacity, SegmentController con) {
		super(initialCapacity);
		this.con = con;
		// TODO Auto-generated constructor stub
	}

	public SegmentProviderQueue(SegmentController con) {
		super();
		this.con = con;
		// TODO Auto-generated constructor stub
	}

	//
	//	/* (non-Javadoc)
	//	 * @see java.util.ArrayList#add(java.lang.Object)
	//	 */
	public boolean add(Vector3i el) {
		assert (isInBound(el));
				return super.add((E) el);
	}

	protected boolean isInBound(Vector3i pos) {
		return con.isInbound(
				Segment.getSegmentIndexFromSegmentElement(pos.x, pos.y, pos.z, tmpPos));

	}
	//
	//	/* (non-Javadoc)
	//	 * @see java.util.ArrayList#add(int, java.lang.Object)
	//	 */
	//	@Override
	//	public void add(int index, E element) {
	//			//		super.add(index, element);
	//		if(con.getUniqueIdentifier().contains("1360199645874_1365559158847_0")){
	//			try{
	//			throw new NullPointerException(con.getUniqueIdentifier()+": "+element);
	//			}catch (Exception e) {
	//				e.printStackTrace();
	//			}
	//		}
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see java.util.ArrayList#addAll(java.util.Collection)
	//	 */
	//	@Override
	//	public boolean addAll(Collection<? extends E> c) {
	//		if(con.getUniqueIdentifier().contains("1360199645874_1365559158847_0")){
	//			try{
	//			throw new NullPointerException(con.getUniqueIdentifier()+": "+c);
	//			}catch (Exception e) {
	//				e.printStackTrace();
	//			}
	//		}
	//			//		return super.addAll(c);
	//
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see java.util.ArrayList#addAll(int, java.util.Collection)
	//	 */
	//	@Override
	//	public boolean addAll(int index, Collection<? extends E> c) {
	//		if(con.getUniqueIdentifier().contains("1360199645874_1365559158847_0")){
	//			try{
	//			throw new NullPointerException(con.getUniqueIdentifier()+": "+c);
	//			}catch (Exception e) {
	//				e.printStackTrace();
	//			}
	//		}
	//			//		return super.addAll(index, c);
	//	}

}
