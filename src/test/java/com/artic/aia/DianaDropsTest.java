package com.artic.aia;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DianaDropsTest {
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
   void testNoDuplicateCatalogOrDianaRowsAndAutoRarityDefault() throws Exception {
      Set<String> allIds = new HashSet<>();
      Set<String> dianaIds = new HashSet<>();
      Set<String> dianaNames = new HashSet<>();

      try (InputStream in = DropCatalog.class.getResourceAsStream("/assets/animationinaction/drops.tsv");
           BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
         String line;
         while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.charAt(0) == '#') {
               continue;
            }
            String[] cols = line.split("\t", -1);
            assertTrue(cols.length >= 9);
            String id = cols[0].trim();
            Category cat = Category.valueOf(cols[1].trim());
            String name = cols[2].trim();

            assertTrue(allIds.add(id.toLowerCase()), "Duplicate catalog id in drops.tsv: " + id);
            if (cat == Category.DIANA) {
               assertTrue(dianaIds.add(id.toLowerCase()), "Duplicate DIANA id: " + id);
               assertTrue(dianaNames.add(name.toLowerCase()), "Duplicate DIANA name: " + name);
            }
         }
      }

      List<DropEntry> dianaEntries = DropCatalog.byCategory(Category.DIANA);
      assertEquals(17, dianaEntries.size(), "Expected 17 Diana entries (Shimmering Wool deduplicated into FISHING)");
      for (DropEntry entry : dianaEntries) {
         assertNull(entry.override, "Diana entry " + entry.id + " must default to AUTO rarity");
         assertFalse(entry.fixedRarity, "Diana entry " + entry.id + " must not set fixedRarity");
         assertNotNull(entry.pattern, "Diana entry " + entry.id + " must have a regex pattern");
      }
   }

   @Test
   void testAllTargetDianaDropsPresentAndExcludedShardsAbsent() {
      String[][] expectedDrops = new String[][]{
         {"mythos_fragment", "Mythos Fragment"},
         {"braided_griffin_feather", "Braided Griffin Feather"},
         {"hilt_of_revelations", "Hilt of Revelations"},
         {"crochet_tiger_plushie", "Crochet Tiger Plushie"},
         {"cretan_urn", "Cretan Urn"},
         {"antique_remedies", "Antique Remedies"},
         {"dwarf_turtle_shelmet", "Dwarf Turtle Shelmet"},
         {"washed_up_souvenir", "Washed-Up Souvenir"},
         {"daedalus_stick", "Daedalus Stick"},
         {"minos_relic", "Minos Relic"},
         {"brain_food", "Brain Food"},
         {"chimera_1", "Enchanted Book (Chimera I)"},
         {"chimera_1", "Chimera I"},
         {"chimera_1", "Chimera 1"},
         {"fateful_stinger", "Fateful Stinger"},
         {"manti_core", "Manti-core"},
         {"crown_of_greed", "Crown of Greed"},
         {"sphinx_shard", "Sphinx Shard"},
         {"king_minos_shard", "King Minos Shard"}
      };

      for (String[] pair : expectedDrops) {
         String id = pair[0];
         String sample = pair[1];
         DropEntry entry = DropCatalog.byId(id);
         assertNotNull(entry, "Missing Diana drop entry: " + id);
         assertEquals(Category.DIANA, entry.category, "Entry " + id + " must belong to Category.DIANA");
         assertTrue(entry.matches("RARE DROP!", sample), "Entry " + id + " failed to match: " + sample);
      }

      // Ensure excluded shards are NOT present
      assertNull(DropCatalog.byId("cretan_bull_shard"), "Cretan Bull Shard must be excluded");
      assertNull(DropCatalog.byId("minotaur_shard"), "Minotaur Shard must be excluded");
      for (DropEntry entry : DropCatalog.byCategory(Category.DIANA)) {
         assertFalse(entry.matches("RARE DROP!", "Cretan Bull Shard"), "Excluded Cretan Bull Shard matched " + entry.id);
         assertFalse(entry.matches("RARE DROP!", "Minotaur Shard"), "Excluded Minotaur Shard matched " + entry.id);
      }
   }

   @Test
   void testChatDetectorDianaBurrowMobAndShardMessages() {
      ChatDetector detector = new ChatDetector();

      // 1. Enchanted Book (Chimera I)
      detector.handle("§9§lRARE DROP! §r§9Enchanted Book (§d§lChimera I§9) §r§b(+250% ✯ Magic Find)");
      DropAnimation current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("chimera_1", current.entry.id);
      assertEquals("Chimera I", current.name);
      assertEquals(Rarity.MYTHIC, current.rarity);

      // 2. Bare Chimera I
      AnimationManager.INSTANCE.reset();
      detector.handle("§9§lRARE DROP! §r§6Chimera I §r§b(+250% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("chimera_1", current.entry.id);
      assertEquals("Chimera I", current.name);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 3. Burrow treasure: WOW! You dug out a Braided Griffin Feather!
      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lWOW! §r§eYou dug out a §r§6Braided Griffin Feather§e!");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("braided_griffin_feather", current.entry.id);
      assertEquals("Braided Griffin Feather", current.name);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 4. Burrow treasure: RARE DROP! You dug out a Mythos Fragment!
      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lRARE DROP! §r§eYou dug out a §r§5Mythos Fragment§e!");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("mythos_fragment", current.entry.id);
      assertEquals("Mythos Fragment", current.name);
      assertEquals(Rarity.EPIC, current.rarity);

      // 5. Sphinx Shard via CHARM!
      AnimationManager.INSTANCE.reset();
      detector.handle("§a§lCHARM! §r§eYou charmed a §r§5Sphinx Shard§e!");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("sphinx_shard", current.entry.id);
      assertEquals("Sphinx Shard", current.name);
      assertEquals(Rarity.EPIC, current.rarity);

      // 6. King Minos Shard via RARE DROP!
      AnimationManager.INSTANCE.reset();
      detector.handle("§9§lRARE DROP! §r§6King Minos Shard §r§b(+280% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("king_minos_shard", current.entry.id);
      assertEquals("King Minos Shard", current.name);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 7. Manti-core via CRAZY RARE DROP!
      AnimationManager.INSTANCE.reset();
      detector.handle("§d§lCRAZY RARE DROP! §r§7(§6Manti-core§7) §r§b(+300% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("manti_core", current.entry.id);
      assertEquals("Manti-core", current.name);
      assertEquals(Rarity.LEGENDARY, current.rarity);
   }
}
