/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.global.logging;

import eng.eSystem.EStringBuilder;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.atcs.PlaneSwitchMessage;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.messaging.IMessageContent;
import eng.jAtcSim.lib.messaging.IMessageParticipant;
import eng.jAtcSim.lib.messaging.Messenger;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.formatting.Formatter;
import eng.jAtcSim.lib.speaking.formatting.LongFormatter;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Confirmation;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Marek Vajgl
 */
public abstract class Recorder extends SimulationLog {

  private static final String logPathDate = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new Date());
  private static String logPathBase = null;
  private static Formatter fmt = new LongFormatter();
  private final OutputStream os;

  public static void init(String folder) {
    if (folder == null) {
      throw new IllegalArgumentException("Value of {folder} cannot not be null.");
    }
    Recorder.logPathBase = folder;
  }

  public static OutputStream createRecorderFileOutputStream(String name) {

    Path full = Paths.get(logPathBase, logPathDate, name);

    Path parent = full.getParent();
    try {
      java.nio.file.Files.createDirectories(parent);
    } catch (IOException ex) {
      throw new ERuntimeException("Failed to create directory for flight recorder. Required directory: " + parent.toString(), ex);
    }
    try {
      java.nio.file.Files.deleteIfExists(full);
    } catch (IOException ex) {
      throw new ERuntimeException("Failed to try delete existing flight recorder. File: " + full.toString(), ex);
    }

    OutputStream fw;
    try {
      fw = new FileOutputStream(full.toFile());
    } catch (IOException ex) {
      throw new ERuntimeException("Failed to open flight recorder file: " + full.toString(), ex);
    }
    return fw;
  }

  public Recorder(String recorderName, OutputStream os, String fromTimeSeparator) {
    super(
        recorderName,
        os);
    super.setAddSimulationTime(true);
    super.setSimulationTimeSeparator(fromTimeSeparator);
    this.os = os;
  }

  public void close() {
    try {
      os.close();
    } catch (IOException e) {
      throw new ERuntimeException("Failed to close recorder " + super.getName());
    }
  }

  @Override
  protected void writeLine(String format, Object... params) {
    super.writeLine(format, params);
  }

  protected String getMessageContentString(IMessageContent content) {
    if (content instanceof StringMessageContent) {
      return ((StringMessageContent) content).getMessageText();
    } else if (content instanceof SpeechList) {
      SpeechList<ISpeech> cmds = (SpeechList) content;
      EStringBuilder sb = new EStringBuilder();
      for (ISpeech cmd : cmds) {
        sb.append(fmt.format(cmd)).append(", ");
      }
      return sb.toString();
    } else if (content instanceof IAtcCommand) {
      return fmt.format((ICommand) content);
    } else if (content instanceof PlaneSwitchMessage) {
      PlaneSwitchMessage m = (PlaneSwitchMessage) content;
      return m.getAsString();
    } else if (content instanceof RadarContactConfirmationNotification) {
      return "{Computer ATC} Radar contact confirmation.";
    } else if (content instanceof GoingAroundNotification) {
      return "Going around. " + ((GoingAroundNotification) content).getReason();
    } else if (content instanceof Confirmation) {
      return "Confirmation (???)" + content.toString();
    } else {
      throw new ERuntimeException("Message content cannot be get for type " + content.getClass().getName());
    }
  }

  protected String getMessageObjectString(IMessageParticipant object) {
    if (object == Messenger.SYSTEM) {
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
}
