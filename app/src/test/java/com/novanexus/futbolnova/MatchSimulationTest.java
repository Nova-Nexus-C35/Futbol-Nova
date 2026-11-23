package com.novanexus.futbolnova;

import org.junit.Test;
import static org.junit.Assert.*;

public final class MatchSimulationTest {
    @Test public void createsTwoValidElevenPlayer433Lineups() {
        MatchSimulation simulation=new MatchSimulation();
        assertTrue(simulation.start("home","away"));
        assertEquals(22,simulation.getPlayers().size());
        int home=0,away=0;
        for(MatchSimulation.PlayerState p:simulation.getPlayers()){if(p.homeTeam)home++;else away++;}
        assertEquals(11,home);assertEquals(11,away);
        assertEquals("ST",simulation.getControlledPlayer().role);
        assertEquals("GK",simulation.getPlayers().get(0).role);
        assertEquals(-47f,simulation.getPlayers().get(11).z,0.001f);
    }
    @Test public void rejectsInvalidTeamIdentifiers() {
        MatchSimulation s=new MatchSimulation();
        assertFalse(s.start("","away"));assertFalse(s.start("same","same"));assertFalse(s.start("home",null));
    }
    @Test public void nearestAttackerPressesBallAndPlayersStayInsidePitch() {
        MatchSimulation s=new MatchSimulation();assertTrue(s.start("home","away"));
        MatchSimulation.PlayerState winger=s.getPlayers().get(8);
        float start=distance(winger.x,winger.z,-17f,-12f);
        for(int i=0;i<60;i++)s.update(1f/60f,-17f,-12f,s.getControlledPlayer().x,s.getControlledPlayer().z);
        float end=distance(winger.x,winger.z,-17f,-12f);
        assertTrue("nearest winger should press toward ball",end<start);
        for(MatchSimulation.PlayerState p:s.getPlayers()){
            assertTrue(Math.abs(p.x)<=MatchSimulation.FIELD_HALF_WIDTH);
            assertTrue(Math.abs(p.z)<=MatchSimulation.FIELD_HALF_LENGTH);
        }
    }
    @Test public void ignoresInvalidDeltaTime() {
        MatchSimulation s=new MatchSimulation();s.start("home","away");
        float x=s.getPlayers().get(0).x;
        s.update(Float.NaN,0f,0f,0f,0f);s.update(-1f,0f,0f,0f,0f);
        assertEquals(x,s.getPlayers().get(0).x,0.0001f);
    }
    @Test public void ballControlAndTackleRulesMatchOriginalThresholds() {
        assertTrue(MatchSimulation.canControlBall(1.1f,1.0f,3.9f));
        assertFalse(MatchSimulation.canControlBall(1.2f,1.0f,3.0f));
        MatchSimulation.TackleResult result=MatchSimulation.evaluateTackle(
                0f,0f,0f,-1f,0f,-1f,0f,1f,1.7f);
        assertTrue(result.inRange);assertTrue(result.success);assertFalse(result.foul);
        MatchSimulation.TackleResult out=MatchSimulation.evaluateTackle(
                0f,0f,0f,-1f,0f,-2f,0f,1f,1.7f);
        assertFalse(out.inRange);
    }

    @Test public void cyclesControlAcrossOutfieldPlayersWithoutSelectingGoalkeeper() {
        MatchSimulation simulation=new MatchSimulation();
        assertTrue(simulation.start("home","away"));
        assertEquals(9,simulation.getControlledPlayerIndex());
        assertEquals(10,simulation.cycleControlledPlayer());
        assertEquals("RW",simulation.getControlledPlayer().role);
        assertEquals(1,simulation.cycleControlledPlayer());
        assertEquals("LB",simulation.getControlledPlayer().role);
        for(int i=0;i<20;i++)simulation.cycleControlledPlayer();
        assertNotEquals(0,simulation.getControlledPlayerIndex());
        assertTrue(simulation.getControlledPlayerIndex()<=10);
    }

    private static float distance(float x1,float z1,float x2,float z2){
        float dx=x1-x2,dz=z1-z2;return (float)Math.sqrt(dx*dx+dz*dz);
    }

    @Test public void initializesMatchFromCanonicalTeamRosters() {
        java.util.List<OriginalProjectData.PlayerRecord> squadA=new java.util.ArrayList<>();
        java.util.List<OriginalProjectData.PlayerRecord> squadB=new java.util.ArrayList<>();
        String[] positions={"POR","LI","DFC","DFC","LD","MCD","MC","MCO","EI","DC","ED"};
        for(int i=0;i<positions.length;i++){
            squadA.add(new OriginalProjectData.PlayerRecord("A"+i,"A Player "+i,positions[i],70,new float[]{80f,75f}));
            squadB.add(new OriginalProjectData.PlayerRecord("B"+i,"B Player "+i,positions[i],65,new float[]{65f,60f}));
        }
        OriginalProjectData.TeamRoster home=new OriginalProjectData.TeamRoster("A","Santiago FC","league",3,squadA);
        OriginalProjectData.TeamRoster away=new OriginalProjectData.TeamRoster("B","Habana FC","league",3,squadB);
        MatchSimulation simulation=new MatchSimulation();
        assertTrue(simulation.start(home,away));
        assertEquals("Santiago FC",simulation.getHomeTeamName());
        assertEquals("Habana FC",simulation.getAwayTeamName());
        assertEquals("A Player 9",simulation.getPlayers().get(9).playerName);
        assertEquals("B Player 0",simulation.getPlayers().get(11).playerName);
        assertEquals("A9",simulation.getControlledPlayer().playerId);
    }

    @Test public void halfTimeSwitchesEndsAndAttackingDirection() {
        MatchSimulation simulation=new MatchSimulation();
        assertTrue(simulation.start("home","away"));
        float homeZ=simulation.getPlayers().get(0).z;
        float awayZ=simulation.getPlayers().get(11).z;
        assertTrue(simulation.homeAttacksNegativeZ());
        simulation.switchEnds();
        assertFalse(simulation.homeAttacksNegativeZ());
        assertEquals(-homeZ,simulation.getPlayers().get(0).z,0.001f);
        assertEquals(-awayZ,simulation.getPlayers().get(11).z,0.001f);
        simulation.switchEnds();
        assertTrue(simulation.homeAttacksNegativeZ());
        assertEquals(homeZ,simulation.getPlayers().get(0).z,0.001f);
    }
}