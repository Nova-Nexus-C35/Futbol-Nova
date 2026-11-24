package com.novanexus.futbolnova;

/**
 * Deterministic regulation-time state machine, independent from rendering.
 * Durations are simulation seconds, so arcade speed can be configured by the caller.
 */
public final class MatchClock {
    public enum Phase { PRE_MATCH, FIRST_HALF, HALF_TIME, SECOND_HALF, FULL_TIME }

    /** Immutable, versioned value object for preserving the exact match-clock state. */
    public static final class Snapshot implements java.io.Serializable {
        public int schemaVersion=1;
        public float halfDurationSeconds,elapsedInHalf,firstHalfAddedTime,secondHalfAddedTime;
        public Phase phase;
        public boolean paused;
        public Snapshot() {}
        public Snapshot(float duration,float elapsed,float firstAdded,float secondAdded,Phase phase,boolean paused){
            this.halfDurationSeconds=duration;this.elapsedInHalf=elapsed;
            this.firstHalfAddedTime=firstAdded;this.secondHalfAddedTime=secondAdded;
            this.phase=phase;this.paused=paused;
        }
    }

    private final float halfDurationSeconds;
    private float elapsedInHalf;
    private float firstHalfAddedTime;
    private float secondHalfAddedTime;
    private Phase phase = Phase.PRE_MATCH;
    private boolean paused;

    public MatchClock() { this(45f * 60f); }

    public MatchClock(float halfDurationSeconds) {
        if (!Float.isFinite(halfDurationSeconds) || halfDurationSeconds <= 0f) {
            throw new IllegalArgumentException("Half duration must be a positive finite number");
        }
        this.halfDurationSeconds = halfDurationSeconds;
    }

    public boolean start() {
        if (phase != Phase.PRE_MATCH) return false;
        phase = Phase.FIRST_HALF;
        paused = false;
        return true;
    }

    public void update(float deltaSeconds) {
        if (paused || !isPlaying() || !Float.isFinite(deltaSeconds) || deltaSeconds <= 0f) return;
        elapsedInHalf += deltaSeconds;
        float limit = halfDurationSeconds + getAddedTimeForCurrentHalf();
        if (elapsedInHalf >= limit) {
            elapsedInHalf = limit;
            paused = true;
            phase = phase == Phase.FIRST_HALF ? Phase.HALF_TIME : Phase.FULL_TIME;
        }
    }

    public boolean startSecondHalf() {
        if (phase != Phase.HALF_TIME) return false;
        elapsedInHalf = 0f;
        phase = Phase.SECOND_HALF;
        paused = false;
        return true;
    }

    public void pause() {
        if (isPlaying()) paused = true;
    }

    public void resume() {
        if (isPlaying()) paused = false;
    }

    public boolean setAddedTime(int seconds) {
        if (seconds < 0 || !isPlaying()) return false;
        if (phase == Phase.FIRST_HALF) firstHalfAddedTime = seconds;
        else secondHalfAddedTime = seconds;
        return true;
    }

    public void reset() {
        elapsedInHalf = 0f;
        firstHalfAddedTime = 0f;
        secondHalfAddedTime = 0f;
        phase = Phase.PRE_MATCH;
        paused = false;
    }

    public boolean isPlaying() {
        return phase == Phase.FIRST_HALF || phase == Phase.SECOND_HALF;
    }

    /** True only while live gameplay input may change the match state. */
    public boolean canAcceptGameplayInput() {
        return isPlaying() && !paused;
    }

    public boolean isPaused() { return paused; }
    public Phase getPhase() { return phase; }
    public float getElapsedInHalfSeconds() { return elapsedInHalf; }
    public float getHalfDurationSeconds() { return halfDurationSeconds; }
    public float getAddedTimeForCurrentHalf() {
        return phase == Phase.SECOND_HALF ? secondHalfAddedTime : firstHalfAddedTime;
    }
    public Snapshot snapshot() {
        return new Snapshot(halfDurationSeconds,elapsedInHalf,firstHalfAddedTime,
                secondHalfAddedTime,phase,paused);
    }

    /** Validates the complete value before changing any live clock field. */
    public boolean restore(Snapshot snapshot) {
        if(snapshot==null||snapshot.schemaVersion!=1||snapshot.phase==null
                ||!Float.isFinite(snapshot.halfDurationSeconds)
                ||Math.abs(snapshot.halfDurationSeconds-halfDurationSeconds)>0.0001f
                ||!validNonNegative(snapshot.elapsedInHalf)
                ||!validNonNegative(snapshot.firstHalfAddedTime)
                ||!validNonNegative(snapshot.secondHalfAddedTime))return false;
        float limit=halfDurationSeconds+(snapshot.phase==Phase.SECOND_HALF||snapshot.phase==Phase.FULL_TIME
                ?snapshot.secondHalfAddedTime:snapshot.firstHalfAddedTime);
        if(snapshot.elapsedInHalf>limit+0.001f)return false;
        if(snapshot.phase==Phase.PRE_MATCH&&snapshot.elapsedInHalf>0.001f)return false;
        if(snapshot.phase==Phase.FIRST_HALF&&snapshot.elapsedInHalf>=limit)return false;
        if(snapshot.phase==Phase.SECOND_HALF&&snapshot.elapsedInHalf>=limit)return false;
        if((snapshot.phase==Phase.HALF_TIME||snapshot.phase==Phase.FULL_TIME)
                &&Math.abs(snapshot.elapsedInHalf-limit)>0.001f)return false;
        if((snapshot.phase==Phase.PRE_MATCH||snapshot.phase==Phase.HALF_TIME||snapshot.phase==Phase.FULL_TIME)
                &&!snapshot.paused)return false;
        elapsedInHalf=snapshot.elapsedInHalf;firstHalfAddedTime=snapshot.firstHalfAddedTime;
        secondHalfAddedTime=snapshot.secondHalfAddedTime;phase=snapshot.phase;paused=snapshot.paused;
        return true;
    }

    private static boolean validNonNegative(float value){return Float.isFinite(value)&&value>=0f;}

    public float getTotalElapsedSeconds() {
        if (phase == Phase.PRE_MATCH) return 0f;
        if (phase == Phase.FIRST_HALF || phase == Phase.HALF_TIME) return elapsedInHalf;
        if (phase == Phase.SECOND_HALF || phase == Phase.FULL_TIME) {
            return halfDurationSeconds + firstHalfAddedTime + elapsedInHalf;
        }
        return 0f;
    }
}
