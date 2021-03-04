package eng.jAtcSim.newPacks;

import eng.jAtcSim.newPacks.views.ViewInitInfo;

import javax.swing.*;

public interface IView {
  void init(JPanel panel, ViewInitInfo initInfo);
}
