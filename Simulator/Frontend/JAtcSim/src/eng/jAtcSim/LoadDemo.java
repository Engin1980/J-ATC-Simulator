package eng.jAtcSim;

import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.textProcessing.formatting.SpeechFormatter;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;
import eng.jAtcSim.lib.traffic.fleets.Fleets;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.lib.world.xml.AltitudeValueParser;

import java.nio.file.Paths;

public class LoadDemo {

  public static void loadDemo() {
    String fileName = "C:\\Users\\Marek Vajgl\\Documents\\IdeaProjects\\J-ATC-Simulator\\_SettingFiles\\LKPR.ar.xml";

    Area ret;

    XmlSettings sett = new XmlSettings();
    sett.forType(int.class).setCustomParser(new AltitudeValueParser());
    sett.forType(Integer.class).setCustomParser(new AltitudeValueParser());
    XmlSerializer ser = new XmlSerializer(sett);

    ret = ser.deserialize(fileName, Area.class);

    System.out.println("done!");
  }

  public static void loadDemoAirplaneTypes() {
    String fileName = "C:\\Users\\Marek Vajgl\\Documents\\IdeaProjects\\J-ATC-Simulator\\_SettingFiles\\planeTypes.tp.xml";

    AirplaneTypes ret;

    XmlSerializer ser = new XmlSerializer();

    ret = ser.deserialize(fileName, AirplaneTypes.class);

    System.out.println("done!");
  }

  public static void loadDemoFleets() {
    String fileName = "C:\\Users\\Marek Vajgl\\Documents\\IdeaProjects\\J-ATC-Simulator\\_SettingFiles\\fleets.fl.xml";

    Fleets fl;

    XmlSerializer ser = new XmlSerializer();

    fl = ser.deserialize(fileName, Fleets.class);

    System.out.println("done!");
  }

  public static void demoSerializer() {
    String fileName = "C:\\Users\\Marek Vajgl\\Documents\\IdeaProjects\\J-ATC-Simulator\\_SettingFiles\\speechResponses.fm.xml";
    SpeechFormatter fmt = SpeechFormatter.create(Paths.get(fileName));

    GoodDayNotification gdn = new GoodDayNotification(new Callsign("CSA", "1234"),
        845, 5000, false, false);

    String s = fmt.format(gdn);
    System.out.println(s);
    System.out.println("done");
  }
}
