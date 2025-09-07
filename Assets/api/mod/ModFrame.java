package api.mod;

import javax.swing.*;

public class ModFrame extends JFrame {
    public ModFrame(){
        setSize(200,100);
        setResizable(false);
        add(new DownloaderComponent());
    }
}
class DownloaderComponent extends JComponent{

}
