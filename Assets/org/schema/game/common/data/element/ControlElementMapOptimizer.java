package org.schema.game.common.data.element;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map.Entry;

import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.schine.network.DataInputStreamPositional;
import org.schema.schine.network.DataOutputStreamPositional;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;

public class ControlElementMapOptimizer {

	public static boolean CHECK_SANITY = false;
	private final ObjectArrayFIFOQueue<ShortArrayList> listPool = new ObjectArrayFIFOQueue<ShortArrayList>();
	private Short2ObjectOpenHashMap<ShortArrayList> data = new Short2ObjectOpenHashMap<ShortArrayList>();
//	private int minX;
//	private int minY;
//	private int minZ;
//	private int maxX;
//	private int maxY;
//	private int maxZ;
//	private int distX;
//	private int distY;
//	private int distZ;
//	private int medianX;
//	private int medianY;
//	private int medianZ;

	private void analyse(Entry<Short, FastCopyLongOpenHashSet> v, int valuesSize, DataOutputStreamPositional outputStream) throws IOException {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;

		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		short type = 0;
		for (long g : v.getValue()) {
			int posX = ElementCollection.getPosX(g);
			int posY = ElementCollection.getPosY(g);
			int posZ = ElementCollection.getPosZ(g);

			minX = Math.min(minX, posX);
			minY = Math.min(minY, posY);
			minZ = Math.min(minZ, posZ);

			maxX = Math.max(maxX, posX);
			maxY = Math.max(maxY, posY);
			maxZ = Math.max(maxZ, posZ);
		}
		int distX = maxX - minX;
		int distY = maxY - minY;
		int distZ = maxZ - minZ;

		boolean bigX = distX > 255;
		boolean bigY = distY > 255;
		boolean bigZ = distZ > 255;

		int medianX = minX + distX / 2;
		int medianY = minY + distY / 2;
		int medianZ = minZ + distZ / 2;

		outputStream.writeBoolean(bigX);
		outputStream.writeBoolean(bigY);
		outputStream.writeBoolean(bigZ);

		outputStream.writeShort(medianX);
		outputStream.writeShort(medianY);
		outputStream.writeShort(medianZ);

		if ((bigX && !bigY && !bigZ)) {
			//X big
//			System.err.println("BIG X");
			bigX(v, outputStream, minX, minY, minZ);
		} else if (!bigX && bigY && !bigZ) {
			//Y big
			bigY(v, outputStream, minX, minY, minZ);
//			System.err.println("BIG Y");
		} else if (!bigX && !bigY && bigZ) {
			//Z big
			bigZ(v, outputStream, minX, minY, minZ);
//			System.err.println("BIG Z");
		} else {
//			System.err.println(" BIG/SMALL::: "+bigX+", "+bigY+", "+bigZ+"; median: "+medianX+"; "+medianY+"; "+medianZ+";;; min max [("+minX+", "+minY+", "+minZ+") ;; ("+maxX+", "+maxY+", "+maxZ+")]");
			//style
			outputStream.writeByte(0);

			int byteSize = 0;
			for (long g : v.getValue()) {
				int posX = ElementCollection.getPosX(g) - medianX;
				int posY = ElementCollection.getPosY(g) - medianY;
				int posZ = ElementCollection.getPosZ(g) - medianZ;
				if (bigX) {
					assert (posX >= Short.MIN_VALUE && posX <= Short.MAX_VALUE) : posX;
					outputStream.writeShort(posX);
					byteSize += 2;
				} else {
					assert (posX >= Byte.MIN_VALUE && posX <= Byte.MAX_VALUE) : posX;
					outputStream.writeByte(posX);
					byteSize++;
				}
				if (bigY) {
					assert (posY >= Short.MIN_VALUE && posY <= Short.MAX_VALUE) : posY;
					outputStream.writeShort(posY);
					byteSize += 2;
				} else {
					assert (posY >= Byte.MIN_VALUE && posY <= Byte.MAX_VALUE) : posY;
					outputStream.writeByte(posY);
					byteSize++;
				}
				if (bigZ) {
					assert (posZ >= Short.MIN_VALUE && posZ <= Short.MAX_VALUE) : posZ;
					outputStream.writeShort(posZ);
					byteSize += 2;
				} else {
					assert (posZ >= Byte.MIN_VALUE && posZ <= Byte.MAX_VALUE) : posZ;
					outputStream.writeByte(posZ);
					byteSize++;
				}
			}
//			System.err.println("size in bytes: "+byteSize+"; position: "+outputStream.position());
//			assert (outputStream.getArray().length > outputStream.position()):outputStream.getArray().length+"; "+outputStream.position();
		}

		data.clear();
	}

	private ShortArrayList getList() {
		if (listPool.isEmpty()) {
			return new ShortArrayList();
		} else {
			ShortArrayList dequeue = listPool.dequeue();
			assert (dequeue.isEmpty());
			return dequeue;
		}
	}

	private void free(ShortArrayList c) {
		c.clear();
		listPool.enqueue(c);
	}

	private void encode(DataOutputStream outputStream, int valuesSize, byte style, int minX, int minY, int minZ) throws IOException {
		for (ShortArrayList a : data.values()) {
			Collections.sort(a);
		}
		outputStream.writeByte(style);

		outputStream.writeShort(minX);
		outputStream.writeShort(minY);
		outputStream.writeShort(minZ);

		outputStream.writeShort(data.size());
//		System.err.println("ENDATA____________________ "+data.size());
		int total = 0;
		for (Entry<Short, ShortArrayList> a : data.entrySet()) {
			short key = a.getKey();
			outputStream.writeShort(key);

			ShortArrayList list = a.getValue();
			int size = list.size();

			ShortArrayList listReduced = getList();

			for (int i = 0; i < size; i++) {
				listReduced.add(list.get(i));

				short amount = 1;
				while (i + 1 < size && list.get(i + 1) == (list.get(i) + 1)) {
					amount++;
					i++;
				}
				listReduced.add(amount);
//				System.err.println("ENCODING "+i+" :: bigPos: "+list.get(i)+" amount: "+amount);
				total += amount;
			}

			assert (listReduced.size() % 2 == 0);

			int ss = listReduced.size();
//			System.err.println("---ENDATA____________________LS "+ss/2);
//			System.err.println("ENC KY:::: "+key+" LS: "+ss/2);
			outputStream.writeShort(ss / 2);
			for (int i = 0; i < ss; i++) {
				outputStream.writeShort(listReduced.get(i));
			}
			free(list);
			free(listReduced);
		}
		assert (total == valuesSize) : total + "/" + valuesSize;
	}

	private void bigX(Entry<Short, FastCopyLongOpenHashSet> v, DataOutputStream outputStream, int minX, int minY, int minZ) throws IOException {
		for (long g : v.getValue()) {
			short big = (short) (ElementCollection.getPosX(g));
			int small0 = ElementCollection.getPosY(g) - minY;
			int small1 = ElementCollection.getPosZ(g) - minZ;

			
			//this is encoded and can be negative. It will be decoded to unsigned on deserialization
			short coord = (short) (small0 + 256 * small1);
			ShortArrayList shortArrayList = data.get(coord);
			if (shortArrayList == null) {
				shortArrayList = getList();
				data.put(coord, shortArrayList);
			}
			shortArrayList.add(big); //list is from 0 to max
		}

		encode(outputStream, v.getValue().size(), (byte) 1, minX, minY, minZ);
	}

	private void bigY(Entry<Short, FastCopyLongOpenHashSet> v, DataOutputStream outputStream, int minX, int minY, int minZ) throws IOException {
		for (long g : v.getValue()) {
			short big = (short) (ElementCollection.getPosY(g));
			int small0 = ElementCollection.getPosX(g) - minX;
			int small1 = ElementCollection.getPosZ(g) - minZ;

			//this is encoded and can be negative. It will be decoded to unsigned on deserialization
			short coord = (short) (small0 + 256 * small1); 
			ShortArrayList shortArrayList = data.get(coord);
			if (shortArrayList == null) {
				shortArrayList = getList();
				data.put(coord, shortArrayList);
			}
			shortArrayList.add(big); //list is from 0 to max
		}

		encode(outputStream, v.getValue().size(), (byte) 2, minX, minY, minZ);

	}

	private void bigZ(Entry<Short, FastCopyLongOpenHashSet> v, DataOutputStream outputStream, int minX, int minY, int minZ) throws IOException {
		for (long g : v.getValue()) {
			short big = (short) (ElementCollection.getPosZ(g));
			int small0 = ElementCollection.getPosX(g) - minX;
			int small1 = ElementCollection.getPosY(g) - minY;

			assert (small0 >= 0 && small0 < 256) : small0;
			assert (small1 >= 0 && small1 < 256) : small1;
			
			//this is encoded and can be negative. It will be decoded to unsigned on deserialization
			short coord = (short) (small0 + 256 * small1);
			ShortArrayList shortArrayList = data.get(coord);
			if (shortArrayList == null) {
				shortArrayList = getList();
				data.put(coord, shortArrayList);
			}
			shortArrayList.add(big); //list is from 0 to max
		}

		encode(outputStream, v.getValue().size(), (byte) 3, minX, minY, minZ);

	}

	public void serialize(ControlElementMapper map, Entry<Short, FastCopyLongOpenHashSet> v, int valuesSize, DataOutputStreamPositional outputStream) throws IOException {

		analyse(v, valuesSize, outputStream);

	}

	public boolean checkSanity(DataOutputStreamPositional outputStream, ControlElementMapper map) throws IOException {

		DataInputStreamPositional dataInputStream = new DataInputStreamPositional(new FastByteArrayInputStream(outputStream.getArray(), 0, outputStream.getArray().length));

		long t = System.currentTimeMillis();
		int keySize = dataInputStream.readInt();
		if (keySize > 0) {
			//WILL NOT HAPPEN ON NT
			assert (false);
			dataInputStream.close();
			return false;
		}
		int version = -keySize;
		keySize = dataInputStream.readInt();

		for (int i = 0; i < keySize; i++) {
			short xKey = dataInputStream.readShort();
			short yKey = dataInputStream.readShort();
			short zKey = dataInputStream.readShort();
			long key = ElementCollection.getIndex(xKey, yKey, zKey);

			int valueSize = dataInputStream.readInt();

			for (int v = 0; v < valueSize; v++) {

				short type = dataInputStream.readShort();

				int elementSize = dataInputStream.readInt();

				boolean bigX = dataInputStream.readBoolean();
				boolean bigY = dataInputStream.readBoolean();
				boolean bigZ = dataInputStream.readBoolean();

				int medianX = dataInputStream.readShort();
				int medianY = dataInputStream.readShort();
				int medianZ = dataInputStream.readShort();

				byte style = dataInputStream.readByte();

				short[] elements = new short[3 * elementSize];
				int eIndex = 0;

//				System.err.println("CHECKING NOW: "+xKey+", "+yKey+", "+zKey+" Median "+medianX+"; "+medianY+", "+medianZ+", "+bigX+", "+bigY+", "+bigZ);
				if (style == 0) {

					if (elementSize > 0) {
						int predictedSize = elementSize * (bigX ? 2 : 1);
						predictedSize += elementSize * (bigY ? 2 : 1);
						predictedSize += elementSize * (bigZ ? 2 : 1);
//						System.err.println("PREDICTED SIZE: "+predictedSize);
						assert (dataInputStream.position() + predictedSize < outputStream.getArray().length) : dataInputStream.position() + predictedSize + " / " + outputStream.getArray().length;
						for (int e = 0; e < elementSize; e++) {

							elements[eIndex] = (short) ((bigX ? dataInputStream.readShort() : dataInputStream.readByte()) + medianX);
							elements[eIndex + 1] = (short) ((bigY ? dataInputStream.readShort() : dataInputStream.readByte()) + medianY);
							elements[eIndex + 2] = (short) ((bigZ ? dataInputStream.readShort() : dataInputStream.readByte()) + medianZ);

							assert (map.getAll().containsKey(key)) : key + "; Median " + medianX + "; " + medianY + ", " + medianZ;
							long val = ElementCollection.getIndex4(elements[eIndex], elements[eIndex + 1], elements[eIndex + 2], type);

							assert (map.getAll().get(key).contains(val)) : xKey + ", " + yKey + ", " + zKey + " -> " + elements[eIndex] + ", " + elements[eIndex + 1] + ", " + elements[eIndex + 2] + "; Median " + medianX + "; " + medianY + ", " + medianZ;

							eIndex += 3;
						}
					}
				} else {
					int eIndexMax = decodeBig(dataInputStream, elements, style);

					for (int e = 0; e < eIndexMax; e += 3) {
						assert (map.getAll().containsKey(key)) : key + "; Median " + medianX + "; " + medianY + "; " + medianZ;
						long val = ElementCollection.getIndex4(elements[e], elements[e + 1], elements[e + 2], type);

						assert (map.getAll().get(key).contains(val)) : xKey + ", " + yKey + ", " + zKey + " -> " + elements[e] + ", " + elements[e + 1] + ", " + elements[e + 2] + "; Median " + medianX + "; " + medianY + ", " + medianZ + "; big " + bigX + ", " + bigY + ", " + bigZ;
					}
				}

			}
		}

		return true;
	}

	public void serializeSimple(Entry<Short, FastCopyLongOpenHashSet> v, DataOutputStream outputStream) throws IOException {

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;

		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		short type = 0;
		for (long g : v.getValue()) {
			type = (short) ElementCollection.getType(g);
			int posX = ElementCollection.getPosX(g);
			int posY = ElementCollection.getPosY(g);
			int posZ = ElementCollection.getPosZ(g);

			minX = Math.min(minX, posX);
			minY = Math.min(minY, posY);
			minZ = Math.min(minZ, posZ);

			maxX = Math.max(maxX, posX);
			maxY = Math.max(maxY, posY);
			maxZ = Math.max(maxZ, posZ);
		}
		int distX = maxX - minX;
		int distY = maxY - minY;
		int distZ = maxZ - minZ;
//		System.err.println("________________ in "+v.getValue().size()+" values, we have ["+distX+", "+distY+", "+distZ+"] "+ElementKeyMap.toString(type));
		final boolean bigX = distX > 255;
		final boolean bigY = distY > 255;
		final boolean bigZ = distZ > 255;

		outputStream.writeBoolean(bigX);
		outputStream.writeBoolean(bigY);
		outputStream.writeBoolean(bigZ);

		int medianX = minX + distX / 2;
		int medianY = minY + distY / 2;
		int medianZ = minZ + distZ / 2;

		outputStream.writeShort(medianX);
		outputStream.writeShort(medianY);
		outputStream.writeShort(medianZ);

//		System.err.println("SENDDDDD SIZE: "+v.getValue().size()+"; MEDIAN: "+medianX+", "+medianY+", "+medianZ+"; ;;; "+bigX+", "+bigY+", "+bigZ);

		for (long g : v.getValue()) {
			int posX = ElementCollection.getPosX(g) - medianX;
			int posY = ElementCollection.getPosY(g) - medianY;
			int posZ = ElementCollection.getPosZ(g) - medianZ;
			//TODO very ugly. loop optimization will do the job, but still
			if (bigX) {
				outputStream.writeShort(posX);
			} else {
				assert (posX >= Byte.MIN_VALUE && posX <= Byte.MAX_VALUE) : posX;
				outputStream.writeByte(posX);
			}
			if (bigY) {
				outputStream.writeShort(posY);
			} else {
				assert (posY >= Byte.MIN_VALUE && posY <= Byte.MAX_VALUE) : posY;
				outputStream.writeByte(posY);
			}
			if (bigZ) {
				outputStream.writeShort(posZ);
			} else {
				assert (posZ >= Byte.MIN_VALUE && posZ <= Byte.MAX_VALUE) : posZ;
				outputStream.writeByte(posZ);
			}
//			ElementCollection.writeIndexAsShortPos(g, outputStream);
		}
	}

	private int decodeBig(DataInputStream dataInputStream, short[] elements, byte style) throws IOException {
		int eIndex = 0;

		short minX = dataInputStream.readShort();
		short minY = dataInputStream.readShort();
		short minZ = dataInputStream.readShort();

		short dataSize = dataInputStream.readShort();

//		System.err.println("DEDATA____________________ "+dataSize);
		for (int j = 0; j < dataSize; j++) {

			int key = dataInputStream.readShort() & 0xFFFF; //convert to unsigned short

			short listSize = dataInputStream.readShort();

//			System.err.println("SWC KY:::: "+key+" LS: "+listSize);
//			System.err.println("---DEATA____________________LS "+listSize);

			short small0 = (short) (key % 256);
			short small1 = (short) (key / 256);

			for (int i = 0; i < listSize; i++) {
				short bigPos = dataInputStream.readShort();
				short bigAmount = dataInputStream.readShort();

//				System.err.println("DECODING "+i+" :: bigPos: "+bigPos+" amount: "+bigAmount);

				for (int k = 0; k < bigAmount; k++) {
					switch(style) {
						case 1 -> decodeX(eIndex, bigPos, small0, small1, k, dataInputStream, elements, minX, minY, minZ);
						case 2 -> decodeY(eIndex, bigPos, small0, small1, k, dataInputStream, elements, minX, minY, minZ);
						case 3 -> decodeZ(eIndex, bigPos, small0, small1, k, dataInputStream, elements, minX, minY, minZ);
					}

					eIndex += 3;
				}
			}
		}
		return eIndex;
	}

	private void decodeX(int eIndex, short bigPos, short small0, short small1, int k, DataInputStream dataInputStream, short[] elements, short minX, short minY, short minZ) {
		elements[eIndex] = (short) (bigPos + k);
		elements[eIndex + 1] = (short) (small0 + minY);
		elements[eIndex + 2] = (short) (small1 + minZ);
	}

	private void decodeY(int eIndex, short bigPos, short small0, short small1, int k, DataInputStream dataInputStream, short[] elements, short minX, short minY, short minZ) {
		elements[eIndex] = (short) (small0 + minX);
		elements[eIndex + 1] = (short) (bigPos + k);
		elements[eIndex + 2] = (short) (small1 + minZ);
	}

	private void decodeZ(int eIndex, short bigPos, short small0, short small1, int k, DataInputStream dataInputStream, short[] elements, short minX, short minY, short minZ) {
		elements[eIndex] = (short) (small0 + minX);
		elements[eIndex + 1] = (short) (small1 + minY);
		elements[eIndex + 2] = (short) (bigPos + k);
	}

	public void deserialize(short type, long key, int elementSize, ControlElementMapper delayedNTUpdatesMap,
	                        DataInputStream dataInputStream) throws IOException {

		boolean bigX = dataInputStream.readBoolean();
		boolean bigY = dataInputStream.readBoolean();
		boolean bigZ = dataInputStream.readBoolean();

		short medianX = dataInputStream.readShort();
		short medianY = dataInputStream.readShort();
		short medianZ = dataInputStream.readShort();

		byte style = dataInputStream.readByte();

		short[] elements = new short[3 * elementSize];
		int eIndex = 0;

		if (style == 0) {

			if (elementSize > 0) {

				for (int e = 0; e < elementSize; e++) {

					elements[eIndex] = (short) ((bigX ? dataInputStream.readShort() : dataInputStream.readByte()) + medianX);
					elements[eIndex + 1] = (short) ((bigY ? dataInputStream.readShort() : dataInputStream.readByte()) + medianY);
					elements[eIndex + 2] = (short) ((bigZ ? dataInputStream.readShort() : dataInputStream.readByte()) + medianZ);

					eIndex += 3;

				}

				delayedNTUpdatesMap.putAll(key, elements, type);
			}
		} else {
			decodeBig(dataInputStream, elements, style);
			delayedNTUpdatesMap.putAll(key, elements, type);
		}
	}

}
