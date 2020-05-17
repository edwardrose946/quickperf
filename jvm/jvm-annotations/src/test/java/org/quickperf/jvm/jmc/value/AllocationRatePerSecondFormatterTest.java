package org.quickperf.jvm.jmc.value;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.quickperf.jvm.allocation.AllocationUnit;

class AllocationRatePerSecondFormatterTest {

  @Test
  void formatBytes() {
    String expected = "1.0 bytes/s";
    String actual = AllocationRatePerSecondFormatter.INSTANCE.format(1, AllocationUnit.BYTE);
    assertEquals(expected, actual);
  }

  @Test
  void formatKiB() {
    String expected = "1.0 KiB/s";
    String actual = AllocationRatePerSecondFormatter.INSTANCE.format(1024, AllocationUnit.BYTE);
    assertEquals(expected, actual);
  }

  @Test
  void formatMiB() {
    String expected = "1.0 MiB/s";
    String actual = AllocationRatePerSecondFormatter.INSTANCE
        .format(1024 * 1024, AllocationUnit.BYTE);
    assertEquals(expected, actual);
  }

  @Test
  void formatGiB() {
    String expected = "1.0 GiB/s";
    String actual = AllocationRatePerSecondFormatter.INSTANCE
        .format(Math.pow(1024.0, 3), AllocationUnit.BYTE);
    assertEquals(expected, actual);
  }
}