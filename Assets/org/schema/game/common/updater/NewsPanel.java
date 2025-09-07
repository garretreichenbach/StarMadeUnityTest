package org.schema.game.common.updater;

import javax.swing.*;
import java.awt.*;

public class NewsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final HtmlDisplayer htmlDisplay;

	/**
	 * Create the panel.
	 */
	public NewsPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {450, 0};
		gridBagLayout.rowHeights = new int[] {10, 290, 0, 0};
		gridBagLayout.columnWeights = new double[] {0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		setSize(600, 300);
		setMinimumSize(new Dimension(600, 300));
		setPreferredSize(new Dimension(600, 300));

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.SOUTHEAST;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		add(panel, gbc_panel);
		panel.setSize(600, 300);

		JButton btnRefreshNews = new JButton("Refresh News");

		btnRefreshNews.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(btnRefreshNews);

		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 2;
		gbc_scrollPane.weighty = 1.0;
		gbc_scrollPane.weightx = 1.0;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;

		htmlDisplay = new HtmlDisplayer();
		add(htmlDisplay, gbc_scrollPane);
		refreshNews();
		btnRefreshNews.addActionListener(e -> refreshNews());
	}

	public void doUpdate() {
		/*
		try {
			int max = 3;
			ArrayList<LauncherNewsPost> news = NewsRetriever.getNews(max);
			for(LauncherNewsPost s : news) {
				try {
					URL url = new URL(s.getUrl());
					htmlDisplay.setPage(url);
					//Todo: Either design a custom news post template so it formats correctly, or get SMD news working again
				} catch(Exception exception) {
					exception.printStackTrace();
					htmlDisplay.setText("Error: " + exception.getClass() + ": " + exception.getMessage());
				}
				invalidate();
				validate();
				repaint();
			}
		} catch(IOException | DocumentException | NotLoggedInException exception) {
			exception.printStackTrace();
		}

		 */
	}

	private void refreshNews() {
		new Thread(() -> SwingUtilities.invokeLater(this::doUpdate)).start();
	}
}