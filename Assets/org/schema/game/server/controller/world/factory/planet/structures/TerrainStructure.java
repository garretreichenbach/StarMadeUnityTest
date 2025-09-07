package org.schema.game.server.controller.world.factory.planet.structures;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.GenerationElementMap;
import org.schema.game.server.controller.RequestDataStructureGen;
import org.schema.schine.resource.FileExt;

import java.io.*;
import java.util.HashMap;
import java.util.Random;

public abstract class TerrainStructure {

	public enum Type {
		SingleBlock(new TerrainStructureSingleBlock()),
		ResourceBlob(new TerrainStructureResourceBlob()),
		ResourceVein(new TerrainStructureResourceVein()),
		Rock(new TerrainStructureRock()),
		TreeEarthSmall(new TerrainStructureMulti("GenericTree/genericTree")),//new TerrainStructureTree(6f, 1f, 3f)),
		TreeEarthForest(new TerrainStructureMulti("EarthOak/earthOak")),//new TerrainStructureTree(11f, 2f, 6.5f)),
		TreeDead(new TerrainStructureMulti("DeadTreeGeneric/deadTree")),
		TreeDeadLarge(new TerrainStructureMulti("DeadOak/deadOak")),
		Cactus(new TerrainStructureCactus()),
		IceShard(new TerrainStructureIceShard()),
		Crater(new TerrainStructureCrater()),

		BP_BigTree(TerrainStructureBlueprint.get("bigTree/tree")),
		BP_Test(TerrainStructureBlueprint.get("bpTest/bpTest")),
		BP_TownTest(TerrainStructureBlueprint.get("TownTest/TownTest")),

		BPM_EarthOak(new TerrainStructureMulti("EarthOak/oak"));

		public TerrainStructure terrainStructure;

		Type(TerrainStructure ts) {
			assert (ts != null);
			terrainStructure = ts;
		}
	}

	public final Vector3i bbMin = new Vector3i(-100000, -100000, -100000);
	public final Vector3i bbMax = new Vector3i(100000, 100000, 100000);

	private static boolean configSetup = false;

	public static void readWriteConfig() {
		if (configSetup)
			return;

		configSetup = true;

//		System.err.println("[SERVER][TERRAIN_STRUCTURE] Reading config...");
		Type[] typeArray = Type.values();
		HashMap<String, TerrainStructure> structureMap = new HashMap<String, TerrainStructure>(typeArray.length);
		HashMap<TerrainStructure, String> structureStringMap = new HashMap<TerrainStructure, String>(typeArray.length);

		for (Type t : typeArray) {
			structureMap.put(t.name(), t.terrainStructure);
			structureStringMap.put(t.terrainStructure, t.name());
		}

		try {
			File f = new FileExt("./blueprints-terrain/config.cfg");
			BufferedReader bf = new BufferedReader(new FileReader(f));
			String line = null;
			while ((line = bf.readLine()) != null) {
				if (line.trim().startsWith("//")) {
					continue;
				}
				if (line.contains("//")) {
					line = line.substring(0, line.indexOf("//"));
				}
				String[] split = line.split("=", 2);

				if (split.length < 2) {
//					System.err.println("[SERVER][TERRAIN_STRUCTURE] Could not read line: " + line);
					continue;
				}

				for (String s : split)
					s.trim();

				boolean found = false;

				for (Type t : typeArray) {
					if (!t.name().equals(split[0]))
						continue;

					if (split[1].startsWith("*")) {
						TerrainStructure ts = structureMap.get(split[1].substring(1));

						if (ts == null)
							System.err.println("[SERVER][TERRAIN_STRUCTURE] No structure class called: " + split[1] + " - Using default for: " + split[0]);
						else
							t.terrainStructure = ts;
					} else if (split[1].endsWith("*")) {
						t.terrainStructure = new TerrainStructureMulti(split[1].substring(0, split[1].length() - 1));
					} else {
						t.terrainStructure = TerrainStructureBlueprint.get(split[1]);
					}
					found = true;
					break;
				}

				if (!found)
					System.err.println("[SERVER][STRUCTURE_CONFIG] No entry found for: " + split[0]);
			}

			bf.close();

		} catch (Exception e) {
			if (!(e instanceof FileNotFoundException))
				e.printStackTrace();

			System.err.println("[TERRAIN_STRUCTURE] Could not read terrain structure config file, using defaults");
		}

//		System.err.println("[SERVER][TERRAIN_STRUCTURE] Writing config...");

		File f;
		PrintWriter pw = null;

		try {
			File dir = new FileExt("./blueprints-terrain/");
			dir.mkdir();
			f = new FileExt("./blueprints-terrain/config.cfg");
			pw = new PrintWriter(f);

			pw.println("// * Prefix denotes a code based terrain structure, mismatching these with other structures may cause unexpected results due to wrong/empty metadata");
			pw.println("// * Suffix denotes a multi blueprint structure, all contiguous indexed file names will be loaded and a random blueprint will placed each time");
			pw.println("// Replacing code based terrain structures with (multi) blueprints is safe");
			pw.println("// Incorrect blueprint file path will result in no structure being placed");
			pw.println("//");

		} catch (Exception e) {
			System.err.println("[TERRAIN_STRUCTURE] Could not write terrain structure config file");
			e.printStackTrace();
		}

		for (Type t : typeArray) {
			if (t.terrainStructure instanceof TerrainStructureBlueprint) {
				TerrainStructureBlueprint bp = (TerrainStructureBlueprint) t.terrainStructure;

				bp.loadFromFile();

				if (pw != null)
					pw.println(t.name() + "=" + bp.path);
			} else if (t.terrainStructure instanceof TerrainStructureMulti) {
				TerrainStructureMulti bp = (TerrainStructureMulti) t.terrainStructure;

				bp.loadAllBlueprints();

				if (pw != null)
					pw.println(t.name() + "=" + bp.path + "*");
			} else if (pw != null) {
				pw.println(t.name() + "=*" + structureStringMap.get(t.terrainStructure));
			}
		}

		if (pw != null)
			pw.close();

		GenerationElementMap.updateLookupArray();
	}

	public abstract void build(Segment seg, RequestDataStructureGen reqData, int x, int y, int z, short metaData0, short metaData1, short metaData2) throws SegmentDataWriteException;

	public static short toHalfFloat(final float v) {
		if (v == 0.0f)
			return (short) 0x0000;

		assert (v <= 65504.0f);  // max value supported by half float
		assert (v >= -65504.0f);

		final int f = Float.floatToRawIntBits(v);

		return (short) (((f >> 16) & 0x8000) | ((((f & 0x7f800000) - 0x38000000) >> 13) & 0x7c00) | ((f >> 13) & 0x03ff));
	}

	public static float toFloat(final short half) {
		return switch(half) {
			case (short) 0x0000 -> 0.0f;
			default -> Float.intBitsToFloat(((half & 0x8000) << 16) | (((half & 0x7c00) + 0x1C000) << 13) | ((half & 0x03FF) << 13));
		};
	}

	public static short random(Random r) {
		return (short) r.nextInt(32768);
	}

	public static int registerBlock(short block) {
		return GenerationElementMap.getBlockDataIndex(SegmentData.makeDataInt(block));
	}
}
