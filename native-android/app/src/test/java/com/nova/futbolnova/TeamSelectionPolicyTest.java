package com.nova.futbolnova;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public final class TeamSelectionPolicyTest {
    private static final class FakeTeam {
        final String id;
        FakeTeam(String id) { this.id = id; }
    }

    @Test public void excludesTeamAlreadySelectedOnOppositeSide() {
        FakeTeam home = new FakeTeam("FN-NOVA-C35");
        FakeTeam away = new FakeTeam("FN-NOVA-RDA");
        FakeTeam third = new FakeTeam("FN-NOVA-GEN");
        List<FakeTeam> candidates = TeamSelectionPolicy.excludingId(
            Arrays.asList(home, away, third), team -> team.id, away.id
        );
        assertEquals(2, candidates.size());
        assertTrue(candidates.contains(home));
        assertTrue(candidates.contains(third));
    }

    @Test public void emptyExclusionKeepsAllValidTeams() {
        FakeTeam first = new FakeTeam("FN-NOVA-C35");
        FakeTeam second = new FakeTeam("FN-NOVA-RDA");
        assertEquals(2, TeamSelectionPolicy.excludingId(
            Arrays.asList(first, second), team -> team.id, ""
        ).size());
    }

    @Test public void emptyInputReturnsNoCandidates() {
        assertTrue(TeamSelectionPolicy.excludingId(
            Collections.<FakeTeam>emptyList(), team -> team.id, "FN-NOVA-C35"
        ).isEmpty());
    }
}
