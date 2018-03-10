package eng.jAtcSim.startup.startupWizard;

import eng.jAtcSim.startup.StartupSettings;

import javax.swing.*;
import java.awt.*;

public abstract class JWizardPanel extends JPanel {

  protected static final Dimension BUTTON_DIMENSION = new Dimension(150, 1);
  protected static final Dimension FILE_FIELD_DIMENSION = new Dimension(500, 1);
  protected static final Dimension LARGE_FRAME_FIELD_DIMENSION = new Dimension(900, 1);
  protected static final int DISTANCE = 4;

  abstract boolean doWizardValidation();

  abstract void fillBySettings(StartupSettings settings);
  abstract void fillSettingsBy(StartupSettings settings);
}
