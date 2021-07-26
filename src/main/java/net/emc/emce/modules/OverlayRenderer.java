package net.emc.emce.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.emc.emce.EarthMCEssentials;
import net.emc.emce.config.ModConfig;
import net.emc.emce.utils.ModUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class OverlayRenderer {
    public static void render(MatrixStack matrixStack) {
        if (!EarthMCEssentials.getConfig().general.enableMod || !EarthMCEssentials.shouldRender())
            return;

        final TextRenderer renderer = EarthMCEssentials.getClient().textRenderer;

        ModConfig config = EarthMCEssentials.getConfig();

        ModUtils.State townlessState = config.townless.positionState;
        ModUtils.State nearbyState = config.nearby.positionState;

        JsonArray townless = EarthMCEssentials.getTownless();
        JsonArray nearby = EarthMCEssentials.getNearbyPlayers();
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null)
            return;

        if (config.townless.enabled) {
            if (!config.townless.presetPositions) {
                // Position of the first player, who determines where the list will be.
                int townlessPlayerOffset = config.townless.yPos;

                Formatting townlessTextFormatting = Formatting.byName(config.townless.headingTextColour);
                MutableText townlessText = new TranslatableText("text_townless_header", townless.size()).formatted(townlessTextFormatting);

                // Draw heading.
                renderer.drawWithShadow(matrixStack, townlessText, config.townless.xPos, config.townless.yPos - 15, 16777215);

                if (EarthMCEssentials.getTownless().size() > 0) {
                    int index = 0;
                    for (int i = 0; i < townless.size(); i++) {
                        Formatting playerTextFormatting = Formatting.byName(config.townless.playerTextColour);

                        if (config.townless.maxLength >= 1) {
                            if (index >= config.townless.maxLength) {
                                MutableText remainingText = new TranslatableText("text_townless_remaining", EarthMCEssentials.getTownless().size()-index).formatted(playerTextFormatting);
                                renderer.drawWithShadow(matrixStack, remainingText, config.townless.xPos, townlessPlayerOffset, 16777215);
                                break;
                            }
                            index++;
                        }

                        MutableText playerName = new TranslatableText(townless.get(i).getAsJsonObject().get("name").getAsString()).formatted(playerTextFormatting);
                        renderer.drawWithShadow(matrixStack, playerName, config.townless.xPos, townlessPlayerOffset, 16777215);

                        // Add offset for the next player.
                        townlessPlayerOffset += 10;
                    }
                }
            } else { // No advanced positioning, use preset states.
                int townlessLongest, nearbyLongest;

                townlessLongest = Math.max(ModUtils.getLongestElement(townless), ModUtils.getTextWidth(new TranslatableText("text_townless_header", townless.size())));
                nearbyLongest = Math.max(ModUtils.getNearbyLongestElement(EarthMCEssentials.getNearbyPlayers()), ModUtils.getTextWidth(new TranslatableText("text_nearby_header", nearby.size())));

                switch(townlessState)
                {
                    case TOP_MIDDLE:
                    {
                        if (nearbyState.equals(ModUtils.State.TOP_MIDDLE))
                            townlessState.setX(ModUtils.getWindowWidth() / 2 - (townlessLongest + nearbyLongest) / 2 );
                        else
                            townlessState.setX(ModUtils.getWindowWidth() / 2 - townlessLongest / 2);

                        townlessState.setY(16);
                        break;
                    }
                    case TOP_RIGHT:
                    {
                        townlessState.setX(ModUtils.getWindowWidth() - townlessLongest - 5);
                        townlessState.setY(ModUtils.getStatusEffectOffset(client.player.getStatusEffects()));
                        break;
                    }
                    case LEFT:
                    {
                        townlessState.setX(5);
                        townlessState.setY(ModUtils.getWindowHeight() / 2 - ModUtils.getTownlessArrayHeight(townless, config.townless.maxLength) / 2);
                        break;
                    }
                    case RIGHT:
                    {
                        townlessState.setX(ModUtils.getWindowWidth() - townlessLongest - 5);
                        townlessState.setY(ModUtils.getWindowHeight() / 2 - ModUtils.getTownlessArrayHeight(townless, config.townless.maxLength) / 2);
                        break;
                    }
                    case BOTTOM_RIGHT:
                    {
                        townlessState.setX(ModUtils.getWindowWidth() - townlessLongest - 5);
                        townlessState.setY(ModUtils.getWindowHeight() - ModUtils.getTownlessArrayHeight(townless, config.townless.maxLength) - 22);
                        break;
                    }
                    case BOTTOM_LEFT:
                    {
                        townlessState.setX(5);
                        townlessState.setY(ModUtils.getWindowHeight() - ModUtils.getTownlessArrayHeight(townless, config.townless.maxLength) - 22);
                        break;
                    }
                    default: // Defaults to top left
                    {
                        townlessState.setX(5);
                        townlessState.setY(16);
                        break;
                    }
                }

                Formatting townlessTextFormatting = Formatting.byName(config.townless.headingTextColour);
                MutableText townlessText = new TranslatableText("text_townless_header", townless.size()).formatted(townlessTextFormatting);

                // Draw heading.
                renderer.drawWithShadow(matrixStack, townlessText, townlessState.getX(), townlessState.getY() - 10, 16777215);

                if (townless.size() >= 1)
                {
                    for (int i = 0; i < townless.size(); i++)
                    {
                        Formatting playerTextFormatting = Formatting.byName(config.townless.playerTextColour);

                        if (config.townless.maxLength >= 1)
                        {
                            if (i >= config.townless.maxLength)
                            {
                                MutableText remainingText = new TranslatableText("text_townless_remaining", townless.size()-i).formatted(playerTextFormatting);
                                renderer.drawWithShadow(matrixStack, remainingText, townlessState.getX(), townlessState.getY() + i*10, 16777215);
                                break;
                            }
                        }

                        MutableText playerName = new TranslatableText(townless.get(i).getAsJsonObject().get("name").getAsString()).formatted(playerTextFormatting);

                        renderer.drawWithShadow(matrixStack, playerName, townlessState.getX(), townlessState.getY() + i*10, 16777215);
                    }
                }
            }
        }

        if (config.nearby.enabled) {
            if (!config.nearby.presetPositions) // Not using preset positions
            {
                // Position of the first player, who determines where the list will be.
                int nearbyPlayerOffset = config.nearby.yPos;

                Formatting nearbyTextFormatting = Formatting.byName(config.nearby.headingTextColour);
                MutableText nearbyText = new TranslatableText("text_nearby_header", nearby.size()).formatted(nearbyTextFormatting);

                // Draw heading.
                renderer.drawWithShadow(matrixStack, nearbyText, config.nearby.xPos, config.nearby.yPos - 15, 16777215);

                if (nearby.size() >= 1) {
                    if (client.player == null) return;

                    for (int i = 0; i < nearby.size(); i++) {
                        JsonObject currentPlayer = nearby.get(i).getAsJsonObject();
                        int distance = Math.abs(currentPlayer.get("x").getAsInt() - (int) client.player.getX()) +
                                Math.abs(currentPlayer.get("z").getAsInt() - (int) client.player.getZ());

                        if (currentPlayer.get("name").getAsString().equals(EarthMCEssentials.getClientResident().getName()))
                            continue;

                        Formatting playerTextFormatting = Formatting.byName(config.nearby.playerTextColour);
                        MutableText playerText = new TranslatableText(currentPlayer.get("name").getAsString(), distance).formatted(playerTextFormatting);

                        renderer.drawWithShadow(matrixStack, playerText, config.nearby.xPos, nearbyPlayerOffset, 16777215);

                        // Add offset for the next player.
                        nearbyPlayerOffset += 10;
                    }
                }
            } else {
                int nearbyLongest, townlessLongest;

                nearbyLongest = Math.max(ModUtils.getNearbyLongestElement(EarthMCEssentials.getNearbyPlayers()), ModUtils.getTextWidth(new TranslatableText("text_nearby_header", nearby.size())));
                townlessLongest = Math.max(ModUtils.getLongestElement(townless), ModUtils.getTextWidth(new TranslatableText("text_townless_header", townless.size())));

                switch (nearbyState) {
                    case TOP_MIDDLE: {
                        if (townlessState.equals(ModUtils.State.TOP_MIDDLE)) {
                            nearbyState.setX(ModUtils.getWindowWidth() / 2 - (townlessLongest + nearbyLongest) / 2 + townlessLongest + 5);
                            nearbyState.setY(townlessState.getY());
                        } else {
                            nearbyState.setX(ModUtils.getWindowWidth() / 2 - nearbyLongest / 2);
                            nearbyState.setY(16);
                        }

                        break;
                    }
                    case TOP_RIGHT: {
                        if (townlessState.equals(ModUtils.State.TOP_RIGHT))
                            nearbyState.setX(ModUtils.getWindowWidth() - townlessLongest - nearbyLongest - 15);
                        else
                            nearbyState.setX(ModUtils.getWindowWidth() - nearbyLongest - 5);

                        if (client.player != null)
                            nearbyState.setY(ModUtils.getStatusEffectOffset(client.player.getStatusEffects()));

                        break;
                    }
                    case LEFT: {
                        if (townlessState.equals(ModUtils.State.LEFT)) {
                            nearbyState.setX(townlessLongest + 10);
                            nearbyState.setY(townlessState.getY());
                        } else {
                            nearbyState.setX(5);
                            nearbyState.setY(ModUtils.getWindowHeight() / 2 - ModUtils.getArrayHeight(nearby) / 2);
                        }

                        break;
                    }
                    case RIGHT: {
                        if (townlessState.equals(ModUtils.State.RIGHT)) {
                            nearbyState.setX(ModUtils.getWindowWidth() - townlessLongest - nearbyLongest - 15);
                            nearbyState.setY(townlessState.getY());
                        } else {
                            nearbyState.setX(ModUtils.getWindowWidth() - nearbyLongest - 5);
                            nearbyState.setY(ModUtils.getWindowHeight() / 2 - ModUtils.getArrayHeight(nearby) / 2);
                        }

                        break;
                    }
                    case BOTTOM_RIGHT: {
                        if (townlessState.equals(ModUtils.State.BOTTOM_RIGHT)) {
                            nearbyState.setX(ModUtils.getWindowWidth() - townlessLongest - nearbyLongest - 15);
                            nearbyState.setY(townlessState.getY());
                        } else {
                            nearbyState.setX(ModUtils.getWindowWidth() - nearbyLongest - 15);
                            nearbyState.setY(ModUtils.getWindowHeight() - ModUtils.getArrayHeight(nearby) - 10);
                        }

                        break;
                    }
                    case BOTTOM_LEFT: {
                        if (townlessState.equals(ModUtils.State.BOTTOM_LEFT)) {
                            nearbyState.setX(townlessLongest + 15);
                            nearbyState.setY(townlessState.getY());
                        } else {
                            nearbyState.setX(5);
                            nearbyState.setY(ModUtils.getWindowHeight() - ModUtils.getArrayHeight(nearby) - 10);
                        }

                        break;
                    }
                    default: // Defaults to top left
                    {
                        if (townlessState.equals(ModUtils.State.TOP_LEFT))
                            nearbyState.setX(townlessLongest + 15);
                        else
                            nearbyState.setX(5);

                        nearbyState.setY(16);

                        break;
                    }
                }

                Formatting nearbyTextFormatting = Formatting.byName(config.nearby.headingTextColour);
                MutableText nearbyText = new TranslatableText("text_nearby_header", nearby.size()).formatted(nearbyTextFormatting);

                // Draw heading.
                renderer.drawWithShadow(matrixStack, nearbyText, nearbyState.getX(), nearbyState.getY() - 10, 16777215);

                if (nearby.size() >= 1) {
                    if (client.player == null) return;

                    for (int i = 0; i < nearby.size(); i++) {
                        JsonObject currentPlayer = (JsonObject) nearby.get(i);
                        int distance = Math.abs(currentPlayer.get("x").getAsInt() - (int) client.player.getX()) +
                                Math.abs(currentPlayer.get("z").getAsInt() - (int) client.player.getZ());

                        if (currentPlayer.get("name").getAsString().equals(EarthMCEssentials.getClientResident().getName()))
                            continue;

                        String prefix = "";

                        if (config.nearby.showRank) {
                            if (!currentPlayer.has("town")) prefix = "(Townless) ";
                            else prefix = "(" + currentPlayer.get("rank").getAsString() + ") ";
                        }

                        Formatting playerTextFormatting = Formatting.byName(config.nearby.playerTextColour);
                        MutableText playerText = new TranslatableText(prefix + currentPlayer.get("name").getAsString() + ": " + distance + "m").formatted(playerTextFormatting);

                        renderer.drawWithShadow(matrixStack, playerText, nearbyState.getX(), nearbyState.getY() + 10 * i, 16777215);
                    }
                }
            }
        }
    }
}
