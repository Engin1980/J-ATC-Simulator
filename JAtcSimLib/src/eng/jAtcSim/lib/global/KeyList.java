/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.global;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.ERuntimeException;

/**
 * @param <TValue> Type of item in Collection.
 * @param <TKey>   Type of key of the item.
 * @author Marek
 */
public class KeyList<TValue extends KeyItem<TKey>, TKey> extends EDistinctList<TValue> {

  private static IList<KeyList> instances = new EList<>();
  private static boolean enabled = false;

  public static void setDuplicatesChecking(boolean enable) {
    KeyList.enabled = enable;

    if (enable){
      for (KeyList instance : instances) {
        instance.setOnDuplicitBehavior(Behavior.exception, true );
      }
    }
    else
      instances.forEach(q -> q.setOnDuplicitBehavior(Behavior.ignore, false));
  }

  public KeyList() {
    super(
        tValue -> tValue.getKey(),
        KeyList.enabled ? Behavior.exception : Behavior.ignore
    );

    instances.add(this);
  }

  public TValue tryGet(TKey key) {
    for (TValue item : this) {
      if (item.getKey().equals(key)) {
        return item;
      }
    }
    return null;
  }

  public TValue get(TKey key) {
    for (TValue item : this) {
      if (item.getKey().equals(key)) {
        return item;
      }
    }
    throw new ERuntimeException("No such element in KeyList - key: " + key.toString());
  }
}
