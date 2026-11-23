package com.novanexus.futbolnova;

import org.junit.Test;
import static org.junit.Assert.*;

public final class MatchAdvantageTest {
    @Test public void keepsPlayGoingWhileFouledTeamRetainsPossession() {
        MatchAdvantage advantage = new MatchAdvantage();
        assertTrue(advantage.begin(false, 12f, -8f, true));
        assertEquals(MatchAdvantage.Outcome.CONTINUE, advantage.update(1f, 0));
        assertTrue(advantage.isActive());
        assertEquals(12f, advantage.getFoulX(), 0.001f);
        assertEquals(-8f, advantage.getFoulZ(), 0.001f);
    }

    @Test public void awardsFoulImmediatelyWhenOpponentWinsPossession() {
        MatchAdvantage advantage = new MatchAdvantage();
        assertTrue(advantage.begin(true, -4f, 21f, false));
        assertEquals(MatchAdvantage.Outcome.AWARD_FREE_KICK, advantage.update(0.1f, 0));
        assertFalse(advantage.isActive());
    }

    @Test public void confirmsAdvantageWhenFouledTeamStillHasBallAtWindowEnd() {
        MatchAdvantage advantage = new MatchAdvantage();
        assertTrue(advantage.begin(true, 0f, 0f, false));
        assertEquals(MatchAdvantage.Outcome.CONTINUE, advantage.update(2f, 1));
        assertEquals(MatchAdvantage.Outcome.PLAY_ON, advantage.update(0.5f, 1));
        assertFalse(advantage.isActive());
    }

    @Test public void awardsFoulWhenWindowEndsWithLooseBall() {
        MatchAdvantage advantage = new MatchAdvantage();
        assertTrue(advantage.begin(false, 3f, 5f, true));
        assertEquals(MatchAdvantage.Outcome.AWARD_FREE_KICK, advantage.update(2.5f, MatchAdvantage.NO_TEAM));
        assertFalse(advantage.isActive());
    }

    @Test public void rejectsInvalidFoulAndIgnoresInvalidDelta() {
        MatchAdvantage advantage = new MatchAdvantage();
        assertFalse(advantage.begin(true, Float.NaN, 0f, false));
        assertTrue(advantage.begin(true, 1f, 2f, false));
        assertEquals(MatchAdvantage.Outcome.CONTINUE, advantage.update(Float.NaN, 1));
        assertEquals(MatchAdvantage.Outcome.CONTINUE, advantage.update(-1f, 1));
        assertEquals(0f, advantage.getElapsedSeconds(), 0.001f);
        assertFalse(advantage.begin(false, 1f, 2f, true));
    }
}
