package eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.base.ISpeech;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class TextSpeechFormatterList<T extends ISpeech> {

  private final IMap<Class<?>, TextSpeechFormatter<? extends T>> inner = new EMap<>();

  public void add(TextSpeechFormatter<? extends T> formatter){
    Class<? extends T> type  = formatter.getSourceType();
    if (inner.containsKey(type)) inner.remove(type);
    inner.set(type, formatter);
  }

  public TextSpeechFormatter<? extends T> get(ISpeech input) {
    Class<? extends ISpeech> type = input.getClass();
    return getByType(type);
  }

  public TextSpeechFormatter<? extends T> getByType(Class<? extends ISpeech> type){
    TextSpeechFormatter<? extends T> ret = inner.tryGet(type);
    return ret;
  }
}
