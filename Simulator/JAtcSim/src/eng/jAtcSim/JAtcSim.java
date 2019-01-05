/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.jAtcSim.app.extenders.swingFactory.FileHistoryManager;
import eng.jAtcSim.app.extenders.swingFactory.SwingFactory;
import eng.jAtcSim.frmPacks.Pack;
import eng.jAtcSim.frmPacks.shared.FrmLog;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.Game;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.logging.ApplicationLog;
import eng.jAtcSim.lib.global.logging.Recorder;
import eng.jAtcSim.lib.traffic.GenericTraffic;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.radarBase.global.SoundManager;
import eng.jAtcSim.app.FrmIntro;
import eng.jAtcSim.app.FrmStartupProgress;
import eng.jAtcSim.app.startupSettings.StartupSettings;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

/**
 * @author Marek
 */
public class JAtcSim {

  private static final boolean FAST_START = false;
  private static final Traffic enginSpecificTraffic =
       new eng.jAtcSim.lib.traffic.TestTrafficOneApproach();
       //new eng.jAtcSim.lib.traffic.TestTrafficOneDeparture();
       //null;
  private static AppSettings appSettings;

  private static FrmLog frmLog;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
//   LoadDemo.demoSerializer();

    AppSettings.init();

    initStylist();

    Acc.setLog(new ApplicationLog());
    frmLog = new FrmLog();

    appSettings = AppSettings.create();

    // various inits
    FileHistoryManager.init();
    Recorder.init(appSettings.logFolder.toString(), appSettings.speechFormatterFile);


    // startupSettings wizard
    StartupSettings startupSettings = XmlLoadHelper.loadStartupSettings(appSettings.startupSettingsFile.toString());

    FrmIntro frmIntro = new FrmIntro(startupSettings);
    Stylist.apply(frmIntro, true);
    frmIntro.setVisible(true);
  }

  public static void loadSimulation(StartupSettings startupSettings, String xmlFileName) {
    Acc.log().writeLine(ApplicationLog.eType.info, "Loading saved simulation game");

    IMap<String, Object> map = new EMap<>();

    FrmStartupProgress frm = new FrmStartupProgress(16);
    frm.setVisible(true);

    Game g;

    try {

      g = Game.load(xmlFileName, map);

      // enable duplicates
      try {
        g.getSimulation().getArea().checkForDuplicits();
      } catch (Exception ex) {
        throw new EApplicationException("Some element in source XML files is not unique. Some of the input XML files is not valid.", ex);
      }

      Acc.log().writeLine(ApplicationLog.eType.info, "Initializing sound environment");
      // sound
      SoundManager.init(appSettings.soundFolder.toString());

      Acc.log().writeLine(ApplicationLog.eType.info, "Starting a GUI");

    } catch (Exception ex) {
      throw ex;
    } finally {
      frm.setVisible(false);
    }

    // starting pack & simulation
    String packType = startupSettings.radar.packClass;
    Pack simPack = createPackInstance(packType);
    simPack.initPack(g, appSettings);
    simPack.startPack();

    simPack.applyStoredData(map);

  }

  public static void startSimulation(StartupSettings startupSettings) {

    try {
      resolveShortXmlFileNamesInStartupSettings(appSettings, startupSettings);
      XmlLoadHelper.saveStartupSettings(startupSettings, appSettings.startupSettingsFile.toString());
    } catch (EApplicationException ex) {
      throw new EApplicationException("Failed to normalize or save default settings.", ex);
    }

    FrmStartupProgress frm = new FrmStartupProgress(10);
    frm.setVisible(true);
    Acc.log().writeLine(ApplicationLog.eType.info, "Starting new simulation game");
    try {
      Game.GameStartupInfo gsi = new Game.GameStartupInfo();
      gsi.areaXmlFile = startupSettings.files.areaXmlFile;
      gsi.emergencyPerDayProbability = startupSettings.traffic.emergencyPerDayProbability;
      gsi.fleetsXmlFile = startupSettings.files.fleetsXmlFile;
      gsi.icao = startupSettings.recent.icao;
      gsi.planesXmlFile = startupSettings.files.planesXmlFile;
      gsi.secondLengthInMs = startupSettings.simulation.secondLengthInMs;
      if (enginSpecificTraffic != null) {
        gsi.specificTraffic = enginSpecificTraffic;
        gsi.trafficSourceType = Game.GameStartupInfo.SourceType.user;
      } else {
        if (startupSettings.traffic.type == StartupSettings.Traffic.eTrafficType.xml) {
          gsi.trafficSourceType = Game.GameStartupInfo.SourceType.xml;
          gsi.specificTraffic = null;
        } else {
          gsi.trafficSourceType = Game.GameStartupInfo.SourceType.user;
          gsi.specificTraffic = generateCustomTraffic(startupSettings.traffic);
        }
      }

      gsi.startTime = new ETime(startupSettings.recent.time);
      gsi.trafficXmlFile = startupSettings.files.trafficXmlFile;

      gsi.weatherXmlFile = startupSettings.files.weatherXmlFile;
      gsi.initialWeather = Weather.createClear();
      switch (startupSettings.weather.type) {
        case user:
          gsi.weatherProviderType = Game.GameStartupInfo.WeatherSourceType.user;
          break;
        case online:
          gsi.weatherProviderType = Game.GameStartupInfo.WeatherSourceType.online;
          break;
        case xml:
          gsi.weatherProviderType = Game.GameStartupInfo.WeatherSourceType.xml;
          break;
        default:
          throw new EEnumValueUnsupportedException(startupSettings.weather.type);
      }

      gsi.allowTrafficDelays = startupSettings.traffic.allowDelays;
      gsi.maxTrafficPlanes = startupSettings.traffic.maxPlanes;
      gsi.trafficDensityPercentage = startupSettings.traffic.densityPercentage;
      gsi.statsIntervalBlockSize = appSettings.stats.blockIntervalSize;

      Game g;
      g = Game.create(gsi);

      // enable duplicates
      try {
        g.getSimulation().getArea().checkForDuplicits();
      } catch (Exception ex) {
        throw new EApplicationException("Some element in source XML files is not unique. Some of the input XML files is not valid.", ex);
      }

      Acc.log().writeLine(ApplicationLog.eType.info, "Initializing sound environment");
      // sound
      SoundManager.init(appSettings.soundFolder.toString());

      Acc.log().writeLine(ApplicationLog.eType.info, "Starting a GUI");
      // starting pack & simulation
      String packType = startupSettings.radar.packClass;
      Pack simPack
          = createPackInstance(packType);

      simPack.initPack(g, appSettings);
      simPack.startPack();
    } catch (Exception ex) {
      throw ex;
    } finally {
      frm.setVisible(false);
    }
  }

  public static void quit() {

  }


  public static void setAppIconToFrame(JFrame frm) {
    setIconToFrame(frm, "icon.png");
  }

  public static void setIconToFrame(JFrame frm, String iconFileName) {
    URL url = frm.getClass().getResource("/" + iconFileName);
    Toolkit tk = frm.getToolkit();
    Image img = tk.getImage(url);
    frm.setIconImage(img);
  }

  public static JLabel getAppImage(JFrame frm) {
    URL url = frm.getClass().getResource("/intro.png");
    Toolkit tk = frm.getToolkit();
    Image img = tk.getImage(url);
    JLabel ret = new JLabel(new ImageIcon(img));
    return ret;
  }

  private static void initStylist() {
    initDefault();
    initIntro();
    initStartupSettings();
    initStartupProgress();
    initMainMenus();
  }

  private static void initMainMenus() {
    Stylist.add(
        "JMenuBar style",
        new Stylist.TypeFilter(JMenuBar.class, true),
        q -> {
          q.setBackground(Color.DARK_GRAY);
          q.setForeground(Color.LIGHT_GRAY);
        });
    Stylist.add(
        "JMenu style",
        new Stylist.TypeFilter(JMenu.class, false),
        q -> {
          q.setBackground(Color.DARK_GRAY);
          q.setForeground(Color.LIGHT_GRAY);
          ((JMenu) q).setOpaque(true);
        });
    Stylist.add(
        "JMenuItem style",
        new Stylist.TypeFilter(JMenuItem.class, false),
        q -> {
          q.setBackground(Color.DARK_GRAY);
          q.setForeground(Color.LIGHT_GRAY);
        });
    Stylist.add(
        "JMenuChecked style",
        new Stylist.TypeFilter(JCheckBoxMenuItem.class, false),
        q -> {
          q.setBackground(Color.DARK_GRAY);
          q.setForeground(Color.LIGHT_GRAY);
        });
  }

  private static void initStartupProgress() {
    Stylist.add(
        "Startup progress form background dark",
        new Stylist.TypeFilter(FrmStartupProgress.class, true),
        q -> q.setBackground(Color.DARK_GRAY)
    );

    Stylist.add(
        "Startup progress - panels dark",
        new Stylist.AndFilter(
            new Stylist.TypeFilter(javax.swing.JPanel.class, false),
            new Stylist.ParentTypeFilter(FrmStartupProgress.class, true)
        )
        ,
        q -> q.setBackground(Color.DARK_GRAY));
  }

  private static void initStartupSettings() {

    Stylist.add(
        "Startup settings - JPanel dark",
        new Stylist.TypeFilter(JPanel.class, true),
        q -> q.setBackground(Color.DARK_GRAY)
    );

    Stylist.add(
        "Startup settings - JPanel border style",
        new Stylist.TypeFilter(javax.swing.JPanel.class, true),
        q -> {
          javax.swing.JPanel p = (javax.swing.JPanel) q;
          TitledBorder b = (TitledBorder) p.getBorder();
          if (b != null) b.setTitleColor(Color.LIGHT_GRAY);
        });

    Stylist.add(
        "Startup settings - JLabel style",
        new Stylist.TypeFilter(JLabel.class, false),
        q -> {
          q.setForeground(Color.LIGHT_GRAY);
        }
    );

    Stylist.add(
        "Startup settings - JRadioButton style",
        new Stylist.TypeFilter(JRadioButton.class, false),
        q -> {
          q.setForeground(Color.LIGHT_GRAY);
          q.setBackground(Color.DARK_GRAY);
        }
    );
    Stylist.add(
        "Startup settings - JCheckBox style",
        new Stylist.TypeFilter(JCheckBox.class, false),
        q -> {
          q.setForeground(Color.LIGHT_GRAY);
          q.setBackground(Color.DARK_GRAY);
        }
    );
    Stylist.add(
        "Startup settings - JTextField style",
        new Stylist.TypeFilter(JTextField.class, false),
        q -> {
          JTextField txt = (JTextField) q;
          if (txt.isEditable() == false) {
            q.setForeground(Color.LIGHT_GRAY);
            q.setBackground(Color.DARK_GRAY);
          }
        }
    );
  }

  private static void initIntro() {
    // intro page
    Stylist.add(
        "Frm Intro - frm size",
        new Stylist.TypeFilter(FrmIntro.class, false),
        q -> {
          Dimension d = new Dimension(500, 420);
          q.setMinimumSize(d);
          q.setPreferredSize(d);
        });

    Stylist.add(
        "Frm Intro - JButton style",
        new Stylist.AndFilter(
            new Stylist.TypeFilter(javax.swing.JButton.class, false),
            new Stylist.ParentTypeFilter(FrmIntro.class, true)
        )
        ,
        q -> {
          Dimension d = new Dimension(450, 32);
          q.setPreferredSize(d);
          q.setMinimumSize(d);
          q.setMaximumSize(d);
        });

    Stylist.add(
        "Frm Intro - JPanel dark background style",
        new Stylist.AndFilter(
            new Stylist.TypeFilter(JPanel.class, true),
            new Stylist.ParentTypeFilter(FrmIntro.class, true)
        ),
        q -> q.setBackground(Color.DARK_GRAY)
    );
  }

  private static void initDefault() {
    // default theme
    Stylist.add(
        "Global - components font",
        new Stylist.TypeFilter(java.awt.Component.class, true),
        q -> {
          Font fnt = new Font("Verdana", 0, 12);
          q.setFont(fnt);
        });
  }

  private static void resolveShortXmlFileNamesInStartupSettings(AppSettings appSettings, StartupSettings
      startupSettings) {
    Path tmp;
    Path appPath;
    appPath = appSettings.getApplicationFolder();

    tmp = Paths.get(startupSettings.files.areaXmlFile);
    if (tmp.isAbsolute()) {
      tmp = appPath.relativize(tmp);
      startupSettings.files.areaXmlFile = tmp.toString();
    }

    tmp = Paths.get(startupSettings.files.fleetsXmlFile);
    if (tmp.isAbsolute()) {
      tmp = appPath.relativize(tmp);
      startupSettings.files.fleetsXmlFile = tmp.toString();
    }

    tmp = Paths.get(startupSettings.files.planesXmlFile);
    if (tmp.isAbsolute()) {
      tmp = appPath.relativize(tmp);
      startupSettings.files.planesXmlFile = tmp.toString();
    }

    if (startupSettings.files.trafficXmlFile != null) {
      tmp = Paths.get(startupSettings.files.trafficXmlFile);
      if (tmp.isAbsolute()) {
        tmp = appPath.relativize(tmp);
        startupSettings.files.trafficXmlFile = tmp.toString();
      }
    }

    if (startupSettings.files.weatherXmlFile != null) {
      tmp = Paths.get(startupSettings.files.weatherXmlFile);
      if (tmp.isAbsolute()) {
        tmp = appPath.relativize(tmp);
        startupSettings.files.weatherXmlFile = tmp.toString();
      }
    }
  }

  private static Pack createPackInstance(String packTypeName) {
    Class<?> clazz;
    Object object;
    try {
      clazz = Class.forName(packTypeName);
      Constructor<?> ctor = clazz.getConstructor();
      object = ctor.newInstance();
    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
      throw new ERuntimeException(
          sf("Failed to create instance of radar pack '%s'.", packTypeName), ex);
    }
    Pack ret = (Pack) object;
    return ret;
  }

  private static GenericTraffic generateCustomTraffic(StartupSettings.Traffic trf) {
    GenericTraffic ret = new GenericTraffic(
        trf.customTraffic.companies, trf.customTraffic.countryCodes,
        trf.customTraffic.movementsPerHour,
        trf.customTraffic.arrivals2departuresRatio / 10d,
        trf.customTraffic.nonCommercialFlightProbability,
        trf.customTraffic.weightTypeA,
        trf.customTraffic.weightTypeB,
        trf.customTraffic.weightTypeC,
        trf.customTraffic.weightTypeD,
        trf.customTraffic.useExtendedCallsigns);
    return ret;
  }
}

