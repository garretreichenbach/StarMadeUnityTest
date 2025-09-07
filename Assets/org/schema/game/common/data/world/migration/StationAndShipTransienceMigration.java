package org.schema.game.common.data.world.migration;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.schema.common.util.StringTools;
import org.schema.game.common.controller.io.SegmentDataIO16;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class StationAndShipTransienceMigration {
	
	private ObjectOpenHashSet<File> toDelete;
	
	public void convertDatabase(String databasePath){
		
		toDelete = new ObjectOpenHashSet<File>();
		File dir = new FileExt(databasePath);
		
		File[] listFiles = dir.listFiles();
		
		
		List<BlueprintEntry> pir = BluePrintController.stationsPirate.readBluePrints();
		List<BlueprintEntry> trade = BluePrintController.stationsTradingGuild.readBluePrints();
		List<BlueprintEntry> neut = BluePrintController.stationsNeutral.readBluePrints();
		
		List<BlueprintEntry> all = new ObjectArrayList<BlueprintEntry>();
		all.addAll(neut);
		all.addAll(trade);
		all.addAll(pir);
		int c = 0;
		for(File endFile : listFiles){
			if(c % 100 == 0 || c == listFiles.length-1){
				System.out.println("PROCESSING DATABASE FILE "+(c+1)+" / "+listFiles.length);
			}
			if(endFile.getName().endsWith(".ent") && endFile.getName().startsWith("ENTITY_SPACESTATION_")){
				for(BlueprintEntry e : all){
					String uidNameStation = "ENTITY_SPACESTATION_Station_" + e.getName();
//					System.out.println("CHECKING ENTITY FILE "+endFile.getName()+" WITH BLUEPRINT "+uidNameStation);
					if(endFile.getName().startsWith(uidNameStation)){
						System.out.println("FOUND POSSILE CANDIDATE "+endFile.getAbsolutePath()+"; COLLECTING RAILS/DOCKS AND CHECKING CHECKSUMS WITH BLUEPRINT "+e.getName()+"");
						checkReplacement(e, endFile, databasePath+"/DATA/");
						String uidNameShip = endFile.getName().replace("SPACESTATION", "SHIP");
						uidNameShip = uidNameShip.substring(0, uidNameShip.lastIndexOf('.'));
						System.out.println("SHIP NAME PATTERN TO CHECK "+uidNameShip);
						for(File shipFile : listFiles){
							if(shipFile.getName().startsWith(uidNameShip)){
								checkReplacement(e, shipFile, databasePath+"/DATA/");
							}
						}
					}
				}
			}
			c++;
		}
		
		long size = 0;
		for(File f : toDelete){
			System.out.println("CLEANING UP FILE: "+f.getName());
			size+= f.length();
			f.delete();
		}
		System.out.println("DATABASE CLEANUP COMPLETED! cleaned up "+StringTools.readableFileSize(size)+" from the database!");
	}

	private void checkReplacement(BlueprintEntry e, File endFile, String dataPath) {
		
		File dir = new FileExt(dataPath);
		
		String name = endFile.getName().substring(0, endFile.getName().length()-4);
		final String pattern = name+".";
		File[] listFiles = dir.listFiles((dir1, name1) -> name1.startsWith(pattern) && name1.endsWith(SegmentDataIO16.BLOCK_FILE_EXT));
		String digests[] = new String[listFiles.length];
		for(int i = 0; i < listFiles.length; i++){
			try {
				FileInputStream fis = new FileInputStream(listFiles[i]);
				digests[i] = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
				fis.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		if(listFiles.length > 0){
			checkReplacementRec(e, listFiles, digests, endFile);
		}else{
			System.out.println("NO FILES FOUND MATCHING PATTERN: "+pattern);
		}
		
		
	}
	public boolean compare(File f0, File f1) throws IOException{
		
		if(f0.length() == f1.length()  ){
			if(f0.length() >= SegmentDataIO16.headerTotalSize){
				DataInputStream in0 = null;
				DataInputStream in1 = null;
			
				try{
					in0 = new DataInputStream(new BufferedInputStream(new FileInputStream(f0)));
					in1 = new DataInputStream(new BufferedInputStream(new FileInputStream(f1)));
			
					if( in0.readInt() != in1.readInt()){
						return false;
					}
					for (int i = 0; i < SegmentDataIO16.size; i++) {
						int offsetA = in0.readInt();
						int offsetB = in1.readInt();
						
						int sizeA = in0.readInt();
						int sizeB = in1.readInt();
						
						if(sizeA != sizeB || offsetA != offsetB){
							return false;
						}
					}
					
					return true;
				}finally{
					in0.close();
					in1.close();
				}
			}else{
				assert(false);
			}
		}
		return false;
	}
	private boolean checkReplacementRec(BlueprintEntry e, File[] listFiles,
			String[] digests, File endFile) {
		File[] rawBlockData = e.getRawBlockData();
		
		for(int i = 0; i < listFiles.length; i++){
			File dbFile = listFiles[i];
			String dig = digests[i];
			boolean found = false;
			for(File bbFile : rawBlockData){
				if(dbFile.getName().substring(dbFile.getName().indexOf('.')).equals(bbFile.getName().substring(bbFile.getName().indexOf('.')))){
					try {
						FileInputStream fis = new FileInputStream(bbFile);
						String digBB = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
						fis.close();
//						System.out.println("COMPARING HEADERS: "+listFiles[i].getName()+" WITH "+bbFile.getName() );
						
						if(compare(listFiles[i], bbFile)){
							System.out.println("FOUND HEADER MATCH! "+(i+1)+"/"+listFiles.length+"; "+listFiles[i].getName()+" WITH "+bbFile.getName());
							found = true;
							break;
						}else{
//							System.out.println("NO MATCH");
						}
						
						
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
			if(!found){
//				System.out.println("MATCH NOT FOUND FOR "+e.getName()+"; "+(e.getChields().size() > 0 ? "CHECKING CHILDREN" : ""));
				for(BlueprintEntry c : e.getChilds()){
					boolean ok = checkReplacementRec(c, listFiles, digests, endFile);
					if(ok){
						return ok;
					}
				}
				return false;
			}
		}
		System.out.println("REPLACING FILES!");
		replaceFiles(rawBlockData, listFiles, endFile);
		//found replacement
		return true;
	}

	private void replaceFiles(File[] rawBlockData, File[] listFiles,
			File endFile) {
		
		String outlineBBFolder;
		String outlineBBUID;
		
		File base = new FileExt("./");
		String absBase = base.getAbsolutePath();
		
		File f = null;
		for(File m : rawBlockData){
			if(m.getAbsolutePath().toLowerCase(Locale.ENGLISH).endsWith(SegmentDataIO16.BLOCK_FILE_EXT)){
				f = m;
				break;
			}
		}
		if(f != null){
			String oFolder = f.getAbsolutePath();
			System.out.println("[BLUEPRINT] BASE PATH: "+absBase);
			String relPath = oFolder.replace(absBase, ".").replaceAll("\\\\", "/");
			System.out.println("[BLUEPRINT] USING TRANSIENT: REL PATH ::: "+relPath);
			outlineBBFolder = relPath.substring(0, relPath.lastIndexOf("/")+1);
			System.out.println("[BLUEPRINT] USING TRANSIENT: BB FODLER ::: "+outlineBBFolder);
			outlineBBUID = relPath.substring(outlineBBFolder.length());
			System.out.println("[BLUEPRINT] USING TRANSIENT: FILE ISOLATED ::: "+outlineBBUID);
			outlineBBUID = outlineBBUID.substring(0, outlineBBUID.indexOf('.'));
			System.out.println("[BLUEPRINT] USING TRANSIENT: EXTRACTED BB UID ::: "+outlineBBUID);
			assert(outlineBBUID.length() > 0);
		}else{
			System.out.println("NO FILES IN BLEUPRINT");
			return;
		}
		
		try {
			Tag readFrom = Tag.readFrom(new BufferedInputStream(new FileInputStream(endFile)), true, false);
			
			Tag[] vals = (Tag[]) readFrom.getValue();
			if(endFile.getName().startsWith("ENTITY_SPACESTATION_")){
				vals = (Tag[]) vals[1].getValue();
			}
			Tag[] n = vals;
			if(vals.length <= 26 || vals[24].getType() != Type.STRUCT){
//				System.err.println("CHECKING TAG: LEN "+vals.length);
				if(vals.length <= 26){
					
					n = new Tag[26];
					int i = 0;
					for(; i < vals.length-1; i++){
						n[i] = vals[i];
					}
					
					while(i < 24){
						n[i] = new Tag(Type.NOTHING, null, null);
						i++;
					}
					n[24] = new Tag(Type.STRUCT, null, new Tag[]{
							new Tag(Type.STRING, null, outlineBBFolder),
							new Tag(Type.STRING, null, outlineBBUID),
							FinishTag.INST});
					
					n[25] = FinishTag.INST;
				}else{
					n[24] = new Tag(Type.STRUCT, null, new Tag[]{
							new Tag(Type.STRING, null, outlineBBFolder),
							new Tag(Type.STRING, null, outlineBBUID),
							FinishTag.INST});
				}
//				System.out.println("CREATED NEW TAG STRUCTURE FOR BLUEPRINT USAGE");
			}
			if(endFile.getName().startsWith("ENTITY_SPACESTATION_")){
				((Tag[]) readFrom.getValue())[1].setValue(n);
			}else{
				readFrom.setValue(n);
			}
			readFrom.writeTo(new BufferedOutputStream(new FileOutputStream(endFile)), true);
			System.out.println("WRITTEN NEW TAG TO "+endFile.getName()+"; adding "+listFiles.length+" to delete set");
			for(int i = 0; i < listFiles.length; i++){
				toDelete.add(listFiles[i]);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
