package randsampFM.twise;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.BoolVar;

public class BoolDecision extends Decision<BoolVar> {

  private final BoolVar var;
  private final boolean value;
  
  public BoolDecision(final BoolVar var, final boolean value) {
    super(2);
    this.var = var;
    this.value = value;
  }

  @Override
  public Integer getDecisionValue() {
    return value ? 1 : 0;
  }

  @Override
  public void apply() throws ContradictionException {
    if (branch == 1) {
      var.getModel().getSolver().getEventObserver().pushDecisionLevel();
      var.instantiateTo(value ? 1 : 0, this);
    } else if (branch == 2) {
      var.instantiateTo(value ? 0 : 1, this);
    }
  }

  @Override
  protected void set(BoolVar var) {
    throw new IllegalStateException("Cannot reuse this decision");
  }

  @Override
  public void free() {}
}
