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

import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.IItemIterable;
import org.openjdk.jmc.common.item.IMemberAccessor;
import org.openjdk.jmc.common.item.IType;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.QuantityConversionException;
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.flightrecorder.JfrAttributes;
import org.openjdk.jmc.flightrecorder.jdk.JdkAggregators;
import org.openjdk.jmc.flightrecorder.jdk.JdkFilters;
import org.quickperf.jvm.allocation.AllocationUnit;

/**
 * Class containing methods that calculate the allocation rate and format the output.
 *
 * @author Edward Rose
 * @version 14/05/2020
 */
public class AllocationRate {

  /**
   * Calculate the allocation rate (per ms) from the collection of Java Flight Recorder Events,
   * format and return as a String.
   *
   * @param jfrEvents IItemCollection jfrEvents
   * @return allocation rate (per second) as a String
   */
  public static String formatAsString(IItemCollection jfrEvents) {
    if (jfrEvents == null || !jfrEvents.hasItems()) {
      return " ";
    }
    double allocationRateBytesPerSecond;
    try {
      allocationRateBytesPerSecond = getAllocationRateBytesPerSecond(jfrEvents);
    } catch (ArithmeticException exception) {
      if (exception.getMessage().equals("Division by zero")
          || exception.getMessage().equals("No allocation events")) {
        return " ";
      } else {
        return "Calculation Error " + System.lineSeparator();
      }
    }
    return AllocationRatePerSecondFormatter.INSTANCE
        .format(allocationRateBytesPerSecond, AllocationUnit.BYTE);
  }

  /**
   * Calculate the allocation rate.
   *
   * @param jfrEvents IItemCollection jfrEvents
   * @return rate (bytes per second)
   */
  private static double getAllocationRateBytesPerSecond(IItemCollection jfrEvents)
      throws ArithmeticException {
    long totalAllocationInBytes = totalAllocationInBytes(jfrEvents);
    long allocationDurationInMs = allocationDurationInMs(jfrEvents);
    double allocationDurationInSeconds = allocationDurationInMs / 1000.0;
    if (allocationDurationInSeconds > 0) {
      return totalAllocationInBytes / allocationDurationInSeconds;
    } else if (allocationDurationInSeconds == 0) {
      throw new ArithmeticException("Division by zero");
    } else {
      throw new ArithmeticException("Allocation duration cannot be negative");
    }
  }

  /**
   * Calculate the total allocation in bytes.
   *
   * @param jfrEvents IItemCollection jfrEvents
   * @return total allocation in bytes.
   * @see <a href="https://github.com/quick-perf/quickperf/issues/64#show_issue">Implementation
   * Ideas</a>
   */
  private static long totalAllocationInBytes(IItemCollection jfrEvents) {
    IQuantity totalAlloc = jfrEvents.getAggregate(JdkAggregators.ALLOCATION_TOTAL);
    return totalAlloc.longValue();
  }

  /**
   * Calculate the duration of allocation in ms.
   *
   * @param jfrEvents IItemCollection jfrEvents
   * @return allocation duration in ms
   * @see <a href="https://github.com/quick-perf/quickperf/issues/64#show_issue">Implementation
   * Ideas</a>
   */
  private static long allocationDurationInMs(IItemCollection jfrEvents) throws ArithmeticException {
    //filter events
    IItemCollection insideTlab = jfrEvents.apply(JdkFilters.ALLOC_INSIDE_TLAB);
    IItemCollection outsideTlab = jfrEvents.apply(JdkFilters.ALLOC_OUTSIDE_TLAB);
    if (!outsideTlab.hasItems() && !insideTlab.hasItems()) {
      throw new ArithmeticException("No allocation events");
    }
    // min timestamp of either events
    long insideTlabMinTimeStamp = minTimeStampInMs(insideTlab);
    long outsideTlabMinTimeStamp = minTimeStampInMs(outsideTlab);
    long minTimeStampInMs = Math.min(insideTlabMinTimeStamp, outsideTlabMinTimeStamp);
    // max timestamp of either events
    long insideTlabMaxTimeStamp = maxTimeStampInMs(insideTlab);
    long outsideTlabMaxTimeStamp = maxTimeStampInMs(outsideTlab);
    long maxTimeStampInMs = Math.max(insideTlabMaxTimeStamp, outsideTlabMaxTimeStamp);
    // calculate duration
    if (minTimeStampInMs > maxTimeStampInMs) {
      throw new ArithmeticException("Allocation duration cannot be negative");
    }
    return maxTimeStampInMs - minTimeStampInMs;
  }

  /**
   * Iterate through the allocation events and find the minimum time stamp.
   *
   * @param allocationEvents IICollection allocationEvents
   * @return minimum time stamp of event
   */
  private static long minTimeStampInMs(IItemCollection allocationEvents)
      throws ArithmeticException {
    long minTimeStamp = Long.MAX_VALUE;
    for (IItemIterable jfrEventCollection : allocationEvents) {
      for (IItem item : jfrEventCollection) {
        long currentTimeStamp = getTimeStampInMs(item);
        minTimeStamp = Math.min(minTimeStamp, currentTimeStamp);
      }
    }
    return minTimeStamp;
  }

  /**
   * Iterate through the allocation events and find the maximum time stamp.
   *
   * @param allocationEvents IICollection allocationEvents
   * @return maximum time stamp of event
   */
  private static long maxTimeStampInMs(IItemCollection allocationEvents)
      throws ArithmeticException {
    long maxTimeStamp = 0;
    for (IItemIterable jfrEventCollection : allocationEvents) {
      for (IItem item : jfrEventCollection) {
        long currentTimeStamp = getTimeStampInMs(item);
        maxTimeStamp = Math.max(maxTimeStamp, currentTimeStamp);
      }
    }
    return maxTimeStamp;
  }

  /**
   * Get the time stamp of an allocation event in ms.
   *
   * @param allocationEvent allocation event
   * @return time stamp in ms
   * @see <a href="https://github.com/quick-perf/quickperf/issues/64#show_issue">Implementation
   * Ideas</a>
   */
  private static long getTimeStampInMs(IItem allocationEvent) throws ArithmeticException {
    IType<IItem> type = (IType<IItem>) allocationEvent.getType();
    IMemberAccessor<IQuantity, IItem> endTimeAccessor = JfrAttributes.END_TIME.getAccessor(type);
    IQuantity quantityEndTime = endTimeAccessor.getMember(allocationEvent);
    long timeStampInMs;
    try {
      timeStampInMs = quantityEndTime.longValueIn(UnitLookup.EPOCH_MS);
    } catch (QuantityConversionException e) {
      System.out.println("Unable to convert the timestamp of an allocation event into ms.");
      e.printStackTrace();
      throw new ArithmeticException();
    }
    if (timeStampInMs < 0) {
      throw new ArithmeticException("Time stamp cannot be negative");
    }
    return timeStampInMs;
  }
}
