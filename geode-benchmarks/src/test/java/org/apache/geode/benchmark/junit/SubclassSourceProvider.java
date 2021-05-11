/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.geode.benchmark.junit;

import java.util.List;
import java.util.stream.Stream;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junitpioneer.jupiter.CartesianAnnotationConsumer;

public class SubclassSourceProvider
    implements CartesianAnnotationConsumer<CartesianSubclassSource>, ArgumentsProvider {

  private List<Class<?>> subclasses;

  @Override
  public void accept(final CartesianSubclassSource subclassSource) {
    try (final ScanResult scanResult = new ClassGraph().enableAllInfo()
        .acceptPackages(subclassSource.acceptPackages())
        .scan()) {
      subclasses = scanResult.getSubclasses(subclassSource.value().getName()).loadClasses();
    }
  }

  @Override
  public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
    return subclasses.stream().map(Arguments::of);
  }
}
