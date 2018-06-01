///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package eng.jAtcSim.lib.global;
//
//import eng.eSystem.collections.EDistinctList;
//import eng.eSystem.collections.EList;
//import eng.eSystem.collections.IList;
//import eng.eSystem.exceptions.ERuntimeException;
//
///**
// * @param <TValue> Type of item in Collection.
// * @param <TKey>   Type of key of the item.
// * @author Marek
// */
//public class KeyList<TValue extends KeyItem<TKey>, TKey> extends EDistinctList<TValue> {
//
//  public KeyList(boolean duplicitCheckEnabled) {
//    super(
//        tValue -> tValue.getKey(),
//        duplicitCheckEnabled ? Behavior.exception : Behavior.ignore
//    );
//  }
//
//  public void setDuplicitCheckEnabled(boolean value) {
//    if (value)
//      super.setOnDuplicitBehavior(Behavior.exception, true);
//    else
//      super.setOnDuplicitBehavior(Behavior.ignore, false);
//  }
//
//  public TValue tryGet(TKey key) {
//    for (TValue item : this) {
//      if (item.getKey().equals(key)) {
//        return item;
//      }
//    }
//    return null;
//  }
//
//  public TValue get(TKey key) {
//    for (TValue item : this) {
//      if (item.getKey().equals(key)) {
//        return item;
//      }
//    }
//    throw new ERuntimeException("No such element in KeyList - key: " + key.toString());
//  }
//}
