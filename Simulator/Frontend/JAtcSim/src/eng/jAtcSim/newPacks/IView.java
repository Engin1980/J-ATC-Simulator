package eng.jAtcSim.newPacks;

import eng.eSystem.collections.IReadOnlyMap;
import eng.jAtcSim.newPacks.context.ViewContext;
import eng.jAtcSim.newPacks.utils.ViewGameInfo;

import javax.swing.*;

public interface IView {
  void init(JPanel panel,
            ViewGameInfo initInfo,
            IReadOnlyMap<String, String> options,
            ViewContext context);

  default void postInit() {
  }
}
