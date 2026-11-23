package com.novanexus.futbolnova;

import com.badlogic.gdx.math.Vector3;
import java.util.List;
import java.util.ArrayList;

/** Core possession, passing, shooting and tackle decisions ported from the original match controller. */
public final class MatchGameplay {
    public static final int NO_OWNER = -1;
    private MatchGameplay() {}

    public static int acquirePossession(List<MatchSimulation.PlayerState> players,
            float ballX,float ballY,float ballZ,float ballSpeed) {
        if(players==null || ballY>1.1f || ballSpeed>4f) return NO_OWNER;
        int best=NO_OWNER; float bestDistance=1.15f;
        for(int i=0;i<players.size();i++){
            MatchSimulation.PlayerState p=players.get(i);
            float d=distance(p.x,p.z,ballX,ballZ);
            if(d<=bestDistance){bestDistance=d;best=i;}
        }
        return best;
    }

    public static final class PassTargetDecision {
        public final int receiverIndex;
        public final boolean offside;
        private PassTargetDecision(int receiverIndex,boolean offside){
            this.receiverIndex=receiverIndex;this.offside=offside;
        }
    }

    public static int findForwardPassTarget(List<MatchSimulation.PlayerState> players,
            int passerIndex,float originX,float originZ,boolean homeTeam) {
        return findForwardPassTarget(players,passerIndex,originX,originZ,homeTeam,true);
    }

    public static int findForwardPassTarget(List<MatchSimulation.PlayerState> players,
            int passerIndex,float originX,float originZ,boolean homeTeam,boolean homeAttacksNegativeZ) {
        PassTargetDecision decision=findForwardPassDecision(players,passerIndex,originX,originZ,homeTeam,homeAttacksNegativeZ);
        return decision.offside?NO_OWNER:decision.receiverIndex;
    }

    /** Prefers onside receivers; if every viable forward option is offside, returns the best flagged option. */
    public static PassTargetDecision findForwardPassDecision(List<MatchSimulation.PlayerState> players,
            int passerIndex,float originX,float originZ,boolean homeTeam,boolean homeAttacksNegativeZ) {
        if(players==null||passerIndex<0||passerIndex>=players.size())
            return new PassTargetDecision(NO_OWNER,false);
        float attackSign=homeTeam?(homeAttacksNegativeZ?-1f:1f):(homeAttacksNegativeZ?1f:-1f);
        List<Float> defenders=new ArrayList<>();
        for(MatchSimulation.PlayerState player:players)if(player.homeTeam!=homeTeam)defenders.add(player.z);
        int bestOnside=NO_OWNER,bestAny=NO_OWNER;
        float bestOnsideScore=Float.NEGATIVE_INFINITY,bestAnyScore=Float.NEGATIVE_INFINITY;
        boolean bestAnyOffside=false;
        for(int i=0;i<players.size();i++){
            if(i==passerIndex)continue;
            MatchSimulation.PlayerState player=players.get(i);
            if(player.homeTeam!=homeTeam||"GK".equals(player.role))continue;
            float distance=distance(originX,originZ,player.x,player.z);
            float gain=(originZ-player.z)*attackSign;
            if(distance<3f||distance>19f||gain<1f)continue;
            if(!isPassLaneClear(players,originX,originZ,player.x,player.z,homeTeam,passerIndex,i))continue;
            MatchRules.OffsideDecision offside=MatchRules.evaluateOffsideAtPass(
                    player.z,originZ,defenders,attackSign,true,"");
            float score=gain*1.4f-distance*0.35f;
            if(score>bestAnyScore){bestAnyScore=score;bestAny=i;bestAnyOffside=offside.offence;}
            if(!offside.offence&&score>bestOnsideScore){bestOnsideScore=score;bestOnside=i;}
        }
        if(bestOnside!=NO_OWNER)return new PassTargetDecision(bestOnside,false);
        return new PassTargetDecision(bestAny,bestAny!=NO_OWNER&&bestAnyOffside);
    }

    public static boolean isPassLaneClear(List<MatchSimulation.PlayerState> players,
            float x1,float z1,float x2,float z2,boolean passingHome,int passerIndex,int receiverIndex) {
        float sx=x2-x1, sz=z2-z1, length2=sx*sx+sz*sz;
        if(length2<0.001f) return false;
        for(int i=0;i<players.size();i++){
            if(i==passerIndex||i==receiverIndex) continue;
            MatchSimulation.PlayerState p=players.get(i);
            if(p.homeTeam==passingHome) continue;
            float t=clamp(((p.x-x1)*sx+(p.z-z1)*sz)/length2,0f,1f);
            float dx=p.x-(x1+sx*t), dz=p.z-(z1+sz*t);
            if(dx*dx+dz*dz<1.15f*1.15f) return false;
        }
        return true;
    }

    public static Vector3 passVelocity(float fromX,float fromZ,float toX,float toZ) {
        Vector3 direction=new Vector3(toX-fromX,0.13f,toZ-fromZ).nor();
        return direction.scl(12.5f);
    }

    public static Vector3 shotVelocity(boolean homeTeam,float aimX) {
        return shotVelocity(homeTeam,aimX,true);
    }

    public static Vector3 shotVelocity(boolean homeTeam,float aimX,boolean homeAttacksNegativeZ) {
        float attackSign=homeTeam?(homeAttacksNegativeZ?-1f:1f):(homeAttacksNegativeZ?1f:-1f);
        Vector3 target=new Vector3(clamp(aimX,-3.1f,3.1f),0.65f,attackSign*52.5f);
        return target.nor().scl(27f).add(0f,5.8f,0f);
    }

    public static Vector3 clearanceVelocity(boolean homeTeam,float ballX) {
        return clearanceVelocity(homeTeam,ballX,true);
    }

    public static Vector3 clearanceVelocity(boolean homeTeam,float ballX,boolean homeAttacksNegativeZ) {
        float attackSign=homeTeam?(homeAttacksNegativeZ?-1f:1f):(homeAttacksNegativeZ?1f:-1f);
        float side=ballX>=0f?-1f:1f;
        return new Vector3(side*0.32f,0.42f,attackSign).nor().scl(18f);
    }

    /** True when a keeper can reach a low or mid-height shot travelling toward their own goal. */
    /** Keeps CPU shot attempts in a plausible central lane of the attacking final third. */
    public static boolean shouldAiShoot(float goalProgress,float lateralPosition) {
        return Float.isFinite(goalProgress)&&Float.isFinite(lateralPosition)
                &&goalProgress>=34f&&goalProgress<=52.5f&&Math.abs(lateralPosition)<=15f;
    }

    public static boolean canGoalkeeperAttemptSave(float keeperX,float keeperZ,
            float ballX,float ballY,float ballZ,float velocityZ,float ownGoalDirection) {
        if (!Float.isFinite(keeperX)||!Float.isFinite(keeperZ)||!Float.isFinite(ballX)
                ||!Float.isFinite(ballY)||!Float.isFinite(ballZ)||!Float.isFinite(velocityZ)
                ||!Float.isFinite(ownGoalDirection)||Math.abs(ownGoalDirection)<0.5f) return false;
        if (velocityZ*ownGoalDirection<=0.35f||ballY<0f||ballY>3.2f) return false;
        float dx=ballX-keeperX,dz=ballZ-keeperZ;
        return dx*dx+dz*dz<=2.4f*2.4f;
    }

    public static MatchSimulation.TackleResult tackle(MatchSimulation.PlayerState attacker,
            MatchSimulation.PlayerState target,float reach) {
        if(attacker==null||target==null) return null;
        return MatchSimulation.evaluateTackle(attacker.x,attacker.z,attacker.forwardX,attacker.forwardZ,
                target.x,target.z,target.forwardX,target.forwardZ,reach);
    }

    private static float distance(float x1,float z1,float x2,float z2){
        float dx=x1-x2,dz=z1-z2;return (float)Math.sqrt(dx*dx+dz*dz);
    }
    private static float clamp(float v,float min,float max){return Math.max(min,Math.min(max,v));}
}
