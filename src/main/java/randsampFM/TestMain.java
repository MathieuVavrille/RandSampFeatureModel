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
    String path = "./models/jhipster.uvl";
    FeatureModel fm = FeatureModel.parse(path);
    //BigInteger nbConfigurations = fm.count();
    long startTime = System.nanoTime();
    SplittedFDList sfd = fm.removeConstraints();
    BigInteger splittedNb = sfd.count();
    System.out.println(sfd.count());
    //System.out.println(splittedNb.equals(nbConfigurations));
    System.out.println((System.nanoTime()-startTime)/1000000);
    System.out.println(sfd.countsRepartition());
    //sfd.saveGraphvizToFolder("./graphviz_outputs/jhipster");
    //System.out.println(sfd.get(0).toUVL(""));
    //System.out.println(fm);
  }
}
