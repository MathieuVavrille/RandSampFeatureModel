package randsampFM;

import randsampFM.featureDiagram.*;
import randsampFM.constraints.*;
import randsampFM.types.*;
import randsampFM.splittedFM.*;

import de.neominik.uvl.ast.UVLModel;
import de.neominik.uvl.UVLParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.math.BigInteger;

public class TestMain {
  public static void main(String[] args) {
    String path = "../uvl-models/Feature_Models/Operating_Systems/KConfig/axTLS.uvl";
    long startTime = System.nanoTime();
    FeatureModel fm = FeatureModel.parse(path);
    long parseTime = System.nanoTime();
    System.out.println((parseTime-startTime)/1000000);
    //System.out.println(fm);
    //BigInteger nbConfigurations = fm.count();
    SplittedFDList sfd = fm.removeConstraints();
    long removeTime = System.nanoTime();
    System.out.println((removeTime-parseTime)/1000000);
    BigInteger splittedNb = sfd.count();
    long countTime = System.nanoTime();
    System.out.println((countTime-removeTime)/1000000);
    System.out.println(sfd.count());
    //System.out.println(splittedNb.equals(nbConfigurations));
    System.out.println((System.nanoTime()-startTime)/1000000);
    System.out.println(sfd.countsRepartition());
    System.out.println(sfd.getFDs().size());
    //System.out.println(sfd.enumerate());
    //sfd.saveGraphvizToFolder("./graphviz_outputs/berkeleydb");
    //System.out.println(sfd.get(0).toUVL(""));
    //System.out.println(fm);
    /*for (FeatureDiagram fd : sfd.getFDs()) {
      System.out.println(fd);
      System.out.println(fd.enumerate());
      System.out.println(fd.count());
      }*/
  }
}
