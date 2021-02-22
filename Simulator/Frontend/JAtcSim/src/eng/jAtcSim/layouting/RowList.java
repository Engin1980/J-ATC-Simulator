package eng.jAtcSim.layouting;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.EAssert;

class RowList extends Block {
  private final IList<Row> rows;

  public RowList(IList<Row> rows) {
    EAssert.Argument.isNotNull(rows);
    EAssert.Argument.isFalse(rows.isEmpty());
    this.rows = rows;
  }

  public IReadOnlyList<Row> getRows() {
    return rows;
  }
}
