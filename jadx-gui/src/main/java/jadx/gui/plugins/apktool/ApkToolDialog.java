package jadx.gui.plugins.apktool;

import jadx.gui.settings.JadxSettings;
import jadx.gui.ui.MainWindow;
import jadx.gui.ui.codearea.AbstractCodeArea;
import jadx.gui.utils.NLS;
import jadx.gui.utils.UiUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author MartinKay
 * @since 2021-11-23 20:59
 */
public class ApkToolDialog extends JDialog {

	private static final Logger LOG = LoggerFactory.getLogger(ApkToolDialog.class);

	private final transient JadxSettings settings;
	private final transient MainWindow mainWindow;
	private final List<Path> files;

	private JComboBox<Path> fileSelectCombo;
	private transient JTextField apkToolPathTextField;
	private transient RSyntaxTextArea textPane;


	public ApkToolDialog(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		this.settings = mainWindow.getSettings();
		this.files = filterOpenFiles(mainWindow);
		if (files.isEmpty()) {
			UiUtils.errorMessage(mainWindow, "Quark is unable to analyze loaded files");
			LOG.error("Quark: The files cannot be analyzed: {}", mainWindow.getWrapper().getOpenPaths());
			return;
		}
		initUI();
	}

	private void initUI() {
		JLabel selectApkText = new JLabel("选择 Apk");
		JLabel apkToolPathText = new JLabel("ApkTool.jar");

		apkToolPathTextField = new JTextField();

		JPanel apkToolConfigPane = new JPanel(new BorderLayout(5, 5));
		apkToolConfigPane.add(apkToolPathText, BorderLayout.WEST);
		apkToolConfigPane.add(apkToolPathTextField, BorderLayout.CENTER);

		fileSelectCombo = new JComboBox<>(files.toArray(new Path[0]));
		fileSelectCombo.setRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value.getFileName().toString()));


		JPanel selectApkPanel = new JPanel(new BorderLayout(5, 5));
		selectApkPanel.add(selectApkText, BorderLayout.WEST);
		selectApkPanel.add(fileSelectCombo, BorderLayout.CENTER);

		// ApkTool配置面板
		JPanel wrapperPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		wrapperPanel.add(selectApkPanel);
		apkToolConfigPane.add(wrapperPanel, BorderLayout.SOUTH);

		// 日志滚动区
		textPane = AbstractCodeArea.getDefaultArea(mainWindow);
		textPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setMinimumSize(new Dimension(100, 150));
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));

		// 按钮区
		JPanel buttonPane = new JPanel();
		JButton start = new JButton("Start");
		JButton close = new JButton("Close");
		close.addActionListener(event -> close());
		start.addActionListener(event -> startApkToolTask());
		buttonPane.add(start);
		buttonPane.add(close);
		getRootPane().setDefaultButton(close);

		JPanel mainPane = new JPanel(new BorderLayout(5, 5));
		mainPane.add(apkToolConfigPane, BorderLayout.NORTH);
		mainPane.add(scrollPane, BorderLayout.CENTER);
		mainPane.add(buttonPane, BorderLayout.SOUTH);
		mainPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		getContentPane().add(mainPane);


		setTitle("ApkTool");
		pack();
		if (!mainWindow.getSettings().loadWindowPos(this)) {
			setSize(800, 600);
		}
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		UiUtils.addEscapeShortCutToDispose(this);
	}

	private void startApkToolTask() {
		Path apkFile = (Path) fileSelectCombo.getSelectedItem();
		if (apkFile != null) {
			new ApkToolManager(mainWindow, apkToolPathTextField, apkFile.toFile(), textPane).start();
		}
	}

	private List<Path> filterOpenFiles(MainWindow mainWindow) {
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.{apk,dex}");
		return mainWindow.getWrapper().getOpenPaths()
				.stream()
				.filter(matcher::matches)
				.collect(Collectors.toList());
	}

	private void close() {
		dispose();
	}

	@Override
	public void dispose() {
		settings.saveWindowPos(this);
		super.dispose();
	}
}
