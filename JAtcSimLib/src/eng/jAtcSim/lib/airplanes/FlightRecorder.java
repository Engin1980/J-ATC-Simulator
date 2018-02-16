/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.airplanes;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.global.EStringBuilder;
import eng.jAtcSim.lib.global.Recorder;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.global.ETime;

import java.nio.file.Path;

/**
 *
 * @author Marek Vajgl
 */
public class FlightRecorder extends Recorder {

  private final static int DEFAULT_STRING_BUILDER_SIZE = 256;
  private final static String SEPARATOR = ";";

  private final EStringBuilder sb = new EStringBuilder(DEFAULT_STRING_BUILDER_SIZE);

  public static FlightRecorder create(Callsign callsign, boolean logToConsole, boolean logToFile){
    Path filePath = null;

    if (logToFile) {
      filePath = Recorder.buildGenericLogFilePath(callsign.toString() + ".log");
    }
    
    FlightRecorder ret = new FlightRecorder(filePath);
    return ret;
  }

  private FlightRecorder(Path filePath) {
    super(filePath, false, true);
  }

  /**
   * Logs common information reported every second.
   *
   * @param plane Plane to log about.
   */
  void logFDR(Airplane plane, Pilot pilot) {
    ETime now = Acc.now();
    sb.clear();

    sb.append("FDR ").append(SEPARATOR);
    sb.appendFormat(" %s ", now.toString()).append(SEPARATOR);
    sb.appendFormat(" %s ", plane.getCallsign().toString()).append(SEPARATOR);

    // coord
    sb.appendFormat(" %20s  ", plane.getCoordinate().toString()).append(SEPARATOR);

    // heading
    sb.appendFormat(" H:%5s => %5s ", plane.getHeadingS(), plane.getTargetHeadingS()).append(SEPARATOR);

    // alt
    sb.appendFormat(" A:%7.0f (%5.0f) => %7d ", plane.getAltitude(), plane.getVerticalSpeed(), plane.getTargetAltitude()).append(SEPARATOR);

    // spd
    sb.appendFormat(" S:%5.0f (%5.0f) => %5d ", plane.getSpeed(), plane.getGS(), plane.getTargetSpeed()).append(SEPARATOR);
    sb.appendFormat("AT: " + pilot.getStateString()).append(SEPARATOR);
    
    // from pilot
    sb.appendFormat(" BEH: {%s} ", pilot.getBehaviorLogString());

    sb.appendLine();
    logLine(sb.toString());
  }

  public void logCVR(Message m) {
    sb.clear();

    String src = getMessageObjectString(m.getSource());
    String trg = getMessageObjectString(m.getTarget());
    String cnt = getMessageContentString(m.getContent());

    ETime now = Acc.now();
    sb.clear();

    sb.append("CVR ").append(SEPARATOR);
    sb.appendFormat(" %s ", now.toString()).append(SEPARATOR);
    sb.appendFormat("FROM: %s ", src).append(SEPARATOR);
    sb.appendFormat("TO: %s ", trg).append(SEPARATOR);
    sb.appendFormat(" %s ", cnt);

    sb.appendLine();
    logLine(sb.toString());
  }

}
