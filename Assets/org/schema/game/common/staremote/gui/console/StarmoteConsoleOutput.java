package org.schema.game.common.staremote.gui.console;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.core.ChatListener;
import org.schema.schine.graphicsengine.core.ChatMessageInterface;

public class StarmoteConsoleOutput extends JPanel implements ChatListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private JTextArea editorPane;
	/**
	 * Create the panel.
	 */
	public StarmoteConsoleOutput(GameClientState state) {
		setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		editorPane = new JTextArea();
		DefaultCaret caret = (DefaultCaret) editorPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scrollPane.setViewportView(editorPane);
		scrollPane.setAutoscrolls(true);
		assert (state != null);
		state.getChatListeners().add(this);

	}

	@Override
	public void notifyOfChat(ChatMessageInterface msg) {
		editorPane.append(msg.toString() + "\n");
	}

}
