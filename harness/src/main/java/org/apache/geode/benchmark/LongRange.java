/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.benchmark;

import static java.lang.Long.max;
import static java.lang.Long.min;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongConsumer;

/**
 * Describes a range [min, max) in long values between min inclusive and max exclusive.
 */
public class LongRange implements Serializable {
  private static final long serialVersionUID = 1L;

  private final long min;
  private final long max;

  public LongRange(long min, long max) {
    this.min = min;
    this.max = max;
  }

  public long getMin() {
    return min;
  }

  public long getMax() {
    return max;
  }

  /**
   * Slices the range in relatively equal ranges in each slice with the remainder distributed across
   * the slices.
   *
   * @param count of slices
   * @return array of {@code count} slices
   */
  public LongRange[] slice(final int count) {
    final LongRange[] slices = new LongRange[count];

    for (int i = 0; i < slices.length; i++) {
      slices[i] = sliceFor(count, i);
    }

    return slices;
  }

  /**
   * Get a single slice for {@code count} slices at {@code index}
   *
   * @param count of slices
   * @param index of slice
   * @return Single slice at index.
   * @throws IndexOutOfBoundsException if index is less than 0 or greater than slices.
   */
  public LongRange sliceFor(final int count, final int index) throws IndexOutOfBoundsException {
    if (index < 0 || index >= count) {
      throw new IndexOutOfBoundsException();
    }

    final long size = max - min;
    final long increment = size / count;
    final long remainder = size % count;
    final long sliceMin =
        min + ((increment + 1) * min(remainder, index)) + (increment * max(0, index - remainder));
    final long sliceMax = sliceMin + increment + (index < remainder ? 1 : 0);
    return new LongRange(sliceMin, sliceMax);
  }

  /**
   * Iterates over each value in the range.
   *
   * @param consumer to invoke with current value.
   */
  public void forEach(LongConsumer consumer) {
    for (long i = min; i < max; i++) {
      consumer.accept(i);
    }
  }

  /**
   * Randomly select a value withing range.
   *
   * @return a random value within range.
   */
  public long random() {
    return ThreadLocalRandom.current().nextLong(min, max);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final LongRange longRange = (LongRange) o;
    return min == longRange.min &&
        max == longRange.max;
  }

  @Override
  public int hashCode() {
    return Objects.hash(min, max);
  }

  @Override
  public String toString() {
    return "LongRange{" + "min=" + min + ", max=" + max + '}';
  }
}
