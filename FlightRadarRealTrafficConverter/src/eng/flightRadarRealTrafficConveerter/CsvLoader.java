package eng.flightRadarRealTrafficConveerter;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.geo.Coordinate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CsvLoader {
  public static IMap<String, String> loadCompanies(Path filePath) {
    List<String> lines = readFileLines(filePath);

    IMap<String, String> ret = new EMap<>();
    for (String line : lines) {
      String pts[] = line.split(";");
      ret.set(pts[0], pts[1]);
    }
    return ret;
  }

  public static String[] loadHereApiKeys(Path apiPath) {
    List<String> lines;
    try {
      lines = readFileLines(apiPath);
    } catch (Exception ex){
      return null;
    }

    String[] ret = new String[2];
    ret[0] = lines.get(0);
    ret[1] = lines.get(1);
    return ret;
  }

  private static List<String> readFileLines(Path filePath) {
    List<String> ret;

    try {
      ret = java.nio.file.Files.readAllLines(filePath);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read content of file " + filePath + ".", e);
    }

    return ret;
  }

  public static void saveCompanies(IMap<String, String> data, Path filePath) {
    Path tempPath = getTempFileName();

    List<String> lines = new ArrayList<>(data.size());

    for (Map.Entry<String, String> entry : data) {
      lines.add(String.format("%s;%s", entry.getKey(), entry.getValue()));
    }

    writeFileLines(lines, tempPath);
    copyFile(tempPath, filePath);
  }

  private static void writeFileLines(List<String> lines, Path tempPath) {
    try {
      java.nio.file.Files.write(tempPath, lines);
    } catch (IOException e) {
      throw new RuntimeException("Failed to save text to file " + tempPath + ".", e);
    }
  }

  public static IMap<String, eng.eSystem.geo.Coordinate> loadAirports(Path filePath) {
    List<String> lines = readFileLines(filePath);

    IMap<String, eng.eSystem.geo.Coordinate> ret = new EMap<>();
    for (String line : lines) {
      String pts[] = line.split(";");
      String name = pts[0];
      double lat = convertStringToDouble(pts[1]);
      double lng = convertStringToDouble(pts[2]);
      eng.eSystem.geo.Coordinate c = new Coordinate(lat, lng);
      ret.set(name, c);
    }
    return ret;
  }

  private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

  private static double convertStringToDouble(String txt) {
    NumberFormat nf = NumberFormat.getInstance(DEFAULT_LOCALE);
    Number tmp;

    try {
      tmp = nf.parse(txt);
    } catch (ParseException e) {
      throw new RuntimeException("Failed to convert " + txt + " into double value.", e);
    }

    return tmp.doubleValue();
  }

  public static void saveAirports(IMap<String, eng.eSystem.geo.Coordinate> data, Path filePath) {
    Path tempPath = getTempFileName();

    List<String> lines = new ArrayList<>(data.size());

    for (Map.Entry<String, eng.eSystem.geo.Coordinate> entry : data) {
      String lat = convertDoubleToString(entry.getValue().getLatitude().get());
      String lng = convertDoubleToString(entry.getValue().getLongitude().get());
      lines.add(String.format("%s;%s;%s", entry.getKey(), lat, lng));
    }

    writeFileLines(lines, tempPath);
    copyFile(tempPath, filePath);
    deleteFile(tempPath);
  }

  private static String convertDoubleToString(double value) {
    NumberFormat nf = NumberFormat.getInstance(DEFAULT_LOCALE);
    String ret;
    ret = nf.format(value);
    return ret;
  }

  private static void deleteFile(Path tempPath) {
    try {
      java.nio.file.Files.delete(tempPath);
    } catch (IOException e) {
      throw new RuntimeException("Failed to delete file " + tempPath + ".", e);
    }
  }

  private static Path getTempFileName() {
    File tmp = null;
    try {
      tmp = File.createTempFile(null, null);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create temporary file.", e);
    }
    Path ret = tmp.toPath();
    return ret;
  }

  private static void copyFile(Path tempPath, Path filePath) {
    try {
      java.nio.file.Files.copy(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException("Failed to copy " + tempPath + " to " + filePath + ".", e);
    }
  }
}
