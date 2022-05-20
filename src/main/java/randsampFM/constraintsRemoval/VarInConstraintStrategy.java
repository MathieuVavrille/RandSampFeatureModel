package randsampFM.constraintsRemoval;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

import java.util.List;

/** From a list of constraints, this strategy will instantiate the variables in these constraints */
public class VarInConstraintStrategy extends AbstractStrategy<IntVar> {
  
  private final List<Constraint> constraints;

  public VarInConstraintStrategy(final IntVar[] vars, List<Constraint> constraints) {
    super(vars);
    this.constraints = constraints;
  }
  
  @Override
  protected Decision<IntVar> computeDecision(IntVar var) {
    throw new IllegalStateException("It is currently not possible to use computeDecision in VarInConstraintStrategy");
  }

  public Decision<IntVar> getDecision() {
    for (Constraint cstr : constraints) {
      if (cstr.isSatisfied() == ESat.UNDEFINED) { // Only choose the not yet satisfied constraints
        for (Propagator<IntVar> prop : cstr.getPropagators()) {
          for (IntVar var : prop.getVars()) {
            if (!var.isInstantiated()) // pick and uninstantiated variable
              return makeIntDecision(var, var.getLB()); // LB is 0, var should be a IntVar for the FM application
          }
        }
      }
    }
    return null;
  }
}
