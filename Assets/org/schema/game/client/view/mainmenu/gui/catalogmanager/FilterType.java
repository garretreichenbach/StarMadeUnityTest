package org.schema.game.client.view.mainmenu.gui.catalogmanager;

public enum FilterType {
    ALL("All", 0),
    SHIPS_ALL("Ships", 2),
    SHIPS_RP("RP Ships", 11),
    SHIPS_PVP("PvP Ships", 12),
    SHIPS_SHELLS("Ship Shells", 15),
    STATIONS_ALL("Stations", 3),
    STATIONS_RP("RP Stations", 13),
    STATIONS_PVP("PvP Stations", 14),
    STATIONS_SHELLS("Station Shells", 16),
    LOGIC("Logic", 7),
    TURRETS("Turrets", 8),
    TEMPLATES("Templates", 9);

    String displayString;
    int num;

    FilterType(String displayString, int num) {
        this.displayString = displayString;
        this.num = num;
    }

    public static int getContentType(int cat) {
        if(cat == 2 || cat == 11 || cat == 12 || cat == 15 || cat == 3 || cat == 13 || cat == 14 || cat == 16 || cat == 7 || cat == 8) {
            return 1;
        } else if(cat == 9) {
            return 2;
        }
        return 0;
    }
}
