package com.novanexus.futbolnova;
import com.badlogic.gdx.math.Vector3;import org.junit.Test;import static org.junit.Assert.*;
public class BallFlightModelTest{
 @Test public void gravityAndFlightAdvance(){BallFlightModel.Step s=BallFlightModel.integrateStep(new Vector3(0,5,0),new Vector3(0,0,10),new Vector3(),0.05f);assertTrue(s.velocity.y<0);assertTrue(s.position.y<5);assertTrue(s.position.z>0);}
 @Test public void spinIsCappedAndDecays(){Vector3 s=BallFlightModel.decaySpin(new Vector3(0,200,0),1f,0.22f);assertTrue(s.len()<=BallFlightModel.MAX_SPIN);assertTrue(s.len()<BallFlightModel.MAX_SPIN);}
 @Test public void zeroVelocityHasNoAerodynamicForce(){assertEquals(0f,BallFlightModel.aerodynamicForce(new Vector3(),new Vector3(),0.0013f,0.0018f,4.8f).len(),0.0001f);}
}
