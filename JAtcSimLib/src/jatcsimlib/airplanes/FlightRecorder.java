/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.airplanes;

import jatcsimlib.Acc;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.EStringBuilder;
import jatcsimlib.global.ETime;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marek Vajgl
 */
public class FlightRecorder {

  private final static int DEFAULT_STRING_BUILDER_SIZE = 256;
  private final static String SEPARATOR = ";\t";
  
  private final boolean isConsole;
  private final boolean isFile;
  private final Path filePath;
  private BufferedWriter wrt;
  private final EStringBuilder sb = new EStringBuilder(DEFAULT_STRING_BUILDER_SIZE);
  

  public FlightRecorder(Callsign callsign, boolean logToConsole) {
    this(callsign, logToConsole, null);
  }

  public FlightRecorder(Callsign callsign, String logPath) {
    this(callsign, false, logPath);
  }

  public FlightRecorder(Callsign callsign, boolean logToConsole, String logPath) {
    this.isConsole = logToConsole;
    if (logPath != null) {
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
   * @param plane Plane to log about.
   */
  public void logFDR (Airplane plane){
    ETime now = Acc.now();
    sb.clear();
    
    sb.append(now.toString()).append(SEPARATOR);
    sb.append(plane.getCallsign().toString()).append(SEPARATOR);
    sb.appendFormat("H:%s", plane.getHeadingS()).append(SEPARATOR);
    sb.appendFormat("A:%d (%d)", plane.getAltitude()/100, plane.getVerticalSpeed()).append(SEPARATOR);
    sb.appendFormat("S:%d", plane.getSpeed()).append(SEPARATOR);
    sb.appendLine();
    
    if (isFile){
      try{
      wrt.write(sb.toString());
      } catch (IOException ex){
        throw new ERuntimeException("Failed to write to flight recorder FDR - file " + filePath.toString(), ex);
      }
    }
    
    if (isConsole){
      System.out.print(sb.toString());
    }
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
    Date d = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
    Path ret = Paths.get(logPath, sdf.format(d), callsign.toString() + ".log");
    return ret;
  }
}
