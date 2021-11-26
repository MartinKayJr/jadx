package jadx.gui.plugins.apktool.treemodel;

import jadx.gui.treemodel.JClass;
import jadx.gui.utils.UiUtils;

import javax.swing.*;
import java.io.File;

/**
 * @author MartinKay
 * @since 2021-11-25 22:00
 */
public class ApkToolSmaliPackage extends ApkToolNode {

	private static final long serialVersionUID = 8160222458533012775L;

	private static final ImageIcon PACKAGE_ICON = UiUtils.openSvgIcon("nodes/package");

	private String fullName;
	private String name;
	private boolean enabled;

	public ApkToolSmaliPackage(File file) {
		if (file != null) {
			name = file.getName();
		}
	}


	@Override
	public JClass getJParent() {
		return null;
	}

	@Override
	public Icon getIcon() {
		return PACKAGE_ICON;
	}

	@Override
	public String makeString() {
		return name;
	}
}
