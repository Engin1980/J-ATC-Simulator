package eng.jAtcSim.frmPacks.sdi;

import eng.eSystem.collections.IMap;
import eng.eSystem.utilites.ExceptionUtils;
import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.Stylist;
import eng.jAtcSim.app.FrmAbout;
import eng.jAtcSim.frmPacks.shared.*;
import eng.jAtcSim.abstractRadar.settings.RadarBehaviorSettings;
import eng.jAtcSim.abstractRadar.settings.RadarDisplaySettings;
import eng.jAtcSim.abstractRadar.RadarViewPort;
import eng.jAtcSim.abstractRadar.global.SoundManager;
import eng.jAtcSim.recording.Recording;
import eng.jAtcSim.recording.Settings;
import eng.eSystem.swing.LayoutManager;
import eng.jAtcSim.shared.MessageBox;
import eng.jAtcSim.app.extenders.swingFactory.SwingFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class FrmMain extends JFrame {

  private Recording recording = null;
  private Pack parent;
  private JPanel pnlContent;
  private JPanel pnlBottom;
  private JPanel pnlLeft;
  private JPanel pnlTop;
  private JPanel pnlRight;
  private SwingRadarPanel srpRadar;
  private FlightListPanel flightListPanel;
  private CommandButtonsPanel pnlCommands;
  private String lastFileName = null;

  public FrmMain() {
    initComponents();
  }

  public IMap<Integer, RadarViewPort> getRadarStoredPositions() {
    return srpRadar.getRadarStoredPositions();
  }

  public void setRadarStoredPositions(IMap<Integer, RadarViewPort> positions) {
    srpRadar.setRadarStoredPositions(positions);
  }

  private void appendListenerForKeyToRadar() {
    ComponentUtils.adjustComponentTree(this,
        q -> q.getClass().equals(JTextField.class) == false,
        q -> q.addKeyListener(new KeyAdapter() {
          @Override
          public void keyReleased(KeyEvent e) {
            srpRadar.setFocus(e.getKeyChar());
          }
        })
    );
  }

  private void initComponents() {

    eng.jAtcSim.JAtcSim.setAppIconToFrame(this);

    this.setPreferredSize(new Dimension(1000, 600));
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setTitle("SDI");

    Color bgColor = new Color(50, 50, 50);

    // menu
    buildMenu();

    // top
    pnlTop = new JPanel(); // buildTopPanel();
    pnlTop.setBackground(bgColor);

    // bottom
    pnlBottom = new JPanel();
    pnlBottom.setName("pnlBottom");
    pnlBottom.setBackground(bgColor);


    // content (radar) panel
    pnlContent = new JPanel();
    pnlContent.setName("pnlContent");
    pnlContent.setLayout(new BorderLayout());
    pnlContent.setBackground(bgColor);

    // left panel
    pnlLeft = new JPanel();
    pnlLeft.setName("pnlLeft");
    pnlLeft.setBackground(bgColor);
    pnlLeft.setLayout(new BorderLayout());

    // right panel
    pnlRight = new JPanel();
    pnlRight.setName("pnlRight");
    pnlRight.setBackground(bgColor);
    pnlRight.setLayout(new BorderLayout());

    // content pane

    LayoutManager.fillBorderedPanel(
        this.getContentPane(),
        pnlTop, pnlBottom, pnlLeft, pnlRight, pnlContent);

    pack();

  }

  private void buildCheckMenuItem(JMenu mnu, String label, boolean checkBoxState, Character charMnemonic, Consumer<JCheckBoxMenuItem> action) {
    JCheckBoxMenuItem item = new JCheckBoxMenuItem(label, checkBoxState);
    item.setName("mnu" + label);
    if (charMnemonic != null) item.setMnemonic(charMnemonic);
    mnu.add(item);
    if (action != null)
      item.addActionListener(e -> action.accept(item));
  }

  private void buildMenuItem(JMenu mnu, String label, Character charMnemonic, Consumer<JMenuItem> action) {
    JMenuItem item = new JMenuItem(label);
    item.setName("mnu" + label);
    if (charMnemonic != null) item.setMnemonic(charMnemonic);
    mnu.add(item);
    if (action != null)
      item.addActionListener(e -> action.accept(item));
  }

  private void buildMenu() {
    JMenuBar mnuBar = new JMenuBar();

    JMenu mnuFile = new JMenu("File");
    mnuFile.setMnemonic(KeyEvent.VK_F);
    mnuBar.add(mnuFile);

    JMenu mnuSimulation = new JMenu("Simulation");
    mnuSimulation.setMnemonic(KeyEvent.VK_S);
    mnuBar.add(mnuSimulation);

    JMenu mnuView = new JMenu("View");
    mnuView.setMnemonic(KeyEvent.VK_V);
    mnuBar.add(mnuView);

    JMenu mnuHelp = new JMenu("Help");
    mnuHelp.setMnemonic(KeyEvent.VK_H);
    mnuBar.add(mnuHelp);

    {
      buildMenuItem(mnuFile, "Save", 's', s -> this.saveSimulation());
      mnuFile.addSeparator();
      buildMenuItem(mnuFile, "Quit", 'q', s -> {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(false);
        System.exit(0);
      });
    }

    {
      buildMenuItem(mnuSimulation, "Pause/Resume", 'p', s -> {
        parent.getSim().pauseUnpauseSim();
      });
      JMenu mnuSpeed = new JMenu("Set speed");
      mnuSpeed.setMnemonic(KeyEvent.VK_S);
      mnuSimulation.add(mnuSpeed);
      {
        buildMenuItem(mnuSpeed, "Frozen (5000ms)", null, s -> setSimulationSpeed(5000));
        buildMenuItem(mnuSpeed, "Slow (2000ms)", null, s -> setSimulationSpeed(2000));
        buildMenuItem(mnuSpeed, "Real (1000ms)", null, s -> setSimulationSpeed(1000));
        buildMenuItem(mnuSpeed, "Accelerated (750ms)", null, s -> setSimulationSpeed(750));
        buildMenuItem(mnuSpeed, "Arcade (500ms)", null, s -> setSimulationSpeed(500));
        buildMenuItem(mnuSpeed, "Fast (250ms)", null, s -> setSimulationSpeed(250));
        buildMenuItem(mnuSpeed, "Skip (50ms)", null, s -> setSimulationSpeed(50));
      }
      mnuSimulation.addSeparator();
      buildCheckMenuItem(mnuSimulation, "Sounds", true, 's', s -> {
        SoundManager.switchEnabled();
        s.setState(SoundManager.isEnabled());
      });
      buildMenuItem(mnuSimulation, "Recording", 'r', s -> viewRecordingPanel());
    }

    {
      buildCheckMenuItem(mnuView, "Flight strips", true, 'f', s -> {
        boolean isVis = pnlLeft.isVisible();
        isVis = !isVis;
        pnlLeft.setVisible(isVis);
        s.setState(isVis);
      });
      buildCheckMenuItem(mnuView, "Command buttons", true, 'c', s -> {
        boolean isVis = pnlCommands.isVisible();
        isVis = !isVis;
        pnlCommands.setVisible(isVis);
        s.setState(isVis);
      });
      buildCheckMenuItem(mnuView, "Scheduled & Stats", true, 's', s -> {
        boolean isVis = pnlRight.isVisible();
        isVis = !isVis;
        pnlRight.setVisible(isVis);
        s.setState(isVis);
      });
      mnuView.addSeparator();
      buildMenuItem(mnuView, "Show mood results", null, s -> {
        MoodHistoryPanel pnl = new MoodHistoryPanel();
        pnl.init(Acc.sim().getStats().getFullMoodHistory());
        SwingFactory.show(pnl, "Rating board");
      });
      buildMenuItem(mnuView, "Show stats graphs", null, s -> {
        StatsGraphPanel pnl = new StatsGraphPanel();
        pnl.init(Acc.sim().getStats());
        SwingFactory.show(pnl, "Statistical graphs");
      });
      buildMenuItem(mnuView, "Add new radar view", 'r', s -> {
        FrmView f = new FrmView();
        f.init(this.parent);
        f.setVisible(true);
      });
    }

    {
      buildMenuItem(mnuHelp, "Project web pages", null, s -> {
        ProcessBuilder pb;
        String url = "https://github.com/Engin1980/J-ATC-Simulator/wiki";
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows"))
          pb = new ProcessBuilder("cmd", "/c", "start", url);
        else
          pb = new ProcessBuilder("xsd-open", url);
        try {
          pb.start();
        } catch (IOException e) {
          Acc.log().writeLine(ApplicationLog.eType.warning, "Failed to start project web pages." + ExceptionUtils.toFullString(e));
        }
      });
      mnuHelp.addSeparator();
      buildMenuItem(mnuHelp, "About", 'o', s -> new FrmAbout().setVisible(true));
    }

    Stylist.apply(mnuBar, true);
    this.setJMenuBar(mnuBar);
  }

  private void setSimulationSpeed(int intervalMs) {
    parent.getSim().setSimulationSecondInterval(intervalMs);
  }

  private void viewRecordingPanel() {
    RecordingPanel pnl;
    if (recording != null)
      pnl = new RecordingPanel(recording.getSettings());
    else
      pnl = new RecordingPanel(null);
    pnl.getRecordingStarted().add(q -> recording_recordingStarted(q));
    pnl.getRecordingStopped().add(() -> recording_recordingStopped());
    pnl.getViewRecordingFolderRequest().add(() -> recording_viewRecordingFolderRequest());
    SwingFactory.show(pnl, "Recording");
  }

  private void recording_viewRecordingFolderRequest() {
    String path = recording.getSettings().getPath();
    try {
      Desktop.getDesktop().open(new File(path));
    } catch (IOException e) {
      MessageBox.show("Unable to open target recording folder " + path + ". Reason: " + e.getMessage(), "Error opening explorer");
    }
  }

  private void recording_recordingStopped() {
    this.recording.stop();
    this.recording = null;
  }

  private void recording_recordingStarted(Settings q) {
    RadarBehaviorSettings bs = new RadarBehaviorSettings(false, new DebugFormatter());

    InitialPosition initPos = srpRadar.getRadar().getPosition();

    RadarDisplaySettings ds = this.parent.getAppSettings().radar.displaySettings.toRadarDisplaySettings();
    recording = new Recording(q,
        this.parent.getSim(), this.parent.getArea(), initPos, this.parent.getRadarStyleSettings(), ds, bs);
  }

  private void saveSimulation() {
    parent.getSim().stop();

    JFileChooser jf = SwingFactory.createFileDialog(SwingFactory.FileDialogType.game, lastFileName);
    int res = jf.showSaveDialog(this);
    if (res == JFileChooser.APPROVE_OPTION) {

      String fileName = jf.getSelectedFile().getAbsolutePath();

      if (!fileName.endsWith(SwingFactory.SAVED_SIMULATION_EXTENSION))
        fileName += SwingFactory.SAVED_SIMULATION_EXTENSION;

      IMap<String, Object> tmp = this.parent.getDataToStore();

      this.parent.getGame().save(fileName, tmp);
      lastFileName = fileName;
      this.parent.getSim().sendTextMessageForUser("Game saved.");
    }

    parent.getSim().start();
  }

  void init(Pack pack) {

    LayoutManager.setFixedWidth(pnlLeft, pack.getAppSettings().getLoadedFlightStripSettings().flightStripSize.width);
    LayoutManager.setFixedWidth(pnlRight, pack.getAppSettings().getLoadedFlightStripSettings().flightStripSize.width);

    this.parent = pack;

    SpeechFormatter formatter = SpeechFormatter.create(pack.getAppSettings().speechFormatterFile);
    RadarBehaviorSettings behSett = new RadarBehaviorSettings(true, formatter);
    RadarDisplaySettings dispSett = pack.getAppSettings().radar.displaySettings.toRadarDisplaySettings();

    this.srpRadar = new SwingRadarPanel();
    this.srpRadar.init(
        this.parent.getSim().getActiveAirport().getInitialPosition(),
        this.parent.getSim(), this.parent.getArea(),
        this.parent.getRadarStyleSettings(), dispSett, behSett
    );
    this.pnlContent.add(srpRadar);

    // Left panel
    this.flightListPanel = new FlightListPanel();
    this.flightListPanel.init(this.parent.getSim(), parent.getAppSettings());
    pnlLeft.add(flightListPanel, BorderLayout.CENTER);
    pnlCommands = new CommandButtonsPanel();
    pnlCommands.setVisible(false);
    pnlLeft.add(pnlCommands, BorderLayout.PAGE_END);

    // Right panel
    ScheduledFlightListPanel scheduledPanel = new ScheduledFlightListPanel();
    scheduledPanel.init(this.parent.getSim(), parent.getAppSettings());
    pnlRight.add(scheduledPanel, BorderLayout.CENTER);
    StatsPanel statsPanel = new StatsPanel();
    statsPanel.init(this.parent.getSim());
    pnlRight.add(statsPanel, BorderLayout.PAGE_END);

    srpRadar.getRadar().getSelectedAirplaneChangedEvent().add((sender, callsign) -> {
      flightListPanel.setSelectedCallsign((Callsign) callsign);
      pnlCommands.setPlane((Callsign) callsign);
    });
    flightListPanel.getSelectedCallsignChangedEvent().add((sender, callsign) -> {
      srpRadar.getRadar().setSelectedCallsign((Callsign) callsign);
      pnlCommands.setPlane((Callsign) callsign);
    });

    pnlCommands.getGeneratedEvent().add(s -> srpRadar.addCommandTextToLine(s));
    pnlCommands.getSendEvent().add(() -> srpRadar.sendCommand());
    pnlCommands.getEraseEvent().add(() -> srpRadar.eraseCommand());

    appendListenerForKeyToRadar();

    srpRadar.requestFocus();
  }
}
