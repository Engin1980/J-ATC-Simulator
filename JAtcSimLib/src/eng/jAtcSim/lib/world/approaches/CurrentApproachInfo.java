package eng.jAtcSim.lib.world.approaches;

import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.RunwayThreshold;

public class CurrentApproachInfo {
  private RunwayThreshold threshold;
  private SpeechList<IFromAtc> iafRoute;
  private SpeechList<IFromAtc> gaRoute;
  private Approach.ApproachType type;
  private Navaid faf;
  private Coordinate mapt;
  private int course;
  private int decisionAltitude;

  public CurrentApproachInfo(RunwayThreshold threshold, SpeechList<IFromAtc> iafRoute, SpeechList<IFromAtc> gaRoute, Approach.ApproachType type, Navaid faf, Coordinate mapt, int course, int decisionAltitude) {
    this.threshold = threshold;
    this.iafRoute = iafRoute;
    this.gaRoute = gaRoute;
    this.type = type;
    this.faf = faf;
    this.mapt = mapt;
    this.course = course;
    this.decisionAltitude = decisionAltitude;
  }

  public RunwayThreshold getThreshold() {
    return threshold;
  }

  public SpeechList<IFromAtc> getIafRoute() {
    return iafRoute;
  }

  public SpeechList<IFromAtc> getGaRoute() {
    return gaRoute;
  }

  public Approach.ApproachType getType() {
    return type;
  }

  public Navaid getFaf() {
    return faf;
  }

  public Coordinate getMapt() {
    return mapt;
  }

  public int getCourse() {
    return course;
  }

  public int getDecisionAltitude() {
    return decisionAltitude;
  }
}
