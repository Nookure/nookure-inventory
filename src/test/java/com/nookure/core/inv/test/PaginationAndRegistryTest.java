package com.nookure.core.inv.test;

import com.nookure.core.inv.GuiMetadata;
import com.nookure.core.inv.I18nAdapter;
import com.nookure.core.inv.NookureInventoryEngine;
import com.nookure.core.inv.OpenInventoryRegistry;
import com.nookure.core.inv.exception.UserFriendlyRuntimeException;
import com.nookure.core.inv.parser.adapters.MiniMessageAdapter;
import com.nookure.core.inv.template.extension.PaginationItemExtension;
import com.nookure.core.inv.template.extension.TranslateExtension;
import io.pebbletemplates.pebble.loader.StringLoader;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

public class PaginationAndRegistryTest {
  @Test
  public void paginationHandlesNumericVariantsAndEmptyTotal() {
    PaginationItemExtension ext = new PaginationItemExtension();

    PaginationItemExtension.PaginationData data = (PaginationItemExtension.PaginationData) ext.execute(
        Map.of("rows", 9, "columns", 5L, "page", "2", "total", 100),
        null,
        null,
        1
    );
    Assertions.assertEquals(45, data.start());
    Assertions.assertEquals(89, data.end());
    Assertions.assertEquals(3, data.lastPage());
    Assertions.assertFalse(data.isEmpty());

    PaginationItemExtension.PaginationData empty = (PaginationItemExtension.PaginationData) ext.execute(
        Map.of("rows", 9L, "columns", 5L, "page", 1, "total", 0),
        null,
        null,
        1
    );
    Assertions.assertTrue(empty.isEmpty());
    Assertions.assertEquals(1, empty.lastPage());

    Assertions.assertThrows(UserFriendlyRuntimeException.class, () -> ext.execute(
        Map.of("rows", 0, "columns", 5, "page", 1, "total", 10),
        null,
        null,
        1
    ));
  }

  @Test
  public void guiMetadataParsesNumericVariants() {
    GuiMetadata fromLong = new GuiMetadata("test.peb", "page", 2L);
    Assertions.assertTrue(fromLong.isPagination());
    Assertions.assertEquals(3, fromLong.nextPage());
    Assertions.assertEquals(1, fromLong.previousPage());

    GuiMetadata fromString = new GuiMetadata("test.peb", "page", "4");
    Assertions.assertTrue(fromString.isPagination());
    Assertions.assertEquals(5, fromString.nextPage());
    Assertions.assertEquals(3, fromString.previousPage());
  }

  @Test
  public void openInventoryRegistryForcePutAndUnregister() {
    OpenInventoryRegistry<String> registry = new OpenInventoryRegistry<>() {};
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();

    registry.register(uuid1, "invA");
    registry.register(uuid2, "invA");
    Assertions.assertEquals(1, registry.size());
    Assertions.assertNull(registry.get(uuid1));
    Assertions.assertEquals("invA", registry.get(uuid2));

    registry.unregister("invA");
    Assertions.assertEquals(0, registry.size());
  }

  @Test
  public void miniMessageAdapterEscapesAndNullSafety() {
    Assertions.assertEquals("", MiniMessageAdapter.format(null));
    Assertions.assertEquals("<red>[VIP]</red>", MiniMessageAdapter.format("[red]\\[VIP\\][/red]"));
  }

  @Test
  public void translateExtensionResolvesContextPlayer() {
    I18nAdapter<String> adapter = new I18nAdapter<>() {
      @Override
      public String translate(String key, boolean isKey, @Nullable String player) {
        return key + ":" + (player == null ? "none" : player);
      }

      @Override
      public Component translateComponent(String key, boolean isKey, @Nullable String player) {
        return Component.text(translate(key, isKey, player));
      }
    };

    NookureInventoryEngine engine = new NookureInventoryEngine(new StringLoader(), new TranslateExtension<>(adapter));
    String rendered = engine.renderTemplate("{{ tl('menu.title') }}", "player", "Lunna");
    Assertions.assertEquals("menu.title:Lunna", rendered);
  }
}
