package com.artic.aia;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;

final class LegacyItems {
   private static final Map<String, LegacyItems.Spec> MAP = new HashMap<>();

   private LegacyItems() {
   }

   private static void put(String legacyKey, String item, String texture) {
      MAP.put(legacyKey, new LegacyItems.Spec(item, texture));
   }

   private static String strip(String name) {
      if (name == null) {
         return "stick";
      } else {
         return name.startsWith("minecraft:") ? name.substring(10) : name;
      }
   }

   private static LegacyItems.Spec spec(String itemName, int damage) {
      String n = strip(itemName);
      LegacyItems.Spec s = MAP.get(n + "|" + damage);
      if (s == null) {
         s = MAP.get(n + "|0");
      }

      if (s == null) {
         s = new LegacyItems.Spec(n, n);
      }

      return s;
   }

   static String texture(String itemName, int damage) {
      return spec(itemName, damage).texture;
   }

   static Item item(String itemName, int damage) {
      return itemById(spec(itemName, damage).item);
   }

   static Item itemById(String id) {
      Item it = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", id));
      if (it == Items.AIR) {
         it = Items.STICK;
      }

      if (!it.builtInRegistryHolder().areComponentsBound()) {
         it.builtInRegistryHolder().bindComponents(DataComponents.COMMON_ITEM_COMPONENTS);
      }

      return it;
   }

   static ItemStack stackById(String id) {
      return new ItemStack(itemById(id));
   }

   static ItemStack stack(String itemName, int damage, boolean glint, int tint) {
      ItemStack s = new ItemStack(item(itemName, damage));
      if (tint >= 0) {
         s.set(DataComponents.DYED_COLOR, new DyedItemColor(tint & 0xFFFFFF));
      }

      if (glint) {
         s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, Boolean.TRUE);
      }

      return s;
   }

   static {
      put("quartz|0", "quartz", "quartz");
      put("ghast_tear|0", "ghast_tear", "ghast_tear");
      put("rotten_flesh|0", "rotten_flesh", "rotten_flesh");
      put("fish|1", "salmon", "salmon");
      put("dye|5", "purple_dye", "purple_dye");
      put("stick|0", "stick", "stick");
      put("blaze_rod|0", "blaze_rod", "blaze_rod");
      put("fishing_rod|0", "fishing_rod", "fishing_rod");
      put("enchanted_book|0", "enchanted_book", "enchanted_book");
      put("iron_sword|0", "iron_sword", "iron_sword");
      put("stone_sword|0", "stone_sword", "stone_sword");
      put("diamond_sword|0", "diamond_sword", "diamond_sword");
      put("golden_sword|0", "golden_sword", "golden_sword");
      put("leather_chestplate|0", "leather_chestplate", "leather_chestplate");
      put("leather_leggings|0", "leather_leggings", "leather_leggings");
      put("leather_boots|0", "leather_boots", "leather_boots");
      put("bow|0", "bow", "bow");
      put("diamond_horse_armor|0", "diamond_horse_armor", "diamond_horse_armor");
      put("ender_eye|0", "ender_eye", "ender_eye");
      put("firework_charge|0", "firework_star", "firework_star");
      put("lead|0", "lead", "lead");
      put("writable_book|0", "writable_book", "writable_book");
      put("slime_ball|0", "slime_ball", "slime_ball");
      put("emerald|0", "emerald", "emerald");
      put("fermented_spider_eye|0", "fermented_spider_eye", "fermented_spider_eye");
      put("diamond|0", "diamond", "diamond");
      put("bone|0", "bone", "bone");
      put("mutton|0", "mutton", "mutton");
      put("prismarine_shard|0", "prismarine_shard", "prismarine_shard");
      put("spider_eye|0", "spider_eye", "spider_eye");
      put("coal|0", "coal", "coal");
      put("golden_shovel|0", "golden_shovel", "golden_shovel");
      put("glass_bottle|0", "glass_bottle", "glass_bottle");
      put("nautilus_shell|0", "nautilus_shell", "nautilus_shell");
      put("gold_nugget|0", "gold_nugget", "gold_nugget");
      put("egg|0", "egg", "egg");
      put("ender_pearl|0", "ender_pearl", "ender_pearl");
      put("comparator|0", "comparator", "comparator");
      put("echo_shard|0", "echo_shard", "echo_shard");
      put("amethyst_shard|0", "amethyst_shard", "amethyst_shard");
      put("cauldron|0", "cauldron", "cauldron");
      put("bucket|0", "bucket", "bucket");
      put("book|0", "book", "book");
      put("popped_chorus_fruit|0", "popped_chorus_fruit", "popped_chorus_fruit");
      put("magma_cream|0", "magma_cream", "magma_cream");
      put("splash_potion|0", "splash_potion", "splash_potion");
      put("blaze_powder|0", "blaze_powder", "blaze_powder");
      put("paper|0", "paper", "paper");
      put("prismarine_crystals|0", "prismarine_crystals", "prismarine_crystals");
      put("redstone|0", "redstone", "redstone");
      put("cookie|0", "cookie", "cookie");
      put("cooked_beef|0", "cooked_beef", "cooked_beef");
      put("feather|0", "feather", "feather");
      put("golden_helmet|0", "golden_helmet", "golden_helmet");
      put("turtle_helmet|0", "turtle_helmet", "turtle_helmet");
      put("flower_pot|0", "flower_pot", "flower_pot");
      put("gold_ingot|0", "gold_ingot", "gold_ingot");
      put("honeycomb|0", "honeycomb", "honeycomb");
      put("string|0", "string", "string");
      put("heart_of_the_sea|0", "heart_of_the_sea", "heart_of_the_sea");
      put("breeze_rod|0", "breeze_rod", "breeze_rod");
      put("wheat|0", "wheat", "wheat");
      put("carrot|0", "carrot", "carrot");
      put("golden_carrot|0", "golden_carrot", "golden_carrot");
      put("baked_potato|0", "baked_potato", "baked_potato");
      put("pumpkin_pie|0", "pumpkin_pie", "pumpkin_pie");
      put("sugar_cane|0", "sugar_cane", "sugar_cane");
      put("glistering_melon_slice|0", "glistering_melon_slice", "glistering_melon_slice");
      put("pink_petals|0", "pink_petals", "pink_petals");
      put("cocoa_beans|0", "cocoa_beans", "cocoa_beans");
      put("mushroom_stew|0", "mushroom_stew", "mushroom_stew");
      put("beetroot|0", "beetroot", "beetroot");
      put("wheat_seeds|0", "wheat_seeds", "wheat_seeds");
      put("glowstone_dust|0", "glowstone_dust", "glowstone_dust");
      put("nether_wart|0", "nether_wart", "nether_wart");
      put("golden_apple|0", "golden_apple", "golden_apple");
      put("seagrass|0", "seagrass", "seagrass");
      put("pitcher_pod|0", "pitcher_pod", "pitcher_pod");
      put("repeater|0", "repeater", "repeater");
      put("iron_ingot|0", "iron_ingot", "iron_ingot");
      put("copper_ingot|0", "copper_ingot", "copper_ingot");
      put("kelp|0", "kelp", "kelp");
      put("lapis_lazuli|0", "lapis_lazuli", "lapis_lazuli");
      put("fire_charge|0", "fire_charge", "fire_charge");
      put("flint|0", "flint", "flint");
      put("dragon_breath|0", "dragon_breath", "dragon_breath");
      put("chorus_fruit|0", "chorus_fruit", "chorus_fruit");
      put("lava_bucket|0", "lava_bucket", "lava_bucket");
      put("melon_seeds|0", "melon_seeds", "melon_seeds");
      put("glow_ink_sac|0", "glow_ink_sac", "glow_ink_sac");
      put("mooshroom_spawn_egg|0", "mooshroom_spawn_egg", "mooshroom_spawn_egg");
      put("wooden_button|0", "oak_button", "oak_button");
      put("oak_button|0", "oak_button", "oak_button");
      put("yellow_wool|0", "yellow_wool", "yellow_wool");
      put("azure_bluet|0", "azure_bluet", "azure_bluet");
      put("wither_skeleton_skull|0", "wither_skeleton_skull", "wither_skeleton_skull");
      put("tripwire_hook|0", "tripwire_hook", "tripwire_hook");
      put("experience_bottle|0", "experience_bottle", "experience_bottle");
      put("goat_horn|0", "goat_horn", "goat_horn");
      put("weeping_vines|0", "weeping_vines", "weeping_vines");
      put("cactus_flower|0", "cactus_flower", "cactus_flower");
      put("fire_coral|0", "fire_coral", "fire_coral");
      put("beetroot_seeds|0", "beetroot_seeds", "beetroot_seeds");
      put("honey_bottle|0", "honey_bottle", "honey_bottle");
      put("beetroot_soup|0", "beetroot_soup", "beetroot_soup");
      put("yellow_bundle|0", "yellow_bundle", "yellow_bundle");
      put("light_blue_dye|0", "light_blue_dye", "light_blue_dye");
      put("redstone_torch|0", "redstone_torch", "redstone_torch");
      put("lime_dye|0", "lime_dye", "lime_dye");
      put("green_dye|0", "green_dye", "green_dye");
      put("iron_horse_armor|0", "iron_horse_armor", "iron_horse_armor");
      put("ice|0", "ice", "ice");
   }

   private static final class Spec {
      final String item;
      final String texture;

      Spec(String item, String texture) {
         this.item = item;
         this.texture = texture;
      }
   }
}
