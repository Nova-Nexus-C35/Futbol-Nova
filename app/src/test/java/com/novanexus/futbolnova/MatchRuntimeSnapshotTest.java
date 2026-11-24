package com.novanexus.futbolnova;

import com.badlogic.gdx.utils.Json;
import org.junit.Test;
import static org.junit.Assert.*;

public class MatchRuntimeSnapshotTest {
    private MatchRuntimeSnapshot createSnapshot(){
        MatchRuntimeSnapshot s=new MatchRuntimeSnapshot();
        MatchSimulation simulation=new MatchSimulation();
        assertTrue(simulation.start("home","away"));
        simulation.update(0.04f,2f,4f,0f,0f);
        s.simulation=simulation.snapshot();
        MatchClock clock=new MatchClock();assertTrue(clock.start());clock.update(30f);clock.pause();
        s.clock=clock.snapshot();
        MatchState state=new MatchState();state.addGoal(true);state.update(12f);state.setPaused(true);
        s.matchState=state.snapshot();
        MatchStatistics statistics=new MatchStatistics();
        statistics.recordShot("home_10",true,30f);statistics.recordGoal("home_10",true,30f);
        statistics.recordPossessionSeconds(12f,true);s.statistics=statistics.snapshot();
        MatchDiscipline discipline=new MatchDiscipline();
        discipline.issueYellow("home_01",MatchDiscipline.Reason.DISSENT,30f);
        s.discipline=discipline.snapshotState();
        s.advantage=new MatchAdvantage().snapshot();
        s.ballPosition=new float[]{2f,0.55f,4f};
        s.ballVelocity=new float[]{1f,0f,2f};
        s.ballSpin=new float[]{0f,1f,0f};
        s.controlledPosition=new float[]{0f,0f,0f};
        return s;
    }

    @Test public void completeSnapshotSurvivesJsonRoundTrip(){
        MatchRuntimeSnapshot original=createSnapshot();
        assertTrue(original.isValid());
        Json json=new Json();
        String encoded=json.toJson(original);
        MatchRuntimeSnapshot decoded=json.fromJson(MatchRuntimeSnapshot.class,encoded);
        assertNotNull(decoded);assertTrue(decoded.isValid());
        assertEquals("home",decoded.simulation.homeTeamName);
        assertEquals(1,decoded.matchState.homeGoals);
        assertEquals(1,decoded.statistics.getClass()==MatchStatistics.Snapshot.class?decoded.statistics.goals[0]:-1);
        assertEquals(MatchDiscipline.Card.YELLOW,decoded.discipline.events.get(0).card);
        assertEquals(30f,decoded.clock.elapsedInHalf,0.001f);
    }

    @Test public void malformedRuntimeSnapshotIsRejected(){
        MatchRuntimeSnapshot snapshot=createSnapshot();
        snapshot.ballVelocity[0]=Float.NaN;
        assertFalse(snapshot.isValid());
    }

    @Test public void missingOrIncompatibleSubsystemsAreRejected(){
        MatchRuntimeSnapshot snapshot=createSnapshot();
        snapshot.schemaVersion=9;
        assertFalse(snapshot.isValid());
    }
}
