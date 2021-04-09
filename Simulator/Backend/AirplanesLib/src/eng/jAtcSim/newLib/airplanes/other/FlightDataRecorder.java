package eng.jAtcSim.newLib.airplanes.other;

import eng.eSystem.EStringBuilder;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.Navigator;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.logging.LogFile;

public class FlightDataRecorder extends AirplaneRecorder {

  private final static int DEFAULT_STRING_BUILDER_SIZE = 256;
  private final static String SEPARATOR = ";";
  private final LogFile logFile;

  public FlightDataRecorder(Callsign callsign) {
    super(callsign);
    this.logFile = LogFile.openInDefaultPath(callsign.toString() + ".fdr.txt");
  }

  public void log(Coordinate coordinate,
                  int heading, int targetHeading,
                  int altitude, int verticalSpeed, int targetAltitude,
                  int speed, int groundSpeed, int targetSpeed,
                  AirplaneState state, Navigator navigator) {
    EStringBuilder sb = new EStringBuilder(DEFAULT_STRING_BUILDER_SIZE);

    sb.appendFormat("%s \t", Context.getShared().getNow().toString());

    sb.appendFormat(" %s ", this.callsign.toString()).append(SEPARATOR);

    // coord
    sb.appendFormat(" %20s  ", coordinate.toString()).append(SEPARATOR);

    // heading
    sb.appendFormat(" H:  %3d => %03d/%03d  ", heading,
            targetHeading,
            (int) Headings.add(heading, -Context.getArea().getAirport().getDeclination())
    ).append(SEPARATOR);

    // alt
    sb.appendFormat(" A:%7d (%5d) => %7d ",
            altitude,
            verticalSpeed,
            targetAltitude)
            .append(SEPARATOR);

    // spd
    sb.appendFormat(" S:%5d (%5d) => %5d ",
            speed,
            groundSpeed,
            targetSpeed)
            .append(SEPARATOR);

    // state
    sb.appendFormat("%-20s", state.toString()).append(SEPARATOR);

    // nav
    sb.appendFormat("%-30s", navigator.toString());

//    // from pilot
//    sb.appendFormat(" BEH: {%s} ", plane.getBehaviorModule().get().toLogString());

    logFile.write(sb.toString());
  }
}
