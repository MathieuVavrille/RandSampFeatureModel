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

import org.javatuples.Pair;
import org.javatuples.Triplet;


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
import java.util.stream.Collectors;

public class TestMain {

  public static void main(String[] args) {
    //assertCounts();
    testPerSolutionCombination("./models/jhipster.uvl");
  }

  private static void testPerSolutionCombination(final String path) {
    long startTime = System.nanoTime();
    FeatureModel fm = FeatureModel.parse(path);
    List<Map<Feature,Boolean>> solutions = new ArrayList<Map<Feature,Boolean>>();
    Map<Feature,BigInteger> solsPerFeature = fm.getFeatureDiagram().countSolutionsPerFeature(true);
    List<Triplet<BigInteger,Feature,Boolean>> featuresTruthSorted = sortFeaturesByCount(solsPerFeature, fm.getFeatureDiagram().count());
    System.out.println(featuresTruthSorted);
    for (Triplet<BigInteger,Feature,Boolean> featureTruth : featuresTruthSorted) {
      completeFeatureWiseCoverage(solutions, featureTruth.getValue1(), featureTruth.getValue2(), fm);
    }
    System.out.println(solutions.size());
    System.out.println((System.nanoTime()-startTime)/1000000);
  }

  private static List<Triplet<BigInteger,Feature,Boolean>> sortFeaturesByCount(final Map<Feature,BigInteger> solsPerFeature, final BigInteger total) {
    List<Triplet<BigInteger,Feature,Boolean>> sorted = new ArrayList<Triplet<BigInteger,Feature,Boolean>>();
    int cpt = 0;
    for (Map.Entry<Feature,BigInteger> entry : solsPerFeature.entrySet()) {
      if (!entry.getValue().equals(total)) {
        sorted.add(new Triplet<BigInteger,Feature,Boolean>(entry.getValue(), entry.getKey(), true));
        sorted.add(new Triplet<BigInteger,Feature,Boolean>(total.subtract(entry.getValue()), entry.getKey(), false));
      }
    }
    sorted.sort(Comparator.naturalOrder());
    return sorted;
  }

  private static Model generateModel(final FeatureModel fm, final Map<Feature,BoolVar> featureToVar) {
    final Model model = new Model("Generated");
    fm.getFeatureDiagram().addConstraints(model, featureToVar).eq(1).post();
    for (CrossConstraint cstr : fm.getCrossConstraints())
      cstr.getCPConstraint(featureToVar).post();
    return model;
  }

  private static Map<Feature, Pair<CombinationStatus,CombinationStatus>> extractCombinations(final List<Map<Feature,Boolean>> solutions, final Feature mainFeature, final boolean value, final Map<Feature,BoolVar> featureToVar) {
    List<Map<Feature,Boolean>> restrictedSolutions = solutions.stream().filter(sol -> sol.get(mainFeature) == value).collect(Collectors.toList());
    Map<Feature, Pair<CombinationStatus,CombinationStatus>> combinations = new HashMap<Feature, Pair<CombinationStatus,CombinationStatus>>();
    for (Map.Entry<Feature, BoolVar> entry : featureToVar.entrySet()) {
      combinations.put(entry.getKey(), statusFromSolutionsAndPropagatedVar(restrictedSolutions, entry.getValue(), entry.getKey()));
    }
    return combinations;
  }

  private static Pair<CombinationStatus,CombinationStatus> statusFromSolutionsAndPropagatedVar(final List<Map<Feature,Boolean>> solutions, final BoolVar var, final Feature feature) {
    boolean hasFalse = false;
    boolean hasTrue = false;
    for (int i = 0; i < solutions.size() && !(hasFalse && hasTrue); i++) {
      if (solutions.get(i).get(feature))
        hasTrue = true;
      else
        hasFalse = true;
    }
    return new Pair<CombinationStatus,CombinationStatus>(hasFalse ? CombinationStatus.FOUND : (var.contains(0) ? CombinationStatus.UNSEEN : CombinationStatus.IMPOSSIBLE), hasTrue ? CombinationStatus.FOUND : (var.contains(1) ? CombinationStatus.UNSEEN : CombinationStatus.IMPOSSIBLE));
  }


  
  private static void completeFeatureWiseCoverage(final List<Map<Feature,Boolean>> solutions, final Feature mainFeature, final boolean truthValue, final FeatureModel fm) {
    int startingSolutionSize = solutions.size();
    System.out.println("\nStart new Feature-Wise coverage with feature " + mainFeature + " and value " + truthValue);
    final Map<Feature,BoolVar> featureToVar = new HashMap<Feature,BoolVar>();
    Model model = generateModel(fm, featureToVar);
    featureToVar.get(mainFeature).eq(truthValue ? 1 : 0).post();
    List<Feature> allFeatures = featureToVar.entrySet().stream().map(s -> s.getKey()).collect(Collectors.toList());
    Solver solver = model.getSolver();
    try {
      solver.propagate();
    } catch (Exception e) {
      return; // Propagation found a contradiction, no solution
    }
    Map<Feature, Pair<CombinationStatus,CombinationStatus>> combinations = extractCombinations(solutions, mainFeature, truthValue, featureToVar);
    solver.plugMonitor(new CombinationHistoryMonitor(solutions, combinations, featureToVar));
    solver.setSearch(new FeatureWiseCoverageStrategy(model, combinations, featureToVar), Search.defaultSearch(model));
    solver.setRestartOnSolutions();
    //System.out.println(combinations);
    while (solver.solve()) {
      //System.out.println("");
      //System.out.println(solutions.get(solutions.size()-1));
      //System.out.println(combinations);
    }
    System.out.println((solutions.size()-startingSolutionSize) + " new solutions");
  }

}
