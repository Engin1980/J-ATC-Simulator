package exml;

public class Log {
  private int indent = 0;

  public void decreaseIndent() {
    this.indent--;
  }

  public void increaseIndent() {
    this.indent++;
  }

  public void log(String s, Object... params) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < indent; i++) {
      sb.append(" ");
    }

    sb.append(String.format(s, params));

    System.out.println(sb.toString());
  }
}
