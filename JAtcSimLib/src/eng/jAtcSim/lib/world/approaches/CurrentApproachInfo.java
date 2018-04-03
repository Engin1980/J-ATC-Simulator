package eng.jAtcSim.lib.world.approaches;

import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
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
  private double slope;
  private double faf2maptCourse;
  private boolean usingIafRoute;

  public boolean isUsingIafRoute() {
    return usingIafRoute;
  }

  public CurrentApproachInfo(RunwayThreshold threshold, boolean usingIafRoute, SpeechList<IFromAtc> iafRoute, SpeechList<IFromAtc> gaRoute, Approach.ApproachType type, Coordinate faf, Coordinate mapt, int course, int decisionAltitude, double slope) {
    this.threshold = threshold;
    this.usingIafRoute = usingIafRoute;
    this.iafRoute = iafRoute;
    this.gaRoute = gaRoute;
    this.type = type;
    this.faf = faf;
    this.mapt = mapt;
    this.course = course;
    this.decisionAltitude = decisionAltitude;
    this.slope = slope;
    this.faf2maptCourse = Coordinates.getBearing(this.faf, this.mapt );
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

  public double getSlope() {
    return slope;
  }

  public double getAltitudeDeltaPerSecond(double gs){
    // add glidePathPercentage here somehow
    return gs * 5;
  }

  public double getFaf2MaptCourse() {
    return this.faf2maptCourse;
  }
}
