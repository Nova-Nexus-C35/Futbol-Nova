package com.novanexus.futbolnova;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlayerAnimationTest {
    @Test public void idlePlayerHasNoGaitOrBodyBob() {
        assertEquals(0f,PlayerAnimation.gaitSwing(0f,1f,0.2f),0.0001f);
        assertEquals(0f,PlayerAnimation.bodyBob(0f,1f,0.2f),0.0001f);
    }

    @Test public void gaitAlternatesAndIsBoundedAtRunSpeed() {
        float first=PlayerAnimation.gaitSwing(5.5f,0f,0f);
        float forward=PlayerAnimation.gaitSwing(5.5f,(float)Math.PI/18f,0f);
        float backward=PlayerAnimation.gaitSwing(5.5f,(float)Math.PI/6f,0f);
        assertEquals(0f,first,0.0001f);
        assertTrue(forward>0f);
        assertTrue(backward<0f);
        assertTrue(Math.abs(forward)<=0.48f);
        assertTrue(Math.abs(backward)<=0.48f);
    }

    @Test public void invalidInputsFallBackToStableIdlePose() {
        assertEquals(0f,PlayerAnimation.gaitSwing(Float.NaN,1f,0f),0f);
        assertEquals(0f,PlayerAnimation.bodyBob(4f,Float.POSITIVE_INFINITY,0f),0f);
    }
}
