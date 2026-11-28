package com.nova.futbolnova;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Pure selection policy shared by the Android pre-match UI and JVM unit tests. */
final class TeamSelectionPolicy {
    private TeamSelectionPolicy() {}

    static <T> List<T> excludingId(List<T> teams, Function<T, String> idProvider, String excludedId) {
        List<T> candidates = new ArrayList<>();
        if (teams == null || idProvider == null) return candidates;
        for (T team : teams) {
            if (team == null) continue;
            String id = idProvider.apply(team);
            if (id == null || id.trim().isEmpty()) continue;
            if (excludedId == null || excludedId.isEmpty() || !excludedId.equals(id)) candidates.add(team);
        }
        return candidates;
    }
}
