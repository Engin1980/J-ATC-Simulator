package eng.jAtcSim.lib.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.IElementParser;
import eng.eSystem.xmlSerialization.XmlDeserializationException;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.world.RunwayThreshold;

public class RunwayThresholdParser implements IElementParser<RunwayThreshold> {


  @Override
  public Class getType() {
    return RunwayThreshold.class;
  }

  @Override
  public RunwayThreshold parse(XElement xElement, XmlSerializer.Deserializer xmlSerializer) throws XmlDeserializationException {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public void format(RunwayThreshold runwayThreshold, XElement xElement, XmlSerializer.Serializer xmlSerializer) throws XmlSerializationException {
    xElement.setContent(runwayThreshold.getName());
  }

  @Override
  public boolean isApplicableOnDescendants() {
    return false;
  }
}
