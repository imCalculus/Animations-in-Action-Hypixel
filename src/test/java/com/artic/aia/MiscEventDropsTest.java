package com.artic.aia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MiscEventDropsTest {
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
   void testApprovedMiscEventDropsPresentAndDeduplicated() {
      // Vampire Fang moved from FISHING into MISC (no duplicate in FISHING)
      for (DropEntry d : DropCatalog.byCategory(Category.FISHING)) {
         assertNotEquals("vampire_fang", d.id, "vampire_fang must be deduplicated into MISC");
      }

      String[] ids = new String[]{
         "vampire_fang", "vampiric_6", "witch_mask",
         "cryopowder_shard", "divine_chocolate_rabbit", "mythic_chocolate_rabbit"
      };
      for (String id : ids) {
         DropEntry d = DropCatalog.byId(id);
         assertNotNull(d, "Missing MISC event drop: " + id);
         assertEquals(Category.MISC, d.category, id + " must belong to Category.MISC");
         assertNull(d.override, id + " must default to AUTO rarity");
         assertFalse(d.fixedRarity, id + " must not set fixedRarity");
      }

      // Skull entries must have valid Mojang URLs
      for (String skullId : new String[]{"witch_mask", "cryopowder_shard", "divine_chocolate_rabbit", "mythic_chocolate_rabbit"}) {
         DropEntry d = DropCatalog.byId(skullId);
         assertEquals(ItemLook.Mode.HEAD, d.look.mode());
         assertTrue(d.look.headKey().startsWith("url:http"));
      }

      // Vampiric VI matches both wrapped and bare names
      DropEntry vampiric = DropCatalog.byId("vampiric_6");
      assertTrue(vampiric.book);
      assertTrue(vampiric.matches("RARE DROP!", "Enchanted Book (Vampiric VI)"));
      assertTrue(vampiric.matches("RARE DROP!", "Vampiric VI"));
   }

   @Test
   void testChatDetectorHoppityRabbitsAndMiscEventDrops() {
      ChatDetector detector = new ChatDetector();

      // 1. HOPPITY'S HUNT DIVINE rabbit (NEW RABBIT!)
      detector.handle("§d§lHOPPITY'S HUNT §r§7You found a §r§b§lDIVINE §r§bOrion §r§7(NEW RABBIT!)");
      DropAnimation current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("divine_chocolate_rabbit", current.entry.id);
      assertEquals("Divine Chocolate Rabbit", current.name);
      assertEquals(Rarity.DIVINE, current.rarity);

      // 2. DUPLICATE RABBIT! MYTHIC rabbit
      AnimationManager.INSTANCE.reset();
      detector.handle("§6§lDUPLICATE RABBIT! §r§d§lMYTHIC §r§dZorro");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("mythic_chocolate_rabbit", current.entry.id);
      assertEquals("Mythic Chocolate Rabbit", current.name);
      assertEquals(Rarity.MYTHIC, current.rarity);

      // 3. HOPPITY'S HUNT COMMON rabbit must NOT trigger
      AnimationManager.INSTANCE.reset();
      detector.handle("§d§lHOPPITY'S HUNT §r§7You found a §r§fCOMMON §r§fFluffy §r§7(NEW RABBIT!)");
      assertNull(AnimationManager.INSTANCE.current(), "Common Hoppity rabbit must not trigger");

      // 4. RARE DROP! Vampire Fang
      AnimationManager.INSTANCE.reset();
      detector.handle("§d§lCRAZY RARE DROP! §r§dVampire Fang §r§b(+200% ✯ Magic Find)");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("vampire_fang", current.entry.id);
      assertEquals(Rarity.MYTHIC, current.rarity);

      // 5. RARE DROP! Witch Mask
      AnimationManager.INSTANCE.reset();
      detector.handle("§9§lRARE DROP! §r§9Witch Mask");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("witch_mask", current.entry.id);
      assertEquals(Rarity.RARE, current.rarity);

      // 6. CHARM! Cryopowder Shard
      AnimationManager.INSTANCE.reset();
      detector.handle("§a§lCHARM! §r§eYou charmed a §r§6Cryopowder Shard§e!");
      current = AnimationManager.INSTANCE.current();
      assertNotNull(current);
      assertEquals("cryopowder_shard", current.entry.id);
      assertEquals(Rarity.LEGENDARY, current.rarity);
   }
}
