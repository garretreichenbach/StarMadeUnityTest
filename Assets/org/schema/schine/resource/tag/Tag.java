package org.schema.schine.resource.tag;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;

public class Tag {

	private static TagTools tools;
	private static ObjectArrayList<Tag[]> tagArrayPool = new ObjectArrayList<Tag[]>(128);
	private long size;
	private Type type;
	private Type listType = null;
	private String name;
	private Object value;
	private short version;
	public static final String NULL_STRING = "null";

	public Tag(String name, Type listType) {
		this(Type.LIST, name, listType);
	}
	public Tag(String name, boolean booleanType) {
		this(Type.BYTE, name, booleanType ? (byte)1 : (byte)0);
	}

	public Tag(Type type, String name, Object value) {
		
		switch (type) {
			case FINISH:
				if (value != null)
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				break;
			case BYTE:
				if (!(value instanceof Byte))
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				break;
			case SHORT:
				if (!(value instanceof Short))
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				break;
			case INT:
				if (!(value instanceof Integer))
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				break;
			case LONG:
				if (!(value instanceof Long))
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				break;
			case FLOAT:
				if (!(value instanceof Float))
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				break;
			case DOUBLE:
				if (!(value instanceof Double))
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				break;
			case BYTE_ARRAY:
				if (!(value instanceof byte[]))
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				break;
			case STRING:
				if (!(value instanceof String))
					throw new IllegalArgumentException(value + "; " + (value != null ? value.getClass().toString() : ""));
				break;
			case VECTOR3f:
				if (!(value instanceof Vector3f))
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				break;
			case VECTOR3i:
				if (!(value instanceof Vector3i))
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				break;
			case VECTOR3b:
				if (!(value instanceof Vector3b))
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				break;
			case LIST:
				if (value instanceof Type) {
					this.listType = (Type) value;
					value = new Tag[0];
				} else {
					if (!(value instanceof Tag[]))
						throw new IllegalArgumentException();
					this.listType = (((Tag[]) value)[0]).type;
				}
				break;
			case STRUCT:
				if (!(value instanceof Tag[]))
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				assert (notNull((Tag[]) value)) : value;
				break;
			case SERIALIZABLE:
				if (!(value instanceof SerializableTagElement))
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				break;
			case VECTOR4f:
				if (!(value instanceof Vector4f))
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				break;
			case MATRIX4f:
				if (!(value instanceof Matrix4f))
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				break;
			case MATRIX3f:
				if (!(value instanceof Matrix3f))
					throw new IllegalArgumentException(value.getClass().getSimpleName());
				break;
			case NOTHING:
				break;
			default:
				throw new IllegalArgumentException();
		}
		this.type = type;
		this.name = name;
		this.value = value;
	}

	public Tag(Type type, String name, Tag[] value) {
		this(type, name, (Object) value);
		assert (type == Type.FINISH || type == Type.NOTHING || value[value.length - 1].type == Type.FINISH);

	}

	private static String getTypeString(Type type) {
		return switch(type) {
			case FINISH -> "TAG_End";
			case BYTE -> "TAG_Byte";
			case SHORT -> "TAG_Short";
			case INT -> "TAG_Int";
			case LONG -> "TAG_Long";
			case FLOAT -> "TAG_Float";
			case DOUBLE -> "TAG_Double";
			case BYTE_ARRAY -> "TAG_Byte_Array";
			case STRING -> "TAG_String";
			case NOTHING -> "TAG_NOTHING";
			case VECTOR3f -> "TAG_Vector3f";
			case VECTOR3i -> "TAG_Vector3i";
			case VECTOR3b -> "TAG_Vector3b";
			case LIST -> "TAG_List";
			case STRUCT -> "TAG_Compound";
			case SERIALIZABLE -> "TAG_Serializable";
			case VECTOR4f -> "TAG_Vector4f";
			case MATRIX4f -> "TAG_Matrix4f";
			case MATRIX3f -> "TAG_Matrix3f";
		};
	}

	public static short[] shortArrayFromTagStruct(Tag struct) {
		Tag[] v = (Tag[]) struct.value;
		assert (v[v.length - 1].type == Type.FINISH);

		short[] r = new short[v.length - 1];
		for (int i = 0; i < v.length - 1; i++) {
			r[i] = ((Short) v[i].value);
		}
		return r;
	}

	public static <E extends Object> void listFromTagStruct(Collection<E> values, Tag t) {
		Tag[] v = (Tag[]) t.value;
		listFromTagStruct(values, v);
	}

	public static <E extends Object> void listFromTagStruct(Collection<E> values, Tag t, ListSpawnObjectCallback<E> cb) {
		Tag[] v = (Tag[]) t.value;
		listFromTagStruct(values, v, cb);
	}

	public static <E extends Object> void listFromTagStructSP(Collection<E> values, Tag t, ListSpawnObjectCallback<E> cb) {
		Tag[] v = (Tag[]) t.value;
		listFromTagStructSP(values, v, cb);
	}

	public static <E extends Object> void listFromTagStructSP(Collection<E> values, Tag[] v, ListSpawnObjectCallback<E> cb) {
		assert (v[v.length - 1].type == Type.FINISH);
		for (int i = 0; i < v.length - 1; i++) {
			values.add(cb.get(v[i]));
		}
	}

	public static <E extends Object> void listFromTagStructSPElimDouble(Collection<E> values, Tag t, ListSpawnObjectCallback<E> cb) {
		Tag[] v = (Tag[]) t.value;
		listFromTagStructSP(values, v, cb);
	}

	public static <E extends Object> void listFromTagStructSPElimDouble(Collection<E> values, Tag[] v, ListSpawnObjectCallback<E> cb) {
		assert (v[v.length - 1].type == Type.FINISH);
		for (int i = 0; i < v.length - 1; i++) {
			E e = cb.get(v[i]);
			if (!values.contains(e)) {
				values.add(e);
			}
		}
	}

	public static <E extends Object> void listFromTagStruct(Collection<E> values, Tag[] v, ListSpawnObjectCallback<E> cb) {
		assert (v[v.length - 1].type == Type.FINISH);
		for (int i = 0; i < v.length - 1; i++) {
			values.add(cb.get(v[i].value));
		}
	}

	@SuppressWarnings("unchecked")
	public static <E extends Object> void listFromTagStruct(Collection<E> values, Tag[] v) {
		assert (v[v.length - 1].type == Type.FINISH);
		for (int i = 0; i < v.length - 1; i++) {
			values.add((E) v[i].value);
		}
	}

	public static <E extends TagSerializable> void listFromTagStruct(Constructor<? extends E> constr, Collection<E> values, Tag[] v) {
		assert (v[v.length - 1].type == Type.FINISH);
		for (int i = 0; i < v.length - 1; i++) {
			try {
				E newInstance = constr.newInstance();
				newInstance.fromTagStructure(v[i]);
				values.add(newInstance);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

		}
	}

	public static Tag listToTagStruct(Collection<? extends Object> values, Type t, String name) {
		Tag[] members = new Tag[values.size() + 1];

		members[values.size()] = FinishTag.INST;

		int i = 0;
		for (Object o : values) {
			members[i] = (new Tag(t, null, o));
			i++;
		}

		Tag root = new Tag(Type.STRUCT, name, members);
		return root;
	}

	public static Tag listToTagStruct(short[] values, String name) {
		Tag[] members = new Tag[values.length + 1];

		members[values.length] = FinishTag.INST;

		int i = 0;
		for (Object o : values) {
			members[i] = (new Tag(Type.SHORT, null, o));
			i++;
		}

		Tag root = new Tag(Type.STRUCT, name, members);
		return root;
	}
	public static void fromString2IntMapToTagStruct(Tag tag, Object2IntMap<String> map){
		Tag[] t = tag.getStruct();
		for(int i = 0; i < t.length-1; i++){
			Tag[] sub = t[i].getStruct();
			
			map.put(sub[0].getString(), sub[1].getInt());
		}
	}
	public static Tag string2IntMapToTagStruct(Object2IntMap<String> map){
		Tag[] t = new Tag[map.size()+1];
		t[t.length -1 ] = FinishTag.INST;
		
		int i = 0;
		for(Entry<String, Integer> e : map.entrySet()){
			t[i++] = new Tag(Type.STRUCT, null, 
					new Tag[]{new Tag(Type.STRING, null, e.getKey()), 
					new Tag(Type.INT, null, e.getValue()), 
					FinishTag.INST});
		}
		
		return new Tag(Type.STRUCT, null, t);
	}
	public static void fromShort2StringMapToTagStruct(Tag tag, Short2ObjectMap<String> map){
		Tag[] t = tag.getStruct();
		for(int i = 0; i < t.length-1; i++){
			Tag[] sub = t[i].getStruct();
			
			map.put(sub[0].getShort(), sub[1].getString());
		}
	}
	public static Tag short2StringMapToTagStruct(Short2ObjectMap<String> map){
		Tag[] t = new Tag[map.size()+1];
		t[t.length -1 ] = FinishTag.INST;
		
		int i = 0;
		for(Entry<Short, String> e : map.entrySet()){
			t[i++] = new Tag(Type.STRUCT, null, 
					new Tag[]{new Tag(Type.SHORT, null, e.getKey()), 
					new Tag(Type.STRING, null, e.getValue()), 
					FinishTag.INST});
		}
		
		return new Tag(Type.STRUCT, null, t);
	}
	public static <E> Tag listToTagStruct(E[] values, Type t, String name) {
		Tag[] members = new Tag[values.length + 1];

		members[values.length] = FinishTag.INST;

		int i = 0;
		for (Object o : values) {
			members[i] = (new Tag(t, null, o));
			i++;
		}

		Tag root = new Tag(Type.STRUCT, name, members);
		return root;
	}

	public static Tag listToTagStruct(Collection<? extends TagSerializable> tagSers, String name) {
		Tag[] members = new Tag[tagSers.size() + 1];

		members[tagSers.size()] = FinishTag.INST;
		int i = 0;
		for (TagSerializable t : tagSers) {
			members[i] = t.toTagStructure();
			i++;
		}

		Tag root = new Tag(Type.STRUCT, name, members);
		return root;
	}

	public static Tag listToTagStruct(Long2ObjectOpenHashMap<String> m, String name) {
		Tag[] members = new Tag[m.size() + 1];

		members[m.size()] = FinishTag.INST;

		int i = 0;
		for (Entry<Long, String> a : m.entrySet()) {
			Tag[] t = new Tag[3];
			t[0] = new Tag(Type.LONG, null, a.getKey());
			t[1] = new Tag(Type.STRING, null, a.getValue());
			t[2] = FinishTag.INST;

			members[i] = (new Tag(Type.STRUCT, null, t));
			i++;
		}

		Tag root = new Tag(Type.STRUCT, name, members);
		return root;
	}

	public static Tag listToTagStruct(Long2LongOpenHashMap m, String name) {
		Tag[] members = new Tag[m.size() + 1];

		members[m.size()] = FinishTag.INST;

		int i = 0;
		for (Entry<Long, Long> a : m.entrySet()) {
			Tag[] t = new Tag[3];
			t[0] = new Tag(Type.LONG, null, a.getKey());
			t[1] = new Tag(Type.LONG, null, a.getValue());
			t[2] = FinishTag.INST;

			members[i] = (new Tag(Type.STRUCT, null, t));
			i++;
		}

		Tag root = new Tag(Type.STRUCT, name, members);
		return root;
	}

	public static Tag listToTagStruct(List<? extends Object> list, Type t, String name) {
		Tag[] members = new Tag[list.size() + 1];

		members[list.size()] = FinishTag.INST;

		for (int i = 0; i < list.size(); i++) {
			members[i] = (new Tag(t, null, list.get(i)));
		}

		Tag root = new Tag(Type.STRUCT, name, members);
		return root;
	}

	public static <E> Tag listToTagStructUsing(List<E> list, Type t, String name, ListObjectCallback<E> cb) {
		Tag[] members = new Tag[list.size() + 1];

		members[list.size()] = FinishTag.INST;

		for (int i = 0; i < list.size(); i++) {
			members[i] = (new Tag(t, null, cb.get(list.get(i))));
		}

		Tag root = new Tag(Type.STRUCT, name, members);
		return root;
	}

	public static Tag listToTagStruct(List<? extends TagSerializable> tagSers, String name) {
		Tag[] members = new Tag[tagSers.size() + 1];

		members[tagSers.size()] = FinishTag.INST;

		for (int i = 0; i < tagSers.size(); i++) {
			members[i] = tagSers.get(i).toTagStructure();
		}

		Tag root = new Tag(Type.STRUCT, name, members);
		return root;
	}
//	private static boolean isInvalidString(String name){
//		if(name == null){
//			return false;
//		}
//		return NULL_STRING.equals(name) || 
//		"PointDist".equals(name) ||
//		"controller".equals(name) ||
//		"dist".equals(name) ||
//		"idPos".equals(name) ||
//		"EffectStruct".equals(name);
//	}
	public static Tag readFrom(InputStream is, boolean closeStream, boolean recordSize) throws IOException {
		InputStream in;
		DataInput dis;
		ProfiledDataInputStream prof = null;
		PushbackInputStream pb = new PushbackInputStream(is, 2); //we need a pushbackstream to look ahead
		byte[] signature = new byte[2];
//		System.err.println("SIGNATURE: "+Arrays.toString(signature));
		pb.read(signature); //read the signature
		pb.unread(signature); //push back the signature to the stream
		if ((signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b)) { //check if matches standard gzip magic number
			in = new DataInputStream(new GZIPInputStream(pb, 4096));
			dis = (DataInput) in;
		} else {
			if (recordSize) {
				System.err.println("RECORDING SIZE!!!");
				in = new ProfiledDataInputStream(new DataInputStream(new BufferedInputStream(pb, 65536)));
				prof = (ProfiledDataInputStream) in;
				dis = (DataInput) in;
			} else {
				in = new DataInputStream(new BufferedInputStream(pb, 65536));
				dis = (DataInput) in;
			}
			short version = dis.readShort();
		}


		byte prType = dis.readByte();
		byte type = (byte) Math.abs(prType);
		boolean hasName = prType > 0;
		Tag tag = null;

		if (type == 0) {
			tag = FinishTag.INST;
		} else {
			String name = null;
			if (hasName) {
				name = dis.readUTF();
//				if(isInvalidString(name)){
//					name = null;
//				}
				
			}
			//        	System.err.println("TAG reading: "+name);
			try {
				long t = System.currentTimeMillis();

				long bytePos = 0;
				if (prof != null) {
					bytePos = prof.getReadSize();
				}
				tag = new Tag(Type.values()[type], name, readPayload(dis, type, prof));
				if (prof != null) {
					tag.size = prof.getReadSize() - bytePos;
				}
				long took = System.currentTimeMillis() - t;
				if (took > 100) {
					System.err.println("[TAG] warning: reading tag: " + name + " took: " + took);
				}
			} catch (IOException e) {
				System.err.println("EXCEPTION WHILE READING TAG " + name);
				throw e;
			}
		}
		if (closeStream) {
			in.close();
		}

		return tag;
	}

	public static Tag readFromZipped(InputStream is, boolean closeStream) throws IOException {
		DataInputStream disI = new DataInputStream(new GZIPInputStream(is));
		long inflatedSize = disI.readLong();
		int deflatedSize = disI.readInt();

		if (tools == null) {
			tools = new TagTools();
		}
		synchronized (tools.inflateBuffer) {
			disI.readFully(tools.inflateBuffer, 0, deflatedSize);

			tools.inflater.setInput(tools.inflateBuffer, 0, deflatedSize);
			try {
				tools.inflater.inflate(tools.inputBuffer, 0, (int) inflatedSize);
			} catch (DataFormatException e1) {
				e1.printStackTrace();
			}

			DataInputStream dis = new DataInputStream(tools.input);

			byte prType = dis.readByte();
			byte type = (byte) Math.abs(prType);
			boolean hasName = prType > 0;
			Tag tag = null;

			if (type == 0) {
				tag = FinishTag.INST;
			} else {
				String name = null;
				if (hasName) {
					name = dis.readUTF();
				}
				//        	System.err.println("TAG reading: "+name);
				try {
					tag = new Tag(Type.values()[type], name, readPayload(dis, type, null));
				} catch (IOException e) {
					System.err.println("EXCEPTION WHILE READING TAG " + name);
					throw e;
				}
			}
			if (closeStream) {
				dis.close();
			}
			return tag;
		}

	}

	public static Tag[] getTagArray() {
		synchronized (tagArrayPool) {
			if (tagArrayPool.isEmpty()) {
				return new Tag[128];
			} else {
				return tagArrayPool.remove(tagArrayPool.size()-1);
			}
		}
	}

	public static void freeTagArray(Tag[] v, int tagPointer) {
		synchronized (tagArrayPool) {
//			if(v.length > 10000){
//				System.err.println("LEN > 1000"+v.length);
//			}
			Arrays.fill(v, 0, tagPointer, null);
//			for(int i = 0; i < v.length; i++){
//				v[i] = null;
//			}
			tagArrayPool.add(v);
		}
	}

	private static void cleanTagArray() {
		synchronized (tagArrayPool) {
			while(tagArrayPool.size() > 32){
				tagArrayPool.remove(tagArrayPool.size() -1);
			}
		}
	}

	private static Object readPayload(DataInput dis, byte type, ProfiledDataInputStream prof) throws IOException {
		cleanTagArray();
		switch (type) {
			case 0:
				return null;
			case 1:
				return dis.readByte();
			case 2:
				return dis.readShort();
			case 3:
				return dis.readInt();
			case 4:
				return dis.readLong();
			case 5:
				return dis.readFloat();
			case 6:
				return dis.readDouble();
			case 7:
				long t = System.currentTimeMillis();
				int length = dis.readInt();
				byte[] ba = new byte[length];
				dis.readFully(ba);
				long took = System.currentTimeMillis() - t;
				if (took > 30) {
					System.err.println("[TAG] WARNING Byte DESERIALIZATION took too long: " + took + "; ");
				}
				return ba;
			case 8:
				return dis.readUTF();
			case 9:
				return new Vector3f(dis.readFloat(), dis.readFloat(), dis.readFloat());
			case 10:
				return new Vector3i(dis.readInt(), dis.readInt(), dis.readInt());
			case 11:
				return new Vector3b(dis.readByte(), dis.readByte(), dis.readByte());
			case 12:
				//LIST
				byte lt = dis.readByte();
				int ll = dis.readInt();
				Tag[] lo = new Tag[ll];
				for (int i = 0; i < ll; i++) {
					long bytePos = 0;
					if (prof != null) {
						bytePos = prof.getReadSize();
					}
					lo[i] = new Tag(Type.values()[lt], null, readPayload(dis, lt, prof));
					if (prof != null) {
						lo[i].size = prof.getReadSize() - bytePos;
					}
				}

				if (lo.length == 0)
					return Type.values()[lt];
				else
					return lo;
			case 13:
				//STRUCT
				byte stt;
//			Tag[] tags = new Tag[0];
				long tm = System.currentTimeMillis();

				Tag[] tmpTags = getTagArray();
				int tagPointer = 0;

				do {
					if (tagPointer == tmpTags.length) {
						int sizeBef = tmpTags.length;

						Tag[] tmpTagsNew = new Tag[tagPointer * 2];
						System.arraycopy(tmpTags, 0, tmpTagsNew, 0, tagPointer);

						freeTagArray(tmpTags, tagPointer);
						tmpTags = tmpTagsNew;
					}
					byte prType = dis.readByte();
					stt = (byte) Math.abs(prType);
					boolean hasName = prType > 0;

					String name = null;
					if (hasName) {
						name = dis.readUTF();
					}
					long bytePos = 0;
					if (prof != null) {
						bytePos = prof.getReadSize();
					}
//				Tag[] newTags = new Tag[tags.length + 1];
//				System.arraycopy(tags, 0, newTags, 0, tags.length);
					long ttt = System.currentTimeMillis();
					try {
						tmpTags[tagPointer] = new Tag(Type.values()[stt], name, readPayload(dis, stt, prof));
					} catch (IOException e) {
						System.err.println("EXCEPTION IN " + name + "; array index: " + tagPointer);
						throw e;
					} catch (ArrayIndexOutOfBoundsException e) {
						System.err.println("ARRAY INDEX OUT OF BOUNDS IN " + name + ", when attempting to access pointer index: " + tagPointer + "; temp tags length " + tmpTags.length + "; tag type #" + stt);
						throw e;
					}
					assert (tmpTags[tagPointer] != null);
					if (prof != null) {
						tmpTags[tagPointer].size = prof.getReadSize() - bytePos;
					}
					long plT = System.currentTimeMillis() - ttt;
					if (plT > 100) {
						System.err.println("[TAG] WARNING: Struct read time of " + name + " took: " + plT);
					}
					//                System.err.println("TAG reading subtype "+name+" "+getTypeString(Type.values()[stt])+"; val "+newTags[tags.length].getValue());
					tagPointer++;
				} while (stt != 0);
				Tag[] tags = new Tag[tagPointer];
				System.arraycopy(tmpTags, 0, tags, 0, tagPointer);
//			for(int i = 0; i < tagPointer; i++){
//				assert(tmpTags[i] != null):tagPointer+"; "+i;
//				tags[i] = tmpTags[i];
//			}
				freeTagArray(tmpTags, tagPointer);

				long tookS = System.currentTimeMillis() - tm;
				if (tookS > 30) {
					System.err.println("[TAG] WARNING STRUCT DESERIALIZATION took too long: " + tookS + "; ");
//					try {
//						throw new Exception("[TAG] WARNING STRUCT DESERIALIZATION took too long: " + tookS + "; ");
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
				}
				return tags;
			case 14:
				byte factoryId = dis.readByte();
				return SerializableTagRegister.register[factoryId].create(dis);
			case 15:
				return new Vector4f(dis.readFloat(), dis.readFloat(), dis.readFloat(), dis.readFloat());
			case 16:
				Matrix4f mat = new Matrix4f();
				mat.m00 = dis.readFloat();
				mat.m01 = dis.readFloat();
				mat.m02 = dis.readFloat();
				mat.m03 = dis.readFloat();
				mat.m10 = dis.readFloat();
				mat.m11 = dis.readFloat();
				mat.m12 = dis.readFloat();
				mat.m13 = dis.readFloat();
				mat.m20 = dis.readFloat();
				mat.m21 = dis.readFloat();
				mat.m22 = dis.readFloat();
				mat.m23 = dis.readFloat();
				mat.m30 = dis.readFloat();
				mat.m31 = dis.readFloat();
				mat.m32 = dis.readFloat();
				mat.m33 = dis.readFloat();
				return mat;
			case 17:
				return null;
			case 18:
				Matrix3f matm = new Matrix3f();
				matm.m00 = dis.readFloat();
				matm.m01 = dis.readFloat();
				matm.m02 = dis.readFloat();
				matm.m10 = dis.readFloat();
				matm.m11 = dis.readFloat();
				matm.m12 = dis.readFloat();
				matm.m20 = dis.readFloat();
				matm.m21 = dis.readFloat();
				matm.m22 = dis.readFloat();
				return matm;
		}
		
		return null;
	}

	public static boolean check(File f) {
		try {
			BufferedInputStream is = new BufferedInputStream(new FileInputStream(f));
			readFrom(is, true, false);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean notNull(Tag[] x) {
		for (int i = 0; i < x.length; i++) {
			if (x[i] == null) {
				System.err.println("FATAL Exception: INDEX " + i + " IS NULL");
				return false;
			}
		}
		return true;
	}

	public void addTag(Tag tag) {
		if (type != Type.LIST && type != Type.STRUCT)
			throw new RuntimeException();
		Tag[] subtags = (Tag[]) value;

		int index = subtags.length;

		//For TAG_Compund entries, we need to add the tag BEFORE the end,
		//or the new tag gets placed after the TAG_End, messing up the data.
		//TAG_End MUST be kept at the very end of the TAG_Compound.
		if (type == Type.STRUCT) index--;
		insertTag(tag, index);
	}

	/**
	 * Find the first nested tag with specified description in a TAG_List or TAG_Compound after a tag with the same description.
	 *
	 * @param description the description to look for. May be null to look for unnamed tags.
	 * @param found       the previously found tag with the same description.
	 * @return the first nested tag that has the specified description after the previously found tag.
	 */
	public Tag findNextTagByName(String name, Tag found) {
		if (type != Type.LIST && type != Type.STRUCT)
			return null;
		Tag[] subtags = (Tag[]) value;
		for (Tag subtag : subtags) {
			if ((subtag.name == null && name == null) || (subtag.name != null && subtag.name.equals(name))) {
				return subtag;
			} else {
				Tag newFound = subtag.findTagByName(name);
				if (newFound != null)
					if (newFound == found)
						continue;
					else
						return newFound;
			}
		}
		return null;
	}

	/**
	 * Find the first nested tag with specified description in a TAG_Compound.
	 *
	 * @param description the description to look for. May be null to look for unnamed tags.
	 * @return the first nested tag that has the specified description.
	 */
	public Tag findTagByName(String name) {
		return findNextTagByName(name, null);
	}

	public Type getListType() {
		return listType;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		switch (type) {
			case FINISH:
				if (value != null)
					throw new IllegalArgumentException();
				break;
			case BYTE:
				if (!(value instanceof Byte))
					throw new IllegalArgumentException();
				break;
			case SHORT:
				if (!(value instanceof Short))
					throw new IllegalArgumentException();
				break;
			case INT:
				if (!(value instanceof Integer))
					throw new IllegalArgumentException();
				break;
			case LONG:
				if (!(value instanceof Long))
					throw new IllegalArgumentException();
				break;
			case FLOAT:
				if (!(value instanceof Float))
					throw new IllegalArgumentException();
				break;
			case DOUBLE:
				if (!(value instanceof Double))
					throw new IllegalArgumentException();
				break;
			case BYTE_ARRAY:
				if (!(value instanceof byte[]))
					throw new IllegalArgumentException();
				break;
			case STRING:
				if (!(value instanceof String))
					throw new IllegalArgumentException();
				break;
			case NOTHING:
				break;
			case VECTOR3f:
				if (!(value instanceof Vector3f))
					throw new IllegalArgumentException();
				break;
			case VECTOR3i:
				if (!(value instanceof Vector3i))
					throw new IllegalArgumentException();
				break;
			case VECTOR3b:
				if (!(value instanceof Vector3b))
					throw new IllegalArgumentException();
				break;
			case LIST:
				if (value instanceof Type) {
					this.listType = (Type) value;
					value = new Tag[0];
				} else {
					if (!(value instanceof Tag[]))
						throw new IllegalArgumentException();
					this.listType = (((Tag[]) value)[0]).type;
				}
				break;
			case STRUCT:
				if (!(value instanceof Tag[]))
					throw new IllegalArgumentException();
				break;
			case SERIALIZABLE:
				if (!(value instanceof SerializableTagElement))
					throw new IllegalArgumentException();
				break;
			case VECTOR4f:
				if (!(value instanceof Vector4f))
					throw new IllegalArgumentException();
				break;
			case MATRIX4f:
				if (!(value instanceof Matrix4f))
					throw new IllegalArgumentException();
				break;
			case MATRIX3f:
				if (!(value instanceof Matrix3f))
					throw new IllegalArgumentException();
				break;
			default:
				throw new IllegalArgumentException();
		}

		this.value = value;
	}

	private void indent(int indent) {
		for (int i = 0; i < indent; i++) {
			System.out.print("   ");
		}
	}

	public void insertTag(Tag tag, int index) {
		if (type != Type.LIST && type != Type.STRUCT)
			throw new RuntimeException();
		Tag[] subtags = (Tag[]) value;
		if (subtags.length > 0)
			if (type == Type.LIST && tag.type != listType)
				throw new IllegalArgumentException();
		if (index > subtags.length)
			throw new IndexOutOfBoundsException();
		Tag[] newValue = new Tag[subtags.length + 1];
		System.arraycopy(subtags, 0, newValue, 0, index);
		newValue[index] = tag;
		System.arraycopy(subtags, index, newValue, index + 1, subtags.length - index);
		value = newValue;
	}

	public void print() {
		print(this, 0);
	}

	private void print(Tag t, int indent) {
		Type type = t.type;
		if (type == Type.FINISH)
			return;
		String name = t.name;
		indent(indent);
		System.out.print(getTypeString(t.type));
		if (name != null)
			System.out.print("(\"" + t.name + "\")");
		if (type == Type.BYTE_ARRAY) {
			byte[] b = (byte[]) t.value;
			System.out.println(": [" + b.length + " bytes]");
		} else if (type == Type.LIST) {
			Tag[] subtags = (Tag[]) t.value;
			System.out.println(": " + subtags.length + " entries of type " + getTypeString(t.listType));
			for (Tag st : subtags) {
				print(st, indent + 1);
			}
			indent(indent);
			System.out.println("}");
		} else if (type == Type.STRUCT) {
			Tag[] subtags = (Tag[]) t.value;
			System.out.println(": " + (subtags.length - 1) + " entries");
			indent(indent);
			System.out.println("{");
			for (Tag st : subtags) {
				print(st, indent + 1);
			}
			indent(indent);
			System.out.println("}");
		} else {
			System.out.println(": " + t.value);
		}
	}

	public void removeSubTag(Tag tag) {
		if (type != Type.LIST && type != Type.STRUCT)
			throw new RuntimeException();
		if (tag == null)
			return;
		Tag[] subtags = (Tag[]) value;
		for (int i = 0; i < subtags.length; i++) {
			if (subtags[i] == tag) {
				removeTag(i);
				return;
			} else {
				if (subtags[i].type == Type.LIST || subtags[i].type == Type.STRUCT) {
					subtags[i].removeSubTag(tag);
				}
			}
		}
	}

	public Tag removeTag(int index) {
		if (type != Type.LIST && type != Type.STRUCT)
			throw new RuntimeException();
		Tag[] subtags = (Tag[]) value;
		Tag victim = subtags[index];
		Tag[] newValue = new Tag[subtags.length - 1];
		System.arraycopy(subtags, 0, newValue, 0, index);
		index++;
		System.arraycopy(subtags, index, newValue, index - 1, subtags.length - index);
		value = newValue;
		return victim;
	}

	@Override
	public String toString() {
		return type.name() + ": " + name + "->" + (value == null ? NULL_STRING : (value instanceof Tag[] ? "STRUCT" : value)) + "(size: " + size + ")";
	}

	private void writePayload(DataOutput dos) throws IOException {
		switch (type) {
			case FINISH:
				break;
			case BYTE:
				dos.writeByte((Byte) value);
				break;
			case SHORT:
				dos.writeShort((Short) value);
				break;
			case INT:
				dos.writeInt((Integer) value);
				break;
			case LONG:
				dos.writeLong((Long) value);
				break;
			case FLOAT:
				dos.writeFloat((Float) value);
				break;
			case DOUBLE:
				dos.writeDouble((Double) value);
				break;
			case BYTE_ARRAY:
				byte[] ba = (byte[]) value;
				dos.writeInt(ba.length);
				dos.write(ba);
				break;
			case STRING:
				assert (!((String) value).equals(NULL_STRING));
				dos.writeUTF((String) value);
				break;
			case NOTHING:
				break;
			case VECTOR3f:
				dos.writeFloat(((Vector3f) value).x);
				dos.writeFloat(((Vector3f) value).y);
				dos.writeFloat(((Vector3f) value).z);
				break;
			case VECTOR3i:
				dos.writeInt(((Vector3i) value).x);
				dos.writeInt(((Vector3i) value).y);
				dos.writeInt(((Vector3i) value).z);
				break;
			case VECTOR3b:
				dos.write(((Vector3b) value).x);
				dos.write(((Vector3b) value).y);
				dos.write(((Vector3b) value).z);
				break;
			case SERIALIZABLE:
				dos.writeByte(((SerializableTagElement) value).getFactoryId());
				((SerializableTagElement) value).writeToTag(dos);
				break;
			case LIST:
				Tag[] list = (Tag[]) value;
				dos.writeByte(listType.ordinal());
				dos.writeInt(list.length);
				for (Tag tt : list) {
					tt.writePayload(dos);
				}
				break;
			case STRUCT:
				//        	System.err.println("TAG writingToDiskLock struct");
				Tag[] subtags = (Tag[]) value;
				for (Tag st : subtags) {
                    // Debug code for a rare error
                    if(st == null){
                        throw new NullPointerException("Subtag was null: " + Arrays.toString(subtags));
                    }
                    //
					//            	System.err.println("TAG writingToDiskLock subtype "+st.name+" "+getTypeString(type));
					Tag subtag = st;
					Type type = subtag.type;
					dos.writeByte((subtag.name != null && !subtag.name.equals(NULL_STRING)) ? type.ordinal() : -type.ordinal());
					if (type != Type.FINISH) {
						if (subtag.name != null && !subtag.name.equals(NULL_STRING)) {
							dos.writeUTF(subtag.name);
						}
						subtag.writePayload(dos);
					}
				}
				break;
			case VECTOR4f:
				dos.writeFloat(((Vector4f) value).x);
				dos.writeFloat(((Vector4f) value).y);
				dos.writeFloat(((Vector4f) value).z);
				dos.writeFloat(((Vector4f) value).w);
				break;
			case MATRIX4f:
				Matrix4f m = ((Matrix4f) value);

				dos.writeFloat(m.m00);
				dos.writeFloat(m.m01);
				dos.writeFloat(m.m02);
				dos.writeFloat(m.m03);
				dos.writeFloat(m.m10);
				dos.writeFloat(m.m11);
				dos.writeFloat(m.m12);
				dos.writeFloat(m.m13);
				dos.writeFloat(m.m20);
				dos.writeFloat(m.m21);
				dos.writeFloat(m.m22);
				dos.writeFloat(m.m23);
				dos.writeFloat(m.m30);
				dos.writeFloat(m.m31);
				dos.writeFloat(m.m32);
				dos.writeFloat(m.m33);
				break;
			case MATRIX3f:
				Matrix3f mm = ((Matrix3f) value);
				
				dos.writeFloat(mm.m00);
				dos.writeFloat(mm.m01);
				dos.writeFloat(mm.m02);
				dos.writeFloat(mm.m10);
				dos.writeFloat(mm.m11);
				dos.writeFloat(mm.m12);
				dos.writeFloat(mm.m20);
				dos.writeFloat(mm.m21);
				dos.writeFloat(mm.m22);
				break;
		}
	}

	public static Tag deserializeNT(DataInput os) throws IOException {
		Type type = Type.values()[os.readByte()];
		return new Tag(type, null, readPayload(os, (byte) type.ordinal(), null));
		
	}
	public void serializeNT(DataOutput os) throws IOException {
		os.writeByte(type.ordinal());
		writePayload(os);
	}
	public void writeTo(OutputStream os, boolean closeStream) throws IOException {
		//		GZIPOutputStream gzos;
		//		DataOutputStream dos = new DataOutputStream(gzos = new GZIPOutputStream(os, 1024));
		DataOutputStream dos;
		if (os instanceof DataOutputStream) {
			dos = (DataOutputStream) os;
		} else {
			dos = new DataOutputStream(os);
		}
		dos.writeShort(version);
		dos.writeByte((name != null && !name.equals(NULL_STRING)) ? type.ordinal() : -type.ordinal());
		if (type != Type.FINISH) {
			if (name != null && !name.equals(NULL_STRING)) {
				dos.writeUTF(name);
			}
			writePayload(dos);
		}

		//		gzos.flush();
		//		gzos.finish();
		if (closeStream) {
			os.close();
			//			gzos.close();
		}
	}

	public void writeToZipped(OutputStream os, boolean closeStream) throws IOException {
		if (tools == null) {
			tools = new TagTools();
		}
		DataOutputStream dos = new DataOutputStream(tools.output);
		synchronized (tools.output) {

			tools.output.reset();
			DataOutputStream to = new DataOutputStream(tools.output);
			dos.writeByte((name != null && !name.equals(NULL_STRING)) ? type.ordinal() : -type.ordinal());
			if (type != Type.FINISH) {
				//        	System.err.println("[TAG] writingToDiskLock: "+description);
				if (name != null && !name.equals(NULL_STRING)) {
					dos.writeUTF(name);
				}
				writePayload(to);
			}
			dos.writeLong(tools.output.position());
			tools.deflater.setInput(tools.output.array, 0, (int) tools.output.position());

			int deflate = tools.deflater.deflate(tools.deflateBuffer);
			dos.writeInt(deflate);

			dos.write(tools.deflateBuffer, 0, deflate);
		}
		os.flush();
		if (closeStream) {
			os.close();
		}

	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	public enum Type {
		FINISH,
		BYTE,
		SHORT,
		INT,
		LONG,
		FLOAT,
		DOUBLE,
		BYTE_ARRAY,
		STRING,
		VECTOR3f,
		VECTOR3i,
		VECTOR3b,
		LIST,
		STRUCT,
		SERIALIZABLE,
		VECTOR4f,
		MATRIX4f,
		NOTHING,
		MATRIX3f,
	}

	public byte getByte() {
		assert(type == Type.BYTE):type;
		return ((Byte) value).byteValue();
	}
	public short getShort() {
		assert(type == Type.SHORT):type;
		return ((Short) value).shortValue();
	}
	public int getInt() {
		assert(type == Type.INT):type;
		return ((Integer) value).intValue();
	}
	public long getLong() {
		assert(type == Type.LONG):type;
		return ((Long) value).longValue();
	}
	public float getFloat() {
		assert(type == Type.FLOAT):type;
		return ((Float) value).floatValue();
	}
	public double getDouble() {
		assert(type == Type.DOUBLE):type;
		return ((Double) value).doubleValue();
	}
	public String getString() {
		assert(type == Type.STRING):type;
		return (String) value;
	}
	public Tag[] getStruct() {
		assert(type == Type.STRUCT):type;
		return (Tag[]) value;
	}
	public Vector3i getVector3i() {
		assert(type == Type.VECTOR3i):type;
		return (Vector3i) value;
	}
	public Vector3f getVector3f() {
		assert(type == Type.VECTOR3f):type;
		return (Vector3f) value;
	}

	public byte[] getByteArray() {
		assert(type == Type.BYTE_ARRAY):type;
		return (byte[]) value;
	}
	public Matrix4f getMatrix4f() {
		assert(type == Type.MATRIX4f):type;
		return (Matrix4f) value;
	}
	public Matrix3f getMatrix3f() {
		assert(type == Type.MATRIX3f):type;
		return (Matrix3f) value;
	}

	public boolean getBoolean() {
		return getByte() != 0;
	}
	public static Tag getStringTag(String b) {
		return new Tag(Type.STRING, null, b);
	}
	public static Tag getFloatTag(float b) {
		return new Tag(Type.FLOAT, null, b);
	}
	public static Tag getLongTag(long b) {
		return new Tag(Type.LONG, null, b);
	}
	public static Tag getIntTag(int b) {
		return new Tag(Type.INT, null, b);
	}
	public static Tag getByteTag(byte b) {
		return new Tag(Type.BYTE, null, b);
	}
	public static Tag getBooleanTag(boolean b) {
		return new Tag(Type.BYTE, null, b ? (byte)1 : (byte)0);
	}

}
