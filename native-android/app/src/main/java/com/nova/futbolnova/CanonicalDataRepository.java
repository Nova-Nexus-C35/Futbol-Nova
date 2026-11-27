package com.nova.futbolnova;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/** Reads the repository's canonical offline JSON data. It never promotes draft data to playable content. */
final class CanonicalDataRepository {
    static final class Team {
        final String id, name, competitionId, division;
        final JSONArray players;
        Team(String id, String name, String competitionId, String division, JSONArray players) {
            this.id=id; this.name=name; this.competitionId=competitionId; this.division=division; this.players=players;
        }
        boolean isPlayable() {
            if (players == null || players.length() != 22) return false;
            Set<String> ids = new HashSet<>();
            for (int i=0;i<players.length();i++) {
                String id=players.optJSONObject(i)==null?"":players.optJSONObject(i).optString("id","");
                if (id.isEmpty() || !ids.add(id)) return false;
            }
            return true;
        }
    }
    static final class Module {
        final String id,name,category,status,description;
        final boolean downloadable;
        Module(JSONObject o) {
            id=o.optString("id",""); name=o.optString("name","Módulo");
            category=o.optString("category","General"); status=o.optString("status","planned");
            description=o.optString("role",o.optString("description",""));
            downloadable=o.optBoolean("downloadable",false);
        }
        boolean installedOrReady(){return "installed".equals(status)||"available".equals(status)||"ready".equals(status);}
    }

    final List<Team> playableTeams = new ArrayList<>();
    final List<Module> modules = new ArrayList<>();
    final List<JSONObject> customPlayers = new ArrayList<>();
    final Map<String,JSONObject> customPlayersById = new HashMap<>();
    final Set<String> loadedClubIds = new HashSet<>();
    final Set<String> loadedPlayerIds = new HashSet<>();
    final List<String> warnings = new ArrayList<>();
    int baseClubCount, generatedClubCount, customPlayerCount, baseLeagueCount;
    int firstDivisionTeams, secondDivisionTeams, thirdDivisionTeams, skillCount, techniqueCount, officialBallCount;
    JSONObject runtimeManifest;
    String dataVersion="";

    CanonicalDataRepository(Context context) {
        try {
            runtimeManifest=read(context,"native_runtime_manifest.json");
            JSONObject sources=runtimeManifest.optJSONObject("canonical_sources");
            if(sources==null)throw new IllegalStateException("native_runtime_manifest.json no define canonical_sources");
            JSONObject custom=read(context,sources.optString("custom_players","custom_players.json"));
            JSONArray customPlayers=custom.optJSONArray("players");
            customPlayerCount=customPlayers==null?0:customPlayers.length();
            if(customPlayers!=null) for(int i=0;i<customPlayers.length();i++){JSONObject p=customPlayers.getJSONObject(i);this.customPlayers.add(p);this.customPlayersById.put(p.optString("id",""),p);}
            dataVersion=custom.optString("schema_version","unknown");
            JSONObject base=read(context,sources.optString("base_clubs","base_league_clubs_2026.json"));
            baseClubCount=base.optInt("club_count",base.optJSONArray("clubs")==null?0:base.optJSONArray("clubs").length());
            JSONObject global=read(context,sources.optString("season_clubs","global_clubs.generated.json"));
            generatedClubCount=global.optJSONArray("clubs")==null?0:global.optJSONArray("clubs").length();
            JSONObject leagues=read(context,sources.optString("league_content","league_content_manifest.json"));
            baseLeagueCount=leagues.optJSONArray("base_leagues")==null?0:leagues.optJSONArray("base_leagues").length();
            loadModules(context,sources.optString("modules","optional_modules_catalog.json"));
            loadRosterFile(context,sources.optString("cuba_first_division","players/cuba_first_division.json"),"Primera división");
            loadRosterFile(context,sources.optString("cuba_second_division","players/cuba_second_division.json"),"Segunda división");
            loadRosterFile(context,sources.optString("cuba_third_division","cuba_players.json"),"Tercera división");
            JSONObject skills=read(context,sources.optString("skills","skills/skill_catalog.json"));
            JSONArray skillLevels=skills.optJSONArray("levels");if(skillLevels!=null)for(int i=0;i<skillLevels.length();i++){JSONArray levelSkills=skillLevels.optJSONObject(i)==null?null:skillLevels.optJSONObject(i).optJSONArray("skills");if(levelSkills!=null)skillCount+=levelSkills.length();}
            JSONObject techniques=read(context,sources.optString("techniques","skills/common_gesture_techniques.json"));
            JSONArray techniqueArray=techniques.optJSONArray("techniques");JSONArray builtIn=techniques.optJSONArray("built_in_gesture_techniques");techniqueCount=(techniqueArray==null?0:techniqueArray.length())+(builtIn==null?0:builtIn.length());
            JSONObject balls=read(context,sources.optString("balls","league_official_balls.json"));
            JSONArray ballArray=balls.optJSONArray("available_balls");officialBallCount=ballArray==null?0:ballArray.length();
            if(playableTeams.isEmpty()) warnings.add("No se encontraron plantillas completas y únicas para un amistoso.");
        } catch (Exception e) {
            // Fail closed: never expose a partially loaded database as playable.
            playableTeams.clear();
            modules.clear();
            customPlayers.clear();
            customPlayersById.clear();
            loadedClubIds.clear();
            loadedPlayerIds.clear();
            baseClubCount=generatedClubCount=customPlayerCount=baseLeagueCount=0;
            firstDivisionTeams=secondDivisionTeams=thirdDivisionTeams=0;
            skillCount=techniqueCount=officialBallCount=0;
            warnings.clear();
            warnings.add("Datos canónicos incompletos: se deshabilitó el partido hasta corregir el paquete. "+e.getMessage());
        }
    }

    private void loadModules(Context c,String file) throws Exception {
        JSONObject manifest=read(c,file);
        JSONArray a=manifest.optJSONArray("modules");
        if(a!=null) for(int i=0;i<a.length();i++) modules.add(new Module(a.getJSONObject(i)));
    }
    private void loadRosterFile(Context c,String file,String division) throws Exception {
        JSONObject root=read(c,file);
        JSONArray rosters=root.optJSONArray("rosters");
        JSONArray attributeKeys=root.optJSONArray("attribute_keys");
        if(rosters==null)throw new IllegalStateException(file+" no contiene el arreglo rosters");
        if(attributeKeys==null||attributeKeys.length()==0)throw new IllegalStateException(file+" no define attribute_keys");
        int playableBefore=playableTeams.size();
        for(int i=0;i<rosters.length();i++) {
            JSONObject r=rosters.optJSONObject(i);
            if(r==null)throw new IllegalStateException(file+" contiene una plantilla que no es un objeto");
            String clubId=r.optString("club_id","").trim();
            if(clubId.isEmpty()||!loadedClubIds.add(clubId))
                throw new IllegalStateException(file+" contiene un club_id vacío o duplicado: "+clubId);
            JSONArray players=r.optJSONArray("players");
            if(players==null||players.length()!=22)
                throw new IllegalStateException(file+" / "+clubId+" debe contener exactamente 22 jugadores");
            for(int p=0;p<players.length();p++) {
                JSONObject player=players.optJSONObject(p);
                if(player==null)throw new IllegalStateException(file+" / "+clubId+" contiene un jugador inválido");
                String playerId=player.optString("id","").trim();
                if(playerId.isEmpty()||!loadedPlayerIds.add(playerId))
                    throw new IllegalStateException(file+" contiene un player ID vacío o duplicado: "+playerId);
                if(player.optString("name","").trim().isEmpty()||player.optString("position","").trim().isEmpty())
                    throw new IllegalStateException(file+" / "+clubId+" / "+playerId+" no tiene nombre o posición");
                JSONArray attributes=player.optJSONArray("attributes");
                if(attributes==null||attributes.length()!=attributeKeys.length())
                    throw new IllegalStateException(file+" / "+playerId+" no coincide con el contrato de atributos");
                for(int a=0;a<attributes.length();a++) {
                    double value=attributes.optDouble(a,Double.NaN);
                    if(Double.isNaN(value)||Double.isInfinite(value)||value<0||value>99||value!=Math.rint(value))
                        throw new IllegalStateException(file+" / "+playerId+" tiene un atributo fuera de 0..99");
                }
            }
            Team t=new Team(clubId,r.optString("club_name","Club"),
                r.optString("competition_id",""),division,players);
            if(t.isPlayable()) playableTeams.add(t);
            else throw new IllegalStateException(file+" / "+clubId+" no supera la validación de plantilla");
        }
        int verifiedCount=playableTeams.size()-playableBefore;
        if("Primera división".equals(division)) firstDivisionTeams=verifiedCount;
        else if("Segunda división".equals(division)) secondDivisionTeams=verifiedCount;
        else if("Tercera división".equals(division)) thirdDivisionTeams=verifiedCount;
    }
    Team findTeamById(String id) {
        if(id==null||id.trim().isEmpty())return null;
        for(Team team:playableTeams)if(id.equals(team.id))return team;
        return null;
    }
    JSONObject read(Context c,String name) throws Exception {
        try(InputStream in=c.getAssets().open(name); ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] b=new byte[16384]; int n;
            while((n=in.read(b))>0) out.write(b,0,n);
            return new JSONObject(out.toString(StandardCharsets.UTF_8.name()));
        }
    }
    private List<JSONObject> startingElevenAndBench(JSONArray roster) {
        List<JSONObject> ordered=new ArrayList<>();
        Set<String> used=new HashSet<>();
        takePosition(roster,ordered,used,"POR");
        takePosition(roster,ordered,used,"LD");
        takePosition(roster,ordered,used,"DFC");
        takePosition(roster,ordered,used,"DFC");
        takePosition(roster,ordered,used,"LI");
        takePosition(roster,ordered,used,"MCD");
        takePosition(roster,ordered,used,"MC","MCO");
        takePosition(roster,ordered,used,"MCO","MC");
        takePosition(roster,ordered,used,"ED","MD");
        takePosition(roster,ordered,used,"DC");
        takePosition(roster,ordered,used,"EI","MI");
        for(int i=0;i<roster.length();i++){
            JSONObject p=roster.optJSONObject(i);if(p==null)continue;
            String id=p.optString("id","");
            if(!id.isEmpty()&&used.add(id))ordered.add(p);
        }
        return ordered;
    }
    private void takePosition(JSONArray roster,List<JSONObject> out,Set<String> used,String... positions){
        for(int i=0;i<roster.length();i++){
            JSONObject p=roster.optJSONObject(i);if(p==null)continue;
            String id=p.optString("id","");if(id.isEmpty()||used.contains(id))continue;
            String position=p.optString("position","");
            boolean match=false;for(String candidate:positions)if(candidate.equals(position)){match=true;break;}
            if(match){used.add(id);out.add(p);return;}
        }
    }
    void applyTeam(int slotStart, Team team) {
        if(team==null||!team.isPlayable())return;
        List<JSONObject> ordered=startingElevenAndBench(team.players);
        for(int i=0;i<Math.min(22,ordered.size());i++) {
            JSONObject p=ordered.get(i);
            JSONArray values=p.optJSONArray("attributes");
            float acc=attribute(values,0,70),spd=attribute(values,1,70),drib=attribute(values,18,70);
            float aware=attribute(values,22,70),tack=attribute(values,23,70),slide=attribute(values,24,70);
            int overall=p.optInt("estimated_overall",70);
            float height=(float)p.optDouble("height_cm",175);
            JSONObject custom=customPlayersById.get(p.optString("id",""));
            if(custom!=null){
                JSONObject ca=custom.optJSONObject("attributes");
                if(ca!=null){acc=(float)ca.optDouble("acc",acc);spd=(float)ca.optDouble("spd",spd);drib=(float)ca.optDouble("drib",drib);aware=(float)ca.optDouble("aware",aware);tack=(float)ca.optDouble("tack",tack);slide=(float)ca.optDouble("slide",slide);}
                overall=custom.optInt("overall",overall);height=(float)custom.optDouble("height_cm",height);
            }
            MainActivity.nativeSetPlayerProfile(slotStart+i,height,overall,(acc+spd)*.5f,drib,(aware+tack+slide)/3.0f);
        }
    }
    private static float attribute(JSONArray a,int index,float fallback) {
        return a!=null&&index<a.length()?(float)a.optDouble(index,fallback):fallback;
    }
}
