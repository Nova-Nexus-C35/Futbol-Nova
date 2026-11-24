package com.novanexus.futbolnova;

import org.junit.Test;
import static org.junit.Assert.*;

public class MatchDisciplineTest {
    @Test public void firstYellowCautionsButAllowsParticipation(){
        MatchDiscipline discipline=new MatchDiscipline();
        assertEquals(MatchDiscipline.Outcome.CAUTION,discipline.issueYellow("home_04"));
        assertEquals(1,discipline.getYellowCards("home_04"));
        assertTrue(discipline.canParticipate("home_04"));
    }

    @Test public void secondYellowDismissesAndPreventsFurtherCards(){
        MatchDiscipline discipline=new MatchDiscipline();
        assertEquals(MatchDiscipline.Outcome.CAUTION,discipline.issueYellow("away_08"));
        assertEquals(MatchDiscipline.Outcome.DISMISSED,discipline.issueYellow("away_08"));
        assertFalse(discipline.canParticipate("away_08"));
        assertTrue(discipline.isSentOff("away_08"));
        assertEquals(MatchDiscipline.Outcome.ALREADY_DISMISSED,discipline.issueYellow("away_08"));
        assertEquals(2,discipline.getYellowCards("away_08"));
    }

    @Test public void thirdFoulTriggersCautionAndResetsInfringementCounter(){
        MatchDiscipline discipline=new MatchDiscipline();
        assertEquals(MatchDiscipline.Outcome.NO_CARD,discipline.recordFoul("home_05"));
        assertEquals(MatchDiscipline.Outcome.NO_CARD,discipline.recordFoul("home_05"));
        assertEquals(2,discipline.getRecord("home_05").foulsSinceCaution);
        assertEquals(MatchDiscipline.Outcome.CAUTION,discipline.recordFoul("home_05"));
        assertEquals(1,discipline.getYellowCards("home_05"));
        assertEquals(0,discipline.getRecord("home_05").foulsSinceCaution);
        assertEquals(MatchDiscipline.Outcome.NO_CARD,discipline.recordFoul("home_05"));
        assertEquals(MatchDiscipline.Outcome.NO_CARD,discipline.recordFoul("home_05"));
        assertEquals(MatchDiscipline.Outcome.DISMISSED,discipline.recordFoul("home_05"));
        assertTrue(discipline.isSentOff("home_05"));
    }

    @Test public void cardEventsKeepReasonAndMatchTimeInOrder(){
        MatchDiscipline discipline=new MatchDiscipline();
        assertEquals(MatchDiscipline.Outcome.CAUTION,
                discipline.issueYellow("home_04",MatchDiscipline.Reason.DISSENT,125f));
        assertEquals(MatchDiscipline.Outcome.DISMISSED,
                discipline.issueRed("away_02",MatchDiscipline.Reason.VIOLENT_CONDUCT,180f));
        assertEquals(2,discipline.snapshotEvents().size());
        MatchDiscipline.CardEvent caution=discipline.snapshotEvents().get(0);
        assertEquals(MatchDiscipline.Card.YELLOW,caution.card);
        assertEquals(MatchDiscipline.Reason.DISSENT,caution.reason);
        assertEquals(125f,caution.elapsedSeconds,0.001f);
        assertFalse(caution.dismissed);
        MatchDiscipline.CardEvent red=discipline.snapshotEvents().get(1);
        assertEquals(MatchDiscipline.Card.RED,red.card);
        assertEquals(MatchDiscipline.Reason.VIOLENT_CONDUCT,red.reason);
        assertTrue(red.dismissed);
        try{discipline.snapshotEvents().clear();fail("event history must be immutable");}
        catch(UnsupportedOperationException expected){/* immutable by contract */}
    }

    @Test public void persistentInfringementEventRecordsMatchTime(){
        MatchDiscipline discipline=new MatchDiscipline();
        discipline.recordFoul("home_05",10f);
        discipline.recordFoul("home_05",20f);
        assertEquals(MatchDiscipline.Outcome.CAUTION,discipline.recordFoul("home_05",30f));
        MatchDiscipline.CardEvent event=discipline.snapshotEvents().get(0);
        assertEquals(MatchDiscipline.Reason.PERSISTENT_INFRINGEMENT,event.reason);
        assertEquals(30f,event.elapsedSeconds,0.001f);
    }

    @Test public void directRedDismissesWithoutYellow(){
        MatchDiscipline discipline=new MatchDiscipline();
        assertEquals(MatchDiscipline.Outcome.DISMISSED,discipline.issueRed("home_03"));
        assertEquals(0,discipline.getYellowCards("home_03"));
        assertFalse(discipline.canParticipate("home_03"));
    }

    @Test public void invalidIdsAndSnapshotsAreSafe(){
        MatchDiscipline discipline=new MatchDiscipline();
        assertEquals(MatchDiscipline.Outcome.INVALID_PLAYER,discipline.issueYellow(" "));
        assertNull(discipline.getRecord(null));
        discipline.issueYellow("home_01");
        assertEquals(1,discipline.snapshot().get("home_01").yellowCards);
        try{discipline.snapshot().clear();fail("snapshot must be immutable");}
        catch(UnsupportedOperationException expected){/* immutable by contract */}
    }

    @Test public void resetClearsCardsAndDismissals(){
        MatchDiscipline discipline=new MatchDiscipline();
        discipline.issueRed("away_01");
        discipline.reset();
        assertTrue(discipline.canParticipate("away_01"));
        assertFalse(discipline.isSentOff("away_01"));
        assertTrue(discipline.snapshot().isEmpty());
    }
    @Test public void snapshotRestoresCardsEventsAndDismissalsAtomically(){
        MatchDiscipline source=new MatchDiscipline();source.issueYellow("home_04",MatchDiscipline.Reason.DISSENT,80f);
        source.issueRed("away_02",MatchDiscipline.Reason.VIOLENT_CONDUCT,125f);
        MatchDiscipline restored=new MatchDiscipline();assertTrue(restored.restore(source.snapshotState()));
        assertEquals(1,restored.getYellowCards("home_04"));assertTrue(restored.isSentOff("away_02"));
        assertEquals(2,restored.snapshotEvents().size());
        MatchDiscipline.Snapshot invalid=source.snapshotState();
        assertFalse(restored.restore(null));assertTrue(restored.isSentOff("away_02"));
    }

