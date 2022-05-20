package randsampFM.constraints;

import randsampFM.types.Configuration;
import randsampFM.types.ConfSet;
import randsampFM.FMSampleCountEnum;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import de.neominik.uvl.ast.*;

import org.javatuples.Pair;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public abstract class CrossConstraint {
  
  /** Fix the features in `forced` to true, and `forbidden` to false.
   * The two sets have to be disjoint. No check will be done !
   * @returns a boolean telling if the constraint was modified or not, and the new constraint  */
  public abstract Pair<Boolean,CrossConstraint> fixVariable(final Set<randsampFM.types.Feature> forced, final Set<randsampFM.types.Feature> forbidden);
  
  /** check whether `this` is satisfied by the configuration  */
  public abstract boolean isSatisfied(final Configuration configuration);

  public ConfSet filterConfSet(final ConfSet notConstrained) {
    Set<Configuration> filteredConfigurations = new HashSet<Configuration>();
    for (Configuration conf : notConstrained.getInnerSet()) {
      if (this.isSatisfied(conf))
        filteredConfigurations.add(conf);
    }
    return new ConfSet(filteredConfigurations);
  }
  
  /** Returns all the variables of the constraint */
  public abstract Set<randsampFM.types.Feature> getVariables();

  /** Returns the ReExpression representing the crossConstraint */
  public abstract ReExpression getCPConstraint(final Map<randsampFM.types.Feature,BoolVar> featureToVar);

  /** Returns the set of features necessarily set to `true`, and the ones necessarily set to `false`, when the goal is to set the formula to `true` */
  public abstract Pair<Set<randsampFM.types.Feature>,Set<randsampFM.types.Feature>> forcedFeaturesForTrue();
  /** Returns the set of features necessarily set to `true`, and the ones necessarily set to `false`, when the goal is to set the formula to `false` */
  public abstract Pair<Set<randsampFM.types.Feature>,Set<randsampFM.types.Feature>> forcedFeaturesForFalse();

  /** Create the constraint from an UVLModel constraint. Depending on the type of the constraint, creates the right class. */
  public static CrossConstraint fromUVLConstraint(final Object constraint) {
    if (constraint instanceof Not) {
      Not notCstr = (Not) constraint;
      return NotCstr.of(fromUVLConstraint(notCstr.getChild()));
    }
    else if (constraint instanceof And) {
      And andCstr = (And) constraint;
      return AndCstr.of(fromUVLConstraint(andCstr.getLeft()), fromUVLConstraint(andCstr.getRight()));
    }
    else if (constraint instanceof Or) {
      Or orCstr = (Or) constraint;
      return OrCstr.of(fromUVLConstraint(orCstr.getLeft()), fromUVLConstraint(orCstr.getRight()));
    }
    else if (constraint instanceof Impl) {
      Impl implCstr = (Impl) constraint;
      return ImplCstr.of(fromUVLConstraint(implCstr.getLeft()), fromUVLConstraint(implCstr.getRight()));
    }
    else if (constraint instanceof Equiv) {
      Equiv equivCstr = (Equiv) constraint;
      return EquivCstr.of(fromUVLConstraint(equivCstr.getLeft()), fromUVLConstraint(equivCstr.getRight()));
    }
    else { // It is a string
      String stringCstr = (String) constraint; // If bug here, then you didn't give a constraint from UVL
      switch (stringCstr) {
      case "true":
        return new TrueCstr();
      case "false":
        return new FalseCstr();
      default:
        return new LitteralCstr(new randsampFM.types.Feature(stringCstr));
      }
    }
  }
}
