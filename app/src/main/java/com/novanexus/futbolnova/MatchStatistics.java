package com.novanexus.futbolnova;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/** Bounded, deterministic per-match event ledger and team counters. */
public final class MatchStatistics {
    public enum EventType { SHOT, PASS_ATTEMPT, TACKLE, FOUL, SAVE, GOAL, OFFSIDE }
    public static final class Event {
        public final EventType type;
        public final boolean homeTeam;
        public final String playerId;
        private Event(EventType type,boolean homeTeam,String playerId){this.type=type;this.homeTeam=homeTeam;this.playerId=playerId;}
    }
    private static final int MAX_EVENTS=256;
    private final List<Event> events=new ArrayList<>();
    private final Map<String,PlayerCounters> playerCounters=new LinkedHashMap<>();
    public static final class PlayerCounters {
        public final String playerId;
        private int shots,passes,tackles,fouls,saves,goals,offsides;
        private PlayerCounters(String id){playerId=id;}
        public int getShots(){return shots;} public int getPasses(){return passes;}
        public int getTackles(){return tackles;} public int getFouls(){return fouls;}
        public int getSaves(){return saves;} public int getGoals(){return goals;}
        public int getOffsides(){return offsides;}
    }
    private final int[] shots=new int[2],passes=new int[2],tackles=new int[2],
            fouls=new int[2],saves=new int[2],goals=new int[2],offsides=new int[2];

    public void recordShot(boolean home){recordShot(null,home);}
    public void recordShot(String playerId,boolean home){shots[index(home)]++;count(playerId).shots++;add(EventType.SHOT,home,playerId);}
    public void recordPassAttempt(boolean home){recordPassAttempt(null,home);}
    public void recordPassAttempt(String playerId,boolean home){passes[index(home)]++;count(playerId).passes++;add(EventType.PASS_ATTEMPT,home,playerId);}
    public void recordTackle(boolean home){recordTackle(null,home);}
    public void recordTackle(String playerId,boolean home){tackles[index(home)]++;count(playerId).tackles++;add(EventType.TACKLE,home,playerId);}
    public void recordFoul(boolean home){recordFoul(null,home);}
    public void recordFoul(String playerId,boolean home){fouls[index(home)]++;count(playerId).fouls++;add(EventType.FOUL,home,playerId);}
    public void recordSave(boolean home){recordSave(null,home);}
    public void recordSave(String playerId,boolean home){saves[index(home)]++;count(playerId).saves++;add(EventType.SAVE,home,playerId);}
    public void recordGoal(boolean home){recordGoal(null,home);}
    public void recordGoal(String playerId,boolean home){goals[index(home)]++;count(playerId).goals++;add(EventType.GOAL,home,playerId);}
    public void recordOffside(boolean offendingHome){recordOffside(null,offendingHome);}
    public void recordOffside(String playerId,boolean offendingHome){offsides[index(offendingHome)]++;count(playerId).offsides++;add(EventType.OFFSIDE,offendingHome,playerId);}
    public PlayerCounters getPlayerCounters(String playerId){return playerId==null?null:playerCounters.get(playerId);}
    public List<PlayerCounters> getPlayers(){return Collections.unmodifiableList(new ArrayList<>(playerCounters.values()));}
    public int getShots(boolean home){return shots[index(home)];}
    public int getPasses(boolean home){return passes[index(home)];}
    public int getTackles(boolean home){return tackles[index(home)];}
    public int getFouls(boolean home){return fouls[index(home)];}
    public int getSaves(boolean home){return saves[index(home)];}
    public int getGoals(boolean home){return goals[index(home)];}
    public int getOffsides(boolean home){return offsides[index(home)];}
    public List<Event> getEvents(){return Collections.unmodifiableList(new ArrayList<>(events));}
    public void reset(){
        events.clear();playerCounters.clear();
        java.util.Arrays.fill(shots,0);java.util.Arrays.fill(passes,0);
        java.util.Arrays.fill(tackles,0);java.util.Arrays.fill(fouls,0);java.util.Arrays.fill(offsides,0);
        java.util.Arrays.fill(saves,0);java.util.Arrays.fill(goals,0);
    }
    private PlayerCounters count(String playerId){
        if(playerId==null||playerId.isEmpty())return EMPTY_COUNTERS;
        PlayerCounters counters=playerCounters.get(playerId);
        if(counters==null){counters=new PlayerCounters(playerId);playerCounters.put(playerId,counters);}
        return counters;
    }
    private static final PlayerCounters EMPTY_COUNTERS=new PlayerCounters("");
    private void add(EventType type,boolean home){add(type,home,null);}
    private void add(EventType type,boolean home,String playerId){
        if(events.size()==MAX_EVENTS)events.remove(0);
        events.add(new Event(type,home,playerId));
    }
    private static int index(boolean home){return home?0:1;}
}
