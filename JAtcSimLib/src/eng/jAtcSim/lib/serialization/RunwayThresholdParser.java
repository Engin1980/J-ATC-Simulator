package eng.jAtcSim.lib.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.IElementParser;
import eng.eSystem.xmlSerialization.XmlDeserializationException;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Runway;
import eng.jAtcSim.lib.world.RunwayThreshold;

public class RunwayThresholdParser implements IElementParser<RunwayThreshold> {

  private Airport known;

  @Override
  public Class getType() {
    return RunwayThreshold.class;
  }

  @Override
  public RunwayThreshold parse(XElement xElement, XmlSerializer.Deserializer xmlSerializer) throws XmlDeserializationException {
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
  public void format(RunwayThreshold runwayThreshold, XElement xElement, XmlSerializer.Serializer xmlSerializer) throws XmlSerializationException {
    xElement.setContent(runwayThreshold.getName());
  }

  public void setRelative(Airport aip){
    this.known = aip;
  }

  @Override
  public boolean isApplicableOnDescendants() {
    return false;
  }
}
