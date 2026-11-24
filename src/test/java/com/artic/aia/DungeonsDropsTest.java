package com.artic.aia;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DungeonsDropsTest {
   @BeforeEach
   void setUp() {
      AIAConfig.master = true;
      AIAConfig.strict = true;
      AIAConfig.serverFilter = "";
      AnimationManager.INSTANCE.reset();
      for (DropEntry d : DropCatalog.ALL) {
         d.enabled = true;
         d.override = null;
      }
   }

   @Test
   void testAllApprovedDungeonsDirectDropsPresentAndAutoRarity() {
      String[][] expectedDrops = new String[][]{
         // Floor I — Bonzo
         {"bonzo_staff", "Bonzo's Staff"},
         {"balloon_snake", "Balloon Snake"},
         {"bonzo_mask", "Bonzo Mask"},
         {"bonzo_mask", "Bonzo's Mask"},
         // Floor II — Scarf
         {"scarfs_studies", "Scarf's Studies"},
         {"adaptive_blade", "Adaptive Blade"},
         {"adaptive_belt", "Adaptive Belt"},
         // Floor III — The Professor
         {"adaptive_helmet", "Adaptive Helmet"},
         {"adaptive_chestplate", "Adaptive Chestplate"},
         {"adaptive_leggings", "Adaptive Leggings"},
         {"adaptive_boots", "Adaptive Boots"},
         // Floor IV — Thorn
         {"spirit_shortbow", "Spirit Bow"},
         {"spirit_shortbow", "Spirit Shortbow"},
         {"spirit_sword", "Spirit Sword"},
         {"spirit_wing", "Spirit Wing"},
         {"spirit_bone", "Spirit Bone"},
         // Floor V — Livid
         {"livid_dagger", "Livid Dagger"},
         {"shadow_assassin_cloak", "Shadow Assassin Cloak"},
         {"shadow_assassin_helmet", "Shadow Assassin Helmet"},
         {"shadow_assassin_chestplate", "Shadow Assassin Chestplate"},
         {"shadow_assassin_leggings", "Shadow Assassin Leggings"},
         {"shadow_assassin_boots", "Shadow Assassin Boots"},
         {"last_breath", "Last Breath"},
         // Floor VI — Sadan
         {"giants_sword", "Giant's Sword"},
         {"necromancer_lord_helmet", "Necromancer Lord Helmet"},
         {"necromancer_lord_chestplate", "Necromancer Lord Chestplate"},
         {"necromancer_lord_leggings", "Necromancer Lord Leggings"},
         {"necromancer_lord_boots", "Necromancer Lord Boots"},
         {"necromancer_sword", "Necromancer Sword"},
         {"summoning_ring", "Summoning Ring"},
         {"precursor_eye", "Precursor Eye"},
         // Floor VII — Wither Lords
         {"necrons_handle", "Necron's Handle"},
         {"implosion_scroll", "Implosion"},
         {"shadow_warp_scroll", "Shadow Warp"},
         {"wither_shield_scroll", "Wither Shield"},
         {"wither_helmet", "Wither Helmet"},
         {"wither_chestplate", "Wither Chestplate"},
         {"wither_leggings", "Wither Leggings"},
         {"wither_boots", "Wither Boots"},
         {"wither_catalyst", "Wither Catalyst"},
         {"auto_recombobulator", "Auto Recombobulator"},
         {"wither_blood", "Wither Blood"},
         {"precursor_relic", "Precursor Relic"},
         {"diamantes_handle", "Diamante's Handle"},
         // General Dungeon Chests, Minibosses & Master Mode
         {"recombobulator_3000", "Recombobulator 3000"},
         {"dungeon_chest_key", "Dungeon Chest Key"},
         {"catacombs_xp_booster", "Catacombs XP Booster"},
         {"treasure_talisman", "Treasure Talisman"},
         {"shadow_fury", "Shadow Fury"},
         {"mimic_shard", "Mimic Shard"},
         {"master_star_1", "First Master Star"},
         {"master_star_2", "Second Master Star"},
         {"master_star_3", "Third Master Star"},
         {"master_star_4", "Fourth Master Star"},
         {"master_star_5", "Fifth Master Star"},
         {"dark_claymore", "Dark Claymore"},
         // Kept Dungeon Ultimate Enchantment Books
         {"soul_eater_1", "Enchanted Book (Soul Eater I)"},
         {"soul_eater_1", "Soul Eater I"},
         {"one_for_all_1", "Enchanted Book (One For All I)"},
         {"one_for_all_1", "One For All"},
         {"legion_1", "Enchanted Book (Legion I)"},
         {"legion_1", "Legion I"}
      };

      for (String[] pair : expectedDrops) {
         String id = pair[0];
         String sample = pair[1];
         DropEntry d = DropCatalog.byId(id);
         assertNotNull(d, "Missing Dungeon drop entry: " + id);
         assertEquals(Category.DUNGEONS, d.category, "Entry " + id + " must belong to Category.DUNGEONS");
         assertNull(d.override, "Entry " + id + " must default to AUTO rarity");
         assertFalse(d.fixedRarity, "Entry " + id + " must not set fixedRarity");
         assertTrue(d.matches("RARE REWARD!", sample), "Entry " + id + " failed to match: " + sample);
      }

      // Spirit Pet now lives exclusively in Category.PETS (not DUNGEONS) and must NOT false-match other Spirit items
      DropEntry spiritPet = DropCatalog.byId("spirit_pet");
      assertNotNull(spiritPet);
      assertEquals(Category.PETS, spiritPet.category);
      assertFalse(spiritPet.matches("RARE REWARD!", "Spirit Wing"));
      assertFalse(spiritPet.matches("RARE REWARD!", "Spirit Bone"));
      assertFalse(spiritPet.matches("RARE REWARD!", "Spirit Bow"));
      assertFalse(spiritPet.matches("RARE REWARD!", "Spirit Sword"));
   }

   @Test
   void testRemovedAndExcludedDungeonItemsAbsent() {
      String[] excluded = new String[]{
         "Red Nose", "Bonzo Fragment", "Suspicious Vial", "Stone of Wisdom",
         "Warped Stone", "Dark Orb", "Sadan's Brooch", "Ancient Rose",
         "Bigfoot's Bola", "Jolly Pink Rock", "Kismet Feather", "Fel Pearl",
         "Hot Potato Book", "Fuming Potato Book", "Rend I", "Ultimate Jerry I",
         "Overload I", "Swarm I", "Ultimate Wise I", "Bank I",
         "No Pain No Gain I", "Combo I", "Last Stand I", "Wisdom I"
      };

      List<DropEntry> dungeonEntries = DropCatalog.byCategory(Category.DUNGEONS);
      for (DropEntry d : dungeonEntries) {
         for (String ex : excluded) {
            assertFalse(d.matches("RARE REWARD!", ex), "Excluded item \"" + ex + "\" matched " + d.id);
         }
      }
   }

   @Test
   void testChatDetectorDungeonChestBossAndPetDrops() {
      ChatDetector detector = new ChatDetector();

      // 1. RARE REWARD! Recombobulator 3000
      detector.handle("§6§lRARE REWARD! §r§6Recombobulator 3000");
      DropAnimation current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("recombobulator_3000", current.entry.id);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 2. BEDROCK CHEST! Necron's Handle
      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lBEDROCK CHEST! §r§6Necron's Handle");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("necrons_handle", current.entry.id);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 3. PET DROP! [Lvl 1] Spirit
      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lPET DROP! §r§6[Lvl 1] Spirit §r§b(+180% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("spirit_pet", current.entry.id);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 4. RARE DROP! Enchanted Book (One For All I)
      AnimationManager.INSTANCE.reset();
      detector.handle("§9§lRARE DROP! §r§6Enchanted Book (One For All I)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("one_for_all_1", current.entry.id);
      assertEquals("One For All I", current.name);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 5. RARE DROP! Mimic Shard
      AnimationManager.INSTANCE.reset();
      detector.handle("§9§lRARE DROP! §r§5Mimic Shard §r§b(+200% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("mimic_shard", current.entry.id);
      assertEquals(Rarity.EPIC, current.rarity);
   }
}
