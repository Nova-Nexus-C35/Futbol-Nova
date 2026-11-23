package com.novanexus.futbolnova;

import com.badlogic.gdx.Preferences;

/** Bounded local history of completed match reports. Index zero is newest. */
public final class MatchReportStore {
    private static final String COUNT="count", PREFIX="report_";
    private static final int MAX_REPORTS=20;
    private MatchReportStore() {}

    public static void save(Preferences preferences,MatchReport report) {
        if(preferences==null||report==null)return;
        int count=Math.max(0,Math.min(MAX_REPORTS,preferences.getInteger(COUNT,0)));
        for(int i=Math.min(count,MAX_REPORTS-1)-1;i>=0;i--){
            String previous=preferences.getString(PREFIX+i,"");
            if(!previous.isEmpty())preferences.putString(PREFIX+(i+1),previous);
        }
        preferences.putString(PREFIX+"0",report.toJson());
        preferences.putInteger(COUNT,Math.min(MAX_REPORTS,count+1));
        preferences.flush();
    }

    public static int count(Preferences preferences) {
        return preferences==null?0:Math.max(0,Math.min(MAX_REPORTS,preferences.getInteger(COUNT,0)));
    }

    public static MatchReport get(Preferences preferences,int newestFirstIndex) {
        if(preferences==null||newestFirstIndex<0||newestFirstIndex>=count(preferences))return null;
        return MatchReport.fromJson(preferences.getString(PREFIX+newestFirstIndex,""));
    }
}
