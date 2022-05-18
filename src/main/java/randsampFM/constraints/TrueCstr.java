package randsampFM.constraints;

import randsampFM.types.Feature;

import org.javatuples.Pair;

import java.util.Set;

public class TrueCstr extends Constraint {

  @Override
  public Pair<Boolean,Constraint> fixVariable(final Feature feature, final boolean value) {
    return new Pair<Boolean, Constraint>(false, this);
  }

  @Override
  public Set<Feature> getVariables() {
    return Set.of();
  }

  @Override
  public String toString() {
    return "TRUE";
  }
}
