package net.earthmc.emc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.earthmc.emc.utils.EmcApi;
import net.earthmc.emc.utils.ModUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class EMCMod implements ModInitializer
{
    public static JsonArray townless;
    public static JsonArray nearby;

    public static JsonObject townInfo;
    public static JsonObject nationInfo;

    int townlessPlayerOffset;
    int nearbyPlayerOffset;

    public static String[] colors;
    public static String clientName = "";
    public static String clientTownName = "";
    public static String clientNationName = "";

    public static boolean timersActivated;

    public static MinecraftClient client;
    public static Screen screen;
    public static ModConfig config;
    KeyBinding configKeybind;

    @Override
    public void onInitialize() // Called when Minecraft starts.
    {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        configKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding("Open Config Menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F4, "EarthMC Essentials"));

        colors = new String[] { "BLUE", "DARK_BLUE", "GREEN", "DARK_GREEN", "AQUA", "DARK_AQUA", "RED", "DARK_RED",
                "LIGHT_PURPLE", "DARK_PURPLE", "YELLOW", "GOLD", "GRAY", "DARK_GRAY", "BLACK", "WHITE" };

        townless = EmcApi.getTownless();
        nationInfo = new JsonObject();
        townInfo = new JsonObject();
        nearby = new JsonArray(); // 'new' because the client cant be near anyone yet.

        //#region ClientTickEvents
        ClientTickEvents.END_CLIENT_TICK.register(client ->
        {
            // Pressed F4 (Config Menu)
            if (configKeybind.wasPressed())
            {
                screen = ModMenuIntegration.getConfigBuilder().build();

                client.openScreen(screen);
		    }
        });
        //#endregion

        //#region HudRenderCallback
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) ->
        {
            if (!config.general.enableMod) return;

            final TextRenderer renderer = client.textRenderer;

            if (config.townless.enabled)
            {
                if (!config.townless.presetPositions)
                {
                    // Position of the first player, who determines where the list will be.
                    townlessPlayerOffset = config.townless.yPos;

                    Formatting townlessTextFormatting = Formatting.byName(config.townless.headingTextColour);
                    MutableText townlessText = new TranslatableText("Townless Players [" + townless.size() + "]").formatted(townlessTextFormatting);

                    // Draw heading.
                    renderer.drawWithShadow(matrixStack, townlessText, config.townless.xPos, config.townless.yPos - 15, 16777215);

                    if (townless.size() >= 1)
                    {
                        for (int i = 0; i < townless.size(); i++)
                        {
                            Formatting playerTextFormatting = Formatting.byName(config.townless.playerTextColour);

                            if (config.townless.maxLength >= 1)
                            {
                                if (i >= config.townless.maxLength)
                                {
                                    MutableText remainingText = new TranslatableText("And " + (townless.size()-i) + " more...").formatted(playerTextFormatting);
                                    renderer.drawWithShadow(matrixStack, remainingText, config.townless.xPos, townlessPlayerOffset, 16777215);
                                    break;
                                }
                            }

                            final JsonObject currentPlayer = (JsonObject) townless.get(i);
                            MutableText playerName = new TranslatableText(currentPlayer.get("name").getAsString()).formatted(playerTextFormatting);

                            renderer.drawWithShadow(matrixStack, playerName, config.townless.xPos, townlessPlayerOffset, 16777215);

                            // Add offset for the next player.
                            townlessPlayerOffset += 10;
                        }
                    }
                }
                else // No advanced positioning, use preset states.
                {
                    int startingPositionX;
                    int startingPositionY;

                    switch(config.townless.positionState.getName())
                    {
                        case "TOP_MIDDLE":
                        {
                            if (ModUtils.getStringWidth("Townless Players [" + townless.size() + "]") > ModUtils.getLongestElement(townless)) startingPositionX = ModUtils.getWindowWidth()/2-ModUtils.getStringWidth("Townless Players [" + townless.size() + "]")/2;
                            else startingPositionX = ModUtils.getWindowWidth()/2-ModUtils.getLongestElement(townless)/2;
                            startingPositionY = 16;
                            break;
                        }
                        case "TOP_RIGHT":
                        {
                            if (ModUtils.getStringWidth("Townless Players [" + townless.size() + "]") > ModUtils.getLongestElement(townless)) startingPositionX = ModUtils.getWindowWidth()-ModUtils.getStringWidth("Townless Players [" + townless.size() + "]")-5;
                            else startingPositionX = ModUtils.getWindowWidth()-ModUtils.getLongestElement(townless)-5;
                            
                            if (EMCMod.client.player.getStatusEffects().isEmpty()) startingPositionY = 16;
                            else startingPositionY = 38;
                            break;
                        }
                        case "LEFT":
                        {
                            startingPositionX = 5;
                            startingPositionY = ModUtils.getWindowHeight()/2-ModUtils.getTownlessArrayHeigth(townless, config.townless.maxLength)/2;
                            break;
                        }
                        case "RIGHT":
                        {
                            if (ModUtils.getStringWidth("Townless Players [" + townless.size() + "]") > ModUtils.getLongestElement(townless)) startingPositionX = ModUtils.getWindowWidth()-ModUtils.getStringWidth("Townless Players [" + townless.size() + "]")-5;
                            else startingPositionX = ModUtils.getWindowWidth()-ModUtils.getLongestElement(townless)-5;
                            startingPositionY = ModUtils.getWindowHeight()/2-ModUtils.getTownlessArrayHeigth(townless, config.townless.maxLength)/2;
                            break;
                        }
                        case "BOTTOM_RIGHT":
                        {
                            if (ModUtils.getStringWidth("Townless Players [" + townless.size() + "]") > ModUtils.getLongestElement(townless)) startingPositionX = ModUtils.getWindowWidth()-ModUtils.getStringWidth("Townless Players [" + townless.size() + "]")-5;
                            else startingPositionX = ModUtils.getWindowWidth()-ModUtils.getLongestElement(townless)-5;
                            startingPositionY = ModUtils.getWindowHeight()-ModUtils.getTownlessArrayHeigth(townless, config.townless.maxLength)-10;
                            break;
                        }
                        case "BOTTOM_LEFT":
                        {
                            startingPositionX = 5;
                            startingPositionY = ModUtils.getWindowHeight()-ModUtils.getTownlessArrayHeigth(townless, config.townless.maxLength)-10;
                            break;
                        }
                        default: //Defaults to top left
                        {
                            startingPositionX = 5;
                            startingPositionY = 16;
                            break;
                        }
                    }

                    Formatting townlessTextFormatting = Formatting.byName(config.townless.headingTextColour);
                    MutableText townlessText = new TranslatableText("Townless Players [" + townless.size() + "]").formatted(townlessTextFormatting);

                    // Draw heading.
                    renderer.drawWithShadow(matrixStack, townlessText, startingPositionX, startingPositionY-10, 16777215);

                    if (townless.size() >= 1)
                    {
                        for (int i = 0; i < townless.size(); i++)
                        {
                            Formatting playerTextFormatting = Formatting.byName(config.townless.playerTextColour);

                            if (config.townless.maxLength >= 1)
                            {
                                if (i >= config.townless.maxLength)
                                {
                                    MutableText remainingText = new TranslatableText("And " + (townless.size()-i) + " more...").formatted(playerTextFormatting);
                                    renderer.drawWithShadow(matrixStack, remainingText, startingPositionX, startingPositionY + i*10, 16777215);
                                    break;
                                }
                            }

                            final JsonObject currentPlayer = (JsonObject) townless.get(i);
                            MutableText playerName = new TranslatableText(currentPlayer.get("name").getAsString()).formatted(playerTextFormatting);

                            renderer.drawWithShadow(matrixStack, playerName, startingPositionX, startingPositionY + i*10, 16777215);
                        }
                    }
                }
            }

            if (config.nearby.enabled)
            {
                if (!config.nearby.presetPositions) //Not using preset positions
                {
                    // Position of the first player, who determines where the list will be.
                    nearbyPlayerOffset = config.nearby.yPos;

                    Formatting nearbyTextFormatting = Formatting.byName(config.nearby.headingTextColour);
                    MutableText nearbyText = new TranslatableText("Nearby Players [" + nearby.size() + "]").formatted(nearbyTextFormatting);

                    // Draw heading.
                    renderer.drawWithShadow(matrixStack, nearbyText, config.nearby.xPos, config.nearby.yPos - 15, 16777215);

                    if (nearby.size() >= 1)
                    {
                        for (int i = 0; i < nearby.size(); i++)
                        {
                            final JsonObject currentPlayer = (JsonObject) nearby.get(i);

                            final int playerX = currentPlayer.get("x").getAsInt();
                            final int playerY = currentPlayer.get("y").getAsInt();
                            final int playerZ = currentPlayer.get("z").getAsInt();

                            if (currentPlayer.get("name").getAsString().equals(clientName)) continue;

                            Formatting playerTextFormatting = Formatting.byName(config.nearby.playerTextColour);
                            MutableText playerText = new TranslatableText(currentPlayer.get("name").getAsString() + ": " + playerX + ", " + playerY + ", " + playerZ).formatted(playerTextFormatting);

                            renderer.drawWithShadow(matrixStack, playerText, config.nearby.xPos, nearbyPlayerOffset, 16777215);

                            // Add offset for the next player.
                            nearbyPlayerOffset += 10;
                        }
                    }
                }
                else
                {
                    int startingPositionX;
                    int startingPositionY;

                    switch(config.nearby.positionState.getName())
                    {
                        case "TOP_MIDDLE":
                        {
                            if (ModUtils.getStringWidth("Nearby Players [" + nearby.size() + "]") > ModUtils.getNearbyLongestElement(nearby)) startingPositionX = ModUtils.getWindowWidth()/2-ModUtils.getStringWidth("Nearby Players [" + nearby.size() + "]")/2;
                            else startingPositionX = ModUtils.getWindowWidth()/2-ModUtils.getNearbyLongestElement(nearby)/2;
                            startingPositionY = 16;
                            break;
                        }
                        case "TOP_RIGHT":
                        {
                            if (ModUtils.getStringWidth("Nearby Players [" + nearby.size() + "]") > ModUtils.getNearbyLongestElement(nearby)) startingPositionX = ModUtils.getWindowWidth()-ModUtils.getStringWidth("Nearby Players [" + nearby.size() + "]")-5;
                            else startingPositionX = ModUtils.getWindowWidth()-ModUtils.getNearbyLongestElement(nearby)-5;
                            
                            if (EMCMod.client.player.getStatusEffects().isEmpty()) startingPositionY = 16;
                            else startingPositionY = 38;
                    
                            break;
                        }
                        case "LEFT":
                        {
                            startingPositionX = 5;
                            startingPositionY = ModUtils.getWindowHeight()/2-ModUtils.getArrayHeight(nearby)/2;
                            break;
                        }
                        case "RIGHT":
                        {
                            if (ModUtils.getStringWidth("Nearby Players [" + nearby.size() + "]") > ModUtils.getNearbyLongestElement(nearby)) startingPositionX = ModUtils.getWindowWidth()-ModUtils.getStringWidth("Nearby Players [" + nearby.size() + "]")-5;
                            else startingPositionX = ModUtils.getWindowWidth()-ModUtils.getNearbyLongestElement(nearby)-5;
                            startingPositionY = ModUtils.getWindowHeight()/2-ModUtils.getArrayHeight(nearby)/2;
                            break;
                        }
                        case "BOTTOM_RIGHT":
                        {
                            if (ModUtils.getStringWidth("Nearby Players [" + nearby.size() + "]") > ModUtils.getNearbyLongestElement(nearby)) startingPositionX = ModUtils.getWindowWidth()-ModUtils.getStringWidth("Nearby Players [" + nearby.size() + "]")-5;
                            else startingPositionX = ModUtils.getWindowWidth()-ModUtils.getNearbyLongestElement(nearby)-5;
                            startingPositionY = ModUtils.getWindowHeight()-ModUtils.getArrayHeight(nearby)-10;
                            break;
                        }
                        case "BOTTOM_LEFT":
                        {
                            startingPositionX = 5;
                            startingPositionY = ModUtils.getWindowHeight()-ModUtils.getArrayHeight(nearby)-10;
                            break;
                        }
                        default: //Defaults to top left
                        {
                            startingPositionX = 5;
                            startingPositionY = 16;
                            break;
                        }
                    }

                    Formatting nearbyTextFormatting = Formatting.byName(config.nearby.headingTextColour);
                    MutableText nearbyText = new TranslatableText("Nearby Players [" + nearby.size() + "]").formatted(nearbyTextFormatting);

                    // Draw heading.
                    renderer.drawWithShadow(matrixStack, nearbyText, startingPositionX, startingPositionY-10, 16777215);

                    if (nearby.size() >= 1)
                    {
                        for (int i = 0; i < nearby.size(); i++)
                        {
                            final JsonObject currentPlayer = (JsonObject) nearby.get(i);

                            final int playerX = currentPlayer.get("x").getAsInt();
                            final int playerY = currentPlayer.get("y").getAsInt();
                            final int playerZ = currentPlayer.get("z").getAsInt();

                            if (currentPlayer.get("name").getAsString().equals(clientName)) continue;

                            Formatting playerTextFormatting = Formatting.byName(config.nearby.playerTextColour);
                            MutableText playerText = new TranslatableText(currentPlayer.get("name").getAsString() + ": " + playerX + ", " + playerY + ", " + playerZ).formatted(playerTextFormatting);

                            renderer.drawWithShadow(matrixStack, playerText, startingPositionX, startingPositionY + 10*i, 16777215);
                        }
                    }
                }
            }

            // Town info is enabled and object isn't empty.
            if (config.townInfo.enabled && !townInfo.entrySet().isEmpty())
            {
                Formatting townInfoHeadingFormatting = Formatting.byName(config.townInfo.headingTextColour);
                Formatting infoTextFormatting = Formatting.byName(config.townInfo.infoTextColour);

                // Draw heading.
                MutableText townInfoText = new TranslatableText("Town Information - " + clientTownName).formatted(townInfoHeadingFormatting);
                renderer.drawWithShadow(matrixStack, townInfoText, config.townInfo.xPos, config.townInfo.yPos - 5, 16777215);

                // Draw info.
                MutableText mayorText = new TranslatableText("Mayor: " + townInfo.get("mayor").getAsString()).formatted(infoTextFormatting);
                if (townInfo.has("mayor")) renderer.drawWithShadow(matrixStack, mayorText, config.townInfo.xPos, config.townInfo.yPos + 10, 16777215);

                MutableText areaText = new TranslatableText("Area/Chunks: " + townInfo.get("area").getAsString()).formatted(infoTextFormatting);
                if (townInfo.has("area")) renderer.drawWithShadow(matrixStack, areaText, config.townInfo.xPos, config.townInfo.yPos + 20, 16777215);

                MutableText residentsText = new TranslatableText("Residents: " + townInfo.get("residents").getAsJsonArray().size()).formatted(infoTextFormatting);
                if (townInfo.has("residents")) renderer.drawWithShadow(matrixStack, residentsText, config.townInfo.xPos, config.townInfo.yPos + 30, 16777215);

                MutableText locationText = new TranslatableText("Location: " + townInfo.get("x").getAsString() + ", " + townInfo.get("z").getAsString()).formatted(infoTextFormatting);
                if (townInfo.has("x") && townInfo.has("z")) renderer.drawWithShadow(matrixStack, locationText, config.townInfo.xPos, config.townInfo.yPos + 40, 16777215);
            }

            // Nation info is enabled and object isn't empty.
            if (config.nationInfo.enabled && !nationInfo.entrySet().isEmpty())
            {
                Formatting nationInfoHeadingFormatting = Formatting.byName(config.nationInfo.headingTextColour);
                Formatting nationInfoTextFormatting = Formatting.byName(config.nationInfo.infoTextColour);

                // Draw heading.
                MutableText nationInfoText = new TranslatableText("Nation Information - " + clientNationName).formatted(nationInfoHeadingFormatting);
                renderer.drawWithShadow(matrixStack, nationInfoText, config.nationInfo.xPos, config.nationInfo.yPos - 5, 16777215);

                // Draw info.
                MutableText kingText = new TranslatableText("King: " + nationInfo.get("king").getAsString()).formatted(nationInfoTextFormatting);
                if (nationInfo.has("king")) renderer.drawWithShadow(matrixStack, kingText, config.nationInfo.xPos, config.nationInfo.yPos + 10, 16777215);

                MutableText capitalText = new TranslatableText("Capital: " + nationInfo.get("capitalName").getAsString()).formatted(nationInfoTextFormatting);
                if (nationInfo.has("capitalName")) renderer.drawWithShadow(matrixStack, capitalText, config.nationInfo.xPos, config.nationInfo.yPos + 20, 16777215);

                MutableText areaText = new TranslatableText("Area/Chunks: " + nationInfo.get("area").getAsString()).formatted(nationInfoTextFormatting);
                if (nationInfo.has("area")) renderer.drawWithShadow(matrixStack, areaText, config.nationInfo.xPos, config.nationInfo.yPos + 30, 16777215);

                MutableText residentsText = new TranslatableText("Residents: " + nationInfo.get("residents").getAsJsonArray().size()).formatted(nationInfoTextFormatting);
                if (nationInfo.has("residents")) renderer.drawWithShadow(matrixStack, residentsText, config.nationInfo.xPos, config.nationInfo.yPos + 40, 16777215);

                MutableText townsText = new TranslatableText("Towns: " + nationInfo.get("towns").getAsJsonArray().size()).formatted(nationInfoTextFormatting);
                if (nationInfo.has("towns")) renderer.drawWithShadow(matrixStack, townsText, config.nationInfo.xPos, config.nationInfo.yPos + 50, 16777215);
            }
        });
        //#endregion
    }
}