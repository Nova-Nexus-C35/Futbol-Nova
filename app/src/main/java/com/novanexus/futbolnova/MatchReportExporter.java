package com.novanexus.futbolnova;

/** Builds a portable UTF-8 CSV export from a completed match report. */
public final class MatchReportExporter {
    private MatchReportExporter() {}

    /** CSV with match totals, team/player totals, retained timeline and cards. */
    public static String toCsv(MatchReport report) {
        if (report == null) throw new IllegalArgumentException("report is required");
        StringBuilder csv = new StringBuilder(1024);
        row(csv,"MATCH","match_id","home_team","away_team","home_goals","away_goals","elapsed_seconds");
        row(csv,"MATCH",v(report.matchId),v(report.homeTeam),v(report.awayTeam),v(report.homeGoals),v(report.awayGoals),v(report.elapsedSeconds));
        row(csv,"TEAM","side","team","shots","shots_on_target","passes_attempted","passes_completed","pass_accuracy_percent","tackles","fouls","saves","offsides","possession_seconds","possession_percent");
        row(csv,"TEAM","HOME",v(report.homeTeam),v(report.homeShots),v(report.homeShotsOnTarget),v(report.homePasses),v(report.homeCompletedPasses),v(report.homePassAccuracyPercent),v(report.homeTackles),v(report.homeFouls),v(report.homeSaves),v(report.homeOffsides),v(report.homePossessionSeconds),v(report.homePossessionPercent));
        row(csv,"TEAM","AWAY",v(report.awayTeam),v(report.awayShots),v(report.awayShotsOnTarget),v(report.awayPasses),v(report.awayCompletedPasses),v(report.awayPassAccuracyPercent),v(report.awayTackles),v(report.awayFouls),v(report.awaySaves),v(report.awayOffsides),v(report.awayPossessionSeconds),v(report.awayPossessionPercent));
        row(csv,"PLAYER","player_id","shots","passes_attempted","passes_completed","pass_accuracy_percent","tackles","fouls","saves","goals","offsides");
        if(report.players!=null) for(MatchReport.PlayerRecord p:report.players) if(p!=null)
            row(csv,"PLAYER",v(p.playerId),v(p.shots),v(p.passes),v(p.completedPasses),v(p.passAccuracyPercent),v(p.tackles),v(p.fouls),v(p.saves),v(p.goals),v(p.offsides));
        row(csv,"EVENT","elapsed_seconds","type","home_team","player_id");
        if(report.events!=null) for(MatchReport.EventRecord e:report.events) if(e!=null)
            row(csv,"EVENT",v(e.elapsedSeconds),v(e.type),v(e.homeTeam),v(e.playerId));
        row(csv,"CARD","player_id","card","reason","elapsed_seconds","dismissed");
        if(report.cards!=null) for(MatchReport.CardRecord c:report.cards) if(c!=null)
            row(csv,"CARD",v(c.playerId),v(c.card),v(c.reason),v(c.elapsedSeconds),v(c.dismissed));
        return csv.toString();
    }

    private static String v(Object value) { return value==null ? "" : String.valueOf(value); }
    private static void row(StringBuilder csv,String... cells) {
        for(int i=0;i<cells.length;i++) {
            if(i>0) csv.append(',');
            String cell=cells[i]==null ? "" : cells[i];
            boolean quote=cell.indexOf(',')>=0||cell.indexOf('"')>=0||cell.indexOf('\n')>=0||cell.indexOf('\r')>=0;
            if(quote) csv.append('"');
            for(int j=0;j<cell.length();j++) { char c=cell.charAt(j); if(c=='"') csv.append('"'); csv.append(c); }
            if(quote) csv.append('"');
        }
        csv.append("\r\n");
    }
}
