package suite;

import java.util.Collection;

import test.CloudTest;

//TODO - this should just be an annotation on classes
//We should find all classes in the classpath and run them
//TODO - add a junit runner to run this suite with tiny tuning?
public interface Suite {

  Iterable<CloudTest> suite();
}
