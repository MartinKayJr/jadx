package jadx.gui.plugins.apktool.utils;

import jadx.gui.plugins.apktool.ApkToolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author MartinKay
 * @since 2021-11-23 18:15
 * ApkTool工具类
 */
public class ApkToolProject {
	private static final Logger LOG = LoggerFactory.getLogger(ApkToolProject.class);

	private final String apkName;
	private final File apkFile;
	static int memoryAllocated = 1500;
	static String heapArg = "-Xmx";
	static String memUnit = "m";
	static List<Process> processList = new ArrayList<>();

	public ApkToolProject(File apkFile) {
		this.apkFile = apkFile;
		this.apkName = apkFile.getName();
	}


	/**
	 * 包含所有东西所在的基本路径
	 *
	 * @return 路径
	 */
	public static String getBasePath() {

		File f = new File("");
		return f.getAbsolutePath();
	}

	/**
	 * apktool.jar 的路径
	 *
	 * @return 路径
	 */
	public static String getApkToolLibraryPath() {
		return getBasePath() + File.separator + "lib" + File.separator
				+ "apktool" + File.separator + "apktool.jar";
	}

	/**
	 * 解码apk并放入内容
	 *
	 * @param apkPath         apk路径
	 * @param outputDirectory 保存提取文件的路径
	 * @throws InterruptedException ex
	 * @throws IOException ex
	 */
	public void extractSmaliUsingAPKTool(String apkPath,
										 String outputDirectory) throws InterruptedException, IOException {
		String[] commands = {"java", heapArg + memoryAllocated + memUnit, "-jar", getApkToolLibraryPath(), "d",
				apkPath, "-f", "-o", outputDirectory};
		runProgram(commands, getProjectPath());
	}

	public void getApkToolCurrentVersion() {
		List<String> cmd = new ArrayList<>();
		cmd.add("java");
		cmd.add("-jar");
		cmd.add(getApkToolLibraryPath());
		cmd.add("-version");
		try {
			runCommand(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void runCommand(List<String> cmd) throws Exception {
		LOG.debug("Running command: {}", String.join(" ", cmd));
		Process process = Runtime.getRuntime().exec(cmd.toArray(new String[0]));
		try (BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			buf.lines().forEach(msg -> {
				LOG.debug("# {}", msg);
				ApkToolManager.appendLogLn(msg);
			});

		} finally {
			process.waitFor();
		}
	}

	/**
	 * 返回当前项目的路径
	 *
	 * @return 当前项目的路径
	 */
	public String getProjectPath() {
		if (!apkName.equals("")) {
			return getBasePath() + File.separator + "Projects" + File.separator
					+ apkName;
		}
		return null;
	}

	/**
	 * 包含解码的 apk 源
	 *
	 * @return 包含解码的 apk 源
	 */
	public String getApkToolSource() {
		return getProjectPath() + File.separator + "apkToolSource";
	}

	/**
	 * It runs the program from the provided working directory
	 *
	 * @param program    Program which would be run
	 * @param workingDir Directory from which program would be run
	 * @return returns 0 if success else 1
	 * @throws InterruptedException ex
	 * @throws IOException ex
	 */
	public static int runProgram(String[] program, String workingDir)
			throws InterruptedException, IOException {
		int errorFound = 0;
		StringBuilder commandRun = new StringBuilder();
		for (String command : program) {
			commandRun.append(command).append(" ");
		}
		ApkToolManager.appendLogLn(commandRun.toString());
		File file = new File(workingDir);
		file.mkdirs();
		Process proc = Runtime.getRuntime().exec(program, null,
				file);
		processList.add(proc);
		ProcessHandler inputStream = new ProcessHandler(proc.getInputStream(),
				"INPUT");
		ProcessHandler errorStream = new ProcessHandler(proc.getErrorStream(),
				"ERROR");
		/* start the stream threads */
		inputStream.start();
		errorStream.start();

		if (0 == proc.waitFor()) {
			ApkToolManager.appendLog("Process completed successfully");
		} else {
			ApkToolManager.appendLog("Encountered errors/warnings while running this program");
		}
		processList.remove(proc);
		return errorFound;
	}
}
