package com.novanexus.futbolnova;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/** Deterministic pre-match selector over squads that are actually available to play. */
public final class TeamSelection {
    private final List<OriginalProjectData.TeamRoster> teams;
    private int homeIndex;
    private int awayIndex;

    public TeamSelection(List<OriginalProjectData.TeamRoster> teams) {
        if (teams == null) throw new IllegalArgumentException("Team list is required");
        List<OriginalProjectData.TeamRoster> valid = new ArrayList<>();
        Set<String> seenClubIds = new HashSet<>();
        for (OriginalProjectData.TeamRoster team : teams) {
            if (team != null && team.clubId != null && !team.clubId.isEmpty()
                    && team.players != null && team.players.size() >= 11
                    && seenClubIds.add(team.clubId)) valid.add(team);
        }
        if (valid.size() < 2) throw new IllegalArgumentException("At least two playable teams are required");
        this.teams = Collections.unmodifiableList(valid);
        homeIndex = 0;
        awayIndex = 1;
    }

    public OriginalProjectData.TeamRoster getHome() { return teams.get(homeIndex); }
    public OriginalProjectData.TeamRoster getAway() { return teams.get(awayIndex); }
    public int getAvailableTeamCount() { return teams.size(); }

    public boolean cycleHome() {
        int next = nextDistinct(homeIndex, awayIndex);
        if (next == homeIndex) return false;
        homeIndex = next;
        return true;
    }

    public boolean cycleAway() {
        int next = nextDistinct(awayIndex, homeIndex);
        if (next == awayIndex) return false;
        awayIndex = next;
        return true;
    }

    private int nextDistinct(int current, int other) {
        for (int offset = 1; offset <= teams.size(); offset++) {
            int candidate = (current + offset) % teams.size();
            if (candidate != other && !teams.get(candidate).clubId.equals(teams.get(other).clubId))
                return candidate;
        }
        return current;
    }
}
