package com.artic.aia;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CatalogIconAuditTest {
   @BeforeAll
   static void bootstrapMinecraft() {
      SharedConstants.tryDetectVersion();
      Bootstrap.bootStrap();
   }

   @Test
   void testAllCategoryTabIconsValidIn26_3() {
      for (Category cat : Category.values()) {
         ItemStack stack = cat.icon();
         assertNotNull(stack, "Category " + cat + " icon must not be null");
         assertFalse(stack.isEmpty(), "Category " + cat + " icon must not be empty");
         assertNotEquals(Items.AIR, stack.getItem(), "Category " + cat + " icon must not be AIR");
         assertNotEquals(Items.BARRIER, stack.getItem(), "Category " + cat + " icon must not be BARRIER");
      }
   }

   @Test
   void testAllDropsResolveToValid26_3IconsAndTextures() throws Exception {
      assertFalse(DropCatalog.ALL.isEmpty(), "DropCatalog.ALL must not be empty");

      for (DropEntry entry : DropCatalog.ALL) {
         ItemLook look = entry.look;
         assertNotNull(look, "Entry " + entry.id + " must have a non-null ItemLook");

         if (look.mode() == ItemLook.Mode.VOXEL) {
            Item item = LegacyItems.item(look.itemName, look.damage);
            assertNotNull(item, "Entry " + entry.id + " resolved to null Item");
            assertNotEquals(Items.AIR, item, "Entry " + entry.id + " resolved to AIR");
            assertNotEquals(Items.BARRIER, item, "Entry " + entry.id + " resolved to BARRIER");

            // Ensure no accidental fallback to STICK when the item isn't supposed to be a stick
            String raw = look.itemName != null && look.itemName.startsWith("minecraft:")
               ? look.itemName.substring(10)
               : look.itemName;
            if (!"stick".equals(raw)) {
               assertNotEquals(Items.STICK, item, "Entry " + entry.id + " (" + look.itemName + ") fell back to STICK");
            }

            ItemStack stack = entry.iconStack();
            assertNotNull(stack, "VOXEL entry " + entry.id + " must return a non-null iconStack");
            assertFalse(stack.isEmpty(), "VOXEL entry " + entry.id + " iconStack must not be empty");
            assertNotEquals(Items.BARRIER, stack.getItem(), "VOXEL entry " + entry.id + " must not use BARRIER");

            if (!(item instanceof BlockItem)) {
               String tex = LegacyItems.texture(look.itemName, look.damage);
               String texPath = "/assets/minecraft/textures/item/" + tex + ".png";
               try (InputStream in = CatalogIconAuditTest.class.getResourceAsStream(texPath)) {
                  assertNotNull(in, "Missing Minecraft 26.3 item texture " + texPath + " for entry " + entry.id);
               }
            }

            if (entry.book) {
               assertTrue(
                  item == Items.ENCHANTED_BOOK || item == Items.BOOK || item == Items.WRITABLE_BOOK,
                  "Book entry " + entry.id + " must use a book visual, got: " + BuiltInRegistries.ITEM.getKey(item)
               );
            }

            if (entry.id.endsWith("_shard")) {
               assertTrue(
                  item == Items.PRISMARINE_SHARD || item == Items.AMETHYST_SHARD || item == Items.ECHO_SHARD,
                  "Shard entry " + entry.id + " must use a shard visual, got: " + BuiltInRegistries.ITEM.getKey(item)
               );
            }
         } else {
            String headKey = look.headKey();
            assertNotNull(headKey, "HEAD entry " + entry.id + " must have a valid headKey");
            if (headKey.startsWith("file:")) {
               String skullPath = "/assets/animationinaction/skulls/" + headKey.substring(5) + ".png";
               try (InputStream in = CatalogIconAuditTest.class.getResourceAsStream(skullPath)) {
                  assertNotNull(in, "Bundled skull texture missing at " + skullPath + " for entry " + entry.id);
               }
            } else if (headKey.startsWith("url:")) {
               String url = headKey.substring(4);
               assertTrue(
                  url.startsWith("http://textures.minecraft.net/texture/")
                     || url.startsWith("https://textures.minecraft.net/texture/"),
                  "HEAD entry " + entry.id + " has invalid Mojang texture URL: " + url
               );
            } else {
               fail("HEAD entry " + entry.id + " has unrecognized headKey format: " + headKey);
            }

            ItemStack fallback = look.fallbackStack();
            assertNotNull(fallback);
            assertEquals(Items.PLAYER_HEAD, fallback.getItem(), "HEAD entry fallbackStack should be PLAYER_HEAD");
         }

         // Verify BuiltInRegistries.ITEM lookup for any raw non-legacy item id is valid
         Identifier key = BuiltInRegistries.ITEM.getKey(look.fallbackStack().getItem());
         assertNotNull(key);
      }
   }

   @Test
   void testHypixelVerifiedSkullsPetsAndGlintFlags() {
      // Warden Heart + all 15 Pets must resolve as valid HEAD textures
      DropEntry warden = DropCatalog.byId("warden_heart");
      assertNotNull(warden);
      assertEquals(ItemLook.Mode.HEAD, warden.look.mode());
      assertTrue(warden.look.headKey().startsWith("url:http"));

      assertEquals(26, DropCatalog.byCategory(Category.PETS).size());
      for (DropEntry pet : DropCatalog.byCategory(Category.PETS)) {
         assertEquals(ItemLook.Mode.HEAD, pet.look.mode(), "Pet " + pet.id + " must use HEAD mode");
         assertTrue(pet.look.headKey().startsWith("url:http"), "Pet " + pet.id + " must have Mojang texture URL");
      }

      // All verified Hypixel API + NEU-REPO skull entries must use HEAD mode
      String[] verifiedApiSkulls = new String[]{
         "severed_hand", "shredded_sinew", "beheaded_horror", "festering_maggot",
         "vial_of_venom", "ensnared_snail", "tarantula_talisman",
         "end_stone_idol", "sinful_dice", "etherwarp_merger", "transmission_tuner",
         "pocket_espresso_machine", "handy_blood_chalice", "void_conqueror_enderman_skin",
         "scorched_power_crystal", "mana_disintegrator", "unfanged_vampire_part",
         "guardian_lucky_block", "mcgrubbers_burger",
         "manti_core", "fateful_stinger", "washed_up_souvenir", "cretan_urn",
         "brain_food", "crochet_tiger_plushie", "mythos_fragment", "dwarf_turtle_shelmet",
         // NEU-REPO verified Runes, Shards, Catalysts & SkyBlock heads
         "paragon_shard", "king_minos_shard", "sphinx_shard",
         "chameleon_shard", "hummingbird_shard",
         "golden_dragon_egg", "jade_dragon_egg", "rose_dragon_egg",
         "pestilence_rune_1", "snake_rune_1", "bite_rune_1", "darkness_within_rune_1",
         "spirit_rune_1", "couture_rune_1", "end_rune_1", "enchant_rune_1",
         "fiery_burst_rune_1", "lavatears_rune_1", "soultwist_rune_1",
         "undead_catalyst", "revenant_catalyst", "spider_catalyst", "tarantula_catalyst",
         "summoning_eye", "arachnes_fang", "hazmat_enderman",
         "kelvin_inverter", "subzero_inverter", "scorched_books",
         "bubba_blister", "overgrown_grass", "burrowing_spores",
         "warty", "suns_grasp",
         "rarefinder_chip", "hypercharge_chip", "vermin_vaporizer_chip",
         "synthesis_chip", "sowledge_chip", "mechamind_chip",
         "evergreen_chip", "overdrive_chip", "cropshot_chip", "quickdraw_chip"
      };
      for (String id : verifiedApiSkulls) {
         DropEntry d = DropCatalog.byId(id);
         assertNotNull(d, "Missing verified skull entry: " + id);
         assertEquals(ItemLook.Mode.HEAD, d.look.mode(), "Entry " + id + " must use HEAD mode");
         assertTrue(d.look.headKey().startsWith("url:http"), "Entry " + id + " must have Mojang texture URL");
      }

      // Exact vanilla 26.3 items from NEU-REPO
      DropEntry redClawEgg = DropCatalog.byId("red_claw_egg");
      assertNotNull(redClawEgg);
      assertEquals(Items.MOOSHROOM_SPAWN_EGG, redClawEgg.iconStack().getItem());

      DropEntry nullAtom = DropCatalog.byId("null_atom");
      assertNotNull(nullAtom);
      assertEquals(Items.OAK_BUTTON, nullAtom.iconStack().getItem());
      assertTrue(nullAtom.look.glint);

      DropEntry shimmeringWool = DropCatalog.byId("shimmering_wool");
      assertNotNull(shimmeringWool);
      assertEquals(
         Identifier.fromNamespaceAndPath("minecraft", "yellow_wool"),
         BuiltInRegistries.ITEM.getKey(shimmeringWool.iconStack().getItem())
      );
      assertTrue(shimmeringWool.look.glint);

      DropEntry antiqueRemedies = DropCatalog.byId("antique_remedies");
      assertNotNull(antiqueRemedies);
      assertEquals(Items.AZURE_BLUET, antiqueRemedies.iconStack().getItem());
      assertTrue(antiqueRemedies.look.glint);

      // Vampire Slayer has its own dedicated Category.VAMPIRE tab with 8 drops
      assertEquals(8, DropCatalog.byCategory(Category.VAMPIRE).size());

      // Summoning Eye lives in DRAGONS and Arachne's Fang lives in TARANTULA
      assertEquals(Category.DRAGONS, DropCatalog.byId("summoning_eye").category);
      assertEquals(Category.TARANTULA, DropCatalog.byId("arachnes_fang").category);

      // Verified Hypixel glowing vanilla items must carry enchantment glint
      String[] glowingIds = new String[]{
         "overflux_capacitor", "daedalus_stick", "necrons_handle",
         "implosion_scroll", "shadow_warp_scroll", "wither_shield_scroll",
         "great_white_shark_tooth", "vampire_fang", "sorrow",
         "crown_of_greed", "braided_griffin_feather"
      };
      for (String id : glowingIds) {
         DropEntry d = DropCatalog.byId(id);
         assertNotNull(d, "Missing glowing entry: " + id);
         assertTrue(d.look.glint, "Entry " + id + " must have glint=true matching Hypixel API");
      }
   }

   @Test
   void testNoDuplicatesAndStrictRarityOrdering() {
      DropCatalog.byCategory(Category.REVENANT); // ensure catalog is initialized
      Set<String> seenIds = new HashSet<>();
      Set<String> seenNames = new HashSet<>();
      for (DropEntry d : DropCatalog.ALL) {
         assertTrue(seenIds.add(d.id), "Duplicate drop id: " + d.id);
         assertTrue(seenNames.add(d.name.toLowerCase()), "Duplicate display name: " + d.name);
         assertFalse(d.fixedRarity, "Drop " + d.id + " must keep AUTO rarity mode (!fixedRarity)");
      }
      assertEquals(413, DropCatalog.ALL.size(), "Expected 413 deduplicated catalog drops");

      for (Category cat : Category.values()) {
         if (cat == Category.GENERAL) continue;
         int prevRank = -1;
         String prevName = "";
         for (DropEntry d : DropCatalog.byCategory(cat)) {
            if (d.section != null && !d.section.isEmpty()) {
               prevRank = -1;
               prevName = "";
            }
            int rank = rarityOrderRank(d.defaultRarity);
            if (prevRank != -1) {
               assertTrue(rank >= prevRank,
                  "Rarity order violation in " + cat + ": " + d.name + " (" + d.defaultRarity + ") after " + prevName);
               if (rank == prevRank) {
                  assertTrue(d.name.compareToIgnoreCase(prevName) >= 0,
                     "A-Z order violation in " + cat + " (" + d.defaultRarity + "): " + d.name + " after " + prevName);
               }
            }
            prevRank = rank;
            prevName = d.name;
         }
      }
   }

   private static int rarityOrderRank(Rarity r) {
      return switch (r) {
         case DIVINE -> 0;
         case MYTHIC -> 1;
         case LEGENDARY -> 2;
         case EPIC -> 3;
         case RARE -> 4;
         case UNCOMMON -> 5;
         case COMMON -> 6;
         case SPECIAL -> 7;
         default -> 8;
      };
   }
}
