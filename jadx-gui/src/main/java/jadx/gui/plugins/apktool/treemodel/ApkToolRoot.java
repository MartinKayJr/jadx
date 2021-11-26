package jadx.gui.plugins.apktool.treemodel;

import jadx.gui.treemodel.JClass;
import jadx.gui.utils.UiUtils;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author MartinKay
 * @since 2021-11-25 17:18
 */
public class ApkToolRoot extends ApkToolNode {

	private static final long serialVersionUID = 3470228069993333978L;

	private static final ImageIcon ROOT_ICON = UiUtils.openSvgIcon("ui/apktool");

	private transient boolean flatPackages = false;

	private final transient File fileRoot;

	private final List<ApkToolNode> customNodes = new ArrayList<>();


	public ApkToolRoot(File fileRoot) {
		this.fileRoot = fileRoot;
	}

	public final void update() {
		removeAllChildren();
		File[] files = fileRoot.listFiles();
		if (files == null) {
			return;
		}
		for (File file : files) {
			switch (file.getName()) {
				case "AndroidManifest.xml":
					break;
				case "apktool.yml":
					break;
				case "original":
					break;
				case "res":
					break;
				case "smali":
					add(new ApkToolSmaliSource(this, file));
					break;
			}
			System.out.println();
		}
	}

	public boolean isFlatPackages() {
		return flatPackages;
	}

	@Override
	public JClass getJParent() {
		return null;
	}

	@Override
	public Icon getIcon() {
		return ROOT_ICON;
	}

	@Override
	public String makeString() {
		return fileRoot.getName();
	}
}
