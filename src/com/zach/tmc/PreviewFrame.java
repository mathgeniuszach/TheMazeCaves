package com.zach.tmc;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JTextPane;

/**
 * The <code>PreviewFrame</code> class is used by the {@link Launcher} to show a
 * preview of a game with the selected display size. 
 * 
 * @author Zach K
 */
public class PreviewFrame extends JFrame {

	/**
	 * Creates a new <code>PreviewFrame</code> with a specific display size.
	 * 
	 * @param displaySize
	 *            The specified display size.
	 */
	public PreviewFrame(int displaySize) {
		// Set the title of the PreviewFrame based on displaySize.
		setTitle("TMC Preview - (" + displaySize + ")");
		
		// Set the display of the PreviewFrame.
		JTextPane display = new JTextPane();
		display.setEditable(false);
		display.setText("XXXXXXXXXXXXXXXX"
					+ "\nX              X"
					+ "\nX              X"
					+ "\nX   Preview    X"
					+ "\nX    Frame     X"
					+ "\nX              X"
					+ "\nX              X"
					+ "\nXXXXXXXXXXXXXXXX");
		display.setFont(new Font("Consolas", Font.PLAIN, displaySize));
		setResizable(false);
		add(display);
		
		// Pack the frame so it's screen size appears just right.
		pack();
		
		// Display the PreviewFrame.
		setVisible(true);
	}
}
