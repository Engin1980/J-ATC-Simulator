package eng.jAtcSim.newLib.airplanes.other;

import eng.eSystem.EStringBuilder;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.area.AreaAcc;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.logging.Journal;
import eng.jAtcSim.newLib.shared.logging.writers.*;

/**
 * @author Marek Vajgl
 */
public class FlightDataRecorder extends AirplaneRecorder {

  private final static int DEFAULT_STRING_BUILDER_SIZE = 256;
  private final static String SEPARATOR = ";";
  private final Journal journal;

  public FlightDataRecorder(Callsign callsign) {
    super(callsign);
    ILogWriter wrt = new FileWriter("R:\\" + callsign.toString() + ".fdr.txt");
    wrt = new AutoNewLineLogWriter(wrt);
    wrt = new SimTimePipeLogWriter(wrt);
    wrt = new RealTimePipeLogWriter(wrt);

    this.journal = new Journal(callsign.toString() + " (CVR)", true, wrt);
  }

  public void log(Coordinate coordinate,
                  int heading, int targetHeading,
                  int altitude, int verticalSpeed, int targetAltitude,
                  int speed, int groundSpeed, int targetSpeed,
                  AirplaneState state) {
    EStringBuilder sb = new EStringBuilder(DEFAULT_STRING_BUILDER_SIZE);
    sb.appendFormat(" %s ", this.callsign.toString()).append(SEPARATOR);

    // coord
    sb.appendFormat(" %20s  ", coordinate.toString()).append(SEPARATOR);

    // heading
    sb.appendFormat(" H:  %3d => %03d/%03d  ", heading,
        targetHeading,
        (int) Headings.add(heading, -AreaAcc.getAirport().getDeclination())
    ).append(SEPARATOR);

    // alt
    sb.appendFormat(" A:%7.0f (%5.0f) => %7d ",
        altitude,
        verticalSpeed,
        targetAltitude)
        .append(SEPARATOR);

    // spd
    sb.appendFormat(" S:%5.0f (%5.0f) => %5d ",
        speed,
        groundSpeed,
        targetSpeed)
        .append(SEPARATOR);

    // state
    sb.appendFormat("%-20s", state.toString()).append(SEPARATOR);

//    // from pilot
//    sb.appendFormat(" BEH: {%s} ", plane.getBehaviorModule().get().toLogString());

    journal.writeLine(sb.toString());
  }
}
