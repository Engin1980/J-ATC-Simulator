package eng.jAtcSim.layouting;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.EAssert;

public class ColumnList extends Block {
  private final IList<Column> columns;

  public ColumnList(IList<Column> columns) {
    EAssert.Argument.isNotNull(columns);
    EAssert.Argument.isFalse(columns.isEmpty());
    this.columns = columns;
  }

  public IReadOnlyList<Column> getColumns() {
    return columns;
  }
}
