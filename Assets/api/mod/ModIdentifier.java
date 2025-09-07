package api.mod;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Created by Jake on 3/17/2021.
 * A mod id + version
 */
public class ModIdentifier {
    public int id;
    public String version;

    public ModIdentifier(int id, String version) {
        this.id = id;
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModIdentifier that = (ModIdentifier) o;
        return id == that.id &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    @Override
    public String toString() {
        SMDModInfo modData = SMDModData.getInstance().getModData(id);
        StringBuilder sb = new StringBuilder();
        if(modData != null){
            sb.append(modData.getName());
            sb.append(" ");
        }
        sb.append("[").append(id).append("] ");
        sb.append("{").append(version).append("}");
        return sb.toString();
    }
    public static ModIdentifier deserialize(String str){
        String[] split = str.split(Pattern.quote("~/~"));
        int id = Integer.parseInt(split[0]);
        return new ModIdentifier(id, split[1]);
    }
    public static ModIdentifier fromMod(ModSkeleton skeleton){
        return new ModIdentifier(skeleton.getSmdResourceId(), skeleton.getModVersion());
    }
    public String serialize(){
        return id + "~/~" + version;
    }
    public boolean equalsMod(ModSkeleton skeleton){
        return this.id == skeleton.getSmdResourceId() && this.version.equals(skeleton.getModVersion());
    }
}
