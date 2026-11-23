package com.novanexus.futbolnova;

/** Pure Java match clock and score state; independent from rendering. */
public final class MatchState {
    private int homeGoals;
    private int awayGoals;
    private float elapsedSeconds;
    private boolean paused;

    public void update(float deltaSeconds) {
        if (!paused && Float.isFinite(deltaSeconds) && deltaSeconds > 0f) elapsedSeconds += deltaSeconds;
    }
    public void addGoal(boolean homeTeam) {
        if (homeTeam) homeGoals++; else awayGoals++;
    }
    public void setPaused(boolean paused) { this.paused = paused; }
    public int getHomeGoals() { return homeGoals; }
    public int getAwayGoals() { return awayGoals; }
    public float getElapsedSeconds() { return elapsedSeconds; }
    public void reset() { homeGoals=0; awayGoals=0; elapsedSeconds=0f; paused=false; }
    public boolean isPaused() { return paused; }
}
