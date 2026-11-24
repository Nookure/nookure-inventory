package com.nookure.core.inv.parser.item;

import com.nookure.core.inv.I18nAdapter;
import org.jetbrains.annotations.Nullable;

public abstract class ItemConverter<T, P> {
  public abstract T convert(Item item, I18nAdapter<P> adapter);

  public T convert(Item item, I18nAdapter<P> adapter, @Nullable P player) {
    return convert(item, adapter);
  }

  public abstract Item convert(T t);
}
