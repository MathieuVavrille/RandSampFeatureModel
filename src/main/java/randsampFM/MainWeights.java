package randsampFM;

import randsampFM.featureDiagram.FeatureDiagram;
import randsampFM.types.Feature;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.javatuples.Pair;

import java.math.BigInteger;
import java.util.Map;
import java.nio.file.Paths;

/*
 * @author Mathieu Vavrille
 * since 01/09/2022
 * @version 0.1
 * */

@Command(name = "FeatureCount", mixinStandardHelpOptions = true, version = "0.1", description = "Counts for each feature, how many times it appears in solutions")
public final class MainWeights implements Runnable {
	
  @Option(names = {"-f", "--file"}, required = true, description = ".ulv file from which the feature model will be parsed.")
  private String inFile;
	
  @Option(names = {"-o", "--outfile"}, required = true, description = "Output .json file where the weights will be saved.")
  private String outFile;
  
  @Override
  public void run() {
    long startTime = System.nanoTime();
    FeatureModel fm = FeatureModel.parse(inFile);
    FeatureDiagram fd = fm.getFeatureDiagram();
    long parseTime = System.nanoTime();
    Map<Feature,BigInteger> solsPerFeature = fd.countSolutionsPerFeature(true);
    BigInteger count = fd.count();
    long weightTime = System.nanoTime();
    ObjectMapper mapper = new ObjectMapper();
    String jsonStr = null;
    try {
      mapper.writeValue(Paths.get(outFile).toFile(), new TimeAndCounts(weightTime-parseTime, parseTime-startTime, count, solsPerFeature));
    } catch (Exception e) {
      System.out.println(e.getStackTrace());
      System.exit(1);
    }
  }
	
  public static void main(String[] args) {
    int exitCode = new CommandLine(new MainWeights()).execute(args);
    System.exit(exitCode);
  }
		
}
