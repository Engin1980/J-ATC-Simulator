package eng.jAtcSim.newLib.textProcessing.text;

import eng.jAtcSim.newLib.speeches.base.ISpeech;
import eng.jAtcSim.newLib.textProcessing.base.IParser;

public interface ITextParser<TTarget extends ISpeech> extends IParser<String, TTarget> {
  String getHelp();

  String getHelp(String commandPrefix);

  ShortcutList getShortcuts();

}
