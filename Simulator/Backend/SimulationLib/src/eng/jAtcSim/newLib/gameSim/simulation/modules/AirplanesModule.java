package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.AirproxController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.EmergencyAppearanceController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.MrvaController;
import eng.jAtcSim.newLib.mood.MoodManager;

public class AirplanesModule {
  private final AirproxController airproxController;
  private final EmergencyAppearanceController emergencyAppearanceController;
  private final MrvaController mrvaController;
  private final AirplanesController airplanesController;
  private final MoodManager moodManager;

  public AirplanesModule(AirplanesController airplanesController, AirproxController airproxController, MrvaController mrvaController, EmergencyAppearanceController emergencyAppearanceController, MoodManager moodManager) {
    EAssert.Argument.isNotNull(airplanesController, "airplanesController");
    EAssert.Argument.isNotNull(airproxController, "airproxController");
    EAssert.Argument.isNotNull(mrvaController, "mrvaController");
    EAssert.Argument.isNotNull(emergencyAppearanceController, "emergencyAppearanceController");
    EAssert.Argument.isNotNull(moodManager, "moodManager");

    this.airproxController = airproxController;
    this.emergencyAppearanceController = emergencyAppearanceController;
    this.mrvaController = mrvaController;
    this.airplanesController = airplanesController;
    this.moodManager = moodManager;
  }
}
