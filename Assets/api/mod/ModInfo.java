package api.mod;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.schema.common.SerializationInterface;

public class ModInfo implements SerializationInterface{

    public String name;
    public String version;
    public String downloadURL;
    public ModInfo(String name, String version){

        this.name = name;
        this.version = version;
    }
    
    public ModInfo(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException{
    	deserialize(b, updateSenderStateId, isOnServer);
    }
    public void fetchDownloadURL(){
        MainModManifest manifest = MainModManifest.getManifest();
        this.downloadURL = manifest.getURL(this);
    }

    public static ModInfo fromString(String s){
        String[] l = s.split(",,,");
        return new ModInfo(l[0], l[1]);
    }
    public String serialize(){
        return name + ",,," + version;
    }

    @Override
    public String toString() {
        return "ModInfo{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", downloadURL='" + downloadURL + '\'' +
                '}';
    }
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeUTF(name);
		b.writeUTF(version);
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		name = b.readUTF();
		version = b.readUTF();
	}
}
