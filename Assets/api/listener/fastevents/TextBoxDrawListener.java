package api.listener.fastevents;

import org.schema.game.client.view.SegmentDrawer;
import org.schema.game.client.view.textbox.AbstractTextBox;

/**
 * Created by Jake on 11/20/2020.
 * <insert description here>
 */
public interface TextBoxDrawListener {
    void draw(SegmentDrawer.TextBoxSeg.TextBoxElement cont, AbstractTextBox box);
    void preDrawBackground(SegmentDrawer.TextBoxSeg seg, AbstractTextBox box);

    void preDraw(SegmentDrawer.TextBoxSeg.TextBoxElement cont, AbstractTextBox abstractTextBox);
}
