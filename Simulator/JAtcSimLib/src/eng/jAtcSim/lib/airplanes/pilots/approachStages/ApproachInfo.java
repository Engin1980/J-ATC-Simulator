package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
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

  public static IList<AltitudeEvent> generateAltitudeEvents(Approach.ApproachType type, ActiveRunwayThreshold threshold) {
    int sfa = 200;
    int lfa = (type == Approach.ApproachType.visual) ? 500 : 1000;
    sfa += threshold.getParent().getParent().getAltitude();
    lfa += threshold.getParent().getParent().getAltitude();
    IList<AltitudeEvent> ret = new EList<>();
    ret.add(new AltitudeEvent(lfa, eAltitudeBasedEvent.longFinal));
    ret.add(new AltitudeEvent(sfa, eAltitudeBasedEvent.shortFinal));
    return ret;
  }
  private IList<IApproachStage> stages;
  private IList<AltitudeEvent> altitudeEvents;
  private ActiveRunwayThreshold threshold;
  private SpeechList gaRoute;
  private Approach.ApproachType type;

  public ApproachInfo(Approach.ApproachType type,
                      ActiveRunwayThreshold threshold,
                      IList<IApproachStage> stages,
                      IList<AltitudeEvent> altitudeEvents,
                      SpeechList gaRoute) {
    this.stages = stages;
    this.altitudeEvents = altitudeEvents;
    this.altitudeEvents.sort(q->q.altitude);
    this.threshold = threshold;
    this.gaRoute = gaRoute;
    this.type = type;
  }

  public ApproachInfo(Approach.ApproachType type,
                      ActiveRunwayThreshold threshold,
                      IList<IApproachStage> stages,
                      SpeechList gaRoute) {
    this(type,
        threshold,
        stages,
        generateAltitudeEvents(type, threshold),
        gaRoute);
  }

  public ActiveRunwayThreshold getThreshold() {
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
    FollowRadialStage stage = (FollowRadialStage) stages.tryGetLast(q -> q instanceof FollowRadialStage);
    if (stage == null)
      return null;
    else
      return stage.getLowerCoordinate();
  }

  public Coordinate getFaf() {
    FollowRadialStage stage = (FollowRadialStage) stages.tryGetFirst(q -> q instanceof FollowRadialStage);
    if (stage == null)
      return null;
    else
      return stage.getUpperCoordinate();
  }

  public double getCourse() {
    FollowRadialStage stage = (FollowRadialStage) stages.tryGetFirst(q -> q instanceof FollowRadialStage);
    if (stage == null)
      return this.threshold.getCourse();
    else
      return stage.getCourse();
  }
}
