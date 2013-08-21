/***************************************************************************
 *
 * @author: Jesus Cuenca (jcuenca@cnb.csic.es)
 *
 * Unidad de  Bioinformatica of Centro Nacional de Biotecnologia , CSIC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307  USA
 *
 *  All comments concerning this program package may be sent to the
 *  e-mail address 'xmipp@cnb.csic.es'
 ***************************************************************************/
/**
 * Why a FileDialog?
 * ImageJ default file dialogs are simple AWT. This class uses the more beautiful and powerful Swing dialogs.
 * 
 * - Features:
 * FileNameExtensionFilters
 * Remember last path
 */

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileDialog {
	private JFileChooser fileChooser;
	String suggestedExtension = "";
	int status=0;

	public FileDialog(){
		fileChooser = new JFileChooser();
	}
	
	private void setupDialog(String title, String path, final String fileName,int type) {
		fileChooser.setDialogTitle(title);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setDialogType(type);

		File fdir = null;
		if (path != null)
			fdir = new File(path);
		if (fdir != null)
			fileChooser.setCurrentDirectory(fdir);
		if (fileName != null)
			fileChooser.setSelectedFile(new File(fileName));
	}
/*
	public static String openDialog(String title, Component parent) {
		FileDialog fd = new FileDialog();
		fd.setupDialog(title, null, null,JFileChooser.OPEN_DIALOG);
		int status = fd.fileChooser.showOpenDialog(parent);
		if (status != JFileChooser.APPROVE_OPTION)
			return "";
		String path=fd.getPath();
		return path;
	}
	/*
	public static String saveDialog(String title, Component parent) {
		FileDialog fd = new FileDialog();
		fd.setupDialog(title, null, null,JFileChooser.SAVE_DIALOG);
		int status = fd.fileChooser.showSaveDialog(parent);
		if (status != JFileChooser.APPROVE_OPTION)
			return "";
		String path=fd.getPath();
		return path;
	} */
	
	public void setupAsSaveDialog(String title){
		setupDialog(title, null, null,JFileChooser.SAVE_DIALOG);
	}
	
	public void setupAsOpenDialog(String title){
		setupDialog(title, null, null,JFileChooser.OPEN_DIALOG);
	}
	
	public void show(Component parent){
		fileChooser.setSelectedFile(new File("." + suggestedExtension));
		
		if(fileChooser.getDialogType() == JFileChooser.SAVE_DIALOG)
			status = fileChooser.showSaveDialog(parent);
		else
			status = fileChooser.showOpenDialog(parent);
		
	}
	
	public String getPath() {
		
		if (status != JFileChooser.APPROVE_OPTION)
			return "";
		
		File selectedFile = fileChooser.getSelectedFile();
		if (selectedFile == null)
			return null;

		if (fileChooser.getDialogType() == JFileChooser.SAVE_DIALOG)
			if ((selectedFile != null) && selectedFile.exists()) {
				int response = JOptionPane.showConfirmDialog(fileChooser,"The file " + selectedFile.getName() + " already exists. Do you want to replace it?",
						"Ovewrite file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (response != JOptionPane.YES_OPTION)
					return null;
			}


		String name = selectedFile.getName();
		String dir = fileChooser.getCurrentDirectory().getPath() + File.separator;

		String returnPath = "";
		if (name != null){
			returnPath = dir + name;
			// if(removeFile)
			//	new File(returnPath).delete();
		}
		return returnPath;
	}

	public void addFilter(FileNameExtensionFilter filter) {
		// fileChooser.setFileFilter(filter);
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.setFileFilter(filter);
		suggestedExtension = filter.getExtensions()[0];
	}
	
	public void addFilter(String description, String extension){
		addFilter(new FileNameExtensionFilter(description,extension));
	}


}
