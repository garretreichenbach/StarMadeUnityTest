/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>NetworkObject</H2>
 * <H3>org.schema.schine.network.objects</H3>
 * NetworkObject.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright � 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.schema.schine.network.objects;

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.schine.network.DataInputStreamPositional;
import org.schema.schine.network.DataOutputStreamPositional;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.remote.*;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.network.synchronization.SynchronizationReceiver;
import org.w3c.dom.*;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * primitive values will be send to the remote network object please remember to
 * use short variable names since they will be used as identifiers when changes
 * occur and we don'transformationArray have to reapply all unchanged values.
 * This is more important for values, that will change often. e.g. x,y,z will
 * change often, but 'clientId' presumably will never change
 */
public abstract class NetworkObject implements NetworkChangeObserver, NetworkObjectInterface {

	private static final int FIELD_CODE_NOT_CHANGED = 0;
	private static final int FIELD_CODE_CHANGED = 1;
	private static final int FIELD_CODE_CHANGED_KEEP_CHANGED = 2;
	public static boolean CHECKUNSAVE = false;
	public static long objectDebugIdGen = 777777;
	public static boolean global_DEBUG;
	private final boolean onServer;
	private final StateInterface state;
	private final Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Int2ObjectOpenHashMap<Field>> fieldMapCache = new Object2ObjectOpenHashMap<>();
	private final Object2ObjectOpenHashMap<Class<? extends NetworkObject>, Object2IntOpenHashMap<Field>> fieldMapKeyCache = new Object2ObjectOpenHashMap<>();
	public boolean newObject = true;
	/**
	 * The id.
	 */
	public RemoteIntPrimitive id = new RemoteIntPrimitive(-123456, this);
	/**
	 * alive.
	 */
	public RemoteBoolean markedDeleted = new RemoteBoolean(false, this);
	public RemoteByte debugMode = new RemoteByte((byte) 0, this);
	public RemoteByteBuffer graphicsEffectModifier = new RemoteByteBuffer(this);
	public RemoteLongBuffer lagAnnouncement = new RemoteLongBuffer(this);

	public ObjectArrayList<Streamable<?>> lastDecoded = new ObjectArrayList<Streamable<?>>(32);
	private Int2ObjectOpenHashMap<Streamable<?>> fieldMap;
	private Object2IntOpenHashMap<Streamable<?>> fieldMapKey;
	private Int2ObjectOpenHashMap<String> fieldNameMap;
	private ObjectArrayList<RemoteBufferInterface> buffers;
	private boolean changed = true;
	private boolean observersInitialized;
	private int lastEncodedSize;

	public NetworkObject(StateInterface state) {
		//		createFieldMap();
		this.onServer = state instanceof ServerStateInterface;
		//send obejcts by default on server
		changed = onServer;
		this.state = state;
	}

	/**
	 * Decode.
	 *
	 * @param newObj              the new obj
	 * @param privateChannel      TODO
	 * @param updateSenderStateId
	 * @return the network object
	 * @throws IOException
	 * @throws Exception
	 */
	public static NetworkObject decode(StateInterface state,
	                                   DataInputStreamPositional inputStream, NetworkObject newObj,
	                                   short packetId, boolean privateChannel, int updateSenderStateId) throws IOException  {
		assert (state.isSynched());
		if (CHECKUNSAVE && !state.isSynched()) {
			throw new UnsaveNetworkOperationException();
		}
		newObj.lastDecoded.clear();

		int size = inputStream.readByte();
		if (size == 0) {
			System.err.println(state + " WARNING: empty Update received for " + newObj + " from client " + updateSenderStateId);
		}
		if (state.isReadingBigChunk()) {
			System.err.println(state + ": decoding NTObject from big chunk: " + newObj.getClass().getSimpleName() + "; fields received: " + size);
		}
		try {

			for (int i = 0; i < size; i++) {

				long posStart = inputStream.position();

				int index = inputStream.readByte() & 0xFF;


				Streamable<?> f = newObj.fieldMap.get(index);

				newObj.lastDecoded.add(f);



				if (f == null) {
					StringBuilder b = new StringBuilder();
					for(Entry<Streamable<?>> e : newObj.fieldMap.int2ObjectEntrySet()) {
						b.append(e+"\n");
					}
					throw new IOException("not found " + index + ": in "
							+ newObj.getClass().getSimpleName()
							+ ";\n\nsenderStateID: " +( updateSenderStateId == 0 ? "SERVER" : ("CLIENT("+updateSenderStateId+")"))
							+ ";\n\nfields:\n" + b);
				}

				if (state.isReadingBigChunk()) {
					System.err.println(state + ": decoding FIELD from big chunk: " + f);
				}
				if (SynchronizationReceiver.serverDebug && state instanceof ServerStateInterface) {
					System.err.println("DEBUG: changed field: " + f);
				}
				try {
					f.fromByteStream(inputStream, updateSenderStateId);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("[EXCEPTION] NT ERROR: From senderID: " + updateSenderStateId + " for field: " + f + " in " + newObj.getClass().getSimpleName());
					throw e;
				}

				long dataAmount = inputStream.position() - posStart;

				Int2LongOpenHashMap fields = state.getReceivedData().get(newObj.getClass());
				if (fields == null) {
					fields = new Int2LongOpenHashMap();
					state.getReceivedData().put(newObj.getClass(), fields);
				}
				fields.addTo(index, dataAmount);

			}

		} catch (Exception e) {
			// System.err.println("#STREAM#IN  "+packetId+" ERROR occurred! OBJ debug id "+objDebugId+": "+newObj+", buffer remaining: "+buffer.remaining());
			e.printStackTrace();
			System.err.println("Exit because of critical error in nt object");
			throw e;
		}
		//		LogUtil.debug().println("NT OBJECTDEC FINISHED S: "+state.getId()+": "+newObj.getClass().getSimpleName()+" avail: "+inputStream.available());
		//		if(newObj.markedDeleted.get()){
		//			System.err.println("[NTObject] Object "+newObj.id+" on  state "+state+" is marked for delete");
		//		}
		return newObj;
	}

	/**
	 * Encode.
	 *
	 * @param remote                    the no
	 * @param leaveObjectInChangedState
	 * @param privateChannel            TODO
	 * @return the string
	 * @throws IOException
	 * @throws Exception
	 */
	public static boolean encode(Sendable local, NetworkObject remote, boolean newObject, DataOutputStreamPositional outputStream, boolean leaveObjectInChangedState, boolean privateChannel, boolean forceAllNew) throws IOException {
		boolean keepChanged = false;
		assert (remote.id.get() >= 0) : remote.id.get()
				+ " id for remote object never set. local it is "
				+ local.getId() + ", " + local + ", " + local.getState();

		assert (remote.observersInitialized) : local + ", " + remote + ": " + local.getState();

		assert (remote.state.isSynched());
		if (CHECKUNSAVE && !remote.state.isSynched()) {
			throw new UnsaveNetworkOperationException();
		}
		assert(!forceAllNew | newObject);
		boolean localDebug = false;
		int size = 0;

		for (Streamable<?> f : remote.fieldMap.values()) {

			boolean changedField = hasFieldChanged(remote, f, outputStream, newObject);

			if (changedField) {
				size++;
				if (localDebug) {
					System.err.println("ENCODE SIZE: + " + f);
				}
			}
		}

		assert (size <= Byte.MAX_VALUE);

		remote.lastEncodedSize = size;
		if(forceAllNew) {
			System.err.println("SENDING ID FOR FULL UPDATE: "+remote.id.getInt());
		}
		//ID of object
		outputStream.writeInt(remote.id.getInt());

		/*
         * adding class description of local sendable object, so it can be
		 * instantiated on the remote location
		 */
		// append size of local object description string
		//System.out.println(local);
		//System.out.println(local.getSendableType());
		outputStream.writeByte(local.getSendableType().getTypeCode());
		// put size of fields in
		outputStream.writeByte(size);

		int verify = 0;
		for (Streamable<?> streamableField : remote.fieldMap.values()) {

			int fieldCode = serializeField(remote, streamableField, outputStream, newObject, leaveObjectInChangedState, forceAllNew);

			if (fieldCode > 0) {
				if (localDebug) {
					System.err.println("ENCODE VERIFY: + " + streamableField);
				}
				if (fieldCode == FIELD_CODE_CHANGED_KEEP_CHANGED) {
					keepChanged = true;
				}
//						debug2.add(f);
				verify++;
			}

		}
		if (verify != size) {
			for (Streamable<?> f : remote.fieldMap.values()) {
				System.err.println("!!!!!!EXCPETION INCLUDING: " + f);
			}
		}
		assert (verify == size) : " ENCODING OF " + local + " failed; newObject "+newObject+" forced " + forceAllNew + "; " + size + "/" + verify;
		global_DEBUG = false;
		return keepChanged;
	}

	private static int serializeField(
			NetworkObject obj, Streamable<?> f, DataOutputStreamPositional outputStream,
			boolean force, boolean leaveObjectInChangedState, boolean forceAllNew) throws IOException {

		if (force && f instanceof RemoteBufferInterface && ((RemoteBufferInterface) f).isEmpty()) {
			f.setChanged(false);
			//empty buffers don't need to be sent
			return FIELD_CODE_NOT_CHANGED;
		}

		if (force || f.hasChanged()) {

			long posStart = outputStream.position();

			int fieldId = obj.fieldMapKey.getInt(f);
			outputStream.writeByte((byte)fieldId);
			f.toByteStream(outputStream);

			int retCode = FIELD_CODE_CHANGED;

			if ((!leaveObjectInChangedState && !f.keepChanged()) || f.initialSynchUpdateOnly()) {
				f.setChanged(false);
			} else {
				retCode = FIELD_CODE_CHANGED_KEEP_CHANGED;
			}

			long dataAmount = outputStream.position() - posStart;
			Int2LongOpenHashMap fields = obj.state.getSentData().get(obj.getClass());
			if (fields == null) {
				fields = new Int2LongOpenHashMap();
				obj.state.getSentData().put(obj.getClass(), fields);
			}
			fields.addTo(fieldId, dataAmount);

			return retCode;

		}

		return FIELD_CODE_NOT_CHANGED;
	}

	private static boolean hasFieldChanged(
			NetworkObject obj, Streamable<?> f, OutputStream outputStream,
			boolean force) {

		if (force && f instanceof RemoteBufferInterface) {
			return !((RemoteBufferInterface) f).isEmpty();
		}
		return (force || f.hasChanged());
	}

	public static String[] getFieldNames(Class<? extends NetworkObject> ntObjClass) {

		HashMap<Byte, Field> fieldFMap;
		HashMap<Field, Byte> fieldFMapKey;
		fieldFMap = new HashMap<Byte, Field>();
		fieldFMapKey = new HashMap<Field, Byte>();

		Field[] fieldsOfObject = ntObjClass.getFields();
		String[] names = new String[fieldsOfObject.length];
		for (int i = 0; i < fieldsOfObject.length; i++) {
			names[i] = fieldsOfObject[i].getName();
		}
		assert (names.length < 127);
		Arrays.sort(names);
		for (byte i = 0; i < names.length; i++) {
			try {
				Field field = ntObjClass.getField(names[i]);
				fieldFMap.put(i, field);
				fieldFMapKey.put(field, i);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}

		return names;
	}

	public void addObserversForFields() {
		try {
			Object[] fieldsOfObject = this.getClass().getFields();
			for (int i = 0; i < fieldsOfObject.length; i++) {
				Object o = fieldsOfObject[i];
				Field f = (Field) o;
				if (f.getModifiers() != Member.DECLARED) {
					continue;
				}

				if (Streamable.class.isAssignableFrom(f.getType())) {
					Streamable<?> remoteField;

					remoteField = (Streamable<?>) (f.getType().cast(f
							.get(this)));

					remoteField.setObserver(this);
				}

			}
			observersInitialized = true;
		} catch (IllegalArgumentException e) {

			e.printStackTrace();
		} catch (IllegalAccessException e) {

			e.printStackTrace();
		}

	}

	/**
	 * Append to doc.
	 *
	 * @param root the root
	 * @param doc  the doc
	 * @throws ParserConfigurationException the parser configuration exception
	 */
	public void appendToDoc(Node root, Document doc)
			throws ParserConfigurationException {

		Element entElem = doc.createElement("entity");

		Attr tAttr = doc.createAttribute("class");
		tAttr.setNodeValue(this.getClass().getSimpleName());
		entElem.setAttributeNode(tAttr);

		Object[] obj = this.getClass().getFields();
		try {
			for (int i = 0; i < obj.length; i++) {

				Object o = obj[i];
				Field f = (Field) o;
				String content = null;
				String type = null;
				if (f.getGenericType().equals(String.class)) {
					type = "string";
					System.err.println("fieldname " + f.getName());
					content = f.get(this).toString();
				} else if (f.getGenericType().equals(Float.TYPE)) {
					type = "float";
					content = f.get(this).toString();
				} else if (f.getGenericType().equals(Integer.TYPE)) {
					type = "int";
					content = f.get(this).toString();
				} else if (f.getGenericType().equals(Boolean.TYPE)) {
					type = "boolean";
					content = f.get(this).toString();
				} else if (f.getGenericType().equals(float[].class)) {
					float[] fArray = (float[]) f.get(this);
					type = "floatArray";
					content = "";
					for (int j = 0; j < fArray.length; j++) {
						content += fArray[j];
						if (j < fArray.length - 1) {
							content += ",";
						}
					}
				} else if (f.getGenericType().equals(String[].class)) {
					String[] fArray = (String[]) f.get(this);
					type = "stringArray";
					content = "";
					for (int j = 0; j < fArray.length; j++) {
						content += fArray[j];
						if (j < fArray.length - 1) {
							content += ",";
						}
					}
				} else if (f.getGenericType().equals(int[].class)) {
					int[] fArray = (int[]) f.get(this);
					type = "intArray";
					content = "";
					for (int j = 0; j < fArray.length; j++) {

						content += fArray[j];
						if (j < fArray.length - 1) {
							content += ",";
						}
					}
				} else {
					break;
				}

				Element elem = doc.createElement(f.getName());
				Attr attr = doc.createAttribute("type");
				attr.setNodeValue(type);
				elem.setAttributeNode(attr);
				elem.setTextContent(content);

				entElem.appendChild(elem);
			}
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		System.err.println("appending entity "
				+ this.getClass().getSimpleName() + ": " + this);
		root.appendChild(entElem);

	}

	//	private void clearReceiveBuffer(Class<Streamable> type, Field f) throws IllegalArgumentException, IllegalAccessException {
	//		Streamable remoteField = (type.cast(f.get(this)));
	//		if(remoteField instanceof RemoteBuffer<?>){
	//			RemoteBuffer<?> b = (RemoteBuffer<?>)remoteField;
	//			b.clearReceiveBuffer();
	//		}
	//	}
	public void clearReceiveBuffers() {
		assert (state.isSynched());
		if (CHECKUNSAVE && !state.isSynched()) {
			throw new UnsaveNetworkOperationException();
		}
		for (RemoteBufferInterface f : buffers) {
			f.clearReceiveBuffer();
		}
	}

	private synchronized void createFieldMap() {
		Int2ObjectOpenHashMap<Field> fieldFMap;
		Object2IntOpenHashMap<Field> fieldFMapKey;
		if (fieldMapCache.containsKey(this.getClass())) {

			fieldFMap = fieldMapCache.get(this.getClass());
			fieldFMapKey = fieldMapKeyCache.get(this.getClass());
		} else {
			fieldFMap = new Int2ObjectOpenHashMap<Field>();
			fieldFMapKey = new Object2IntOpenHashMap<Field>();

			Field[] fieldsOfObjectRaw = getClass().getFields();
			List<Field> streamableFields = new ObjectArrayList<>();
			for(Field f : fieldsOfObjectRaw)
			if (Streamable.class.isAssignableFrom(f.getType())) {
				streamableFields.add(f);
			}

			String[] names = new String[streamableFields.size()];
			for (int i = 0; i < streamableFields.size(); i++) {
				names[i] = streamableFields.get(i).getName();
			}
			assert (names.length < 254):getClass().getSimpleName();
			Arrays.sort(names);
			for (int i = 0; i < names.length; i++) {
				try {
					Field field = getClass().getField(names[i]);
					fieldFMap.put(i, field);
					fieldFMapKey.put(field, i);
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
			fieldMapCache.put(this.getClass(), fieldFMap);
			fieldMapKeyCache.put(this.getClass(), fieldFMapKey);
		}
		this.buffers = new ObjectArrayList<RemoteBufferInterface>();
		this.fieldMap = new Int2ObjectOpenHashMap<Streamable<?>>();
		this.fieldNameMap = new Int2ObjectOpenHashMap<String>();
		this.fieldMapKey = new Object2IntOpenHashMap<Streamable<?>>();

		for (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<Field> f : fieldFMap.int2ObjectEntrySet()) {
			if (Streamable.class.isAssignableFrom(f.getValue().getType())) {
				try {
					Streamable<?> obj = (Streamable<?>) f.getValue().get(this);
					assert (obj != null) : this.getClass().getSimpleName() + " -> " + f.getValue().getName() + ": " + f.getValue().getType().getSimpleName();
					this.fieldMap.put(f.getIntKey(), obj);
					this.fieldNameMap.put(f.getIntKey(), f.getValue().getName());
					this.fieldMapKey.put(obj, f.getIntKey());

					if (obj instanceof RemoteBufferInterface) {
						buffers.add((RemoteBufferInterface) obj);
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					throw new RuntimeException(e.getClass() + ": " + e.getMessage());
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					throw new RuntimeException(e.getClass() + ": " + e.getMessage());
				}
			}
		}
		fieldMap.trim();
		fieldNameMap.trim();
		fieldMapKey.trim();

	}

	/**
	 * Decode change.
	 *
	 * @param inputStream
	 * @param privateChannel
	 * @param updateSenderStateId
	 * @throws IOException
	 * @throws Exception
	 */
	public void decodeChange(StateInterface state,
	                         DataInputStreamPositional inputStream, short packetId, boolean privateChannel, int updateSenderStateId) throws IOException
	{
			try {
				decode(state, inputStream, this, packetId, privateChannel, updateSenderStateId);
			} catch (IOException e) {
				System.err.println("NT Exception happened when decoding change of " + this + " on " + state);
				throw e;
			}

	}

	/**
	 * Encode change.
	 *
	 * @param sendable       the sendable
	 * @param privateChannel TODO
	 * @return the string
	 * @throws IOException
	 * @throws Exception
	 */
	public boolean encodeChange(Sendable sendable,
	                            DataOutputStreamPositional outputStream, boolean privateChannel) throws IOException {
		return encode(sendable, sendable.getNetworkObject(), false, outputStream, false, privateChannel, false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		NetworkObject o = (NetworkObject) other;
		return this.id.get() == o.id.get();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "NetworkObject(" + getClass().getSimpleName() + "; " + id.get() + ")";
	}

	public String getChangedFieldsString() {
		StringBuffer sb = new StringBuffer();
		//		try {
		//
		//
		//			for (Streamable<?> f : fieldMap.values()) {
		//
		//
		//				if (Streamable.class.isAssignableFrom(f.getType())) {
		//					Streamable remoteField;
		//
		//					remoteField = (Streamable) (f.getType().cast(f
		//							.get(this)));
		//					if(remoteField.hasChanged()){
		//						sb.append(f.getName()+"; ");
		//					}
		//
		//				}
		//
		//
		//		} catch (IllegalArgumentException e) {
		//
		//			e.printStackTrace();
		//		} catch (IllegalAccessException e) {
		//
		//			e.printStackTrace();
		//		}
		return sb.toString();
	}

	/**
	 * @return the state
	 */
	public StateInterface getState() {
		return state;
	}

	public void init() {
		createFieldMap();
	}

	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * @param changed the changed to set
	 */
	public void setChanged(boolean changed) {

		this.changed = changed;
	}

	/**
	 * @return the onServer
	 */
	@Override
	public boolean isOnServer() {
		return onServer;
	}

	/**
	 * On delete.
	 *
	 * @param stateI the state i
	 */
	public abstract void onDelete(StateInterface stateI);

	/**
	 * On init.
	 *
	 * @param stateI the state i
	 * @
	 */
	public abstract void onInit(StateInterface stateI)
	;

	public void setAllFieldsChanged() {
		assert (state.isSynched());
		if (CHECKUNSAVE && !state.isSynched()) {
			throw new UnsaveNetworkOperationException();
		}
		for (Streamable<?> f : this.fieldMap.values()) {
			assert (f != null);
			f.setChanged(true);
		}
		this.changed = true;

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.remote.NetworkChangeObserver#update(org.schema.schine.network.objects.remote.RemoteField)
	 */
	@Override
	public void update(Streamable<?> o) {

		if (CHECKUNSAVE && !state.isSynched()) {
			assert (state.isSynched());
			throw new UnsaveNetworkOperationException();
		}
		changed = true;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.remote.NetworkChangeObserver#isSynched()
	 */
	@Override
	public boolean isSynched() {
		return state.isSynched();
	}

	/**
	 * @return the lastEncodedSize
	 */
	public int getLastEncodedSize() {
		return lastEncodedSize;
	}
	public RemoteLongBuffer getLagAnnouncement() {
		return lagAnnouncement;
	}
}
