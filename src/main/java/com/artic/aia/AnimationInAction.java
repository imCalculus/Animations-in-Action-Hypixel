package com.artic.aia;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class AnimationInAction implements ClientModInitializer {
   public static final String MODID = "animationinaction";
   public static final String NAME = "Animation in Action";
   public static final String VERSION = "1.0.0";
   public static KeyMapping openKey;
   public static volatile boolean openGuiNextTick;

   @Override
   public void onInitializeClient() {
      AIAConfig.init(FabricLoader.getInstance().getConfigDir().toFile());
      DropCatalog.init();
      Gfx.init();
      KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MODID, "general"));
      openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.animationinaction.open", InputConstants.Type.KEYBOARD, 344, category));
      PictureInPictureRendererRegistry.register(ctx -> new Visual.SpinItemRenderer());
      ChatDetector detector = new ChatDetector();
      ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
         if (!overlay) {
            detector.onMessage(message);
         }
      });
      ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> detector.onMessage(message));
      ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> AiaCommand.register(dispatcher));
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         while (openKey.consumeClick()) {
            openGuiNextTick = true;
         }

         if (openGuiNextTick) {
            openGuiNextTick = false;
            if (!(client.gui.screen() instanceof ConfigScreen)) {
               client.gui.setScreen(new ConfigScreen());
            }
         }

         AnimationManager.INSTANCE.tick(client);
      });
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MODID, "reveal_overlay"), (drawContext, tickCounter) -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc.gui.screen() == null || !AIAConfig.aboveGui) {
            AnimationManager.INSTANCE.draw(drawContext, drawContext.guiWidth(), drawContext.guiHeight());
         }
      });
      ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
         ScreenEvents.afterExtract(screen).register((s, drawContext, mouseX, mouseY, tickDelta) -> {
            if (AIAConfig.aboveGui) {
               AnimationManager.INSTANCE.draw(drawContext, drawContext.guiWidth(), drawContext.guiHeight());
            }
         });
      });
   }
}
