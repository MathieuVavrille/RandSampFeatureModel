package randsampFM.constraints;

import randsampFM.types;

import de.neominik.uvl.ast.*;

import org.javatuples.Pair;

public abstract class Constraint {

  public static fromUVLConstraint(final Object constraint) {
    if (constraint instanceof Not) {
      Not notCstr = (Not) constraint;
      return new NotCstr(fromUVLConstraint(notCstr.child));
    }
    else if (constraint instanceof And) {
      And andCstr = (And) constraint;
      return new AndCstr(fromUVLConstraint(andCstr.left), fromUVLConstraint(andCstr.right));
    }
    else if (constraint instanceof Or) {
      Or orCstr = (Or) constraint;
      return new OrCstr(fromUVLConstraint(orCstr.left), fromUVLConstraint(orCstr.right));
    }
    else if (constraint instanceof Impl) {
      Impl implCstr = (Impl) constraint;
      return new ImplCstr(fromUVLConstraint(implCstr.left), fromUVLConstraint(implCstr.right));
    }
    else if (constraint instanceof Equiv) {
      Equiv equivCstr = (Equiv) constraint;
      return new EquivCstr(fromUVLConstraint(equivCstr.left), fromUVLConstraint(equivCstr.right));
    }
    else { // It is a string
      String stringCstr = (String) constraint; // If bug here, then you didn't give a constraint from UVL
      switch (stringCstr) {
      case "true":
        return new TrueCstr();
      case "false":
        return new FalseCstr();
      default:
        return new LitteralCstr(stringCstr);
      }
    }
  }
  
  /** Fix the given feature, and returns the new constraint, and a boolean telling if the constraint was modified or not */
  public abstract Pair<Boolean,Constraint> fixVariable(final Feature feature);
  /** Returns all the variables of the constraint */
  public abstract Set<Feature> getVariables();
    
  public isTrue() { return False; }
  public isFalse() { return False; }
}
