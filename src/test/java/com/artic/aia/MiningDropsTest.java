package com.artic.aia;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MiningDropsTest {
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
   void testAllElevenApprovedMiningDropsPresentAndAutoRarity() {
      String[][] expectedDrops = new String[][]{
         {"divan_alloy", "Divan's Alloy"},
         {"quick_claw", "Quick Claw"},
         {"jaderald", "Jaderald"},
         {"precious_pearl", "Precious Pearl"},
         {"divans_fragment", "Divan Fragment"},
         {"divans_fragment", "Divan's Fragment"},
         {"gemstone_mixture", "Gemstone Mixture"},
         {"refined_mineral", "Refined Mineral"},
         {"glossy_gemstone", "Glossy Gemstone"},
         {"suspicious_scrap", "Suspicious Scrap"},
         {"glacite_amalgamation", "Glacite Amalgamation"},
         {"frozen_amalgam", "Frozen Amalgam"}
      };

      for (String[] pair : expectedDrops) {
         String id = pair[0];
         String sample = pair[1];
         DropEntry d = DropCatalog.byId(id);
         assertNotNull(d, "Missing Mining drop entry: " + id);
         assertEquals(Category.MINING, d.category, "Entry " + id + " must belong to Category.MINING");
         assertNull(d.override, "Entry " + id + " must default to AUTO rarity");
         assertFalse(d.fixedRarity, "Entry " + id + " must not set fixedRarity");
         assertTrue(d.matches("RARE DROP!", sample), "Entry " + id + " failed to match: " + sample);
      }

      // All 8 skull-based Mining drops must use HEAD mode with Mojang URLs
      String[] skullIds = new String[]{
         "divan_alloy", "quick_claw", "jaderald", "precious_pearl",
         "divans_fragment", "gemstone_mixture", "suspicious_scrap", "glacite_amalgamation"
      };
      for (String id : skullIds) {
         DropEntry d = DropCatalog.byId(id);
         assertEquals(ItemLook.Mode.HEAD, d.look.mode(), "Mining entry " + id + " must use HEAD mode");
         assertTrue(d.look.headKey().startsWith("url:http"), "Mining entry " + id + " must have Mojang URL");
      }
   }

   @Test
   void testSkippedMiningItemsAbsent() {
      String[] skipped = new String[]{
         "Sludge Juice", "Electron Transmitter", "FTX 3070", "Robotron Reflector",
         "Superlite Motor", "Control Switch", "Synthetic Heart", "Jungle Key",
         "Worm Membrane", "Corleonite", "Goblin Egg", "End Stone Geode", "Endermite Nest"
      };
      List<DropEntry> miningEntries = DropCatalog.byCategory(Category.MINING);
      for (DropEntry d : miningEntries) {
         for (String s : skipped) {
            assertFalse(d.matches("RARE DROP!", s), "Skipped item \"" + s + "\" matched " + d.id);
         }
      }
   }

   @Test
   void testChatDetectorMiningNucleusMineshaftAndFiestaDrops() {
      ChatDetector detector = new ChatDetector();

      // 1. RARE DROP! Divan's Alloy
      detector.handle("§d§lCRAZY RARE DROP! §r§6Divan's Alloy");
      DropAnimation current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("divan_alloy", current.entry.id);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 2. MINING FIESTA! Refined Mineral
      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lMINING FIESTA! §r§9Refined Mineral §r§b(+150% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("refined_mineral", current.entry.id);
      assertEquals(Rarity.RARE, current.rarity);

      // 3. CORPSE LOOT! Glacite Amalgamation
      AnimationManager.INSTANCE.reset();
      detector.handle("§b§lCORPSE LOOT! §r§5Glacite Amalgamation");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("glacite_amalgamation", current.entry.id);
      assertEquals(Rarity.EPIC, current.rarity);

      // 4. RARE DROP! Jaderald
      AnimationManager.INSTANCE.reset();
      detector.handle("§9§lRARE DROP! §r§5Jaderald");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("jaderald", current.entry.id);
      assertEquals(Rarity.EPIC, current.rarity);
   }
}
