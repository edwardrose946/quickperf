package org.quickperf.jvm.jmc.value;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.quickperf.jvm.jmc.value.AllocationRate.*;

import java.util.Iterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openjdk.jmc.common.item.IAccessorKey;
import org.openjdk.jmc.common.item.IAggregator;
import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.IItemIterable;
import org.openjdk.jmc.common.item.IMemberAccessor;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.QuantityConversionException;
import org.openjdk.jmc.common.unit.StructContentType;
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.flightrecorder.jdk.JdkFilters;

class AllocationRateTest {

  private IItemCollection mockedJfrEvents;
  private Iterator mockedJfrEventsIterator;
  private IItemIterable mockedAllocationEvents;
  private Iterator mockedAllocationEventsIterator;
  private IQuantity mockedTotalAlloc;
  private IItem mockedEvent;
  private IItem mockedEvent2;
  private IItem mockedEvent3;
  private StructContentType mockedIType;
  private IMemberAccessor mockedIMemberAccessor;
  private IQuantity mockedIQuantity;


  /**
   * Set up an IItemCollection of jfr events.
   *
   * Set up the jfrEvents with a total allocation value of 1 KiB.
   *
   * The first three values returned from mockedIQuantity.longValueIn() are used in minTimeStamp()
   * of allocation events inside Tlab.The next three elements are used in minTimeStamp() of
   * allocation events outside Tlab.
   * <p>
   * The next three elements are used in maxTimeStamp() of allocation events inside Tlab. The next
   * three elements are used in maxTimeStamp() of allocation events outside Tlab.
   * <p>
   * Example for test 1:
   * <p>
   * minInside = min(1000,2000,3000)
   * minOutside = min(10_000, 10_000, 11_000)
   * min of both = 1000
   * <p>
   * maxInside = max(1000,2000,3000)
   * maxOutside = max(10_000, 10_000, 11_000)
   * max of both = 11_000
   * <p>
   * duration = 11_000 - 1000 = 10_000 ms
   * <p>
   * Allocation rate = 1024 bytes / 10 seconds
   * 102.4 KiB/s
   * <p>
   */

  @BeforeEach
  public void setUpIItemCollection() {
    mockedJfrEvents = mock(IItemCollection.class);
    mockedJfrEventsIterator = mock(Iterator.class);
    mockedTotalAlloc = mock(IQuantity.class);
    mockedAllocationEvents = mock(IItemIterable.class);
    mockedAllocationEventsIterator = mock(Iterator.class);
    mockedEvent = mock(IItem.class);
    mockedEvent2 = mock(IItem.class);
    mockedEvent3 = mock(IItem.class);
    mockedIType = mock(StructContentType.class);
    mockedIMemberAccessor = mock(IMemberAccessor.class);
    mockedIQuantity = mock(IQuantity.class);

    when(mockedJfrEvents.hasItems()).thenReturn(true);

    when(mockedTotalAlloc.longValue()).thenReturn(1024L);

    when(mockedJfrEvents.getAggregate(any(IAggregator.class))).thenReturn(mockedTotalAlloc);

    when(mockedJfrEvents.apply(JdkFilters.ALLOC_INSIDE_TLAB)).thenReturn(mockedJfrEvents);
    when(mockedJfrEvents.apply(JdkFilters.ALLOC_OUTSIDE_TLAB)).thenReturn(mockedJfrEvents);

    when(mockedJfrEvents.iterator())
        .thenReturn(mockedJfrEventsIterator, mockedJfrEventsIterator, mockedJfrEventsIterator,
            mockedJfrEventsIterator);
    when(mockedJfrEventsIterator.next())
        .thenReturn(mockedAllocationEvents, mockedAllocationEvents, mockedAllocationEvents,
            mockedAllocationEvents);
    when(mockedJfrEventsIterator.hasNext())
        .thenReturn(true, false, true, false, true, false, true, false);

    when(mockedAllocationEvents.iterator())
        .thenReturn(mockedAllocationEventsIterator, mockedAllocationEventsIterator,
            mockedAllocationEventsIterator,
            mockedAllocationEventsIterator);
    when(mockedAllocationEventsIterator.next())
        .thenReturn(mockedEvent, mockedEvent2, mockedEvent3, mockedEvent, mockedEvent2,
            mockedEvent3, mockedEvent, mockedEvent2, mockedEvent3);
    when(mockedAllocationEventsIterator.hasNext())
        .thenReturn(true, true, true, false, true, true, true, false, true, true, true, false, true,
            true, true, false);

    when(mockedEvent.getType()).thenReturn(mockedIType);
    when(mockedEvent2.getType()).thenReturn(mockedIType);
    when(mockedEvent3.getType()).thenReturn(mockedIType);

    when(mockedIType.getAccessor(any(IAccessorKey.class))).thenReturn(mockedIMemberAccessor);

    when(mockedIMemberAccessor.getMember(any(IItem.class))).thenReturn(mockedIQuantity);

    try {
      when(mockedIQuantity.longValueIn(UnitLookup.EPOCH_MS))
          .thenReturn(1000L, 2000L, 3000L, 10_000L, 10_000L, 11_000L, 1000L, 2000L, 3000L, 10000L,
              11000L, 11000L);
    } catch (QuantityConversionException e) {
      e.printStackTrace();
    }
  }

  /**
   * 1 KiB over 10 seconds.
   */
  @Test
  void format100BytesPerSecondAsString() {
    when(mockedTotalAlloc.longValue()).thenReturn(1024L);

    String expected = "102.4 bytes/s";
    String actual = formatAsString(mockedJfrEvents);
    assertEquals(expected, actual);
  }

  /**
   * 1 MiB over 10 seconds.
   */
  @Test
  void format100KiBPerSecondAsString() {
    when(mockedTotalAlloc.longValue()).thenReturn(1024L * 1024L);

    String expected = "102.4 KiB/s";
    String actual = formatAsString(mockedJfrEvents);
    assertEquals(expected, actual);
  }

  /**
   * 1 GiB over 10 seconds.
   */
  @Test
  void format100MiBPerSecondAsString() {
    when(mockedTotalAlloc.longValue()).thenReturn((long) Math.pow(1024L, 3));

    String expected = "102.4 MiB/s";
    String actual = formatAsString(mockedJfrEvents);
    assertEquals(expected, actual);
  }

  /**
   * 1 TiB over 10 seconds.
   */
  @Test
  void format100GiBPerSecondAsString() {
    when(mockedTotalAlloc.longValue()).thenReturn((long) Math.pow(1024, 4));

    String expected = "102.4 GiB/s";
    String actual = formatAsString(mockedJfrEvents);
    assertEquals(expected, actual);
  }

  /**
   * Difference between allocation time stamps is zero, therefore the rate should return " ".
   */
  @Test
  void allZeroTimeStamps() {
    when(mockedTotalAlloc.longValue()).thenReturn(1000L);
    try {
      when(mockedIQuantity.longValueIn(UnitLookup.EPOCH_MS))
          .thenReturn(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
              0L);
    } catch (QuantityConversionException e) {
      e.printStackTrace();
    }

    String expected = " ";
    String actual = formatAsString(mockedJfrEvents);
    assertEquals(expected, actual);
  }

  /**
   * Negative time stamps could not necessarily be detected if their difference is zero.
   */
  @Test
  void negativeTimeStamps() {
    when(mockedTotalAlloc.longValue()).thenReturn(1000L);
    try {
      when(mockedIQuantity.longValueIn(UnitLookup.EPOCH_MS))
          .thenReturn(-10L, -10L, -10L, -10L, -10L, -10L, -10L, -10L, -10L, -10L, -10L,
              -10L);
    } catch (QuantityConversionException e) {
      e.printStackTrace();
    }

    String expected = "Calculation Error";
    String actual = formatAsString(mockedJfrEvents);
    assertEquals(expected, actual);
  }

  @Test
  void negativeMinimumTimeStamps() {
    when(mockedTotalAlloc.longValue()).thenReturn(1000L);
    try {
      when(mockedIQuantity.longValueIn(UnitLookup.EPOCH_MS))
          .thenReturn(-10L, -10L, -10L, -10L, -10L, -10L, 10L, 10L, 10L, 10L, 10L,
              10L);
    } catch (QuantityConversionException e) {
      e.printStackTrace();
    }

    String expected = "Calculation Error";
    String actual = formatAsString(mockedJfrEvents);
    assertEquals(expected, actual);
  }


  @Test
  void negativeMaximumTimeStamps() {
    when(mockedTotalAlloc.longValue()).thenReturn(1000L);
    try {
      when(mockedIQuantity.longValueIn(UnitLookup.EPOCH_MS))
          .thenReturn(10L, 10L, 10L, 10L, 10L, 10L, -10L, -10L, -10L, -10L, -10L,
              -10L);
    } catch (QuantityConversionException e) {
      e.printStackTrace();
    }

    String expected = "Calculation Error";
    String actual = formatAsString(mockedJfrEvents);
    assertEquals(expected, actual);
  }

  /**
   * Maximum time stamps must be greater than the minimum time stamps otherwise duration will be
   * negative and give an incorrect result.
   */
  @Test
  void minimumTimeStampsGreaterThanMaximumTimeStamps() {
    when(mockedTotalAlloc.longValue()).thenReturn(1000L);
    try {
      when(mockedIQuantity.longValueIn(UnitLookup.EPOCH_MS))
          .thenReturn(10L, 10L, 10L, 10L, 10L, 10L, 5L, 5L, 5L, 5L, 5L,
              5L);
    } catch (QuantityConversionException e) {
      e.printStackTrace();
    }

    String expected = "Calculation Error";
    String actual = formatAsString(mockedJfrEvents);
    assertEquals(expected, actual);
  }

  @Test
  void formatStringOfEmptyCollection() {
    IItemCollection empty = mock(IItemCollection.class);
    when(empty.hasItems()).thenReturn(false);

    String expected = " ";
    String actual = formatAsString(empty);
    assertEquals(expected, actual);
  }

  @Test
  void formatStringNullCollection() {
    IItemCollection nullCollection = null;

    String expected = " ";
    String actual = formatAsString(nullCollection);
    assertEquals(expected, actual);
  }

  @Test
  void emptyInsideAndOutsideTlabCollections() {
    when(mockedJfrEvents.hasItems()).thenReturn(true, false,false);

    String expected = " ";
    String actual = formatAsString(mockedJfrEvents);
    assertEquals(expected, actual);
  }
}