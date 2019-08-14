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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LongRangeTest {

  @Test
  public void constructValidRanges() {
    new LongRange(0, 1);
    new LongRange(0, Long.MAX_VALUE);
    new LongRange(-1, 0);
    new LongRange(Long.MIN_VALUE, -1);
  }

  @Test
  public void constructInvalidRangesThrowsException() {
    assertThatThrownBy(() -> new LongRange(0, 0)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new LongRange(1, 0)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new LongRange(Long.MIN_VALUE, Long.MAX_VALUE))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new LongRange(-1, Long.MAX_VALUE))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new LongRange(Long.MIN_VALUE, 0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void getMin() {
    assertThat(new LongRange(1, 2).getMin()).isEqualTo(1);
  }

  @Test
  public void getMax() {
    assertThat(new LongRange(1, 2).getMax()).isEqualTo(2);
  }

  @Test
  public void size() {
    assertThat(new LongRange(1, 2).size()).isEqualTo(1);
    assertThat(new LongRange(0, Long.MAX_VALUE).size()).isEqualTo(Long.MAX_VALUE);
  }

  @Test
  void sliceWhole() {
    final LongRange[] slices = new LongRange(0, 50).slice(5);
    assertThat(slices).hasSize(5);
    assertThat(slices).containsExactly(new LongRange(0, 10), new LongRange(10, 20),
        new LongRange(20, 30), new LongRange(30, 40), new LongRange(40, 50));
  }

  @Test
  void sliceWithRemainder() {
    final LongRange[] slices = new LongRange(0, 53).slice(5);
    assertThat(slices).hasSize(5);
    assertThat(slices).containsExactly(new LongRange(0, 11), new LongRange(11, 22),
        new LongRange(22, 33), new LongRange(33, 43), new LongRange(43, 53));
  }

  @Test
  void sliceFor() {
    assertThat(new LongRange(0, 50).sliceFor(5, 2)).isEqualTo(new LongRange(20, 30));
  }

  @Test
  void sliceForWithRemainder() {
    assertThat(new LongRange(0, 53).sliceFor(5, 2)).isEqualTo(new LongRange(22, 33));
    assertThat(new LongRange(10000, Long.MAX_VALUE).sliceFor(2, 0))
        .isEqualTo(new LongRange(10000, 4611686018427392904L));
  }

  @Test
  void sliceForThrowsIndexOutOfBoundsException() {
    final LongRange range = new LongRange(0, 50);
    assertThatThrownBy(() -> range.sliceFor(5, -1)).isInstanceOf(IndexOutOfBoundsException.class);
    assertThatThrownBy(() -> range.sliceFor(5, 10)).isInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  void slicesOfSize() {
    final LongRange[] slices = new LongRange(0, 50).slicesOfSize(10);
    assertThat(slices).hasSize(5);
    assertThat(slices).containsExactly(new LongRange(0, 10), new LongRange(10, 20),
        new LongRange(20, 30), new LongRange(30, 40), new LongRange(40, 50));
  }

  @Test
  void slicesOfSizeWithRemainer() {
    final LongRange[] slices = new LongRange(0, 53).slicesOfSize(10);
    assertThat(slices).hasSize(6);
    assertThat(slices).containsExactly(new LongRange(0, 9), new LongRange(9, 18),
        new LongRange(18, 27), new LongRange(27, 36), new LongRange(36, 45), new LongRange(45, 53));
  }

}
