package com.zach.tmc;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The <code>Game</code> class is responsible for the bulk work of The Maze
 * Caves. It is responsible for parsing the level, creating a display and a
 * player, handling the controls, etc.
 * 
 * @author Zach K
 */
public class Game extends JFrame implements Runnable {
	/** The reference to the launcher this <code>Game</code> came from. */
	public Launcher launcher;
	
	/** The main piece of the display that houses the main text and {@link KeyListener}. */
	private JTextPane pane;
	
	/** The user's control for moving left. */
	public int leftKeyCode;
	/** The user's control for moving up. */
	public int upKeyCode;
	/** The user's control for moving right. */
	public int rightKeyCode;
	/** The user's control for moving down. */
	public int downKeyCode;
	/** The user's control for interacting with objects. */
	public int actionKeyCode;
	/** The user's custom piece in the <code>Game</code>. */
	public char playerPiece;
	
	/** The {@link KeyEvent} virtual key code for the last key pressed. */
	public volatile int keyCode = 0;
	
	/** The data of the save file, if it exists. */
	public String data[];
	/** The path to the level this <code>Game</code> is using. */
	public String levelPath;
	/** The title of the level. */
	public String title;
	/** The description of the level, used for displaying an information message at the start. */
	public String description;
	/** The end message of the level, used for displaying a message after the level is beaten. */
	public String message;
	
	/** The grid map of the current floor. */
	public char[][] map;
	/** This <code>Map</code> holds all of the <code>Room</code>s in the floor. Each {@link Room} has its own reference character in this <code>Map</code>.*/
	public Map<Character, Room> floor = new HashMap<Character, Room>();
	/** The active {@link Room}. */
	public Room room;
	
	/** Holds all of the active and inactive keys for conditional testing and using doors. */
	public ArrayList<String> keys = new ArrayList<String>();
	
	/** The {@link Debugger} that belongs to this <code>Game</code>. */
	public Debugger debugger;
	/** Variable to determine the {@link #debugger}'s status. */
	public boolean debugging = false;
	
	/** Variable to determine the <code>Game</code>'s status. */
	public boolean gameRunning = true;
	/** The current tick the game is on, reset every second. */
	public long tick = 0;
	
	/** The number floor the player is on. */
	public int floorNumber = 0;
	/** The x coordinate of the {@link #room} in the {@link #map}. */
	public int rx;
	/** The y coordinate of the {@link #room} in the {@link #map}. */
	public int ry;
	/** The x coordinate of the player in the {@link #room}. */
	public int x;
	/** The y coordinate of the player in the {@link #room}. */
	public int y;
	/** The queued x coordinate of the player, which may become the value of {@link x}. */
	public int newX;
	/** The queued y coordinate of the player, which may become the value of {@link y}. */
	public int newY;
	
	/**
	 * Enumeration for the player's direction of movement.
	 */
	public enum Direction {
		/** Not used. */
		NONE,
		/** Used when a player just pops into a room, either from the floor just becoming loaded or from a teleporter. */
		CENTER,
		/** Used when a player moves left. */
		LEFT,
		/** Used when a player moves up. */
		UP,
		/** Used when a player moves right. */
		RIGHT,
		/** Used when a player moves down. */
		DOWN
	}
	/** The last direction the player moved in. */
	public Direction direction;
	
	/** The <code>Game</code>'s level in {@link Document} form. */
	Document levelDocument;
	/** Reference to {@link Launcher#xPath} */
	XPath xPath = Launcher.xPath;
	
	/** The version number of the level file this Game uses. */
	public static final int VERSION = 2;
	/** The width of the display in characters. */
	public static final int ROOM_WIDTH = 16;
	/** The height of the display in characters. */
	public static final int ROOM_HEIGHT = 8;
	
	/**
	 * Constructs a new <code>Game</code> with a reference to the {@link Launcher} it
	 * started from and the level it will use.
	 * 
	 * @param launcher
	 *            The {@link Launcher} this <code>Game</code> came from.
	 * @param levelPath
	 *            The path to the level this <code>Game</code> will use.
	 */
	public Game(Launcher launcher, String levelPath) {
		this.launcher = launcher;
		this.levelPath = levelPath;
	}
	
	/**
	 * Constructs a new <code>Game</code> with a reference to the {@link Launcher}
	 * it started from and the save file data it will use.
	 * 
	 * @param launcher
	 *            The {@link Launcher} this <code>Game</code> came from.
	 * @param data
	 *            The save file data this <code>Game</code> will use.
	 */
	public Game(Launcher launcher, String[] data) throws IllegalArgumentException, NumberFormatException {
		this.launcher = launcher;
		
		if (data.length < 6) {
			throw new IllegalArgumentException("Corrupted save file");
		}
		this.data = data;
		
		this.levelPath = data[0];
		floorNumber = Integer.parseInt(data[1]);
	}

	/**
	 * Since <code>Game</code> implements a {@link Runnable}, this run method is
	 * used to launch the <code>Game</code> on a separate thread.
	 */
	@Override
	public void run() {
		// Attempt to parse the level into a document for use.
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			if (levelPath.startsWith("/")) {
				System.out.println(levelPath);
				levelDocument = documentBuilder.parse(Game.class.getResourceAsStream(levelPath));
			} else {
				levelDocument = documentBuilder.parse(levelPath);
			}
			
			// If the level is successfully parsed, then the data about that level is taken and saved.
			Element levelRoot = (Element) xPath.evaluate("/level", levelDocument, XPathConstants.NODE);
			
			title = levelRoot.getAttribute("title");
			description = decode(levelRoot.getAttribute("description"));
			message = levelRoot.getAttribute("message");
			
			// If the version of the level is not the same as the VERSION constant, ask if the user wants to cancel loading the level. 
			String versionS = levelRoot.getAttribute("version");
			int input = JOptionPane.YES_OPTION;
			
			if (versionS.isEmpty()) {
				input = JOptionPane.showConfirmDialog(null, "This level is from an older version.\nAre you sure you want to load it? (Loading older levels could cause problems)", "", JOptionPane.WARNING_MESSAGE);
			} else {
				int version = Integer.parseInt(versionS);
				if (version < VERSION) {
					input = JOptionPane.showConfirmDialog(null, "This level is from an older version.\nAre you sure you want to load it? (Loading older levels could cause problems)", "", JOptionPane.WARNING_MESSAGE);
				} else if (version > VERSION) {
					input = JOptionPane.showConfirmDialog(null, "This level is from an newer version.\nAre you sure you want to load it? (Loading newer levels could cause problems)", "", JOptionPane.WARNING_MESSAGE);
				}
			}
			
			if (input != JOptionPane.YES_OPTION) {
				// If the user decides they do not want to load the level, close the Game. Otherwise, continue.
				return;
			}
			
			// If the level has a description, display a message with that description.
			if (!description.isEmpty() && data == null) {
				JOptionPane.showMessageDialog(null, description, "Info - " + title, JOptionPane.PLAIN_MESSAGE);
			}
		} catch (Exception e) {
			// If there are any errors, the Game closes.
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "There was an error reading that level.\nError: " + e.toString());
			return;
		}
		
		// Attempt to collect the user settings and create the display of the Game.
		try {
			// Set up the debugger.
			debugger = new Debugger(this);
			
			// Collect user settings.
			leftKeyCode = Integer.parseInt(xPath.evaluate("/settings/key_left", Launcher.settingsFile));
			upKeyCode = Integer.parseInt(xPath.evaluate("/settings/key_up", Launcher.settingsFile));
			rightKeyCode = Integer.parseInt(xPath.evaluate("/settings/key_right", Launcher.settingsFile));
			downKeyCode = Integer.parseInt(xPath.evaluate("/settings/key_down", Launcher.settingsFile));
			actionKeyCode = Integer.parseInt(xPath.evaluate("/settings/key_action", Launcher.settingsFile));
			playerPiece = xPath.evaluate("/settings/player", Launcher.settingsFile).charAt(0);
			if (playerPiece == '\n') {
				playerPiece = ' ';
			}
			int displaySize = Integer.parseInt(xPath.evaluate("/settings/font_size", Launcher.settingsFile));
			
			// Create the display of the Game.
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					// Set frame attributes.
					setTitle("TMC - " + title);
					setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					
					// Stop the Game when the display is closed.
					addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosed(WindowEvent e) {
							gameRunning = false;
						}
					});
					
					// Create the text pane inside the display.
					pane = new JTextPane();
					pane.setEditable(false);
					pane.addKeyListener(new KeyListener() {

						@Override
						public void keyTyped(KeyEvent e) {
						}

						@Override
						public void keyPressed(KeyEvent e) {
							// When a key is pressed, the key is saved.
							keyCode = e.getKeyCode();
							if (keyCode == KeyEvent.VK_F1) {
								JOptionPane.showMessageDialog(null, description, "Info - " + title, JOptionPane.PLAIN_MESSAGE);
							}
							if (e.isControlDown()) {
								if (keyCode == KeyEvent.VK_D && !debugging) {
									// If "Control + D" is pressed, then the debugger is displayed.
									debugger.display();
								} else if (keyCode == KeyEvent.VK_S) {
									// If "Control + S" is pressed, then the game data is saved into a file.
									// This part of the code collects the data into a string.
									StringBuilder data = new StringBuilder();
									data.append(levelPath + "\n" + floorNumber + "\n" + rx + "\n" + ry + "\n" + x + "\n" + y);
									for (String key : keys) {
										data.append("\n" + key);
									}
									
									char[] dataArray = data.toString().toCharArray();
									byte key = (byte) (tick % 128);
									
									for (int i = 0; i < dataArray.length; i++) {
										dataArray[i] += key;
										dataArray[i] *= 10;
									}
									
									try {
										JFileChooser fileChooser = new JFileChooser();
										fileChooser.setFileFilter(new SaveFileFilter());
										fileChooser.setAcceptAllFileFilterUsed(false);
										int returnValue = fileChooser.showSaveDialog(null);
										if (returnValue == JFileChooser.APPROVE_OPTION) {
											// Get the file path. If it does not end with .save, the path is recreated with that suffix.
											String filePath = fileChooser.getSelectedFile().getPath();
											if (!filePath.endsWith(".save")) {
												filePath += ".save";
											}
											
											//
											OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8);
											writer.write(key);
											writer.write(String.valueOf(dataArray));
											writer.close();
											
											JOptionPane.showMessageDialog(null, "Sucessfully saved data to \"" + filePath + "\"");
										} else {
											JOptionPane.showMessageDialog(null, "Could not save data.");
										}
									} catch (Exception e1) {
										
									}
								}
							}
						}

						@Override
						public void keyReleased(KeyEvent e) {
						}
						
					});
					pane.setText(	"                "
								+ "\n                "
								+ "\n                "
								+ "\n                "
								+ "\n                "
								+ "\n                "
								+ "\n                "
								+ "\n                ");
					pane.setFont(new Font("Consolas", Font.PLAIN, displaySize));
					setResizable(false);
					add(pane);
					// Pack the frame so it's screen size appears just right.
					pack();
					
					// Reset the current tick the Game is on back to 0 every second and update the debugger.
					Timer tickTimer = new Timer();
					tickTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							if (debugging) {
								debugger.debugTPS(tick);
								tick = 0;
							}
						}
					}, 0, 1000);
					
					// Show the display.
					setVisible(true);
				}
			});
		} catch (Exception e) {
			// If there are any errors, than the settings file is reloaded, the display is disposed of, and the Game closes.
			launcher.loadSettings();
			dispose();
			return;
		}
		
		// Load the "first" floor of the Game.
		try {
			loadFloor();
		} catch (Exception e) {
			// If there are any errors, the display is disposed of and the Game closes.
			JOptionPane.showMessageDialog(null, "Floor #" + floorNumber + " could not be loaded.\nError: " + e.toString());
			dispose();
			return;
		}
		
		// This is the main loop of the Game.
		try {
			while (gameRunning) {
				// Check for keyboard input and act accordingly.
				if (keyCode != 0) {
					if (keyCode == leftKeyCode) {
						newX--;
						direction = Direction.LEFT;
					} else if (keyCode == upKeyCode) {
						newY--;
						direction = Direction.UP;
					} else if (keyCode == rightKeyCode) {
						newX++;
						direction = Direction.RIGHT;
					} else if (keyCode == downKeyCode) {
						newY++;
						direction = Direction.DOWN;
					} 
					
					// Check if the player is moving into a new room. If so, get the new room and new coordinates.
					loadRoom(false);
					
					// Tell the room to load the player's position and check for interaction with objects.
					room.act(false, true);
					
					// Reset the last pressed key.
					keyCode = 0;
				}
				
				// Increase the current tick.
				tick++;
			}
		} catch (Exception e) {
			// Any error closes the Game.
			JOptionPane.showMessageDialog(null, "An unexpected internal error occured while playing this game.\nError: " + e.toString() + "\nThe program will now exit.");
		}
		// If the main loop ends, then the display and the debugger are disposed of.
		dispose();
		debugger.dispose();
	}
	
	/**
	 * This method loads the floor specified by {@link #floorNumber}.
	 * 
	 * @throws XPathExpressionException
	 *             If there is an issue parsing the level, such as when something is
	 *             missing.
	 * @throws IllegalArgumentException
	 *             If a room cannot be created with its data given in a level.
	 * @throws NullPointerException
	 *             If the floor doesn't exist.
	 * @throws IndexOutOfBoundsException
	 *             If the player's x and/or y coordinates are outside the bounds of
	 *             the display.
	 * @throws ArrayIndexOutOfBoundsException
	 *             If a position in the {@link #map} that doesn't exist is referred
	 *             to (if the player's x and/or y coordinates are outside the bounds
	 *             of the array).
	 */
	public void loadFloor() throws XPathExpressionException, IllegalArgumentException, NullPointerException,
			IndexOutOfBoundsException, ArrayIndexOutOfBoundsException {
		// Begin by clearing the currently loaded floor.
		floor.clear();
		
		// Store the main XPath for that floor for further usage. 
		String floorPath = "/level/floor[@id='" + floorNumber + "']";
		
		// Collect the main character map on that floor as an array of strings.
		String[] mapText = xPath.evaluate(floorPath + "/map", levelDocument).trim().split("\n");
		// Evaluate that array of strings and turn it into a 2 dimensional character array.
		map = new char[mapText.length][];
		for (int i = 0; i < map.length; i++) {
			// This for loop runs for every row in the array of strings.
			map[i] = mapText[i].trim().toCharArray();
			for (char c : map[i]) {
				/*
				 * For every character in that row, a room is created unless a room for that
				 * character already exists or that character is a bridge.
				 */
				if (Character.isLetter(c) && floor.get(c) == null) {
					Element roomElement = (Element) xPath.evaluate(floorPath + "/" + c, levelDocument, XPathConstants.NODE);
					
					Room room = new Room(Game.this, roomElement, c);
					floor.put(c, room);
				}
			}
		}
		
		// Put the map into the debugger.
		debugger.debugMap();
		
		// Get the player's starting coordinates.
		direction = Direction.CENTER;
		if (data != null) {
			rx = Integer.parseInt(data[2]);
			ry = Integer.parseInt(data[3]);
			x = Integer.parseInt(data[4]);
			y = Integer.parseInt(data[5]);
		} else {
			Element player = (Element) xPath.evaluate(floorPath + "/player", levelDocument, XPathConstants.NODE);
			rx = Integer.parseInt(player.getAttribute("rx"));
			ry = Integer.parseInt(player.getAttribute("ry"));
			x = Integer.parseInt(player.getAttribute("x"));
			y = Integer.parseInt(player.getAttribute("y"));
		}
		
		if (x < 0 || x >= ROOM_WIDTH) {
			throw new IndexOutOfBoundsException("The player's starting x coordinate is invalid.");
		}
		if (y < 0 || y >= ROOM_HEIGHT) {
			throw new IndexOutOfBoundsException("The player's starting y coordinate is invalid.");
		}
		
		// Set up keys and delete the data from the save file.
		if (data != null) {
			keys.clear();
			
			for (int i = 6; i < data.length; i++) {
				keys.add(data[i]);
			}
			
			data = null;
		}
		
		// Reset the queued coordinates.
		newX = x;
		newY = y;
		
		// Get the starting room on that floor.
		room = floor.get(map[ry][rx]);
		loadRoom(true);
		// Put the player's piece in the starting coordinates.
		room.act(true, false);
	}
	
	/**
	 * This method sets the {@link #pane} with a 2 dimensional character map.
	 * 
	 * @param map 
	 *            The character map this method will use.
	 */
	public void setDisplay(char[][] map) {
		StringBuilder mapText = new StringBuilder();
		for (char[] cc : map) {
			for (char c : cc) {
				mapText.append(c);
			}
			mapText.append("\n");
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				pane.setText(mapText.toString());
			}
		});
	}
	
	/**
	 * This method loads the room the player is going into (if they are going into
	 * one). It takes into account transporters and transmitters.
	 * 
	 * @param force
	 *            Whether or not the room has been already changed and needs to be
	 *            force loaded.
	 */
	public void loadRoom(boolean force) {
		// Create a variable to track whether or not the player has changed rooms, or is forced to change rooms.
		boolean roomChanged = force;
		// In the case that the player moves outside the room on the left, right, top, or bottom, act accordingly.
		if (newX >= ROOM_WIDTH) {
			newX = 0;
			roomChanged = true;
		} else if (newX < 0) {
			newX = ROOM_WIDTH - 1;
			roomChanged = true;
		}
		if (newY >= ROOM_HEIGHT) {
			newY = 0;
			roomChanged = true;
		} else if (newY < 0) {
			newY = ROOM_HEIGHT - 1;
			roomChanged = true;
		}
		boolean roomChangedCopy = roomChanged;
		
		// If the room needs to be changed or is forced to change, move to a new room and player position and check for any transporters.
		if (roomChanged) {
			switch (direction) {
			case LEFT:
				rx--;
				break;
			case UP:
				ry--;
				break;
			case RIGHT:
				rx++;
				break;
			case DOWN:
				ry++;
				break;
			default:
				break;
			}
			
			Transporter transporter = room.transporters.get(direction);
			if (transporter != null) {
				int[] data = transporter.data(this);
				direction = transporter.direction;
				// Check if the transporter is trying to move the player outside the room's boundaries.
				if (!(data[3] >= Game.ROOM_WIDTH || data[3] < 0 || data[4] >= Game.ROOM_HEIGHT || data[4] < 0)) {
					// In the case of a successful transport, the new room coordinates are changed.
					rx = data[1];
					ry = data[2];
					newX = data[3];
					newY = data[4];
				} else {
					// In the case of a failed transport, do nothing and continue.
					JOptionPane.showMessageDialog(null, "The transporter failed.");
				}
			}
			
			// In the case that the queued position of the room to be loaded is outside the map, wrap the position around.
			while (ry >= map.length) {
				ry -= map.length;
			} 
			while (ry < 0) {
				ry += map.length;
			} 
			while (rx >= map[ry].length) {
				rx -= map[ry].length;
			} 
			while (rx < 0) {
				rx += map[ry].length;
			}
			
			room = floor.get(map[ry][rx]);
		}
		
		// This while loop runs until the room remains constant or the Game ends.
		while (roomChanged && gameRunning) {
			// If there is a room at the queued position, check for transmitters.
			if (Character.isLetter(map[ry][rx])) {
				Transporter transmitter = room.transmitters.get(direction);
				if (transmitter != null) {
					// If there are transmitters, check if the transmitter is trying to move the player outside the room's boundaries. 
					int[] data = transmitter.data(this);
					direction = transmitter.direction;
					if (!(data[3] >= Game.ROOM_WIDTH || data[3] < 0 || data[4] >= Game.ROOM_HEIGHT || data[4] < 0)) {
						// In the case of a successful transport, the new room coordinates are changed.
						rx = data[1];
						ry = data[2];
						newX = data[3];
						newY = data[4];
					} else {
						// In the case of a failed transport, the loop closes.
						roomChanged = false;
						JOptionPane.showMessageDialog(null, "The transmitter failed.");
						break;
					}
				} else {
					// If there are no transmitters, then the loop closes.
					roomChanged = false;
					break;
				}
			} else {
				// If there is not a room in that position, move to a new position.
				switch (direction) {
				case LEFT:
					rx--;
					break;
				case UP:
					ry--;
					break;
				case RIGHT:
					rx++;
					break;
				case DOWN:
					ry++;
					break;
				default:
					break;
				}
			}
			
			// In the case that the queued position of the room to be loaded is outside the map, wrap the position around.
			while (ry >= map.length) {
				ry -= map.length;
			} 
			while (ry < 0) {
				ry += map.length;
			} 
			while (rx >= map[ry].length) {
				rx -= map[ry].length;
			} 
			while (rx < 0) {
				rx += map[ry].length;
			}
			
			room = floor.get(map[ry][rx]);
		}
		
		// If the room has been changed at all through this process, then the player is forced to move and the room is reloaded.
		if (roomChangedCopy) {
			room.act(true, false);
			room.reload();
		}
	}
	
	/**
	 * This method takes a message and decodes it (evaluates special characters).
	 * 
	 * @param message
	 *            The message to decode.
	 * @return The decoded message.
	 */
	public String decode(String message) {
		StringBuilder messageBuilder = new StringBuilder(message);
		
		int start;
		while ((start = messageBuilder.indexOf("\\'")) != -1) {
			messageBuilder.replace(start, start + 2, "\"");
		}
		while ((start = messageBuilder.indexOf("\t")) != -1) {
			messageBuilder.replace(start, start + 2, "");
		}
		while ((start = messageBuilder.indexOf("\n")) != -1) {
			messageBuilder.replace(start, start + 2, "");
		}
		while ((start = messageBuilder.indexOf("\\n")) != -1) {
			messageBuilder.replace(start, start + 2, "\n");
		}
		while ((start = messageBuilder.indexOf("\\[")) != -1) {
			int end = messageBuilder.indexOf("]", start) + 1;
			try {
				messageBuilder.replace(start, end, "ERROR");
			} catch (Exception e) {
				messageBuilder.replace(start, end, "ERROR");
			}
		}
		return messageBuilder.toString();
	}
}