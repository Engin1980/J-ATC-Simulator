package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.SpeechList;

public class ApplicationResult {
  public IFromAirplane rejection = null;
  public SpeechList<IFromAirplane> informations = new SpeechList<>();

  private static ApplicationResult empty = new ApplicationResult();
  public static ApplicationResult getEmpty(){
    return empty;
  }
}
