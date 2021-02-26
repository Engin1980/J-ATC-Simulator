package eng.jAtcSim.newPacks;

import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.settings.AppSettings;

import javax.swing.*;

public interface IView {
  void init(JPanel panel, ISimulation simulation, AppSettings settings);
}
