package api.listener.events.gui;

import api.listener.events.Event;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.font.unicode.UnicodeFont;

public class BigMessagePopupEvent extends Event {
    private  String uid;
    private  String title;
    private  String subtitle;
    private  float popupDelay;
    private  Color color;
    //private  String audioString;
    private UnicodeFont titleFont;
    private UnicodeFont subTitleFont;

    public BigMessagePopupEvent(String uid, String title, String subtitle, float popupDelay, Color color, UnicodeFont titleFont, UnicodeFont subTitleFont) {

        this.uid = uid;
        this.title = title;
        this.subtitle = subtitle;
        this.popupDelay = popupDelay;
        this.color = color;
        //this.audioString = audioString;
        this.titleFont = titleFont;
        this.subTitleFont = subTitleFont;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public float getPopupDelay() {
        return popupDelay;
    }

    public void setPopupDelay(float popupDelay) {
        this.popupDelay = popupDelay;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    //public String getAudioString() {
    //    return audioString;
    //}

    //public void setAudioString(String audioString) {
    //    this.audioString = audioString;
    //}

    public UnicodeFont getTitleFont() {
        return titleFont;
    }

    public void setTitleFont(UnicodeFont titleFont) {
        this.titleFont = titleFont;
    }

    public UnicodeFont getSubTitleFont() {
        return subTitleFont;
    }

    public void setSubTitleFont(UnicodeFont subTitleFont) {
        this.subTitleFont = subTitleFont;
    }
}
