package org.quickperf.testng.jvm.jmc;

import org.quickperf.jvm.annotations.ProfileJvm;
import org.testng.annotations.Test;

public class AllocationRateProfileJVMTest {

  @ProfileJvm
  @Test
  public void test() {
    for (int i = 0; i < 1_000; i++) {
      int[] arr = new int[256];
    }
  }
}
