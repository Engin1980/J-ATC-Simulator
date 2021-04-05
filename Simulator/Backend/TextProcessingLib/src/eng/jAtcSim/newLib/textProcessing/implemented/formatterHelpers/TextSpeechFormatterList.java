package eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.base.ISpeech;

public class TextSpeechFormatterList<T extends ISpeech> {

  private final IMap<Class<?>, TextSpeechFormatter<? extends T>> inner = new EMap<>();

  public void add(TextSpeechFormatter<? extends T> formatter){
    Class<? extends T> type  = formatter.getSourceType();
    if (inner.containsKey(type)) inner.remove(type);
    inner.set(type, formatter);
  }

  public TextSpeechFormatter<? extends T> tryGet(ISpeech input) {
    Class<? extends ISpeech> type = input.getClass();
    return tryGetByType(type);
  }

  public TextSpeechFormatter<? extends T> tryGetByType(Class<? extends ISpeech> type){
    TextSpeechFormatter<? extends T> ret = inner.tryGet(type).orElse(null);
    return ret;
  }
}
