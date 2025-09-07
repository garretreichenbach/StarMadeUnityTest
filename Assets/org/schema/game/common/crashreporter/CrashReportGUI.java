package org.schema.game.common.crashreporter;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.schema.game.common.util.GuiErrorHandler;

public class CrashReportGUI extends JFrame implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private JPanel contentPane;
	private JTextField textField;
	private JProgressBar progressBar;

	/**
	 * Create the frame.
	 */
	public CrashReportGUI() {
		setTitle("Report Bug or Crash");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 626, 413);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Basic Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 2;
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		contentPane.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{198, 112, 86, 0};
		gbl_panel_1.rowHeights = new int[]{20, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		JLabel lblEmail = new JLabel("Email (required)");
		GridBagConstraints gbc_lblEmail = new GridBagConstraints();
		gbc_lblEmail.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblEmail.insets = new Insets(0, 0, 0, 5);
		gbc_lblEmail.gridx = 0;
		gbc_lblEmail.gridy = 0;
		panel_1.add(lblEmail, gbc_lblEmail);
		lblEmail.setFont(new Font("Arial", Font.PLAIN, 16));

		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 2;
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.BOTH;
		gbc_textField.anchor = GridBagConstraints.NORTHWEST;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panel_1.add(textField, gbc_textField);
		textField.setPreferredSize(new Dimension(300, 20));
		textField.setColumns(10);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Bug/Crash description", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setPreferredSize(new Dimension(300, 300));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.weighty = 100.0;
		gbc_panel.gridheight = 8;
		gbc_panel.gridwidth = 2;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		contentPane.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{93, 0};
		gbl_panel.rowHeights = new int[]{19, 19, 19, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblWhereDidThe = new JLabel("Where did the game crash?");
		GridBagConstraints gbc_lblWhereDidThe = new GridBagConstraints();
		gbc_lblWhereDidThe.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblWhereDidThe.insets = new Insets(0, 0, 5, 0);
		gbc_lblWhereDidThe.gridx = 0;
		gbc_lblWhereDidThe.gridy = 0;
		panel.add(lblWhereDidThe, gbc_lblWhereDidThe);
		lblWhereDidThe.setFont(new Font("Arial", Font.PLAIN, 16));

		JLabel lblWhatWereYou = new JLabel("What were you doing in the game when the Bug occurred?");
		GridBagConstraints gbc_lblWhatWereYou = new GridBagConstraints();
		gbc_lblWhatWereYou.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblWhatWereYou.insets = new Insets(0, 0, 5, 0);
		gbc_lblWhatWereYou.gridx = 0;
		gbc_lblWhatWereYou.gridy = 1;
		panel.add(lblWhatWereYou, gbc_lblWhatWereYou);
		lblWhatWereYou.setFont(new Font("Arial", Font.PLAIN, 16));

		JLabel lblPleaseDescribeThe = new JLabel("Please describe the Problem:");
		GridBagConstraints gbc_lblPleaseDescribeThe = new GridBagConstraints();
		gbc_lblPleaseDescribeThe.insets = new Insets(0, 0, 5, 0);
		gbc_lblPleaseDescribeThe.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblPleaseDescribeThe.gridx = 0;
		gbc_lblPleaseDescribeThe.gridy = 2;
		panel.add(lblPleaseDescribeThe, gbc_lblPleaseDescribeThe);
		lblPleaseDescribeThe.setFont(new Font("Arial", Font.PLAIN, 16));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 3;
		panel.add(scrollPane, gbc_scrollPane);

		final JTextArea textArea = new JTextArea();
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		scrollPane.setViewportView(textArea);

		JButton btnSend = new JButton("Send Report and Logs");
		btnSend.addActionListener(arg0 -> {
			CrashReporter c = new CrashReporter();
			c.addObserver(CrashReportGUI.this);
			try {
				if (textArea.getText().length() > 10000) {
					throw new IllegalArgumentException("The description is to long! \n\nIf this is an attempt to spam me:  :(");
				}
				if (textField.getText().length() > 300) {
					throw new IllegalArgumentException("The Email is to long! \n\nIf this is an attempt to spam me:  :(");
				}
				c.fillAutomaticInformation(textField.getText(), textArea.getText());
				c.startCreashReport();
			} catch (Exception e) {
				GuiErrorHandler.processNormalErrorDialogException(e, false);
			}
		});

		JTextPane txtpnAllInformationYou = new JTextPane();
		txtpnAllInformationYou.setFont(new Font("Arial", Font.PLAIN, 10));
		txtpnAllInformationYou.setEditable(false);
		txtpnAllInformationYou.setText("All information you send will only be used to fix bugs and crashes in StarMade, and that purpose only. The information won't be saved permanently and will never be given to a third party.");
		GridBagConstraints gbc_txtpnAllInformationYou = new GridBagConstraints();
		gbc_txtpnAllInformationYou.gridwidth = 2;
		gbc_txtpnAllInformationYou.anchor = GridBagConstraints.SOUTH;
		gbc_txtpnAllInformationYou.insets = new Insets(0, 0, 5, 0);
		gbc_txtpnAllInformationYou.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtpnAllInformationYou.gridx = 0;
		gbc_txtpnAllInformationYou.gridy = 9;
		contentPane.add(txtpnAllInformationYou, gbc_txtpnAllInformationYou);
		GridBagConstraints gbc_btnSend = new GridBagConstraints();
		gbc_btnSend.anchor = GridBagConstraints.WEST;
		gbc_btnSend.insets = new Insets(0, 0, 0, 5);
		gbc_btnSend.gridx = 0;
		gbc_btnSend.gridy = 10;
		contentPane.add(btnSend, gbc_btnSend);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.weightx = 100.0;
		gbc_progressBar.fill = GridBagConstraints.BOTH;
		gbc_progressBar.gridx = 1;
		gbc_progressBar.gridy = 10;
		contentPane.add(progressBar, gbc_progressBar);
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		if (args.length > 0 && args[0].equals("-nogui")) {
			if (args.length < 3) {
				System.out.println("You need at least 2 more arguments:\n" +
						"Example:\n" +
						"java -jar CrashAndBugReport.jar -nogui myemail@mymaildom.com description of the bug i had");
				try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
			} else {
				StringBuffer desc = new StringBuffer();
				for (int i = 2; i < args.length; i++) {
					desc.append(args[i] + " ");
				}
				CrashReporter c = new CrashReporter();
				try {
					c.fillAutomaticInformation(args[1], desc.toString());
					c.startCreashReport();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			EventQueue.invokeLater(() -> {
				try {
					CrashReportGUI frame = new CrashReportGUI();
					frame.setLocation(200, 200);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg != null) {
			if (arg instanceof Integer) {
				progressBar.setValue((Integer) arg);
			} else {
				if (arg.toString().equals("FINISHED")) {
					GuiErrorHandler.exitInfoDialog("Thank You For Sending the Report!\n\n" +
							"I (schema) will be automatically notified about this Report\n" +
							"and I will try to fix your issue as soon as I can!\n\n" +
							"Thanks for playing StarMade!\n" +
							"");
				}
				progressBar.setString(arg.toString());
			}
		}

	}

}
