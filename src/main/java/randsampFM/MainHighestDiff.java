package randsampFM;

import randsampFM.featureDiagram.FeatureDiagram;
import randsampFM.twise.BestFrequencyStrategy;
import randsampFM.constraints.CrossConstraint;
import randsampFM.types.Feature;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.search.limits.SolutionCounter;
import org.chocosolver.solver.search.limits.TimeCounter;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


import java.math.BigInteger;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.FileWriter;
import java.util.stream.Collectors;

/*
 * @author Mathieu Vavrille
 * since 01/09/2022
 * @version 0.1
 * */

@Command(name = "HighestDiffStrategy", mixinStandardHelpOptions = true, version = "0.1", description = "Generates solutions by doing a strategy choosing the variable that has been sampled with a frequency far from its expected frequency")
public final class MainHighestDiff implements Runnable {
	
  @Option(names = {"-f", "--file"}, required = true, description = ".ulv file from which the feature model will be parsed.")
  private String inFile;
	
  @Option(names = {"-s", "--samples"}, required = false, description = "number of samples to draw.")
  private int nbSamples = 100;
	
  @Option(names = {"-o", "--outfile"}, required = true, description = "Output .json file where the weights will be saved.")
  private String outFile;

  @Option(names = {"-t", "--time"}, required = false, description = "Time limit for each sample, in seconds. No limit by default")
  private long timeLimit = 0L;
  
  @Override
  public void run() {
    long startTime = System.nanoTime();
    FeatureModel fm = FeatureModel.parse(inFile);
    long parseTime = System.nanoTime();
    Map<Feature,BigInteger> totalSolsPerFeature = fm.getFeatureDiagram().countSolutionsPerFeature(false);
    long countingTime = System.nanoTime();
    List<Map<Feature,Boolean>> solutions = new ArrayList<Map<Feature,Boolean>>();
    final Map<Feature,BoolVar> featureToVar = new HashMap<Feature,BoolVar>();
    Model model = generateModel(fm, featureToVar);
    Solver solver = model.getSolver();
    BestFrequencyStrategy strat = new BestFrequencyStrategy(solutions, totalSolsPerFeature, fm.getFeatureDiagram().count(), featureToVar);
    solver.plugMonitor(strat);
    solver.setSearch(strat);
    solver.setRestartOnSolutions();
    solver.setNoGoodRecordingFromSolutions(model.retrieveIntVars(true));
    if (timeLimit == 0L)
      solver.findAllSolutions(new SolutionCounter(model, nbSamples));
    else
      solver.findAllSolutions(new SolutionCounter(model, nbSamples), new TimeCounter(model, timeLimit*1000000000L))
    long totalTime = System.nanoTime();
    List<Feature> features = featureToVar.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
    try {
      FileWriter myWriter = new FileWriter(outFile);
      myWriter.write((parseTime - startTime) + " " + (countingTime-parseTime) + " " + (totalTime-countingTime)+"\n");
      myWriter.write(features + "\n");
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
	
  public static void main(String[] args) {
    int exitCode = new CommandLine(new MainHighestDiff()).execute(args);
    System.exit(exitCode);
  }

  private Model generateModel(final FeatureModel fm, final Map<Feature,BoolVar> featureToVar) {
    final Model model = new Model("Generated");
    fm.getFeatureDiagram().addConstraints(model, featureToVar).eq(1).post();
    for (CrossConstraint cstr : fm.getCrossConstraints())
      cstr.getCPConstraint(featureToVar).post();
    return model;
  }
		
}
