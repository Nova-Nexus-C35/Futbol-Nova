package com.novanexus.futbolnova;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Java/libGDX port of the original Fútbol Nova 4-3-3 11v11 movement foundation. */
public final class MatchSimulation {
    public static final float FIELD_HALF_WIDTH = 34f;
    public static final float FIELD_HALF_LENGTH = 52.5f;
    public static final float AI_DECISION_INTERVAL = 0.20f;
    public static final int PLAYERS_PER_TEAM = 11;
    public static final int CONTROLLED_HOME_PLAYER_INDEX = 9;

    private static final String[] ROLES = {"GK","LB","LCB","RCB","RB","DM","CM","AM","LW","ST","RW"};
    private static final float[] FORMATION_X = {0f,-22f,-7f,7f,22f,-10f,0f,10f,-18f,0f,18f};
    private static final float[] FORMATION_Z = {47f,32f,36f,36f,32f,19f,14f,8f,-9f,-16f,-9f};

    public static final class PlayerState {
        public final String playerId, playerName, teamId, role;
        public final int overall;
        public final boolean homeTeam;
        public final int shirtNumber;
        public float homeX, homeZ;
        public float x, z, targetX, targetZ, stamina = 100f, speed, movementSpeed, forwardX, forwardZ = -1f;

        private PlayerState(String playerId, String playerName, String teamId, String role,
                            int overall, boolean homeTeam, int shirtNumber, float x, float z,
                            float pace) {
            this.playerId=playerId; this.playerName=playerName; this.teamId=teamId;
            this.role=role; this.overall=overall; this.homeTeam=homeTeam;
            this.shirtNumber=shirtNumber; this.homeX=x; this.homeZ=z;
            this.x=x; this.z=z; this.targetX=x; this.targetZ=z;
            this.speed="GK".equals(role)?4.2f:4.1f+pace*0.012f;
        }
    }

    private final List<PlayerState> players = new ArrayList<>(22);
    private float decisionClock;
    private int controlledPlayerIndex = CONTROLLED_HOME_PLAYER_INDEX;
    private String homeTeamName = "home", awayTeamName = "away";
    private boolean homeAttacksNegativeZ = true;

    public boolean start(String homeTeamId, String awayTeamId) {
        if (homeTeamId==null || awayTeamId==null || homeTeamId.trim().isEmpty()
                || awayTeamId.trim().isEmpty() || homeTeamId.equals(awayTeamId)) return false;
        homeTeamName=homeTeamId; awayTeamName=awayTeamId; homeAttacksNegativeZ=true;
        players.clear();
        controlledPlayerIndex = CONTROLLED_HOME_PLAYER_INDEX;
        for (int side=0; side<2; side++) {
            boolean home=side==0;
            String teamId=home?homeTeamId:awayTeamId;
            for (int i=0;i<PLAYERS_PER_TEAM;i++) {
                float z=home?FORMATION_Z[i]:-FORMATION_Z[i];
                String id=teamId+"_"+String.format(java.util.Locale.US,"%02d",i+1);
                players.add(new PlayerState(id,"Jugador "+(i+1),teamId,ROLES[i],50,
                        home,i+1,FORMATION_X[i],z,50f));
            }
        }
        decisionClock=AI_DECISION_INTERVAL;
        return validateLineups(players);
    }

    /** Starts a match with two real 22-player squads from the bundled canonical catalogue. */
    public boolean start(OriginalProjectData.TeamRoster home, OriginalProjectData.TeamRoster away) {
        if(home==null||away==null||home.clubId.isEmpty()||away.clubId.isEmpty()
                ||home.clubId.equals(away.clubId))return false;
        List<OriginalProjectData.PlayerRecord> homeXI=selectStartingEleven(home);
        List<OriginalProjectData.PlayerRecord> awayXI=selectStartingEleven(away);
        if(homeXI.size()!=PLAYERS_PER_TEAM||awayXI.size()!=PLAYERS_PER_TEAM)return false;
        players.clear(); controlledPlayerIndex=CONTROLLED_HOME_PLAYER_INDEX;
        homeTeamName=home.clubName;awayTeamName=away.clubName;homeAttacksNegativeZ=true;
        appendRoster(home,homeXI,true);
        appendRoster(away,awayXI,false);
        decisionClock=AI_DECISION_INTERVAL;
        return validateLineups(players);
    }

    private void appendRoster(OriginalProjectData.TeamRoster team,
            List<OriginalProjectData.PlayerRecord> selected,boolean home) {
        for(int i=0;i<PLAYERS_PER_TEAM;i++){
            OriginalProjectData.PlayerRecord record=selected.get(i);
            float z=home?FORMATION_Z[i]:-FORMATION_Z[i];
            float pace=(record.attribute(0)+record.attribute(1))*0.5f;
            players.add(new PlayerState(record.id,record.name,team.clubId,ROLES[i],
                    record.overall,home,i+1,FORMATION_X[i],z,pace));
        }
    }

    private List<OriginalProjectData.PlayerRecord> selectStartingEleven(OriginalProjectData.TeamRoster team) {
        String[][] preferences={{"POR"},{"LI"},{"DFC"},{"DFC"},{"LD"},{"MCD","MC"},
                {"MC","MCD"},{"MCO","MC"},{"EI","MI"},{"DC"},{"ED","MD"}};
        List<OriginalProjectData.PlayerRecord> selected=new ArrayList<>(PLAYERS_PER_TEAM);
        Set<String> used=new HashSet<>();
        for(String[] accepted:preferences){
            OriginalProjectData.PlayerRecord best=null;
            for(OriginalProjectData.PlayerRecord player:team.players){
                if(used.contains(player.id))continue;
                for(String position:accepted){
                    if(position.equals(player.position)&&(best==null||player.overall>best.overall))best=player;
                }
            }
            if(best==null){
                for(OriginalProjectData.PlayerRecord player:team.players)
                    if(!used.contains(player.id)&&(best==null||player.overall>best.overall))best=player;
            }
            if(best==null)return java.util.Collections.emptyList();
            selected.add(best);used.add(best.id);
        }
        return selected;
    }

    public boolean homeAttacksNegativeZ(){return homeAttacksNegativeZ;}

    /** Mirrors both teams at half-time and reverses their attacking direction. */
    public void switchEnds(){
        if(players.size()!=22)return;
        for(PlayerState player:players){
            player.homeZ=-player.homeZ;
            player.z=-player.z;
            player.targetZ=-player.targetZ;
            player.forwardZ=-player.forwardZ;
        }
        homeAttacksNegativeZ=!homeAttacksNegativeZ;
    }

    public String getHomeTeamName(){return homeTeamName;}
    public String getAwayTeamName(){return awayTeamName;}

    public List<PlayerState> getPlayers() { return Collections.unmodifiableList(players); }
    public int getControlledPlayerIndex() { return controlledPlayerIndex; }
    public PlayerState getControlledPlayer() {
        return players.size()==22?players.get(controlledPlayerIndex):null;
    }

    /** Cycles through the ten outfield players on the home team; the goalkeeper is AI-controlled. */
    public int cycleControlledPlayer() {
        if (players.size()!=22) return -1;
        controlledPlayerIndex = controlledPlayerIndex >= 10 ? 1 : controlledPlayerIndex + 1;
        return controlledPlayerIndex;
    }

    public void update(float deltaSeconds,float ballX,float ballZ,float controlledX,float controlledZ) {
        if (players.size()!=22 || !Float.isFinite(deltaSeconds) || deltaSeconds<=0f) return;
        float delta=Math.min(deltaSeconds,0.05f);
        PlayerState controlled=getControlledPlayer();
        float previousX=controlled.x, previousZ=controlled.z;
        controlled.x=clamp(controlledX,-FIELD_HALF_WIDTH+2f,FIELD_HALF_WIDTH-2f);
        controlled.z=clamp(controlledZ,-FIELD_HALF_LENGTH+2f,FIELD_HALF_LENGTH-2f);
        float controlledDx=controlled.x-previousX, controlledDz=controlled.z-previousZ;
        float controlledDistance=(float)Math.sqrt(controlledDx*controlledDx+controlledDz*controlledDz);
        controlled.movementSpeed=controlledDistance/delta;
        if(controlledDistance>0.0001f){controlled.forwardX=controlledDx/controlledDistance;controlled.forwardZ=controlledDz/controlledDistance;}
        else controlled.movementSpeed=0f;

        decisionClock-=delta;
        if (decisionClock<=0f) {
            decisionClock+=AI_DECISION_INTERVAL;
            if (decisionClock<=0f) decisionClock=AI_DECISION_INTERVAL;
            for (int i=0;i<players.size();i++)
                if (i!=controlledPlayerIndex) chooseTarget(i,ballX,ballZ);
        }

        for (int i=0;i<players.size();i++) {
            if (i==controlledPlayerIndex) continue;
            PlayerState player=players.get(i);
            float dx=player.targetX-player.x, dz=player.targetZ-player.z;
            float distance=(float)Math.sqrt(dx*dx+dz*dz);
            if (distance>0.0001f) {
                float fatigue=0.78f+0.22f*(player.stamina/100f);
                float step=Math.min(distance,player.speed*fatigue*delta);
                player.x+=dx/distance*step; player.z+=dz/distance*step;
                player.forwardX=dx/distance; player.forwardZ=dz/distance; player.movementSpeed=step/delta;
                player.stamina=Math.max(0f,player.stamina-delta*0.10f);
            } else { player.movementSpeed=0f; player.stamina=Math.min(100f,player.stamina+delta*0.16f); }
            applySeparation(i,delta);
            player.x=clamp(player.x,-FIELD_HALF_WIDTH+0.6f,FIELD_HALF_WIDTH-0.6f);
            player.z=clamp(player.z,-FIELD_HALF_LENGTH+0.6f,FIELD_HALF_LENGTH-0.6f);
        }
    }

    /** Role-aware support/defensive shape target used by off-ball teammates. */
    public static final class TacticalTarget {
        public final float x,z;
        private TacticalTarget(float x,float z){this.x=x;this.z=z;}
    }

    public static TacticalTarget calculateOffBallTarget(PlayerState player,float ballX,float ballZ,
            boolean teamAttacking,boolean homeAttacksNegativeZ) {
        if(player==null||!Float.isFinite(ballX)||!Float.isFinite(ballZ))
            return new TacticalTarget(0f,0f);
        float attackSign=player.homeTeam?(homeAttacksNegativeZ?-1f:1f):(homeAttacksNegativeZ?1f:-1f);
        float targetX=player.homeX+clamp(ballX*0.14f,-5f,5f);
        float targetZ=player.homeZ+clamp((ballZ-player.homeZ)*0.18f,-6f,6f);
        if(teamAttacking){
            float progress=(ballZ-player.homeZ)*attackSign;
            float advance;
            if("ST".equals(player.role))advance=clamp(progress*0.45f,-2f,12f);
            else if("LW".equals(player.role)||"RW".equals(player.role))advance=clamp(progress*0.38f,-3f,10f);
            else if("AM".equals(player.role))advance=clamp(progress*0.34f,-3f,8f);
            else if("CM".equals(player.role))advance=clamp(progress*0.28f,-3f,6f);
            else if("DM".equals(player.role))advance=clamp(progress*0.20f,-2f,4f);
            else advance=clamp(progress*0.14f,-1f,2.5f);
            targetZ=player.homeZ+attackSign*advance;
            if("LW".equals(player.role)||"RW".equals(player.role))
                targetX=player.homeX+clamp(ballX*0.08f,-2.5f,2.5f);
            else if("ST".equals(player.role))
                targetX=player.homeX+clamp(ballX*0.12f,-3f,3f);
            else targetX=player.homeX+clamp(ballX*0.16f,-5.5f,5.5f);
        }
        return new TacticalTarget(clamp(targetX,-FIELD_HALF_WIDTH+2f,FIELD_HALF_WIDTH-2f),
                clamp(targetZ,-FIELD_HALF_LENGTH+3f,FIELD_HALF_LENGTH-3f));
    }

    private void chooseTarget(int index,float ballX,float ballZ) {
        PlayerState p=players.get(index);
        float attackSign=p.homeTeam?(homeAttacksNegativeZ?-1f:1f):(homeAttacksNegativeZ?1f:-1f);
        if ("GK".equals(p.role)) {
            float ownGoalZ=p.homeTeam?(homeAttacksNegativeZ?49f:-49f):(homeAttacksNegativeZ?-49f:49f);
            float stepOut=0f;
            if (ballZ*attackSign>22f) stepOut=clamp((Math.abs(ballZ)-22f)/27f,0f,1f)*5.5f;
            p.targetX=clamp(ballX*0.42f,-5f,5f);
            p.targetZ=ownGoalZ+attackSign*stepOut;
            return;
        }
        int nearestOwn=nearestTeammateToBall(p.homeTeam,ballX,ballZ);
        int nearestOpposition=nearestTeammateToBall(!p.homeTeam,ballX,ballZ);
        float ownDistance=nearestOwn<0?Float.MAX_VALUE:distance(players.get(nearestOwn).x,
                players.get(nearestOwn).z,ballX,ballZ);
        float oppositionDistance=nearestOpposition<0?Float.MAX_VALUE:distance(
                players.get(nearestOpposition).x,players.get(nearestOpposition).z,ballX,ballZ);
        boolean teamAttacking=ownDistance<=oppositionDistance;
        TacticalTarget shape=calculateOffBallTarget(p,ballX,ballZ,teamAttacking,homeAttacksNegativeZ);
        p.targetX=shape.x;p.targetZ=shape.z;
        if(!teamAttacking&&isDefensiveRole(p.role)){
            TacticalTarget mark=calculateDefensiveMarkTarget(p,players,homeAttacksNegativeZ);
            p.targetX=mark.x;p.targetZ=mark.z;
            if(Math.abs(mark.x-p.homeX)<0.01f&&Math.abs(mark.z-p.homeZ)<0.01f){
                TacticalTarget cover=calculateDefensiveCoverTarget(p,players,ballX,ballZ,
                        homeAttacksNegativeZ);
                p.targetX=cover.x;p.targetZ=cover.z;
            }
        }
        if(nearestOwn==index&&distance(p.homeX,p.homeZ,ballX,ballZ)<29f){
            p.targetX=clamp(ballX,-FIELD_HALF_WIDTH+2f,FIELD_HALF_WIDTH-2f);
            p.targetZ=clamp(ballZ+attackSign*1.25f,-FIELD_HALF_LENGTH+3f,FIELD_HALF_LENGTH-3f);
        }
    }

    private static boolean isDefensiveRole(String role) {
        return "LB".equals(role)||"LCB".equals(role)||"RCB".equals(role)
                ||"RB".equals(role)||"DM".equals(role);
    }

    /** Assigns a nearby opposing attacker to its closest defensive-role player. */
    public static TacticalTarget calculateDefensiveMarkTarget(PlayerState defender,
            List<PlayerState> lineup,boolean homeAttacksNegativeZ) {
        if(defender==null||lineup==null||!isDefensiveRole(defender.role))
            return new TacticalTarget(defender==null?0f:defender.homeX,
                    defender==null?0f:defender.homeZ);
        float attackSign=defender.homeTeam?(homeAttacksNegativeZ?-1f:1f)
                :(homeAttacksNegativeZ?1f:-1f);
        PlayerState assigned=null;
        float assignedDistance=Float.MAX_VALUE;
        for(PlayerState opponent:lineup){
            if(opponent.homeTeam==defender.homeTeam||"GK".equals(opponent.role))continue;
            float defenderDistance=distance(defender.x,defender.z,opponent.x,opponent.z);
            boolean closestMarker=true;
            for(PlayerState teammate:lineup){
                if(teammate.homeTeam!=defender.homeTeam||!isDefensiveRole(teammate.role)
                        ||teammate==defender)continue;
                if(distance(teammate.x,teammate.z,opponent.x,opponent.z)<defenderDistance){
                    closestMarker=false;break;
                }
            }
            if(closestMarker&&defenderDistance<assignedDistance){
                assigned=opponent;assignedDistance=defenderDistance;
            }
        }
        if(assigned==null)return new TacticalTarget(
                clamp(defender.homeX,-FIELD_HALF_WIDTH+2f,FIELD_HALF_WIDTH-2f),
                clamp(defender.homeZ,-FIELD_HALF_LENGTH+3f,FIELD_HALF_LENGTH-3f));
        float targetX=assigned.x;
        float targetZ=assigned.z-attackSign*2.2f;
        float dx=targetX-defender.homeX,dz=targetZ-defender.homeZ;
        float displacement=(float)Math.sqrt(dx*dx+dz*dz);
        if(displacement>13f){targetX=defender.homeX+dx*13f/displacement;
            targetZ=defender.homeZ+dz*13f/displacement;}
        return new TacticalTarget(clamp(targetX,-FIELD_HALF_WIDTH+2f,FIELD_HALF_WIDTH-2f),
                clamp(targetZ,-FIELD_HALF_LENGTH+3f,FIELD_HALF_LENGTH-3f));
    }

    /** Holds a goal-side cover lane when no opponent is assigned to this defender. */
    public static TacticalTarget calculateDefensiveCoverTarget(PlayerState defender,
            List<PlayerState> lineup,float ballX,float ballZ,boolean homeAttacksNegativeZ) {
        if(defender==null||lineup==null||!isDefensiveRole(defender.role)
                ||!Float.isFinite(ballX)||!Float.isFinite(ballZ))
            return new TacticalTarget(defender==null?0f:defender.homeX,
                    defender==null?0f:defender.homeZ);
        float attackSign=defender.homeTeam?(homeAttacksNegativeZ?-1f:1f)
                :(homeAttacksNegativeZ?1f:-1f);
        float nearestDistance=Float.MAX_VALUE;
        PlayerState nearestDefender=null;
        for(PlayerState teammate:lineup){
            if(teammate.homeTeam!=defender.homeTeam||!isDefensiveRole(teammate.role))continue;
            float d=distance(teammate.x,teammate.z,ballX,ballZ);
            if(d<nearestDistance){nearestDistance=d;nearestDefender=teammate;}
        }
        if(nearestDefender==defender)
            return new TacticalTarget(clamp(ballX,-FIELD_HALF_WIDTH+2f,FIELD_HALF_WIDTH-2f),
                    clamp(ballZ+attackSign*1.4f,-FIELD_HALF_LENGTH+3f,FIELD_HALF_LENGTH-3f));
        float targetX=defender.homeX+(ballX-defender.homeX)*0.22f;
        float targetZ=ballZ+attackSign*5f;
        float maxShift=11f;
        float dx=targetX-defender.homeX,dz=targetZ-defender.homeZ;
        float displacement=(float)Math.sqrt(dx*dx+dz*dz);
        if(displacement>maxShift){targetX=defender.homeX+dx*maxShift/displacement;
            targetZ=defender.homeZ+dz*maxShift/displacement;}
        return new TacticalTarget(clamp(targetX,-FIELD_HALF_WIDTH+2f,FIELD_HALF_WIDTH-2f),
                clamp(targetZ,-FIELD_HALF_LENGTH+3f,FIELD_HALF_LENGTH-3f));
    }

    private int nearestTeammateToBall(boolean home,float ballX,float ballZ) {
        int nearest=-1; float best=Float.MAX_VALUE;
        for(int i=0;i<players.size();i++) {
            PlayerState p=players.get(i); if(p.homeTeam!=home) continue;
            float d=distance(p.x,p.z,ballX,ballZ);
            if(d<best){best=d;nearest=i;}
        }
        return nearest;
    }

    private void applySeparation(int index,float delta) {
        PlayerState source=players.get(index);
        float ox=0f,oz=0f;
        for(int j=0;j<players.size();j++) {
            if(j==index) continue;
            PlayerState other=players.get(j);
            float dx=source.x-other.x,dz=source.z-other.z;
            float d=(float)Math.sqrt(dx*dx+dz*dz);
            if(d>0.001f&&d<0.85f) {
                float force=(0.85f-d)*2.2f/d;
                ox+=dx*force; oz+=dz*force;
            }
        }
        float length=(float)Math.sqrt(ox*ox+oz*oz);
        if(length>2f){ox*=2f/length;oz*=2f/length;}
        source.x+=ox*delta; source.z+=oz*delta;
    }

    public static boolean canControlBall(float distance,float height,float speed) {
        return distance<=1.15f&&height<=1.1f&&speed<=4f;
    }

    public static TackleResult evaluateTackle(float ax,float az,float afx,float afz,
            float tx,float tz,float tfx,float tfz,float reach) {
        float dx=tx-ax,dz=tz-az;
        float d=(float)Math.sqrt(dx*dx+dz*dz);
        if(d>reach||d<0.001f)return new TackleResult(false,false,false,d);
        float al=(float)Math.sqrt(afx*afx+afz*afz);
        float facing=al>0.001f?(afx*dx+afz*dz)/(al*d):-1f;
        float tl=(float)Math.sqrt(tfx*tfx+tfz*tfz);
        float behind=tl>0.001f?(tfx*-dx+tfz*-dz)/(tl*d):1f;
        boolean foul=behind< -0.25f&&d<=1.05f;
        return new TackleResult(true,foul,!foul&&facing>=0.15f,d);
    }

    public static boolean validateLineups(List<PlayerState> lineup) {
        if(lineup==null||lineup.size()!=22)return false;
        Set<String> ids=new HashSet<>(); int home=0,away=0;
        for(PlayerState p:lineup) {
            if(p==null||p.playerId==null||p.playerId.isEmpty()||!ids.add(p.playerId))return false;
            if(p.homeTeam)home++;else away++;
        }
        return home==11&&away==11;
    }

    public static final class TackleResult {
        public final boolean inRange,foul,success; public final float distance;
        private TackleResult(boolean inRange,boolean foul,boolean success,float distance) {
            this.inRange=inRange;this.foul=foul;this.success=success;this.distance=distance;
        }
    }
    private static float distance(float x1,float z1,float x2,float z2) {
        float dx=x1-x2,dz=z1-z2;return (float)Math.sqrt(dx*dx+dz*dz);
    }
    private static float clamp(float v,float min,float max){return Math.max(min,Math.min(max,v));}
}
