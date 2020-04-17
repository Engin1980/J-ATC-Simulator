package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.speeches.ISpeech;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.SpeechList;

public class ApplicationResult {
  public Rejection rejection = null;
  public SpeechList<ISpeech> informations = new SpeechList<>();

  private static ApplicationResult empty = new ApplicationResult();
  public static ApplicationResult getEmpty(){
    return empty;
  }
}
