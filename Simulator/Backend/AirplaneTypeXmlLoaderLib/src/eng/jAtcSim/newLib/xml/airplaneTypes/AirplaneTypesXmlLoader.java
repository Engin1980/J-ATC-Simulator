package eng.jAtcSim.newLib.xml.airplaneTypes;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ApplicationException;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;
import exml.loading.XLoadContext;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AirplaneTypesXmlLoader {
  public static AirplaneTypes load(String fileName) {
    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (Exception ex) {
      throw new ApplicationException(sf("Failed to load airplane types from '{0}'.", fileName));
    }

    AirplaneTypes ret = load(doc.getRoot());
    return ret;
  }

  private static AirplaneTypes load(XElement source) {
    XLoadContext ctx = new XLoadContext().withDefaultParsers();

    IList<AirplaneType> tmp = new EList<>();

    analyseXElement(source, tmp, ctx);

    AirplaneTypes ret = AirplaneTypes.create(tmp);
    return ret;
  }

  private static void analyseXElement(XElement source, IList<AirplaneType> types, XLoadContext ctx) {
    source.getChildren("type").forEach(q -> types.add(loadAirplaneType(q, ctx)));
    source.getChildren("group").forEach(q -> analyseXElement(q, types, ctx));
  }

  private static AirplaneType loadAirplaneType(XElement source, XLoadContext ctx) {
    AirplaneType ret = ctx.loadObject(source, AirplaneType.class);
    return ret;
  }
}
