package org.schema.game.client.view.creaturetool.swing;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.creaturetool.CreatureTool;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CreatureToolFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private JPanel contentPane;
	private CreatureTool creatureTool;
	/**
	 * Create the frame.
	 */
	public CreatureToolFrame(GameClientState state, CreatureTool creatureTool) {
//		setBounds(GLFrame.getWidth() + 100, 50, 450, 700);
		setBounds(100, 50, 450, 700);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		CreatureToolMainPanel creatureToolMainPanel = new CreatureToolMainPanel(state, creatureTool);
		contentPane.add(creatureToolMainPanel, BorderLayout.CENTER);
		this.creatureTool = creatureTool;
	}

	/* (non-Javadoc)
	 * @see java.awt.Window#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		creatureTool.onGUIDispose();
	}

}
