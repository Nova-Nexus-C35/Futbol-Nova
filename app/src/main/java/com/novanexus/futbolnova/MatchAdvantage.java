package com.novanexus.futbolnova;

/**
 * Small, deterministic advantage-play state machine.
 *
 * The current referee model is deliberately conservative: a fouled team gets
 * up to 2.5 simulation seconds to retain or regain the ball. If the opponent
 * wins possession, or the window expires while the ball is loose, the foul
 * restart is awarded. If the fouled team still has possession at expiry, play
 * continues and the advantage is considered realized.
 */
public final class MatchAdvantage {
    public static final int NO_TEAM = -1;
    public static final float DEFAULT_WINDOW_SECONDS = 2.5f;

    public enum Outcome { NONE, CONTINUE, PLAY_ON, AWARD_FREE_KICK }

    private final float windowSeconds;
    private boolean active;
    private boolean awardedHome;
    private boolean defendingHome;
    private float foulX;
    private float foulZ;
    private float elapsedSeconds;

    public MatchAdvantage() {
        this(DEFAULT_WINDOW_SECONDS);
    }

    MatchAdvantage(float windowSeconds) {
        this.windowSeconds = Float.isFinite(windowSeconds) && windowSeconds > 0f
                ? windowSeconds : DEFAULT_WINDOW_SECONDS;
    }

    public boolean begin(boolean awardedHome, float foulX, float foulZ, boolean defendingHome) {
        if (active || !Float.isFinite(foulX) || !Float.isFinite(foulZ)) return false;
        this.awardedHome = awardedHome;
        this.defendingHome = defendingHome;
        this.foulX = foulX;
        this.foulZ = foulZ;
        this.elapsedSeconds = 0f;
        this.active = true;
        return true;
    }

    public Outcome update(float deltaSeconds, int possessionTeam) {
        if (!active) return Outcome.NONE;
        if (!Float.isFinite(deltaSeconds) || deltaSeconds <= 0f) return Outcome.CONTINUE;
        if (possessionTeam != NO_TEAM && possessionTeam != (awardedHome ? 1 : 0)) {
            clear();
            return Outcome.AWARD_FREE_KICK;
        }
        elapsedSeconds += deltaSeconds;
        if (elapsedSeconds < windowSeconds) return Outcome.CONTINUE;
        if (possessionTeam == (awardedHome ? 1 : 0)) {
            clear();
            return Outcome.PLAY_ON;
        }
        clear();
        return Outcome.AWARD_FREE_KICK;
    }

    public boolean isActive() { return active; }
    public boolean isAwardedHome() { return awardedHome; }
    public boolean isDefendingHome() { return defendingHome; }
    public float getFoulX() { return foulX; }
    public float getFoulZ() { return foulZ; }
    public float getElapsedSeconds() { return elapsedSeconds; }

    public void clear() {
        active = false;
        elapsedSeconds = 0f;
    }
}
