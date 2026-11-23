package com.novanexus.futbolnova;

import com.badlogic.gdx.math.Vector3;
import org.junit.Test;
import static org.junit.Assert.*;

public final class BallCollisionModelTest {
    @Test public void postCollisionReflectsBallBackIntoField(){
        Vector3 position=new Vector3(3.66f,1.22f,52.4f);
        Vector3 velocity=new Vector3(0f,0f,10f);
        assertTrue(BallCollisionModel.resolveGoalFrame(position,velocity,3.66f,2.44f,52.5f,0.55f));
        assertTrue(velocity.z<0f);
        assertTrue(position.z<52.5f);
    }

    @Test public void crossbarCollisionDeflectsShot(){
        Vector3 position=new Vector3(0f,2.44f,52.4f);
        Vector3 velocity=new Vector3(0f,3f,10f);
        assertTrue(BallCollisionModel.resolveGoalFrame(position,velocity,3.66f,2.44f,52.5f,0.55f));
        assertTrue(velocity.z<0f);
    }

    @Test public void fastPlayerContactDeflectsButSlowBallRemainsControllable(){
        MatchSimulation simulation=new MatchSimulation();
        assertTrue(simulation.start("home","away"));
        MatchSimulation.PlayerState player=simulation.getPlayers().get(9);
        Vector3 position=new Vector3(player.x,0.6f,player.z+0.5f);
        Vector3 velocity=new Vector3(0f,0f,-8f);
        assertTrue(BallCollisionModel.resolvePlayerContact(position,velocity,simulation.getPlayers()));
        assertTrue(velocity.z>0f);

        Vector3 slowPosition=new Vector3(player.x,0.6f,player.z+0.5f);
        Vector3 slowVelocity=new Vector3(0f,0f,-2f);
        assertFalse(BallCollisionModel.resolvePlayerContact(slowPosition,slowVelocity,simulation.getPlayers()));
    }
}
