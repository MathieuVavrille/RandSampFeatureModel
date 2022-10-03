package randsampFM;

import randsampFM.featureDiagram.*;
import randsampFM.constraints.*;
import randsampFM.types.*;
import randsampFM.splittedFM.*;
import randsampFM.twise.*;
import randsampFM.parser.UVLParser;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.search.limits.FailCounter;

import org.javatuples.Pair;

import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.math.BigInteger;
import java.util.Comparator;
//import java.util.Comparator;
import java.util.stream.Collectors;

public class TestMain {

  public static final Random random = new Random(97);

  public static void main(String[] args) {
    //assertCounts();
    testRandomDiffFrequency("./models/automotive01.uvl");
  }

  private static void testRandomDiffFrequency(final String path) {
    long startTime = System.nanoTime();
    FeatureModel fm = FeatureModel.parse(path);
    List<Map<Feature,Boolean>> solutions = new ArrayList<Map<Feature,Boolean>>();
    Map<Feature,BigInteger> totalSolsPerFeature = fm.getFeatureDiagram().countSolutionsPerFeature(false);
    final Map<Feature,BoolVar> featureToVar = new HashMap<Feature,BoolVar>();
    Model model = generateModel(fm, featureToVar);
    Solver solver = model.getSolver();
    RandomFrequencyStrategy strat = new RandomFrequencyStrategy(solutions, totalSolsPerFeature, fm.getFeatureDiagram().count(), featureToVar, random);
    solver.plugMonitor(strat);
    solver.setSearch(strat);
    solver.setRestartOnSolutions();
    solver.setNoGoodRecordingFromSolutions(model.retrieveIntVars(true));
    solver.setLubyRestart(50L, new FailCounter(model, 50), Integer.MAX_VALUE);
    int solCpt = 0;
    while (solCpt < 100 && solver.solve()) {
      System.out.println(solCpt++);
    }
    List<Feature> features = featureToVar.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
    //System.out.println(features);
    try {
      FileWriter myWriter = new FileWriter("test.sols");
      for (int i = 0; i < solutions.size(); i++) {
        myWriter.write(i + ",");
        for (int j = 0; j < features.size(); j++)
          myWriter.write((solutions.get(i).get(features.get(j)) ? " " : " -") + (j+1));
        myWriter.write("\n");
      }
      myWriter.close();
    }
    catch (IOException e) {
      System.out.println("Cannot write to file");
    }
  }

  private static void testBestDiffFrequency(final String path) {
    long startTime = System.nanoTime();
    FeatureModel fm = FeatureModel.parse(path);
    List<Map<Feature,Boolean>> solutions = new ArrayList<Map<Feature,Boolean>>();
    Map<Feature,BigInteger> totalSolsPerFeature = fm.getFeatureDiagram().countSolutionsPerFeature(false);
    final Map<Feature,BoolVar> featureToVar = new HashMap<Feature,BoolVar>();
    Model model = generateModel(fm, featureToVar);
    Solver solver = model.getSolver();
    BestFrequencyStrategy strat = new BestFrequencyStrategy(solutions, totalSolsPerFeature, fm.getFeatureDiagram().count(), featureToVar);
    solver.plugMonitor(strat);
    solver.setSearch(strat);
    solver.setRestartOnSolutions();
    solver.setNoGoodRecordingFromSolutions(model.retrieveIntVars(true));
    int solCpt = 0;
    while (solCpt < 100 && solver.solve()) {
      System.out.println(++solCpt);
    }
    List<Feature> features = featureToVar.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
    //System.out.println(features);
    try {
      FileWriter myWriter = new FileWriter("test.sols");
      for (int i = 0; i < solutions.size(); i++) {
        myWriter.write(i + ",");
        for (int j = 0; j < features.size(); j++)
          myWriter.write((solutions.get(i).get(features.get(j)) ? " " : " -") + (j+1));
        myWriter.write("\n");
      }
      myWriter.close();
    }
    catch (IOException e) {
      System.out.println("Cannot write to file");
    }
  }
  
  









  

  private static void testPerSolutionCombination(final String path) {
    long startTime = System.nanoTime();
    FeatureModel fm = FeatureModel.parse(path);
    List<Map<Feature,Boolean>> solutions = new ArrayList<Map<Feature,Boolean>>();
    Map<Feature,BigInteger> solsPerFeature = fm.getFeatureDiagram().countSolutionsPerFeature(false);
    FeatureWiseCombinations.setFeatures(fm.getFeatureDiagram().getFeatures().stream().collect(Collectors.toList()));
    List<Pair<Feature,Boolean>> featuresTruthSorted = sortFeaturesByCount(solsPerFeature, fm.getFeatureDiagram().count());
    System.out.println(featuresTruthSorted);
    System.out.println(solsPerFeature);
    // Feature-wise coverage
    FeatureWiseCombinations impossibleFeatures = completeFeatureWiseCoverage(solutions, new ArrayList<Map<Feature,Boolean>>(), new ArrayList<Pair<Feature,Boolean>>(), new FeatureWiseCombinations(), featuresTruthSorted, fm);
    System.out.println("IMPOSSIBLE FEATURES " + impossibleFeatures);
    System.out.println("SOLUTIONS SIZE AFTER FEATUREWISE " + solutions.size());
    Map<Pair<Feature,Boolean>, FeatureWiseCombinations> featureToImpossible = new HashMap<Pair<Feature,Boolean>, FeatureWiseCombinations>();
    for (Pair<Feature,Boolean> featureTruth : featuresTruthSorted) {
      featureToImpossible.put(featureTruth, completeFeatureWiseCoverage(solutions, restrictSolutions(solutions, featureTruth.getValue0(), featureTruth.getValue1()), List.of(featureTruth), new FeatureWiseCombinations(), featuresTruthSorted, fm));
    }
    //System.out.println("FEATURE TO IMPOSSIBLE " + featureToImpossible);
    System.out.println("SOLUTIONS SIZE AFTER PAIRWISE " + solutions.size());
    twiseCoverage(3, solutions, impossibleFeatures, featureToImpossible, featuresTruthSorted, fm);
    System.out.println(solutions.size());
    System.out.println((System.nanoTime()-startTime)/1000000);
  }

  private static List<Pair<Feature,Boolean>> sortFeaturesByCount(final Map<Feature,BigInteger> solsPerFeature, final BigInteger total) {
    List<Pair<Feature,Boolean>> sorted = new ArrayList<Pair<Feature,Boolean>>();
    int cpt = 0;
    for (Map.Entry<Feature,BigInteger> entry : solsPerFeature.entrySet()) {
      if (!entry.getValue().equals(total)) {
        if (!entry.getValue().equals(BigInteger.ZERO))
          sorted.add(new Pair<Feature,Boolean>(entry.getKey(), true));
        if (!entry.getValue().equals(total))
          sorted.add(new Pair<Feature,Boolean>(entry.getKey(), false));
      }
    }
    sorted.sort(Comparator.comparing(p -> p.getValue1() ? solsPerFeature.get(p.getValue0()) : total.subtract(solsPerFeature.get(p.getValue0()))));
    return sorted;
  }

  private static Model generateModel(final FeatureModel fm, final Map<Feature,BoolVar> featureToVar) {
    final Model model = new Model("Generated");
    fm.getFeatureDiagram().addConstraints(model, featureToVar).eq(1).post();
    for (CrossConstraint cstr : fm.getCrossConstraints())
      cstr.getCPConstraint(featureToVar).post();
    return model;
  }

  private static List<Map<Feature,Boolean>> restrictSolutions(final List<Map<Feature,Boolean>> solutions, final Feature mainFeature, final boolean value) {
    return solutions.stream().filter(sol -> sol.get(mainFeature) == value).collect(Collectors.toList());
  }

  private static FeatureWiseCombinations extractSeenCombinations(final List<Map<Feature,Boolean>> restrictedSolutions, final Map<Feature,BoolVar> featureToVar) {
    FeatureWiseCombinations seenCombinations = new FeatureWiseCombinations();
    for (Map<Feature,Boolean> solution : restrictedSolutions) {
      for (Map.Entry<Feature,Boolean> entry : solution.entrySet())
        seenCombinations.set(entry.getKey(), entry.getValue());
    }
    return seenCombinations;
  }

  
  private static FeatureWiseCombinations completeFeatureWiseCoverage(final List<Map<Feature,Boolean>> solutions, final List<Map<Feature,Boolean>> restrictedSolutions, final List<Pair<Feature,Boolean>> fixedFeatures, final FeatureWiseCombinations impossibleFeatures, final List<Pair<Feature,Boolean>> featuresTruthSorted, final FeatureModel fm) {
    int startingSolutionSize = solutions.size();
    System.out.println("\nFIXED = " + fixedFeatures);
    System.out.println("RESTRICTED SOLUTIONS SIZE " + restrictedSolutions.size());
    //System.out.println("\nStart new Feature-Wise coverage with feature " + mainFeature + " and value " + truthValue);
    final Map<Feature,BoolVar> featureToVar = new HashMap<Feature,BoolVar>();
    Model model = generateModel(fm, featureToVar);

    // fix features from the selected features, or the impossible ones
    for (Pair<Feature,Boolean> fixed : fixedFeatures)
      featureToVar.get(fixed.getValue0()).eq(fixed.getValue1() ? 1 : 0).post();
    //impossibleFeatures.setVariables(featureToVar);

    Solver solver = model.getSolver();
    // Propagate once to reduce possible combinations
    try {
      solver.propagate();
    } catch (Exception e) {
      return new FeatureWiseCombinations(true); // Propagation found a contradiction, no solution
    }
    FeatureWiseCombinations seenCombinations = extractSeenCombinations(restrictedSolutions, featureToVar);
    for (Map.Entry<Feature,BoolVar> entry : featureToVar.entrySet()) {
      if (entry.getValue().isInstantiated())
        impossibleFeatures.set(entry.getKey(), entry.getValue().getValue()==0);
    }
    
    solver.plugMonitor(new CombinationHistoryMonitor(solutions, seenCombinations, featureToVar));
    solver.setSearch(new FeatureWiseCoverageStrategy(model, seenCombinations, impossibleFeatures, featureToVar, featuresTruthSorted), Search.defaultSearch(model));
    solver.setRestartOnSolutions();
    
    while (solver.solve()) {
      System.out.println(solutions.get(solutions.size()-1));
    }
    System.out.println((solutions.size()-startingSolutionSize) + " new solutions");
    return impossibleFeatures;
  }

  private static void twiseCoverage(final int t, final List<Map<Feature,Boolean>> solutions, final FeatureWiseCombinations impossibleFeatures, final Map<Pair<Feature,Boolean>,FeatureWiseCombinations> featureToImpossible, final List<Pair<Feature,Boolean>> featuresTruthSorted, final FeatureModel fm) {
    twiseCoverageRec(t, 0, solutions, solutions, new ArrayList<Pair<Feature,Boolean>>(), impossibleFeatures, featureToImpossible, featuresTruthSorted, fm);
  }

  private static void twiseCoverageRec(final int t, final int minId, final List<Map<Feature,Boolean>> solutions, final List<Map<Feature,Boolean>> restrictedSolutions, final List<Pair<Feature,Boolean>> fixedFeatures, final FeatureWiseCombinations impossibleFeatures, final Map<Pair<Feature,Boolean>,FeatureWiseCombinations> featureToImpossible, final List<Pair<Feature,Boolean>> featuresTruthSorted, final FeatureModel fm) {
    if (t == 1) {
      System.out.println(fixedFeatures);
      completeFeatureWiseCoverage(solutions, restrictedSolutions, fixedFeatures, impossibleFeatures, featuresTruthSorted, fm);
    }
    else {
      for (int currentId = minId; currentId < featuresTruthSorted.size()-t+2; currentId++) {
        Feature currentFeature = featuresTruthSorted.get(currentId).getValue0();
        boolean truthValue = featuresTruthSorted.get(currentId).getValue1();
        //System.out.println(fixedFeatures + " " +  currentFeature + " " + impossibleFeatures.contains(currentFeature, truthValue));
        if (!(impossibleFeatures.contains(currentFeature, false) || impossibleFeatures.contains(currentFeature, true))) {
          FeatureWiseCombinations currentImpossibleFeatures = impossibleFeatures.unionNew(featureToImpossible.get(featuresTruthSorted.get(currentId)));
          List<Map<Feature,Boolean>> newRestrictedSolutions = restrictSolutions(restrictedSolutions, currentFeature, truthValue);
          fixedFeatures.add(featuresTruthSorted.get(currentId));
          twiseCoverageRec(t-1, currentId+1, solutions, newRestrictedSolutions, fixedFeatures, currentImpossibleFeatures, featureToImpossible, featuresTruthSorted, fm);
          fixedFeatures.remove(fixedFeatures.size()-1);
        }
      }
    }
  }

}
