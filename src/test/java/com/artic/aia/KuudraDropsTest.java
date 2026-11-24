package com.artic.aia;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KuudraDropsTest {
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
   void testAllThirtyNineCrimsonIsleAndKuudraDirectDropsPresent() {
      List<DropEntry> kuudra = DropCatalog.byCategory(Category.KUUDRA);
      assertEquals(39, kuudra.size(), "Category.KUUDRA must contain exactly the 39 approved direct drops");

      String[][] expected = new String[][]{
         {"kuudra_mandible", "Kuudra Mandible"},
         {"wheel_of_fate", "Wheel of Fate"},
         {"tormentor", "Tormentor"},
         {"enrager", "Enrager"},
         {"hellstorm_wand", "Hellstorm Wand"},
         {"hollow_wand", "Hollow Wand"},
         {"aurora_staff", "Aurora Staff"},
         {"crimson_helmet", "Crimson Helmet"},
         {"crimson_chestplate", "Crimson Chestplate"},
         {"crimson_leggings", "Crimson Leggings"},
         {"crimson_boots", "Crimson Boots"},
         {"aurora_helmet", "Aurora Helmet"},
         {"aurora_chestplate", "Aurora Chestplate"},
         {"aurora_leggings", "Aurora Leggings"},
         {"aurora_boots", "Aurora Boots"},
         {"terror_helmet", "Terror Helmet"},
         {"terror_chestplate", "Terror Chestplate"},
         {"terror_leggings", "Terror Leggings"},
         {"terror_boots", "Terror Boots"},
         {"hollow_helmet", "Hollow Helmet"},
         {"hollow_chestplate", "Hollow Chestplate"},
         {"hollow_leggings", "Hollow Leggings"},
         {"hollow_boots", "Hollow Boots"},
         {"fervor_helmet", "Fervor Helmet"},
         {"fervor_chestplate", "Fervor Chestplate"},
         {"fervor_leggings", "Fervor Leggings"},
         {"fervor_boots", "Fervor Boots"},
         {"molten_necklace", "Molten Necklace"},
         {"molten_cloak", "Molten Cloak"},
         {"molten_belt", "Molten Belt"},
         {"molten_bracelet", "Molten Bracelet"},
         {"fatal_tempo_1", "Enchanted Book (Fatal Tempo I)"},
         {"fatal_tempo_1", "Fatal Tempo I"},
         {"inferno_1", "Enchanted Book (Inferno I)"},
         {"inferno_1", "Inferno I"},
         {"kraken_shard", "Kraken Shard"},
         {"magma_urchin", "Magma Urchin"},
         {"lava_shell_necklace", "Lava Shell Necklace"},
         {"searing_stone", "Searing Stone"},
         {"cinderbat_shard", "Cinderbat Shard"},
         {"barbarian_duke_shard", "Barbarian Duke Shard"},
         {"barbarian_duke_shard", "Barbarian Duke X Shard"}
      };

      for (String[] pair : expected) {
         DropEntry d = DropCatalog.byId(pair[0]);
         assertNotNull(d, "Missing KUUDRA entry: " + pair[0]);
         assertEquals(Category.KUUDRA, d.category);
         assertNull(d.override);
         assertFalse(d.fixedRarity);
         assertTrue(d.matches("RARE REWARD!", pair[1]), pair[0] + " failed to match: " + pair[1]);
      }

      // Verify excluded Crimson Isle items are NOT present
      String[] excluded = new String[]{
         "Kuudra Teeth", "Tentacle Meat", "Ferocious Mana I", "Hardened Mana I",
         "Mana Vampire I", "Strong Mana I", "Nether Star", "Bladesoul Soul",
         "Spell Powder", "Leather Cloth", "Lumino Fiber", "Burning Eye",
         "Smoldering Polarization", "Wither Soul", "Hallowed Skull", "Kada Lead"
      };
      for (DropEntry d : kuudra) {
         for (String ex : excluded) {
            assertFalse(d.matches("RARE DROP!", ex), "Excluded item \"" + ex + "\" matched " + d.id);
         }
      }
   }

   @Test
   void testChatDetectorKuudraPaidChestAndCrimsonIsleDrops() {
      ChatDetector detector = new ChatDetector();

      detector.handle("§6§lPAID CHEST! §r§dWheel of Fate");
      DropAnimation current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("wheel_of_fate", current.entry.id);
      assertEquals(Rarity.MYTHIC, current.rarity);

      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lRARE REWARD! §r§6Enchanted Book (Fatal Tempo I)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("fatal_tempo_1", current.entry.id);
      assertEquals("Fatal Tempo I", current.name);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      AnimationManager.INSTANCE.reset();
      detector.handle("§a§lCHARM! §r§eYou charmed a §r§6Kraken Shard§e!");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("kraken_shard", current.entry.id);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      AnimationManager.INSTANCE.reset();
      detector.handle("§9§lRARE DROP! §r§5Lava Shell Necklace §r§b(+200% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("lava_shell_necklace", current.entry.id);
      assertEquals(Rarity.EPIC, current.rarity);
   }
}
