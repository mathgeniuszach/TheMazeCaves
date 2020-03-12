package com.zach.tmc;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.swing.SwingConstants;

/**
 * The <code>Launcher</code> class the main class that launches new 
 * {@link Game}s of The Maze Caves.
 * 
 * @author Zach K
 */
public class Launcher extends JFrame {
	/** This is the current version of The Maze Caves. */
	public static final String VERSION = "1.0.10";
	
	/** This <code>Launcher</code>. Used to pass this <code>Launcher</code> to listeners. */
	private Launcher launcher = this;
	/** The main panel where everything in the <code>Launcher</code> GUI is stored. */
	private JPanel contentPane;

	/** Stores the save game that is selected. */
	private JTextField textFieldFilePath;

	/** Stores the user's control for moving left. */
	private JButton btnKeyLeft;
	/** Stores the user's control for moving up. */
	private JButton btnKeyUp;
	/** Stores the user's control for moving right. */
	private JButton btnKeyRight;
	/** Stores the user's control for moving down. */
	private JButton btnKeyDown;
	/** Stores the user's control for interacting with objects. */
	private JButton btnKeyAction;

	/** Stores the user's custom piece in the game. */
	private JButton btnPlayerPiece;

	/** Controls the length of the "New Game" and "Load Game" buttons. */
	private JSplitPane splitPane;

	/** Stores the user's setting for the display size of the game. */
	private JSlider sliderDisplaySize;
	/** Shows the user's setting for the display size of the game. */
	private JLabel lblDisplaySizeNumber;
	
	// These are static objects. Some of these are created when the settings are loaded.
			/** The settings.xml file in {@link Document} form. */
			public static Document settingsFile;
			/** Standard {@link XPath}. */
			public static XPath xPath;
			/** Standard {@link Transformer}. */
			public static Transformer transformer;
			
			/** The main path of the application data in The Maze Caves. */
			public static final String MAIN_PATH = System.getenv("APPDATA") + "\\TheMazeCaves";
			/** The path for the settings.xml file in The Maze Caves. */
			public static final String SETTINGS_PATH = MAIN_PATH + "\\settings.xml";

	/**
	 * Creates a <code>Launcher</code>. Each <code>Launcher</code> is a separate
	 * program with a different frame. It is also important to note that the
	 * <code>Launcher</code> will have the look and feel of the system it is running
	 * on unless otherwise specified.
	 * 
	 * @param args
	 *            Special arguments to run when the program starts.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// Get the arguments as a list.
					List<String> arguments = Arrays.asList(args);
					
					// Change the look and feel of the Launcher if there is an argument to.
					if (arguments.contains("SystemLookAndFeel")) {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					}
					
					// Create the Launcher.
					Launcher frame = new Launcher();
					
					// Display the Launcher.
					frame.setVisible(true);
				} catch (Exception e) {
					// Any errors that make it this far back to the main method have their stack trace printed.
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Constructor for creating a <code>Launcher</code>.
	 */
	public Launcher() {
		// Set frame attributes.
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setTitle("TMC Launcher");
		setBounds(100, 100, 400, 325);

		// Create the content pane.
		contentPane = new JPanel();
		contentPane.setLayout(null);
		setContentPane(contentPane);

		// Create a label that displays the name, version, and author of the program.
		JLabel lblTheMazeCaves = new JLabel("The Maze Caves - " + VERSION + " - Zachary Kerner");
		lblTheMazeCaves.setBounds(0, 4, 384, 14);
		lblTheMazeCaves.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblTheMazeCaves);

		// Create the text field for holding the save Game selected.
		textFieldFilePath = new JTextField();
		textFieldFilePath.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				setSetting("file_path", textFieldFilePath.getText());
			}
		});
		textFieldFilePath.setBounds(10, 25, 255, 22);
		textFieldFilePath.setColumns(100);
		contentPane.add(textFieldFilePath);

		// Create a button for selecting a saved Game.
		JButton btnSelect = new JButton("Select");
		btnSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new SaveFileFilter());
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.setCurrentDirectory(new File(textFieldFilePath.getText()).getParentFile());
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					String filePath = fileChooser.getSelectedFile().getPath();
					textFieldFilePath.setText(filePath);
					setSetting("file_path", filePath);
				}
			}
		});
		btnSelect.setBounds(272, 25, 98, 23);
		btnSelect.setToolTipText("Selects a saved game to play (all saved games are in \".save\" format).");
		contentPane.add(btnSelect);

		// Create the split pane that controls the length of two buttons.
		splitPane = new JSplitPane();
		splitPane.setBounds(10, 53, 360, 25);
		splitPane.setDividerLocation(180);
		splitPane.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName() == "dividerLocation") {
					setSetting("divider_location", String.valueOf(splitPane.getDividerLocation()));
				}
			}
		});
		contentPane.add(splitPane);

		// Create the "New Game" button. It opens up a new Game.
		JButton btnNew = new JButton("New Game");
		btnNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NewLevelFrame newLevelCreator = new NewLevelFrame(Launcher.this);
				newLevelCreator.setVisible(true);
			}
		});
		btnNew.setToolTipText("Opens up a new game.");
		splitPane.setLeftComponent(btnNew);

		// Create the "Load Game" button. It opens up the saved Game selected.
		JButton btnLoad = new JButton("Load Game");
		btnLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// Read the save file.
					BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFieldFilePath.getText()), "UTF8"));
					byte key = (byte) reader.read();
					
					ArrayList<Character> characterList = new ArrayList<Character>();
					while (reader.ready()) {
						characterList.add((char) reader.read());
					}
					char[] dataArray = new char[characterList.size()];
					
					// Decode the save file.
					for (int i = 0; i < dataArray.length; i++) {
						dataArray[i] = (char) (characterList.get(i) / 10 - key);
					}
					
					reader.close();
					
					String[] data = String.valueOf(dataArray).split("\n");
					
					// Create a game with the save file.
					
					Game game = new Game(launcher, data);
					new Thread(game).start();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Could not load that save file.");
				}
			}
		});
		btnLoad.setToolTipText("Opens up the saved game selected.");
		splitPane.setRightComponent(btnLoad);

		// Create a line to the left of the "Settings" label.
		JSeparator separatorLeft = new JSeparator();
		separatorLeft.setBounds(10, 88, 147, 2);
		contentPane.add(separatorLeft);

		// Create the "Settings" label.
		JLabel lblSettings = new JLabel("Settings");
		lblSettings.setHorizontalAlignment(Label.LEFT);
		lblSettings.setBounds(164, 82, 53, 14);
		contentPane.add(lblSettings);

		// Create a line to the right of the "Settings" label.
		JSeparator separatorRight = new JSeparator();
		separatorRight.setBounds(223, 89, 147, 2);
		contentPane.add(separatorRight);

		// Create a label to show the Controls.
		JLabel lblControls = new JLabel("Controls");
		lblControls.setBounds(10, 97, 53, 14);
		contentPane.add(lblControls);

		// Create a condensed KeyAdapter for changing controls.
		KeyAdapter keyListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				Component component = e.getComponent();
				int code = e.getKeyCode();
				if (code != KeyEvent.VK_ESCAPE) {
					if (component.equals(btnKeyLeft)) {
						btnKeyLeft.setText(KeyEvent.getKeyText(code).toUpperCase());
						setSetting("key_left", String.valueOf(code));
					} else if (component.equals(btnKeyUp)) {
						btnKeyUp.setText(KeyEvent.getKeyText(code).toUpperCase());
						setSetting("key_up", String.valueOf(code));
					} else if (component.equals(btnKeyRight)) {
						btnKeyRight.setText(KeyEvent.getKeyText(code).toUpperCase());
						setSetting("key_right", String.valueOf(code));
					} else if (component.equals(btnKeyDown)) {
						btnKeyDown.setText(KeyEvent.getKeyText(code).toUpperCase());
						setSetting("key_down", String.valueOf(code));
					} else if (component.equals(btnKeyAction)) {
						btnKeyAction.setText(KeyEvent.getKeyText(code).toUpperCase());
						setSetting("key_action", String.valueOf(code));
					}
				}
			}
		};

		// Create a label to refer to the user's control for moving left.
		Label lblKeyLeft = new Label("Left:");
		lblKeyLeft.setAlignment(Label.RIGHT);
		lblKeyLeft.setBounds(10, 117, 36, 22);
		contentPane.add(lblKeyLeft);

		// Create the button that stores the user's control for moving left.
		btnKeyLeft = new JButton();
		btnKeyLeft.addKeyListener(keyListener);
		btnKeyLeft.setBounds(52, 119, 135, 20);
		contentPane.add(btnKeyLeft);

		// Create a label to refer to the user's control for moving up.
		Label lblKeyUp = new Label("Up:");
		lblKeyUp.setAlignment(Label.RIGHT);
		lblKeyUp.setBounds(10, 145, 36, 22);
		contentPane.add(lblKeyUp);

		// Create the button that stores the user's control for moving up.
		btnKeyUp = new JButton();
		btnKeyUp.addKeyListener(keyListener);
		btnKeyUp.setBounds(52, 147, 135, 20);
		contentPane.add(btnKeyUp);

		// Create a label to refer to the user's control for moving right.
		Label lblKeyRight = new Label("Right:");
		lblKeyRight.setAlignment(Label.RIGHT);
		lblKeyRight.setBounds(10, 173, 36, 22);
		contentPane.add(lblKeyRight);

		// Create the button that stores the user's control for moving right.
		btnKeyRight = new JButton();
		btnKeyRight.addKeyListener(keyListener);
		btnKeyRight.setBounds(52, 175, 135, 20);
		contentPane.add(btnKeyRight);

		// Create a label to refer to the user's control for moving down.
		Label lblKeyDown = new Label("Down:");
		lblKeyDown.setAlignment(Label.RIGHT);
		lblKeyDown.setBounds(10, 201, 36, 22);
		contentPane.add(lblKeyDown);

		// Create the button that stores the user's control for moving down.
		btnKeyDown = new JButton();
		btnKeyDown.addKeyListener(keyListener);
		btnKeyDown.setBounds(52, 203, 135, 20);
		contentPane.add(btnKeyDown);

		// Create a label to refer to the user's control for interacting with objects.
		Label lblKeyAction = new Label("Action:");
		lblKeyAction.setAlignment(Label.RIGHT);
		lblKeyAction.setBounds(0, 229, 46, 22);
		contentPane.add(lblKeyAction);

		// Create the button that stores the user's control for interacting with objects.
		btnKeyAction = new JButton();
		btnKeyAction.addKeyListener(keyListener);
		btnKeyAction.setBounds(52, 231, 135, 20);
		contentPane.add(btnKeyAction);

		// Create a label to refer to the user's custom piece in the Game.
		JLabel lblPlayerPiece = new JLabel("Player Piece");
		lblPlayerPiece.setBounds(212, 97, 73, 14);
		contentPane.add(lblPlayerPiece);

		// Create the button that stores the user's custom piece in the Game.
		btnPlayerPiece = new JButton();
		btnPlayerPiece.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (c < 128 && c != '\n') {
					btnPlayerPiece.setText(String.valueOf(c));
					setSetting("player", String.valueOf(c));
				}
			}
		});
		btnPlayerPiece.setBounds(212, 119, 67, 20);
		contentPane.add(btnPlayerPiece);

		// Create a label to refer to the user's setting for the display size of the Game.
		JLabel lblDisplaySize = new JLabel("Display Size");
		lblDisplaySize.setBounds(306, 97, 73, 14);
		contentPane.add(lblDisplaySize);

		// Create the label that shows the user's setting for the display size of the Game.
		lblDisplaySizeNumber = new JLabel("32");
		lblDisplaySizeNumber.setBounds(356, 181, 21, 14);
		contentPane.add(lblDisplaySizeNumber);

		// Create the slider that stores the user's setting for the display size of the Game.
		sliderDisplaySize = new JSlider();
		sliderDisplaySize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setDisplaySizeNumberLabel();
			}
		});
		sliderDisplaySize.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				setSetting("font_size", String.valueOf(sliderDisplaySize.getValue()));
			}
		});
		sliderDisplaySize.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				setSetting("font_size", String.valueOf(sliderDisplaySize.getValue()));
			}
		});
		sliderDisplaySize.setOrientation(JSlider.VERTICAL);
		sliderDisplaySize.setValue(32);
		sliderDisplaySize.setMinimum(3);
		sliderDisplaySize.setMaximum(72);
		sliderDisplaySize.setBounds(331, 114, 23, 132);
		contentPane.add(sliderDisplaySize);

		// Create a "Show" button. It shows a preview of a Game with the size selected.
		JButton btnShowDisplay = new JButton("Show");
		btnShowDisplay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PreviewFrame(sliderDisplaySize.getValue());
			}
		});
		btnShowDisplay.setBounds(306, 247, 69, 23);
		btnShowDisplay.setToolTipText("Shows a preview of a game with the size selected.");
		contentPane.add(btnShowDisplay);
		
		// Create a "Help" button that shows information about the controls.
		JButton btnHelp = new JButton("Help");
		btnHelp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Here is some information about each of the settings:\n\n"
						+ "Controls: These are the controls for moving around and interacting with objects in the\n"
						+ "    game. To change one of these controls, click the button and press a key on your\n"
						+ "    keyboard.\n"
						+ "Piece: This is the character that displays on the screen when you play the game.\n"
						+ "    To change this, click the button and type the letter you want to use.\n"
						+ "Display Size: This is the size of the display of the game. To change this, click and\n"
						+ "    drag the slider up and down. To preview the size of the display, click the \"Show\"\n"
						+ "    button.\n\n"
						+ "(If your looking for information about how to play the game, select \"New Game\" and play \"Level 1\".)");
			}
		});
		btnHelp.setBounds(10, 262, 89, 23);
		contentPane.add(btnHelp);
		
		// Load the user's settings.
		loadSettings();
	}

	/**
	 * Sets lblDisplaySizeNumber's position and text values based on the value of
	 * the display size slider.
	 */
	public void setDisplaySizeNumberLabel() {
		int value = sliderDisplaySize.getValue();
		lblDisplaySizeNumber.setBounds(356, (int) ((1 - ((double) value - 3) / 69.0) * 117 + 114), 21, 14);
		lblDisplaySizeNumber.setText(String.valueOf(value));
	}

	/**
	 * Sets the value of a setting in the settings.xml file.
	 * 
	 * @param name
	 *            The name of the setting
	 * @param value
	 *            The value to set
	 */
	public void setSetting(String name, String value) {
		try {
			// Get the node with the name specified.
			Node setting = (Node) xPath.evaluate("/settings/" + name, settingsFile, XPathConstants.NODE);
			// Set that node's text content to value.
			setting.setTextContent(value);

			// Write the changed settingsFile Document to the settings.xml file.
			DOMSource source = new DOMSource(settingsFile);
			StreamResult result = new StreamResult(new FileWriter(new File(SETTINGS_PATH)));
			transformer.transform(source, result);
		} catch (Exception e) {
			// In the case of an error, rewrite the settings.xml file.
			int input = JOptionPane.showConfirmDialog(null, "The settings.xml file could not be read.\nWould you like to recreate it?\n(Clicking no or cancel will close the launcher)");
			if (input != JOptionPane.YES_OPTION) {
				// If the user does not want to rewrite the settings.xml file, close the program.
				System.exit(0);
			} else {
				makeSettingsFile();
			}
		}
	}

	/**
	 * Loads the settings.xml file and creates the {@link #xPath} and
	 * {@link #transformer} objects.
	 */
	public void loadSettings() {
		try {
			// Create the XPath and Transformer.
			xPath = XPathFactory.newInstance().newXPath();
			transformer = TransformerFactory.newInstance().newTransformer();
			
			// Load the settingsFile Document from the settings.xml file.
			settingsFile = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(SETTINGS_PATH);

			// Collect settings from settingsFile.
			String filePath = xPath.evaluate("/settings/file_path", settingsFile);
			int dividerLocation = Integer.parseInt(xPath.evaluate("/settings/divider_location", settingsFile));
			int leftKeyCode = Integer.parseInt(xPath.evaluate("/settings/key_left", settingsFile));
			int upKeyCode = Integer.parseInt(xPath.evaluate("/settings/key_up", settingsFile));
			int rightKeyCode = Integer.parseInt(xPath.evaluate("/settings/key_right", settingsFile));
			int downKeyCode = Integer.parseInt(xPath.evaluate("/settings/key_down", settingsFile));
			int actionKeyCode = Integer.parseInt(xPath.evaluate("/settings/key_action", settingsFile));
			char playerPiece = xPath.evaluate("/settings/player", settingsFile).charAt(0);
			if (playerPiece == '\n') {
				playerPiece = ' ';
			}
			int displaySize = Integer.parseInt(xPath.evaluate("/settings/font_size", settingsFile));

			// Display the settings collected.
			textFieldFilePath.setText(filePath);
			splitPane.setDividerLocation(dividerLocation);
			btnKeyLeft.setText(KeyEvent.getKeyText(leftKeyCode).toUpperCase());
			btnKeyUp.setText(KeyEvent.getKeyText(upKeyCode).toUpperCase());
			btnKeyRight.setText(KeyEvent.getKeyText(rightKeyCode).toUpperCase());
			btnKeyDown.setText(KeyEvent.getKeyText(downKeyCode).toUpperCase());
			btnKeyAction.setText(KeyEvent.getKeyText(actionKeyCode).toUpperCase());
			btnPlayerPiece.setText(String.valueOf(playerPiece));
			sliderDisplaySize.setValue(displaySize);
		} catch (TransformerConfigurationException e) {
			// In the case that the Transformer cannot be created, close the program. 
			JOptionPane.showMessageDialog(null, "Could not create the Transformer.\nThe program will now exit.");
			System.exit(3);
		} catch (Exception e) {
			// In the case of any other error, rewrite the settings.xml file.
			File directory = new File(MAIN_PATH);
			if (directory.isDirectory()) {
				int input = JOptionPane.showConfirmDialog(null, "The settings.xml file could not be read.\nWould you like to recreate it?\n(Clicking no or cancel will close the launcher)");
				if (input != 0) {
					// If the user does not want to rewrite the settings.xml file, close the program.
					System.exit(0);
				}
			}
			makeSettingsFile();
		}
	}

	/**
	 * Makes the settings.xml file. These are the default settings:<br>
	 * <br>
	 * <table border="1" cellpadding="2">
	 *   <caption>Default Settings</caption>
	 *   <tr>
	 *     <td><b>file_path</b></td>
	 *     <td></td>
	 *   </tr>
	 *   <tr>
	 *     <td><b>divider_location</b></td>
	 *     <td>180</td>
	 *   </tr>
	 *   <tr>
	 *     <td><b>key_left</b></td>
	 *     <td>LEFT</td>
	 *   </tr>
	 *   <tr>
	 *     <td><b>key_up</b></td>
	 *     <td>UP</td>
	 *   </tr>
	 *   <tr>
	 *     <td><b>key_right</b></td>
	 *     <td>RIGHT</td>
	 *   </tr>
	 *   <tr>
	 *     <td><b>key_down</b></td>
	 *     <td>DOWN</td>
	 *   </tr>
	 *   <tr>
	 *     <td><b>key_action</b></td>
	 *     <td>ENTER</td>
	 *   </tr>
	 * </table>
	 */
	public void makeSettingsFile() {
		try {
			// Make the directories for the settings.xml file if they don't exist.
			File directory = new File(MAIN_PATH);
			if (directory.isFile()) {
				directory.delete();
			}
			if (!directory.exists()) {
				directory.mkdirs();
			}

			// Create the settingsFile document.
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			settingsFile = builder.newDocument();

			// Create the rootElement.
			Element rootElement = settingsFile.createElement("settings");
			settingsFile.appendChild(rootElement);

			// Create the filePathElement.
			Element filePathElement = settingsFile.createElement("file_path");
			rootElement.appendChild(filePathElement);

			// Create the dividerLocationElement and set splitPane's divider location to 180.
			Element dividerLocationElement = settingsFile.createElement("divider_location");
			dividerLocationElement.appendChild(settingsFile.createTextNode("180"));
			rootElement.appendChild(dividerLocationElement);
			splitPane.setDividerLocation(180);

			// Create the keyLeftElement and set btnKeyLeft's text to "LEFT".
			Element keyLeftElement = settingsFile.createElement("key_left");
			keyLeftElement.appendChild(settingsFile.createTextNode("37"));
			rootElement.appendChild(keyLeftElement);
			btnKeyLeft.setText("LEFT");

			// Create the keyUpElement and set btnKeyUp's text to "UP".
			Element keyUpElement = settingsFile.createElement("key_up");
			keyUpElement.appendChild(settingsFile.createTextNode("38"));
			rootElement.appendChild(keyUpElement);
			btnKeyUp.setText("UP");

			// Create the keyRightElement and set btnKeyRight's text to "RIGHT".
			Element keyRightElement = settingsFile.createElement("key_right");
			keyRightElement.appendChild(settingsFile.createTextNode("39"));
			rootElement.appendChild(keyRightElement);
			btnKeyRight.setText("RIGHT");

			// Create the keyDownElement and set btnKeyDown's text to "DOWN".
			Element keyDownElement = settingsFile.createElement("key_down");
			keyDownElement.appendChild(settingsFile.createTextNode("40"));
			rootElement.appendChild(keyDownElement);
			btnKeyDown.setText("DOWN");

			// Create the keyActionElement and set btnKeyAction's text to "ENTER".
			Element keyActionElement = settingsFile.createElement("key_action");
			keyActionElement.appendChild(settingsFile.createTextNode("10"));
			rootElement.appendChild(keyActionElement);
			btnKeyAction.setText("ENTER");

			// Create the playerElement and set btnPlayerPiece's text to "P".
			Element playerElement = settingsFile.createElement("player");
			playerElement.appendChild(settingsFile.createTextNode("P"));
			rootElement.appendChild(playerElement);
			btnPlayerPiece.setText("P");

			// Create the fontSizeElement, set sliderDisplaySize's value to 32, and change lblDisplaySizeNumber.
			Element fontSizeElement = settingsFile.createElement("font_size");
			fontSizeElement.appendChild(settingsFile.createTextNode("32"));
			rootElement.appendChild(fontSizeElement);
			sliderDisplaySize.setValue(32);
			setDisplaySizeNumberLabel();

			// Write the changed settingsFile Document to the settings.xml file.
			DOMSource source = new DOMSource(settingsFile);
			
			StreamResult result = new StreamResult(new File(SETTINGS_PATH));
			transformer.transform(source, result);
		} catch (Exception e) {
			// In the case of an error, close the program.
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "The settings.xml file could not be created.\nError: " + e.toString() + "\nThe program will now exit.");
			System.exit(2);
		}
	}
}