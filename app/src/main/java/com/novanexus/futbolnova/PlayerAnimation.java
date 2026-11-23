package com.novanexus.futbolnova;

/** Deterministic gait parameters shared by procedural player visuals. */
public final class PlayerAnimation {
    private static final float MAX_RUN_SPEED = 5.5f;
    private PlayerAnimation() {}

    /** Alternating limb swing in radians; zero speed produces a relaxed idle pose. */
    public static float gaitSwing(float speed,float time,float phaseOffset) {
        if(!Float.isFinite(speed)||!Float.isFinite(time)||!Float.isFinite(phaseOffset))return 0f;
        float blend=clamp(speed/MAX_RUN_SPEED,0f,1f);
        return (float)Math.sin(time*9f+phaseOffset)*(0.48f*blend);
    }

    /** Small vertical body motion synchronized with the running cycle. */
    public static float bodyBob(float speed,float time,float phaseOffset) {
        if(!Float.isFinite(speed)||!Float.isFinite(time)||!Float.isFinite(phaseOffset))return 0f;
        float blend=clamp(speed/MAX_RUN_SPEED,0f,1f);
        return Math.abs((float)Math.sin(time*18f+phaseOffset))*0.045f*blend;
    }

    private static float clamp(float value,float min,float max){return Math.max(min,Math.min(max,value));}
}
