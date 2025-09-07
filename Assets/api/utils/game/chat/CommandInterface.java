package api.utils.game.chat;

import api.mod.StarMod;
import org.schema.game.common.data.player.PlayerState;

import javax.annotation.Nullable;

/**
 * CommandInterface
 * <Description>
 *
 * @author TheDerpGamer
 * @since 04/08/2021
 */
public interface CommandInterface {
    String getCommand();
    String[] getAliases();
    String getDescription();
    boolean isAdminOnly();
    boolean onCommand(PlayerState sender, String[] args);
    @Deprecated
    void serverAction(@Nullable PlayerState sender, String[] args);
    StarMod getMod();
}
