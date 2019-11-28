package eng.jatcsim.routeTranslator;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

  private static Map<String, String> maps;
  private static Scanner scanner = new Scanner(System.in);

  public static void main(String[] args) {

    String line = readLine();

    while (line != null && !line.equals("")) {
      String xml = processLine(line);
      System.out.println("Converted as:");
      System.out.println();
      System.out.println(xml);
      System.out.println();
      System.out.println();
      line = readLine();
    }

  }

  private static String processLine(String line) {
    StringBuilder ret = new StringBuilder();
    while (line.length() > 0) {
      Mapping mapping = findMapping(line);
      if (mapping == null) {
        throw new RuntimeException("Unable to find mapping matching " + line);
      } else {
        String tmp = applyMapping(mapping);
        ret.append(tmp);
        ret.append("\n\r");

        line = cutUsedPartOfMapping(line, mapping);
        line = line.trim();
      }
    }
    return ret.toString();
  }

  private static String cutUsedPartOfMapping(String line, Mapping mapping) {
    String found = mapping.matcher.group(0);
    int len = found.length();
    String ret = line.substring(len);
    return ret;
  }

  private static String applyMapping(Mapping mapping) {
    StringBuilder ret = new StringBuilder(mapping.rewritePattern);
    while (ret.indexOf(":") >= 0) {
      int location = ret.indexOf(":");
      char c = ret.charAt(location + 1);
      String s = Character.toString(c);
      int groupIndex = Integer.parseInt(s);

      String tmp = mapping.matcher.group(groupIndex);
      ret.delete(location, location + 2);
      ret.insert(location, tmp);
    }
    return ret.toString();
  }

  private static Mapping findMapping(String line) {
    Mapping ret = null;
    for (String key : maps.keySet()) {
      String tmp = "^" + key;
      Pattern pattern = Pattern.compile(tmp);

      Matcher matcher = pattern.matcher(line);
      if (matcher.find()) {
        ret = new Mapping();
        ret.lookForPattern = key;
        ret.rewritePattern = maps.get(key);
        ret.matcher = matcher;
        break;
      }
    }
    return ret;
  }

  private static String readLine() {
    System.out.println("Write command or leave empty for exit:");
    String ret = scanner.nextLine();
    ret = ret.trim();
    return ret;
  }

  static {
    maps = new HashMap<>();
    maps.put("PD (\\S+)", "<proceedDirect fix=\":1\" />");

    maps.put("SL (\\d{3})", "<speed restriction=\"below\" value=\":1\"/>");
    maps.put("SM (\\d{3})", "<speed restriction=\"above\" value=\":1\"/>");
    maps.put("SC", "<speed restriction=\"clear\" />");
    maps.put("FH (\\d{3})", "<heading value=\":1\" direction=\"nearest\" />");
    maps.put("TL (\\d{3})", "<heading value=\":1\" direction=\"left\" />");
    maps.put("TR (\\d{3})", "<heading value=\":1\" direction=\"right\" />");

    maps.put("AA (\\d(\\d+)\\+)", "<after property=\"altitude\" value=\":200+\" />");
    maps.put("AA (\\d(\\d+)+\\-)", "<after property=\"altitude\" value=\":200-\" />");
    maps.put("AA (\\d(\\d+)+)", "<after property=\"altitude\" value=\":200\" />");
    maps.put("AD (\\S+)", "<after property=\"distance\" value=\":1\" />");
    maps.put("AR (\\S+)", "<after property=\"radial\" value=\":1\" />");
    maps.put("AN (\\S+)", "<after property=\"navaid\" value=\":1\" />");
    maps.put("AL (\\d+)", "<altitudeRestriction restriction=\"above\" value=\":100\" />");
    maps.put("AM (\\d+)", "<altitudeRestriction restriction=\"below\" value=\":100\" />");
    maps.put("AE (\\d+)", "<altitudeRestriction restriction=\"exactly\" value=\":100\" />");
    maps.put("AC", "<altitudeRestrictionClear />");

    maps.put("DM (\\d{2,3})", "<altitude direction=\"descend\" value=\":100\" />");
    maps.put("CM (\\d{2,3})", "<altitude direction=\"climb\" value=\":100\" />");

    maps.put("H (\\S+)", "<hold fix=\":1\" />");
    maps.put("T", "<then />");
  }
}

class Mapping {
  String lookForPattern;
  String rewritePattern;
  Matcher matcher;
}
