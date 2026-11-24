package com.artic.aia;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class SoundPlayer {
   private static final Identifier RARE_DROP = Identifier.fromNamespaceAndPath("animationinaction", "rare_drop");
   private static final Identifier FALLBACK = Identifier.fromNamespaceAndPath("minecraft", "entity.player.levelup");
   public static volatile String lastStatus = "not played yet";

   private SoundPlayer() {
   }

   public static void play(Rarity rarity) {
      if (AIAConfig.sound && AIAConfig.volume > 0.0F) {
         Minecraft mc = Minecraft.getInstance();
         if (mc == null) {
            return;
         }
         float vol = AIAConfig.volume;
         mc.execute(() -> {
            try {
               SoundManager sm = mc.getSoundManager();
               if (sm.getSoundEvent(RARE_DROP) == null) {
                  lastStatus = "animationinaction:rare_drop is NOT registered, played vanilla fallback";
                  AIAConfig.log("sound: " + lastStatus);
                  sm.play(SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(FALLBACK), 1.0F, 1.0F));
                  return;
               }

               sm.play(SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(RARE_DROP), 1.0F, vol));
               lastStatus = "sent to the sound engine (volume " + vol + ")";
               AIAConfig.log("sound: " + lastStatus);
            } catch (Throwable t) {
               lastStatus = "FAILED: " + t;
               AIAConfig.log("sound failed: " + t);

               try {
                  mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(FALLBACK), 1.0F, 1.0F));
               } catch (Throwable ignored) {
               }
            }
         });
      } else {
         lastStatus = "sound is off in the config (or volume is 0)";
      }
   }
}
