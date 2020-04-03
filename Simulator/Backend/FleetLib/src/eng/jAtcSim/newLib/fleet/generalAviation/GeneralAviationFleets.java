package eng.jAtcSim.newLib.fleet.generalAviation;

import eng.eSystem.collections.*;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.fleet.airliners.CompanyFleet;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class GeneralAviationFleets {
  public static GeneralAviationFleets create(IList<CountryFleet> countryFleets, CountryFleet defaultFleet) {
    return new GeneralAviationFleets(countryFleets, defaultFleet);
  }

  private GeneralAviationFleets(IList<CountryFleet> countryFleets, CountryFleet defaultFleet) {
    EAssert.Argument.isNotNull(countryFleets, "countryFleets");
    EAssert.Argument.isNotNull(defaultFleet, "defaultFleet");
    EAssert.Argument.isTrue(
        defaultFleet.getTypes().isEmpty() == false,
        "There must be at least one type in default general aviation fleet.");
    this.countryFleets = countryFleets;
    this.defaultFleet = defaultFleet;
  }

  private final IList<CountryFleet> countryFleets;
  private final CountryFleet defaultFleet;
}
