package com.nookure.core.inv.paper.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nookure.core.inv.exception.UserFriendlyRuntimeException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * This utility class is used to create player skulls with custom textures.
 * Originally inspired by deanveloper's SkullCreator, modernized to use
 * Paper's PlayerProfile API and Bukkit's PlayerProfile API without NMS reflection.
 */
public class SkullCreator {
  private static final Logger logger = Logger.getLogger("NookureInventory");
  private static boolean warningPosted = false;

  private SkullCreator() {
  }

  /**
   * Creates a player skull, should work in both legacy and new Bukkit APIs.
   */
  public static ItemStack createSkull() {
    checkLegacy();

    try {
      return new ItemStack(Material.valueOf("PLAYER_HEAD"));
    } catch (IllegalArgumentException e) {
      //noinspection deprecation
      return new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (byte) 3);
    }
  }

  /**
   * Creates a player skull item with the skin based on a player's name.
   *
   * @param name The Player's name.
   * @return The head of the Player.
   * @deprecated names don't make for good identifiers.
   */
  @Deprecated
  public static ItemStack itemFromName(String name) {
    return itemWithName(createSkull(), name);
  }

  /**
   * Creates a player skull item with the skin based on a player's UUID.
   *
   * @param id The Player's UUID.
   * @return The head of the Player.
   */
  public static ItemStack itemFromUuid(UUID id) {
    return itemWithUuid(createSkull(), id);
  }

  /**
   * Creates a player skull item with the skin at a Mojang URL.
   *
   * @param url The Mojang URL.
   * @return The head of the Player.
   */
  public static ItemStack itemFromUrl(String url) {
    return itemWithUrl(createSkull(), url);
  }

  /**
   * Creates a player skull item with the skin based on a base64 string.
   *
   * @param base64 The Base64 string.
   * @return The head of the Player.
   */
  public static ItemStack itemFromBase64(String base64) {
    return itemWithBase64(createSkull(), base64);
  }

  /**
   * Modifies a skull to use the skin of the player with a given name.
   *
   * @param item The item to apply the name to. Must be a player skull.
   * @param name The Player's name.
   * @return The head of the Player.
   * @deprecated names don't make for good identifiers.
   */
  @Deprecated
  public static ItemStack itemWithName(ItemStack item, String name) {
    notNull(item, "item");
    notNull(name, "name");

    if (!(item.getItemMeta() instanceof SkullMeta meta)) {
      return null;
    }

    if (ServerUtils.isPaper) {
      meta.setPlayerProfile(Bukkit.createProfile(name));
    } else {
      //noinspection deprecation
      meta.setOwner(name);
    }
    item.setItemMeta(meta);

    return item;
  }

  /**
   * Modifies a skull to use the skin of the player with a given UUID.
   *
   * @param item The item to apply the name to. Must be a player skull.
   * @param id   The Player's UUID.
   * @return The head of the Player.
   */
  public static ItemStack itemWithUuid(ItemStack item, UUID id) {
    notNull(item, "item");
    notNull(id, "id");

    if (!(item.getItemMeta() instanceof SkullMeta meta)) {
      return null;
    }

    meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
    item.setItemMeta(meta);

    return item;
  }

  /**
   * Modifies a skull to use the skin at the given Mojang URL.
   *
   * @param item The item to apply the skin to. Must be a player skull.
   * @param url  The URL of the Mojang skin.
   * @return The head associated with the URL.
   */
  public static ItemStack itemWithUrl(ItemStack item, String url) {
    notNull(item, "item");
    notNull(url, "url");

    return itemWithBase64(item, urlToBase64(url));
  }

  /**
   * Modifies a skull to use the skin based on the given base64 string.
   *
   * @param item   The ItemStack to put the base64 onto. Must be a player skull.
   * @param base64 The base64 string containing the texture.
   * @return The head with a custom texture.
   */
  public static ItemStack itemWithBase64(ItemStack item, String base64) {
    notNull(item, "item");
    notNull(base64, "base64");

    if (!(item.getItemMeta() instanceof SkullMeta meta)) {
      return null;
    }

    applyBase64(meta, base64);
    item.setItemMeta(meta);

    return item;
  }

  /**
   * Sets the block to a skull with the given name.
   *
   * @param block The block to set.
   * @param name  The player to set it to.
   * @deprecated names don't make for good identifiers.
   */
  @Deprecated
  public static void blockWithName(Block block, String name) {
    notNull(block, "block");
    notNull(name, "name");

    setToSkull(block);
    Skull state = (Skull) block.getState();
    //noinspection deprecation
    state.setOwningPlayer(Bukkit.getOfflinePlayer(name));
    state.update(false, false);
  }

  /**
   * Sets the block to a skull with the given UUID.
   *
   * @param block The block to set.
   * @param id    The player to set it to.
   */
  public static void blockWithUuid(Block block, UUID id) {
    notNull(block, "block");
    notNull(id, "id");

    setToSkull(block);
    Skull state = (Skull) block.getState();
    state.setOwningPlayer(Bukkit.getOfflinePlayer(id));
    state.update(false, false);
  }

  /**
   * Sets the block to a skull with the skin found at the provided Mojang URL.
   *
   * @param block The block to set.
   * @param url   The Mojang URL to set it to use.
   */
  public static void blockWithUrl(Block block, String url) {
    notNull(block, "block");
    notNull(url, "url");

    blockWithBase64(block, urlToBase64(url));
  }

  /**
   * Sets the block to a skull with the skin for the base64 string.
   *
   * @param block  The block to set.
   * @param base64 The base64 to set it to use.
   */
  public static void blockWithBase64(Block block, String base64) {
    notNull(block, "block");
    notNull(base64, "base64");

    setToSkull(block);
    Skull state = (Skull) block.getState();
    applyBase64(state, base64);
    state.update(false, false);
  }

  private static void setToSkull(Block block) {
    checkLegacy();

    try {
      block.setType(Material.valueOf("PLAYER_HEAD"), false);
    } catch (IllegalArgumentException e) {
      block.setType(Material.valueOf("SKULL"), false);
      Skull state = (Skull) block.getState();
      //noinspection deprecation
      state.setSkullType(SkullType.PLAYER);
      state.update(false, false);
    }
  }

  private static void notNull(Object o, String name) {
    if (o == null) {
      throw new UserFriendlyRuntimeException(new NullPointerException(name + " should not be null!"));
    }
  }

  private static String urlToBase64(String url) {
    URI actualUrl;
    try {
      actualUrl = new URI(url);
    } catch (URISyntaxException e) {
      throw new UserFriendlyRuntimeException("Invalid skin URL: " + url, e);
    }
    String toEncode = "{\"textures\":{\"SKIN\":{\"url\":\"" + actualUrl + "\"}}}";
    return Base64.getEncoder().encodeToString(toEncode.getBytes(StandardCharsets.UTF_8));
  }

  private static UUID deterministicUuid(String b64) {
    return UUID.nameUUIDFromBytes(b64.getBytes(StandardCharsets.UTF_8));
  }

  private static void applyBase64(SkullMeta meta, String b64) {
    UUID id = deterministicUuid(b64);
    if (ServerUtils.isPaper) {
      PlayerProfile profile = Bukkit.createProfile(id, "Player");
      profile.setProperty(new ProfileProperty("textures", b64));
      meta.setPlayerProfile(profile);
    } else {
      meta.setOwnerProfile(createSpigotProfile(id, b64));
    }
  }

  private static void applyBase64(Skull block, String b64) {
    UUID id = deterministicUuid(b64);
    if (ServerUtils.isPaper) {
      PlayerProfile profile = Bukkit.createProfile(id, "Player");
      profile.setProperty(new ProfileProperty("textures", b64));
      block.setPlayerProfile(profile);
    } else {
      block.setOwnerProfile(createSpigotProfile(id, b64));
    }
  }

  private static org.bukkit.profile.PlayerProfile createSpigotProfile(UUID id, String b64) {
    org.bukkit.profile.PlayerProfile profile = Bukkit.createPlayerProfile(id, "Player");
    URL skinUrl = extractSkinUrl(b64);
    if (skinUrl != null) {
      PlayerTextures textures = profile.getTextures();
      textures.setSkin(skinUrl);
      profile.setTextures(textures);
    }
    return profile;
  }

  private static URL extractSkinUrl(String b64) {
    try {
      String decoded = new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
      JsonObject root = JsonParser.parseString(decoded).getAsJsonObject();
      String url = root.getAsJsonObject("textures")
          .getAsJsonObject("SKIN")
          .get("url")
          .getAsString();
      return URI.create(url).toURL();
    } catch (IllegalArgumentException | IllegalStateException | NullPointerException | MalformedURLException e) {
      throw new UserFriendlyRuntimeException("Invalid base64 skin texture payload", e);
    }
  }

  private static void checkLegacy() {
    try {
      Material.class.getDeclaredField("PLAYER_HEAD");
      Material.valueOf("SKULL");

      if (!warningPosted) {
        logger.warning("Using the legacy bukkit API with 1.13+ bukkit versions is not supported!");
        warningPosted = true;
      }
    } catch (NoSuchFieldException | IllegalArgumentException ignored) {
    }
  }
}
