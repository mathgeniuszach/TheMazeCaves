package com.zach.tmc;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The <code>Room</code> class houses data about each individual room in each
 * {@link Game}. This mainly includes the objects and blocks inside; however,
 * the <code>Room</code> class is also responsible for the player's movement and
 * collision detection.
 * 
 * @author Zach K
 */
public class Room {
	/** Reference to the {@link Game} which this <code>Room</code> came from. */
	public Game game;
	/** The character in the {@link Game#map} linked to this <code>Room</code>. */
	public char reference;
	
	/** The text in this <code>Room</code>. */
	public char[][] room;
	/** The text of this <code>Room</code> when it was originally created without objects. */
	public char[][] roomBackup;
	
	/** Houses the {@link Transporter}s in this room. */
	public Map<Game.Direction, Transporter> transporters = new HashMap<>();
	/** Houses the transmitters ({@link Transporter}) in this room. */
	public Map<Game.Direction, Transporter> transmitters = new HashMap<>();
	/** Houses the objects in this room. */
	public Map<Dimension, RoomObject> objectMap = new HashMap<Dimension, RoomObject>();
	/** The object the player is standing on or planning to move on. */
	public RoomObject object;
	
	/**
	 * Constructs a new <code>Room</code> from an {@link Element} with a reference
	 * to the {@link Game} it came from.
	 * 
	 * @param game
	 *            The {@link Game} this room came from.
	 * @param roomElement
	 *            The {@link Element} to create the room with.
	 * @param reference
	 *            The character in the {@link Game#floor} linked to this
	 *            <code>Room</code>.
	 * @throws IllegalArgumentException
	 *             When the room cannot be created with the data given, for a wide
	 *             variety of reasons.
	 */
	public Room(Game game, Element roomElement, char reference) throws IllegalArgumentException {
		this.game = game;
		this.reference = reference;
		load(roomElement);
	}
	
	/**
	 * Loads the room and its data using an {@link Element}.
	 * 
	 * @param roomElement
	 *            The {@link Element} to load the room with.
	 * @throws IllegalArgumentException
	 *             When the room cannot be created with the data given, for a wide
	 *             variety of reasons.
	 */
	public void load(Element roomElement) throws IllegalArgumentException {
		// Load the map of the room.
		NodeList mapList = roomElement.getElementsByTagName("map");
		
		String[] roomText;
		if (mapList.getLength() > 0) {
			roomText = mapList.item(0).getTextContent().trim().split("\n");
		} else {
			// If there is no map in the Element, throw an error.
			throw new IllegalArgumentException("There is no map in room '" + reference + "'");
		}
		room = new char[Game.ROOM_HEIGHT][Game.ROOM_WIDTH];
		roomBackup = new char[Game.ROOM_HEIGHT][Game.ROOM_WIDTH];
		for (int j = 0; j < room.length; j++) {
			room[j] = roomText[j].trim().substring(1, Game.ROOM_WIDTH + 1).toCharArray();
			roomBackup[j] = roomText[j].trim().substring(1, Game.ROOM_WIDTH + 1).toCharArray();
			if (room[j].length != Game.ROOM_WIDTH) {
				// If a row in the map in the Element is not the same size as the game's width, throw an error.
				throw new IllegalArgumentException("The length of row " + j + " in the map in room '" + reference + "' is invalid");
			}
		}
		if (room.length != Game.ROOM_HEIGHT) {
			// If the height of the map in the Element is not the same size as the game's height, throw an error.
			throw new IllegalArgumentException("The height of the map in room '" + reference + "' is invalid");
		}
		
		// Load the transporters.
		NodeList transporters = roomElement.getElementsByTagName("transporter");
		for (int i = 0; i < transporters.getLength(); i++) {
			Element transporter = (Element) transporters.item(i);
			
			Game.Direction directionFrom = Game.Direction.valueOf(transporter.getAttribute("from"));
			if (this.transporters.get(directionFrom) != null) {
				// If there are more than one transporters with the same "from" direction, throw an error.
				throw new IllegalArgumentException("There are conflicting transporters (" + directionFrom + ") in room '" + reference + "'");
			}
			
			String direction = transporter.getAttribute("to");
			Game.Direction directionTo;
			if (direction.isEmpty()) {
				directionTo = directionFrom;
			} else {
				directionTo = Game.Direction.valueOf(direction);
			}
			
			String tx = transporter.getAttribute("tx");
			if (tx.isEmpty()) {
				tx = "~0";
			}
			String ty = transporter.getAttribute("ty");
			if (ty.isEmpty()) {
				ty = "~0";
			}
			String rtx = transporter.getAttribute("trx");
			if (rtx.isEmpty()) {
				rtx = "~0";
			}
			String rty = transporter.getAttribute("try");
			if (rty.isEmpty()) {
				rty = "~0";
			}
			
			Transporter objectTransporter = new Transporter(directionTo, rtx, rty, tx, ty);
			
			this.transporters.put(directionFrom, objectTransporter);
		}
		
		// Load the transmitters.
		NodeList transmitters = roomElement.getElementsByTagName("transmitter");
		for (int i = 0; i < transmitters.getLength(); i++) {
			Element transmitter = (Element) transmitters.item(i);
			
			Game.Direction directionFrom = Game.Direction.valueOf(transmitter.getAttribute("from"));
			if (this.transmitters.get(directionFrom) != null) {
				// If there are more than one transmitters with the same "from" direction, throw an error.
				throw new IllegalArgumentException("There are conflicting transmitters (" + directionFrom + ") in room '" + reference + "'");
			}
			
			String direction = transmitter.getAttribute("to");
			Game.Direction directionTo;
			if (direction.isEmpty()) {
				directionTo = directionFrom;
			} else {
				directionTo = Game.Direction.valueOf(direction);
			}
			
			String tx = transmitter.getAttribute("tx");
			if (tx.isEmpty()) {
				tx = "~0";
			}
			String ty = transmitter.getAttribute("ty");
			if (ty.isEmpty()) {
				ty = "~0";
			}
			String rtx = transmitter.getAttribute("trx");
			if (rtx.isEmpty()) {
				rtx = "~0";
			}
			String rty = transmitter.getAttribute("try");
			if (rty.isEmpty()) {
				rty = "~0";
			}
			
			Transporter objectTransmitter = new Transporter(directionTo, rtx, rty, tx, ty);
			
			this.transmitters.put(directionFrom, objectTransmitter);
		}

		// Load the buttons.
		NodeList buttons = roomElement.getElementsByTagName("button");
		for (int i = 0; i < buttons.getLength(); i++) {
			Element button = (Element) buttons.item(i);
			
			char piece = button.getAttribute("piece").charAt(0);
			
			Button objectButton = new Button(piece, button, game);
			
			String notify = button.getAttribute("notify");
			String instant = button.getAttribute("instant");
			objectButton.setAttributes("", notify, instant);
			
			int x = Integer.parseInt(button.getAttribute("x"));
			int y = Integer.parseInt(button.getAttribute("y"));
			Dimension position = new Dimension(x, y);
			if (objectMap.get(position) != null) {
				// If there is already an object in the position the button attempts to be put in, throw an error.
				throw new IllegalArgumentException("There are conflicting objects in room '" + reference + "' at x = " + x + ", y = " + y);
			}
			
			objectMap.put(position, objectButton);
			
			room[y][x] = piece;
		}
		
		// Load the blocks.
		NodeList blocks = roomElement.getElementsByTagName("block");
		for (int i = 0; i < blocks.getLength(); i++) {
			Element block = (Element) blocks.item(i);
			
			char piece = block.getAttribute("piece").charAt(0);
			
			Block objectBlock = new Block(piece);
			
			String collidable = block.getAttribute("collidable");
			String notify = block.getAttribute("notify");
			objectBlock.setAttributes(collidable, notify, "");
			
			int x = Integer.parseInt(block.getAttribute("x"));
			int y = Integer.parseInt(block.getAttribute("y"));
			Dimension position = new Dimension(x, y);
			if (objectMap.get(position) != null) {
				// If there is already an object in the position the block attempts to be put in, throw an error.
				throw new IllegalArgumentException("There are conflicting objects in room '" + reference + "' at x = " + x + ", y = " + y);
			}
			
			objectMap.put(position, objectBlock);
			
			room[y][x] = piece;
		}
		
		// Load the doors.
		NodeList doors = roomElement.getElementsByTagName("door");
		for (int i = 0; i < doors.getLength(); i++) {
			Element door = (Element) doors.item(i);
			
			int x = Integer.parseInt(door.getAttribute("x"));
			int y = Integer.parseInt(door.getAttribute("y"));
			Dimension position = new Dimension(x, y);
			if (objectMap.get(position) != null) {
				// If there is already an object in the position the door attempts to be put in, throw an error.
				throw new IllegalArgumentException("There are conflicting objects in room '" + reference + "' at x = " + x + ", y = " + y);
			}
			
			char piece = door.getAttribute("piece").charAt(0);
			String key = door.getAttribute("key").replace('\n', ' ');
			
			Door objectDoor = new Door(piece, key, Boolean.valueOf(door.getAttribute("inverted")), game);
			
			String collidable = door.getAttribute("collidable");
			String notify = door.getAttribute("notify");
			objectDoor.setAttributes(collidable, notify, "");
			
			objectMap.put(position, objectDoor);
			
			room[y][x] = piece;
		}
	}
	
	/**
	 * Redisplays and recreates the {@link #room} using {@link #roomBackup}. 
	 */
	public void reload() {
		// Recreate non-player part of the room.
		RoomObject object;
		for (int i = 0; i < room.length; i++) {
			for (int j = 0; j < room[i].length; j++) {
				// For every position, Check if there is an object in that space.
				object = objectMap.get(new Dimension(j, i));
				if (object != null && !(object instanceof Door && !object.getCollision(game))) {
					// If there is an object in that position and it is not an open door, set that space to the object's piece.
					room[i][j] = object.piece;
				} else {
					// If there is no object or an open door in that position, set that space to the roomBackup's space.
					room[i][j] = roomBackup[i][j];
				}
			}
		}
		// Redisplay the player's piece.
		if (this.object != null && this.object.notify) {
			room[game.newY][game.newX] = '!';
		} else {
			room[game.newY][game.newX] = game.playerPiece;
		}
		
		// Set the display.
		game.setDisplay(room);
	}
	
	/**
	 * The act method checks for collision and moves the player using the queued
	 * coordinates. It also checks if the player interacted with a button.
	 * 
	 * @param force
	 *            Whether or not to force the player into the queued position.
	 * @param displayLast
	 *            Whether or not to display that player's last position.
	 */
	public void act(boolean force, boolean displayLast) {
		// If the last space where the player was should be redisplayed, do so.
		if (displayLast) {
			if (object == null || object instanceof Door && !object.getCollision(game)) {
				room[game.y][game.x] = roomBackup[game.y][game.x];
			} else {
				room[game.y][game.x] = object.piece;
			}
		}
		// Check for an object at the queued position.
		object = objectMap.get(new Dimension(game.newX, game.newY));
		if (object == null) {
			// If there is no object in the queued position, move there if forced to or if the position is empty.
			if (room[game.newY][game.newX] == ' ' || force) {
				game.x = game.newX;
				game.y = game.newY;
				room[game.newY][game.newX] = game.playerPiece;
			} else {
				// In the case that the player does not move, the queued position is reset and the piece is redisplayed.
				game.newX = game.x;
				game.newY = game.y;
				object = objectMap.get(new Dimension(game.x, game.y));
				if (object != null && object.notify) {
					room[game.newY][game.newX] = '!';
				} else {
					room[game.newY][game.newX] = game.playerPiece;
				}
			}
		} else {
			// If there is an object in the queued position, move there if the object is not collidable or if forced to.
			if (!object.getCollision(game) || force) {
				game.x = game.newX;
				game.y = game.newY;
				if (object.notify) {
					room[game.newY][game.newX] = '!';
				} else {
					room[game.newY][game.newX] = game.playerPiece;
				}
			} else {
				// In the case that the player does not move, the queued position is reset and the piece is redisplayed.
				game.newX = game.x;
				game.newY = game.y;
				object = objectMap.get(new Dimension(game.x, game.y));
				if (object != null && object.notify) {
					room[game.newY][game.newX] = '!';
				} else {
					room[game.newY][game.newX] = game.playerPiece;
				}
			}
		}
		
		game.setDisplay(room);
		
		/*
		 * If there is an Button in the place where the player is, and either the action
		 * key is being pressed or the object is instant (It instantly activates when
		 * you move into it), activate the Button.
		 */
		if (object instanceof Button) {
			if (game.keyCode == game.actionKeyCode || object.instant) {
				game.keyCode = 0;
				((Button) object).activate();
			}
		}
		
		// If debugging, let the debugger know that the information has changed.
		if (game.debugging) {
			game.debugger.debug();
		}
	}
}

/**
 * The <code>Transporter</code> class is responsible for holding the data about
 * transporters and transmitters in the room.
 * 
 * @author Zach K
 */
class Transporter {
	/** The direction to move the player in. */
	Game.Direction direction;
	/** The relative x coordinate of the room in the {@link Game#map} to move the player into. */
	String rx;
	/** The relative y coordinate of the room in the {@link Game#map} to move the player into. */
	String ry;
	/** The relative x coordinate to move the player into. */
	String x;
	/** The relative y coordinate to move the player into. */
	String y;
	
	/**
	 * Constructs a <code>Transporter</code> with its appropriate data.
	 * 
	 * @param direction
	 *            The direction to move the player in.
	 * @param rx
	 *            The relative x coordinate of the room in the {@link Game#map} to
	 *            move the player into.
	 * @param ry
	 *            The relative y coordinate of the room in the {@link Game#map} to
	 *            move the player into.
	 * @param x
	 *            The relative x coordinate to move the player into.
	 * @param y
	 *            The relative y coordinate to move the player into.
	 */
	Transporter(Game.Direction direction, String rx, String ry, String x, String y) {
		this.direction = direction;
		
		this.rx = rx;
		this.ry = ry;
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Calculates the real rx, ry, x, and y coordinates to move the player to based
	 * on the game given and the relative coordinates in the
	 * <code>Transporter</code>
	 * 
	 * @param game
	 *            The {@link Game} to calculate data with.
	 * @return An array with the real rx, ry, x, and y coordinates based on the
	 *         game.
	 */
	int[] data(Game game) {
		int[] data = new int[5];
		
		data[0] = game.floorNumber;
		
		if (this.rx.startsWith("~")) {
			data[1] = game.rx + Integer.parseInt(this.rx.substring(1));
		} else {
			data[1] = Integer.parseInt(this.rx);
		}
		
		if (this.ry.startsWith("~")) {
			data[2] = game.ry + Integer.parseInt(this.ry.substring(1));
		} else {
			data[2] = Integer.parseInt(this.ry);
		}
		
		if (this.x.startsWith("~")) {
			data[3] = game.newX + Integer.parseInt(this.x.substring(1));
		} else {
			data[3] = Integer.parseInt(this.x);
		}
		
		if (this.y.startsWith("~")) {
			data[4] = game.newY + Integer.parseInt(this.y.substring(1));
		} else {
			data[4] = Integer.parseInt(this.y);
		}
		
		return data;
	}
}

/**
 * The <code>RoomObject</code> class acts as a superclass for all of the objects
 * in each {@link Room}.
 * 
 * @author Zach K
 * @see Block
 * @see Door
 * @see Button
 */
abstract class RoomObject {
	/** The piece of the <code>RoomObject</code>. */
	char piece;
	
	/** Whether or not the block is solid. Does not always apply to {@link Door}s. */
	boolean collidable = true;
	/** Whether or not the to notify the player when they are on this object. */
	boolean notify = false;
	/** Whether or not to instantly activate this object. Only works for {@link Button}s. */
	boolean instant = false;
	
	/**
	 * Constructs a <code>RoomObject</code> with a piece.
	 * 
	 * @param piece
	 *            The piece of the <code>RoomObject</code>.
	 */
	RoomObject(char piece) {
		this.piece = piece;
	}
	
	/**
	 * Checks if the <code>RoomObject</code> is collidable.
	 * 
	 * @param game
	 *            The {@link Game} to check with.
	 * @return True if the object's {@link #collidable} is true or the object is a
	 *         closed {@link Door}, false otherwise.
	 */
	public boolean getCollision(Game game) {
		if (collidable) {
			return true;
		}
		if (this instanceof Door) {
			return !game.keys.contains(((Door) this).key) ^ ((Door) this).inverted;
		}
		return false;
	}
	
	/**
	 * Sets the attributes of this <code>RoomObject</code> using three strings. If a
	 * string is empty than that attribute is not evaluated.
	 * 
	 * @param collidable
	 *            What to set {@link #collidable} to.
	 * @param notify
	 *            What to set {@link #notify} to.
	 * @param instant
	 *            What to set {@link #instant} to.
	 */
	public void setAttributes(String collidable, String notify, String instant) {
		if (!collidable.isEmpty()) {
			this.collidable = Boolean.valueOf(collidable);
		}
		if (!notify.isEmpty()) {
			this.notify = Boolean.valueOf(notify);
		}
		if (!instant.isEmpty()) {
			this.instant = Boolean.valueOf(instant);
		}
	}
	
	/**
	 * Sets the <code>RoomObject</code>'s {@link #collidable} field.
	 * 
	 * @param collidable
	 *            What to set {@link #collidable} to.
	 */
	public void setCollidable(boolean collidable) {
		this.collidable = collidable;
	}
	
	/**
	 * Sets the <code>RoomObject</code>'s {@link #notify} field.
	 * 
	 * @param collidable
	 *            What to set {@link #notify} to.
	 */
	public void setNotify(boolean notify) {
		this.notify = notify;
	}
	
	/**
	 * Sets the <code>RoomObject</code>'s {@link #instant} field.
	 * 
	 * @param collidable
	 *            What to set {@link #instant} to.
	 */
	public void setInstant(boolean instant) {
		this.instant = instant;
	}
	
	/**
	 * Returns the attributes of this <code>RoomObject</code> in String form.
	 * 
	 * @return The attributes of this <code>RoomObject</code> in String form.
	 */
	public String getAttributes() {
		return "c=" + collidable + ",n=" + notify + ",i=" + instant;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "[" + piece + "," + getAttributes() + "]";
	}
}

/**
 * The <code>Block</code> class is a simple {@link RoomObject} that does nothing
 * special but sit in the {@link Room}.
 * 
 * @author Zach K
 */
class Block extends RoomObject {
	/**
	 * Constructs a <code>Block</code> with a piece.
	 * 
	 * @param piece
	 *            The piece of the <code>Block</code>.
	 */
	Block(char piece) {
		super(piece);
		
		this.collidable = true;
		this.notify = false;
		this.instant = false;
	}
}

/**
 * The <code>Door</code> class is a {@link RoomObject} that acts like a
 * {@link Block}, but can be open or closed based on it's {@link #key}.
 * 
 * @author Zach K
 */
class Door extends RoomObject {
	/** The {@link Game} this <code>Door</code> is a part of. */
	Game game;
	/** The key that opens the <code>Door</code>. */
	String key;
	/** Whether or not the <code>Door</code> is inverted (the key closes the door). */
	boolean inverted;
	
	/**
	 * Constructs a <code>Door</code> with a piece, key, and game.
	 * 
	 * @param piece
	 *            The piece of the <code>Door</code>
	 * @param key
	 *            The key that opens the <code>Door</code>.\
	 * @param inverted
	 *            Whether or not the <code>Door</code> is inverted, meaning the key
	 *            closes the <code>Door</code> rather than opening it.
	 * @param game
	 *            The {@link Game} this <code>Door</code> belongs to.
	 */
	Door(char piece, String key, boolean inverted, Game game) {
		super(piece);
		
		this.collidable = false;
		this.notify = false;
		this.instant = false;
		
		this.key = key;
		this.inverted = inverted;
		
		this.game = game;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "[" + piece + ",key=\"" + key + "\",open=" + !getCollision(game) + "," + getAttributes() + "," + "]";
	}
}

/**
 * The <code>Button</code> class is a {@link RoomObject} that performs actions
 * when interacted with.
 * 
 * @author Zach K
 */
class Button extends RoomObject {
	/**	A list of {@link Action}s this Button performs on interaction. */
	ArrayList<Action> actions = new ArrayList<>(3);
	
	Button(char piece, Element buttonElement, Game game) {
		super(piece);
		this.collidable = false;
		this.notify = true;
		this.instant = false;
		
		// Get the Button's in-line action.
		if (!buttonElement.getAttribute("type").isEmpty()) {
			this.actions.add(new Action(game, buttonElement));
		}
		
		// Get the Button's child actions.
		NodeList actions = buttonElement.getElementsByTagName("action");
		
		for (int i = 0; i < actions.getLength(); i++) {
			this.actions.add(new Action(game, (Element) actions.item(i)));
		}
	}
	
	/**
	 * Activates this <code>Button</code>s actions.
	 */
	void activate() {
		for (Action action : actions) {
			action.activate();
		}
	}
	
	@Override
	public String toString() {
		StringBuilder buttonAttributes = new StringBuilder(getClass().getName() + "[" + piece + "," + getAttributes() + "]");
		for (Action action : actions) {
			buttonAttributes.append("\n  " + action);
		}
		return buttonAttributes.toString();
	}
	
}

/**
 * The <code>Action</code> class is responsible for storing data about each of
 * the things a {@link Button} will do on interaction. These are the current
 * types:
 * <table border="1" cellpadding="2">
 *   <caption>Action Types</caption>
 *   <tr>
 *     <th>Name</th>
 *     <th>Description</th>
 *     <th>Specified by</th>
 *   </tr>
 *   <tr>
 *     <td><b>message</b></td>
 *     <td>Displays a message.</td>
 *     <td>message</td>
 *   </tr>
 *   <tr>
 *     <td><b>ladder</b></td>
 *     <td>Moves the player to another floor.</td>
 *     <td>floor</td>
 *   </tr>
 *   <tr>
 *     <td><b>teleporter</b></td>
 *     <td>Moves the player to another spot on the same floor.</td>
 *     <td>rx, ry, x, y</td>
 *   </tr>
 *   <tr>
 *     <td><b>ending</b></td>
 *     <td>Ends the game.</td>
 *   </tr>
 *   <tr>
 *     <td><b>setter</b></td>
 *     <td>Sets a key's value specified</td>
 *     <td>key, value</td>
 *   </tr>
 * </table>
 * 
 * @author Zach K
 */
class Action {
	Game game;
	Element dataElement;
	
	/**
	 * Constructs an <code>Action</code> with a {@link Game} and {@link Element}.
	 * 
	 * @param game
	 *            The {@link Game} this Action performs in.
	 * @param dataElement
	 *            The element which this action holds its data in.
	 */
	Action(Game game, Element dataElement) {
		this.game = game;
		this.dataElement = dataElement;
	}
	
	/**
	 * Activates this <code>Action</code>.
	 */
	void activate() {
		// Check the condition of this Action. If true or empty, activate this Action.
		StringBuilder conditionBuilder = new StringBuilder(dataElement.getAttribute("condition"));
		while (conditionBuilder.indexOf("[") != -1) {
			int nodeStart = conditionBuilder.indexOf("[");
			int nodeEnd = conditionBuilder.indexOf("]");
			String node = conditionBuilder.substring(nodeStart + 1, nodeEnd);
			conditionBuilder.replace(nodeStart, nodeEnd + 1, String.valueOf(game.keys.contains(node)));
		}
		String condition = conditionBuilder.toString();
		
		if (condition.isEmpty() || BooleanEvaluator.eval(condition)) {
			// Check what type of Action this is.
			switch (dataElement.getAttribute("type")) {
			case "message":
				// In the case of a message, display the message.
				JOptionPane.showMessageDialog(null, game.decode(dataElement.getAttribute("message")));
				break;
			case "ladder":
				// In the case of a ladder, move the player to another floor.
				int floor = game.floorNumber;
				
				String floorS = dataElement.getAttribute("floor");
				
				if (!floorS.isEmpty()) {
					if (floorS.startsWith("~")) {
						floor += Integer.parseInt(floorS.substring(1));
					} else {
						floor = Integer.parseInt(floorS);
					}
				}
				
				if (game.floorNumber != floor) {
					try {
						game.floorNumber = floor;
						game.loadFloor();
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, "Floor #" + game.floorNumber + " could not be loaded.\nError: " + e.toString());
						game.gameRunning = false;
					}
				}
				break;
			case "teleporter":
				// In the case of a teleporter, move the player to another spot on the same floor.
				int rx = game.rx;
				int ry = game.ry;
				int x = game.x;
				int y = game.y;
				
				String rxS = dataElement.getAttribute("trx");
				String ryS = dataElement.getAttribute("try");
				String xS = dataElement.getAttribute("tx");
				String yS = dataElement.getAttribute("ty");
				
				if (!rxS.isEmpty()) {
					if (rxS.startsWith("~")) {
						rx += Integer.parseInt(rxS.substring(1));
					} else {
						rx = Integer.parseInt(rxS);
					}
				}
				
				if (!ryS.isEmpty()) {
					if (ryS.startsWith("~")) {
						ry += Integer.parseInt(ryS.substring(1));
					} else {
						ry = Integer.parseInt(ryS);
					}
				}
				
				if (!xS.isEmpty()) {
					if (xS.startsWith("~")) {
						x += Integer.parseInt(xS.substring(1));
					} else {
						x = Integer.parseInt(xS);
					}
				}
				
				if (!yS.isEmpty()) {
					if (yS.startsWith("~")) {
						y += Integer.parseInt(yS.substring(1));
					} else {
						y = Integer.parseInt(yS);
					}
				}
				
				if (!(x >= Game.ROOM_WIDTH || x < 0 || y >= Game.ROOM_HEIGHT || y < 0)) {
					game.direction = Game.Direction.CENTER;
					
					// Change the room.
					if (game.rx != rx || game.ry != ry) {
						game.rx = rx;
						game.ry = ry;
						
						game.loadRoom(true);
					}
					
					// Change the spot in the room.
					if (game.newX != x || game.newY != y) {
						game.newX = x;
						game.newY = y;
						
						game.room.act(true, true);
					}
					
				} else {
					JOptionPane.showMessageDialog(null, "The teleporter failed.");
				}
				break;
			case "ending":
				// In the case of an ending, end the game.
				game.gameRunning = false;
				JOptionPane.showMessageDialog(null, game.message);
				break;
			case "setter":
				// In the case of a setter, change the value of the specified key.
				String key = dataElement.getAttribute("key");
				StringBuilder value = new StringBuilder(dataElement.getAttribute("value"));
				while (value.indexOf("[") != -1) {
					int nodeStart = value.indexOf("[");
					int nodeEnd = value.indexOf("]");
					String node = value.substring(nodeStart + 1, nodeEnd);
					value.replace(nodeStart, nodeEnd + 1, String.valueOf(game.keys.contains(node)));
				}
				try {
					boolean keyExists = game.keys.contains(key);
					boolean booleanValue = Boolean.valueOf(BooleanEvaluator.eval(value.toString()));
					System.out.println(booleanValue);
					if (booleanValue && !keyExists) {
						game.keys.add(key);
					} else if (!booleanValue && keyExists) {
						game.keys.remove(key);
					}
					
					game.room.reload();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Could not set the key \"" + key + "\" to \"" + value + "\"");
				}
				break;
			default:
				// Anything else does nothing.
				break;
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder attributes = new StringBuilder(50);
		String type = dataElement.getAttribute("type");
		
		switch (type) {
		case "message":
			attributes.append("type=message");
			attributes.append(",message=\"" + dataElement.getAttribute("message") + "\"");
			break;
		case "ladder":
			attributes.append("type=ladder");
			attributes.append(",floor=" + dataElement.getAttribute("floor"));
			break;
		case "teleporter":
			attributes.append("type=teleporter");
			String rx = dataElement.getAttribute("trx");
			String ry = dataElement.getAttribute("try");
			String x = dataElement.getAttribute("tx");
			String y = dataElement.getAttribute("ty");
			
			attributes.append(",trx=");
			if (!rx.isEmpty()) {
				attributes.append(dataElement.getAttribute("trx"));
			}
			attributes.append(",try=");
			if (!ry.isEmpty()) {
				attributes.append(dataElement.getAttribute("try"));
			}
			
			attributes.append(",tx=");
			if (!x.isEmpty()) {
				attributes.append(dataElement.getAttribute("tx"));
			}
			attributes.append(",ty=");
			if (!y.isEmpty()) {
				attributes.append(dataElement.getAttribute("ty"));
			}
			
			break;
		case "ending":
			attributes.append("type=ending");
			break;
		case "setter":
			attributes.append("type=setter");
			attributes.append(",key=\"" + dataElement.getAttribute("key") + "\",value=\"" + dataElement.getAttribute("value"));
			break;
		default:
			attributes.append("type=unknown");
			break;
		}
		
		String condition = dataElement.getAttribute("condition");
		if (!condition.isEmpty()) {
			attributes.append(",condition=\"" + condition + "\"");
		}
		return "Action[" + attributes + "]";
	}
}
