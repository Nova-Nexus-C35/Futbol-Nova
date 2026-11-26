package com.novanexus.futbolnova;

import org.junit.Test;
import static org.junit.Assert.*;

public class PlayerAnimationPolicyTest {
    @Test public void locomotionUsesStableSpeedBands() {
        assertEquals("idle", PlayerAnimationPolicy.locomotionHint(0f));
        assertEquals("idle", PlayerAnimationPolicy.locomotionHint(0.25f));
        assertEquals("walk", PlayerAnimationPolicy.locomotionHint(1.2f));
        assertEquals("sprint", PlayerAnimationPolicy.locomotionHint(3.1f));
        assertEquals("idle", PlayerAnimationPolicy.locomotionHint(Float.NaN));
    }

    @Test public void tackleChoosesSlideOnlyAtHighApproachSpeed() {
        assertEquals("standing_tackle", PlayerAnimationPolicy.tackleHint(2.99f));
        assertEquals("slide_tackle", PlayerAnimationPolicy.tackleHint(3.0f));
        assertEquals("standing_tackle", PlayerAnimationPolicy.tackleHint(Float.POSITIVE_INFINITY));
    }

    @Test public void aerialControlSelectsChestOrHeaderByBallHeight() {
        assertNull(PlayerAnimationPolicy.aerialControlHint(0.6f));
        assertEquals("soccer_chest_control", PlayerAnimationPolicy.aerialControlHint(0.8f));
        assertEquals("soccer_header", PlayerAnimationPolicy.aerialControlHint(1.05f));
        assertNull(PlayerAnimationPolicy.aerialControlHint(1.11f));
        assertNull(PlayerAnimationPolicy.aerialControlHint(Float.NaN));
    }

    @Test public void goalkeeperActionHandlesCatchPunchAndBothDiveSides() {
        assertEquals("keeper_catch", PlayerAnimationPolicy.goalkeeperSaveHint(true, 1f));
        assertEquals("keeper_punch", PlayerAnimationPolicy.goalkeeperSaveHint(false, 0f));
        assertEquals("keeper_dive_left", PlayerAnimationPolicy.goalkeeperSaveHint(false, -1f));
        assertEquals("keeper_dive_right", PlayerAnimationPolicy.goalkeeperSaveHint(false, 1f));
    }
}