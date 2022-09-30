package randsampFM.twise;

import randsampFM.types.Feature;

import org.chocosolver.memory.IStateBool;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.BoolVar;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.List;
import java.util.Map;

public class FeatureWiseCoverageStrategy extends AbstractStrategy<BoolVar> {

  private final FeatureWiseCombinations seenCombinations;
  private final FeatureWiseCombinations impossibleCombinations;
  private final Map<Feature, BoolVar> featureToVar;
  private final List<Pair<Feature, Boolean>> featureTruthSorted;
  
  private final IStateBool isFirstDecision;
  private int firstInterestingCombination = 0;
  private final IStateInt backtrackableFirstCombination;

  public FeatureWiseCoverageStrategy(final Model model, final FeatureWiseCombinations seenCombinations, final FeatureWiseCombinations impossibleCombinations, final Map<Feature, BoolVar> featureToVar, final List<Pair<Feature, Boolean>> featureTruthSorted) {
    this.seenCombinations = seenCombinations;
    this.impossibleCombinations = impossibleCombinations;
    this.featureToVar = featureToVar;
    this.isFirstDecision = model.getEnvironment().makeBool(true);
    this.backtrackableFirstCombination = model.getEnvironment().makeInt(firstInterestingCombination);
    this.featureTruthSorted = featureTruthSorted;
  }
  

  @Override
  public Decision<BoolVar> getDecision() {
    if (isFirstDecision.get())
      backtrackableFirstCombination.set(firstInterestingCombination);
    Pair<Feature,Boolean> chosenCombination = getUnseenCombination();
    if (isFirstDecision.get()) {
      firstInterestingCombination = backtrackableFirstCombination.get();
      if (chosenCombination == null)
        return new FailDecision(getOneVar());
      else
        return new BoolEqRootDecision(featureToVar.get(chosenCombination.getValue0()), chosenCombination.getValue1(), isFirstDecision, chosenCombination.getValue0(), impossibleCombinations);
    }
    else {
      if (chosenCombination == null)
        return null;
      else
        return new BoolDecision(featureToVar.get(chosenCombination.getValue0()), chosenCombination.getValue1());
    }
  }

  /** Returns one variable. There is no property, and it should only be used as a placeholder */
  private BoolVar getOneVar() {
    for (Map.Entry<Feature,BoolVar> entry : featureToVar.entrySet())
      return entry.getValue();
    throw new IllegalStateException("There are no variables");
  }

  private Pair<Feature,Boolean> getUnseenCombination() {
    int i = backtrackableFirstCombination.get();
    while (i < featureTruthSorted.size()) {
      Feature currentFeature = featureTruthSorted.get(i).getValue0();
      boolean currentTruth = featureTruthSorted.get(i).getValue1();
      if (!featureToVar.get(currentFeature).isInstantiated() && !seenCombinations.contains(currentFeature, currentTruth)) {
        backtrackableFirstCombination.set(i);
        return new Pair<Feature,Boolean>(currentFeature, currentTruth);
      }
      i++;
    }
    backtrackableFirstCombination.set(i);
    return null;
  }
  
}
