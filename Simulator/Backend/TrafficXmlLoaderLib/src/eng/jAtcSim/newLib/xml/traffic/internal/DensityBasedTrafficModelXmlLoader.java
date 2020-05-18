package eng.jAtcSim.newLib.xml.traffic.internal;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.traffic.models.DensityBasedTrafficModel;

public class DensityBasedTrafficModelXmlLoader {
  public DensityBasedTrafficModel load(XElement source) {
    SmartXmlLoaderUtils.setContext(source);
    double defaultGeneralAviationProbability = SmartXmlLoaderUtils.loadDouble("defaultGeneralAviationProbability");

    //TODO isFullDayTraffic not implemented
    boolean isFullDayTraffic = SmartXmlLoaderUtils.loadBoolean(source.getChild("companies"), "isFullDayTraffic");
    IList<DensityBasedTrafficModel.Company> companies = SmartXmlLoaderUtils.loadList(
        source.getChild("companies").getChildren("company"),
        q -> loadCompany(q));
    IList<DensityBasedTrafficModel.Company> countries = SmartXmlLoaderUtils.loadList(
        source.getChild("countries").getChildren("country"),
        q -> loadCompany(q));
    DensityBasedTrafficModel.HourBlockMovements[] density = loadDensities(
        source.getChild("density").getChildren("item"),
        defaultGeneralAviationProbability);

    IList<DensityBasedTrafficModel.DirectionWeight> directions = new EList<>();
    SmartXmlLoaderUtils.loadList(source.getChild("directions").getChildren("direction"),
        directions,
        q -> DensityBasedTrafficModel.DirectionWeight.load(q));

    DensityBasedTrafficModel ret = DensityBasedTrafficModel.create(
        density, companies, countries, directions);
    return ret;
  }

  private DensityBasedTrafficModel.Company loadCompany(XElement source) {
    String code = SmartXmlLoaderUtils.loadString(source, "icao");
    double weight = SmartXmlLoaderUtils.loadDouble(source, "weight");
    Character category = SmartXmlLoaderUtils.loadChar(source, "category", null);
    DensityBasedTrafficModel.Company cw = new DensityBasedTrafficModel.Company(code, category, weight);
    return cw;
  }

  private DensityBasedTrafficModel.HourBlockMovements[] loadDensities(IReadOnlyList<XElement> xmls, double defaultGeneralAviationProbability) {
    DensityBasedTrafficModel.HourBlockMovements[] ret = new DensityBasedTrafficModel.HourBlockMovements[24];

    for (XElement child : xmls) {
      int hour = SmartXmlLoaderUtils.loadInteger(child, "hour");
      int arrs = SmartXmlLoaderUtils.loadInteger(child, "arrivals");
      int deps = SmartXmlLoaderUtils.loadInteger(child, "departures");
      double gaProb = SmartXmlLoaderUtils.loadDouble("generalAviationProbability", defaultGeneralAviationProbability);


      DensityBasedTrafficModel.HourBlockMovements hbm = new DensityBasedTrafficModel.HourBlockMovements(arrs, deps, gaProb);
      ret[hour] = hbm;
    }

    for (int i = 0; i < ret.length; i++) {
      if (ret[i] == null)
        ret[i] = new DensityBasedTrafficModel.HourBlockMovements(0, 0, defaultGeneralAviationProbability);
    }

    return ret;
  }
}
