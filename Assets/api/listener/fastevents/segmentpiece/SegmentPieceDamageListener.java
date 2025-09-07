package api.listener.fastevents.segmentpiece;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;

public interface SegmentPieceDamageListener {

    int onBlockDamage(
            SegmentController controller,
            long pos,
            short type,
            int damage,
            DamageDealerType damageType,
            Damager from,
            boolean isServer
    );
}
