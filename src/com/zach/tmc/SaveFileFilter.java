package com.zach.tmc;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * The <code>SaveFileFilter</code> class is a <code>FileFilter</code> that
 * checks for files that end with ".save" and folders.
 * 
 * @author Zach K
 */
public class SaveFileFilter extends FileFilter {

	@Override
	public boolean accept(File file) {
		if (file.getName().endsWith(".save") || file.isDirectory()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String getDescription() {
		return "Save Files (.save)";
	}

}
