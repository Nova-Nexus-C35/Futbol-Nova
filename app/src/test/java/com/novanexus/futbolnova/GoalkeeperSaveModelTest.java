package com.novanexus.futbolnova;

import org.junit.Test;
import static org.junit.Assert.*;

public final class GoalkeeperSaveModelTest {
    @Test public void predictsAReachableShotAndChoosesCatchForSlowLowBall() {
        GoalkeeperSaveModel.Plan plan=GoalkeeperSaveModel.evaluate(
                0f,49f,0.2f,1.2f,45f,0.2f,4f,10f,1f);
        assertNotNull(plan);
        assertTrue(plan.time>0f&&plan.time<=1.2f);
        assertEquals(GoalkeeperSaveModel.Action.DEFLECT,plan.action);
        assertTrue(plan.targetZ<52.5f);
    }

    @Test public void choosesCatchForControllableLowShot() {
        GoalkeeperSaveModel.Plan plan=GoalkeeperSaveModel.evaluate(
                0f,49f,0f,0.7f,48f,0f,5f,4f,1f);
        assertNotNull(plan);
        assertEquals(GoalkeeperSaveModel.Action.CATCH,plan.action);
    }

    @Test public void rejectsUnreachableOrInvalidShots() {
        assertNull(GoalkeeperSaveModel.evaluate(0f,49f,3.5f,1f,45f,0f,0f,10f,1f));
        assertNull(GoalkeeperSaveModel.evaluate(0f,49f,0f,1f,45f,0f,0f,-10f,1f));
        assertNull(GoalkeeperSaveModel.evaluate(Float.NaN,49f,0f,1f,45f,0f,0f,10f,1f));
        assertNull(GoalkeeperSaveModel.evaluate(0f,49f,0f,0.5f,45f,0f,0f,10f,1f));
    }

    @Test public void supportsBothGoalDirectionsAndLateralDives() {
        GoalkeeperSaveModel.Plan positive=GoalkeeperSaveModel.evaluate(
                0f,49f,1f,1.1f,45f,0f,4.5f,10f,1f);
        GoalkeeperSaveModel.Plan negative=GoalkeeperSaveModel.evaluate(
                0f,-49f,-1f,1.1f,-45f,0f,4.5f,-10f,-1f);
        assertNotNull(positive);assertNotNull(negative);
        assertEquals(1f,positive.diveDirection,0.0001f);
        assertEquals(-1f,negative.diveDirection,0.0001f);
        assertEquals(-1f,negative.targetX<0f?-1f:1f,0.0001f);
    }
}
