package com.artic.aia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForagingAndDragonEggsTest {
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
   void testForagingShardsAndRemovalsAndPetsDragonEggs() {
      // 1. Foraging additions
      assertEquals(2, DropCatalog.byCategory(Category.FORAGING).size());
      for (String[] pair : new String[][]{{"chameleon_shard", "Chameleon Shard"}, {"hummingbird_shard", "Hummingbird Shard"}}) {
         DropEntry d = DropCatalog.byId(pair[0]);
         assertNotNull(d, "Missing Foraging shard: " + pair[0]);
         assertEquals(Category.FORAGING, d.category);
         assertNull(d.override);
         assertFalse(d.fixedRarity);
         assertEquals(ItemLook.Mode.HEAD, d.look.mode());
         assertTrue(d.look.headKey().startsWith("url:http"));
         assertTrue(d.matches("RARE DROP!", pair[1]));
      }

      // 2. Five removed items must be completely absent from DropCatalog.ALL
      for (String removedId : new String[]{
         "flawed_opal_gemstone",
         "wisps_ice_flavored_water",
         "shard_of_pure_wisdom",
         "refined_chip",
         "piece_of_skyblock_code"
      }) {
         assertNull(DropCatalog.byId(removedId), "Removed item must not exist in drops.tsv: " + removedId);
      }

      // 3. All three Dragon Eggs must live in PETS (and NOT in FORAGING or MISC)
      for (String[] pair : new String[][]{
         {"golden_dragon_egg", "Golden Dragon Egg"},
         {"jade_dragon_egg", "Jade Dragon Egg"},
         {"rose_dragon_egg", "Rose Dragon Egg"}
      }) {
         DropEntry d = DropCatalog.byId(pair[0]);
         assertNotNull(d, "Missing Dragon Egg in PETS: " + pair[0]);
         assertEquals(Category.PETS, d.category, pair[0] + " must belong to Category.PETS");
         assertNull(d.override);
         assertFalse(d.fixedRarity);
         assertEquals(ItemLook.Mode.HEAD, d.look.mode());
         assertTrue(d.look.headKey().startsWith("url:http"));
         assertTrue(d.matches("RARE DROP!", pair[1]));
      }
   }

   @Test
   void testChatDetectorForagingShardsAndDragonEggs() {
      ChatDetector detector = new ChatDetector();

      detector.handle("§a§lCHARM! §r§eYou charmed a §r§6Chameleon Shard§e!");
      DropAnimation current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("chameleon_shard", current.entry.id);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      AnimationManager.INSTANCE.reset();
      detector.handle("§9§lRARE DROP! §r§5Hummingbird Shard §r§b(+180% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("hummingbird_shard", current.entry.id);
      assertEquals(Rarity.EPIC, current.rarity);

      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lRARE DROP! §r§6Jade Dragon Egg");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("jade_dragon_egg", current.entry.id);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lRARE DROP! §r§6Rose Dragon Egg");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("rose_dragon_egg", current.entry.id);
      assertEquals(Rarity.LEGENDARY, current.rarity);

      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lRARE DROP! §r§6Golden Dragon Egg");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("golden_dragon_egg", current.entry.id);
      assertEquals(Rarity.LEGENDARY, current.rarity);
   }
}
