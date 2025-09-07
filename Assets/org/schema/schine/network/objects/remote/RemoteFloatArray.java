package org.schema.schine.network.objects.remote;

import java.util.Arrays;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteFloatArray extends RemoteArray<Float> {

	private float[] transientArray;

	public RemoteFloatArray(int size, boolean synchOn) {
		super(new RemoteFloat[size], synchOn);
	}

	public RemoteFloatArray(int size, NetworkObject synchOn) {
		super(new RemoteFloat[size], synchOn);
	}

	@Override
	public int byteLength() {
		return get().length * ByteUtil.SIZEOF_FLOAT;
	}

	/**
	 * @return the transientArray
	 */
	public float[] getTransientArray() {
		return transientArray;
	}

	@Override
	protected void init(RemoteField<Float>[] e) {
		this.set(e);
	}

	//	@Override
	//	public int toByteStream( DataOutputStream buffer) throws IOException {
	//		for(int i = 0; i < get().length; i++){
	//			buffer.write(ByteUtil.floatToByteArray(transientArray[i], floatBuffer));
	//		}
	//		return length();
	//	}

	@Override
	public void set(int i, Float value) {
		transientArray[i] = value;
		super.get()[i].set(value, forcedClientSending);
	}

	@Override
	public void set(RemoteField<Float>[] value) {
		super.set(value);
		for (int i = 0; i < value.length; i++) {
			get()[i] = new RemoteFloat(0f, onServer);
		}

		transientArray = new float[value.length];
		addObservers();
	}

	@Override
	public String toString() {
		return "(rfA" + Arrays.toString(transientArray) + ")";
	}

	public void setArray(float[] value) {
		if (value == null) {
			throw new NullPointerException("cannot set array Null");
		}
		if (value.length != get().length) {
			throw new IllegalArgumentException("Cannot change array size of remote array");
		}

		for (int i = 0; i < transientArray.length; i++) {
			transientArray[i] = value[i];
			get(i).set(value[i], forcedClientSending);
		}
	}

}
