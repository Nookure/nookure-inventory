package com.nookure.core.inv;

import com.nookure.core.inv.exception.UserFriendlyRuntimeException;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class GuiMetadata {
  private final String layout;
  private final Object[] args;
  private final boolean isPagination;
  private final int nextPage;
  private final int previousPage;

  public GuiMetadata(
      @NotNull String layout,
      @NotNull Object... args
  ) {
    this.layout = layout;
    this.args = args;

    Map<String, Object> context = NookureInventoryEngine.toMap(args);
    Object rawPage = context.get("page");
    this.isPagination = rawPage != null;

    if (isPagination) {
      int page = parsePage(rawPage);
      this.nextPage = page + 1;
      this.previousPage = page - 1;
    } else {
      this.nextPage = -1;
      this.previousPage = -1;
    }
  }

  private static int parsePage(Object rawPage) {
    if (rawPage instanceof Number number) {
      return number.intValue();
    }
    if (rawPage instanceof String str) {
      try {
        return Integer.parseInt(str.trim());
      } catch (NumberFormatException e) {
        throw new UserFriendlyRuntimeException("Invalid page number: " + str, e);
      }
    }
    throw new UserFriendlyRuntimeException("Invalid page type: " + rawPage.getClass().getName());
  }

  public boolean isPagination() {
    return isPagination;
  }

  public String layout() {
    return layout;
  }

  public Object[] args() {
    return args;
  }

  public int nextPage() {
    return nextPage;
  }

  public int previousPage() {
    return previousPage;
  }
}
