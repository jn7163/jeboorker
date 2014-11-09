package org.rr.jeborker.gui.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.components.tree.TreeUtil;
import org.rr.commons.utils.ListUtils;

public class FileSystemTreeModel extends AbstractFileTreeModel {

	private static final long serialVersionUID = -456216843620742653L;

	private JTree tree;

	private DefaultMutableTreeNode root;

	public FileSystemTreeModel(JTree tree) {
		super(tree, new DefaultMutableTreeNode("root"));
		this.root = (DefaultMutableTreeNode) getRoot();
		this.tree = tree;
		init();
	}

	private void init() {
		List<File> specialFolders = getSpecialFolders();
		for(File specialFolder : specialFolders) {
			IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(specialFolder);
			FileSystemNode basePathNode = new FileSystemNode(resourceHandler, null);
			this.root.add(basePathNode);
		}

		File[] listRoots = File.listRoots();
		Arrays.sort(listRoots);
		for(File root : listRoots) {
			if(!root.toString().equalsIgnoreCase("A:\\")) {
				IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(root);
				FileSystemNode basePathNode = new FileSystemNode(resourceHandler, null);
				this.root.add(basePathNode);
			}
		}
	}

	protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
		super.fireTreeStructureChanged(source, path, childIndices, children);
	}

	/**
	 * This sets the user object of the TreeNode identified by path and posts a node changed. If you use custom user objects in the TreeModel you're going to
	 * need to subclass this and set the user object of the changed node to something meaningful.
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		final FileSystemNode aNode = (FileSystemNode) path.getLastPathComponent();
		String oldPathName = aNode.getResource().getResourceString();
		String newPathName = oldPathName.substring(0, oldPathName.length() - aNode.getResource().getName().length()) + newValue;

		try {
			aNode.renameTo(ResourceHandlerFactory.getResourceHandler(newPathName));
		} catch (IOException e) {
			LoggerFactory.getLogger().log(Level.WARNING, "Rename " + oldPathName +" to" + newPathName + " has failed.", e);
		}
		nodeChanged(aNode);
	}

	public void dispose() {
		TreeModelListener[] treeModelListeners = getTreeModelListeners();
		for(TreeModelListener treeModelListener : treeModelListeners) {
			removeTreeModelListener(treeModelListener);
		}
	}

	public TreePath restoreExpansionState(JTree tree, List<String> fullPathSegments) {
		String treeExpansionPathString = ListUtils.join(fullPathSegments, TreeUtil.PATH_SEPARATOR);
		TreePath lastExpandedRow = TreeUtil.restoreExpanstionState(tree, treeExpansionPathString);
		return lastExpandedRow;
	}

	/**
	 * Remove all deleted files from the model.
	 */
	public void removeDeletedFileNodes() {
		int rows = tree.getRowCount();
		ArrayList<FileSystemNode> nodesToRemove = new ArrayList<FileSystemNode>();
		for(int i = 0; i< rows; i++) {
			Object aNode = tree.getPathForRow(i).getLastPathComponent();
			if(aNode instanceof FileSystemNode) {
				FileSystemNode fsNode = (FileSystemNode) aNode;
				if(!fsNode.getResource().exists()) {
					nodesToRemove.add(fsNode);
				}
			}
		}

		for(FileSystemNode fsNode : nodesToRemove) {
			if(fsNode.getParent().getIndex(fsNode) != -1) {
				removeNodeFromParent(fsNode);
			}
		}
	}
	
	/**
	 * Remove all deleted files from the model.
	 */
	public List<FileSystemNode> getDeletedFileNodes() {
		int rows = tree.getRowCount();
		ArrayList<FileSystemNode> nodesToRemove = new ArrayList<FileSystemNode>();
		for(int i = 0; i< rows; i++) {
			Object aNode = tree.getPathForRow(i).getLastPathComponent();
			if(aNode instanceof FileSystemNode) {
				FileSystemNode fsNode = (FileSystemNode) aNode;
				if(!fsNode.getResource().exists()) {
					nodesToRemove.add(fsNode);
				}
			}
		}
		return nodesToRemove;
	}

	/**
	 * Get some special folder to be shown at the root file levels.
	 */
	private List<File> getSpecialFolders() {
		final ArrayList<File> result = new ArrayList<File>();
		final FileSystemView fileSystemView = FileSystemView.getFileSystemView();

		File defaultDirectory = fileSystemView.getDefaultDirectory();
		if(defaultDirectory != null) {
			result.add(defaultDirectory);
		}
		File homeDirectory = fileSystemView.getHomeDirectory();
		if(homeDirectory != null && !homeDirectory.equals(defaultDirectory)) {
			result.add(homeDirectory);
		}
		return result;
	}
}
