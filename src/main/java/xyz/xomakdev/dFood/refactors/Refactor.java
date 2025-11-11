package xyz.xomakdev.dFood.refactors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.xomakdev.dFood.utils.ConfigurationUtil;

@Getter
@RequiredArgsConstructor
public class Refactor {
    private final boolean romeMath;
    private final String refDuration;
    private final String refLevel;
    private final String ref;

    public static Refactor loadFromConfig() {
        return new Refactor(
                ConfigurationUtil.getBoolean("refactor.potion_list.rome_math"),
                ConfigurationUtil.getString("refactor.potion_list.ref_duration"),
                ConfigurationUtil.getString("refactor.potion_list.ref_level"),
                ConfigurationUtil.getString("refactor.potion_list.ref")
        );
    }

    public String formatDuration(int durationTicks) {
        int totalSeconds = durationTicks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        if ("MM:SS".equals(refDuration)) {
            return String.format("%d:%02d", minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }

    public String formatLevel(int level) {
        if (romeMath) {
            return toRoman(level + 1);
        }
        return String.valueOf(level + 1);
    }

    private String toRoman(int number) {
        if (number < 1 || number > 10) return String.valueOf(number);

        String[] romanNumerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return romanNumerals[number - 1];
    }

    public String formatEffect(String effectName, int level, int duration) {
        String formattedDuration = formatDuration(duration);
        String formattedLevel = formatLevel(level);

        if (level > 0) {
            return refLevel
                    .replace("%effect%", effectName)
                    .replace("%level%", formattedLevel)
                    .replace("%duration%", formattedDuration);
        } else {
            return ref
                    .replace("%effect%", effectName)
                    .replace("%duration%", formattedDuration);
        }
    }
}