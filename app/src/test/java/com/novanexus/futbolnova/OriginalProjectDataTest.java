package com.novanexus.futbolnova;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public final class OriginalProjectDataTest {
    @Test
    public void parsesOriginalCubanLeagueAndPlayerData() {
        String leagues = "{\"schema_version\":\"1.2.0\",\"competitions\":[{\"clubs\":[{\"id\":\"CUB-1\"},{\"id\":\"CUB-2\"}]},{\"clubs\":[{\"id\":\"CUB-3\"}]}]}";
        String players = "{\"player_count\":22,\"rosters\":[{},{}]}";
        String custom = "{\"players\":[{},{}]}";
        OriginalProjectData.Summary result = OriginalProjectData.summarize(leagues, players, custom);
        assertEquals("1.2.0", result.cubaLeagueSchemaVersion);
        assertEquals(2, result.cubaCompetitions);
        assertEquals(3, result.cubaClubs);
        assertEquals(2, result.cubaRosterGroups);
        assertEquals(22, result.cubaPlayers);
        assertEquals(2, result.customPlayers);
    }
}
