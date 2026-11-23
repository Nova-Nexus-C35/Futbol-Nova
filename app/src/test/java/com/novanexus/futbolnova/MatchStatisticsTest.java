package com.novanexus.futbolnova;

import org.junit.Test;
import static org.junit.Assert.*;

public class MatchStatisticsTest {
    @Test public void recordsIndependentTeamCountersAndEvents(){
        MatchStatistics stats=new MatchStatistics();
        stats.recordShot(true);stats.recordShot(false);stats.recordPassAttempt(true);
        stats.recordTackle(false);stats.recordFoul(false);stats.recordSave(true);stats.recordGoal(true);
        assertEquals(1,stats.getShots(true));assertEquals(1,stats.getShots(false));
        assertEquals(1,stats.getPasses(true));assertEquals(1,stats.getTackles(false));
        assertEquals(1,stats.getFouls(false));assertEquals(1,stats.getSaves(true));
        assertEquals(1,stats.getGoals(true));assertEquals(7,stats.getEvents().size());
        assertEquals(MatchStatistics.EventType.GOAL,stats.getEvents().get(6).type);
    }
    @Test public void eventLedgerIsBoundedAndCountersRemainComplete(){
        MatchStatistics stats=new MatchStatistics();
        for(int i=0;i<300;i++)stats.recordShot(true);
        assertEquals(300,stats.getShots(true));assertEquals(256,stats.getEvents().size());
    }
    @Test public void resetClearsCountersAndEvents(){
        MatchStatistics stats=new MatchStatistics();stats.recordGoal(true);stats.recordPassAttempt(false);
        stats.reset();
        assertEquals(0,stats.getGoals(true));assertEquals(0,stats.getPasses(false));
        assertTrue(stats.getEvents().isEmpty());
    }

    @Test public void recordsOffsideAgainstTheOffendingTeam(){
        MatchStatistics stats=new MatchStatistics();
        stats.recordOffside(true);
        assertEquals(1,stats.getOffsides(true));
        assertEquals(0,stats.getOffsides(false));
        assertEquals(MatchStatistics.EventType.OFFSIDE,stats.getEvents().get(0).type);
        stats.reset();
        assertEquals(0,stats.getOffsides(true));
    }

    @Test public void tracksIndividualPlayersBeyondTheBoundedEventWindow(){
        MatchStatistics stats=new MatchStatistics();
        for(int i=0;i<300;i++)stats.recordShot("player-7",true);
        stats.recordPassAttempt("player-7",true);
        stats.recordTackle("player-9",false);
        assertEquals(300,stats.getPlayerCounters("player-7").getShots());
        assertEquals(1,stats.getPlayerCounters("player-7").getPasses());
        assertEquals(1,stats.getPlayerCounters("player-9").getTackles());
        assertEquals(256,stats.getEvents().size());
        assertEquals("player-7",stats.getEvents().get(255).playerId);
        stats.reset();
        assertNull(stats.getPlayerCounters("player-7"));
        assertTrue(stats.getPlayers().isEmpty());
    }
}
