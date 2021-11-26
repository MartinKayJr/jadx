package jadx.gui.plugins.apktool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jadx.gui.jobs.BackgroundExecutor;
import jadx.gui.plugins.apktool.treemodel.ApkToolRoot;
import jadx.gui.plugins.apktool.utils.ApkToolProject;
import jadx.gui.plugins.apktool.utils.OkHttpClient;
import jadx.gui.treemodel.JRoot;
import jadx.gui.ui.MainWindow;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author MartinKay
 * @since 2021-11-24 16:59
 * ApkTool启动程序
 */
public class ApkToolManager {
	private static final Logger LOG = LoggerFactory.getLogger(ApkToolManager.class);

	private static final int LARGE_APK_SIZE = 30;

	private final MainWindow mainWindow;
	private final File apkFile;
	private final JTextField apkToolPathTextField;
	private final ApkToolProject apkToolProject;
	private static RSyntaxTextArea consolePane;


	public ApkToolManager(MainWindow mainWindow, JTextField apkToolPathTextField, File apkFile, RSyntaxTextArea textPane) {
		this.mainWindow = mainWindow;
		this.apkFile = apkFile;
		this.apkToolPathTextField = apkToolPathTextField;
		consolePane = textPane;
		apkToolProject = new ApkToolProject(apkFile);
	}

	public void start() {
		if (!checkFileSize(LARGE_APK_SIZE)) {
			int result = JOptionPane.showConfirmDialog(mainWindow,
					"选择的文件太大（超过30M）可能需要很长时间来分析，是否要继续",
					"ApkTool: 警告", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.NO_OPTION) {
				return;
			}
		}
		BackgroundExecutor executor = mainWindow.getBackgroundExecutor();
		executor.execute("ApkTool安装", this::checkApkToolJar, installStatus -> {
			executor.execute("ApkTool反编译", this::startDecompile, decompileStatus -> {
				loadReport();
			});
		});
	}

	private void loadReport() {

	}

	private void startDecompile() {
		try {
			apkToolProject.extractSmaliUsingAPKTool(apkFile.getAbsolutePath(), apkToolProject.getApkToolSource());
			ApkToolRoot apkToolRoot = new ApkToolRoot(new File(apkToolProject.getApkToolSource()));
			apkToolRoot.update();
			JRoot root = mainWindow.getCacheObject().getJRoot();
			root.replaceCustomNode(apkToolRoot);
			root.update();
			mainWindow.reloadTree();
			mainWindow.getTabbedPane().showNode(apkToolRoot);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void checkApkToolJar() {
		try {
			String resultStr = OkHttpClient.syncGet("https://api.github.com/repos/iBotPeaches/Apktool/releases/latest", null);
			JsonObject jsonObject = JsonParser.parseString(resultStr).getAsJsonObject();
			JsonElement tag_name = jsonObject.get("tag_name");
			consolePane.append("ApkTool最新版本：" + tag_name.toString() + "\n");
			appendLog("ApkTool当前使用版本：");
			apkToolProject.getApkToolCurrentVersion();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void appendLog(String logStr) {
		consolePane.append(logStr);
	}

	public static void appendLogLn(String logStr) {
		appendLog(logStr + "\n");
	}

	/**
	 * 检测文件大小
	 *
	 * @param sizeThreshold 限定大小
	 * @return 是否超过
	 */
	public boolean checkFileSize(int sizeThreshold) {
		try {
			int fileSize = (int) Files.size(apkFile.toPath()) / 1024 / 1024;
			if (fileSize > sizeThreshold) {
				return false;
			}
		} catch (Exception e) {
			LOG.error("Failed to calculate file: {}", e.getMessage(), e);
			return false;
		}
		return true;
	}
}
