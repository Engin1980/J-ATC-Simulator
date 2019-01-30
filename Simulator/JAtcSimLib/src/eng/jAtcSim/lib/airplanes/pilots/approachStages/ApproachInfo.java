package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.world.RunwayThreshold;

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

  public RunwayThreshold getThreshold() {
    return threshold;
  }

  public SpeechList getGaRoute() {
    return gaRoute;
  }
}
