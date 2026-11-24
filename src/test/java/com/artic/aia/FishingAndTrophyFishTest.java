package com.artic.aia;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FishingAndTrophyFishTest {
   private static final String[][] APPROVED_FISHING = new String[][]{
      {"radioactive_vial", "Radioactive Vial"},
      {"magma_lord_fragment", "Magma Lord Fragment"},
      {"thunder_in_a_bottle", "Thunder in a Bottle"},
      {"shimmering_wool", "Shimmering Wool"},
      {"emperor_skull", "Emperor's Skull"},
      {"lucky_clover_core", "Lucky Clover Core"},
      {"great_white_shark_tooth", "Great White Shark Tooth"},
      {"deep_sea_orb", "Deep Sea Orb"},
      {"soul_fragment", "Soul Fragment"},
      {"lucky_hoof", "Lucky Hoof"},
      {"hilt_of_true_ice", "Hilt of True Ice"},
      {"flexbone", "Flexbone"},
      {"wet_book", "Wet Book"},
      {"alligator_skin", "Alligator Skin"},
      {"titanoboa_shed", "Titanoboa Shed"},
      {"flash_1", "Enchanted Book (Flash I)"},
      {"flash_1", "Flash I"},
      {"charm_1", "Enchanted Book (Charm I)"},
      {"charm_1", "Charm I"},
      {"corruption_1", "Enchanted Book (Corruption I)"},
      {"corruption_1", "Corruption I"},
      {"piscary_6", "Enchanted Book (Piscary VI)"},
      {"piscary_6", "Piscary VI"},
      {"lure_6", "Enchanted Book (Lure VI)"},
      {"lure_6", "Lure VI"},
      {"luck_of_the_sea_6", "Enchanted Book (Luck of the Sea VI)"},
      {"luck_of_the_sea_6", "Luck of the Sea VI"},
      {"caster_6", "Enchanted Book (Caster VI)"},
      {"caster_6", "Caster VI"},
      {"blessing_6", "Enchanted Book (Blessing VI)"},
      {"blessing_6", "Blessing VI"},
      {"angler_6", "Enchanted Book (Angler VI)"},
      {"angler_6", "Angler VI"}
   };

   private static final String[][] TROPHY_SPECIES = new String[][]{
      {"blobfish", "Blobfish"},
      {"gusher", "Gusher"},
      {"obfuscated_1", "Obfuscated 1"},
      {"sulphur_skitter", "Sulphur Skitter"},
      {"steaming_hot_flounder", "Steaming-Hot Flounder"},
      {"obfuscated_2", "Obfuscated 2"},
      {"slugfish", "Slugfish"},
      {"flyfish", "Flyfish"},
      {"obfuscated_3", "Obfuscated 3"},
      {"vanille", "Vanille"},
      {"lava_horse", "Lava Horse"},
      {"mana_ray", "Mana Ray"},
      {"volcanic_stonefish", "Volcanic Stonefish"},
      {"moldfin", "Moldfin"},
      {"soul_fish", "Soul Fish"},
      {"karate_fish", "Karate Fish"},
      {"skeleton_fish", "Skeleton Fish"},
      {"golden_fish", "Golden Fish"}
   };

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
   void testExactTwentyFourFishingDropsAndAutoRarity() {
      List<DropEntry> fishing = DropCatalog.byCategory(Category.FISHING);
      assertEquals(24, fishing.size(), "Category.FISHING must contain exactly 24 approved drops");

      for (String[] pair : APPROVED_FISHING) {
         DropEntry d = DropCatalog.byId(pair[0]);
         assertNotNull(d, "Missing FISHING entry: " + pair[0]);
         assertEquals(Category.FISHING, d.category);
         assertNull(d.override);
         assertFalse(d.fixedRarity);
         assertTrue(d.matches("RARE DROP!", pair[1]), pair[0] + " failed to match: " + pair[1]);
      }

      // Excluded items must not exist in FISHING
      assertNull(DropCatalog.byId("phantom_rod"));
      assertNull(DropCatalog.byId("music_rune_1"));
      assertNull(DropCatalog.byId("thunder_fragment"));
   }

   @Test
   void testSingleToggleTrophyFishCategoryWithAllThirtySixEntries() {
      List<DropEntry> trophies = DropCatalog.byCategory(Category.TROPHY_FISH);
      assertEquals(36, trophies.size(), "Category.TROPHY_FISH must contain exactly 36 entries (18 species x Gold/Diamond)");

      // Two stacked grouped toggle rows: Diamond on top (18), Gold underneath (18)
      List<DropCatalog.Row> rows = DropCatalog.rows(Category.TROPHY_FISH);
      assertEquals(2, rows.size(), "TROPHY_FISH tab should expose Diamond and Gold grouped toggle rows");
      assertTrue(rows.get(0).isGroup());
      assertEquals("All Diamond Trophy Fish (18)", rows.get(0).label);
      assertEquals(18, rows.get(0).members.size());
      assertTrue(rows.get(1).isGroup());
      assertEquals("All Gold Trophy Fish (18)", rows.get(1).label);
      assertEquals(18, rows.get(1).members.size());

      for (String[] sp : TROPHY_SPECIES) {
         DropEntry gold = DropCatalog.byId(sp[0] + "_gold");
         DropEntry diam = DropCatalog.byId(sp[0] + "_diamond");
         assertNotNull(gold, "Missing Gold Trophy Fish: " + sp[0]);
         assertNotNull(diam, "Missing Diamond Trophy Fish: " + sp[0]);
         assertEquals(ItemLook.Mode.HEAD, gold.look.mode());
         assertEquals(ItemLook.Mode.HEAD, diam.look.mode());
         assertTrue(gold.look.headKey().startsWith("url:http"));
         assertTrue(diam.look.headKey().startsWith("url:http"));
         assertTrue(gold.matches("TROPHY FISH!", sp[1] + " GOLD"));
         assertTrue(diam.matches("TROPHY FISH!", sp[1] + " DIAMOND"));
         assertFalse(gold.matches("TROPHY FISH!", sp[1] + " BRONZE"));
         assertFalse(diam.matches("TROPHY FISH!", sp[1] + " SILVER"));
      }
   }

   @Test
   void testChatDetectorTrophyFishAndFishingBooks() {
      ChatDetector detector = new ChatDetector();

      // 1. TROPHY FISH! GOLD -> LEGENDARY
      detector.handle("§6§lTROPHY FISH! §r§7You caught a §r§aBlobfish §r§6§lGOLD§r§7!");
      DropAnimation current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("blobfish_gold", current.entry.id);
      assertEquals("Blobfish (Gold)", current.name);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 2. NEW DISCOVERY! TROPHY FISH! DIAMOND -> DIVINE
      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lNEW DISCOVERY! §r§6§lTROPHY FISH! §r§7You caught a §r§fGolden Fish §r§b§lDIAMOND§r§7!");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("golden_fish_diamond", current.entry.id);
      assertEquals("Golden Fish (Diamond)", current.name);
      assertEquals(Rarity.DIVINE, current.rarity);

      // 3. BRONZE and SILVER must be ignored
      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lTROPHY FISH! §r§7You caught a §r§aBlobfish §r§8§lBRONZE§r§7!");
      assertNull(AnimationManager.INSTANCE.current(), "Bronze trophy fish must not trigger");

      detector.handle("§6§lTROPHY FISH! §r§7You caught a §r§aBlobfish §r§7§lSILVER§r§7!");
      assertNull(AnimationManager.INSTANCE.current(), "Silver trophy fish must not trigger");

      // 4. Fishing book drop
      AnimationManager.INSTANCE.reset();
      detector.handle("§9§lRARE DROP! §r§5Enchanted Book (Flash I) §r§b(+200% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("flash_1", current.entry.id);
      assertEquals("Flash I", current.name);
      assertEquals(Rarity.EPIC, current.rarity);
   }
}
