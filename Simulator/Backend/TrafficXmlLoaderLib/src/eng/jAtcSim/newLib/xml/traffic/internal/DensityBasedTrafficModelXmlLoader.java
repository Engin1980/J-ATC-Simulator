package eng.jAtcSim.newLib.xml.traffic.internal;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.traffic.models.DensityBasedTrafficModel;

public class DensityBasedTrafficModelXmlLoader {
  public DensityBasedTrafficModel load(XElement source) {
    XmlLoaderUtils.setContext(source);
    double defaultGeneralAviationProbability = XmlLoaderUtils.loadDouble("defaultGeneralAviationProbability");

    //TODO isFullDayTraffic not implemented
    boolean isFullDayTraffic = XmlLoaderUtils.loadBoolean(source.getChild("companies"), "isFullDayTraffic");
    IList<DensityBasedTrafficModel.Company> companies = XmlLoaderUtils.loadList(
        source.getChild("companies").getChildren("company"),
        q -> loadCompany(q));
    IList<DensityBasedTrafficModel.Company> countries = XmlLoaderUtils.loadList(
        source.getChild("countries").getChildren("country"),
        q -> loadCompany(q));
    DensityBasedTrafficModel.HourBlockMovements[] density = loadDensities(
        source.getChild("density").getChildren("item"),
        defaultGeneralAviationProbability);

    IList<DensityBasedTrafficModel.DirectionWeight> directions = new EList<>();
    XmlLoaderUtils.loadList(source.getChild("directions").getChildren("direction"),
        directions,
        q -> DensityBasedTrafficModel.DirectionWeight.load(q));

    DensityBasedTrafficModel ret = DensityBasedTrafficModel.create(
        density, companies, countries, directions);
    return ret;
  }

  private DensityBasedTrafficModel.Company loadCompany(XElement source) {
    String code = XmlLoaderUtils.loadString(source, "icao");
    double weight = XmlLoaderUtils.loadDouble(source, "weight");
    Character category = XmlLoaderUtils.loadChar(source, "category", null);
    DensityBasedTrafficModel.Company cw = new DensityBasedTrafficModel.Company(code, category, weight);
    return cw;
  }

  private DensityBasedTrafficModel.HourBlockMovements[] loadDensities(IReadOnlyList<XElement> xmls, double defaultGeneralAviationProbability) {
    DensityBasedTrafficModel.HourBlockMovements[] ret = new DensityBasedTrafficModel.HourBlockMovements[24];

    for (XElement child : xmls) {
      int hour = XmlLoaderUtils.loadInteger(child, "hour");
      int arrs = XmlLoaderUtils.loadInteger(child, "arrivals");
      int deps = XmlLoaderUtils.loadInteger(child, "departures");
      double gaProb = XmlLoaderUtils.loadDouble("generalAviationProbability", defaultGeneralAviationProbability);


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
