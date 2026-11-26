package com.novanexus.futbolnova;

/** Pure gameplay-to-animation decisions; kept independent of rendering for deterministic tests. */
public final class PlayerAnimationPolicy {
    private static final float SPRINT_SPEED = 2.8f;
    private static final float MOVING_SPEED = 0.25f;
    private static final float HEADER_HEIGHT = 0.98f;
    private static final float CHEST_CONTROL_HEIGHT = 0.65f;
    private static final float MAX_CONTROL_HEIGHT = 1.1f;
    private static final float SLIDE_TACKLE_SPEED = 3.0f;

    private PlayerAnimationPolicy() {
    }

    public static String locomotionHint(float speed) {
        if (!Float.isFinite(speed) || speed <= MOVING_SPEED) return "idle";
        return speed > SPRINT_SPEED ? "sprint" : "walk";
    }

    public static String tackleHint(float speed) {
        return Float.isFinite(speed) && speed >= SLIDE_TACKLE_SPEED
                ? "slide_tackle" : "standing_tackle";
    }

    public static String aerialControlHint(float ballHeight) {
        if (!Float.isFinite(ballHeight) || ballHeight < CHEST_CONTROL_HEIGHT
                || ballHeight > MAX_CONTROL_HEIGHT) return null;
        return ballHeight >= HEADER_HEIGHT ? "soccer_header" : "soccer_chest_control";
    }

    public static String goalkeeperSaveHint(boolean caught, float diveDirection) {
        if (caught) return "keeper_catch";
        if (!Float.isFinite(diveDirection) || diveDirection == 0f) return "keeper_punch";
        return diveDirection < 0f ? "keeper_dive_left" : "keeper_dive_right";
    }
}