package eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.speeches.base.ISpeech;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public abstract class TextSpeechFormatter<T> {

  public abstract Class<? extends T> getSourceType();

  public abstract String format(T input);
}
