package org.quickperf.jvm.jmc.value;

import org.junit.jupiter.api.Test;
import org.quickperf.jvm.allocation.AllocationUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AllocationRatePerSecondFormatterTest {

  private AllocationRatePerSecondFormatter allocationRatePerSecondFormatter = AllocationRatePerSecondFormatter.INSTANCE;

  @Test
  void should_format_bytes_per_second() {
    String result = allocationRatePerSecondFormatter
                    .format(1, AllocationUnit.BYTE);
    assertThat(result).isEqualTo("1.0 bytes/s");
  }

  @Test
  void should_kilo_bytes_per_second() {
    String result = allocationRatePerSecondFormatter
                    .format(1024, AllocationUnit.BYTE);
    assertThat(result).isEqualTo("1.0 KiB/s");
  }

  @Test
  void should_mega_bytes_per_second() {
    String result = allocationRatePerSecondFormatter
                   .format(1024 * 1024, AllocationUnit.BYTE);
    assertThat(result).isEqualTo("1.0 MiB/s");
  }

  @Test
  void should_giga_bytes_per_second() {
    String result = allocationRatePerSecondFormatter
                   .format(Math.pow(1024.0, 3), AllocationUnit.BYTE);
    assertThat(result).isEqualTo("1.0 GiB/s");
  }

}