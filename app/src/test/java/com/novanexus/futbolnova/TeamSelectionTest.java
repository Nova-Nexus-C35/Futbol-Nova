package com.novanexus.futbolnova;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public final class TeamSelectionTest {
    private static OriginalProjectData.TeamRoster team(String id, String name) {
        List<OriginalProjectData.PlayerRecord> players = new ArrayList<>();
        String[] positions={"POR","LI","DFC","DFC","LD","MCD","MC","MCO","EI","DC","ED"};
        for(int i=0;i<positions.length;i++)
            players.add(new OriginalProjectData.PlayerRecord(id+"-"+i,name+" Player "+i,
                    positions[i],60,new float[]{70f,70f}));
        return new OriginalProjectData.TeamRoster(id,name,"test-league",3,players);
    }

    @Test public void startsWithTwoDifferentPlayableTeams() {
        TeamSelection selection=new TeamSelection(Arrays.asList(
                team("A","Alpha"),team("B","Bravo"),team("C","Charlie")));
        assertEquals("A",selection.getHome().clubId);
        assertEquals("B",selection.getAway().clubId);
        assertEquals(3,selection.getAvailableTeamCount());
    }

    @Test public void cyclingEitherSideNeverSelectsTheOpponent() {
        TeamSelection selection=new TeamSelection(Arrays.asList(
                team("A","Alpha"),team("B","Bravo"),team("C","Charlie")));
        for(int i=0;i<10;i++){
            assertTrue(selection.cycleHome());
            assertNotEquals(selection.getHome().clubId,selection.getAway().clubId);
            assertTrue(selection.cycleAway());
            assertNotEquals(selection.getHome().clubId,selection.getAway().clubId);
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void requiresAtLeastTwoPlayableSquads() {
        new TeamSelection(Arrays.asList(team("A","Alpha")));
    }
}
