package com.novanexus.futbolnova;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Bounded, deterministic per-match event ledger and team counters. */
public final class MatchStatistics {
    public enum EventType { SHOT, PASS_ATTEMPT, TACKLE, FOUL, SAVE, GOAL, OFFSIDE }
    public static final class Event {
        public final EventType type;
        public final boolean homeTeam;
        private Event(EventType type,boolean homeTeam){this.type=type;this.homeTeam=homeTeam;}
    }
    private static final int MAX_EVENTS=256;
    private final List<Event> events=new ArrayList<>();
    private final int[] shots=new int[2],passes=new int[2],tackles=new int[2],
            fouls=new int[2],saves=new int[2],goals=new int[2],offsides=new int[2];

    public void recordShot(boolean home){shots[index(home)]++;add(EventType.SHOT,home);}
    public void recordPassAttempt(boolean home){passes[index(home)]++;add(EventType.PASS_ATTEMPT,home);}
    public void recordTackle(boolean home){tackles[index(home)]++;add(EventType.TACKLE,home);}
    public void recordFoul(boolean home){fouls[index(home)]++;add(EventType.FOUL,home);}
    public void recordSave(boolean home){saves[index(home)]++;add(EventType.SAVE,home);}
    public void recordGoal(boolean home){goals[index(home)]++;add(EventType.GOAL,home);}
    public void recordOffside(boolean offendingHome){offsides[index(offendingHome)]++;add(EventType.OFFSIDE,offendingHome);}
    public int getShots(boolean home){return shots[index(home)];}
    public int getPasses(boolean home){return passes[index(home)];}
    public int getTackles(boolean home){return tackles[index(home)];}
    public int getFouls(boolean home){return fouls[index(home)];}
    public int getSaves(boolean home){return saves[index(home)];}
    public int getGoals(boolean home){return goals[index(home)];}
    public int getOffsides(boolean home){return offsides[index(home)];}
    public List<Event> getEvents(){return Collections.unmodifiableList(new ArrayList<>(events));}
    public void reset(){
        events.clear();
        java.util.Arrays.fill(shots,0);java.util.Arrays.fill(passes,0);
        java.util.Arrays.fill(tackles,0);java.util.Arrays.fill(fouls,0);java.util.Arrays.fill(offsides,0);
        java.util.Arrays.fill(saves,0);java.util.Arrays.fill(goals,0);
    }
    private void add(EventType type,boolean home){
        if(events.size()==MAX_EVENTS)events.remove(0);
        events.add(new Event(type,home));
    }
    private static int index(boolean home){return home?0:1;}
}
