package net.emc.emce.modules;

import net.emc.emce.EarthMCEssentials;
import net.emc.emce.caches.AllianceDataCache;
import net.emc.emce.caches.Cache;
import net.emc.emce.caches.NationDataCache;
import net.emc.emce.caches.TownDataCache;
import net.emc.emce.config.ModConfig;
import net.emc.emce.utils.EarthMCAPI;
import net.emc.emce.utils.Messaging;
import net.emc.emce.utils.ModUtils;
import net.minecraft.client.MinecraftClient;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.emc.emce.EarthMCEssentials.instance;
import static net.emc.emce.utils.EarthMCAPI.playerOnline;

public class TaskScheduler {
    public ScheduledExecutorService service;
    public boolean townlessRunning, nearbyRunning, cacheCheckRunning, newsRunning;
    public boolean hasMap = false;

    private static final List<Cache<?>> CACHES = Arrays.asList(
            NationDataCache.INSTANCE,
            TownDataCache.INSTANCE,
            AllianceDataCache.INSTANCE
    );

    public void start() {
        ModConfig config = ModConfig.instance();

        // Pre-fill data.
        if (config.general.enableMod) {
            if (config.townless.enabled) EarthMCAPI.getTownless().thenAccept(instance()::setTownlessResidents);
            if (config.nearby.enabled) EarthMCAPI.getNearby().thenAccept(instance()::setNearbyPlayers);
        }

        startTownless();
        startNearby();
        startNews();
        startCacheCheck();
    }

    public void stop() {
        townlessRunning = false;
        nearbyRunning = false;
        newsRunning = false;
        cacheCheckRunning = false;

        Messaging.sendDebugMessage("Stopping scheduled tasks...");
    }

    public void initMap() {
        service = Executors.newScheduledThreadPool(2);
        service.scheduleAtFixedRate(() -> {
            if (hasMap) return;

            if (playerOnline("aurora")) setHasMap("aurora");
            else if (playerOnline("nova")) setHasMap("nova");
            else setHasMap(null);
        }, 15, 10, TimeUnit.SECONDS); // Give enough time for Dynmap & Vercel to update.
    }

    public void setHasMap(String map) {
        if (map == null) {
            Messaging.sendDebugMessage("Player not found on any map.");
            stop();

            instance().mapName = "aurora";
            hasMap = false;
        }
        else {
            Messaging.sendDebugMessage("Player found on: " + map);

            hasMap = true;
            start();
        }
    }

    private void startTownless() {
        townlessRunning = true;
        ModConfig config = ModConfig.instance();

        service.scheduleAtFixedRate(() -> {
            if (townlessRunning && config.townless.enabled && shouldRun()) {
                Messaging.sendDebugMessage("Starting townless task.");
                EarthMCAPI.getTownless().thenAccept(townless -> {
                    instance().setTownlessResidents(townless);
                    Messaging.sendDebugMessage("Finished townless task.");
                });
            }
        }, 30, Math.max(config.api.intervals.townless, 30), TimeUnit.SECONDS);
    }

    private void startNearby() {
        nearbyRunning = true;
        final ModConfig config = ModConfig.instance();

        service.scheduleAtFixedRate(() -> {
            if (nearbyRunning && config.nearby.enabled && shouldRun()) {
                Messaging.sendDebugMessage("Starting nearby task.");
                EarthMCAPI.getNearby().thenAccept(nearby -> {
                    instance().setNearbyPlayers(nearby);
                    Messaging.sendDebugMessage("Finished nearby task.");
                });
            }
        }, 10, Math.max(config.api.intervals.nearby, 10), TimeUnit.SECONDS);
    }

    private void startNews() {
        newsRunning = true;
        final ModConfig config = ModConfig.instance();

        service.scheduleAtFixedRate(() -> {
            if (newsRunning && config.news.enabled && shouldRun()) {
                Messaging.sendDebugMessage("Starting news task.");
                EarthMCAPI.getNews().thenAccept(news -> {
                    instance().setNews(news);
                    Messaging.sendDebugMessage("Finished news task.");
                });
            }
        }, 10, Math.max(config.api.intervals.news, 10), TimeUnit.SECONDS);
    }

    private void startCacheCheck() {
        cacheCheckRunning = true;

        service.scheduleAtFixedRate(() -> {
            for (Cache<?> cache : CACHES)
                if (cache.needsUpdate())
                    cache.clearCache();
        }, 0, 5, TimeUnit.MINUTES);
    }

    private boolean shouldRun() {
        ModConfig config = ModConfig.instance();
        return config.general.enableMod && ModUtils.isConnectedToEMC();
    }
}
