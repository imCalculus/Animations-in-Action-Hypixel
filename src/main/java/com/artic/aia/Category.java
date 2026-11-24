package com.artic.aia;

import net.minecraft.world.item.ItemStack;

public enum Category {
   GENERAL("General", "snowball"),
   REVENANT("Revenant Slayer", "rotten_flesh"),
   SVEN("Sven Slayer", "bone"),
   TARANTULA("Tarantula Slayer", "spider_eye"),
   ENDERMAN("Ender Slayer", "ender_pearl"),
   DEMONLORD("Inferno Slayer", "blaze_rod"),
   VAMPIRE("Vampire Slayer", "redstone"),
   DIANA("Diana", "emerald"),
   DUNGEONS("Dungeon Chests", "wither_skeleton_skull"),
   KUUDRA("Crimson & Kuudra", "magma_cream"),
   DRAGONS("Dragons", "dragon_head"),
   FISHING("Fishing", "fishing_rod"),
   TROPHY_FISH("Trophy Fish", "tropical_fish"),
   FARMING("Farming", "diamond_hoe"),
   MINING("Mining", "diamond_pickaxe"),
   FORAGING("Foraging", "diamond_axe"),
   DYES("Dyes", "light_blue_dye"),
   PETS("Pets", "lead"),
   MISC("Misc", "nether_star");

   public final String title;
   private final String iconItem;
   private ItemStack icon;

   private Category(String title, String iconItem) {
      this.title = title;
      this.iconItem = iconItem;
   }

   public ItemStack icon() {
      if (this.icon == null) {
         this.icon = LegacyItems.stackById(this.iconItem);
      }

      return this.icon;
   }
}
