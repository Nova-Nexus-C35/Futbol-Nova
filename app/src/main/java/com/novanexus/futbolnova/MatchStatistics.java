package com.novanexus.futbolnova;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/** Bounded, deterministic per-match event ledger and team counters. */
public final class MatchStatistics {
    public enum EventType { SHOT, PASS_ATTEMPT, PASS_COMPLETED, TACKLE, FOUL, SAVE, GOAL, OFFSIDE }
    public static final class Event {
        public final EventType type;
        public final boolean homeTeam;
        public final String playerId;
        /** Match-clock time in simulation seconds when the event was recorded. */
        public final float elapsedSeconds;
        private Event(EventType type,boolean homeTeam,String playerId,float elapsedSeconds){
            this.type=type;this.homeTeam=homeTeam;this.playerId=playerId;
            this.elapsedSeconds=Float.isFinite(elapsedSeconds)&&elapsedSeconds>=0f?elapsedSeconds:0f;
        }
    }
    private static final int MAX_EVENTS=256;
    private final List<Event> events=new ArrayList<>();
    private final Map<String,PlayerCounters> playerCounters=new LinkedHashMap<>();
    public static final class PlayerCounters {
        public final String playerId;
        private int shots,passes,completedPasses,tackles,fouls,saves,goals,offsides;
        private PlayerCounters(String id){playerId=id;}
        public int getShots(){return shots;} public int getPasses(){return passes;}
        public int getCompletedPasses(){return completedPasses;}
        public float getPassAccuracyPercent(){return passes>0?100f*completedPasses/passes:0f;}
        public int getTackles(){return tackles;} public int getFouls(){return fouls;}
        public int getSaves(){return saves;} public int getGoals(){return goals;}
        public int getOffsides(){return offsides;}
    }
    private final int[] shots=new int[2],shotsOnTarget=new int[2],passes=new int[2],completedPasses=new int[2],tackles=new int[2],
            fouls=new int[2],saves=new int[2],goals=new int[2],offsides=new int[2];
    private final float[] possessionSeconds=new float[2];

    public void recordShot(boolean home){recordShot(null,home,0f);}
    public void recordShot(String playerId,boolean home){recordShot(playerId,home,0f);}
    public void recordShot(String playerId,boolean home,float elapsedSeconds){shots[index(home)]++;count(playerId).shots++;add(EventType.SHOT,home,playerId,elapsedSeconds);}
    public void recordPassAttempt(boolean home){recordPassAttempt(null,home,0f);}
    public void recordPassAttempt(String playerId,boolean home){recordPassAttempt(playerId,home,0f);}
    public void recordPassAttempt(String playerId,boolean home,float elapsedSeconds){passes[index(home)]++;count(playerId).passes++;add(EventType.PASS_ATTEMPT,home,playerId,elapsedSeconds);}
    public void recordPassCompleted(String playerId,boolean home){recordPassCompleted(playerId,home,0f);}
    public void recordPassCompleted(String playerId,boolean home,float elapsedSeconds){
        completedPasses[index(home)]++;count(playerId).completedPasses++;
        add(EventType.PASS_COMPLETED,home,playerId,elapsedSeconds);
    }
    public void recordTackle(boolean home){recordTackle(null,home,0f);}
    public void recordTackle(String playerId,boolean home){recordTackle(playerId,home,0f);}
    public void recordTackle(String playerId,boolean home,float elapsedSeconds){tackles[index(home)]++;count(playerId).tackles++;add(EventType.TACKLE,home,playerId,elapsedSeconds);}
    public void recordFoul(boolean home){recordFoul(null,home,0f);}
    public void recordFoul(String playerId,boolean home){recordFoul(playerId,home,0f);}
    public void recordFoul(String playerId,boolean home,float elapsedSeconds){fouls[index(home)]++;count(playerId).fouls++;add(EventType.FOUL,home,playerId,elapsedSeconds);}
    public void recordSave(boolean home){recordSave(null,home,0f);}
    public void recordSave(String playerId,boolean home){recordSave(playerId,home,0f);}
    public void recordSave(String playerId,boolean home,float elapsedSeconds){saves[index(home)]++;shotsOnTarget[index(!home)]++;count(playerId).saves++;add(EventType.SAVE,home,playerId,elapsedSeconds);}
    public void recordGoal(boolean home){recordGoal(null,home,0f);}
    public void recordGoal(String playerId,boolean home){recordGoal(playerId,home,0f);}
    public void recordGoal(String playerId,boolean home,float elapsedSeconds){goals[index(home)]++;shotsOnTarget[index(home)]++;count(playerId).goals++;add(EventType.GOAL,home,playerId,elapsedSeconds);}
    public void recordOffside(boolean offendingHome){recordOffside(null,offendingHome,0f);}
    public void recordOffside(String playerId,boolean offendingHome){recordOffside(playerId,offendingHome,0f);}
    public void recordOffside(String playerId,boolean offendingHome,float elapsedSeconds){offsides[index(offendingHome)]++;count(playerId).offsides++;add(EventType.OFFSIDE,offendingHome,playerId,elapsedSeconds);}
    public PlayerCounters getPlayerCounters(String playerId){return playerId==null?null:playerCounters.get(playerId);}
    public List<PlayerCounters> getPlayers(){return Collections.unmodifiableList(new ArrayList<>(playerCounters.values()));}
    public int getShots(boolean home){return shots[index(home)];}
    /** On-target shots are confirmed only by a goal or a goalkeeper save. */
    public int getShotsOnTarget(boolean home){return shotsOnTarget[index(home)];}
    public int getPasses(boolean home){return passes[index(home)];}
    public int getCompletedPasses(boolean home){return completedPasses[index(home)];}
    public float getPassAccuracyPercent(boolean home){
        int attempted=passes[index(home)];
        return attempted>0?100f*completedPasses[index(home)]/attempted:0f;
    }
    public int getTackles(boolean home){return tackles[index(home)];}
    public int getFouls(boolean home){return fouls[index(home)];}
    public int getSaves(boolean home){return saves[index(home)];}
    public int getGoals(boolean home){return goals[index(home)];}
    public int getOffsides(boolean home){return offsides[index(home)];}
    /** Adds controlled-ball time in match-clock seconds, excluding dead-ball intervals. */
    public void recordPossessionSeconds(float seconds,boolean home){
        if(Float.isFinite(seconds)&&seconds>0f)possessionSeconds[index(home)]+=seconds;
    }
    public float getPossessionSeconds(boolean home){return possessionSeconds[index(home)];}
    public float getPossessionPercent(boolean home){
        float total=possessionSeconds[0]+possessionSeconds[1];
        return total>0f?100f*possessionSeconds[index(home)]/total:0f;
    }
    public List<Event> getEvents(){return Collections.unmodifiableList(new ArrayList<>(events));}
    public void reset(){
        events.clear();playerCounters.clear();
        java.util.Arrays.fill(shots,0);java.util.Arrays.fill(shotsOnTarget,0);java.util.Arrays.fill(passes,0);
        java.util.Arrays.fill(completedPasses,0);
        java.util.Arrays.fill(tackles,0);java.util.Arrays.fill(fouls,0);java.util.Arrays.fill(offsides,0);
        java.util.Arrays.fill(saves,0);java.util.Arrays.fill(goals,0);
        java.util.Arrays.fill(possessionSeconds,0f);
    }
    private PlayerCounters count(String playerId){
        if(playerId==null||playerId.isEmpty())return new PlayerCounters("");
        PlayerCounters counters=playerCounters.get(playerId);
        if(counters==null){counters=new PlayerCounters(playerId);playerCounters.put(playerId,counters);}
        return counters;
    }
    private void add(EventType type,boolean home){add(type,home,null,0f);}
    private void add(EventType type,boolean home,String playerId){add(type,home,playerId,0f);}
    private void add(EventType type,boolean home,String playerId,float elapsedSeconds){
        if(events.size()==MAX_EVENTS)events.remove(0);
        events.add(new Event(type,home,playerId,elapsedSeconds));
    }
    private static int index(boolean home){return home?0:1;}
}
