package eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers;

import eng.jAtcSim.newLib.speeches.base.ISpeech;

public abstract class TextSpeechFormatter<T extends ISpeech> {

  public abstract String format(ISpeech input);

  public abstract Class<? extends T> getSourceType();
}
