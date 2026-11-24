package com.artic.aia;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class ChatText {
   private static final char SECTION = '§';

   private ChatText() {
   }

   public static String toFormatted(Component text) {
      List<String> codes = new ArrayList<>();
      List<StringBuilder> texts = new ArrayList<>();
      text.visit((style, str) -> {
         if (str != null && !str.isEmpty()) {
            String cx = codeFor(style);
            int n = codes.size();
            if (n > 0 && codes.get(n - 1).equals(cx)) {
               texts.get(n - 1).append(str);
            } else {
               codes.add(cx);
               texts.add(new StringBuilder(str));
            }

            return Optional.empty();
         } else {
            return Optional.empty();
         }
      }, Style.EMPTY);
      StringBuilder out = new StringBuilder();

      for (int i = 0; i < codes.size(); i++) {
         String c = codes.get(i);
         out.append(c).append(texts.get(i));
         if (!c.isEmpty()) {
            out.append(SECTION).append('r');
         }
      }

      return out.toString();
   }

   private static String codeFor(Style style) {
      StringBuilder sb = new StringBuilder();
      TextColor col = style.getColor();
      if (col != null) {
         ChatFormatting f = nearestColour(col.getValue());
         if (f != null) {
            sb.append(f.toString());
         }
      }

      if (style.isBold()) {
         sb.append(SECTION).append('l');
      }

      if (style.isItalic()) {
         sb.append(SECTION).append('o');
      }

      if (style.isUnderlined()) {
         sb.append(SECTION).append('n');
      }

      if (style.isObfuscated()) {
         sb.append(SECTION).append('k');
      }

      if (style.isStrikethrough()) {
         sb.append(SECTION).append('m');
      }

      return sb.toString();
   }

   private static ChatFormatting nearestColour(int rgb) {
      rgb &= 0xFFFFFF;
      ChatFormatting best = null;
      long bestDist = Long.MAX_VALUE;

      for (ChatFormatting f : ChatFormatting.values()) {
         TextColor tc = TextColor.fromLegacyFormat(f);
         if (tc != null) {
            int c = tc.getValue() & 0xFFFFFF;
            if (c == rgb) {
               return f;
            }

            long dr = (long)((c >> 16 & 0xFF) - (rgb >> 16 & 0xFF));
            long dg = (long)((c >> 8 & 0xFF) - (rgb >> 8 & 0xFF));
            long db = (long)((c & 0xFF) - (rgb & 0xFF));
            long d = dr * dr + dg * dg + db * db;
            if (d < bestDist) {
               bestDist = d;
               best = f;
            }
         }
      }

      return best;
   }
}
