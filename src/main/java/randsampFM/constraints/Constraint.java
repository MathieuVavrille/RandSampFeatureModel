package randsampFM.constraints;

import randsampFM.types.Feature;

import de.neominik.uvl.ast.*;

import org.javatuples.Pair;

import java.util.Set;

public abstract class Constraint {

  public static Constraint fromUVLConstraint(final Object constraint) {
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
        return new LitteralCstr(new Feature(stringCstr));
      }
    }
  }
  
  /** Fix the given feature, and returns the new constraint, and a boolean telling if the constraint was modified or not */
  public abstract Pair<Boolean,Constraint> fixVariable(final Feature feature);
  /** Returns all the variables of the constraint */
  public abstract Set<Feature> getVariables();
}
