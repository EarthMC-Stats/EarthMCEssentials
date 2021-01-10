package net.earthmc.emc.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import static net.earthmc.emc.EMCMod.*;
import static net.earthmc.emc.utils.Timers.*;

public class NearbyCommand
{
    public static void register(CommandDispatcher<CottonClientCommandSource> dispatcher)
    {
        dispatcher.register(ArgumentBuilders.literal("nearby").executes(c ->
        {
            if (client.player == null) return -1;

            Formatting headingFormatting = Formatting.byName(config.nearby.headingTextColour);
            Formatting textFormatting = Formatting.byName(config.nearby.playerTextColour);

            c.getSource().sendFeedback(new TranslatableText("text_nearby_header", nearby.size()).formatted(headingFormatting));

            for (int i = 0; i < nearby.size(); i++)
            {
                JsonObject currentPlayer = (JsonObject) nearby.get(i);
                int distance = Math.abs(currentPlayer.get("x").getAsInt() - (int) client.player.getX()) +
                               Math.abs(currentPlayer.get("z").getAsInt() - (int) client.player.getZ());

                c.getSource().sendFeedback(new TranslatableText(currentPlayer.get("name").getAsString() + ": " + distance + "m").formatted(textFormatting));
            }

            return 1;
        }).then(ArgumentBuilders.literal("refresh").executes(c ->
        {
            restart(nearbyTimer);
            c.getSource().sendFeedback(new TranslatableText("msg_nearby_refresh"));

            return 1;
        })).then(ArgumentBuilders.literal("clear").executes(c ->
        {
            nearby = new JsonArray();
            c.getSource().sendFeedback(new TranslatableText("msg_nearby_clear"));

            return 1;
        })));
    }
}
