package com.zach.tmc;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;

/**
 * The <code>NewLevelFrame</code> class is used by the {@link Launcher} to
 * create new games with a selected level. 
 * 
 * @author Zach K
 */
public class NewLevelFrame extends JFrame implements ActionListener {

	/** Reference to the launcher that this <code>NewLevelFrame</code> came from. */
	public Launcher launcher;
	
	/** The main panel where everything in the <code>NewLevelFrame</code> GUI is stored. */
	private JPanel contentPane;
	/** Radio button for level 1. */
	private JRadioButton rdbtnLevel_1;
	/** Radio button for level 2. */
	private JRadioButton rdbtnLevel_2;
	/** Radio button for a custom level. */
	private JRadioButton rdbtnCustomLevel;
	/** Creates an instance of Game on a separate thread with the level selected. */
	private JButton btnCreateNewLevel;

	/**	Stores the path to the level selected. */
	public StringBuilder filePath = new StringBuilder("/levels/level-1.xml");
	
	/**
	 * Constructs a new <code>NewLevelFrame</code> with a reference to the launcher
	 * it started from.
	 * 
	 * @param launcher
	 *            The launcher it started from.
	 */
	public NewLevelFrame(Launcher launcher) {
		// Save the reference to the launcher this NewLevelFrame started from.
		this.launcher = launcher;
		
		// Set frame attributes.
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setTitle("New Level Creator");
		setBounds(100, 100, 400, 90);
		
		// Create the content pane.
		contentPane = new JPanel();
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		setContentPane(contentPane);

		// Create a label that asks you to select a level.
		JLabel lblSelectALevel = new JLabel("Select A Level:");
		contentPane.add(lblSelectALevel);
		
		// Create the radio button for level 1.
		rdbtnLevel_1 = new JRadioButton("Level 1");
		rdbtnLevel_1.addActionListener(this);
		rdbtnLevel_1.setSelected(true);
		contentPane.add(rdbtnLevel_1);

		// Create the radio button for level 2.
		rdbtnLevel_2 = new JRadioButton("Level 2");
		rdbtnLevel_2.addActionListener(this);
		contentPane.add(rdbtnLevel_2);

		// Create the radio button for a custom level.
		rdbtnCustomLevel = new JRadioButton("Custom Level");
		rdbtnCustomLevel.addActionListener(this);
		contentPane.add(rdbtnCustomLevel);

		// Create a button group to link the radio buttons together
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(rdbtnLevel_1);
		buttonGroup.add(rdbtnLevel_2);
		buttonGroup.add(rdbtnCustomLevel);

		// Create the button that creates an instance of Game on a separate thread with the level selected.
		btnCreateNewLevel = new JButton("Create New Level");
		btnCreateNewLevel.addActionListener(this);
		contentPane.add(btnCreateNewLevel);
		
		// Display the NewLevelFrame.
		setVisible(true);
	}

	/**
	 * This method (part of the {@link ActionListener} implementation) stores the
	 * selected level into {@link #filePath}.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Get the source of the event.
		Object source = e.getSource();
		// Check the source of the event and act accordingly.
		if (source.equals(rdbtnLevel_1)) {
			filePath.replace(0, filePath.length(), "/levels/level-1.xml");
		} else if (source.equals(rdbtnLevel_2)) {
			filePath.replace(0, filePath.length(), "/levels/level-2.xml");
		} else if (source.equals(rdbtnCustomLevel)) {
			// In the case that the custom level radio button is selected, a dialog will open asking you to select a file.
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new XMLFileFilter());
			fileChooser.setAcceptAllFileFilterUsed(false);
			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				filePath.replace(0, filePath.length(), fileChooser.getSelectedFile().getPath());
			} else {
				// If the dialog is closed, the last clicked button will be reselected.
				if (filePath.charAt(0) == '/') {
					switch(Integer.parseInt(filePath.substring(14, 15))) {
					case 1:
						rdbtnLevel_1.setSelected(true);
						break;
					case 2:
						rdbtnLevel_2.setSelected(true);
						break;
					}
					
				}
			}
		} else if (source.equals(btnCreateNewLevel)) {
			/* 
			 * In the case that the "Create New Level" button was selected, a new instance 
			 * of Game will be created on a separate thread and the NewLevelFrame will be
			 * disposed.
			 */ 
			Game game = new Game(launcher, filePath.toString());
			new Thread(game).start();
			dispose();
		}
	}

}
