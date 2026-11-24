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

class FarmingGardenDropsTest {
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
   void testNoDuplicateCatalogOrFarmingRowsAndAutoRarityDefault() throws Exception {
      Set<String> allIds = new HashSet<>();
      Set<String> farmingIds = new HashSet<>();
      Set<String> farmingNames = new HashSet<>();

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
            if (cat == Category.FARMING) {
               assertTrue(farmingIds.add(id.toLowerCase()), "Duplicate FARMING id: " + id);
               assertTrue(farmingNames.add(name.toLowerCase()), "Duplicate FARMING name: " + name);
            }
         }
      }

      List<DropEntry> farmingEntries = DropCatalog.byCategory(Category.FARMING);
      assertEquals(33, farmingEntries.size(), "Expected 33 FARMING entries (Refined Chip removed)");
      for (DropEntry entry : farmingEntries) {
         assertNull(entry.override, "Farming entry " + entry.id + " must default to AUTO rarity");
         assertFalse(entry.fixedRarity, "Farming entry " + entry.id + " must not set fixedRarity");
         assertNotNull(entry.pattern, "Farming entry " + entry.id + " must have a compiled regex");
      }
   }

   @Test
   void testAllApprovedFarmingAndGardenDropsPresent() {
      String[][] expectedDrops = new String[][]{
         // Rare Crops & Crop Drops
         {"cornucopia", "Cornucopia"},
         {"carrot_zest", "Carrot Zest"},
         {"deepfries", "Deepfries"},
         {"aggourdian", "Aggourdian"},
         {"cane_knot", "Cane Knot"},
         {"melon_juice", "Melon Juice"},
         {"cactus_flower", "Cactus Flower"},
         {"designer_coffee_beans", "Designer Coffee Beans"},
         {"feastfungus", "Feastfungus"},
         {"botroot", "Botroot"},
         {"salted_sunflower_seeds", "Salted Sunflower Seeds"},
         {"crystalized_moonlight", "Crystalized Moonlight"},
         {"crystalized_moonlight", "Crystallized Moonlight"},
         {"floral_gelatin", "Floral Gelatin"},
         {"cropie", "Cropie"},
         {"squash", "Squash"},
         {"fermento", "Fermento"},
         {"helianthus", "Helianthus"},
         {"warty", "Warty"},
         // Garden, Greenhouse & Harvest Bounty Drops
         {"overgrown_grass", "Overgrown Grass"},
         {"burrowing_spores", "Burrowing Spores"},
         {"suns_grasp", "Sun's Grasp"},
         {"rarefinder_chip", "Rarefinder Chip"},
         {"hypercharge_chip", "Hypercharge Chip"},
         {"vermin_vaporizer_chip", "Vermin Vaporizer Chip"},
         {"synthesis_chip", "Synthesis Chip"},
         {"sowledge_chip", "Sowledge Chip"},
         {"mechamind_chip", "Mechamind Chip"},
         {"evergreen_chip", "Evergreen Chip"},
         {"overdrive_chip", "Overdrive Chip"},
         {"cropshot_chip", "Cropshot Chip"},
         {"quickdraw_chip", "Quickdraw Chip"},
         // Pest Books / Enchants
         {"farming_for_dummies", "Farming for Dummies"},
         {"harvesting_6", "Enchanted Book (Harvesting VI)"},
         {"harvesting_6", "Harvesting VI"},
         {"harvesting_6", "Harvesting 6"}
      };

      for (String[] pair : expectedDrops) {
         String id = pair[0];
         String sample = pair[1];
         DropEntry entry = DropCatalog.byId(id);
         assertNotNull(entry, "Missing Farming/Garden drop entry: " + id);
         assertEquals(Category.FARMING, entry.category, "Entry " + id + " must belong to Category.FARMING");
         assertTrue(entry.matches("RARE CROP!", sample), "Entry " + id + " failed to match: " + sample);
      }
   }

   @Test
   void testExplicitlyExcludedFarmingItemsAbsent() {
      String[] excludedSamples = new String[]{
         "Sowdust",
         "Ethereal Vines",
         "Squeaky Toy",
         "Squeaky Mousemat",
         "Bookworm's Favorite Book",
         "Bee Shard",
         "Slug Shard",
         "Honey Jar",
         "Cheese",
         "Dung",
         "Wild Strawberry Dye"
      };

      for (DropEntry entry : DropCatalog.byCategory(Category.FARMING)) {
         for (String excluded : excludedSamples) {
            assertFalse(
               entry.matches("RARE DROP!", excluded),
               "Excluded item \"" + excluded + "\" must not match FARMING entry " + entry.id
            );
         }
      }
   }

   @Test
   void testChatDetectorRareCropRareDropAndHarvestBounty() {
      ChatDetector detector = new ChatDetector();

      // 1. RARE CROP!
      detector.handle("§6§lRARE CROP! §r§9Cornucopia §r§b(+150% ✯ Magic Find)");
      DropAnimation current = AnimationManager.INSTANCE.current();
      assertNotNull(current, "RARE CROP! Cornucopia should trigger immediately");
      assertEquals("cornucopia", current.entry.id);
      assertEquals("Cornucopia", current.name);
      assertEquals(Rarity.RARE, current.rarity);

      // 2. Harvest Bounty: with "You found a ..."
      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lHarvest Bounty: §r§eYou found a §r§6Sun's Grasp§e!");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current, "Harvest Bounty: Sun's Grasp should trigger immediately");
      assertEquals("suns_grasp", current.entry.id);
      assertEquals("Sun's Grasp", current.name);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 3. Harvest Bounty: with Chip drop
      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lHarvest Bounty: §r§5Rarefinder Chip §r§b(+120% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current, "Harvest Bounty: Rarefinder Chip should trigger immediately");
      assertEquals("rarefinder_chip", current.entry.id);
      assertEquals("Rarefinder Chip", current.name);
      assertEquals(Rarity.EPIC, current.rarity);

      // 4. RARE DROP! Overgrown Grass
      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lRARE DROP! §r§6Overgrown Grass");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current, "RARE DROP! Overgrown Grass should trigger immediately");
      assertEquals("overgrown_grass", current.entry.id);
      assertEquals("Overgrown Grass", current.name);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      // 5. RARE DROP! Enchanted Book (Harvesting VI)
      AnimationManager.INSTANCE.reset();
      detector.handle("§9§lRARE DROP! §r§9Enchanted Book (Harvesting VI) §r§b(+180% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current, "RARE DROP! Harvesting VI should trigger immediately");
      assertEquals("harvesting_6", current.entry.id);
      assertEquals("Harvesting VI", current.name);
      assertEquals(Rarity.RARE, current.rarity);

      // 6. HARVEST FEAST! Rare Crop drop
      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lHARVEST FEAST! §r§9Carrot Zest §r§b(+150% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current, "HARVEST FEAST! Carrot Zest should trigger immediately");
      assertEquals("carrot_zest", current.entry.id);
      assertEquals("Carrot Zest", current.name);
      assertEquals(Rarity.RARE, current.rarity);
   }
}
