package org.quickperf.jvm.jmc.value;

import org.quickperf.jvm.allocation.Allocation;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.allocation.ByteAllocationMeasureFormatter;

public class AllocationRatePerSecondFormatter {

  public static final AllocationRatePerSecondFormatter INSTANCE = new AllocationRatePerSecondFormatter();

  private AllocationRatePerSecondFormatter() {
  }

  public String format(double allocationRate, AllocationUnit allocationUnit) {
    // convert to appropriate byte measure unit
    String format = ByteAllocationMeasureFormatter.INSTANCE
        .format(new Allocation(allocationRate, allocationUnit));
    String target = format.split("\\s\\(")[0];
    return abbreviate(target);
  }

  private String abbreviate(String target) {
    if (target.contains("Kilo bytes")) {
      return target.replace("Kilo bytes", "KiB/s");
    } else if (target.contains("Mega bytes")) {
      return target.replace("Mega bytes", "MiB/s");
    } else if (target.contains("Giga bytes")) {
      return target.replace("Giga bytes", "GiB/s");
    }
    return target.replace("bytes", "bytes/s");
  }
}
