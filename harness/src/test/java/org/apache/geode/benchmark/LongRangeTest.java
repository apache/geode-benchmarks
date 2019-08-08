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
  public void getMin() {
    assertThat(new LongRange(1, 2).getMin()).isEqualTo(1);
  }

  @Test
  public void getMax() {
    assertThat(new LongRange(1, 2).getMax()).isEqualTo(2);
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

}
