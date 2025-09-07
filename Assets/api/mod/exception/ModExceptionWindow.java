package api.mod.exception;

import api.mod.ModSkeleton;
import api.mod.ModStarter;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

/**
 * Created by Jake on 2/16/2021.
 * <insert description here>
 */
public class ModExceptionWindow extends JPanel {
    private static boolean pressedOption = false;
    private static boolean option = false;
    public static boolean display(ModSkeleton mod, Throwable e){
        try {
            pressedOption = false;
            e.printStackTrace();
            JFrame frame = new JFrame("Mod Exception");
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setResizable(true);
            frame.setLayout(null);
            double height = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
            frame.setSize(900, (int) Math.min(800, height-40));
            frame.setVisible(true);
            ModExceptionWindow contentPane = new ModExceptionWindow(mod, e);
            frame.setContentPane(contentPane);
            // Update pane
            frame.setSize(901, (int) Math.min(800, height-40));
            int timesRan = 0;
            while (true) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                timesRan++;
                if (!frame.isVisible()) {
                    System.err.println("[ModExceptionWindow] Not visible, returning: " + timesRan);
                    return false;
                }
                if (pressedOption) {
                    System.err.println("[ModExceptionWindow] Exiting window");
                    frame.setVisible(false);
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                    return option;
                }
            }
        }catch (HeadlessException ex){
            ex.printStackTrace();
            System.exit(-1);
            return false;
        }
    }

    public ModExceptionWindow(ModSkeleton mod, Throwable e) {
        setLayout(null);
        JTextArea text = makeText(new Color(168, 168, 168), 10,  28,"StarLoader encountered an error when trying to enable mods.");
        text.setFont(text.getFont().deriveFont(18F));

        JTextArea modText = makeText(new Color(203, 203, 203), 50,  28,"Mod: " + mod.getDebugName());
        modText.setFont(modText.getFont().deriveFont(18F));

        JTextArea field = new JTextArea();
        field.setBounds(20, 90,900-60, 600);
        String stacktrace = ExceptionUtils.getStackTrace(e);
        field.setText("Load Stage: " + ModStarter.getCurrentLoadStage() + "\n\n" + stacktrace);
        add(field);

        JButton button = new JButton("Continue Loading");
        button.setBounds(20, 700, 200, 50);

        JButton button2 = new JButton("Exit Game");
        button2.setBounds(240, 700, 200, 50);

        button.addActionListener(new ClickListener(true));
        button2.addActionListener(new ClickListener(false));

        add(button);
        add(button2);

    }
    public JTextArea makeText(Color c, int y, int height, String text) {
        JTextArea area = new JTextArea("    " + text);
        area.setBounds(0, y, 1500, height);
        area.setBackground(c);
        area.setEditable(false);
        add(area);
        return area;
    }
    private static class ClickListener implements ActionListener{
        boolean type;

        public ClickListener(boolean type) {
            this.type = type;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ModExceptionWindow.pressedOption = true;
            ModExceptionWindow.option = type;
        }
    }
}
