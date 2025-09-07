package api.listener.fastevents;


import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.inventory.StashInventory;

import javax.validation.constraints.NotNull;

public interface StorageItemPullListener {
    void onItemPullChecks(@NotNull ManagerContainer managerContainer, @NotNull LongOpenHashSet longOpenHashSet);
    void onPreItemPull(@NotNull StashInventory puller, @NotNull SegmentPiece segmentPiece, boolean transferOk);
    void onPostItemPull(@NotNull StashInventory puller, @NotNull SegmentPiece segmentPiece, boolean transferOk);
}
