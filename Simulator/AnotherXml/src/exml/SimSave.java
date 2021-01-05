package exml;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.ISet;
import eng.eSystem.eXml.XElement;

public class SimSave {

  private IMap<Object, ISet<String>> usedFields = new EMap<>();

  public void saveRemainingFields(ISimPersistable o, XElement elm, XmlContext ctx) {
    ISet<String> remainingFields = FieldUtils.getRemainingFields(o, usedFields.tryGet(o));

    for (String remainingField : remainingFields) {
      FieldUtils.saveField(o, remainingField, elm, ctx);
    }
    usedFields.getOrSet(o, new ESet<>()).addMany(remainingFields);
  }
}
