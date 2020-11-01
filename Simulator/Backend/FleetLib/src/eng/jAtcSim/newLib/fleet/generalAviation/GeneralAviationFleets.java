package eng.jAtcSim.newLib.fleet.generalAviation;

import eng.eSystem.collections.IList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.fleet.Fleets;

public class GeneralAviationFleets extends Fleets<CountryFleet> {
  public static GeneralAviationFleets create(IList<CountryFleet> fleets, CountryFleet defaultFleet) {
    return new GeneralAviationFleets(fleets, defaultFleet);
  }

  private GeneralAviationFleets(IList<CountryFleet> fleets, CountryFleet defaultFleet) {
    super(fleets, q -> q.getCountryCode(), defaultFleet);
    EAssert.Argument.isTrue(
        defaultFleet.getTypes().isEmpty() == false,
        "There must be at least one type in default general aviation fleet.");
  }
}
