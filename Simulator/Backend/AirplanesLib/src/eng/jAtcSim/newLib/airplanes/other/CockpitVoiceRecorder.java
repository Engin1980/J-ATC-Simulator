package eng.jAtcSim.newLib.airplanes.other;

import eng.eSystem.EStringBuilder;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.logging.Log;
import eng.jAtcSim.newLib.shared.logging.writers.*;

public class CockpitVoiceRecorder extends AirplaneRecorder {

  private final ILogWriter writer;

  public CockpitVoiceRecorder(Callsign callsign) {
    super(callsign);
    this.writer = new RealTimePipeLogWriter(
        new SimTimePipeLogWriter(
            new AutoNewLineLogWriter(
                new FileWriter("R:\\" + callsign.toString() + ".cvr.txt"))));
  }

  private final static int DEFAULT_STRING_BUILDER_SIZE = 256;
  private final static String SEPARATOR = ";";

  private final EStringBuilder sb = new EStringBuilder(DEFAULT_STRING_BUILDER_SIZE);

  public static FlightDataRecorder create(Callsign callsign) {
    String fileName = AirplaneRecorder.getRecorderFileName(
        callsign.toString(false) + ".log");
    FlightDataRecorder ret = new FlightDataRecorder(
        callsign, new FileSaver(fileName));
    return ret;
  }

  private FlightDataRecorder(Callsign cls, AbstractSaver os) {
    super(cls.toString(), os, SEPARATOR);
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

    super.writeLine(sb.toString());
  }

  /**
   * Logs common information reported every second.
   *
   * @param plane Plane to write about.
   */
  void logFDR(Airplane plane) {
    ETime now = Acc.now();
    sb.clear();

    sb.append("FDR ").append(SEPARATOR);
    sb.appendFormat(" %s ", now.toString()).append(SEPARATOR);
    sb.appendFormat(" %s ", plane.getFlightModule().getCallsign().toString()).append(SEPARATOR);

    // coord
    sb.appendFormat(" %20s  ", plane.getCoordinate().toString()).append(SEPARATOR);

    // heading
    sb.appendFormat(" H:  %3d => %03d/%03d  ", plane.getSha().getHeading(),
        plane.getSha().getTargetHeading(),
        (int) Headings.add(plane.getSha().getTargetHeading(), -Acc.airport().getDeclination())
    ).append(SEPARATOR);

    // alt
    sb.appendFormat(" A:%7.0f (%5.0f) => %7d ",
        plane.getSha().getAltitude(),
        plane.getSha().getVerticalSpeed(),
        plane.getSha().getTargetAltitude())
        .append(SEPARATOR);

    // spd
    sb.appendFormat(" S:%5.0f (%5.0f) => %5d ",
        plane.getSha().getSpeed(),
        plane.getSha().getGS(),
        plane.getSha().getTargetSpeed())
        .append(SEPARATOR);
    sb.appendFormat("%-20s", plane.getState().toString()).append(SEPARATOR);

    // from pilot
    sb.appendFormat(" BEH: {%s} ", plane.getBehaviorModule().get().toLogString());

    super.writeLine(sb.toString());
  }

  public void logProcessedCurrentSpeeches(SpeechList current) {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine("Current processed speeches");
    for (int i = 0; i < current.size(); i++) {
      ISpeech sp = current.get(i);
      sb.append("\t").append(sp.toString()).appendLine();
    }
    super.writeLine(sb.toString());
  }

  public void logProcessedAfterSpeeches(SpeechList<IAtcCommand> cmds, String extensions) {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine("Processed after speeches of " + extensions);
    for (int i = 0; i < cmds.size(); i++) {
      IAtcCommand cmd = cmds.get(i);
      sb.appendLine("\t").appendLine(cmd.toString()).appendLine();
    }
    super.writeLine(sb.toString());
  }

}
