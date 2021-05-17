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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * {@code }@CartesianSubclassSource} is a argument source subclass of the specified {@linkplain
 * #value Class} for tests annotated with {@code @CartesianProductTest}.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(CartesianSubclassSource.CartesianSubclassSources.class)
@ArgumentsSource(SubclassSourceProvider.class)
public @interface CartesianSubclassSource {

  /**
   * The Class to find subclasses for.
   */
  Class<?> value();

  /**
   * Class Packages to accept. All by default.
   */
  String[] acceptPackages() default {};

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  @interface CartesianSubclassSources {
    CartesianSubclassSource[] value();
  }
}
