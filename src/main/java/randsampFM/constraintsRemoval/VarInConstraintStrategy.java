package randsampFM.constraintsRemoval;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.memory.IStateInt;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/** From a list of constraints, this strategy will instantiate the variables in these constraints */
public class VarInConstraintStrategy extends AbstractStrategy<IntVar> {
  
  private final List<Constraint> constraints;
  private final IStateInt depth;

  public VarInConstraintStrategy(final IntVar[] vars, List<Constraint> constraints) {
    super(vars);
    this.constraints = constraints;
    /*for (Constraint c : constraints) {
      System.out.println(c);
      }*/
    this.depth = vars[0].getModel().getEnvironment().makeInt();
  }

  public static VarInConstraintStrategy findConstraints(final IntVar[] allVars, final Set<IntVar> featureVars, final List<Constraint> originalCrossConstraints, final Constraint[] allConstraints) {
    Map<Variable, List<Constraint>> varsToConstraints = new HashMap<Variable,List<Constraint>>();
    for (Constraint cstr : allConstraints) { // Associate all vars to the constraints they appear in
      for (Variable var : cstr.getPropagators()[0].getVars()) {
        List<Constraint> currentConstraints = varsToConstraints.getOrDefault(var, new ArrayList<Constraint>());
        currentConstraints.add(cstr);
        varsToConstraints.put(var, currentConstraints);
      }
    }
    List<Constraint> importantConstraints = new ArrayList<Constraint>();
    for (Constraint cstr : originalCrossConstraints) {
      importantConstraints.add(cstr);
      for (Variable var : cstr.getPropagators()[0].getVars())
          if (!featureVars.contains(var))
            fillConstraints(var, importantConstraints, featureVars, varsToConstraints);
    }
    return new VarInConstraintStrategy(allVars, importantConstraints);
  }
  
  private static void fillConstraints(final Variable currentVar, final List<Constraint> importantConstraints, final Set<IntVar> featureVars, final Map<Variable, List<Constraint>> varsToConstraints) {
    for (Constraint cstr : varsToConstraints.get(currentVar)) {
      if (!importantConstraints.contains(cstr)) {
        importantConstraints.add(cstr);
        for (Variable var : cstr.getPropagators()[0].getVars())
          if (!featureVars.contains(var))
            fillConstraints(var, importantConstraints, featureVars, varsToConstraints);
      }
    }
  }
  
  @Override
  protected Decision<IntVar> computeDecision(IntVar var) {
    throw new IllegalStateException("It is currently not possible to use computeDecision in VarInConstraintStrategy");
  }

  public Decision<IntVar> getDecision() {
    Map<IntVar,Integer> varsOccurences = new HashMap<IntVar,Integer>();
    depth.add(2);
    for (Constraint cstr : constraints) {
      if (cstr.isSatisfied() == ESat.UNDEFINED) { // Only choose the not yet satisfied constraints
        Propagator<IntVar> prop = cstr.getPropagators()[0];
        for (IntVar var : prop.getVars()) {
          if (!var.isInstantiated()) {// pick and uninstantiated variable
            varsOccurences.put(var,varsOccurences.getOrDefault(var,0)+1);
            return makeIntDecision(var, var.getLB()); // LB is 0, var should be a IntVar for the FM application
          }
        }
      }
    }
    /*IntVar bestVar = null;
    int bestVal = 10000000;
    for (Map.Entry<IntVar, Integer> entry : varsOccurences.entrySet()) {
      if (bestVar == null || entry.getValue() < bestVal) {
        bestVar = entry.getKey();
        bestVal = entry.getValue();
      }
    }
    if (bestVar != null)
    return makeIntDecision(bestVar, bestVar.getLB());*/
    return null;
  }
}
