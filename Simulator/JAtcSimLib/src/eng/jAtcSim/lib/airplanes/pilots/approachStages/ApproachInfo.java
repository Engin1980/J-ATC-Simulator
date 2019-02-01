package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.world.RunwayThreshold;
import eng.jAtcSim.lib.world.approaches.Approach;

public class ApproachInfo {
  public static class AltitudeEvent implements Comparable<AltitudeEvent> {
    private int altitude;
    private eAltitudeBasedEvent event;

    public AltitudeEvent(int altitude, eAltitudeBasedEvent event) {
      this.altitude = altitude;
      this.event = event;
    }

    @Override
    public int compareTo(AltitudeEvent o) {
      return Integer.compare(this.altitude, o.altitude);
    }

    public int getAltitude() {
      return altitude;
    }

    public eAltitudeBasedEvent getEvent() {
      return event;
    }
  }

  public enum eAltitudeBasedEvent {
    longFinal,
    shortFinal
  }

  public static final double DEGREES_TO_RADS = .0174532925;
  private final static int DEFAULT_LONG_FINAL_ALTITUDE_AGL = 1000;
  private final static int DEFAULT_SHORT_FINAL_ALTITUDE_AGL = 200;

  private IList<IApproachStage> stages;
  private IList<AltitudeEvent> altitudeEvents;
  private RunwayThreshold threshold;
  private SpeechList gaRoute;
  private Approach.ApproachType type;

  public RunwayThreshold getThreshold() {
    return threshold;
  }

  public SpeechList getGaRoute() {
    return gaRoute;
  }

  public IReadOnlyList<IApproachStage> getStages() {
    return stages;
  }

  public Approach.ApproachType getType() {
    return type;
  }

  public boolean isUsingIafRoute() {
    boolean ret = stages.getFirst() instanceof RouteStage;
    return ret;
  }

  public Coordinate getMapt() {
    FollowRadialStage stage = (FollowRadialStage) stages.tryGetLast(q->q instanceof FollowRadialStage);
    if (stage == null)
      return null;
    else
      return stage.getLowerCoordinate();
  }

  public Coordinate getFaf() {
    FollowRadialStage stage = (FollowRadialStage) stages.tryGetLast(q->q instanceof FollowRadialStage);
    if (stage == null)
      return null;
    else
      return stage.getUpperCoordinate();
  }

  public double getCourse() {
    return
  }
}
