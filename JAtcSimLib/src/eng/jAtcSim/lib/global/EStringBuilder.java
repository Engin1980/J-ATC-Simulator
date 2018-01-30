/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.global;

/**
 *
 * @author Marek
 */
public class EStringBuilder {
  private final StringBuilder sb;

  public EStringBuilder(){
    sb = new StringBuilder();
  }
  public EStringBuilder(int initialCapacity){
    sb = new StringBuilder(initialCapacity);
  }
  
  public void clear(){
    sb.setLength(0);
  }
  
  public EStringBuilder append(String text){
    sb.append(text);
    return this;
  }
  
  public EStringBuilder appendFormat(String format, Object... args){
    append(
      String.format(format, (Object[]) args));
    return this;
  }
  
  public EStringBuilder appendFormatLine(String format, Object... args){
    appendLine(
      String.format(format, (Object[]) args));
    return this;
  }
  
  public EStringBuilder appendLine(String line){
    append(line);
    append("\r\n");
    return this;
  }
  
  public EStringBuilder appendLine(){
    this.appendLine("");
    return this;
  }
  
  @Override
  public String toString() {
    return sb.toString();
  }
  
}
