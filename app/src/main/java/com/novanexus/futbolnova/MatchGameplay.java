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
            float gain=(player.z-originZ)*attackSign;
            if(distance<3f||distance>19f||gain<1f)continue;
            if(!isPassLaneClear(players,originX,originZ,player.x,player.z,homeTeam,passerIndex,i))continue;
            MatchRules.OffsideDecision offside=MatchRules.evaluateOffsideAtPass(
                    player.z,originZ,defenders,attackSign,true,"");
            float nearestDefender=nearestOpponentDistance(players,player.x,player.z,homeTeam);
            float score=scorePassOption(gain,distance,nearestDefender,player.role);
            if(score>bestAnyScore){bestAnyScore=score;bestAny=i;bestAnyOffside=offside.offence;}
            if(!offside.offence&&score>bestOnsideScore){bestOnsideScore=score;bestOnside=i;}
        }
        if(bestOnside!=NO_OWNER)return new PassTargetDecision(bestOnside,false);
        return new PassTargetDecision(bestAny,bestAny!=NO_OWNER&&bestAnyOffside);
    }

    /** Scores a pass by forward progress, travel cost, receiver space and role suitability. */
    public static float scorePassOption(float forwardGain,float passDistance,
            float nearestDefenderDistance,String receiverRole) {
        if(!Float.isFinite(forwardGain)||!Float.isFinite(passDistance)
                ||!Float.isFinite(nearestDefenderDistance)||forwardGain<0f
                ||passDistance<0f||nearestDefenderDistance<0f)return Float.NEGATIVE_INFINITY;
        float spaceBonus=Math.min(nearestDefenderDistance,12f)*0.28f;
        float roleBonus=0f;
        if("ST".equals(receiverRole))roleBonus=0.65f;
        else if("LW".equals(receiverRole)||"RW".equals(receiverRole))roleBonus=0.45f;
        else if("AM".equals(receiverRole))roleBonus=0.3f;
        return forwardGain*1.4f-passDistance*0.35f+spaceBonus+roleBonus;
    }

    private static float nearestOpponentDistance(List<MatchSimulation.PlayerState> players,
            float x,float z,boolean homeTeam) {
        float nearest=Float.MAX_VALUE;
        for(MatchSimulation.PlayerState player:players) {
            if(player.homeTeam==homeTeam)continue;
            nearest=Math.min(nearest,distance(x,z,player.x,player.z));
        }
        return nearest==Float.MAX_VALUE?0f:nearest;
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

    /** Keeps CPU shot attempts in a plausible central lane of the attacking final third. */
    public static boolean shouldAiShoot(float goalProgress,float lateralPosition) {
        return Float.isFinite(goalProgress)&&Float.isFinite(lateralPosition)
                &&goalProgress>=34f&&goalProgress<=52.5f&&Math.abs(lateralPosition)<=15f;
    }

    /** CPU carries the ball into space while still outside shooting range and not under pressure. */
    public static boolean shouldAiCarryBall(float goalProgress,float nearestOpponentDistance) {
        return Float.isFinite(goalProgress)&&Float.isFinite(nearestOpponentDistance)
                &&goalProgress>=0f&&goalProgress<34f&&nearestOpponentDistance>=4.5f;
    }

    /**
     * Selects a safe forward dribbling lane for an AI ball carrier.
     * The three candidate lanes are scored by nearest-opponent clearance, while
     * lateral movement is kept modest and the target remains inside the pitch.
     * The returned vector stores the target X/Z in its x/z components.
     */
    public static Vector3 chooseAiCarryTarget(List<MatchSimulation.PlayerState> players,
            int ownerIndex,boolean homeAttacksNegativeZ) {
        if(players==null||ownerIndex<0||ownerIndex>=players.size())return new Vector3();
        MatchSimulation.PlayerState owner=players.get(ownerIndex);
        float attackSign=owner.homeTeam?(homeAttacksNegativeZ?-1f:1f):(homeAttacksNegativeZ?1f:-1f);
        float targetZ=clamp(owner.z+attackSign*5.5f,
                -MatchSimulation.FIELD_HALF_LENGTH+2f,MatchSimulation.FIELD_HALF_LENGTH-2f);
        float[] lateralOffsets={0f,-4.5f,4.5f};
        float bestX=owner.x,bestScore=Float.NEGATIVE_INFINITY;
        for(float offset:lateralOffsets){
            float candidateX=clamp(owner.x+offset,
                    -MatchSimulation.FIELD_HALF_WIDTH+2f,MatchSimulation.FIELD_HALF_WIDTH-2f);
            float nearestOpponent=Float.MAX_VALUE;
            for(MatchSimulation.PlayerState candidate:players){
                if(candidate.homeTeam==owner.homeTeam)continue;
                float dx=candidate.x-candidateX,dz=candidate.z-targetZ;
                nearestOpponent=Math.min(nearestOpponent,(float)Math.sqrt(dx*dx+dz*dz));
            }
            float score=nearestOpponent*1.25f-Math.abs(candidateX-owner.x)*0.20f;
            if(score>bestScore){bestScore=score;bestX=candidateX;}
        }
        return new Vector3(bestX,0f,targetZ);
    }

    /** Builds a penalty shot toward the side opposite the goalkeeper's current lateral position. */
    public static Vector3 penaltyShotVelocity(float goalkeeperX, boolean homeTeam,
            boolean homeAttacksNegativeZ) {
        float attackSign = homeTeam
                ? (homeAttacksNegativeZ ? -1f : 1f)
                : (homeAttacksNegativeZ ? 1f : -1f);
        if (!Float.isFinite(goalkeeperX)) {
            return new Vector3(0f, 2.5f, attackSign * 28f);
        }
        float targetX = goalkeeperX >= 0f ? -2.85f : 2.85f;
        Vector3 direction = new Vector3(targetX, 0.12f, attackSign * 11f).nor();
        return direction.scl(28f).add(0f, 2.5f, 0f);
    }

    /** Selects the far-post lane instead of aiming predictably at the goalkeeper. */
    public static float chooseAiShotAimX(float carrierX,float goalkeeperX,float nearestDefenderDistance) {
        if(!Float.isFinite(carrierX)||!Float.isFinite(goalkeeperX)||!Float.isFinite(nearestDefenderDistance))
            return 0f;
        float direction;
        if(goalkeeperX>0.65f)direction=-1f;
        else if(goalkeeperX< -0.65f)direction=1f;
        else direction=carrierX>=0f?-1f:1f;
        float pressure=nearestDefenderDistance<2.5f?0.45f:0f;
        return clamp(direction*(2.45f+pressure),-3.1f,3.1f);
    }

    /**
     * Predicts whether a shot will cross the goal mouth within the keeper's reaction window
     * and within the keeper's lateral reach. Returns the predicted contact time, or -1.
     * Uses the same gravity constant as BallFlightModel; the short prediction horizon keeps
     * aerodynamic approximation error bounded for the save decision.
     */
    public static float goalkeeperInterceptTime(float keeperX,float keeperZ,
            float ballX,float ballY,float ballZ,float velocityX,float velocityY,float velocityZ,
            float ownGoalDirection) {
        if(!Float.isFinite(keeperX)||!Float.isFinite(keeperZ)||!Float.isFinite(ballX)
                ||!Float.isFinite(ballY)||!Float.isFinite(ballZ)||!Float.isFinite(velocityX)
                ||!Float.isFinite(velocityY)||!Float.isFinite(velocityZ)
                ||!Float.isFinite(ownGoalDirection)||Math.abs(ownGoalDirection)<0.5f)
            return -1f;
        if(velocityZ*ownGoalDirection<=0.35f)return -1f;
        float goalPlane=ownGoalDirection*52.5f-ownGoalDirection*0.65f;
        float time=(goalPlane-ballZ)/velocityZ;
        if(!Float.isFinite(time)||time<0f||time>1.2f)return -1f;
        float predictedX=ballX+velocityX*time;
        float predictedY=ballY+velocityY*time-0.5f*BallFlightModel.DEFAULT_GRAVITY*time*time;
        if(predictedX< -3.66f||predictedX>3.66f||predictedY<0.15f||predictedY>2.44f)
            return -1f;
        if(Math.abs(predictedX-keeperX)>2.7f||Math.abs(goalPlane-keeperZ)>5.5f)
            return -1f;
        return time;
    }

    /** True when a keeper can reach a low or mid-height shot travelling toward their own goal. */
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
