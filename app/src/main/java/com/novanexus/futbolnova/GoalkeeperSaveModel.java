package com.novanexus.futbolnova;

/**
 * Deterministic goalkeeper interception planner. It predicts the ball at the goal plane,
 * checks the keeper's time-dependent lateral reach and classifies a controlled catch or parry.
 */
public final class GoalkeeperSaveModel {
    public enum Action { CATCH, DEFLECT }

    public static final class Plan {
        public final float time, interceptX, interceptY, interceptZ;
        public final float targetX, targetZ, diveDirection, speed;
        public final Action action;

        private Plan(float time,float x,float y,float z,float targetX,float targetZ,
                float diveDirection,float speed,Action action) {
            this.time=time;interceptX=x;interceptY=y;interceptZ=z;
            this.targetX=targetX;this.targetZ=targetZ;
            this.diveDirection=diveDirection;this.speed=speed;this.action=action;
        }
    }

    private GoalkeeperSaveModel() { }

    public static Plan evaluate(float keeperX,float keeperZ,float ballX,float ballY,float ballZ,
            float velocityX,float velocityY,float velocityZ,float ownGoalDirection) {
        if(!finite(keeperX,keeperZ,ballX,ballY,ballZ,velocityX,velocityY,velocityZ,ownGoalDirection)
                ||Math.abs(ownGoalDirection)<0.5f)return null;
        if(velocityZ*ownGoalDirection<=0.35f)return null;
        float goalPlane=ownGoalDirection*(52.5f-0.65f);
        float time=(goalPlane-ballZ)/velocityZ;
        if(!Float.isFinite(time)||time<0f||time>1.2f)return null;
        float x=ballX+velocityX*time;
        float y=ballY+velocityY*time-0.5f*9.81f*time*time;
        if(!Float.isFinite(x)||!Float.isFinite(y)||x< -3.66f||x>3.66f
                ||y<0.15f||y>2.44f)return null;
        float lateralDistance=Math.abs(x-keeperX);
        float reachable=1.0f+4.2f*time;
        if(lateralDistance>reachable||Math.abs(goalPlane-keeperZ)>5.5f)return null;
        float speed=(float)Math.sqrt(velocityX*velocityX+velocityY*velocityY+velocityZ*velocityZ);
        float direction=x-keeperX;
        if(Math.abs(direction)<0.08f)direction=velocityX==0f?1f:-Math.signum(velocityX);
        Action action=speed<=9f&&y<=1.55f?Action.CATCH:Action.DEFLECT;
        return new Plan(time,x,y,goalPlane,clamp(x,-3.0f,3.0f),
                clamp(goalPlane-ownGoalDirection*1.0f,-50f,50f),
                direction<0f?-1f:1f,speed,action);
    }

    private static boolean finite(float... values) {
        for(float value:values)if(!Float.isFinite(value))return false;
        return true;
    }

    private static float clamp(float value,float min,float max) {
        return Math.max(min,Math.min(max,value));
    }
}
