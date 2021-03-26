package eng.jAtcSim.app;

import eng.eSystem.swing.LayoutManager;
import eng.eSystem.utilites.ExceptionUtils;
import eng.jAtcSim.JAtcSim;
import eng.jAtcSim.Stylist;
import eng.jAtcSim.app.extenders.swingFactory.FileHistoryManager;
import eng.jAtcSim.app.extenders.swingFactory.SwingFactory;
import eng.jAtcSim.app.startupSettings.FrmStartupSettings;
import eng.jAtcSim.app.startupSettings.StartupSettings;
import eng.jAtcSim.shared.MessageBox;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FrmIntro extends JFrame {

  private final StartupSettings startupSettings;

  public FrmIntro(StartupSettings startupSettings) {
    initializeComponents();
    this.setTitle("JAtcSim - Main menu");
    this.startupSettings = startupSettings;
  }

  public StartupSettings getStartupSettings() {
    return startupSettings;
  }

  private void initializeComponents() {

    JAtcSim.setAppIconToFrame(this);

    JLabel lblImage = JAtcSim.getAppImage(this);

    JButton btnStartupSettings = new JButton("Adjust app settings");
    btnStartupSettings.addActionListener(o -> btnStartupSettings_click());
    JButton btnRun = new JButton("Start simulation");
    btnRun.addActionListener(o -> btnRun_click());
    JButton btnLoadSim = new JButton("Load simulation");
    btnLoadSim.addActionListener(q -> btnLoadSim_click());
    JButton btnExit = new JButton("Quit");
    btnExit.addActionListener(o -> btnExit_click());


    JPanel pnl = LayoutManager.createBorderedPanel(16,
            LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.center, 16,
                    lblImage, btnStartupSettings, btnRun, btnLoadSim, btnExit));

    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(pnl);
    this.pack();
    this.setLocationRelativeTo(null);
  }


  private void btnStartupSettings_click() {
    FrmStartupSettings frm = new FrmStartupSettings();
    Stylist.apply(frm, true);
    frm.fillBySettings(this.startupSettings);
    SwingFactory.showDialog(frm, "Startup settings", this);

    if (frm.isDialogResultOk()) {
      frm.fillSettingsBy(this.startupSettings);
    }

  }

  private void btnRun_click() {
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    this.setVisible(false);
    try {
      JAtcSim.startSimulation(this.startupSettings);
    } catch (Exception ex) {
      ex.printStackTrace();
      MessageBox.show("Failed to start up the simulation. Something is wrong. Check the app settings. \n\n" +
              ExceptionUtils.toFullString(ex, "\n"), "Error during simulation start-up.");
      this.setVisible(true);
    }
  }

  private void btnLoadSim_click() {
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    JFileChooser jf = SwingFactory.createFileDialog(SwingFactory.FileDialogType.game, null);
    int res = jf.showOpenDialog(this);
    if (res != JFileChooser.APPROVE_OPTION) return;

    this.setVisible(false);
    try {
      JAtcSim.loadSimulation(this.startupSettings, jf.getSelectedFile().getAbsolutePath());

      // on successful load update files history
      FileHistoryManager.updateHistory(SwingFactory.FileDialogType.game.toString(), jf.getSelectedFile().toPath().toString());
    } catch (Exception ex) {
      ex.printStackTrace();
      MessageBox.show("Failed to load the simulation. \n\n" +
              ExceptionUtils.toFullString(ex, "\n"), "Error during simulation load.");
      this.setVisible(true);
    }
  }

  private void btnExit_click() {
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    this.setVisible(false);
    this.dispose();
    JAtcSim.quit();
  }
}

class ImagedPanel extends JPanel {
  private final Image bgImg;

  public ImagedPanel(String imageFileName) throws IOException {
    bgImg = ImageIO.read(new File(imageFileName));
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawImage(bgImg, 0, 0, g.getClipBounds().width, g.getClipBounds().height, null);
  }
}
