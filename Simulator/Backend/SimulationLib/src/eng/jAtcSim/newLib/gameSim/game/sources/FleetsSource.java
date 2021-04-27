package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.exceptions.ApplicationException;
import eng.jAtcSim.newLib.fleet.airliners.AirlinesFleets;
import eng.jAtcSim.newLib.fleet.generalAviation.GeneralAviationFleets;
import eng.jAtcSim.newLib.xml.fleets.FleetsXmlLoader;
import exml.annotations.XConstructor;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class FleetsSource extends Source<FleetsSource.Fleets> {

  public static class Fleets {
    public final GeneralAviationFleets gaFleets;
    public final AirlinesFleets companyFleets;

    public Fleets(GeneralAviationFleets gaFleets, AirlinesFleets companyFleets) {
      this.gaFleets = gaFleets;
      this.companyFleets = companyFleets;
    }
  }

  private final String generalAviationFileName;
  private final String companyFileName;

  public String getGeneralAviationFileName() {
    return generalAviationFileName;
  }

  public String getCompanyFileName() {
    return companyFileName;
  }

  @XConstructor
  FleetsSource(String generalAviationFileName, String companyFileName) {
    this.generalAviationFileName = generalAviationFileName;
    this.companyFileName = companyFileName;
  }

  public void init() {
    GeneralAviationFleets gaFleets;
    AirlinesFleets companyFleets;

    try {
      gaFleets = FleetsXmlLoader.loadGeneralAviationFleets(generalAviationFileName);
    } catch (Exception e) {
      throw new ApplicationException(sf("Failed to load g-a xml-fleets-file from '%s'", this.generalAviationFileName), e);
    }
    try {
      companyFleets = FleetsXmlLoader.loadCompanyFleet(companyFileName);
    } catch (Exception e) {
      throw new ApplicationException(sf("Failed to load company xml-fleets-file from '%s'", this.companyFileName), e);
    }

    Fleets fleets = new Fleets(gaFleets, companyFleets);
    super.setContent(fleets);
  }
}
