package jadx.gui.plugins.apktool.treemodel;

import jadx.gui.treemodel.JClass;
import jadx.gui.utils.UiUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

/**
 * @author MartinKay
 * @since 2021-11-25 18:02
 * ApkTool Smali源代码根目录
 */
public class ApkToolSmaliSource extends ApkToolNode {

	private static final long serialVersionUID = -3308780747277367321L;

	private static final ImageIcon SOURCE_FOLDER_ICON = UiUtils.openSvgIcon("nodes/source_folder");

	private String name;
	private final transient File smaliFile;
	private final transient boolean flatPackages;

	ApkToolSmaliSource(ApkToolRoot apkToolRoot, File smaliFile) {
		this.flatPackages = apkToolRoot.isFlatPackages();
		this.smaliFile = smaliFile;
		name = smaliFile.getName();
		update();
	}

	public final void update() {
		removeAllChildren();
		loadPackage(smaliFile, this);
	}

	private void loadPackage(File smaliFile, DefaultMutableTreeNode node) {
		File[] files = smaliFile.listFiles();
		if (files == null)
			return;

		for (File file : files) {
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
			node.add(childNode);
			if (file.isDirectory()) {
				loadPackage(file, childNode);
			}
		}
	}

	@Override
	public JClass getJParent() {
		return null;
	}

	@Override
	public Icon getIcon() {
		return SOURCE_FOLDER_ICON;
	}

	@Override
	public String makeString() {
		return name;
	}
}
