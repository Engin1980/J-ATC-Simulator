package eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.*;

import java.util.regex.Pattern;

public class TextParsing {
  public static IList<String> tokenize(String line) {
    IList<String> ret;
    if (line == null || line.isEmpty()) {
      return new EList<>();
    }
    line = line.toUpperCase();
    String[] pts = line.split(" ");
    ret = EList.of(pts);
    ret.remove(q -> q.length() == 0);
    return ret;
  }

  public static String toLineString(IReadOnlyList<String> processed) {
    EStringBuilder ret = new EStringBuilder();
    ret.appendItems(processed, " ");
    return ret.toString();
  }

  public static<T> IList<String> getInterestingBlocks(IList<String> toDo, IList<String> done,
                                                   TextSpeechParser<? extends T> p) {
    IList<String> ret;
    int index = getMatchingPatternIndex(toDo, p);
    if (index < 0) {
      ret = null;
    } else {
      //moving pattern from "toDo" to "done" and to "ret"
      ret = new EList<>();
      for (int i = 0; i < p.getPatterns()[index].length; i++) {
        String s = toDo.get(0);
        toDo.removeAt(0);
        done.add(s);
        ret.add(s);
      }
      if (p.ifMatchCollectAllThatLeft()){
        done.addMany(toDo);
        ret.addMany(toDo);
        toDo.clear();
      }
    }
    return ret;
  }

  private static <T> int getMatchingPatternIndex(IList<String> toDo, TextSpeechParser<? extends T> parser) {
    int ret = -1;

    for (int i = 0; i < parser.getPatterns().length; i++) {
      String[] pattern = parser.getPatterns()[i];

      if (pattern.length > toDo.size()) continue; // pattern longer than input

      ret = i;
      for (int j = 0; j < pattern.length; j++) {
        Pattern p = Pattern.compile("^(" + pattern[j] + ")$");
        String pt = toDo.get(j);
        if (p.matcher(pt).find() == false) { // some part of pattern does not fit
          ret = -1;
          break;
        }
      }

      if (ret != -1) break;
    }

    return ret;
  }
}
