package net.emc.emce.modules;

import io.github.emcw.KnownMap;
import net.emc.emce.EMCEssentials;
import net.emc.emce.caches.AllianceDataCache;
import net.emc.emce.caches.Cache;
import net.emc.emce.config.ModConfig;

import net.emc.emce.utils.Messaging;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TaskScheduler {
    public boolean townlessRunning, nearbyRunning, cacheCheckRunning;
    public boolean hasMap = false;

    private static final List<Cache<?>> CACHES = List.of(AllianceDataCache.INSTANCE);
    private ScheduledExecutorService service;

    public void start() {
        ModConfig config = ModConfig.instance();

        // Pre-fill data.
        if (config.general.enableMod) {
            if (config.townless.enabled) EMCEssentials.instance().updateTownless();
            if (config.nearby.enabled) EMCEssentials.instance().updateNearbyPlayers();
        }

        startCacheCheck();
        startTownless();
        startNearby();
    }

    public void stop() {
        townlessRunning = false;
        nearbyRunning = false;
        cacheCheckRunning = false;

        Messaging.sendDebugMessage("Stopping scheduled tasks...");
    }

    public void initMap() {
        service = Executors.newScheduledThreadPool(2);
        service.scheduleAtFixedRate(this::checkMap, 0, 10, TimeUnit.SECONDS);
    }

    void checkMap() {
        if (hasMap) return;

        if (EMCEssentials.instance().clientOnlineInSquaremap(KnownMap.AURORA)) {
            setHasMap(KnownMap.AURORA.getName());
        }
        else setHasMap(null);
    }

    public void reset() {
        OverlayRenderer.Clear();
        setHasMap(null);
    }

    public void setHasMap(String map) {
        if (map == null) {
            hasMap = false;
            EMCEssentials.instance().currentMap = KnownMap.AURORA;

            stop();
            Messaging.sendDebugMessage("Player not found on any map!");
        } else {
            hasMap = true;

            start();
            Messaging.sendDebugMessage("Player found on: " + map);
        }
    }

    private void startTownless() {
        townlessRunning = true;
        ModConfig config = ModConfig.instance();

        service.scheduleAtFixedRate(() -> {
            if (townlessRunning && config.townless.enabled && shouldRun()) {
                EMCEssentials.instance().updateTownless();
                Messaging.sendDebugMessage("Updating townless...");
            }
        }, 5, Math.min(config.intervals.townless, 200), TimeUnit.SECONDS);
    }

    private void startNearby() {
        nearbyRunning = true;
        ModConfig config = ModConfig.instance();

        service.scheduleAtFixedRate(() -> {
            if (nearbyRunning && config.nearby.enabled && shouldRun()) {
                EMCEssentials.instance().updateNearbyPlayers();
                Messaging.sendDebugMessage("Updating nearby...");
            }
        }, 5, Math.min(config.intervals.nearby, 30), TimeUnit.SECONDS);
    }

    private void startCacheCheck() {
        cacheCheckRunning = true;

        service.scheduleAtFixedRate(() -> {
            if (!cacheCheckRunning) return;

            for (Cache<?> cache : CACHES) {
                if (cache.cacheNeedsUpdate()) {
                    cache.clearCache();
                }
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    boolean shouldRun() {
        boolean modEnabled = ModConfig.instance().general.enableMod;
        boolean focused = MinecraftClient.getInstance().isWindowFocused();

        return modEnabled && focused;
    }
}