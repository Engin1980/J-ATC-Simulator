package eng.jAtcSim.lib.world.approaches;

import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.global.UnitProvider;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.world.RunwayThreshold;

public class CurrentApproachInfo {
  private RunwayThreshold threshold;
  private SpeechList<IFromAtc> iafRoute;
  private SpeechList<IFromAtc> gaRoute;
  private Approach.ApproachType type;
  private Coordinate faf;
  private Coordinate mapt;
  private int course;
  private int decisionAltitude;
  private double glidePathPercentage = 3;

  public CurrentApproachInfo(RunwayThreshold threshold, SpeechList<IFromAtc> iafRoute, SpeechList<IFromAtc> gaRoute, Approach.ApproachType type, Coordinate faf, Coordinate mapt, int course, int decisionAltitude) {
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

  public Coordinate getFaf() {
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

  public double getGlidePathPerNM() {
    return UnitProvider.nmToFt(Math.tan(glidePathPercentage * Math.PI / 180));
  }

  public double getAltitudeDeltaPerSecond(double gs){
    // add glidePathPercentage here somehow
    return gs * 5;
  }

  public boolean willUseIafRouting() {
    return !this.iafRoute.isEmpty();
  }
}
