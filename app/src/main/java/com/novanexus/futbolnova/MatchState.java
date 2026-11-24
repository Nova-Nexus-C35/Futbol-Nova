package com.novanexus.futbolnova;

/** Pure Java match clock and score state; independent from rendering. */
public final class MatchState {
    public static final class Snapshot {
        public int schemaVersion=1;
        public int homeGoals,awayGoals;
        public float elapsedSeconds;
        public boolean paused;
        public Snapshot() {}
        public Snapshot(int home,int away,float elapsed,boolean paused){
            homeGoals=home;awayGoals=away;elapsedSeconds=elapsed;this.paused=paused;
        }
    }
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
    public Snapshot snapshot(){return new Snapshot(homeGoals,awayGoals,elapsedSeconds,paused);}
    /** Rejects malformed values without partially mutating the live score. */
    public boolean restore(Snapshot snapshot){
        if(snapshot==null||snapshot.schemaVersion!=1||snapshot.homeGoals<0||snapshot.awayGoals<0
                ||!Float.isFinite(snapshot.elapsedSeconds)||snapshot.elapsedSeconds<0f)return false;
        homeGoals=snapshot.homeGoals;awayGoals=snapshot.awayGoals;
        elapsedSeconds=snapshot.elapsedSeconds;paused=snapshot.paused;return true;
    }
    public void reset() { homeGoals=0; awayGoals=0; elapsedSeconds=0f; paused=false; }
    public boolean isPaused() { return paused; }
}
