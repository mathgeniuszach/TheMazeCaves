package com.zach.tmc;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextPane;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import javax.swing.JLabel;

/**
 * The <code>Debugger</code> class is a frame that displays most of the data in
 * the {@link #game} when "Control + D" is pressed.
 * 
 * @author Zach K
 */
public class Debugger extends JFrame {
	/** Reference to the {@link Game} which this <code>Debugger</code> came from. */
	public Game game;
	
	/** The main panel where everything in the <code>Debugger</code> GUI is stored. */
	private JPanel contentPane;
	
	/** The left side text pane that holds the map. */
	private JTextPane txtpnMap;
	/** The right side text pane that holds the details. */
	private JTextPane txtpnMain;
	private JLabel lblTickspeed;

	/**
	 * Constructor for creating a <code>Debugger</code>.
	 * 
	 * @param game
	 *            The {@link Game} this <code>Debugger</code> came from.
	 */
	public Debugger(Game game) {
		this.game = game;
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				// Set frame attributes.
				setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				setBounds(100, 100, 300, 140);
				setMinimumSize(new Dimension(300, 140));
				setTitle(game.title + " - Debug");
				addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						game.debugging = false;
					}
				});
				
				// Create the content pane.
				contentPane = new JPanel();
				contentPane.setBackground(Color.LIGHT_GRAY);
				contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
				setContentPane(contentPane);
				contentPane.setLayout(new BorderLayout(0, 0));
				
				// Create a split pane for separating both text panes.
				JSplitPane splitPane = new JSplitPane();
				contentPane.add(splitPane);
				
				// Create the left side text pane that displays the map. 
				txtpnMap = new JTextPane();
				txtpnMap.setEditable(false);
				txtpnMap.setFont(new Font("Consolas", Font.PLAIN, 15));
				txtpnMap.setText("OOO\nOOO\nOOO");
				splitPane.setLeftComponent(txtpnMap);
				
				// Create the right side text pane that displays information.
				txtpnMain = new JTextPane();
				txtpnMain.setEditable(false);
				txtpnMain.setFont(new Font("Consolas", Font.PLAIN, 12));
				txtpnMain.setText("F0|0, 0|0, 0|CENTER");
				splitPane.setRightComponent(txtpnMain);
				
				// Create the label at the top of the Debugger which shows the number of ticks per second of the main loop.
				lblTickspeed = new JLabel("6 tps");
				contentPane.add(lblTickspeed, BorderLayout.NORTH);
			}
		});
	}

	/**
	 * Displays the debugger.
	 */
	public void display() {
		game.debugging = true;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				setVisible(true);
			}
		});
	}
	
	/**
	 * Debugs the data in the {@link Game} into {@link #txtpnMain}.
	 */
	public void debug() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (game.room.object == null) {
					txtpnMain.setText("F" + game.floorNumber + "|" + game.rx + ", " + game.ry + "|" + game.x + ", " + game.y + "|" + game.direction);
				} else {
					txtpnMain.setText("F" + game.floorNumber + "|" + game.rx + ", " + game.ry + "|" + game.x + ", " + game.y + "|" + game.direction
							+ "\n" + game.room.object);
				}
			}
		});
	}
	
	/**
	 * Debugs the map in the {@link Game} into {@link #txtpnMap}.
	 */
	public void debugMap() {
		StringBuilder mapText = new StringBuilder();
		for (char[] cc : game.map) {
			for (char c : cc) {
				mapText.append(c);
			}
			mapText.append("\n");
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				txtpnMap.setText(mapText.toString());
			}
		});
	}
	
	/**
	 * Debugs the amount of ticks per second the main loop is running.
	 * 
	 * @param tps
	 *            The amount of ticks per second.
	 */
	public void debugTPS(long tps) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				lblTickspeed.setText(tps + " tps");
			}
		});
	}
}
