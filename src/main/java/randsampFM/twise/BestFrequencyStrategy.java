package randsampFM.twise;

import randsampFM.types.Feature;


import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;



import java.math.BigInteger;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;



public class BestFrequencyStrategy extends AbstractStrategy<IntVar> implements IMonitorSolution {

  private final List<Map<Feature,Boolean>> solutions;
  private final Map<Feature,BigInteger> totalSolsPerFeature;
  private final BigInteger totalCount;
  private final Map<Feature,BoolVar> featureToVar;
  private Map<Feature,BigInteger> foundSolsPerFeature;
  private BigInteger solutionSize;
  private Map<Feature,BigInteger> differenceToFrequency; // Scaled by solutionSize*totalCount
  private final IStateInt depth;

  public BestFrequencyStrategy(final List<Map<Feature,Boolean>> solutions, final Map<Feature,BigInteger> totalSolsPerFeature, final BigInteger totalCount, final Map<Feature,BoolVar> featureToVar) {
    this.solutions = solutions;
    this.totalSolsPerFeature = totalSolsPerFeature;
    this.totalCount = totalCount;
    this.featureToVar = featureToVar;
    this.foundSolsPerFeature = totalSolsPerFeature.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> BigInteger.ZERO));
    this.solutionSize = BigInteger.ZERO;
    this.differenceToFrequency = totalSolsPerFeature.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> BigInteger.ZERO));
    this.depth = getOneVar().getModel().getEnvironment().makeInt(0);
  }
  

  @Override
  public Decision<IntVar> getDecision() {
    //System.out.println(depth.get() + " getDecision");
    depth.set(depth.get()+1);
    BoolVar currentVariable = null;
    BigInteger currentDiffAbs = null;
    Boolean currentTruth = null;
    int nbUninstantiated = 0;
    for (Map.Entry<Feature, BigInteger> diff : differenceToFrequency.entrySet()) {
      if (!featureToVar.get(diff.getKey()).isInstantiated())
        nbUninstantiated++;
      if (!featureToVar.get(diff.getKey()).isInstantiated() && (currentVariable == null || currentDiffAbs.compareTo(diff.getValue().abs()) < 1)) {
        currentVariable = featureToVar.get(diff.getKey());
        currentDiffAbs = diff.getValue().abs();
        currentTruth = diff.getValue().compareTo(BigInteger.ZERO) <= 0;
      }
    }
    //System.out.println(nbUninstantiated + " " + currentVariable + " " + currentTruth);
    if (currentVariable != null)
      return makeIntDecision(currentVariable, currentTruth ? 1 : 0);//new BoolDecision(currentVariable, currentTruth);
    else
      return null;
  }

  @Override
  public void onSolution() {
    Map<Feature,Boolean> currentSolution = featureToVar.entrySet().stream().collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue().getValue() == 1));
    for (Map.Entry<Feature,Boolean> entry : currentSolution.entrySet())
      if (entry.getValue())
        foundSolsPerFeature.put(entry.getKey(), foundSolsPerFeature.get(entry.getKey()).add(BigInteger.ONE));
    this.solutionSize = solutionSize.add(BigInteger.ONE);
    solutions.add(currentSolution);
    recomputeDifferences();
    //System.out.println(solutions.size());
  }

  private void recomputeDifferences() {
    for (Map.Entry<Feature,BigInteger> total : totalSolsPerFeature.entrySet())
      this.differenceToFrequency.put(total.getKey(),
                                     totalCount.multiply(foundSolsPerFeature.get(total.getKey()))
                                     .subtract(solutionSize.multiply(total.getValue())));
  }
  
  /** Returns one variable. There is no property, and it should only be used as a placeholder */
  private BoolVar getOneVar() {
    for (Map.Entry<Feature,BoolVar> entry : featureToVar.entrySet())
      return entry.getValue();
    throw new IllegalStateException("There are no variables");
  }
}
