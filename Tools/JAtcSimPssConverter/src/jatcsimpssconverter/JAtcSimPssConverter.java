/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimpssconverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marek
 */
public class JAtcSimPssConverter {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    String rangeFile = "range.txt";
    String aipFile = "R:\\PSSAPT.dat";
    String vorFile = "R:\\PSSVOR.dat";
    String ndbFile = "R:\\PSSNDB.dat";
    String fixFile = "R:\\PSSWPT.dat";

    String outFile = "R:\\out.xml";

    RectangleF range = readRectangle(rangeFile);
    if (range == null) {
      return;
    }

    String[] lines;
    StringBuilder sb = new StringBuilder();

    sb.append("<!-- Airports -->\r\n");
    lines = readLinesOfFile(aipFile);
    sb.append(
        processFile(lines, "Airport", range));
    
    sb.append("<!-- VORs -->\r\n");
    lines = readLinesOfFile(vorFile);
    sb.append(
        processFile(lines, "VOR", range));
    
    sb.append("\r\n<!-- NDBs -->\r\n");
    lines = readLinesOfFile(ndbFile);
    sb.append(
        processFile(lines, "NDB", range));

    sb.append("\r\n<!-- FIXes -->\r\n");
    lines = readLinesOfFile(fixFile);
    sb.append(
        processFile(lines, "Fix", range));

    writeTextToFile(outFile, sb.toString());
  }

  private static RectangleF readRectangle(String rangeFile) {
    RectangleF ret = null;
    String[] lines = readLinesOfFile(rangeFile);
    if (lines == null || lines.length == 0) {
      System.out.println("Warning: Range-file " + rangeFile + " has no content. Default LKPR values will be used.");
      ret = new RectangleF(48.55, 51.08, 12.07, 18.85);
    } else {

      String[] pts = lines[0].split(" ");
      double mila;
      double mala;
      double milo;
      double malo;

      try {
        mila = Double.parseDouble(pts[0]);
        mala = Double.parseDouble(pts[1]);
        milo = Double.parseDouble(pts[2]);
        malo = Double.parseDouble(pts[3]);

        ret = new RectangleF(mila, mala, milo, malo);
      } catch (NumberFormatException ex) {
        System.out.println("Error: Failed to parse range rectangle int doubles: " + lines[0]);
        ret = null;
      }
    }

    return ret;
  }

  private static String[] readLinesOfFile(String rangeFile) {
    List<String> tmp = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(rangeFile))) {
      String line = br.readLine();
      while (line != null) {
        tmp.add(line);
        line = br.readLine();
      }
    } catch (IOException ex) {
      System.out.println("Error: Unable to read from " + rangeFile);
      return null;
    }

    String[] ret = new String[0];
    ret = tmp.toArray(ret);
    return ret;
  }

  private static Object processFile(String[] lines, String type, RectangleF range) {
    StringBuilder sb = new StringBuilder();
    for (String line : lines) {
      if (line.startsWith(";")) {
        continue;
      }
      String abbr;
      String latS;
      String lonS;

      if (type.equals("Airport")){
        abbr = line.substring(0, 5).trim();
        latS = line.substring(6, 15).trim();
        lonS = line.substring(15, 27).trim();
      }
      else if (type.equals("Fix")) {
        abbr = line.substring(0, 5).trim();
        latS = line.substring(6, 16).trim();
        lonS = line.substring(16).trim();
      } else { //VOR,NDB
        abbr = line.substring(0, 3).trim();
        latS = line.substring(5, 15).trim();
        lonS = line.substring(16, 26).trim();
      }

      double lat = Double.parseDouble(latS);
      double lon = Double.parseDouble(lonS);
      if (lat < range.minLat || lat > range.maxLat
          || lon < range.minLon || lon > range.maxLon) {
        continue;
      }

      String xt = type;
      if (type.equals("Fix")) {
        if (abbr.matches("^\\d.+")) {
          continue;
        } else if (abbr.matches(".+\\d.+")) {
          xt = "FixMinor";
        }
      }

      String xmlLine = String.format(
          "    <navaid name=\"%s\" type=\"%s\" coordinate=\"%s %s\" />",
          abbr, xt, latS, lonS);
      sb.append(xmlLine);
      sb.append("\r\n");
    }

    return sb.toString();
  }

  private static void writeTextToFile(String outFile, String toString) {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))) {
      bw.write(toString);
    } catch (IOException ex) {
      System.out.println("Error: failed to write output into " + outFile);
    }
  }

}

class RectangleF {

  public final double minLat;
  public final double maxLat;
  public final double minLon;
  public final double maxLon;

  public RectangleF(double minLat, double maxLat, double minLon, double maxLon) {
    this.minLat = minLat;
    this.maxLat = maxLat;
    this.minLon = minLon;
    this.maxLon = maxLon;
  }
}
