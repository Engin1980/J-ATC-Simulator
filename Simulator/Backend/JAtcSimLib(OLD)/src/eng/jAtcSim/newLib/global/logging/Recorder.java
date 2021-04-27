/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.area.global.logging;

import eng.eSystem.EStringBuilder;
import eng.eSystem.exceptions.ApplicationException;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.IMessageParticipant;
import eng.jAtcSim.newLib.messaging.Messenger;
import eng.jAtcSim.newLib.messaging.StringMessageContent;
import eng.jAtcSim.newLib.area.speaking.ICommand;
import eng.jAtcSim.newLib.area.speaking.ISpeech;
import eng.jAtcSim.newLib.area.speaking.SpeechList;
import eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications.commandResponses.Confirmation;
import eng.jAtcSim.newLib.area.speaking.fromAtc.IAtc2Atc;
import eng.jAtcSim.newLib.area.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.newLib.area.speaking.fromAtc.atc2atc.PlaneSwitchMessage;
import eng.jAtcSim.newLib.area.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import eng.jAtcSim.newLib.area.textProcessing.formatting.IFormatter;
import eng.jAtcSim.newLib.area.textProcessing.formatting.SpeechFormatter;
import eng.jAtcSim.newLib.atcs.Atc;
import eng.jAtcSim.newLib.textProcessing.formatting.IFormatter;
import eng.jAtcSim.newLib.textProcessing.formatting.SpeechFormatter;
import eng.jAtcSim.newLib.speaking.fromAtc.atc2atc.PlaneSwitchMessage;
import eng.jAtcSim.newLib.speaking.ICommand;
import eng.jAtcSim.newLib.speaking.ISpeech;
import eng.jAtcSim.newLib.speaking.SpeechList;
import eng.jAtcSim.newLib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.newLib.speaking.fromAirplane.notifications.commandResponses.Confirmation;
import eng.jAtcSim.newLib.speaking.fromAtc.IAtc2Atc;
import eng.jAtcSim.newLib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.newLib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Marek Vajgl
 */
public abstract class Recorder extends SimulationLog {

  private static String logPathBase = null;
  private static IFormatter fmt = null;

  public static void init(String folder, Path speechFormaterXmlSourceFilePath) {
    if (folder == null) {
      throw new IllegalArgumentException("Value of {folder} cannot not be null.");
    }
    if (speechFormaterXmlSourceFilePath == null) {
      throw new IllegalArgumentException("Xml source file for Speech formatter initialization is not set.");
    }
    Recorder.logPathBase = folder;
    fmt = SpeechFormatter.create(speechFormaterXmlSourceFilePath.toAbsolutePath());
  }

  public static String getRecorderFileName(String name) {

    Path full = Paths.get(logPathBase, name);

    Path parent = full.getParent();
    try {
      java.nio.file.Files.createDirectories(parent);
    } catch (IOException ex) {
      throw new ApplicationException("Failed to create directory for flight recorder. Required directory: " + parent.toString(), ex);
    }
    try {
      java.nio.file.Files.deleteIfExists(full);
    } catch (IOException ex) {
      throw new ApplicationException("Failed to try delete existing flight recorder. File: " + full.toString(), ex);
    }

    return full.toString();
  }

  public Recorder(String recorderName, AbstractSaver os, String fromTimeSeparator) {
    super(
        recorderName,
        os);
    super.setAddSimulationTime(true);
    super.setSimulationTimeSeparator(fromTimeSeparator);
  }

  public void close() {
    super.close();
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
    } else if (content instanceof IAtc2Atc) {
      return "{ATC->ATC}" + content.toString();
    } else {
      throw new UnsupportedOperationException("Message content cannot be getContent for kind " + content.getClass().getName());
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
      return plane.getFlightModule().getCallsign().toString();
    } else {
      throw new UnsupportedOperationException("Unknown object {" + object + "}.");
    }
  }
}
