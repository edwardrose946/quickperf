package org.quickperf.jvm.jmc.value;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

  private IItemCollection jfrEvents;
  private Iterator jfrEventsIterator;
  private IItemIterable allocationEvents;
  private Iterator allocationEventsIterator;
  private IQuantity totalAlloc;
  private IItem mockedEvent;
  private IItem mockedEvent2;
  private IItem mockedEvent3;
  private StructContentType mockedIType;
  private IMemberAccessor mockedIMemberAccessor;
  private IQuantity mockedIQuantity;


  @BeforeEach
  public void setUpIItemCollection() {
    jfrEvents = mock(IItemCollection.class);
    jfrEventsIterator = mock(Iterator.class);
    totalAlloc = mock(IQuantity.class);
    allocationEvents = mock(IItemIterable.class);
    allocationEventsIterator = mock(Iterator.class);
    mockedEvent = mock(IItem.class);
    mockedEvent2 = mock(IItem.class);
    mockedEvent3 = mock(IItem.class);
    mockedIType = mock(StructContentType.class);
    mockedIMemberAccessor = mock(IMemberAccessor.class);
    mockedIQuantity = mock(IQuantity.class);

    when(totalAlloc.longValue()).thenReturn(1024L);

    when(jfrEvents.hasItems()).thenReturn(true);

    when(jfrEvents.getAggregate(any(IAggregator.class))).thenReturn(totalAlloc);

    when(jfrEvents.apply(JdkFilters.ALLOC_INSIDE_TLAB)).thenReturn(jfrEvents);
    when(jfrEvents.apply(JdkFilters.ALLOC_OUTSIDE_TLAB)).thenReturn(jfrEvents);

    when(jfrEvents.iterator())
        .thenReturn(jfrEventsIterator, jfrEventsIterator, jfrEventsIterator, jfrEventsIterator);
    when(jfrEventsIterator.next())
        .thenReturn(allocationEvents, allocationEvents, allocationEvents, allocationEvents);
    when(jfrEventsIterator.hasNext())
        .thenReturn(true, false, true, false, true, false, true, false);

    when(allocationEvents.iterator())
        .thenReturn(allocationEventsIterator, allocationEventsIterator, allocationEventsIterator,
            allocationEventsIterator);
    when(allocationEventsIterator.next())
        .thenReturn(mockedEvent, mockedEvent2, mockedEvent3, mockedEvent, mockedEvent2,
            mockedEvent3, mockedEvent, mockedEvent2, mockedEvent3);
    when(allocationEventsIterator.hasNext())
        .thenReturn(true, true, true, false, true, true, true, false, true, true, true, false, true,
            true, true, false);

    when(mockedEvent.getType()).thenReturn(mockedIType);
    when(mockedEvent2.getType()).thenReturn(mockedIType);
    when(mockedEvent3.getType()).thenReturn(mockedIType);

    when(mockedIType.getAccessor(any(IAccessorKey.class))).thenReturn(mockedIMemberAccessor);

    when(mockedIMemberAccessor.getMember(any(IItem.class))).thenReturn(mockedIQuantity);
  }

  @Test
  void formatAsString() {
    try {
      when(mockedIQuantity.longValueIn(UnitLookup.EPOCH_MS))
          .thenReturn(1000L, 2000L, 3000L, 4000L, 5000L, 6000L, 7000L, 8000L, 9000L, 10000L, 11000L,
              11000L);
    } catch (QuantityConversionException e) {
      e.printStackTrace();
    }

    System.out.println(AllocationRate.formatAsString(jfrEvents));

  }


}