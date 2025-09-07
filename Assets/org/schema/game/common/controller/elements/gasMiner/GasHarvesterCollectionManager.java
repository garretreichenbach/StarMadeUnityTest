package org.schema.game.common.controller.elements.gasMiner;

import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;

public class GasHarvesterCollectionManager extends ControlBlockElementCollectionManager<GasHarvesterUnit, GasHarvesterCollectionManager, GasHarvesterElementManager> implements PlayerUsableInterface {
    public GasHarvesterCollectionManager(SegmentPiece segmentPiece, SegmentController segmentController, GasHarvesterElementManager elementManager) {
        super(segmentPiece, ElementKeyMap.GAS_SCOOP_MODULE, segmentController, elementManager);
    }

    @Override
    protected Class<GasHarvesterUnit> getType() {
        return GasHarvesterUnit.class;
    }

    @Override
    public GasHarvesterUnit getInstance() {
        return new GasHarvesterUnit();
    }

    @Override
    public String getModuleName() {
        return Lng.str("Gas Harvester System");
    }

    @Override
    public void addHudConext(ControllerStateUnit controllerStateUnit, HudContextHelpManager hcm, HudContextHelperContainer.Hos hos) {
        hcm.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Harvest Gaseous Resources"), hos, ContextFilter.IMPORTANT);
    }
}
