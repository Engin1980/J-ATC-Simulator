package eng.jAtcSim.newLib.xml.traffic.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.traffic.models.SimpleGenericTrafficModel;

public class SimpleGenericTrafficModelXmlLoader {
  public SimpleGenericTrafficModel load(XElement source) {
    XmlLoaderUtils.setContext(source);
    double delayProbability = XmlLoaderUtils.loadDouble("delayProbability");
    int maxDelayInMinutesPerStep = XmlLoaderUtils.loadInteger("maxDelayInMinutesPerStep");
    double defaultProbabilityOfDeparture = XmlLoaderUtils.loadDouble("defaultProbabilityOfDeparture");
    double defaultProbabilityOfGeneralAviation = XmlLoaderUtils.loadDouble("defaultProbabilityOfGeneralAviation");
    boolean useExtendedCallsigns = XmlLoaderUtils.loadBoolean("useExtendedCallsigns");

    IList<MovementsForHour> movementsForHours = XmlLoaderUtils.loadList(
        source.getChild("movements").getChildren("movementsForHour"),
        q -> loadMovementsForHour(q, defaultProbabilityOfDeparture, defaultProbabilityOfGeneralAviation));

    IList<ValueAndWeight> companies = XmlLoaderUtils.loadList(
        source.getChild("companies").getChildren("item"),
        q -> loadValueAndWeight(q)
    );

    IList<ValueAndWeight> countries = XmlLoaderUtils.loadList(
        source.getChild("countryCodes").getChildren("item"),
        q -> loadValueAndWeight(q)
    );

    SimpleGenericTrafficModel ret = SimpleGenericTrafficModel.create(
        movementsForHours, companies, countries, delayProbability, maxDelayInMinutesPerStep, useExtendedCallsigns
    );
    return ret;
  }

  private ValueAndWeight loadValueAndWeight(XElement source) {
    String value = source.getContent();
    int weight = XmlLoaderUtils.loadInteger(source, "weight", 1);

    ValueAndWeight ret = ValueAndWeight.create(value,weight);
    return ret;
  }

  private MovementsForHour loadMovementsForHour(XElement source,
                                                double defaultProbabilityOfDeparture,
                                                double defaultProbabilityOfGeneralAviation) {
    XmlLoaderUtils.setContext(source);

    int count = XmlLoaderUtils.loadInteger("count");
    double departureProbability = XmlLoaderUtils.loadDouble("departureProbability", defaultProbabilityOfDeparture);
    double gaProbability = XmlLoaderUtils.loadDouble("probabilityOfGeneralAviation", defaultProbabilityOfGeneralAviation);

    MovementsForHour ret = MovementsForHour.create(count, gaProbability, departureProbability);
    return ret;
  }
}
