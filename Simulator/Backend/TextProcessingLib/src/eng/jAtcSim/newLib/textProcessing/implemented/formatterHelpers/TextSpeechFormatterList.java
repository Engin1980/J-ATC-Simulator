package eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.base.ISpeech;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class TextSpeechFormatterList {

  private final IMap<Class<?>, TextSpeechFormatter<?>> inner = new EMap<>();

  public void add(TextSpeechFormatter<?> formatter){
    Class<?> type  = formatter.getSourceType();
    if (inner.containsKey(type)) inner.remove(type);
    inner.set(type, formatter);
  }

  public TextSpeechFormatter<?> get(Object input) {
    Class<?> type = input.getClass();
    return getByType(type);
  }

  public TextSpeechFormatter<?> getByType(Class<?> type){
    TextSpeechFormatter<?> ret = inner.tryGet(type);
    return ret;
  }
}
