package net.emc.emce.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.emc.emce.utils.MsgUtils;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import static net.emc.emce.EMCE.*;
import static net.emc.emce.utils.Timers.*;

public class InfoCommands {
    public static void registerTownInfoCommand() {
        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("towninfo").then(
            ClientCommandManager.argument("townName", StringArgumentType.string()).executes(c -> {
                String townName = StringArgumentType.getString(c, "townName");
                JsonObject townObject = new JsonObject();
                JsonArray towns = allTowns;

                for (int i  = 0; i < towns.size(); i++) {
                    JsonObject town = (JsonObject) towns.get(i);

                    if (town.get("name").getAsString().toLowerCase().equals(townName.toLowerCase())) {
                        townObject = town;
                        break;
                    }
                }

                FabricClientCommandSource source = c.getSource();
                if (!townObject.has("name")) 
                    source.sendFeedback(new TranslatableText("text_towninfo_err", townName).formatted(Formatting.RED));
                else {
                    Formatting townInfoTextColour = Formatting.byName(config.commands.townInfoTextColour);

                    source.sendFeedback(new TranslatableText("text_towninfo_header", townObject.get("name").getAsString()).formatted(townInfoTextColour));
                    source.sendFeedback(new TranslatableText("text_towninfo_mayor", townObject.get("mayor").getAsString()).formatted(townInfoTextColour));
                    source.sendFeedback(new TranslatableText("text_towninfo_area", townObject.get("area").getAsString()).formatted(townInfoTextColour));
                    source.sendFeedback(new TranslatableText("text_shared_residents", townObject.get("residents").getAsJsonArray().size()).formatted(townInfoTextColour));
                    source.sendFeedback(new TranslatableText("text_towninfo_location", townObject.get("x").getAsString(), townObject.get("z").getAsString()).formatted(townInfoTextColour));
                }

                return 1;
            })
        ).executes(c -> {
            FabricClientCommandSource source = c.getSource();
            restartTimer(residentInfoTimer); // Makes sure clientTownName isn't delayed.

            if (clientTownName.equals(""))
                MsgUtils.sendPlayer("text_shared_notregistered", false, Formatting.RED, true);
            else {
                JsonObject townObject = new JsonObject();
                JsonArray towns = allTowns;

                for (int i = 0; i < towns.size(); i++) {
                    JsonObject town = (JsonObject) towns.get(i);
                    if (town.get("name").getAsString().toLowerCase().equals(clientTownName.toLowerCase())) {
                        townObject = town;
                        break;
                    }
                }

                if (!townObject.has("name")) 
                    source.sendFeedback(new TranslatableText("text_towninfo_err", clientTownName).formatted(Formatting.RED));
                else {
                    Formatting townInfoTextColour = Formatting.byName(config.commands.townInfoTextColour);

                    source.sendFeedback(new TranslatableText("text_towninfo_header", townObject.get("name").getAsString()).formatted(townInfoTextColour));
                    source.sendFeedback(new TranslatableText("text_towninfo_mayor", townObject.get("mayor").getAsString()).formatted(townInfoTextColour));
                    source.sendFeedback(new TranslatableText("text_towninfo_area", townObject.get("area").getAsString()).formatted(townInfoTextColour));
                    source.sendFeedback(new TranslatableText("text_shared_residents", townObject.get("residents").getAsJsonArray().size()).formatted(townInfoTextColour));
                    source.sendFeedback(new TranslatableText("text_towninfo_location", townObject.get("x").getAsString(), townObject.get("z").getAsString()).formatted(townInfoTextColour));
                }
            }

            return 1;
        }));
    }

    public static void registerNationInfoCommand() {
        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("nationinfo").then(
            ClientCommandManager.argument("nationName", StringArgumentType.string()).executes(c -> {
            String nationName = StringArgumentType.getString(c, "nationName");
            JsonObject nationObject = new JsonObject();

            for (int i = 0; i < allNations.size(); i++) {
                JsonObject nation = (JsonObject) allNations.get(i);

                if (nation.get("name").getAsString().toLowerCase().equals(nationName.toLowerCase())) {
                    nationObject = nation;
                    break;
                }
            }

            FabricClientCommandSource source = c.getSource();
            if (!nationObject.has("name"))
                MsgUtils.sendPlayer("text_nationinfo_err", false, Formatting.RED, true);
            else {
                Formatting nationInfoTextColour = Formatting.byName(config.commands.nationInfoTextColour);

                source.sendFeedback(new TranslatableText("text_nationinfo_header", nationObject.get("name").getAsString()).formatted(nationInfoTextColour));
                source.sendFeedback(new TranslatableText("text_nationinfo_king", nationObject.get("king").getAsString()).formatted(nationInfoTextColour));
                source.sendFeedback(new TranslatableText("text_nationinfo_capital", nationObject.get("capitalName").getAsString()).formatted(nationInfoTextColour));
                source.sendFeedback(new TranslatableText("text_nationinfo_area", nationObject.get("area").getAsString()).formatted(nationInfoTextColour));
                source.sendFeedback(new TranslatableText("text_shared_residents", nationObject.get("residents").getAsJsonArray().size()).formatted(nationInfoTextColour));
                source.sendFeedback(new TranslatableText("text_nationinfo_towns", nationObject.get("towns").getAsJsonArray().size()).formatted(nationInfoTextColour));
            }

            return 1;
        })).executes(c -> {
            FabricClientCommandSource source = c.getSource();
            restartTimer(residentInfoTimer); // Makes sure clientNationName isn't delayed.

            if (clientNationName.equals(""))
                MsgUtils.sendPlayer("text_shared_notregistered", false, Formatting.RED, true, clientName);
            else {
                JsonObject nationObject = new JsonObject();
                JsonArray nations = allNations;
                
                for (int i = 0; i < nations.size(); i++) {
                    JsonObject nation = (JsonObject) nations.get(i);
                    if (nation.get("name").getAsString().toLowerCase().equals(clientNationName.toLowerCase())) {
                        nationObject = nation;
                        break;
                    }
                }

                if (!nationObject.has("name"))
                    source.sendFeedback(new TranslatableText("text_nationinfo_err", clientNationName).formatted(Formatting.RED));
                else {
                    Formatting nationInfoTextColour = Formatting.byName(config.commands.nationInfoTextColour);

                    source.sendFeedback(new TranslatableText("text_nationinfo_header", nationObject.get("name").getAsString()).formatted(nationInfoTextColour));
                    source.sendFeedback(new TranslatableText("text_nationinfo_king", nationObject.get("king").getAsString()).formatted(nationInfoTextColour));
                    source.sendFeedback(new TranslatableText("text_nationinfo_capital", nationObject.get("capitalName").getAsString()).formatted(nationInfoTextColour));
                    source.sendFeedback(new TranslatableText("text_nationinfo_area", nationObject.get("area").getAsString()).formatted(nationInfoTextColour));
                    source.sendFeedback(new TranslatableText("text_shared_residents", nationObject.get("residents").getAsJsonArray().size()).formatted(nationInfoTextColour));
                    source.sendFeedback(new TranslatableText("text_nationinfo_towns", nationObject.get("towns").getAsJsonArray().size()).formatted(nationInfoTextColour));
                }
            }

            return 1;
        }));
    }
}