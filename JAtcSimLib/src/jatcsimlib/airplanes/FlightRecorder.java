/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.airplanes;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.pilots.Pilot;
import jatcsimlib.atcs.Atc;
import jatcsimlib.commands.Command;
import jatcsimlib.commands.CommandList;
import jatcsimlib.commands.formatting.Formatter;
import jatcsimlib.commands.formatting.Formatters;
import jatcsimlib.commands.formatting.LongFormatter;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.EStringBuilder;
import jatcsimlib.global.ETime;
import jatcsimlib.messaging.IContent;
import jatcsimlib.messaging.Message;
import jatcsimlib.messaging.StringMessage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Marek Vajgl
 */
public class FlightRecorder {

  private final static int DEFAULT_STRING_BUILDER_SIZE = 256;
  private final static String SEPARATOR = ";";

  public final static String logPath = "R:\\jatcsim\\FDRs\\";
  private static String logPathDate = null;

  private final boolean isConsole;
  private final boolean isFile;
  private final Path filePath;
  private BufferedWriter wrt;
  private final EStringBuilder sb = new EStringBuilder(DEFAULT_STRING_BUILDER_SIZE);

  public FlightRecorder(Callsign callsign, boolean logToConsole, boolean logToFile) {
    this.isConsole = logToConsole;

    if (logToFile) {
      if (logPath == null) {
        throw new ERuntimeException("Cannot initialize flight recorder. FlightRecorder.logPath property must be set first.");
      }
      filePath = buildLogFileName(logPath, callsign);
      isFile = true;
      openFile();
    } else {
      filePath = null;
      isFile = false;
    }
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
    sb.appendFormat(" A:%7d (%5d) => %7d ", plane.getAltitude(), plane.getVerticalSpeed(), plane.getTargetAltitude()).append(SEPARATOR);

    // spd
    sb.appendFormat(" S:%5d => %5d ", plane.getSpeed(), plane.getTargetSpeed()).append(SEPARATOR);

    // from pilot
    sb.appendFormat(" HLD {%s} ", pilot.getHoldLogString()).append(SEPARATOR);
    sb.appendFormat(" APP {%s} ", pilot.getApproachLogString()).append(SEPARATOR);
    sb.appendFormat(" SPD {%s} ", pilot.getSpeedLogString());

    sb.appendLine();
    logLine(sb.toString());
  }

  private void logLine(String line) throws ERuntimeException {
    if (isFile) {
      try {
        wrt.write(line);
        wrt.flush();
      } catch (IOException ex) {
        throw new ERuntimeException("Failed to write to flight recorder FDR - file " + filePath.toString(), ex);
      }
    }

    if (isConsole) {
      System.out.print(sb.toString());
    }
  }

  public void close() {
    closeFile();
  }

  private void openFile() {

    Path dir = filePath.getParent();
    try {
      java.nio.file.Files.createDirectories(dir);
    } catch (IOException ex) {
      throw new ERuntimeException("Failed to create directory for flight recorder. Required directory: " + dir.toString(), ex);
    }
    try {
      java.nio.file.Files.deleteIfExists(filePath);
    } catch (IOException ex) {
      throw new ERuntimeException("Failed to try delete existing flight recorder. Tested file: " + filePath.toString(), ex);
    }

    java.io.FileWriter fw;
    try {
      fw = new FileWriter(filePath.toFile());
    } catch (IOException ex) {
      throw new ERuntimeException("Failed to open flight recorder file: " + filePath.toString(), ex);
    }

    wrt = new BufferedWriter(fw);

  }

  private void closeFile() {
    try {
      wrt.flush();
      wrt.close();
      wrt = null;
    } catch (IOException ex) {
      throw new ERuntimeException("Failed to close flight recorder file " + filePath.toString(), ex);
    }
  }

  private static Path buildLogFileName(String logPath, Callsign callsign) {
    if (logPathDate == null) {
      Date d = new Date();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
      logPathDate = sdf.format(d);
    }
    Path ret = Paths.get(logPath, logPathDate, callsign.toString() + ".log");
    return ret;
  }

  public void logCVR(Message m) {
    sb.clear();

    String src = getMessageObjectString(m.source);
    String trg = getMessageObjectString(m.target);
    String cnt = getMessageContentString(m.content);

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

  private String getMessageObjectString(Object object) {
    if (object == Message.SYSTEM) {
      return "<SYSTEM>";
    } else if (object instanceof Atc) {
      Atc atc = (Atc) object;
      return atc.getName();
    } else if (object instanceof Airplane) {
      Airplane plane = (Airplane) object;
      return plane.getCallsign().toString();
    } else {
      throw new ENotSupportedException("Unknown object type.");
    }
  }

  private static Formatter fmt = new LongFormatter();

  private String getMessageContentString(IContent content) {
    if (content instanceof StringMessage) {
      return ((StringMessage) content).text;
    } else if (content instanceof CommandList) {
      CommandList cmds = (CommandList) content;
      EStringBuilder sb = new EStringBuilder();
      for (Command cmd : cmds) {
        sb.append(Formatters.format(cmd, fmt)).append(", ");
      }
      return sb.toString();
    } else if (content instanceof Command) {
      return Formatters.format((Command) content, fmt);
    } else {
      throw new ERuntimeException("Message content cannot be get for type " + content.getClass().getName());
    }
  }
}
