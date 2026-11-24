package com.novanexus.futbolnova;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/** Reads the original Fútbol Nova sports data migrated from Nova-Workspace. */
public final class OriginalProjectData {
    private static final String ROOT = "data/original/";

    private OriginalProjectData() {}

    public static Summary loadBundledSummary() {
        String leagues = Gdx.files.internal(ROOT + "cuba_leagues.json").readString("UTF-8");
        String players = Gdx.files.internal(ROOT + "cuba_players.json").readString("UTF-8");
        String custom = Gdx.files.internal(ROOT + "custom_players.json").readString("UTF-8");
        return summarize(leagues, players, custom);
    }

    static Summary summarize(String leaguesJson, String playersJson, String customPlayersJson) {
        JsonReader reader = new JsonReader();
        JsonValue leagueRoot = reader.parse(leaguesJson);
        JsonValue competitions = requiredArray(leagueRoot, "competitions");
        int clubCount = 0;
        for (JsonValue competition : competitions) {
            JsonValue clubs = competition.get("clubs");
            if (clubs != null && clubs.isArray()) clubCount += clubs.size;
        }

        JsonValue playerRoot = reader.parse(playersJson);
        JsonValue rosters = requiredArray(playerRoot, "rosters");
        int playerCount = playerRoot.getInt("player_count", 0);
        if (playerCount <= 0) {
            for (JsonValue roster : rosters) {
                JsonValue players = roster.get("players");
                if (players != null && players.isArray()) playerCount += players.size;
            }
        }

        JsonValue customRoot = reader.parse(customPlayersJson);
        JsonValue customPlayers = requiredArray(customRoot, "players");
        return new Summary(leagueRoot.getString("schema_version", "unknown"),
                competitions.size, clubCount, rosters.size, playerCount, customPlayers.size);
    }

    private static JsonValue requiredArray(JsonValue root, String name) {
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException("Invalid original Fútbol Nova JSON root");
        }
        JsonValue value = root.get(name);
        if (value == null || !value.isArray()) {
            throw new IllegalArgumentException("Missing original Fútbol Nova array: " + name);
        }
        return value;
    }

    /** Loads only squads actually present in the canonical player catalogue. */
    public static java.util.List<TeamRoster> loadPlayableRosters() {
        String json = Gdx.files.internal(ROOT + "cuba_players.json").readString("UTF-8");
        return parsePlayableRosters(json);
    }

    static java.util.List<TeamRoster> parsePlayableRosters(String json) {
        JsonValue root = new JsonReader().parse(json);
        JsonValue rosters = requiredArray(root, "rosters");
        java.util.List<TeamRoster> result = new java.util.ArrayList<>();
        for (JsonValue roster : rosters) {
            JsonValue playerArray = roster.get("players");
            if (playerArray == null || !playerArray.isArray() || playerArray.size < 11) continue;
            java.util.List<PlayerRecord> players = new java.util.ArrayList<>();
            for (JsonValue player : playerArray) {
                JsonValue rawAttributes = player.get("attributes");
                float[] attributes = new float[rawAttributes != null && rawAttributes.isArray() ? rawAttributes.size : 0];
                for (int i = 0; i < attributes.length; i++) attributes[i] = rawAttributes.get(i).asFloat();
                players.add(new PlayerRecord(player.getString("id", ""),
                        player.getString("name", "Jugador"),
                        player.getString("position", ""),
                        player.getInt("estimated_overall", 0), attributes));
            }
            result.add(new TeamRoster(roster.getString("club_id", ""),
                    roster.getString("club_name", "Equipo"),
                    roster.getString("competition_id", ""),
                    roster.getInt("division_level", 0), players));
        }
        return java.util.Collections.unmodifiableList(result);
    }

    public static final class PlayerRecord {
        public final String id, name, position;
        public final int overall;
        private final float[] attributes;
        PlayerRecord(String id, String name, String position, int overall, float[] attributes) {
            this.id=id; this.name=name; this.position=position; this.overall=overall;
            this.attributes=attributes.clone();
        }
        float[] copyAttributes() { return attributes.clone(); }
        public float attribute(int index) {
            return index >= 0 && index < attributes.length ? attributes[index] : 50f;
        }
    }

    public static final class TeamRoster {
        public final String clubId, clubName, competitionId;
        public final int divisionLevel;
        public final java.util.List<PlayerRecord> players;
        TeamRoster(String clubId, String clubName, String competitionId, int divisionLevel,
                   java.util.List<PlayerRecord> players) {
            this.clubId=clubId; this.clubName=clubName; this.competitionId=competitionId;
            this.divisionLevel=divisionLevel;
            this.players=java.util.Collections.unmodifiableList(new java.util.ArrayList<>(players));
        }
    }

    public static final class Summary {
        public final String cubaLeagueSchemaVersion;
        public final int cubaCompetitions, cubaClubs, cubaRosterGroups, cubaPlayers, customPlayers;

        Summary(String schemaVersion, int competitions, int clubs, int rosterGroups,
                int players, int customPlayers) {
            this.cubaLeagueSchemaVersion = schemaVersion;
            this.cubaCompetitions = competitions;
            this.cubaClubs = clubs;
            this.cubaRosterGroups = rosterGroups;
            this.cubaPlayers = players;
            this.customPlayers = customPlayers;
        }

        @Override public String toString() {
            return "Cuba schema " + cubaLeagueSchemaVersion + ", competitions=" + cubaCompetitions
                    + ", clubs=" + cubaClubs + ", rosterGroups=" + cubaRosterGroups
                    + ", players=" + cubaPlayers + ", customPlayers=" + customPlayers;
        }
    }
}
