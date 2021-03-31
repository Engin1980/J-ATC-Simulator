package eng.jAtcSim.app.startupSettings;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.traffic.models.SimpleGenericTrafficModel;
import exml.IXPersistable;
import exml.annotations.XAttribute;
import exml.annotations.XIgnored;
import exml.loading.XLoadContext;
import exml.saving.XSaveContext;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class StartupSettings implements IXPersistable {

  public static class Files implements IXPersistable {
    private static String normalizePath(String path) {
      String ret;
      if (path == null)
        ret = null;
      else
        ret = path.replace('\\', '/');
      return ret;
    }

    @XAttribute public String areaXmlFile;
    @XAttribute public String companiesFleetsXmlFile;
    @XAttribute public String generalAviationFleetsXmlFile;
    @XAttribute public String planesXmlFile;
    @XAttribute public String trafficXmlFile;
    @XAttribute public String weatherXmlFile;

    public void normalizeSlashes() {
      this.trafficXmlFile = normalizePath(this.trafficXmlFile);
      this.planesXmlFile = normalizePath(this.planesXmlFile);
      this.areaXmlFile = normalizePath(this.areaXmlFile);
      this.generalAviationFleetsXmlFile = normalizePath(this.generalAviationFleetsXmlFile);
      this.companiesFleetsXmlFile = normalizePath(this.companiesFleetsXmlFile);
      this.weatherXmlFile = normalizePath(this.weatherXmlFile);
    }
  }

  public static class Recent implements IXPersistable {
    @XAttribute public String icao;
    @XAttribute public LocalTime time;
  }

  public static class Weather implements IXPersistable {
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

    @XAttribute public int cloudBaseAltitudeFt;
    @XAttribute public double cloudBaseProbability;
    @XAttribute public eSnowState snowState;
    @XAttribute public WeatherSourceType type = WeatherSourceType.user;
    @XAttribute public int visibilityInM;
    @XAttribute public int windDirection;
    @XAttribute public int windSpeed;
  }

  public static class Traffic implements IXPersistable {
    public enum eTrafficType {
      user,
      xml
    }

    @XAttribute public boolean allowDelays;
    public CustomTraffic customTraffic = new CustomTraffic();
    @XAttribute public double densityPercentage;
    @XAttribute public double emergencyPerDayProbability = 1 / 7d;
    @XAttribute public int maxPlanes;
    @XAttribute public eTrafficType type;
  }

  public static class CustomTraffic implements IXPersistable {

    private static final int MOVEMENTS_PER_HOUR_LENGTH = 24;
    @XIgnored private IMap<String, Integer> companies = new EMap<>();
    @XIgnored private IMap<String, Integer> countryCodes = new EMap<>();
    @XAttribute private double departureProbability = .5;
    @XAttribute private double generalAviationProbability = .5;
    @XIgnored private SimpleGenericTrafficModel.MovementsForHour[] movementsForHours;
    @XAttribute private boolean useExtendedCallsigns = false;

    public CustomTraffic() {
      this.companies.set("CSA", 1);
      this.companies.set("EZY", 1);
      this.countryCodes.set("OK", 1);
      this.movementsForHours = new SimpleGenericTrafficModel.MovementsForHour[MOVEMENTS_PER_HOUR_LENGTH];
      for (int i = 0; i < movementsForHours.length; i++) {
        movementsForHours[i] = new SimpleGenericTrafficModel.MovementsForHour(5, generalAviationProbability, departureProbability);
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

    @Override
    public void load(XElement elm, XLoadContext ctx) {
      companies = new EMap<>();
      elm.getChild("companies").getChildren().forEach(q -> elementToEntry(q, companies));

      countryCodes = new EMap<>();
      elm.getChild("countryCodes").getChildren().forEach(q -> elementToEntry(q, countryCodes));

      ctx.fields.loadField(this, "generalAviationProbability", elm);
      ctx.fields.loadField(this, "departureProbability", elm);

      {
        IList<SimpleGenericTrafficModel.MovementsForHour> lst = new EList<>();
        elm.getChild("movementsForHours").getChildren().forEach(q -> {
          int count = Integer.parseInt(q.getAttribute("count"));
          double gap = q.hasAttribute("generalAviationProbability") ?
                  Double.parseDouble(q.getAttribute("generalAviationProbability")) : this.generalAviationProbability;
          double dp = q.hasAttribute("departureProbability") ?
                  Double.parseDouble(q.getAttribute("departureProbability")) : this.departureProbability;
          lst.add(new SimpleGenericTrafficModel.MovementsForHour(count, gap, dp));
        });
        this.movementsForHours = lst.toArray(SimpleGenericTrafficModel.MovementsForHour.class);
        EAssert.isTrue(this.movementsForHours.length == MOVEMENTS_PER_HOUR_LENGTH);
      }
    }

    @Override
    public void save(XElement elm, XSaveContext ctx) {
      {
        XElement tmp = new XElement("companies");
        elm.addElement(tmp);
        companies.forEach(q -> tmp.addElement(entryToElement(q)));
      }
      {
        XElement tmp = new XElement("countryCodes");
        elm.addElement(tmp);
        countryCodes.forEach(q -> tmp.addElement(entryToElement(q)));
      }
      {
        XElement tmp = new XElement("movementsForHours");
        elm.addElement(tmp);
        Arrays.stream(movementsForHours).forEach(q -> {
          XElement it = new XElement("item");
          it.setAttribute("count", Integer.toString(q.count));
          if (q.generalAviationProbability != this.generalAviationProbability)
            it.setAttribute("generalAviationProbability", Double.toString(q.generalAviationProbability));
          if (q.departureProbability != this.departureProbability)
            it.setAttribute("departureProbability", Double.toString(q.departureProbability));
          tmp.addElement(it);
        });
      }
    }

    private void elementToEntry(XElement elm, IMap<String, Integer> map) {
      String key = elm.getAttribute("key");
      int val = elm.hasAttribute("weight") ? Integer.parseInt(elm.getAttribute("weight")) : 1;
      map.set(key, val);
    }

    private XElement entryToElement(Map.Entry<String, Integer> e) {
      XElement ret = new XElement("item");
      ret.setAttribute("key", e.getKey());
      if (e.getValue() != null) ret.setAttribute("weight", e.getValue().toString());
      return ret;
    }
  }

  public static class Layout implements IXPersistable {
    @XAttribute public String layoutXmlFile = null;
  }

  public static class Simulation implements IXPersistable {
    @XAttribute public int secondLengthInMs = 1000;
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
