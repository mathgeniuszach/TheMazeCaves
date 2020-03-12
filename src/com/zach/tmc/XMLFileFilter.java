package com.zach.tmc;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * The <code>XMLFileFilter</code> class is a <code>FileFilter</code> that checks
 * for files that end with ".xml" and folders.
 * 
 * @author Zach K
 */
public class XMLFileFilter extends FileFilter {

	@Override
	public boolean accept(File file) {
		if (file.getName().endsWith(".xml") || file.isDirectory()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String getDescription() {
		return "eXtensible Markup Language Files (.xml)";
	}

}
