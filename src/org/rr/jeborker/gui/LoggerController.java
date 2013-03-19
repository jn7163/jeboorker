package org.rr.jeborker.gui;

import java.awt.Point;

import javax.swing.JFrame;

import org.rr.jeborker.JeboorkerLogger;
import org.rr.jeborker.JeboorkerPreferences;

public class LoggerController {
	
	private static LoggerView logMonitorView = null;
	
	static LoggerController getInstance() {
		LoggerController controller = new LoggerController();
		return controller;
	}	
	
	public void showLogMonitorDialog() {
		LoggerView view = getView();
		view.setVisible(true);
	}	
	
	private LoggerView getView() {
		if(logMonitorView == null) {
			JFrame mainWindow = MainController.getController().getMainWindow();
			logMonitorView = new LoggerView(mainWindow, this, JeboorkerLogger.log);
			this.initialize();
		}
		return logMonitorView;
	}
	
	private void initialize() {
		restorePropeties();
	}	
	
	public void close() {
		storeProperties();
		
		logMonitorView.setVisible(false);
		logMonitorView.dispose();
	}	
	
	private void storeProperties() {
		JeboorkerPreferences.addGenericEntryAsNumber("logDialogSizeWidth", getView().getSize().width);
		JeboorkerPreferences.addGenericEntryAsNumber("logDialogSizeHeight", getView().getSize().height);
		JeboorkerPreferences.addGenericEntryAsNumber("logDialogLocationX", getView().getLocation().x);
		JeboorkerPreferences.addGenericEntryAsNumber("logDialogLocationY", getView().getLocation().y);
	}	

	private void restorePropeties() {
		//restore the window size from the preferences.
		Number metadataDialogSizeWidth = JeboorkerPreferences.getGenericEntryAsNumber("logDialogSizeWidth");
		Number metadataDialogSizeHeight = JeboorkerPreferences.getGenericEntryAsNumber("logDialogSizeHeight");
		if(metadataDialogSizeWidth!=null && metadataDialogSizeHeight!=null) {
			getView().setSize(metadataDialogSizeWidth.intValue(), metadataDialogSizeHeight.intValue());
		}
		
		//restore window location
		Point entryAsScreenLocation = JeboorkerPreferences.getGenericEntryAsScreenLocation("logDialogLocationX", "logDialogLocationY");
		if(entryAsScreenLocation != null) {
			getView().setLocation(entryAsScreenLocation);
		}		
	}
}