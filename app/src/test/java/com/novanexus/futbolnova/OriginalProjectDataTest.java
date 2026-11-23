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

    @Test
    public void parsesPlayableRosterWithRealPlayerIdentityAndAttributes() {
        StringBuilder json = new StringBuilder("{\"rosters\":[{\"club_id\":\"CUB-S3-001\",\"club_name\":\"Guayacán de Viñales\",\"competition_id\":\"cuba_tercera_division_nova\",\"division_level\":3,\"players\":[");
        String[] positions={"POR","LI","DFC","DFC","LD","MCD","MC","MCO","EI","DC","ED"};
        for(int i=0;i<positions.length;i++){
            if(i>0)json.append(',');
            json.append("{\"id\":\"P").append(i).append("\",\"name\":\"Jugador ").append(i)
                    .append("\",\"position\":\"").append(positions[i])
                    .append("\",\"estimated_overall\":").append(60+i)
                    .append(",\"attributes\":[70,80]}");
        }
        json.append("]}]}");
        java.util.List<OriginalProjectData.TeamRoster> rosters=OriginalProjectData.parsePlayableRosters(json.toString());
        assertEquals(1,rosters.size());
        assertEquals("CUB-S3-001",rosters.get(0).clubId);
        assertEquals("Guayacán de Viñales",rosters.get(0).clubName);
        assertEquals(11,rosters.get(0).players.size());
        assertEquals("Jugador 0",rosters.get(0).players.get(0).name);
        assertEquals(70f,rosters.get(0).players.get(0).attribute(0),0.001f);
    }
}