package com.novanexus.futbolnova;

import com.badlogic.gdx.math.Vector3;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public final class MatchGameplayTest {
 @Test public void possessionRequiresSlowLowBallNearPlayer(){
  MatchSimulation s=new MatchSimulation();assertTrue(s.start("home","away"));
  MatchSimulation.PlayerState p=s.getPlayers().get(9);
  assertEquals(9,MatchGameplay.acquirePossession(s.getPlayers(),p.x,0.55f,p.z,2f));
  assertEquals(MatchGameplay.NO_OWNER,MatchGameplay.acquirePossession(s.getPlayers(),p.x,2f,p.z,1f));
  assertEquals(MatchGameplay.NO_OWNER,MatchGameplay.acquirePossession(s.getPlayers(),p.x,0.55f,p.z,5f));
 }
 @Test public void findsForwardPassTargetForBothAttackDirections(){
  MatchSimulation s=new MatchSimulation();s.start("home","away");List<MatchSimulation.PlayerState> p=s.getPlayers();
  int home=MatchGameplay.findForwardPassTarget(p,7,p.get(7).x,p.get(7).z,true);
  int away=MatchGameplay.findForwardPassTarget(p,18,p.get(18).x,p.get(18).z,false);
  assertTrue(home<11&&home>=0);assertTrue(away>=11);
 }
 @Test public void passLaneRejectsOpponentInPassingCorridor(){
  MatchSimulation s=new MatchSimulation();s.start("home","away");List<MatchSimulation.PlayerState> p=s.getPlayers();
  MatchSimulation.PlayerState passer=p.get(9),receiver=p.get(8),blocker=p.get(18);
  blocker.x=(passer.x+receiver.x)*0.5f;blocker.z=(passer.z+receiver.z)*0.5f;
  assertFalse(MatchGameplay.isPassLaneClear(p,passer.x,passer.z,receiver.x,receiver.z,true,9,8));
 }
 @Test public void passAndShotProduceForwardVelocity(){
  Vector3 pass=MatchGameplay.passVelocity(0,0,0,-10);
  Vector3 shot=MatchGameplay.shotVelocity(true,1.5f);
  assertTrue(pass.z<0);assertTrue(shot.z<0);assertTrue(shot.y>0);
  assertTrue(pass.len()>10f);
 }

 @Test public void shotAndClearanceReverseDirectionAfterHalfTime(){
  Vector3 firstShot=MatchGameplay.shotVelocity(true,0f,true);
  Vector3 secondShot=MatchGameplay.shotVelocity(true,0f,false);
  Vector3 firstClearance=MatchGameplay.clearanceVelocity(true,0f,true);
  Vector3 secondClearance=MatchGameplay.clearanceVelocity(true,0f,false);
  assertTrue(firstShot.z<0f);assertTrue(secondShot.z>0f);
  assertTrue(firstClearance.z<0f);assertTrue(secondClearance.z>0f);
 }

 @Test public void goalkeeperOnlyAttemptsReachableShotsMovingTowardItsOwnGoal(){
  assertTrue(MatchGameplay.canGoalkeeperAttemptSave(0f,49f,0.5f,1.2f,48f,12f,1f));
  assertFalse(MatchGameplay.canGoalkeeperAttemptSave(0f,49f,0.5f,1.2f,48f,-12f,1f));
  assertFalse(MatchGameplay.canGoalkeeperAttemptSave(0f,49f,0.5f,4f,48f,12f,1f));
  assertFalse(MatchGameplay.canGoalkeeperAttemptSave(0f,49f,5f,1.2f,48f,12f,1f));
 }

 @Test public void cpuShootsOnlyFromTheAttackingFinalThirdAndCentralLane(){
  assertTrue(MatchGameplay.shouldAiShoot(40f,4f));
  assertFalse(MatchGameplay.shouldAiShoot(25f,4f));
  assertFalse(MatchGameplay.shouldAiShoot(40f,18f));
  assertFalse(MatchGameplay.shouldAiShoot(Float.NaN,0f));
 }

 @Test public void flagsOffsideReceiverWhenNoOnsideForwardOptionExists(){
  MatchSimulation simulation=new MatchSimulation();assertTrue(simulation.start("home","away"));
  List<MatchSimulation.PlayerState> players=simulation.getPlayers();
  MatchSimulation.PlayerState passer=players.get(7),receiver=players.get(9);
  passer.x=0f;passer.z=0f;receiver.x=0f;receiver.z=-15f;
  for(int i=0;i<players.size();i++){
   MatchSimulation.PlayerState player=players.get(i);
   if(i<11&&i!=7&&i!=9){player.x=28f;player.z=0f;}
   if(i>=11){player.x=25f;player.z=(i%2==0)?-10f:-12f;}
  }
  MatchGameplay.PassTargetDecision decision=MatchGameplay.findForwardPassDecision(players,7,0f,0f,true,true);
  assertEquals(9,decision.receiverIndex);assertTrue(decision.offside);
  assertEquals(MatchGameplay.NO_OWNER,MatchGameplay.findForwardPassTarget(players,7,0f,0f,true,true));
  simulation.switchEnds();
  passer=players.get(7);receiver=players.get(9);
  passer.x=0f;passer.z=0f;receiver.x=0f;receiver.z=15f;
  for(int i=0;i<players.size();i++){
   MatchSimulation.PlayerState player=players.get(i);
   if(i<11&&i!=7&&i!=9){player.x=28f;player.z=0f;}
   if(i>=11){player.x=25f;player.z=(i%2==0)?10f:12f;}
  }
  MatchGameplay.PassTargetDecision secondHalf=MatchGameplay.findForwardPassDecision(players,7,0f,0f,true,false);
  assertEquals(9,secondHalf.receiverIndex);assertTrue(secondHalf.offside);
 }

 @Test public void cpuCarriesIntoSpaceButReleasesUnderPressureOrNearGoal(){
  assertTrue(MatchGameplay.shouldAiCarryBall(12f,7f));
  assertFalse(MatchGameplay.shouldAiCarryBall(34f,7f));
  assertFalse(MatchGameplay.shouldAiCarryBall(20f,3f));
  assertFalse(MatchGameplay.shouldAiCarryBall(Float.NaN,8f));
 }
}
