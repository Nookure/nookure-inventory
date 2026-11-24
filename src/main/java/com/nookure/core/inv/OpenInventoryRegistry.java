package com.nookure.core.inv;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public abstract class OpenInventoryRegistry<T> {
  private final BiMap<UUID, T> registry = HashBiMap.create();
  private final Object lock = new Object();

  public void register(UUID uuid, T value) {
    synchronized (lock) {
      registry.forcePut(uuid, value);
    }
  }

  @Nullable
  public T get(UUID uuid) {
    synchronized (lock) {
      return registry.get(uuid);
    }
  }

  @Nullable
  public UUID get(T value) {
    synchronized (lock) {
      return registry.inverse().get(value);
    }
  }

  public void unregister(UUID uuid) {
    synchronized (lock) {
      registry.remove(uuid);
    }
  }

  public void unregister(T value) {
    synchronized (lock) {
      registry.inverse().remove(value);
    }
  }

  public boolean contains(UUID uuid) {
    synchronized (lock) {
      return registry.containsKey(uuid);
    }
  }

  public boolean contains(T value) {
    synchronized (lock) {
      return registry.containsValue(value);
    }
  }

  public BiMap<UUID, T> getRegistry() {
    synchronized (lock) {
      return Maps.unmodifiableBiMap(HashBiMap.create(registry));
    }
  }

  public void clear() {
    synchronized (lock) {
      registry.clear();
    }
  }

  public int size() {
    synchronized (lock) {
      return registry.size();
    }
  }

  public Set<T> values() {
    synchronized (lock) {
      return Set.copyOf(registry.values());
    }
  }

  public Set<UUID> keys() {
    synchronized (lock) {
      return Set.copyOf(registry.keySet());
    }
  }
}
