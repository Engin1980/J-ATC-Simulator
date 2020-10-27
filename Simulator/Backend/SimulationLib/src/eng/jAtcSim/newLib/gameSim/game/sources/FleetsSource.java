package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.fleet.airliners.AirlinesFleets;
import eng.jAtcSim.newLib.fleet.generalAviation.GeneralAviationFleets;
import eng.jAtcSim.newLib.xml.fleets.FleetsXmlLoader;

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

  private FleetsSource.Fleets content;
  private String generalAviationFileName;
  private String companyFileName;

  public String getGeneralAviationFileName() {
    return generalAviationFileName;
  }

  public String getCompanyFileName() {
    return companyFileName;
  }

  public FleetsSource(String generalAviationFileName, String companyFileName) {
    this.generalAviationFileName = generalAviationFileName;
    this.companyFileName = companyFileName;
  }

  public void init() {
    GeneralAviationFleets gaFleets;
    AirlinesFleets companyFleets;

    try {
      gaFleets = FleetsXmlLoader.loadGeneralAviationFleets(generalAviationFileName);
    } catch (Exception e) {
      throw new EApplicationException(sf("Failed to load g-a xml-fleets-file from '%s'", this.generalAviationFileName), e);
    }
    try {
      companyFleets = FleetsXmlLoader.loadAirlinesFleets(companyFileName);
    } catch (Exception e) {
      throw new EApplicationException(sf("Failed to load company xml-fleets-file from '%s'", this.companyFileName), e);
    }

    this.content = new Fleets(gaFleets, companyFleets);

    super.setInitialized();
  }

  @Override
  protected Fleets _getContent() {
    return content;
  }
}
