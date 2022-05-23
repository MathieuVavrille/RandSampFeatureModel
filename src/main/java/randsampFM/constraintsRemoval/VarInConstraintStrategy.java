package randsampFM.constraintsRemoval;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.memory.IStateInt;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/** From a list of constraints, this strategy will instantiate the variables in these constraints */
public class VarInConstraintStrategy extends AbstractStrategy<IntVar> {
  
  private final List<Constraint> constraints;
  private final IStateInt depth;

  public VarInConstraintStrategy(final IntVar[] vars, List<Constraint> constraints) {
    super(vars);
    this.constraints = constraints;
    this.depth = vars[0].getModel().getEnvironment().makeInt();
  }

  public static VarInConstraintStrategy findConstraints(final IntVar[] allVars, final Set<IntVar> featureVars, final Constraint[] allConstraints) {
    Set<Constraint> constraints = new HashSet<Constraint>();
    for (IntVar var : allVars) {
      if (!featureVars.contains(var)) {
        for (Constraint cstr : allConstraints) {
          if (!constraints.contains(cstr))
            constraints.add(cstr);
        }
      }
    }
    return new VarInConstraintStrategy(allVars, new ArrayList<Constraint>(constraints));
  }
  
  @Override
  protected Decision<IntVar> computeDecision(IntVar var) {
    throw new IllegalStateException("It is currently not possible to use computeDecision in VarInConstraintStrategy");
  }

  public Decision<IntVar> getDecision() {
    depth.add(2);
    for (Constraint cstr : constraints) {
      if (cstr.isSatisfied() == ESat.UNDEFINED) { // Only choose the not yet satisfied constraints
        for (Propagator<IntVar> prop : cstr.getPropagators()) {
          for (IntVar var : prop.getVars()) {
            if (!var.isInstantiated()) {// pick and uninstantiated variable
              /*for (int i = 0; i < depth.get(); i++)
                System.out.print("  ");
                System.out.println(var);*/
              return makeIntDecision(var, var.getLB()); // LB is 0, var should be a IntVar for the FM application
            }
          }
        }
      }
    }
    return null;
  }
}
