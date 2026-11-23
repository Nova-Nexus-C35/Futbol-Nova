package com.novanexus.futbolnova;

import org.junit.Test;
import static org.junit.Assert.*;

public class MatchReportTest {
    @Test public void finishedMatchSnapshotRoundTripsAllCoreFields(){
        MatchStatistics stats=new MatchStatistics();
        stats.recordShot("home_09",true);stats.recordFoul("away_04",false);
        stats.recordSave("away_01",false);stats.recordOffside("home_11",true);
        MatchDiscipline discipline=new MatchDiscipline();
        discipline.issueYellow("away_04",MatchDiscipline.Reason.PERSISTENT_INFRINGEMENT,1530f);
        MatchReport original=MatchReport.capture("Cuba","Visitante",2,1,5400f,stats,discipline);
        MatchReport restored=MatchReport.fromJson(original.toJson());
        assertNotNull(restored);assertEquals("Cuba",restored.homeTeam);
        assertEquals("Visitante",restored.awayTeam);assertEquals(2,restored.homeGoals);
        assertEquals(1,restored.awayGoals);assertEquals(5400f,restored.elapsedSeconds,0.001f);
        assertEquals(1,restored.homeShots);assertEquals(1,restored.awayFouls);
        assertEquals(1,restored.awaySaves);assertEquals(1,restored.homeOffsides);
        assertEquals(1,restored.cards.size());assertEquals("away_04",restored.cards.get(0).playerId);
        assertEquals("YELLOW",restored.cards.get(0).card);
        assertEquals("PERSISTENT_INFRINGEMENT",restored.cards.get(0).reason);
        assertEquals(1530f,restored.cards.get(0).elapsedSeconds,0.001f);
    }

    @Test public void reportPreservesOrderedGameplayTimelineAcrossJsonRoundTrip(){
        MatchStatistics stats=new MatchStatistics();
        stats.recordPassAttempt("home_06",true);
        stats.recordShot("home_09",true);
        stats.recordSave("away_01",false);
        stats.recordGoal("home_09",true);
        MatchReport original=MatchReport.capture("Cuba","Visitante",1,0,2700f,stats,new MatchDiscipline());
        MatchReport restored=MatchReport.fromJson(original.toJson());
        assertNotNull(restored);
        assertEquals(4,restored.events.size());
        assertEquals("PASS_ATTEMPT",restored.events.get(0).type);
        assertTrue(restored.events.get(0).homeTeam);
        assertEquals("home_06",restored.events.get(0).playerId);
        assertEquals("SHOT",restored.events.get(1).type);
        assertEquals("SAVE",restored.events.get(2).type);
        assertFalse(restored.events.get(2).homeTeam);
        assertEquals("GOAL",restored.events.get(3).type);
        assertEquals("home_09",restored.events.get(3).playerId);
    }

    @Test public void reportCapturesPerPlayerStatistics(){
        MatchStatistics stats=new MatchStatistics();
        stats.recordShot("home_09",true);
        stats.recordShot("home_09",true);
        stats.recordPassAttempt("home_09",true);
        stats.recordGoal("home_09",true);
        stats.recordFoul("away_04",false);
        MatchReport original=MatchReport.capture("Cuba","Visitante",1,0,2700f,stats,new MatchDiscipline());
        MatchReport restored=MatchReport.fromJson(original.toJson());
        assertNotNull(restored);
        assertEquals(2,restored.players.size());
        MatchReport.PlayerRecord scorer=restored.players.get(0);
        assertEquals("home_09",scorer.playerId);
        assertEquals(2,scorer.shots);
        assertEquals(1,scorer.passes);
        assertEquals(1,scorer.goals);
        MatchReport.PlayerRecord fouler=restored.players.get(1);
        assertEquals("away_04",fouler.playerId);
        assertEquals(1,fouler.fouls);
    }

    @Test public void invalidReportJsonIsRejected(){
        assertNull(MatchReport.fromJson(""));
        assertNull(MatchReport.fromJson("{invalid"));
    }
}
