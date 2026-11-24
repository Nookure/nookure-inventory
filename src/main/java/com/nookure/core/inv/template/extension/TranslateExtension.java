package com.nookure.core.inv.template.extension;

import com.nookure.core.inv.I18nAdapter;
import com.nookure.core.inv.exception.UserFriendlyRuntimeException;
import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class TranslateExtension<T> extends AbstractExtension implements Function {
  private final I18nAdapter<T> adapter;

  @Inject
  public TranslateExtension(I18nAdapter<T> adapter) {
    this.adapter = adapter;
  }

  @Override
  public Map<String, Function> getFunctions() {
    return Map.of("tl", this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
    Object rawKey = args.get("key");
    if (rawKey == null) {
      throw new UserFriendlyRuntimeException("Missing required argument 'key' --> tl(key: string, @Optional player: object)");
    }

    String key = rawKey.toString();
    Object player = args.get("player");
    if (player == null && context != null) {
      player = context.getVariable("player");
    }

    return adapter.translate(key, true, (T) player);
  }

  @Override
  public List<String> getArgumentNames() {
    return List.of("key", "player");
  }
}
