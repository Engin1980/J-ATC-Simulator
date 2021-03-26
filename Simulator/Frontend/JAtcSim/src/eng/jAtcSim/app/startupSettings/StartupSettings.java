package eng.jAtcSim.app.startupSettings;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.traffic.models.SimpleGenericTrafficModel;

import java.time.LocalTime;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class StartupSettings {

  public static class Files {
    private static String normalizePath(String path) {
      String ret;
      if (path == null)
        ret = null;
      else
        ret = path.replace('\\', '/');
      return ret;
    }

    public String areaXmlFile;
    public String companiesFleetsXmlFile;
    public String generalAviationFleetsXmlFile;
    public String planesXmlFile;
    public String trafficXmlFile;
    public String weatherXmlFile;

    public void normalizeSlashes() {
      this.trafficXmlFile = normalizePath(this.trafficXmlFile);
      this.planesXmlFile = normalizePath(this.planesXmlFile);
      this.areaXmlFile = normalizePath(this.areaXmlFile);
      this.generalAviationFleetsXmlFile = normalizePath(this.generalAviationFleetsXmlFile);
      this.companiesFleetsXmlFile = normalizePath(this.companiesFleetsXmlFile);
      this.weatherXmlFile = normalizePath(this.weatherXmlFile);
    }
  }

  public static class Recent {
    public String icao;
    public LocalTime time;
  }

  public static class Weather {
    public enum WeatherSourceType {
      user,
      online,
      xml
    }

    public enum eSnowState {
      none,
      normal,
      intensive
    }

    public int cloudBaseAltitudeFt;
    public double cloudBaseProbability;
    public eSnowState snowState;
    public WeatherSourceType type = WeatherSourceType.user;
    public int visibilityInM;
    public int windDirection;
    public int windSpeed;
  }

  public static class Traffic {
    public enum eTrafficType {
      user,
      xml
    }

    public boolean allowDelays;
    public CustomTraffic customTraffic = new CustomTraffic();
    public double densityPercentage;
    public double emergencyPerDayProbability = 1 / 7d;
    public int maxPlanes;
    public eTrafficType type;
  }

  public static class CustomTraffic {

    private static final int MOVEMENTS_PER_HOUR_LENGTH = 24;
    private IMap<String, Integer> companies = new EMap<>();
    private IMap<String, Integer> countryCodes = new EMap<>();
    private double departureProbability = .5;
    private double generalAviationProbability = .5;
    private SimpleGenericTrafficModel.MovementsForHour[] movementsForHours;
    private boolean useExtendedCallsigns = false;

    public CustomTraffic() {
      this.companies.set("CSA", 1);
      this.companies.set("EZY", 1);
      this.countryCodes.set("OK", 1);
      this.movementsForHours = new SimpleGenericTrafficModel.MovementsForHour[MOVEMENTS_PER_HOUR_LENGTH];
      for (int i = 0; i < movementsForHours.length; i++) {
        movementsForHours[i] = new SimpleGenericTrafficModel.MovementsForHour(5, null, null);
      }
    }

    public IMap<String, Integer> getCompanies() {
      return companies;
    }

    public void setCompanies(IMap<String, Integer> companies) {
      EAssert.Argument.isNotNull(companies, "companies");
      this.companies = companies;
    }

    public IMap<String, Integer> getCountryCodes() {
      return countryCodes;
    }

    public void setCountryCodes(IMap<String, Integer> countryCodes) {
      EAssert.Argument.isNotNull(countryCodes, "countryCodes");
      this.countryCodes = countryCodes;
    }

    public double getDepartureProbability() {
      return departureProbability;
    }

    public void setDepartureProbability(double departureProbability) {
      EAssert.Argument.isTrue(NumberUtils.isBetweenOrEqual(0, departureProbability, 1), "Value must be between 0 and 1.");
      this.departureProbability = departureProbability;
    }

    public double getGeneralAviationProbability() {
      return generalAviationProbability;
    }

    public void setGeneralAviationProbability(double generalAviationProbability) {
      EAssert.Argument.isTrue(NumberUtils.isBetweenOrEqual(0, generalAviationProbability, 1), "Value must be between 0 and 1.");
      this.generalAviationProbability = generalAviationProbability;
    }

    public SimpleGenericTrafficModel.MovementsForHour[] getMovementsForHours() {
      return movementsForHours;
    }

    public void setMovementsForHours(SimpleGenericTrafficModel.MovementsForHour[] movementsForHours) {
      EAssert.Argument.isNotNull(movementsForHours, "movementsPerHour");
      EAssert.Argument.isTrue(movementsForHours.length == MOVEMENTS_PER_HOUR_LENGTH,
              sf("There must be exactly %d records in movements per hour.", MOVEMENTS_PER_HOUR_LENGTH));
      this.movementsForHours = movementsForHours;
    }

    public boolean isUseExtendedCallsigns() {
      return useExtendedCallsigns;
    }

    public void setUseExtendedCallsigns(boolean useExtendedCallsigns) {
      this.useExtendedCallsigns = useExtendedCallsigns;
    }
  }

  public static class Layout {
    public String layoutXmlFile = null;
  }

  public static class Simulation {
    public int secondLengthInMs = 1000;
  }

  public final Files files;
  public final Layout layout;
  public final Recent recent;
  public final Simulation simulation;
  public final Traffic traffic;
  public final Weather weather;

  public StartupSettings() {
    this.files = new Files();
    this.layout = new Layout();
    this.recent = new Recent();
    this.weather = new Weather();
    this.traffic = new Traffic();
    this.simulation = new Simulation();
  }
}
