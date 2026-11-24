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
        assertEquals(1,stats.getGoals(true));assertEquals(1,stats.getShotsOnTarget(true));
        assertEquals(1,stats.getShotsOnTarget(false));assertEquals(7,stats.getEvents().size());
        assertEquals(MatchStatistics.EventType.GOAL,stats.getEvents().get(6).type);
    }
    @Test public void eventLedgerIsBoundedAndCountersRemainComplete(){
        MatchStatistics stats=new MatchStatistics();
        for(int i=0;i<300;i++)stats.recordShot(true);
        assertEquals(300,stats.getShots(true));assertEquals(256,stats.getEvents().size());
    }
    @Test public void tracksCompletedPassesAndAccuracyPerTeamAndPlayer(){
        MatchStatistics stats=new MatchStatistics();
        stats.recordPassAttempt("home_06",true);
        stats.recordPassAttempt("home_06",true);
        stats.recordPassCompleted("home_06",true);
        stats.recordPassAttempt("away_08",false);
        stats.recordPassCompleted("away_08",false);
        assertEquals(2,stats.getPasses(true));
        assertEquals(1,stats.getCompletedPasses(true));
        assertEquals(50f,stats.getPassAccuracyPercent(true),0.001f);
        assertEquals(100f,stats.getPassAccuracyPercent(false),0.001f);
        assertEquals(1,stats.getPlayerCounters("home_06").getCompletedPasses());
        assertEquals(MatchStatistics.EventType.PASS_COMPLETED,stats.getEvents().get(2).type);
        stats.reset();
        assertEquals(0,stats.getCompletedPasses(true));
        assertEquals(0f,stats.getPassAccuracyPercent(true),0.001f);
    }

    @Test public void eventLedgerStoresMatchClockTimeAndSanitizesInvalidTime(){
        MatchStatistics stats=new MatchStatistics();
        stats.recordPassAttempt("home_06",true,127.5f);
        stats.recordGoal("home_09",true,Float.NaN);
        assertEquals(127.5f,stats.getEvents().get(0).elapsedSeconds,0.001f);
        assertEquals(0f,stats.getEvents().get(1).elapsedSeconds,0.001f);
        stats.reset();
        assertTrue(stats.getEvents().isEmpty());
    }

    @Test public void possessionTimeProducesBalancedPercentagesAndResets(){
        MatchStatistics stats=new MatchStatistics();
        stats.recordPossessionSeconds(30f,true);
        stats.recordPossessionSeconds(10f,false);
        stats.recordPossessionSeconds(Float.NaN,true);
        stats.recordPossessionSeconds(-2f,false);
        assertEquals(30f,stats.getPossessionSeconds(true),0.001f);
        assertEquals(10f,stats.getPossessionSeconds(false),0.001f);
        assertEquals(75f,stats.getPossessionPercent(true),0.001f);
        assertEquals(25f,stats.getPossessionPercent(false),0.001f);
        stats.reset();
        assertEquals(0f,stats.getPossessionSeconds(true),0.001f);
        assertEquals(0f,stats.getPossessionPercent(false),0.001f);
    }

    @Test public void resetClearsCountersAndEvents(){
        MatchStatistics stats=new MatchStatistics();stats.recordGoal(true);stats.recordPassAttempt(false);
        stats.reset();
        assertEquals(0,stats.getGoals(true));assertEquals(0,stats.getPasses(false));
        assertEquals(0,stats.getShotsOnTarget(true));
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
        assertEquals("player-7",stats.getEvents().get(254).playerId);
        assertEquals("player-9",stats.getEvents().get(255).playerId);
        stats.reset();
        assertNull(stats.getPlayerCounters("player-7"));
        assertTrue(stats.getPlayers().isEmpty());
    }
    @Test public void snapshotRestoresCountersPossessionPlayersAndEventsAtomically(){
        MatchStatistics source=new MatchStatistics();source.recordShot("home_09",true,12f);
        source.recordPassAttempt("home_09",true,15f);source.recordPassCompleted("home_09",true,16f);
        source.recordGoal("home_09",true,20f);source.recordPossessionSeconds(40f,true);
        MatchStatistics restored=new MatchStatistics();assertTrue(restored.restore(source.snapshot()));
        assertEquals(1,restored.getShots(true));assertEquals(1,restored.getGoals(true));
        assertEquals(40f,restored.getPossessionSeconds(true),0.001f);
        assertEquals(4,restored.getEvents().size());
        assertEquals(20f,restored.getEvents().get(3).elapsedSeconds,0.001f);
        assertEquals(1,restored.getPlayerCounters("home_09").getCompletedPasses());
        assertFalse(restored.restore(null));assertEquals(1,restored.getGoals(true));
    }

