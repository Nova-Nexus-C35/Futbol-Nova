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
    public int homeShots,awayShots,homeShotsOnTarget,awayShotsOnTarget,homePasses,awayPasses;
    public int homeCompletedPasses,awayCompletedPasses;
    public float homePassAccuracyPercent,awayPassAccuracyPercent;
    public int homeTackles,awayTackles;
    public int homeFouls,awayFouls,homeSaves,awaySaves,homeOffsides,awayOffsides;
    public float homePossessionSeconds,awayPossessionSeconds,homePossessionPercent,awayPossessionPercent;
    public List<CardRecord> cards = new ArrayList<>();
    /** Ordered event timeline copied from the bounded in-match statistics ledger. */
    public List<EventRecord> events = new ArrayList<>();
    public List<PlayerRecord> players = new ArrayList<>();

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

    /** One chronological gameplay event; order is preserved even when no timestamp exists. */
    /** Per-player totals captured at full time. */
    public static final class PlayerRecord {
        public String playerId;
        public int shots,passes,completedPasses,tackles,fouls,saves,goals,offsides;
        public float passAccuracyPercent;
        public PlayerRecord() {}
        PlayerRecord(MatchStatistics.PlayerCounters counters) {
            playerId=counters.playerId;
            shots=counters.getShots();passes=counters.getPasses();completedPasses=counters.getCompletedPasses();
            passAccuracyPercent=counters.getPassAccuracyPercent();
            tackles=counters.getTackles();fouls=counters.getFouls();
            saves=counters.getSaves();goals=counters.getGoals();
            offsides=counters.getOffsides();
        }
    }
    public static final class EventRecord {
        public String type;
        public boolean homeTeam;
        public String playerId;
        public EventRecord() {}
        EventRecord(MatchStatistics.Event event) {
            type=event.type.name();homeTeam=event.homeTeam;playerId=event.playerId;
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
        report.homeShotsOnTarget=stats.getShotsOnTarget(true);report.awayShotsOnTarget=stats.getShotsOnTarget(false);
        report.homePasses=stats.getPasses(true);report.awayPasses=stats.getPasses(false);
        report.homeCompletedPasses=stats.getCompletedPasses(true);report.awayCompletedPasses=stats.getCompletedPasses(false);
        report.homePassAccuracyPercent=stats.getPassAccuracyPercent(true);
        report.awayPassAccuracyPercent=stats.getPassAccuracyPercent(false);
        report.homeTackles=stats.getTackles(true);report.awayTackles=stats.getTackles(false);
        report.homeFouls=stats.getFouls(true);report.awayFouls=stats.getFouls(false);
        report.homeSaves=stats.getSaves(true);report.awaySaves=stats.getSaves(false);
        report.homeOffsides=stats.getOffsides(true);report.awayOffsides=stats.getOffsides(false);
        report.homePossessionSeconds=stats.getPossessionSeconds(true);
        report.awayPossessionSeconds=stats.getPossessionSeconds(false);
        report.homePossessionPercent=stats.getPossessionPercent(true);
        report.awayPossessionPercent=stats.getPossessionPercent(false);
        for(MatchDiscipline.CardEvent event:discipline.snapshotEvents())report.cards.add(new CardRecord(event));
        for(MatchStatistics.Event event:stats.getEvents())report.events.add(new EventRecord(event));
        for(MatchStatistics.PlayerCounters player:stats.getPlayers())report.players.add(new PlayerRecord(player));
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
