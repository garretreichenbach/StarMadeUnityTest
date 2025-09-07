package api.utils.game.module;

import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;

import java.io.*;
import java.util.Collection;

/**
 * Created by Jake on 12/10/2020.
 *
 * Organizational class for SegmentController tag deserialize/serialize
 */
public class ModTagUtils {
    public static Tag onTagSerialize(Collection<ModManagerContainerModule> modules) {
        System.err.println("ON TAG SERIALIZE");

        Tag[] struct = new Tag[modules.size() + 1];
        struct[modules.size()] = FinishTag.INST;
        int i = 0;
        for (ModManagerContainerModule module : modules) {
            System.err.println("Module: " + module);
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            PacketWriteBuffer buffer = new PacketWriteBuffer(new DataOutputStream(byteArray));
            try {
                module.onTagSerialize(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] bytes = byteArray.toByteArray();
            System.err.println("DATA LENGTH: " + bytes.length);
            struct[i] = new Tag(Tag.Type.BYTE_ARRAY, module.getTagName(), bytes);
            i++;
        }

        return new Tag(Tag.Type.STRUCT, "ModMCModules", struct);
    }

    public static void onTagDeserialize(ManagerContainer<?> container, Tag tag) {
        assert tag.getType() == Tag.Type.STRUCT;
        System.err.println("DESER: " + tag.getType());

        Tag[] struct = tag.getStruct();
        for (Tag subTag : struct) {
            if(subTag.getType() == Tag.Type.FINISH){
                continue;
            }
            System.err.println("type: " + subTag.getType());
            String subTagName = subTag.getName();
            String modName = subTagName.split("~")[0];
            String moduleName = subTagName.split("~")[1];

            boolean completed = false;
            for (ModManagerContainerModule value : container.getModModuleMap().values()) {
                if (value.getName().equals(moduleName) && value.getMod().getName().equals(modName)) {
                    System.err.println("DATA LENGTH DESER: " + subTag.getByteArray().length);
                    PacketReadBuffer buffer = new PacketReadBuffer(new DataInputStream(new ByteArrayInputStream(subTag.getByteArray())));
                    try {
                        value.onTagDeserialize(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    completed = true;
                    break;
                }
            }

            if (!completed) {
                System.err.println("Warning: Tried to deserialize mod tag: " + subTagName + ", but no ModMCModule matched.");
            }
        }
    }

    //Binary data utils, often easier to use than making a tag structure
    public static PacketWriteBuffer getNewReadBuffer(ByteArrayOutputStream byteStream) {
        return new PacketWriteBuffer(new DataOutputStream(byteStream));
    }

    public static Tag getTagFromData(ByteArrayOutputStream data) {
        return new Tag(Tag.Type.BYTE_ARRAY, "bytedata", data.toByteArray());
    }

    public static PacketReadBuffer getDataFromTag(Tag tag) {
        assert tag.getType() == Tag.Type.BYTE_ARRAY : "Tag was not a byte array. name=" + tag.getName();
        ByteArrayInputStream input = new ByteArrayInputStream(tag.getByteArray());
        return new PacketReadBuffer(new DataInputStream(input));
    }
}
