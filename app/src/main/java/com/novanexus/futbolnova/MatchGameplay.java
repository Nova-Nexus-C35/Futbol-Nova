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

    public static int findForwardPassTarget(List<MatchSimulation.PlayerState> players,
            int passerIndex, float originX,float originZ,boolean homeTeam) {
        return findForwardPassTarget(players,passerIndex,originX,originZ,homeTeam,true);
    }

    public static int findForwardPassTarget(List<MatchSimulation.PlayerState> players,
            int passerIndex, float originX,float originZ,boolean homeTeam,boolean homeAttacksNegativeZ) {
        if(players==null || passerIndex<0 || passerIndex>=players.size()) return NO_OWNER;
        float attackSign=homeTeam?(homeAttacksNegativeZ?-1f:1f):(homeAttacksNegativeZ?1f:-1f), bestScore=Float.NEGATIVE_INFINITY;
        List<Float> defenders=new ArrayList<>();
        for(MatchSimulation.PlayerState p:players)if(p.homeTeam!=homeTeam)defenders.add(p.z);
        int best=NO_OWNER;
        for(int i=0;i<players.size();i++){
            if(i==passerIndex) continue;
            MatchSimulation.PlayerState p=players.get(i);
            if(p.homeTeam!=homeTeam || "GK".equals(p.role)) continue;
            float d=distance(originX,originZ,p.x,p.z);
            float gain=(originZ-p.z)*attackSign;
            if(d<3f || d>19f || gain<1f) continue;
            MatchRules.OffsideDecision offside=MatchRules.evaluateOffsideAtPass(p.z,originZ,defenders,attackSign,true,"");
            if(offside.offence)continue;
            if(!isPassLaneClear(players,originX,originZ,p.x,p.z,homeTeam,passerIndex,i)) continue;
            float score=gain*1.4f-d*0.35f;
            if(score>bestScore){bestScore=score;best=i;}
        }
        return best;
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
