package com.deathrayresearch.outlier.columns;

import com.deathrayresearch.outlier.Table;
import com.deathrayresearch.outlier.io.TypeUtils;
import com.deathrayresearch.outlier.mapper.DateTimeMapUtils;
import com.deathrayresearch.outlier.store.ColumnMetadata;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.roaringbitmap.RoaringBitmap;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;

/**
 * A column in a base table that contains float values
 */
public class LocalDateTimeColumn extends AbstractColumn implements DateTimeMapUtils {

  public static final Long MISSING_VALUE = Long.MIN_VALUE;

  private static int DEFAULT_ARRAY_SIZE = 128;

  private LongArrayList data;

  @Override
  public void addCell(String stringvalue) {

    if (stringvalue == null) {
      add(Long.MIN_VALUE);
    } else {
      LocalDateTime dateTime = convert(stringvalue);
      if (dateTime != null) {
        add(dateTime);
      } else {
        add(Long.MIN_VALUE);
      }
    }
  }

  public void add(LocalDateTime dateTime) {
    long dt = PackedLocalDateTime.pack(dateTime);
    add(dt);
  }

  public LocalDateTime convert(String value) {
    if (Strings.isNullOrEmpty(value)
        || TypeUtils.MISSING_INDICATORS.contains(value)
        || value.equals("-1")) {
      return null;
    }
    value = Strings.padStart(value, 4, '0');
    return LocalDateTime.parse(value, TypeUtils.dateTimeFormatter);
  }

  public static LocalDateTimeColumn create(String name) {
    return new LocalDateTimeColumn(name);
  }

  private LocalDateTimeColumn(String name) {
    super(name);
    data = new LongArrayList(DEFAULT_ARRAY_SIZE);
  }

  public LocalDateTimeColumn(ColumnMetadata metadata) {
    super(metadata);
    data = new LongArrayList(DEFAULT_ARRAY_SIZE);
  }

  public LocalDateTimeColumn(String name, int initialSize) {
    super(name);
    data = new LongArrayList(initialSize);
  }

  public int size() {
    return data.size();
  }

  public LongArrayList data() {
    return data;
  }

  @Override
  public ColumnType type() {
    return ColumnType.LOCAL_DATE_TIME;
  }

  public void add(long dateTime) {
    data.add(dateTime);
  }

  @Override
  public String getString(int row) {
    return PackedLocalDateTime.toString(getLong(row));
  }

  @Override
  public LocalDateTimeColumn emptyCopy() {
    return new LocalDateTimeColumn(name());
  }

  @Override
  public void clear() {
    data.clear();
  }

  private LocalDateTimeColumn copy() {
    LocalDateTimeColumn copy = emptyCopy();
    copy.data.addAll(data);
    return copy;
  }

  @Override
  public LocalDateTimeColumn sortAscending() {
    LocalDateTimeColumn copy = this.copy();
    Collections.sort(copy.data);
    return copy;

  }

  @Override
  public Column sortDescending() {
    LocalDateTimeColumn copy = sortAscending();
    Collections.reverse(copy.data);
    return copy;
  }

  // TODO(lwhite): Implement column summary()
  @Override
  public Table summary() {
    return null;
  }

  // TODO(lwhite): Implement countUnique()
  @Override
  public int countUnique() {
    return 0;
  }

  @Override
  public LocalDateTimeColumn unique() {
    LongSet ints = new LongOpenHashSet(data.size());
    for (long i : data) {
      ints.add(i);
    }
    return LocalDateTimeColumn.create(name() + " Unique values",
            LongArrayList.wrap(ints.toLongArray()));
  }

  @Override
  public boolean isEmpty() {
    return data.isEmpty();
  }

  public long getLong(int index) {
    return data.getLong(index);
  }

  public LocalDateTime get(int index) {
    return PackedLocalDateTime.asLocalDateTime(getLong(index));
  }

  @Override
  public IntComparator rowComparator() {
    return comparator;
  }

  IntComparator comparator = new IntComparator() {

    @Override
    public int compare(Integer r1, Integer r2) {
      return compare((int) r1, (int) r2);
    }

    @Override
    public int compare(int r1, int r2) {
      long f1 = getLong(r1);
      long f2 = getLong(r2);
      return Long.compare(f1, f2);
    }
  };

  public CategoryColumn dayOfWeek() {
    CategoryColumn newColumn = CategoryColumn.create(this.name() + " day of week" , this.size());
    for (int r = 0; r < this.size(); r++) {
      long c1 = this.getLong(r);
      if (c1 == (LocalDateTimeColumn.MISSING_VALUE)) {
        newColumn.set(r, null);
      } else {
        LocalDateTime value1 = PackedLocalDateTime.asLocalDateTime(c1);
        newColumn.add(value1.getDayOfWeek().toString());
      }
    }
    return newColumn;
  }

  public IntColumn dayOfYear() {
    IntColumn newColumn = IntColumn.create(this.name() + " day of year", this.size());
    for (int r = 0; r < this.size(); r++) {
      long c1 = this.getLong(r);
      if (c1 == (LocalDateTimeColumn.MISSING_VALUE)) {
        newColumn.add(IntColumn.MISSING_VALUE);
      } else {
        newColumn.add(PackedLocalDateTime.getDayOfYear(c1));
      }
    }
    return newColumn;
  }

  public IntColumn dayOfMonth() {
    IntColumn newColumn = IntColumn.create(this.name() + " day of month");
    for (int r = 0; r < this.size(); r++) {
      long c1 = this.getLong(r);
      if (c1 == FloatColumn.MISSING_VALUE) {
        newColumn.add(IntColumn.MISSING_VALUE);
      } else {
        newColumn.add(PackedLocalDateTime.getDayOfMonth(c1));
      }
    }
    return newColumn;
  }

  public IntColumn monthNumber() {
    IntColumn newColumn = IntColumn.create(this.name() + " month");
    for (int r = 0; r < this.size(); r++) {
      long c1 = this.getLong(r);
      if (c1 == MISSING_VALUE) {
        newColumn.add(IntColumn.MISSING_VALUE);
      } else {
        newColumn.add(PackedLocalDateTime.getMonthValue(c1));
      }
    }
    return newColumn;
  }

  public CategoryColumn monthName() {
    CategoryColumn newColumn = CategoryColumn.create(this.name() + " month");
    for (int r = 0; r < this.size(); r++) {
      long c1 = this.getLong(r);
      if (c1 == MISSING_VALUE) {
        newColumn.add(CategoryColumn.MISSING_VALUE);
      } else {
        newColumn.add(Month.of(PackedLocalDateTime.getMonthValue(c1)).name());
      }
    }
    return newColumn;
  }

  public IntColumn year() {
    IntColumn newColumn = IntColumn.create(this.name() + " year");
    for (int r = 0; r < this.size(); r++) {
      long c1 = this.getLong(r);
      if (c1 == MISSING_VALUE) {
        newColumn.add(IntColumn.MISSING_VALUE);
      } else {
        newColumn.add(PackedLocalDateTime.getYear(PackedLocalDateTime.date(c1)));
      }
    }
    return newColumn;
  }

  public RoaringBitmap isEqualTo(LocalDateTime value) {
    RoaringBitmap results = new RoaringBitmap();
    long packedLocalDate = PackedLocalDateTime.pack(value);
    int i = 0;
    for (long next : data) {
      if (packedLocalDate == next) {
        results.add(i);
      }
      i++;
    }
    return results;
  }

  public static LocalDateTimeColumn create(String fileName, LongArrayList dateTimes) {
    LocalDateTimeColumn column = new LocalDateTimeColumn(fileName, dateTimes.size());
    column.data = dateTimes;
    return column;
  }

  public String print() {
    StringBuilder builder = new StringBuilder();
    for (long next : data) {
      builder.append(String.valueOf(PackedLocalDateTime.asLocalDateTime(next)));
      builder.append('\n');
    }
    return builder.toString();
  }

  @Override
  public String toString() {
    return "LocalDateTime column: " + name();
  }

  @Override
  public void appendColumnData(Column column) {
    Preconditions.checkArgument(column.type() == this.type());
    LocalDateTimeColumn intColumn = (LocalDateTimeColumn) column;
    for (int i = 0; i < intColumn.size(); i++) {
      add(intColumn.get(i));
    }
  }
}
