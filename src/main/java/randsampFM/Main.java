package randsampFM;

import randsampFM.featureDiagram.FeatureDiagram;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;

/*
 * @author Erwan Meunier
 * @author Mathieu Vavrille
 * since 04/08/2022
 * @version 0.1
 * */

@Command(name = "Randsampfm", mixinStandardHelpOptions = true, version = "RandSampFeatureModel 0.1", description = "Provides Feature Models enumeration, counting and sampling")
public final class Main implements Runnable {
  
  public final static long seed = 97;
  public final static Random generator = new Random(seed);

  /** Strategy to use when solving */
  @ArgGroup(exclusive = true, multiplicity = "0..1")
  CountingMethod countingMethod;
  static class CountingMethod {
    @Option(names = "-countingChoco", required=true, description="Runs the counting with the CP model.") boolean countingChoco;
    @Option(names = "-countingMinisat", required=true, description="Runs the counting with the Minisat #SAT solver. The transformation either uses Tseitin ('Tseitin' parameter) or develops the formula ('develop' parameter)") String countingMiniSat = "Tseitin";
    @Option(names = "-enumeration", required=true, description="Runs the counting by enumerating all the solutions. Highly inefficient") boolean enumerate;
  }
	
	
  @Option(names = {"-f", "--file"}, required = true, description = ".ulv file from which the feature model will be parsed.")
  private String path;
  
  @Option(names = {"-v","--verbose"}, required = false, description = "Verbose mode.")
  private boolean verbose = false;
  
  @ArgGroup(exclusive = false)
  DimacsOutput dimacsOutput;
  static class DimacsOutput {
    @Option(names = {"-d","--dimacs"}, required = true, description = "Saves the dimacs conversion to a file.") String fileName;
    @Option(names = {"-t","--tseitin"}, required = false, description = "Uses the Tseitin conversion") boolean isTseitin = false;
  }
  
  @Override
  public void run() {
    FeatureModel fm = FeatureModel.parse(path);

    if (dimacsOutput != null) {
      long dimacsGenerationTime = System.nanoTime();
      Stryng dimacsString = fm.toDimacs(dimacsOutput.isTseitin);
      try {
        FileWriter myWriter = new FileWriter(dimacsOutput.fileName);
        myWriter.write(dimacsString);
        myWriter.close();
      }
      catch (IOException e) {
        System.out.println("Cannot write to file " + dimacsOutput.fileName);
      }
    }
    
    if (countingMethod != null) {
      long startCountingTime = System.nanoTime();
      if (countingMethod.countingChoco) {
        System.out.println(fm.removeConstraints().count());
        System.out.println("Counting time with Choco = " + (System.nanoTime()-startCountingTime));
      }
      else if (countingMethod.countingMiniSat != null) {
        System.out.println(countingMethod.countingMiniSat);
        System.out.println(fm.getMiniSatInstance(countingMethod.countingMiniSat.equals("Tseitin")).count(fm.getFeatureDiagram(), fm.getSiLink()));
        System.out.println("Counting time with minisat " + (countingMethod.countingMiniSat.equals("Tseitin") ? "Tseitin" : "developed") + " = " + (System.nanoTime()-startCountingTime));
      }
      else {
        System.out.println(fm.enumerate().size());
        System.out.println("Counting time with enumeration = " + (System.nanoTime()-startCountingTime));
      }
    }
		
  }
	
  public static void main(String[] args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }
		
}
