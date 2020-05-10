package eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.types;

import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EApplicationException;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class Sentence {

  public static abstract class Block {
  }

  public static class StaticBlock extends Block {
    public String text;

    public StaticBlock(String text) {
      this.text = text;
    }
  }

  public static class VariableBlock extends Block {
    public String variable;

    public VariableBlock(String variable) {
      this.variable = variable;
    }
  }

  public static class ConditionalBlock extends Block {
    public IList<Block> content = new EList<>();
  }

  public final String kind;
  public final String text;
  public IList<Block> content = new EList<>();

  public Sentence(String kind, String text) {
    this.kind = kind;
    this.text = text;
    this.build();
  }

  private void build() {
    build(this.text, this.content);
  }

  private static void build(String text, IList<Block> parent) {
    String tmp = text;
    while (tmp.length() != 0) {
      int indexOfVar = tmp.indexOf('{');
      int indexOfCond = tmp.indexOf('[');

      if (indexOfVar == -1 && indexOfCond == -1) {
        StaticBlock staticBlock = new StaticBlock(tmp);
        parent.add(staticBlock);
        tmp = "";
      } else {
        if (indexOfVar == -1) indexOfVar = text.length();
        if (indexOfCond == -1) indexOfCond = text.length();
        int indexMin = Math.min(indexOfVar, indexOfCond);
        if (indexMin > 0) {
          String staticContent = tmp.substring(0, indexMin);
          StaticBlock staticBlock = new StaticBlock(staticContent);
          parent.add(staticBlock);
        }
        boolean isVar = indexOfVar < indexOfCond;
        int indexOfEnd = getClosingBracketIndex(tmp,
            isVar ? indexOfVar : indexOfCond,
            isVar ? '{' : '[',
            isVar ? '}' : ']');
        if (isVar) {
          VariableBlock variableBlock = new VariableBlock(tmp.substring(indexOfVar + 1, indexOfEnd));
          parent.add(variableBlock);
        } else {
          ConditionalBlock conditionalBlock = new ConditionalBlock();
          String sub = tmp.substring(indexOfCond + 1, indexOfEnd);
          build(sub, conditionalBlock.content);
          parent.add(conditionalBlock);
        }
        tmp = tmp.substring(indexOfEnd + 1);
      }
    }
  }

  private static int getClosingBracketIndex(String text, int fromIndex, char openChar, char closeChar) {
    int level = 0;
    for (int i = 0; i < text.length(); i++) {
      char current = text.charAt(i);
      if (current == openChar)
        level++;
      else if (current == closeChar) {
        level--;
        if (level == 0)
          return i;
      }
    }
    throw new EApplicationException("Unable to find closing bracket " + closeChar + "  for sequence " + text.substring(fromIndex));
  }
}
