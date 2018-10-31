package test;

import java.io.Serializable;

import infrastructure.Infrastructure;

public interface CloudTest extends Serializable {

  TestConfig configure(Infrastructure infrastructure);

}
