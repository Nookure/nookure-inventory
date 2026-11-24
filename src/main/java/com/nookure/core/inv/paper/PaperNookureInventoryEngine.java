package com.nookure.core.inv.paper;

import com.nookure.core.inv.GuiMetadata;
import com.nookure.core.inv.I18nAdapter;
import com.nookure.core.inv.InventoryOpener;
import com.nookure.core.inv.NookureInventoryEngine;
import com.nookure.core.inv.paper.service.CustomActionRegistry;
import com.nookure.core.inv.paper.utils.ServerUtils;
import com.nookure.core.inv.parser.GuiLayout;
import com.nookure.core.inv.template.extension.GetBukkitPlayerExtension;
import com.nookure.core.inv.template.extension.OpenInventoryExtension;
import com.nookure.core.inv.template.extension.PaginationItemExtension;
import com.nookure.core.inv.template.extension.TranslateExtension;
import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.loader.FileLoader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PaperNookureInventoryEngine extends NookureInventoryEngine implements InventoryOpener<Player> {
  private final JavaPlugin plugin;
  private final PaperOpenInventoryRegistry registry = new PaperOpenInventoryRegistry();
  private final I18nAdapter<Player> i18nAdapter;

  public PaperNookureInventoryEngine(
      @NotNull JavaPlugin plugin,
      @NotNull String templateFolder,
      @NotNull I18nAdapter<Player> i18nAdapter,
      AbstractExtension... extensions
  ) {
    super(getLoader(plugin, templateFolder), extensions);
    this.i18nAdapter = i18nAdapter;
    this.plugin = plugin;

    registerBukkitEvents();
    registerService();
  }

  private static FileLoader getLoader(JavaPlugin plugin, String templateFolder) {
    return new FileLoader(plugin.getDataFolder().toPath().resolve(templateFolder).toString());
  }

  public void registerBukkitEvents() {
    Bukkit.getPluginManager().registerEvents(new InventoryListener(registry), plugin);
  }

  public void registerService() {
    if (Bukkit.getServicesManager().load(CustomActionRegistry.class) == null) {
      Bukkit.getServicesManager()
          .register(
              CustomActionRegistry.class,
              new CustomActionRegistry(),
              plugin,
              ServicePriority.Normal
          );
    }
  }

  @Override
  public void openAsync(@NotNull Player player, @NotNull String layout, Object... args) {
    Map<String, Object> context = toMap(args);
    context.putIfAbsent("player", player);

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      GuiLayout guiLayout;

      try {
        guiLayout = parseLayout(layout, context);
      } catch (Exception e) {
        plugin.getLogger().log(Level.SEVERE, "Failed to parse GUI layout '" + layout + "' for player " + player.getName(), e);
        return;
      }

      Bukkit.getScheduler().runTask(plugin, () -> {
        if (!player.isOnline()) {
          return;
        }

        String permission = guiLayout.head() != null ? guiLayout.head().permission() : null;
        if (permission != null && !player.hasPermission(permission)) {
          String permissionMessage = guiLayout.head().noPermissionMessage();
          if (permissionMessage == null) {
            permissionMessage = "<red>You do not have permission to open this inventory";
          }

          if (ServerUtils.isPaper) {
            player.sendMessage(i18nAdapter.translateComponent(permissionMessage, false, player));
          } else {
            player.sendMessage(i18nAdapter.translate(permissionMessage, false, player));
          }
          return;
        }

        InventoryContainer container = new InventoryContainer(
            player,
            guiLayout,
            i18nAdapter,
            registry,
            this,
            new GuiMetadata(layout, args)
        );
        registry.register(player.getUniqueId(), container);
        player.openInventory(container.getInventory());
      });
    });
  }

  public static class Builder {
    private JavaPlugin plugin;
    private String templateFolder;
    private I18nAdapter<Player> i18nAdapter;
    private AbstractExtension[] extensions;

    public Builder plugin(JavaPlugin plugin) {
      this.plugin = plugin;
      return this;
    }

    public Builder templateFolder(String templateFolder) {
      this.templateFolder = templateFolder;
      return this;
    }

    public Builder i18nAdapter(I18nAdapter<Player> i18nAdapter) {
      this.i18nAdapter = i18nAdapter;
      return this;
    }

    public Builder extensions(AbstractExtension... extensions) {
      this.extensions = extensions;
      return this;
    }

    public PaperNookureInventoryEngine build() {
      if (plugin == null) {
        throw new IllegalStateException("plugin is required");
      }

      if (templateFolder == null) {
        templateFolder = "gui";
      }

      if (i18nAdapter == null) {
        i18nAdapter = new DefaultPaperAdapter();
      }

      List<AbstractExtension> allExtensions = new ArrayList<>();
      allExtensions.add(new PaginationItemExtension());
      allExtensions.add(new OpenInventoryExtension());
      allExtensions.add(new GetBukkitPlayerExtension());
      allExtensions.add(new TranslateExtension<>(i18nAdapter));
      if (extensions != null) {
        Collections.addAll(allExtensions, extensions);
      }

      return new PaperNookureInventoryEngine(
          plugin,
          templateFolder,
          i18nAdapter,
          allExtensions.toArray(AbstractExtension[]::new)
      );
    }
  }
}
