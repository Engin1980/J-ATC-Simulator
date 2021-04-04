package eng.jAtcSim.newPacks;

import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyMap;
import eng.jAtcSim.newPacks.views.ViewInitInfo;

import javax.swing.*;

public interface IView {
  void init(JPanel panel, ViewInitInfo initInfo, IReadOnlyMap<String, String> options);

  default void postInit() {
  }
}
