package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Map;
import java.util.regex.Pattern;

@Entity
@Table(name = "genres")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer genreId;

    @Column(nullable = false, length = 100)
    private String name;

    private static final Pattern MOJIBAKE_PATTERN = Pattern.compile("(Ã.|Â.|Ä.|á»|áº|Æ|Ð|Ñ|�)");
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Map<String, String> GENRE_CANONICAL_MAP = Map.of(
            "hanh dong", "Hành động",
            "kinh di", "Kinh dị",
            "hoat hinh", "Hoạt hình",
            "tinh cam", "Tình cảm"
    );

    public String getName() {
        return normalizeVietnamese(name);
    }

    public void setName(String name) {
        this.name = normalizeVietnamese(name);
    }

    private static String normalizeVietnamese(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String normalized = value;
        if (MOJIBAKE_PATTERN.matcher(normalized).find()) {
            try {
                normalized = new String(normalized.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            } catch (Exception ignored) {
                // keep original text if decoding fails
            }
        }

        String canonical = toCanonical(normalized);
        if (GENRE_CANONICAL_MAP.containsKey(canonical)) {
            return GENRE_CANONICAL_MAP.get(canonical);
        }

        // Fallback for records that already lost bytes and became replacement chars.
        String lower = normalized.toLowerCase();
        if (lower.startsWith("hành") && normalized.contains("�")) {
            return "Hành động";
        }
        if (lower.startsWith("kinh d") && normalized.contains("�")) {
            return "Kinh dị";
        }
        return normalized;
    }

    private static String toCanonical(String value) {
        String nfd = Normalizer.normalize(value, Normalizer.Form.NFD);
        String noMarks = nfd.replaceAll("\\p{M}+", "").toLowerCase();
        return NON_ALNUM.matcher(noMarks).replaceAll(" ").trim();
    }
}
