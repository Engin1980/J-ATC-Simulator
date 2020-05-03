package eng.jAtcSim.newLib.airplanes.other;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.EStringBuilder;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.area.AreaAcc;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.logging.writers.*;

import java.io.IOException;

/**
 * @author Marek Vajgl
 */
public class FlightDataRecorder extends AirplaneRecorder {

  private final ILogWriter writer;
  private final static int DEFAULT_STRING_BUILDER_SIZE = 256;
  private final static String SEPARATOR = ";";

  public FlightDataRecorder(Callsign callsign) {
    super(callsign);
    this.writer = new RealTimePipeLogWriter(
        new SimTimePipeLogWriter(
            new AutoNewLineLogWriter(
                new FileWriter("R:\\" + callsign.toString() + ".fdr.txt"))));
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

    try {
      writer.writeLine(sb.toString());
    } catch (IOException e) {
      logErrorToAppLog(new IOException(this.callsign.toString() + " FDR write failed.", e));
    }
  }
}
