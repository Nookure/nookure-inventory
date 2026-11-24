package com.nookure.core.inv.template.extension;

import com.nookure.core.inv.exception.UserFriendlyRuntimeException;
import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.util.List;
import java.util.Map;

public class PaginationItemExtension extends AbstractExtension implements Function {
  @Override
  public Map<String, Function> getFunctions() {
    return Map.of("pagination", this);
  }

  public record PaginationData(long start, long end, int lastPage, boolean isEmpty) {
    public PaginationData(long start, long end, int lastPage) {
      this(start, end, lastPage, end < start);
    }
  }

  @Override
  public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
    Long rows = parseLong(args.get("rows"), "rows");
    Long columns = parseLong(args.get("columns"), "columns");
    Integer page = parseInt(args.get("page"), "page");
    Integer total = parseInt(args.get("total"), "total");

    if (rows == null || columns == null || page == null || total == null) {
      throw new UserFriendlyRuntimeException("Missing required arguments for pagination ---> pagination(rows, columns, page, total)");
    }

    if (rows <= 0 || columns <= 0 || page <= 0 || total < 0) {
      throw new UserFriendlyRuntimeException("Pagination arguments rows, columns, and page must be >= 1, and total must be >= 0");
    }

    long pageSize = rows * columns;
    int lastPage = Math.max(1, (int) Math.ceil((double) total / pageSize));
    long start = (long) (page - 1) * pageSize;

    if (total == 0 || start >= total) {
      return new PaginationData(0, -1, lastPage, true);
    }

    long end = Math.min(start + pageSize, total) - 1;
    return new PaginationData(start, end, lastPage, false);
  }

  private static Long parseLong(Object value, String name) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    if (value instanceof String str) {
      try {
        return Long.parseLong(str.trim());
      } catch (NumberFormatException e) {
        throw new UserFriendlyRuntimeException("Invalid number for pagination argument '" + name + "': " + str, e);
      }
    }
    throw new UserFriendlyRuntimeException("Invalid type for pagination argument '" + name + "': " + value.getClass().getName());
  }

  private static Integer parseInt(Object value, String name) {
    Long parsed = parseLong(value, name);
    return parsed == null ? null : parsed.intValue();
  }

  @Override
  public List<String> getArgumentNames() {
    return List.of("rows", "columns", "page", "total");
  }
}
