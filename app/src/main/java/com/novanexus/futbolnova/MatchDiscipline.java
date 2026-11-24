package com.novanexus.futbolnova;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Per-match disciplinary ledger. A second caution dismisses the player immediately. */
public final class MatchDiscipline {
    public enum Card { NONE, YELLOW, RED }
    public enum Outcome { NO_CARD, CAUTION, DISMISSED, ALREADY_DISMISSED, INVALID_PLAYER }
    public enum Reason {
        PERSISTENT_INFRINGEMENT, REFEREE_DECISION, SERIOUS_FOUL_PLAY,
        VIOLENT_CONDUCT, DENIAL_OF_GOAL_SCORING_OPPORTUNITY, DISSENT, OTHER
    }
    public static final class CardEvent implements java.io.Serializable {
        public String playerId;
        public Card card;
        public Reason reason;
        /** Match elapsed seconds; -1 means the caller did not supply a match time. */
        public float elapsedSeconds;
        public boolean dismissed;
        public CardEvent() {}
        private CardEvent(String id,Card card,Reason reason,float seconds,boolean dismissed){
            this.playerId=id;this.card=card;this.reason=reason;this.elapsedSeconds=seconds;this.dismissed=dismissed;
        }
    }

    public static final class PlayerRecord implements java.io.Serializable {
        public String playerId;
        public int yellowCards;
        public int foulsSinceCaution;
        public boolean sentOff;
        public PlayerRecord() {}
        private PlayerRecord(String id,int yellows,int fouls,boolean dismissed){
            playerId=id;yellowCards=yellows;foulsSinceCaution=fouls;sentOff=dismissed;
        }
    }

    private static final class MutableRecord {
        int yellows;
        int foulsSinceCaution;
        boolean dismissed;
    }

    private final Map<String,MutableRecord> records=new LinkedHashMap<>();
    private final java.util.List<CardEvent> events=new java.util.ArrayList<>();

    /** Three recorded fouls trigger a caution for persistent infringement; this is an explicit gameplay heuristic. */
    public Outcome recordFoul(String playerId){return recordFoul(playerId,-1f);}

    public Outcome recordFoul(String playerId,float elapsedSeconds){
        if(!valid(playerId))return Outcome.INVALID_PLAYER;
        MutableRecord record=records.computeIfAbsent(playerId,k->new MutableRecord());
        if(record.dismissed)return Outcome.ALREADY_DISMISSED;
        record.foulsSinceCaution++;
        if(record.foulsSinceCaution<3)return Outcome.NO_CARD;
        record.foulsSinceCaution=0;
        return issueYellow(playerId,Reason.PERSISTENT_INFRINGEMENT,elapsedSeconds);
    }

    public Outcome issueYellow(String playerId){return issueYellow(playerId,Reason.REFEREE_DECISION,-1f);}

    public Outcome issueYellow(String playerId,Reason reason,float elapsedSeconds){
        if(!valid(playerId))return Outcome.INVALID_PLAYER;
        MutableRecord record=records.computeIfAbsent(playerId,k->new MutableRecord());
        if(record.dismissed)return Outcome.ALREADY_DISMISSED;
        record.yellows++;
        boolean dismissed=record.yellows>=2;
        if(dismissed)record.dismissed=true;
        events.add(new CardEvent(playerId,Card.YELLOW,reason==null?Reason.OTHER:reason,
                validTime(elapsedSeconds),dismissed));
        return dismissed?Outcome.DISMISSED:Outcome.CAUTION;
    }

    public Outcome issueRed(String playerId){return issueRed(playerId,Reason.REFEREE_DECISION,-1f);}

    public Outcome issueRed(String playerId,Reason reason,float elapsedSeconds){
        if(!valid(playerId))return Outcome.INVALID_PLAYER;
        MutableRecord record=records.computeIfAbsent(playerId,k->new MutableRecord());
        if(record.dismissed)return Outcome.ALREADY_DISMISSED;
        record.dismissed=true;
        events.add(new CardEvent(playerId,Card.RED,reason==null?Reason.OTHER:reason,
                validTime(elapsedSeconds),true));
        return Outcome.DISMISSED;
    }

    /** Immutable chronological card history for the match report. */
    public java.util.List<CardEvent> snapshotEvents(){
        return Collections.unmodifiableList(new java.util.ArrayList<>(events));
    }

    public boolean canParticipate(String playerId){
        if(!valid(playerId))return false;
        MutableRecord record=records.get(playerId);
        return record==null||!record.dismissed;
    }

    public int getYellowCards(String playerId){
        MutableRecord record=valid(playerId)?records.get(playerId):null;
        return record==null?0:record.yellows;
    }

    public boolean isSentOff(String playerId){
        MutableRecord record=valid(playerId)?records.get(playerId):null;
        return record!=null&&record.dismissed;
    }

    public PlayerRecord getRecord(String playerId){
        if(!valid(playerId))return null;
        MutableRecord record=records.get(playerId);
        return record==null?new PlayerRecord(playerId,0,0,false)
                :new PlayerRecord(playerId,record.yellows,record.foulsSinceCaution,record.dismissed);
    }

    public Map<String,PlayerRecord> snapshot(){
        Map<String,PlayerRecord> copy=new LinkedHashMap<>();
        for(Map.Entry<String,MutableRecord> entry:records.entrySet()){
            MutableRecord value=entry.getValue();
            copy.put(entry.getKey(),new PlayerRecord(entry.getKey(),value.yellows,value.foulsSinceCaution,value.dismissed));
        }
        return Collections.unmodifiableMap(copy);
    }


    /** Versioned deep snapshot of every disciplinary record and chronological card event. */
    public static final class Snapshot implements java.io.Serializable {
        public int schemaVersion=1;
        public java.util.List<PlayerRecord> records=new java.util.ArrayList<>();
        public java.util.List<CardEvent> events=new java.util.ArrayList<>();
        public Snapshot() {}
        private Snapshot(java.util.List<PlayerRecord> records,java.util.List<CardEvent> events){
            this.records=Collections.unmodifiableList(new java.util.ArrayList<>(records));
            this.events=Collections.unmodifiableList(new java.util.ArrayList<>(events));
        }
    }

    public Snapshot snapshotState(){
        return new Snapshot(new java.util.ArrayList<>(snapshot().values()),new java.util.ArrayList<>(events));
    }

    /** Validates all IDs, counters, enums and event times before replacing live state. */
    public boolean restore(Snapshot snapshot){
        if(snapshot==null||snapshot.schemaVersion!=1||snapshot.records==null||snapshot.events==null)return false;
        Map<String,MutableRecord> restored=new LinkedHashMap<>();
        for(PlayerRecord record:snapshot.records){
            if(record==null||!valid(record.playerId)||record.yellowCards<0||record.yellowCards>2
                    ||record.foulsSinceCaution<0||record.foulsSinceCaution>2
                    ||record.sentOff!=(record.yellowCards>=2)&&record.yellowCards>0
                    ||restored.containsKey(record.playerId))return false;
            MutableRecord value=new MutableRecord();value.yellows=record.yellowCards;
            value.foulsSinceCaution=record.foulsSinceCaution;value.dismissed=record.sentOff;
            restored.put(record.playerId,value);
        }
        java.util.List<CardEvent> restoredEvents=new java.util.ArrayList<>();
        for(CardEvent event:snapshot.events){
            if(event==null||!valid(event.playerId)||event.card==null||event.card==Card.NONE
                    ||event.reason==null||!Float.isFinite(event.elapsedSeconds)||event.elapsedSeconds< -1f)return false;
            MutableRecord record=restored.get(event.playerId);
            if(record==null)return false;
            restoredEvents.add(new CardEvent(event.playerId,event.card,event.reason,event.elapsedSeconds,event.dismissed));
        }
        Map<String,Integer> yellowsFromEvents=new LinkedHashMap<>();
        Map<String,Boolean> dismissedFromEvents=new LinkedHashMap<>();
        for(CardEvent event:restoredEvents){
            if(event.card==Card.YELLOW)yellowsFromEvents.put(event.playerId,yellowsFromEvents.getOrDefault(event.playerId,0)+1);
            if(event.dismissed||event.card==Card.RED)dismissedFromEvents.put(event.playerId,true);
        }
        for(Map.Entry<String,MutableRecord> entry:restored.entrySet()){
            MutableRecord value=entry.getValue();
            if(yellowsFromEvents.getOrDefault(entry.getKey(),0)!=value.yellows
                    ||dismissedFromEvents.getOrDefault(entry.getKey(),false)!=value.dismissed)return false;
        }
        records.clear();records.putAll(restored);events.clear();events.addAll(restoredEvents);return true;
    }

    public void reset(){records.clear();events.clear();}

    private static float validTime(float seconds){
        return Float.isFinite(seconds)&&seconds>=0f?seconds:-1f;
    }

    private static boolean valid(String id){return id!=null&&!id.trim().isEmpty();}
}
