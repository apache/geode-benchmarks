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

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class EnableIfClassExistsCondition implements ExecutionCondition {

  public static final ConditionEvaluationResult ENABLED =
      enabled("No @EnableIfClassExists conditions resulting in 'disabled' execution encountered");

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
    Optional<AnnotatedElement> optionalElement = context.getElement();
    if (optionalElement.isPresent()) {
      AnnotatedElement annotatedElement = optionalElement.get();
      return findRepeatableAnnotations(annotatedElement, EnableIfClassExists.class).stream()
          .map(this::evaluate)
          .filter(ConditionEvaluationResult::isDisabled)
          .findFirst()
          .orElse(ENABLED);
    }
    return ENABLED;
  }

  private ConditionEvaluationResult evaluate(final EnableIfClassExists enableIfClassExists) {
    final String className = enableIfClassExists.value();
    try {
      Class.forName(className);
      return enabled(format("Class [%s] exists.", className));
    } catch (ClassNotFoundException e) {
      return disabled(format("Class [%s] does not exist.", className));
    }
  }
}
