package com.novanexus.futbolnova;

import org.junit.Test;
import static org.junit.Assert.*;

public final class MatchClockTest {
    @Test public void requiresExplicitKickoffAndAdvancesFirstHalf() {
        MatchClock clock = new MatchClock(60f);
        clock.update(10f);
        assertEquals(MatchClock.Phase.PRE_MATCH, clock.getPhase());
        assertTrue(clock.start());
        clock.update(20f);
        assertEquals(20f, clock.getElapsedInHalfSeconds(), 0.001f);
        assertEquals(MatchClock.Phase.FIRST_HALF, clock.getPhase());
    }

    @Test public void reachesHalfTimeAndRequiresExplicitSecondHalfStart() {
        MatchClock clock = new MatchClock(30f);
        assertTrue(clock.start());
        clock.update(40f);
        assertEquals(MatchClock.Phase.HALF_TIME, clock.getPhase());
        assertTrue(clock.isPaused());
        assertFalse(clock.isPlaying());
        assertTrue(clock.startSecondHalf());
        assertEquals(MatchClock.Phase.SECOND_HALF, clock.getPhase());
        assertEquals(0f, clock.getElapsedInHalfSeconds(), 0.001f);
        clock.update(30f);
        assertEquals(MatchClock.Phase.FULL_TIME, clock.getPhase());
    }

    @Test public void addedTimeExtendsOnlyTheCurrentHalf() {
        MatchClock clock = new MatchClock(10f);
        assertTrue(clock.start());
        assertTrue(clock.setAddedTime(5));
        clock.update(12f);
        assertEquals(MatchClock.Phase.FIRST_HALF, clock.getPhase());
        clock.update(3f);
        assertEquals(MatchClock.Phase.HALF_TIME, clock.getPhase());
        assertTrue(clock.startSecondHalf());
        assertTrue(clock.setAddedTime(2));
        clock.update(11f);
        assertEquals(MatchClock.Phase.SECOND_HALF, clock.getPhase());
        clock.update(1f);
        assertEquals(MatchClock.Phase.FULL_TIME, clock.getPhase());
    }

    @Test public void gameplayInputIsDisabledOutsideUnpausedLivePlay() {
        MatchClock clock = new MatchClock(20f);
        assertFalse(clock.canAcceptGameplayInput());
        clock.start();
        assertTrue(clock.canAcceptGameplayInput());
        clock.pause();
        assertFalse(clock.canAcceptGameplayInput());
        clock.resume();
        assertTrue(clock.canAcceptGameplayInput());
        clock.update(20f);
        assertFalse(clock.canAcceptGameplayInput());
    }

    @Test public void pauseResumeAndInvalidDeltaAreSafe() {
        MatchClock clock = new MatchClock(20f);
        clock.start();
        clock.pause();
        clock.update(5f);
        assertEquals(0f, clock.getElapsedInHalfSeconds(), 0.001f);
        clock.resume();
        clock.update(Float.NaN);
        clock.update(-2f);
        clock.update(5f);
        assertEquals(5f, clock.getElapsedInHalfSeconds(), 0.001f);
    }

    @Test public void resetAllowsASecondCleanMatch() {
        MatchClock clock = new MatchClock(5f);
        clock.start();
        clock.update(5f);
        clock.reset();
        assertEquals(MatchClock.Phase.PRE_MATCH, clock.getPhase());
        assertEquals(0f, clock.getTotalElapsedSeconds(), 0.001f);
        assertTrue(clock.start());
    }

    @Test public void snapshotRestoresExactClockAndRejectsInvalidValuesAtomically(){
        MatchClock source=new MatchClock(100f);source.start();source.setAddedTime(5);source.update(42f);
        MatchClock.Snapshot saved=source.snapshot();
        MatchClock restored=new MatchClock(100f);
        assertTrue(restored.restore(saved));
        assertEquals(source.getPhase(),restored.getPhase());
        assertEquals(source.getElapsedInHalfSeconds(),restored.getElapsedInHalfSeconds(),0.001f);
        assertEquals(source.getTotalElapsedSeconds(),restored.getTotalElapsedSeconds(),0.001f);
        MatchClock.Snapshot invalid=new MatchClock.Snapshot(100f,Float.NaN,5f,0f,MatchClock.Phase.FIRST_HALF,false);
        assertFalse(restored.restore(invalid));
        assertEquals(42f,restored.getElapsedInHalfSeconds(),0.001f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidHalfDuration() {
        new MatchClock(Float.NaN);
    }
}
