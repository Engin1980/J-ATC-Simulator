/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.global;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.Callsign;
import jatcsimlib.atcs.Atc;
import jatcsimlib.atcs.PlaneSwitchMessage;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.newMessaging.App;
import jatcsimlib.newMessaging.IMessageContent;
import jatcsimlib.newMessaging.IMessageParticipant;
import jatcsimlib.newMessaging.StringMessageContent;
import jatcsimlib.speaking.commands.Command;
import jatcsimlib.speaking.commands.CommandList;
import jatcsimlib.speaking.formatting.Formatter;
import jatcsimlib.speaking.formatting.LongFormatter;

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
public abstract class Recorder {

  public static final String GENERIC_LOG_PATH = "R:\\jatcsim\\FDRs\\";
  private static final String logPathDate = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new Date());

  private final boolean isConsole;

  private final boolean isFile;
  private final Path filePath;
  private BufferedWriter wrt;

  public Recorder(Path filePath, boolean toConsole, boolean autoOpenFile) {
    this.isFile = (filePath != null);
    this.filePath = filePath;
    this.isConsole = toConsole;
    if (autoOpenFile) {
      openFile();
    }
  }

  protected static Path buildGenericLogFilePath(String fileName) {
    Path ret = Paths.get(GENERIC_LOG_PATH, logPathDate, fileName);
    return ret;
  }

  public final void logLine(String line) throws ERuntimeException {
    if (isFile) {
      try {
        wrt.write(line);
        wrt.flush();
      } catch (IOException ex) {
        throw new ERuntimeException("Failed to write to flight recorder FDR - file " + filePath.toString(), ex);
      }
    }

    if (isConsole) {
      System.out.print(line);
    }
  }

  public final void open() {
    if (isFile && wrt == null) {
      openFile();
    }
  }

  public final void close() {
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

  private static Formatter fmt = new LongFormatter();

  protected String getMessageContentString(IMessageContent content) {
    if (content instanceof StringMessageContent) {
      return ((StringMessageContent) content).getMessageText();
    } else if (content instanceof CommandList) {
      CommandList cmds = (CommandList) content;
      EStringBuilder sb = new EStringBuilder();
      for (Command cmd : cmds) {
        sb.append(fmt.format(cmd)).append(", ");
      }
      return sb.toString();
    } else if (content instanceof Command) {
      return fmt.format((Command) content);
    } else if (content instanceof PlaneSwitchMessage) {
      PlaneSwitchMessage m = (PlaneSwitchMessage) content;
      return m.getAsString();
    } else {
      throw new ERuntimeException("Message content cannot be get for type " + content.getClass().getName());
    }
  }

  protected String getMessageObjectString(IMessageParticipant object) {
    if (object == App.me()) {
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
