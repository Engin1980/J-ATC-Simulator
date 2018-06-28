package eng.jAtcSim.frmPacks.sdi;

import eng.eSystem.collections.IMap;
import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.frmPacks.shared.*;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.speaking.formatting.LongFormatter;
import eng.jAtcSim.lib.world.InitialPosition;
import eng.jAtcSim.radarBase.BehaviorSettings;
import eng.jAtcSim.radarBase.RadarViewPort;
import eng.jAtcSim.radarBase.global.SoundManager;
import eng.jAtcSim.recording.Recording;
import eng.jAtcSim.recording.Settings;
import eng.jAtcSim.shared.LayoutManager;
import eng.jAtcSim.shared.MessageBox;
import eng.jAtcSim.startup.extenders.SwingFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class FrmMain extends JFrame {

  private static final String SOUND_OFF_LABEL = "Sound off";
  private static final String SOUND_ON_LABEL = "Sound on";
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
  private JButton btnSound;

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

    this.setPreferredSize(new Dimension(1000, 600));
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setTitle("SDI");

    Color bgColor = new Color(50, 50, 50);

    // top
    pnlTop = buildTopPanel();
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
    pnlLeft.setPreferredSize(new Dimension(200, 200));

    // right panel
    pnlRight = new JPanel();
    pnlRight.setName("pnlRight");
    pnlRight.setBackground(bgColor);
    pnlRight.setLayout(new BorderLayout());
    pnlRight.setPreferredSize(new Dimension(200, 200));

    // content pane

    LayoutManager.fillBorderedPanel(
        this.getContentPane(),
        pnlTop, pnlBottom, pnlLeft, pnlRight, pnlContent);

    pack();

  }

  private JPanel buildTopPanel() {

    JButton btnStrips = new JButton("Strips");
    adjustJComponentColors(btnStrips);
    btnStrips.addActionListener(o -> {
      boolean isVis = pnlLeft.isVisible();
      isVis = !isVis;
      pnlLeft.setVisible(isVis);
    });

    JButton btnCommands = new JButton("Cmds");
    adjustJComponentColors(btnCommands);
    btnCommands.addActionListener(o -> {
      boolean isVis = pnlCommands.isVisible();
      isVis = !isVis;
      pnlCommands.setVisible(isVis);
    });

    JButton btnMovs = new JButton("Movs & Stats");
    adjustJComponentColors(btnMovs);
    btnMovs.addActionListener(o -> {
      boolean isVis = pnlRight.isVisible();
      isVis = !isVis;
      pnlRight.setVisible(isVis);
    });

    JButton btnPause = new JButton("Pause");
    adjustJComponentColors(btnPause);
    btnPause.addActionListener(o -> {
      if (parent.getSim().isRunning()) {
        parent.getSim().stop();
        btnPause.setText("Resume");
      } else {
        parent.getSim().start();
        btnPause.setText("Pause");
      }
    });

    JButton btnSave = new JButton("Save");
    adjustJComponentColors(btnSave);
    btnSave.addActionListener(o -> saveSimulation());

    JButton btnView = new JButton("Add view");
    adjustJComponentColors(btnView);
    btnView.addActionListener(o -> {
      FrmView f = new FrmView();
      f.init(this.parent);
      f.setVisible(true);
    });

    JButton btnRecording = new JButton("Recording");
    adjustJComponentColors(btnRecording);
    btnRecording.addActionListener(q -> btnRecording_click());

    btnSound = new JButton(SOUND_OFF_LABEL);
    adjustJComponentColors(btnSound);
    btnSound.addActionListener(this::btnSound_click);

    JPanel ret = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, 4,
        btnStrips, btnCommands, btnMovs, btnPause, btnSave, btnView, btnRecording);
    ret.setName("pnlTop");
    return ret;
  }


  private void btnSound_click(ActionEvent actionEvent) {
    SoundManager.switchEnabled();
    btnSound.setText(SoundManager.isEnabled() ? SOUND_OFF_LABEL : SOUND_ON_LABEL);
  }

  private void btnRecording_click() {
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
    BehaviorSettings bs = new BehaviorSettings(false, new LongFormatter());

    InitialPosition initPos = srpRadar.getRadar().getPosition();

    recording = new Recording(q,
        this.parent.getSim(), this.parent.getArea(), initPos, this.parent.getDisplaySettings(), bs);
  }

  private void saveSimulation() {
    JFileChooser jf = SwingFactory.createFileDialog(SwingFactory.FileDialogType.game, lastFileName);
    int res = jf.showSaveDialog(this);
    if (res != JFileChooser.APPROVE_OPTION) return;

    String fileName = jf.getSelectedFile().getAbsolutePath();

    IMap<String, Object> tmp = this.parent.getDataToStore();

    this.parent.getGame().save(fileName, tmp);
    lastFileName = fileName;
  }

  private void adjustJComponentColors(JComponent component) {
    component.setBackground(new Color(50, 50, 50));
    component.setForeground(new Color(200, 200, 200));
  }

  void init(Pack pack) {

    this.parent = pack;

    // radar
    BehaviorSettings behSett = new BehaviorSettings(true, new LongFormatter());
    this.srpRadar = new SwingRadarPanel();
    this.srpRadar.init(
        this.parent.getSim().getActiveAirport().getInitialPosition(),
        this.parent.getSim(), this.parent.getArea(),
        this.parent.getDisplaySettings(), behSett
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
  }


}
