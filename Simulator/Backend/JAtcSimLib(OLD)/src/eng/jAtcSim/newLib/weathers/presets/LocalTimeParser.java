package eng.jAtcSim.newLib.area.weathers.presets;

import eng.eSystem.xmlSerialization.exceptions.XmlSerializationException;
import eng.eSystem.xmlSerialization.supports.IValueParser;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

class LocalTimeParser implements IValueParser<LocalTime> {

  @Override
  public LocalTime parse(String s) {
    LocalTime ret;
    if (s.contains(":") == false){
      s = s.substring(0,2) + ":" + s.substring(2);
    }
    try{
      ret = LocalTime.parse(s, DateTimeFormatter.ofPattern("h:mm"));
    } catch (Exception ex){
      throw new XmlSerializationException("Unable to parse " + s + " to LocalTime.", ex);
    }
    return ret;
  }

  @Override
  public String format(LocalTime localTime) {
    String s = localTime.format(DateTimeFormatter.ofPattern("h:mm"));
    return s;
  }
}
