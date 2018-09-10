package eng.jAtcSim;

import eng.eSystem.xmlSerialization.Log;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.traffic.fleets.Fleets;
import eng.jAtcSim.lib.world.Area;

public class LoadDemo {

  public static void loadDemo() {
    String fileName = "C:\\Users\\Marek Vajgl\\Documents\\IdeaProjects\\J-ATC-Simulator\\_SettingFiles\\LKPR.ar.xml";

    Area ret;

    XmlSerializer ser = new XmlSerializer();

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
}
