package com.novanexus.futbolnova;

import com.badlogic.gdx.math.Vector3;
import java.util.List;

/** Lightweight deterministic collision response for the mobile match ball. */
public final class BallCollisionModel {
    private static final float POST_THICKNESS_RADIUS=0.08f;
    private static final float PLAYER_CONTACT_RADIUS=0.90f;
    private BallCollisionModel() {}

    /** Resolves contact with both posts and the crossbar of one goal plane. */
    public static boolean resolveGoalFrame(Vector3 position,Vector3 velocity,
            float halfGoalWidth,float goalHeight,float goalLineZ,float ballRadius) {
        if(position==null||velocity==null||!Float.isFinite(ballRadius)||ballRadius<=0f)return false;
        float combined=ballRadius+POST_THICKNESS_RADIUS;
        for(float postX:new float[]{-halfGoalWidth,halfGoalWidth}){
            float dx=position.x-postX,dy=position.y-goalHeight*0.5f,dz=position.z-goalLineZ;
            float distance2=dx*dx+dy*dy+dz*dz;
            if(distance2>combined*combined)continue;
            float distance=(float)Math.sqrt(Math.max(0f,distance2));
            Vector3 normal=new Vector3(dx,dy,dz);
            if(distance<0.0001f)normal.set(0f,0f,-Math.signum(velocity.z==0f?goalLineZ:velocity.z));
            else normal.scl(1f/distance);
            position.mulAdd(normal,combined-distance+0.001f);
            reflect(velocity,normal,0.62f);
            return true;
        }
        float closestX=Math.max(-halfGoalWidth,Math.min(halfGoalWidth,position.x));
        float dx=position.x-closestX,dy=position.y-goalHeight,dz=position.z-goalLineZ;
        float distance2=dx*dx+dy*dy+dz*dz;
        if(distance2>combined*combined)return false;
        float distance=(float)Math.sqrt(Math.max(0f,distance2));
        Vector3 normal=new Vector3(dx,dy,dz);
        if(distance<0.0001f)normal.set(0f,0f,-Math.signum(velocity.z==0f?goalLineZ:velocity.z));
        else normal.scl(1f/distance);
        position.mulAdd(normal,combined-distance+0.001f);
        reflect(velocity,normal,0.58f);
        return true;
    }

    /** Fast, low ball contacts are deflected by player bodies; slow balls remain controllable. */
    public static boolean resolvePlayerContact(Vector3 position,Vector3 velocity,
            List<MatchSimulation.PlayerState> players) {
        if(position==null||velocity==null||players==null||position.y>1.85f
                ||velocity.x*velocity.x+velocity.z*velocity.z<16f)return false;
        for(MatchSimulation.PlayerState player:players){
            if(!player.active)continue;
            float dx=position.x-player.x,dz=position.z-player.z;
            float distance2=dx*dx+dz*dz;
            if(distance2>=PLAYER_CONTACT_RADIUS*PLAYER_CONTACT_RADIUS)continue;
            float distance=(float)Math.sqrt(Math.max(0f,distance2));
            Vector3 normal=new Vector3(dx,0f,dz);
            if(distance<0.0001f)normal.set(-velocity.x,0f,-velocity.z).nor();
            else normal.scl(1f/distance);
            float dot=velocity.x*normal.x+velocity.z*normal.z;
            if(dot>=0f)continue;
            position.x=player.x+normal.x*(PLAYER_CONTACT_RADIUS+0.001f);
            position.z=player.z+normal.z*(PLAYER_CONTACT_RADIUS+0.001f);
            reflect(velocity,normal,0.42f);
            return true;
        }
        return false;
    }

    private static void reflect(Vector3 velocity,Vector3 normal,float restitution){
        float dot=velocity.dot(normal);
        if(dot<0f)velocity.mulAdd(normal,-(1f+restitution)*dot);
    }
}
