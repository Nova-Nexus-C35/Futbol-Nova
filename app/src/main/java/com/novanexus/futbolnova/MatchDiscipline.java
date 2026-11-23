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
    public static final class CardEvent {
        public final String playerId;
        public final Card card;
        public final Reason reason;
        /** Match elapsed seconds; -1 means the caller did not supply a match time. */
        public final float elapsedSeconds;
        public final boolean dismissed;
        private CardEvent(String id,Card card,Reason reason,float seconds,boolean dismissed){
            this.playerId=id;this.card=card;this.reason=reason;this.elapsedSeconds=seconds;this.dismissed=dismissed;
        }
    }

    public static final class PlayerRecord {
        public final String playerId;
        public final int yellowCards;
        public final int foulsSinceCaution;
        public final boolean sentOff;
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

    public void reset(){records.clear();events.clear();}

    private static float validTime(float seconds){
        return Float.isFinite(seconds)&&seconds>=0f?seconds:-1f;
    }

    private static boolean valid(String id){return id!=null&&!id.trim().isEmpty();}
}
