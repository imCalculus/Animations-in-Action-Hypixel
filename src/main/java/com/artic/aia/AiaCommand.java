package com.artic.aia;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class AiaCommand {
   private static final String USAGE = "/aia [test <id> [rarity] | sound | debug]";

   private AiaCommand() {
   }

   public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
      dispatcher.register(build("aia"));
      dispatcher.register(build("animationinaction"));
   }

   private static LiteralArgumentBuilder<FabricClientCommandSource> build(String name) {
      return ClientCommands.literal(name)
         .executes(ctx -> {
            AnimationInAction.openGuiNextTick = true;
            return 1;
         })
         .then(
            ClientCommands.literal("test")
               .executes(ctx -> {
                  test("warden_heart", null);
                  return 1;
               })
               .then(
                  ClientCommands.argument("id", StringArgumentType.word())
                     .suggests((ctx, builder) -> {
                        for (DropEntry d : DropCatalog.ALL) {
                           builder.suggest(d.id);
                        }

                        return builder.buildFuture();
                     })
                     .executes(ctx -> {
                        test(StringArgumentType.getString(ctx, "id"), null);
                        return 1;
                     })
                     .then(
                        ClientCommands.argument("rarity", StringArgumentType.word())
                           .suggests((ctx, builder) -> {
                              for (Rarity r : Rarity.values()) {
                                 builder.suggest(r.name().toLowerCase());
                              }

                              return builder.buildFuture();
                           })
                           .executes(ctx -> {
                              test(StringArgumentType.getString(ctx, "id"), StringArgumentType.getString(ctx, "rarity"));
                              return 1;
                           })
                     )
               )
         )
         .then(
            ClientCommands.literal("debug")
               .executes(ctx -> {
                  AIAConfig.debug = !AIAConfig.debug;
                  AIAConfig.save();
                  say("Chat logging is now " + (AIAConfig.debug ? "§aON" : "§cOFF") + "§f -> " + AIAConfig.logFile.getPath());
                  return 1;
               })
         )
         .then(
            ClientCommands.literal("sound")
               .executes(ctx -> {
                  SoundPlayer.play(Rarity.LEGENDARY);
                  say("Sound requested. Sound enabled: " + AIAConfig.sound + ", volume: " + AIAConfig.volume + " (check your Master volume slider too)");
                  Thread t = new Thread(() -> {
                     try {
                        Thread.sleep(400L);
                     } catch (InterruptedException ignored) {
                     }

                     Minecraft.getInstance().execute(() -> say("Result: " + SoundPlayer.lastStatus));
                  }, "AIA-sound-status");
                  t.setDaemon(true);
                  t.start();
                  return 1;
               })
         );
   }

   private static void test(String id, String rarityName) {
      DropEntry d = DropCatalog.byId(id);
      if (d == null) {
         say("§cUnknown id. Press Tab after /aia test to see all ids.");
      } else {
         Rarity r = d.override != null ? d.override : d.defaultRarity;
         if (rarityName != null) {
            for (Rarity x : Rarity.values()) {
               if (x.name().equalsIgnoreCase(rarityName)) {
                  r = x;
               }
            }
         }

         AnimationManager.INSTANCE.submit(d, d.name, r, true);
      }
   }

   static void say(String s) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         mc.gui.hud.getChat().addClientSystemMessage(Component.literal("§b[Animation in Action] §f" + s));
      }
   }
}
