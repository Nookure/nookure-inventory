package com.nookure.core.inv.test;

import com.google.gson.Gson;
import com.nookure.core.inv.parser.item.action.json.OpenInventoryProps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JSONTest {
  @Test
  public void serializeTest() {
    OpenInventoryProps props = new OpenInventoryProps("test", Map.of("player", UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479")));
    Gson gson = new Gson();
    String json = gson.toJson(props);

    Assertions.assertEquals("{\"inventoryName\":\"test\",\"context\":{\"player\":\"f47ac10b-58cc-4372-a567-0e02b2c3d479\"}}", json);
  }

  @Test
  public void deserializeTest() {
    String json = "{\"inventoryName\":\"test\",\"context\":{\"player\":\"f47ac10b-58cc-4372-a567-0e02b2c3d479\"}}";
    Gson gson = new Gson();
    OpenInventoryProps props = gson.fromJson(json, OpenInventoryProps.class);

    Assertions.assertEquals("test", props.getInventoryName());
    Assertions.assertEquals("f47ac10b-58cc-4372-a567-0e02b2c3d479", props.getContext().get("player"));
  }

  @Test
  public void deserializeTypedContextAndUnknownFieldsTest() {
    Map<String, Object> original = new HashMap<>();
    original.put("page", 2);
    original.put("ratio", 1.5);
    original.put("enabled", true);
    original.put("nullable", null);
    OpenInventoryProps props = new OpenInventoryProps("paginated", original);

    Gson gson = new Gson();
    String json = gson.toJson(props);
    OpenInventoryProps decoded = gson.fromJson(json, OpenInventoryProps.class);

    Assertions.assertEquals("paginated", decoded.getInventoryName());
    Assertions.assertEquals(2, decoded.getContext().get("page"));
    Assertions.assertEquals(1.5, decoded.getContext().get("ratio"));
    Assertions.assertEquals(true, decoded.getContext().get("enabled"));
    Assertions.assertFalse(decoded.getContext().containsKey("nullable"));

    String withNullAndExtra = "{\"unknown\":123,\"inventoryName\":\"extra\",\"context\":{\"page\":3,\"nullable\":null}}";
    OpenInventoryProps extraDecoded = gson.fromJson(withNullAndExtra, OpenInventoryProps.class);
    Assertions.assertEquals("extra", extraDecoded.getInventoryName());
    Assertions.assertEquals(3, extraDecoded.getContext().get("page"));
    Assertions.assertTrue(extraDecoded.getContext().containsKey("nullable"));
    Assertions.assertNull(extraDecoded.getContext().get("nullable"));
  }
}
