package eng.jAtcSim.newLib.area.xml;

import eng.eSystem.collections.IList;

public interface ISanitizer {
  void checkSanity(IList<SanityResult> results);
}
