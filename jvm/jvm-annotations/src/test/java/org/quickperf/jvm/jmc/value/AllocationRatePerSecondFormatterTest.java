/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.jvm.jmc.value;

import org.junit.Test;
import org.quickperf.jvm.allocation.AllocationUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class AllocationRatePerSecondFormatterTest {

  private AllocationRatePerSecondFormatter allocationRatePerSecondFormatter = AllocationRatePerSecondFormatter.INSTANCE;

  @Test
  public void should_format_bytes_per_second() {
    String result = allocationRatePerSecondFormatter
                    .format(1, AllocationUnit.BYTE);
    assertThat(result).isEqualTo("1.0 bytes/s");
  }

  @Test
  public void should_kilo_bytes_per_second() {
    String result = allocationRatePerSecondFormatter
                    .format(1024, AllocationUnit.BYTE);
    assertThat(result).isEqualTo("1.0 KiB/s");
  }

  @Test
  public void should_mega_bytes_per_second() {
    String result = allocationRatePerSecondFormatter
                   .format(1024 * 1024, AllocationUnit.BYTE);
    assertThat(result).isEqualTo("1.0 MiB/s");
  }

  @Test
  public void should_giga_bytes_per_second() {
    String result = allocationRatePerSecondFormatter
                   .format(Math.pow(1024.0, 3), AllocationUnit.BYTE);
    assertThat(result).isEqualTo("1.0 GiB/s");
  }

}