package eng.jAtcSim.newLib.fleet.generalAviation;

import eng.eSystem.collections.IList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.fleet.Fleets;

public class GeneralAviationFleets extends Fleets<CountryFleet> {
  public static GeneralAviationFleets create(IList<CountryFleet> countryFleets, CountryFleet defaultFleet) {
    return new GeneralAviationFleets(countryFleets, defaultFleet);
  }

  private GeneralAviationFleets(IList<CountryFleet> countryFleets, CountryFleet defaultFleet) {
    super(countryFleets, q -> q.getCountryCode(), defaultFleet);
    EAssert.Argument.isTrue(
        defaultFleet.getTypes().isEmpty() == false,
        "There must be at least one type in default general aviation fleet.");
  }
}
