package net.emc.emce.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.emc.emce.EarthMCEssentials;
import net.emc.emce.caches.NationDataCache;
import net.emc.emce.caches.TownDataCache;
import net.emc.emce.utils.MsgUtils;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Locale;

public class InfoCommands {
    public static void registerTownInfoCommand() {
        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("towninfo").then(
            ClientCommandManager.argument("townName", StringArgumentType.string()).executes(c -> {
                String townName = StringArgumentType.getString(c, "townName");

                TownDataCache.INSTANCE.getCache().thenAccept(towns -> {
                    JsonObject townObject = towns.get(townName.toLowerCase(Locale.ROOT));

                    if (townObject == null)
                        MsgUtils.sendPlayer("text_towninfo_err", false, Formatting.RED, true, townName);
                    else {
                        sendTownInfo(townObject, c.getSource());
                    }
                });

                return 1;
            })
        ).executes(c -> {
            FabricClientCommandSource source = c.getSource();

            if (EarthMCEssentials.instance().getClientResident() == null) {
                MsgUtils.sendPlayer("text_shared_notregistered", false, Formatting.RED, true, MinecraftClient.getInstance().player.getName());
                return 1;
            }

            if (EarthMCEssentials.instance().getClientResident().getTown().equals("") || EarthMCEssentials.instance().getClientResident().getTown().equals("No Town"))
                MsgUtils.sendPlayer("text_towninfo_not_registered", false, Formatting.RED, true);
            else {
                TownDataCache.INSTANCE.getCache().thenAccept(towns -> {
                    JsonObject townObject = towns.get(EarthMCEssentials.instance().getClientResident().getTown().toLowerCase(Locale.ROOT));

                    if (townObject == null)
                        source.sendFeedback(new TranslatableText("text_towninfo_err", EarthMCEssentials.instance().getClientResident().getTown()).formatted(Formatting.RED));
                    else {
                        sendTownInfo(townObject, source);
                    }
                });
            }

            return 1;
        }));
    }

    public static void registerNationInfoCommand() {
        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("nationinfo").then(
            ClientCommandManager.argument("nationName", StringArgumentType.string()).executes(c -> {
            String nationName = StringArgumentType.getString(c, "nationName");

            NationDataCache.INSTANCE.getCache().thenAccept(nations -> {
                JsonObject nationObject = nations.get(nationName.toLowerCase(Locale.ROOT));

                if (nationObject == null)
                    MsgUtils.sendPlayer("text_nationinfo_err", false, Formatting.RED, true, nationName);
                else {
                    sendNationInfo(nationObject, c.getSource());
                }
            });

            return 1;
        })).executes(c -> {
            FabricClientCommandSource source = c.getSource();

            if (EarthMCEssentials.instance().getClientResident() == null) {
                MsgUtils.sendPlayer("text_shared_notregistered", false, Formatting.RED, true, MinecraftClient.getInstance().player.getName());
                return 1;
            }

            if (EarthMCEssentials.instance().getClientResident().getNation().equals("") || EarthMCEssentials.instance().getClientResident().getNation().equals("No Nation"))
                MsgUtils.sendPlayer("text_nationinfo_not_registered", false, Formatting.RED, true);
            else {
                NationDataCache.INSTANCE.getCache().thenAccept(nations -> {
                    JsonObject nationObject = nations.get(EarthMCEssentials.instance().getClientResident().getNation().toLowerCase(Locale.ROOT));

                    if (nationObject == null)
                        MsgUtils.sendPlayer("text_nationinfo_err", false, Formatting.RED, true, EarthMCEssentials.instance().getClientResident().getNation());
                    else {
                        sendNationInfo(nationObject, source);
                    }
                });
            }

            return 1;
        }));
    }

    private static void sendTownInfo(JsonObject townObject, FabricClientCommandSource source) {
        Formatting townInfoTextColour = Formatting.byName(EarthMCEssentials.instance().getConfig().commands.townInfoTextColour.name());

        source.sendFeedback(new TranslatableText("text_towninfo_header", townObject.get("name").getAsString()).formatted(townInfoTextColour));
        source.sendFeedback(new TranslatableText("text_towninfo_mayor", townObject.get("mayor").getAsString()).formatted(townInfoTextColour));
        source.sendFeedback(new TranslatableText("text_towninfo_area", townObject.get("area").getAsString()).formatted(townInfoTextColour));
        source.sendFeedback(new TranslatableText("text_shared_residents", townObject.get("residents").getAsJsonArray().size()).formatted(townInfoTextColour));
        source.sendFeedback(new TranslatableText("text_towninfo_location", townObject.get("x").getAsString(), townObject.get("z").getAsString()).formatted(townInfoTextColour));
    }

    private static void sendNationInfo(JsonObject nationObject, FabricClientCommandSource source) {
        Formatting nationInfoTextColour = Formatting.byName(EarthMCEssentials.instance().getConfig().commands.nationInfoTextColour.name());

        source.sendFeedback(new TranslatableText("text_nationinfo_header", nationObject.get("name").getAsString()).formatted(nationInfoTextColour));
        source.sendFeedback(new TranslatableText("text_nationinfo_king", nationObject.get("king").getAsString()).formatted(nationInfoTextColour));
        source.sendFeedback(new TranslatableText("text_nationinfo_capital", nationObject.get("capitalName").getAsString()).formatted(nationInfoTextColour));
        source.sendFeedback(new TranslatableText("text_nationinfo_area", nationObject.get("area").getAsString()).formatted(nationInfoTextColour));
        source.sendFeedback(new TranslatableText("text_shared_residents", nationObject.get("residents").getAsJsonArray().size()).formatted(nationInfoTextColour));
        source.sendFeedback(new TranslatableText("text_nationinfo_towns", nationObject.get("towns").getAsJsonArray().size()).formatted(nationInfoTextColour));
    }
}