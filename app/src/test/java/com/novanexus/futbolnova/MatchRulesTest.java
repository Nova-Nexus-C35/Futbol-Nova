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
}