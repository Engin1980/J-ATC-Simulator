package eng.jAtcSim.newLib.fleet.generalAviation;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.fleet.TypeAndWeight;

public class CountryFleet {
  private final String countryCode;
  private final String aircraftPrefix;
  private final String name;
  private final IList<TypeAndWeight> types;

  public static CountryFleet create(String countryCode, String aircraftPrefix, String name, IList<TypeAndWeight> types) {
    return new CountryFleet(countryCode, aircraftPrefix, name, types);
  }

  public CountryFleet(String countryCode, String aircraftPrefix, String name, IList<TypeAndWeight> types) {
    EAssert.Argument.isNonemptyString(countryCode, "countryCode");
    EAssert.Argument.isNonemptyString(aircraftPrefix, "aircraftPrefix");
    EAssert.Argument.isNotNull(types, "types");
    EAssert.Argument.isNonemptyString(name, "name");

    this.name = name;
    this.countryCode = countryCode;
    this.aircraftPrefix = aircraftPrefix;
    this.types = types;
  }

  public String getName() {
    return name;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public String getAircraftPrefix() {
    return aircraftPrefix;
  }

  public IList<TypeAndWeight> getTypes() {
    return types;
  }
}
