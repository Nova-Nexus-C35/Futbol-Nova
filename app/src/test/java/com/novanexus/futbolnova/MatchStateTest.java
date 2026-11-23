package com.novanexus.futbolnova;

import org.junit.Test;
import static org.junit.Assert.*;

public class MatchStateTest {
    @Test public void clockAdvancesOnlyForPositiveFiniteDelta() {
        MatchState state = new MatchState();
        state.update(2f); state.update(-1f); state.update(Float.NaN);
        assertEquals(2f, state.getElapsedSeconds(), 0.0001f);
    }
    @Test public void pauseStopsClock() {
        MatchState state = new MatchState();
        state.setPaused(true); state.update(5f);
        assertEquals(0f, state.getElapsedSeconds(), 0.0001f);
    }
    @Test public void goalsAreAssignedToRequestedSide() {
        MatchState state = new MatchState();
        state.addGoal(true); state.addGoal(false); state.addGoal(true);
        assertEquals(2, state.getHomeGoals());
        assertEquals(1, state.getAwayGoals());
    }

    @Test public void resetClearsScoreAndElapsedTime() {
        MatchState state=new MatchState();
        state.addGoal(true);state.addGoal(false);state.update(12f);
        state.reset();
        assertEquals(0,state.getHomeGoals());assertEquals(0,state.getAwayGoals());
        assertEquals(0f,state.getElapsedSeconds(),0.0001f);assertFalse(state.isPaused());
    }
}