package eng.jAtcSim.newLib.area.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.supports.IElementParser;
import eng.jAtcSim.newLib.world.Airport;
import eng.jAtcSim.newLib.world.ActiveRunway;
import eng.jAtcSim.newLib.world.ActiveRunwayThreshold;

public class RunwayThresholdParser implements IElementParser<ActiveRunwayThreshold> {

  private Airport known;

  @Override
  public ActiveRunwayThreshold parse(XElement xElement, XmlSerializer.Deserializer xmlSerializer)   {
    String c = xElement.getContent();
    ActiveRunwayThreshold ret = null;
    for (ActiveRunway runway : known.getRunways()) {
      for (ActiveRunwayThreshold threshold : runway.getThresholds()) {
        if (threshold.getName().equals(c)){
          ret = threshold;
          break;
        }
      }
    }
    if (ret == null)
      throw new ApplicationException("Unable to find threshold " + c + ".");
    return ret;
  }

  @Override
  public void format(ActiveRunwayThreshold runwayThreshold, XElement xElement, XmlSerializer.Serializer xmlSerializer)   {
    xElement.setContent(runwayThreshold.getName());
  }

  public void setRelative(Airport aip){
    this.known = aip;
  }

}
