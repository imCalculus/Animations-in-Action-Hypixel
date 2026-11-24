package com.artic.aia;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

public class ChatDetector {
   private static final String CODE = "§[0-9a-fk-orA-FK-OR]";
   private static final Pattern CODES = Pattern.compile(CODE);
   private static final Pattern STYLE_ONLY_PREFIX = Pattern.compile(
      "(?:\\s|" + CODE + "|RNG\\s+METER!|\\[SLAYER\\]|NEW\\s+DISCOVERY!)*",
      Pattern.CASE_INSENSITIVE
   );
   private static final Pattern HAS_STYLE = Pattern.compile(".*§[0-9a-fA-FlL].*", Pattern.DOTALL);
   private static final Pattern NAME_COLOUR = Pattern.compile("§([0-9a-fA-F])");
   private static final Pattern MF_TAIL = Pattern.compile(
      "\\s*(?:\\([^()]*(?:Magic\\s*Find|\\bMF\\b|✯|RNG\\s*Meter|☘|☯|❁)[^()]*\\)|\\[[^\\[\\]]*(?:Magic\\s*Find|\\bMF\\b|✯|RNG\\s*Meter|☘|☯|❁)[^\\[\\]]*\\])\\s*$",
      Pattern.CASE_INSENSITIVE
   );
   private static final Pattern FORMATTED_MF_TAIL = Pattern.compile(
      "(?:\\s|" + CODE + ")*(?:\\([^()]*(?:Magic\\s*Find|\\bMF\\b|✯|RNG\\s*Meter)[^()]*\\)|\\[[^\\[\\]]*(?:Magic\\s*Find|\\bMF\\b|✯|RNG\\s*Meter)[^\\[\\]]*\\])(?:\\s|" + CODE + ")*$",
      Pattern.CASE_INSENSITIVE
   );
   private static final Pattern RNG_METER_PREFIX = Pattern.compile("^(?:(?:RNG\\s+METER!|\\[SLAYER\\]|NEW\\s+DISCOVERY!)\\s*)+", Pattern.CASE_INSENSITIVE);
   private static final Pattern LEADING_JUNK = Pattern.compile("^[\\s:!\\-–—]+");
   private static final Pattern LEADING_SYMBOL = Pattern.compile("^[◆✦✧❁❈❂☠✎⚡✯]+\\s*");
   private static final Pattern OTHER_PLAYER = Pattern.compile(
      "(?:^|\\s)(?:\\[[^\\]]{1,12}\\]\\s*)?([A-Za-z0-9_]{3,16})\\s+(?:has\\s+)?(?:found|got|obtained|received|unlocked|dropped|opened|looted)\\b",
      Pattern.CASE_INSENSITIVE
   );
   private static final Pattern DROP_HEADER = Pattern.compile("^((?:[A-Z]{2,}\\s+){0,3}(?:DROP|CROP|CATCH|FIND|REWARD|CHARM|FEAST|CHEST|FIESTA|LOOT)!)");
   private static final Pattern BURROW_WOW = Pattern.compile("^(WOW!)(?=\\s+You\\s+(?:dug\\s+out|found|dropped)\\b)", Pattern.CASE_INSENSITIVE);
   private static final Pattern HARVEST_BOUNTY = Pattern.compile("^(Harvest\\s+Bounty[:!])", Pattern.CASE_INSENSITIVE);
   private static final Pattern EXCLAMATION_HEADER = Pattern.compile("^(\\[!\\])");
   private static final Pattern PRAY_RNGESUS_HEADER = Pattern.compile("^(PRAY\\s+TO\\s+RNGESUS(?:\\s+DROP)?!)", Pattern.CASE_INSENSITIVE);
   private static final Pattern HOPPITY_HEADER = Pattern.compile("^(HOPPITY['’]?S\\s+HUNT!?|(?:NEW|DUPLICATE)\\s+RABBIT!)", Pattern.CASE_INSENSITIVE);
   private static final Pattern TROPHY_FISH_HEADER = Pattern.compile("^(TROPHY\\s+FISH!)", Pattern.CASE_INSENSITIVE);
   private static final Pattern UNLOCK = Pattern.compile(
      "^(?:\\[[^\\]]{1,16}\\]\\s*)*([A-Za-z0-9_]{3,16})\\s+(?:has\\s+)?(?:unlocked|obtained|received|looted|earned|claimed)\\s+(.+?)\\s*!?\\s*$",
      Pattern.CASE_INSENSITIVE
   );
   private static final Pattern ANY_STYLE = Pattern.compile(".*§[0-9a-fA-FlLrR].*", Pattern.DOTALL);
   private static final Pattern TRAILING_COUNT = Pattern.compile("(?:\\s*\\(\\d+\\))+\\s*$");
   private static final Pattern QTY_PREFIX = Pattern.compile("^(?:\\(\\s*\\d[\\d,]*\\s*[xX]?\\s*\\)|\\d[\\d,]*\\s*[xX])\\s+");
   private static final Pattern LEAD_IN = Pattern.compile(
      "^(?:(?:you\\s+)?(?:dug\\s+out|found|got|obtained|received|caught|fished\\s+up|looted|unearthed|charmed)\\s+)?(?:an?\\s+|the\\s+)?",
      Pattern.CASE_INSENSITIVE
   );
   private static final Pattern QTY_SUFFIX = Pattern.compile("\\s*\\(?\\s*(?:[xX]\\s*\\d[\\d,]*|\\d[\\d,]*\\s*[xX])\\s*\\)?\\s*$");
   private static final Pattern END_PUNCT = Pattern.compile("[\\s.!]+$");
   private final Map<String, Long> recent = new HashMap<>();

   public void onMessage(Component message) {
      if (message != null) {
         try {
            this.handle(ChatText.toFormatted(message));
         } catch (Throwable t) {
            AIAConfig.log("ERROR in ChatDetector: " + t);
         }
      }
   }

   void handle(String formatted) {
      String plain = CODES.matcher(formatted).replaceAll("").trim();
      plain = RNG_METER_PREFIX.matcher(plain).replaceFirst("").trim();
      if (AIAConfig.debug) {
         AIAConfig.log("LINE  " + formatted.replace('§', '&'));
      }

      if (AIAConfig.master && serverAllowed()) {
         Minecraft mc = Minecraft.getInstance();
         String me = mc != null && mc.player != null ? mc.player.getName().getString() : "";
         String header = null;

         for (String h : AIAConfig.headers()) {
            if (plain.startsWith(h)) {
               header = h;
               break;
            }
         }

         if (header == null) {
            Matcher hm = DROP_HEADER.matcher(plain);
            if (hm.find()) {
               header = hm.group(1);
            } else {
               Matcher pm = PRAY_RNGESUS_HEADER.matcher(plain);
               if (pm.find()) {
                  header = pm.group(1);
               } else {
                  Matcher em = EXCLAMATION_HEADER.matcher(plain);
                  if (em.find()) {
                     header = em.group(1);
                  } else {
                     Matcher wm = BURROW_WOW.matcher(plain);
                     if (wm.find()) {
                        header = wm.group(1);
                     } else {
                        Matcher bm = HARVEST_BOUNTY.matcher(plain);
                        if (bm.find()) {
                           header = bm.group(1);
                        } else {
                           Matcher hopm = HOPPITY_HEADER.matcher(plain);
                           if (hopm.find()) {
                              header = hopm.group(1);
                           } else {
                              Matcher tfm = TROPHY_FISH_HEADER.matcher(plain);
                              if (tfm.find()) {
                                 header = tfm.group(1);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         String rest;
         if (header != null) {
            if (AIAConfig.strict) {
               int idx = formatted.indexOf(header);
               if (idx < 0) {
                  reject("header is split by formatting codes");
                  return;
               }

               String prefix = formatted.substring(0, idx);
               if (!STYLE_ONLY_PREFIX.matcher(prefix).matches()) {
                  reject("text before header");
                  return;
               }

               if (!HAS_STYLE.matcher(prefix).matches()) {
                  reject("header carries no colour/bold (not a server line)");
                  return;
               }
            }

            rest = LEADING_JUNK.matcher(plain.substring(header.length())).replaceFirst("").trim();
            Matcher om = OTHER_PLAYER.matcher(rest);
            if (om.find()) {
               String who = om.group(1);
               if (!who.equalsIgnoreCase("you") && !who.equalsIgnoreCase(me)) {
                  reject("line is about another player: " + who);
                  return;
               }

               rest = LEADING_JUNK.matcher(rest.substring(om.end())).replaceFirst("").trim();
            }
         } else {
            Matcher um = UNLOCK.matcher(plain);
            if (!um.find()) {
               return;
            }

            if (AIAConfig.strict && !ANY_STYLE.matcher(formatted).matches()) {
               reject("unlock line has no server formatting");
               return;
            }

            if (!um.group(1).equalsIgnoreCase(me)) {
               reject("someone else unlocked it: " + um.group(1));
               return;
            }

            header = "unlocked";
            rest = um.group(2).trim();
         }

         long now = System.currentTimeMillis();
         Long last = this.recent.get(plain);
         if (last != null && now - last < 2500L) {
            reject("duplicate within 2.5s");
         } else {
            this.recent.put(plain, now);
            if (this.recent.size() > 64) {
               Iterator<Entry<String, Long>> it = this.recent.entrySet().iterator();

               while (it.hasNext()) {
                  if (now - it.next().getValue() > 10000L) {
                     it.remove();
                  }
               }
            }

            String body = cleanBody(rest);
            DropEntry match = null;

            for (DropEntry d : DropCatalog.ALL) {
               if (d.matches(header, body)) {
                  match = d;
                  break;
               }
            }

            if (match == null) {
               if (AIAConfig.debug) {
                  AIAConfig.log("  -> drop line, but no catalog entry matches: " + body);
               }
            } else if (!match.enabled) {
               if (AIAConfig.debug) {
                  AIAConfig.log("  -> matched " + match.id + " but it is switched off");
               }
            } else {
               String name = match.name;
               if (match.category == Category.PETS || match.category == Category.TROPHY_FISH || match.id.endsWith("_chocolate_rabbit")) {
                  name = match.name;
               } else if (!match.book && match.pattern != null) {
                  Matcher nm = match.pattern.matcher(body);
                  if (nm.find()) {
                     String matched = LEADING_SYMBOL.matcher(nm.group().trim()).replaceFirst("").trim();
                     if (!matched.isEmpty() && matched.length() <= 40) {
                        name = matched;
                     }
                  }
               } else if (!match.book && !body.isEmpty() && body.length() <= 40) {
                  name = body;
               }

               Rarity rarity = match.override;
               if (rarity == null && !match.fixedRarity) {
                  if (match.category == Category.TROPHY_FISH) {
                     rarity = match.defaultRarity;
                  } else {
                     rarity = detectRarity(formatted, header, name);
                  }
               }

               if (rarity == null) {
                  rarity = match.defaultRarity;
               }

               if (AIAConfig.debug) {
                  AIAConfig.log("  -> TRIGGER " + match.id + " as \"" + name + "\" (" + rarity + ")");
               }

               AnimationManager.INSTANCE.submit(match, name, rarity, false);
            }
         }
      }
   }

   static String cleanBody(String rest) {
      String b = rest.trim();

      for (int i = 0; i < 4; i++) {
         String before = b;
         b = TRAILING_COUNT.matcher(b).replaceFirst("");
         b = MF_TAIL.matcher(b).replaceFirst("");
         b = b.trim();
         if (b.equals(before)) {
            break;
         }
      }

      while (hasOuterParens(b)) {
         b = b.substring(1, b.length() - 1).trim();
      }

      b = QTY_PREFIX.matcher(b).replaceFirst("").trim();
      b = LEADING_SYMBOL.matcher(b).replaceFirst("").trim();
      b = LEAD_IN.matcher(b).replaceFirst("").trim();
      b = LEADING_SYMBOL.matcher(b).replaceFirst("").trim();
      b = QTY_SUFFIX.matcher(b).replaceFirst("").trim();
      return END_PUNCT.matcher(b).replaceFirst("").trim();
   }

   private static boolean hasOuterParens(String s) {
      if (s.length() < 2 || s.charAt(0) != '(' || s.charAt(s.length() - 1) != ')') {
         return false;
      }

      int depth = 0;
      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         if (c == '(') {
            depth++;
         } else if (c == ')') {
            depth--;
            if (depth == 0 && i < s.length() - 1) {
               return false;
            }
         }
      }

      return depth == 0;
   }

   private static void reject(String why) {
      if (AIAConfig.debug) {
         AIAConfig.log("  -> rejected: " + why);
      }
   }

   static Rarity detectRarity(String formatted, String header, String itemName) {
      String scan = FORMATTED_MF_TAIL.matcher(formatted).replaceFirst("");
      int idx = header == null ? -1 : scan.indexOf(header);
      int start = idx >= 0 ? idx + header.length() : 0;
      int at = itemName == null ? -1 : scan.lastIndexOf(itemName);
      if (at < start) {
         if (idx < 0) {
            return null;
         } else {
            Matcher m = NAME_COLOUR.matcher(scan.substring(start));

            while (m.find()) {
               Rarity r = Rarity.fromCode(m.group(1).charAt(0));
               if (r != null && r != Rarity.COMMON) {
                  return r;
               }
            }

            return null;
         }
      } else {
         char colour = 0;
         String before = scan.substring(start, at);

         for (int i = 0; i + 1 < before.length(); i++) {
            if (before.charAt(i) == '§') {
               char c = Character.toLowerCase(before.charAt(i + 1));
               if (c == 'r') {
                  colour = 0;
               } else if (Rarity.fromCode(c) != null) {
                  colour = c;
               }

               i++;
            }
         }

         if (colour == 0) {
            return null;
         } else {
            Rarity r = Rarity.fromCode(colour);
            return r != null && r != Rarity.COMMON ? r : null;
         }
      }
   }

   private static boolean serverAllowed() {
      String f = AIAConfig.serverFilter == null ? "" : AIAConfig.serverFilter.trim().toLowerCase();
      if (f.isEmpty()) {
         return true;
      } else {
         Minecraft mc = Minecraft.getInstance();
         ServerData sd = mc != null ? mc.getCurrentServer() : null;
         return sd != null && sd.ip != null && sd.ip.toLowerCase().contains(f);
      }
   }
}
