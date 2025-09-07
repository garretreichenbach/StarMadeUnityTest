package api.utils.gui;

import api.common.GameClient;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;

public abstract class SimplePlayerTextInput extends PlayerGameTextInput {
    public SimplePlayerTextInput(String windowName, String desc) {
        super("SIMPLE_TEXT", GameClient.getClientState(),64, windowName, desc, "");
        this.activate();
    }

    @Override
    public void onDeactivate() {

    }

    @Override
    public String[] getCommandPrefixes() {
        return new String[0];
    }

    @Override
    public String handleAutoComplete(String s, TextCallback callback, String s1) throws PrefixNotFoundException {
        return null;
    }

    @Override
    public void onFailedTextCheck(String s) {

    }
}
/*
public void openCreateFleetDialog() {
        PlayerGameTextInput var1;
        (var1 = new PlayerGameTextInput("INFLEET", this.getState(), 64, Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_FLEET_FLEETOPTIONBUTTONS_0, Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_FLEET_FLEETOPTIONBUTTONS_1, Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_FLEET_FLEETOPTIONBUTTONS_13) {
            public void onFailedTextCheck(String var1) {
            }

            public String handleAutoComplete(String var1, TextCallback var2, String var3) throws PrefixNotFoundException {
                return null;
            }

            public String[] getCommandPrefixes() {
                return null;
            }

            public boolean onInput(String var1) {
                if (var1.length() <= 0) {
                    this.getState().getController().popupAlertTextMessage(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_FLEET_FLEETOPTIONBUTTONS_2, 0.0F);
                    return false;
                } else {
                    this.getState().getFleetManager().requestCreateFleet(var1, this.getState().getPlayer().getName());
                    return true;
                }
            }

            public void onDeactivate() {
            }
        }).setInputChecker(new InputChecker() {
            public boolean check(String var1, TextCallback var2) {
                if (EntityRequest.isShipNameValid(var1)) {
                    return true;
                } else {
                    var2.onFailedTextCheck(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_FLEET_FLEETOPTIONBUTTONS_3);
                    return false;
                }
            }
        });
        var1.activate();
    }
 */
