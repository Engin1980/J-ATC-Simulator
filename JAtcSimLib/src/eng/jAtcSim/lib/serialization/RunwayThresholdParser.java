package eng.jAtcSim.lib.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.supports.IElementParser;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Runway;
import eng.jAtcSim.lib.world.RunwayThreshold;

public class RunwayThresholdParser implements IElementParser<RunwayThreshold> {

  private Airport known;

  @Override
  public RunwayThreshold parse(XElement xElement, XmlSerializer.Deserializer xmlSerializer)   {
    String c = xElement.getContent();
    RunwayThreshold ret = null;
    for (Runway runway : known.getRunways()) {
      for (RunwayThreshold threshold : runway.getThresholds()) {
        if (threshold.getName().equals(c)){
          ret = threshold;
          break;
        }
      }
    }
    if (ret == null)
      throw new EApplicationException("Unable to find threshold " + c + ".");
    return ret;
  }

  @Override
  public void format(RunwayThreshold runwayThreshold, XElement xElement, XmlSerializer.Serializer xmlSerializer)   {
    xElement.setContent(runwayThreshold.getName());
  }

  public void setRelative(Airport aip){
    this.known = aip;
  }

}
