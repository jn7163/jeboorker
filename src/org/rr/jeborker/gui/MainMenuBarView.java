package org.rr.jeborker.gui;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.rr.commons.collection.TransformValueList;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.converter.ConverterFactory;
import org.rr.jeborker.converter.IEBookConverter;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.gui.action.ApplicationAction;
import org.rr.jeborker.gui.resources.ImageResourceBundle;
import org.rr.jeborker.metadata.MetadataHandlerFactory;

class MainMenuBarView extends JMenuBar {

	private static final long serialVersionUID = -8134987169763660105L;

	private static Icon eyesVisible;

	private static Icon eyesInvisible;
	
	static {
		eyesVisible = ImageResourceBundle.getResourceAsImageIcon("eyes_blue_16.png");
		eyesInvisible = ImageResourceBundle.getResourceAsImageIcon("eyes_gray_16.png");
	}
	
	private JMenu fileMenuBar;
	
	private JMenu editMenuBar;
	
	private JMenu extrasMenuBar;	

	JMenu mnVerzeichnisEntfernen;

	JMenu mnVerzeichnisRefresh;
	
	private JMenu mnVerzeichnisShowHide;

	
	MainMenuBarView() {
		this.init();
	}
	
	private void init() {
		add(createFileMenu());
		add(createEditMenu());
		add(createExtrasMenu());
	}
	
	/**
	 * Creates the file menu entry with it's menu items.
	 * @return The new created menu entry.
	 */
	private JMenu createFileMenu() {
		String fileMenuBarName = Bundle.getString("EborkerMainView.file");
		this.fileMenuBar = new JMenu(SwingUtils.removeMnemonicMarker(fileMenuBarName));
		this.fileMenuBar.setMnemonic(SwingUtils.getMnemonicKeyCode(fileMenuBarName));
		
		this.fileMenuBar.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				createDynamicFileMenu();
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
			
			private void createDynamicFileMenu() {
				final MainController controller = MainController.getController();
				final List<EbookPropertyItem> selectedItems = controller.getSelectedEbookPropertyItems();
				final List<IResourceHandler> selectedResources = controller.getMainTreeController().getSelectedTreeItems();
				final int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();	

				fileMenuBar.removeAll();
				
				JMenuItem mntmAddEbooks = new JMenuItem();
				mntmAddEbooks.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.ADD_BASE_PATH_ACTION, null));
				fileMenuBar.add(mntmAddEbooks);
				
				final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
				final List<String> basePath = preferenceStore.getBasePath();
				{
					String name = Bundle.getString("EborkerMainView.removeBasePath");
					mnVerzeichnisEntfernen = new JMenu(SwingUtils.removeMnemonicMarker(name));
					mnVerzeichnisEntfernen.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
					for (Iterator<String> iterator = basePath.iterator(); iterator.hasNext();) {
						String path = iterator.next();
						JMenuItem pathItem = new JMenuItem();
						pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REMOVE_BASE_PATH_ACTION, path));
						mnVerzeichnisEntfernen.add(pathItem);
					}
					fileMenuBar.add(mnVerzeichnisEntfernen);
					if(basePath.isEmpty()) {
						mnVerzeichnisEntfernen.setEnabled(false);
					}	
					
					mnVerzeichnisEntfernen.add(new JSeparator());		
					
					if(basePath.size() > 1) {
						JMenuItem pathItem = new JMenuItem();
						pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REMOVE_BASE_PATH_ACTION, "removeAll"));
						mnVerzeichnisEntfernen.add(pathItem);				
					}			
				}

				{
					String name = Bundle.getString("EborkerMainView.refreshBasePath");
					mnVerzeichnisRefresh = new JMenu(SwingUtils.removeMnemonicMarker(name));
					mnVerzeichnisRefresh.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
					for (Iterator<String> iterator = basePath.iterator(); iterator.hasNext();) {
						String path = iterator.next();
						JMenuItem pathItem = new JMenuItem();
						pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REFRESH_BASE_PATH_ACTION, path));
						mnVerzeichnisRefresh.add(pathItem);
					}
					fileMenuBar.add(mnVerzeichnisRefresh);
					if(basePath.isEmpty()) {
						mnVerzeichnisRefresh.setEnabled(false);
					}
					
					mnVerzeichnisRefresh.add(new JSeparator());
					
					if(basePath.size() > 1) {
						JMenuItem pathItem = new JMenuItem();
						pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REFRESH_BASE_PATH_ACTION, "refreshAll"));
						mnVerzeichnisRefresh.add(pathItem);				
					}
				}
				
				{
					String name = Bundle.getString("EborkerMainView.basePathVisibility");
					mnVerzeichnisShowHide = new JMenu(SwingUtils.removeMnemonicMarker(name));
					mnVerzeichnisShowHide.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
					for (Iterator<String> iterator = basePath.iterator(); iterator.hasNext();) {
						String path = iterator.next();
						ApplicationAction action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SHOW_HIDE_BASE_PATH_ACTION, path);
						
						boolean isShow = MainMenuBarController.getController().isShowHideBasePathStatusShow(path);
						if(isShow) {
							action.putValue(Action.SMALL_ICON, eyesVisible);
						} else {
							action.putValue(Action.SMALL_ICON, eyesInvisible);
						}		
						JMenuItem pathItem = new JMenuItem(action);
						mnVerzeichnisShowHide.add(pathItem);
					}
					
					mnVerzeichnisShowHide.add(new JSeparator());
					
					if(basePath.size() > 1) {
						{
							JMenuItem pathItem = new JMenuItem();
							pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SHOW_HIDE_BASE_PATH_ACTION, "showAll"));
							mnVerzeichnisShowHide.add(pathItem);
						}
						{
							JMenuItem pathItem = new JMenuItem();
							pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SHOW_HIDE_BASE_PATH_ACTION, "hideAll"));
							mnVerzeichnisShowHide.add(pathItem);
						}		
					}
					
					fileMenuBar.add(mnVerzeichnisShowHide);
					if(basePath.isEmpty()) {
						mnVerzeichnisShowHide.setEnabled(false);
					}		
				}
				
				fileMenuBar.add(new JSeparator());
				
				final JMenuItem saveMetadataMenuEntry = new JMenuItem((ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, "")));
				saveMetadataMenuEntry.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
				fileMenuBar.add(saveMetadataMenuEntry);
				
				fileMenuBar.add(new JSeparator());
				
				//Open folder only for single selection.
				final JMenuItem openFolderMenuEntry;
				final JMenuItem openFileMenuEntry;
				final JMenuItem deleteFileMenuEntry;
				if(selectedItems.size() == 1) {
					openFolderMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FOLDER_ACTION, selectedItems.get(0).getFile()));
					openFolderMenuEntry.setEnabled(true);
					
					openFileMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FILE_ACTION, selectedItems.get(0).getFile()));
					openFileMenuEntry.setEnabled(true);
				} else {
					if(selectedResources.size() == 1) {
						openFolderMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FOLDER_ACTION, selectedResources.get(0).toString()));
						openFolderMenuEntry.setEnabled(true);
						
						openFileMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FILE_ACTION, selectedResources.get(0).toString()));
						openFileMenuEntry.setEnabled(true);						
					} else {					
						openFolderMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FOLDER_ACTION, ""));
						openFolderMenuEntry.setEnabled(false);
						
						openFileMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FILE_ACTION, ""));
						openFileMenuEntry.setEnabled(false);
					}
				}
				
				if(selectedItems.size() >= 1) {
					deleteFileMenuEntry = new JMenuItem(ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.DELETE_FILE_ACTION, selectedItems, selectedEbookPropertyItemRows));
					deleteFileMenuEntry.setEnabled(true);	
				} else {
					List<IResourceHandler> selectedTreeItems = MainController.getController().getMainTreeController().getSelectedTreeItems();
					if(selectedTreeItems.size() > 0) {
						deleteFileMenuEntry = new JMenuItem(ActionFactory.getActionForResource(ActionFactory.DYNAMIC_ACTION_TYPES.DELETE_FILE_ACTION, selectedTreeItems));
						deleteFileMenuEntry.setEnabled(true);
					} else {
						deleteFileMenuEntry = new JMenuItem(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.DELETE_FILE_ACTION, ""));
						deleteFileMenuEntry.setEnabled(false);
					}
				}
				
				JMenu copyToSubMenu = MainMenuBarController.createCopyToMenu();
				if(selectedItems.size() >= 1) {
					copyToSubMenu.setEnabled(true);
				}  else {
					if(controller.getMainTreeController().getSelectedTreeItems().size() > 0) {
						copyToSubMenu.setEnabled(true);	
					} else {
						copyToSubMenu.setEnabled(false);
					}
				}
				
				fileMenuBar.add(openFileMenuEntry);
				fileMenuBar.add(openFolderMenuEntry);
				fileMenuBar.add(copyToSubMenu);
				fileMenuBar.add(deleteFileMenuEntry);
				
				fileMenuBar.add(new JSeparator());
				
				//quit menu entry
				JMenuItem mntmQuit = new JMenuItem();
				mntmQuit.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.QUIT_ACTION, (String) null));
				mntmQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
				
				fileMenuBar.add(mntmQuit);
			}
		});
		
		return fileMenuBar;
	}
	
	private JMenu createEditMenu() {
		String editMenuBarName = Bundle.getString("EborkerMainView.edit");
		this.editMenuBar = new JMenu(SwingUtils.removeMnemonicMarker(editMenuBarName));
		this.editMenuBar.setMnemonic(SwingUtils.getMnemonicKeyCode(editMenuBarName));
		
		this.editMenuBar.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				final MainController controller = MainController.getController();
				final List<EbookPropertyItem> selectedItems = controller.getSelectedEbookPropertyItems();
				final int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();	
				final List<IResourceHandler> selectedItemResources = new TransformValueList<EbookPropertyItem, IResourceHandler>(selectedItems) {

					@Override
					public IResourceHandler transform(EbookPropertyItem source) {
						return source.getResourceHandler();
					}
				};
				
				editMenuBar.removeAll();
				createDynamicEditMenu(selectedItems, selectedEbookPropertyItemRows);
				
				JMenuItem find = new JMenuItem(ActionFactory.getTableFindAction(null));
				find.setText(SwingUtils.removeMnemonicMarker(Bundle.getString("MainMenuBarView.find")));
				find.setMnemonic(SwingUtils.getMnemonicKeyCode(Bundle.getString("MainMenuBarView.find")));
				find.setIcon(ImageResourceBundle.getResourceAsImageIcon("find_16.png"));
				find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));
				editMenuBar.add(find);						
				
				editMenuBar.add(new JSeparator());
				
				createDynamicMetadataMenuEntries(selectedItems, selectedEbookPropertyItemRows);
				
				JMenuItem metadataDownloadItem = new JMenuItem();
				metadataDownloadItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.VIEW_METADATA_DOWNLOAD_ACTION, null));
				editMenuBar.add(metadataDownloadItem);
				if(selectedItems.isEmpty() || !MetadataHandlerFactory.hasWriterSupport(selectedItemResources)) {
					metadataDownloadItem.setEnabled(false);
				}				
				
				createConvertMenuEntry(selectedItems, selectedEbookPropertyItemRows);
				
				editMenuBar.add(new JSeparator());
				
				JMenuItem editPreferencesItem = new JMenuItem();
				editPreferencesItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.VIEW_PREFERENCE_DIALOG_ACTION, null));
				editMenuBar.add(editPreferencesItem);				
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
			
			private void createDynamicEditMenu(List<EbookPropertyItem> selectedItems, int[] selectedEbookPropertyItemRows) {
				List<IResourceHandler> selectedTreeItems = MainController.getController().getMainTreeController().getSelectedTreeItems();
				
				JMenuItem copyClipboard = new JMenuItem(ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.COPY_TO_CLIPBOARD_ACTION, selectedItems, selectedEbookPropertyItemRows));
				if(!copyClipboard.isEnabled()) {
					copyClipboard = new JMenuItem(ActionFactory.getActionForResource(ActionFactory.DYNAMIC_ACTION_TYPES.COPY_TO_CLIPBOARD_ACTION, selectedTreeItems));
				}
				copyClipboard.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
				editMenuBar.add(copyClipboard);	
				
				
				JMenuItem pasteClipboard = new JMenuItem(ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.PASTE_FROM_CLIPBOARD_ACTION, selectedItems, selectedEbookPropertyItemRows));
				if(!pasteClipboard.isEnabled()) {
					pasteClipboard = new JMenuItem(ActionFactory.getActionForResource(ActionFactory.DYNAMIC_ACTION_TYPES.PASTE_FROM_CLIPBOARD_ACTION, selectedTreeItems));
				}
				pasteClipboard.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
				editMenuBar.add(pasteClipboard);	
			}
			
			/**
			 * Creates the menu entries for the metadata menu entry. 
			 * @param items Items for the menu items.
			 * @return The list of menu entries.
			 */
			private void createDynamicMetadataMenuEntries(List<EbookPropertyItem> selectedItems, int[] selectedEbookPropertyItemRows) {
				String name = Bundle.getString("EborkerMainView.cover");
				JMenu coverSubMenu = new JMenu(SwingUtils.removeMnemonicMarker(name));
				coverSubMenu.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
				coverSubMenu.setIcon(ImageResourceBundle.getResourceAsImageIcon("image_16.png"));
				MainView.addCoverMenuItems(coverSubMenu, selectedItems, selectedEbookPropertyItemRows);
				editMenuBar.add(coverSubMenu);		
				if(coverSubMenu.getMenuComponentCount() == 0) {
					coverSubMenu.setEnabled(false);
				}
				
				Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.EDIT_PLAIN_METADATA_ACTION, selectedItems, selectedEbookPropertyItemRows);
				editMenuBar.add(new JMenuItem(action));		
				
				action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.REFRESH_ENTRY_ACTION, selectedItems, selectedEbookPropertyItemRows);
				editMenuBar.add(new JMenuItem(action));				
			}
			
			private void createConvertMenuEntry(List<EbookPropertyItem> selectedItems, int[] selectedEbookPropertyItemRows) {
				String name = Bundle.getString("EborkerMainView.convert");
				JMenu convertSubMenu = new JMenu(SwingUtils.removeMnemonicMarker(name));
				convertSubMenu.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
				convertSubMenu.setIcon(ImageResourceBundle.getResourceAsImageIcon("convert_16.png"));
				convertSubMenu.setEnabled(false);
				
				if(!selectedItems.isEmpty() && sameType(selectedItems)) {
					List<IEBookConverter> converter = ConverterFactory.getConverter(selectedItems.get(0).getResourceHandler());
					for(IEBookConverter c : converter) {
						Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.CONVERT_EBOOK_ACTION, selectedItems, selectedEbookPropertyItemRows);
						action.putValue("converterClass", c.getClass());
						JMenuItem converterMenuItem = new JMenuItem(action);
						
						converterMenuItem.setText(StringUtils.capitalize(c.getConversionSourceType().getName()) + " " + Bundle.getString("MainMenuBarView.conversion.connector") + " " + StringUtils.capitalize(c.getConversionTargetType().getName()));
						convertSubMenu.add(converterMenuItem);
						convertSubMenu.setEnabled(true);
					}
				} 
				editMenuBar.add(convertSubMenu);
			}
		});		

		
		return this.editMenuBar;
	}
	
	private JMenu createExtrasMenu() {
		final String extrasMenuBarName = Bundle.getString("EborkerMainView.extras");
		
		this.extrasMenuBar = new JMenu(SwingUtils.removeMnemonicMarker(extrasMenuBarName));
		this.extrasMenuBar.setMnemonic(SwingUtils.getMnemonicKeyCode(extrasMenuBarName));

		JMenuItem logItem = new JMenuItem();
		logItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.VIEW_LOG_MONITOR_ACTION, null));
		extrasMenuBar.add(logItem);
		
		{ // look and feel menu
			String lookAndFeelName = Bundle.getString("EborkerMainView.laf");
			JMenu lookAndFeelMenu = new JMenu(SwingUtils.removeMnemonicMarker(lookAndFeelName));
			lookAndFeelMenu.setMnemonic(SwingUtils.getMnemonicKeyCode(lookAndFeelName));
			final String currentLaf = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.PREFERENCE_KEYS.LOOK_AND_FEEL)
					.getEntryAsString(PreferenceStoreFactory.PREFERENCE_KEYS.LOOK_AND_FEEL);
			final ButtonGroup grp = new ButtonGroup();
			final HashMap<String, JMenu> subMenus = new HashMap<String, JMenu>();
			for(String lafName : JeboorkerConstants.LOOK_AND_FEELS.keySet()) {
				JMenu parentMenu = lookAndFeelMenu;
				String lafViewName = lafName;
				if(lafName.contains(";")) {
					List<String> split = ListUtils.split(lafName, ";");
					parentMenu = subMenus.containsKey(split.get(0)) ? subMenus.get(split.get(0)) : new JMenu(split.get(0));
					subMenus.put(split.get(0), parentMenu);
					lookAndFeelMenu.add(parentMenu);
					lafViewName = split.get(1);
				}
				ApplicationAction action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.CHANGE_LOOK_AND_FEEL_ACTION, lafName);
				JRadioButtonMenuItem radioMenuItem = new JRadioButtonMenuItem(action);
				radioMenuItem.setText(lafViewName);
				grp.add(radioMenuItem);
				
				if(JeboorkerConstants.LOOK_AND_FEELS.get(lafName).equals(currentLaf)) {
					radioMenuItem.setSelected(true);
				} else {
					radioMenuItem.setSelected(false);
				}
				parentMenu.add(radioMenuItem);
			}
			extrasMenuBar.add(lookAndFeelMenu);
		}
		
		return this.extrasMenuBar;
	}
	
	/**
	 * Tests of the selected {@link EbookPropertyItem} are from the same mime type. 
	 */
	private static boolean sameType(List<EbookPropertyItem> selectedItems) {
		String type = null;
		for(EbookPropertyItem item : selectedItems) {
			if(type == null) {
				type = item.getMimeType();
			} else {
				if(item.getMimeType().equals(type)) {
					continue;
				} else {
					return false;
				}
			}
		}
		return true;
	}
	
}
