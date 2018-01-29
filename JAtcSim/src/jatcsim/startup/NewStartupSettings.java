package jatcsim.startup;

public class NewStartupSettings {
  public static class Files{
    public String trafficXmlFile;
    public String planesXmlFile;
    public String areaXmlFile;
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
    public int maxPlanes;
    public boolean useExtendedCallsigns;
    public boolean delayAllowed;
    public int weightTypeD;
    public int weightTypeC;
    public int weightTypeB;
    public int weightTypeA;
    public int arrivals2departuresRatio;
    public int vfr2ifrRatio;
    public int movementsPerHour;
    public boolean useXml;

  }

  public static class Radar{
    public String packClass = "jatcsim.frmPacks.simple.Pack";
  }

  public static class Simulation{
    public int secondLengthInMs = 1000;
  }


  public Files files;
  public Recent recent;
  public Weather weather;
  public Traffic traffic;
  public Radar radar;
  public Simulation simulation;
}
