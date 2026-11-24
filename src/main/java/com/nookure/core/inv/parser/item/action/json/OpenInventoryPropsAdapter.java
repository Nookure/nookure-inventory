package com.nookure.core.inv.parser.item.action.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OpenInventoryPropsAdapter extends TypeAdapter<OpenInventoryProps> {
  @Override
  public void write(JsonWriter out, OpenInventoryProps value) throws IOException {
    out.beginObject();
    out.name("inventoryName").value(value.getInventoryName());
    out.name("context").beginObject();
    if (value.getContext() != null) {
      for (Map.Entry<String, Object> entry : value.getContext().entrySet()) {
        out.name(entry.getKey());
        Object val = entry.getValue();
        if (val == null) {
          out.nullValue();
        } else if (val instanceof String s) {
          out.value(s);
        } else if (val instanceof Number n) {
          out.value(n);
        } else if (val instanceof Boolean b) {
          out.value(b);
        } else {
          out.value(val.toString());
        }
      }
    }
    out.endObject();
    out.endObject();
  }

  @Override
  public OpenInventoryProps read(JsonReader in) throws IOException {
    String inventoryName = null;
    Map<String, Object> context = new HashMap<>();

    in.beginObject();
    while (in.hasNext()) {
      String name = in.nextName();
      if (name.equals("inventoryName")) {
        if (in.peek() == JsonToken.NULL) {
          in.nextNull();
          inventoryName = null;
        } else {
          inventoryName = in.nextString();
        }
      } else if (name.equals("context")) {
        if (in.peek() == JsonToken.NULL) {
          in.nextNull();
          context = new HashMap<>();
        } else {
          context = readContext(in);
        }
      } else {
        in.skipValue();
      }
    }
    in.endObject();

    return new OpenInventoryProps(inventoryName, context);
  }

  private Map<String, Object> readContext(JsonReader in) throws IOException {
    Map<String, Object> context = new HashMap<>();

    in.beginObject();
    while (in.hasNext()) {
      String name = in.nextName();
      JsonToken token = in.peek();
      switch (token) {
        case STRING -> context.put(name, in.nextString());
        case BOOLEAN -> context.put(name, in.nextBoolean());
        case NULL -> {
          in.nextNull();
          context.put(name, null);
        }
        case NUMBER -> context.put(name, parseNumber(in.nextString()));
        default -> in.skipValue();
      }
    }
    in.endObject();

    return context;
  }

  private static Number parseNumber(String raw) {
    if (raw.contains(".") || raw.contains("e") || raw.contains("E")) {
      return Double.parseDouble(raw);
    }
    long value = Long.parseLong(raw);
    if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
      return (int) value;
    }
    return value;
  }
}
