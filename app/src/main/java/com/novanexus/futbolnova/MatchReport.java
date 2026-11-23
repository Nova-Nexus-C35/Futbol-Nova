package com.novanexus.futbolnova;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import java.util.ArrayList;
import java.util.List;

/** Finished-match snapshot, serializable independently of Android. */
public final class MatchReport {
    public long matchId;
    public String homeTeam;
    public String awayTeam;
    public int homeGoals;
    public int awayGoals;
    public float elapsedSeconds;
    public int homeShots,awayShots,homePasses,awayPasses,homeTackles,awayTackles;
    public int homeFouls,awayFouls,homeSaves,awaySaves,homeOffsides,awayOffsides;
    public List<CardRecord> cards = new ArrayList<>();

    public static final class CardRecord {
        public String playerId;
        public String card;
        public String reason;
        public float elapsedSeconds;
        public boolean dismissed;
        public CardRecord() {}
        CardRecord(MatchDiscipline.CardEvent event) {
            playerId=event.playerId;card=event.card.name();reason=event.reason.name();
            elapsedSeconds=event.elapsedSeconds;dismissed=event.dismissed;
        }
    }

    public MatchReport() {}

    public static MatchReport capture(String home,String away,int homeGoals,int awayGoals,
            float elapsed,MatchStatistics stats,MatchDiscipline discipline) {
        MatchReport report=new MatchReport();
        report.matchId=System.currentTimeMillis();report.homeTeam=home;report.awayTeam=away;
        report.homeGoals=homeGoals;report.awayGoals=awayGoals;
        report.elapsedSeconds=Float.isFinite(elapsed)&&elapsed>=0f?elapsed:0f;
        report.homeShots=stats.getShots(true);report.awayShots=stats.getShots(false);
        report.homePasses=stats.getPasses(true);report.awayPasses=stats.getPasses(false);
        report.homeTackles=stats.getTackles(true);report.awayTackles=stats.getTackles(false);
        report.homeFouls=stats.getFouls(true);report.awayFouls=stats.getFouls(false);
        report.homeSaves=stats.getSaves(true);report.awaySaves=stats.getSaves(false);
        report.homeOffsides=stats.getOffsides(true);report.awayOffsides=stats.getOffsides(false);
        for(MatchDiscipline.CardEvent event:discipline.snapshotEvents())report.cards.add(new CardRecord(event));
        return report;
    }

    public String toJson() {
        Json json=new Json();json.setOutputType(JsonWriter.OutputType.json);return json.toJson(this);
    }

    public static MatchReport fromJson(String value) {
        if(value==null||value.trim().isEmpty())return null;
        try{return new Json().fromJson(MatchReport.class,value);}
        catch(RuntimeException invalid){return null;}
    }
}
