package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.SpeechList;

public class ApplicationResult {
  public PlaneRejection rejection = null;
  public SpeechList<IFromPlaneSpeech> informations = new SpeechList<>();

  private static ApplicationResult empty = new ApplicationResult();
  public static ApplicationResult getEmpty(){
    return empty;
  }
}
