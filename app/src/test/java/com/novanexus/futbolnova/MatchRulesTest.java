package com.novanexus.futbolnova;
import org.junit.Test;import java.util.Arrays;import static org.junit.Assert.*;
public class MatchRulesTest{
 @Test public void detectsOffsideAndRestartExemptions(){MatchRules.OffsideDecision o=MatchRules.evaluateOffsideAtPass(-30,-20,Arrays.asList(-25f,-22f,10f),-1,true,"");assertTrue(o.offsidePosition);assertTrue(o.offence);MatchRules.OffsideDecision e=MatchRules.evaluateOffsideAtPass(-30,-20,Arrays.asList(-25f,-22f),-1,true,"throw_in");assertFalse(e.offence);assertEquals("restart_exemption",e.reason);}
 @Test public void detectsGoalsAndCrossbarMiss(){MatchRules.ExitDecision g=MatchRules.classifyBallExit(0,1,-53.2f,false,0.22f);assertEquals(MatchRules.Restart.GOAL,g.restart);assertTrue(g.awardedHome);assertEquals(MatchRules.Restart.GOAL_KICK,MatchRules.classifyBallExit(0,3,-53.2f,true,0.22f).restart);}
 @Test public void classifiesRestarts(){assertEquals(MatchRules.Restart.THROW_IN,MatchRules.classifyBallExit(35,0,12,true,0.22f).restart);assertEquals(MatchRules.Restart.CORNER,MatchRules.classifyBallExit(10,0,53,true,0.22f).restart);assertEquals(MatchRules.Restart.GOAL_KICK,MatchRules.classifyBallExit(10,0,53,false,0.22f).restart);}
 @Test public void requiresFullBallToCrossAndFitInsideGoal(){assertEquals(MatchRules.Restart.IN_PLAY,MatchRules.classifyBallExit(34.1f,0,12,true,0.22f).restart);assertEquals(MatchRules.Restart.IN_PLAY,MatchRules.classifyBallExit(34,0,52.5f,true,0.22f).restart);assertEquals(MatchRules.Restart.GOAL_KICK,MatchRules.classifyBallExit(3.5f,1,53,false,0.22f).restart);}

 @Test public void goalAwardingReversesWhenTeamsSwitchEnds(){
  MatchRules.ExitDecision firstHalf=MatchRules.classifyBallExit(0,1,-53.2f,false,34f,52.5f,3.66f,2.44f,0.22f,true);
  assertEquals(MatchRules.Restart.GOAL,firstHalf.restart);assertTrue(firstHalf.awardedHome);
  MatchRules.ExitDecision secondHalf=MatchRules.classifyBallExit(0,1,53.2f,false,34f,52.5f,3.66f,2.44f,0.22f,false);
  assertEquals(MatchRules.Restart.GOAL,secondHalf.restart);assertTrue(secondHalf.awardedHome);
 }

 @Test public void classifiesFoulsInsideAndOutsideBothPenaltyAreas(){
  assertEquals(MatchRules.Restart.PENALTY,MatchRules.classifyFoulRestart(0f,42f,true,true));
  assertEquals(MatchRules.Restart.FREE_KICK,MatchRules.classifyFoulRestart(0f,20f,true,true));
  assertEquals(MatchRules.Restart.PENALTY,MatchRules.classifyFoulRestart(0f,-42f,false,true));
  assertEquals(MatchRules.Restart.FREE_KICK,MatchRules.classifyFoulRestart(24f,42f,true,true));
  assertEquals(MatchRules.Restart.PENALTY,MatchRules.classifyFoulRestart(0f,-42f,true,false));
 }

 @Test public void restartVelocityUsesAwardedTeamsAttackingDirection(){
  com.badlogic.gdx.math.Vector3 homeKickoff=MatchRules.restartVelocity(MatchRules.Restart.IN_PLAY,true,true,true,0f,0f);
  com.badlogic.gdx.math.Vector3 awayKickoff=MatchRules.restartVelocity(MatchRules.Restart.IN_PLAY,true,false,true,0f,0f);
  com.badlogic.gdx.math.Vector3 homeThrow=MatchRules.restartVelocity(MatchRules.Restart.THROW_IN,false,true,true,20f,0f);
  com.badlogic.gdx.math.Vector3 awayThrow=MatchRules.restartVelocity(MatchRules.Restart.THROW_IN,false,false,true,-20f,0f);
  assertTrue(homeKickoff.z<0f);assertTrue(awayKickoff.z>0f);
  assertTrue(homeThrow.z<0f);assertTrue(awayThrow.z>0f);
  assertTrue(homeThrow.x<0f);assertTrue(awayThrow.x>0f);
 }
 @Test public void cornerGoalKickAndFreeKickHaveExpectedLaunches(){
  com.badlogic.gdx.math.Vector3 corner=MatchRules.restartVelocity(MatchRules.Restart.CORNER,false,true,true,10f,0f);
  com.badlogic.gdx.math.Vector3 goalKick=MatchRules.restartVelocity(MatchRules.Restart.GOAL_KICK,false,false,true,0f,0f);
  com.badlogic.gdx.math.Vector3 freeKick=MatchRules.restartVelocity(MatchRules.Restart.FREE_KICK,false,true,false,0f,0f);
  assertTrue(corner.z<0f);assertEquals(16f,corner.len(),0.001f);
  assertTrue(goalKick.z>0f);assertTrue(goalKick.y>0f);
  assertTrue(freeKick.z>0f);assertTrue(freeKick.y>0f);
 }
 @Test public void penaltyRestartUsesGoalkeeperAndInvalidInputsStaySafe(){
  com.badlogic.gdx.math.Vector3 keeperRight=MatchRules.restartVelocity(MatchRules.Restart.PENALTY,false,true,true,0f,1.8f);
  com.badlogic.gdx.math.Vector3 keeperLeft=MatchRules.restartVelocity(MatchRules.Restart.PENALTY,false,true,true,0f,-1.8f);
  com.badlogic.gdx.math.Vector3 invalid=MatchRules.restartVelocity(MatchRules.Restart.THROW_IN,false,true,true,Float.NaN,0f);
  assertTrue(keeperRight.x<0f);assertTrue(keeperLeft.x>0f);
  assertEquals(0f,invalid.len2(),0.0001f);
 }
}
