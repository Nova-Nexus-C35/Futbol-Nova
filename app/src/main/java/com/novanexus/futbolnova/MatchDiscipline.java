package com.novanexus.futbolnova;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Per-match disciplinary ledger. A second caution dismisses the player immediately. */
public final class MatchDiscipline {
    public enum Card { NONE, YELLOW, RED }
    public enum Outcome { NO_CARD, CAUTION, DISMISSED, ALREADY_DISMISSED, INVALID_PLAYER }

    public static final class PlayerRecord {
        public final String playerId;
        public final int yellowCards;
        public final boolean sentOff;
        private PlayerRecord(String id,int yellows,boolean dismissed){
            playerId=id;yellowCards=yellows;sentOff=dismissed;
        }
    }

    private static final class MutableRecord {
        int yellows;
        boolean dismissed;
    }

    private final Map<String,MutableRecord> records=new LinkedHashMap<>();

    public Outcome issueYellow(String playerId){
        if(!valid(playerId))return Outcome.INVALID_PLAYER;
        MutableRecord record=records.computeIfAbsent(playerId,k->new MutableRecord());
        if(record.dismissed)return Outcome.ALREADY_DISMISSED;
        record.yellows++;
        if(record.yellows>=2){record.dismissed=true;return Outcome.DISMISSED;}
        return Outcome.CAUTION;
    }

    public Outcome issueRed(String playerId){
        if(!valid(playerId))return Outcome.INVALID_PLAYER;
        MutableRecord record=records.computeIfAbsent(playerId,k->new MutableRecord());
        if(record.dismissed)return Outcome.ALREADY_DISMISSED;
        record.dismissed=true;
        return Outcome.DISMISSED;
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
        return record==null?new PlayerRecord(playerId,0,false)
                :new PlayerRecord(playerId,record.yellows,record.dismissed);
    }

    public Map<String,PlayerRecord> snapshot(){
        Map<String,PlayerRecord> copy=new LinkedHashMap<>();
        for(Map.Entry<String,MutableRecord> entry:records.entrySet()){
            MutableRecord value=entry.getValue();
            copy.put(entry.getKey(),new PlayerRecord(entry.getKey(),value.yellows,value.dismissed));
        }
        return Collections.unmodifiableMap(copy);
    }

    public void reset(){records.clear();}

    private static boolean valid(String id){return id!=null&&!id.trim().isEmpty();}
}
