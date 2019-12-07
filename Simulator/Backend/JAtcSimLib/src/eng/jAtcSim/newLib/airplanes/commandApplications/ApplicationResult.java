package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.speaking.IFromAirplane;
import eng.jAtcSim.newLib.speaking.SpeechList;

public class ApplicationResult {
  public IFromAirplane rejection = null;
  public SpeechList<IFromAirplane> informations = new SpeechList<>();

  private static ApplicationResult empty = new ApplicationResult();
  public static ApplicationResult getEmpty(){
    return empty;
  }
}
