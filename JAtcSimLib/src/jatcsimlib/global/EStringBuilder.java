/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.global;

/**
 *
 * @author Marek
 */
public class EStringBuilder {
  private final StringBuilder sb = new StringBuilder();

  public void append(String text){
    sb.append(text);
  }
  
  public void appendFormat(String format, String... args){
    append(
      String.format(format, (Object[]) args));
  }
  
  public void appendFormatLine(String format, String... args){
    appendLine(
      String.format(format, (Object[]) args));
  }
  
  public void appendLine(String line){
    append(line);
    append("\r\n");
  }
  
  @Override
  public String toString() {
    return sb.toString();
  }
  
}
