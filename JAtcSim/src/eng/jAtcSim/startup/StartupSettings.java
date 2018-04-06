package eng.jAtcSim.startup;

public class StartupSettings {
  public static class Files{
    public String trafficXmlFile;
    public String planesXmlFile;
    public String areaXmlFile;
    public String fleetsXmlFile;
  }

  public static class Recent{
    public String time; // TODO time should be local-time
    public String icao;
  }

  public static class Weather{
    public int userChanges;
    public boolean useOnline;
    public String metar;
  }

  public static class Traffic{
    public enum eTrafficType{
      custom,
      xml,
      airportDefined
    }

    public eTrafficType type;
    public String trafficAirportDefinedTitle;
    public CustomTraffic customTraffic;
    public int maxPlanes;
    public double densityPercentage;
  }

  public static class CustomTraffic{

    public boolean useExtendedCallsigns;
    public boolean delayAllowed;
    public int weightTypeD;
    public int weightTypeC;
    public int weightTypeB;
    public int weightTypeA;
    public int arrivals2departuresRatio;
    public int movementsPerHour;

  }

  public static class Radar{
    public String packClass = "Pack";
  }

  public static class Simulation{
    public int secondLengthInMs = 1000;
    public double emergencyPerDayProbability = 1/7d;
  }


  public Files files;
  public Recent recent;
  public Weather weather;
  public Traffic traffic;
  public Radar radar;
  public Simulation simulation;
}
