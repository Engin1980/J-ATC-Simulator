/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimxml.serialization;

/**
 *
 * @author Marek
 */
public class MapItem {
  public String collectionProperty;
  public String elementNameOrNull;
  public Class itemType;

  public MapItem(String collectionProperty, String elementNameOrNull, Class itemType) {
    this.collectionProperty = collectionProperty;
    this.elementNameOrNull = elementNameOrNull;
    this.itemType = itemType;
  }

  public MapItem(String collectionProperty, Class itemType) {
    this.collectionProperty = collectionProperty;
    this.itemType = itemType;
  }
}
