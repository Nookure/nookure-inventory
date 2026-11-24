package com.nookure.core.inv.paper;

import com.nookure.core.inv.I18nAdapter;
import com.nookure.core.inv.exception.UserFriendlyRuntimeException;
import com.nookure.core.inv.paper.utils.ServerUtils;
import com.nookure.core.inv.paper.utils.SkullCreator;
import com.nookure.core.inv.parser.item.HeadType;
import com.nookure.core.inv.parser.item.Item;
import com.nookure.core.inv.parser.item.ItemConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaperItemConverter extends ItemConverter<ItemStack, Player> {
  private static final PaperItemConverter instance = new PaperItemConverter();
  public static final String ID_KEY = "nookureinventory";
  public static final NamespacedKey ITEM_ID = new NamespacedKey(ID_KEY, "item_id");

  public static PaperItemConverter getInstance() {
    return instance;
  }

  @Override
  public Item convert(ItemStack itemStack) {
    return null;
  }

  @Override
  public ItemStack convert(Item item, I18nAdapter<Player> adapter) {
    return convert(item, adapter, null);
  }

  @Override
  @SuppressWarnings("deprecation")
  public ItemStack convert(Item item, I18nAdapter<Player> adapter, @Nullable Player player) {
    if (item.material() == null && item.head() == null) {
      throw new UserFriendlyRuntimeException("Material or head must be set for item " + item.id());
    }

    ItemStack itemStack;
    if (item.material() == null) {
      itemStack = createHead(item.head(), item.headType(), item.id());

      if (itemStack == null) {
        throw new UserFriendlyRuntimeException("Invalid head settings for item " + item.id());
      }
    } else {
      try {
        itemStack = new ItemStack(Material.valueOf(item.material().toUpperCase()));
      } catch (IllegalArgumentException e) {
        throw new UserFriendlyRuntimeException("Invalid material " + item.material() + " for item " + item.id());
      }
    }

    itemStack.setAmount(item.amount());

    ItemMeta meta = itemStack.getItemMeta();

    if (meta == null) {
      return itemStack;
    }

    if (ServerUtils.isPaper) {
      if (item.name() != null) {
        meta.displayName(nonItalic(adapter.translateComponent(item.name(), item.tl(), player)));
      }

      List<Component> lore = new ArrayList<>();
      if (item.lore() != null && item.lore().loreLines() != null) {
        item.lore().loreLines().forEach(line ->
            lore.add(nonItalic(adapter.translateComponent(line, item.tl(), player)))
        );
      }

      if (item.literalLore() != null) {
        item.literalLore().loreLines().forEach(line ->
            lore.add(nonItalic(adapter.translateComponent(line, item.tl(), player)))
        );
      }

      if (!lore.isEmpty()) {
        meta.lore(lore);
      }
    } else {
      if (item.name() != null) {
        meta.setDisplayName(adapter.translate(item.name(), item.tl(), player));
      }

      List<String> lore = new ArrayList<>();
      if (item.lore() != null && item.lore().loreLines() != null) {
        item.lore().loreLines().forEach(line -> lore.add(adapter.translate(line, item.tl(), player)));
      }

      if (item.literalLore() != null) {
        item.literalLore().loreLines().forEach(line -> lore.add(adapter.translate(line, item.tl(), player)));
      }

      if (!lore.isEmpty()) {
        meta.setLore(lore);
      }
    }

    PersistentDataContainer data = meta.getPersistentDataContainer();
    String resolvedId = item.id();
    if (resolvedId == null) {
      String prefix = item.material() != null ? item.material() : "HEAD";
      resolvedId = prefix + "$" + item.slot();
      item.setId(resolvedId);
    }
    data.set(ITEM_ID, PersistentDataType.STRING, resolvedId);

    if (item.enchanted()) {
      Registry.ENCHANTMENT.iterator().forEachRemaining(enchantment -> {
        if (meta.getEnchants().isEmpty()) {
          meta.addEnchant(enchantment, 1, true);
          meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
      });
    }

    itemStack.setItemMeta(meta);
    return itemStack;
  }

  private static Component nonItalic(Component component) {
    return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
  }

  private ItemStack createHead(String value, HeadType type, String itemId) {
    if (type == null) {
      throw new UserFriendlyRuntimeException("headType must be set when using head on item " + itemId);
    }

    return switch (type) {
      case PLAYER -> {
        // noinspection deprecation
        yield SkullCreator.itemFromName(value);
      }
      case PLAYER_UUID -> {
        UUID uuid;
        try {
          uuid = UUID.fromString(value);
        } catch (IllegalArgumentException e) {
          throw new UserFriendlyRuntimeException("Invalid UUID " + value + " for item head");
        }
        yield SkullCreator.itemFromUuid(uuid);
      }
      case URL -> SkullCreator.itemFromUrl(value);
      case BASE64 -> SkullCreator.itemFromBase64(value);
    };
  }
}
