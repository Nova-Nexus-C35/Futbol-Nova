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

    @Test public void dismissedPlayerKeepsStableIndexButStopsParticipating(){
        MatchSimulation simulation=new MatchSimulation();assertTrue(simulation.start("home","away"));
        MatchSimulation.PlayerState dismissed=simulation.getPlayers().get(9);
        String id=dismissed.playerId;
        assertEquals(11,simulation.getActivePlayerCount(true));
        assertTrue(simulation.dismissPlayer(id));
        assertFalse(dismissed.active);
        assertEquals(10,simulation.getActivePlayerCount(true));
        assertEquals(22,simulation.getPlayers().size());
        assertNotEquals(id,simulation.getControlledPlayer().playerId);
        assertFalse(simulation.dismissPlayer(id));
    }

    @Test public void canonicalSquadKeepsUnusedPlayersOnBenchAndAllowsLegalSubstitution() {
        java.util.List<OriginalProjectData.PlayerRecord> homeSquad=new java.util.ArrayList<>();
        java.util.List<OriginalProjectData.PlayerRecord> awaySquad=new java.util.ArrayList<>();
        for(int i=0;i<14;i++){
            String position=i==0?"POR":(i<3?"DFC":(i<5?"LI":"MC"));
            homeSquad.add(new OriginalProjectData.PlayerRecord("H"+i,"Home "+i,position,70+i,new float[]{70f,75f}));
            awaySquad.add(new OriginalProjectData.PlayerRecord("A"+i,"Away "+i,position,65+i,new float[]{65f,70f}));
        }
        OriginalProjectData.TeamRoster home=new OriginalProjectData.TeamRoster("H","Home FC","league",1,homeSquad);
        OriginalProjectData.TeamRoster away=new OriginalProjectData.TeamRoster("A","Away FC","league",1,awaySquad);
        MatchSimulation simulation=new MatchSimulation();
        assertTrue(simulation.start(home,away));
        assertEquals(3,simulation.getBenchPlayers(true).size());
        assertEquals(3,simulation.getBenchPlayers(false).size());
        MatchSimulation.PlayerState outgoing=simulation.getPlayers().get(9);
        String outgoingId=outgoing.playerId;
        assertTrue(simulation.substitutePlayer(true,outgoingId,"H13",1,false));
        MatchSimulation.PlayerState replacement=simulation.getPlayers().get(9);
        assertEquals("H13",replacement.playerId);
        assertEquals("Home 13",replacement.playerName);
        assertEquals(outgoing.role,replacement.role);
        assertEquals(22,simulation.getPlayers().size());
        assertEquals(11,simulation.getActivePlayerCount(true));
        assertEquals(2,simulation.getBenchPlayers(true).size());
        assertEquals(1,simulation.getSubstitutionsUsed(true));
        assertEquals(1,simulation.getSubstitutionWindowsUsed(true));
    }

    @Test public void substitutionsEnforceFivePlayersThreeWindowsAndHalftimeException() {
        java.util.List<OriginalProjectData.PlayerRecord> homeSquad=new java.util.ArrayList<>();
        java.util.List<OriginalProjectData.PlayerRecord> awaySquad=new java.util.ArrayList<>();
        for(int i=0;i<17;i++){
            String position=i==0?"POR":"MC";
            homeSquad.add(new OriginalProjectData.PlayerRecord("H"+i,"Home "+i,position,60+i,new float[]{60f,60f}));
            awaySquad.add(new OriginalProjectData.PlayerRecord("A"+i,"Away "+i,position,60+i,new float[]{60f,60f}));
        }
        MatchSimulation simulation=new MatchSimulation();
        assertTrue(simulation.start(
                new OriginalProjectData.TeamRoster("H","Home","L",1,homeSquad),
                new OriginalProjectData.TeamRoster("A","Away","L",1,awaySquad)));
        assertFalse(simulation.substitutePlayer(true,"H9","H16",4,false));
        assertTrue(simulation.substitutePlayer(true,"H9","H16",1,false));
        assertTrue(simulation.substitutePlayer(true,"H10","H15",1,false));
        assertTrue(simulation.substitutePlayer(true,"H11","H14",2,false));
        assertTrue(simulation.substitutePlayer(true,"H12","H13",3,false));
        assertTrue(simulation.substitutePlayer(true,"H8","H12",0,true));
        assertEquals(5,simulation.getSubstitutionsUsed(true));
        assertEquals(3,simulation.getSubstitutionWindowsUsed(true));
        assertFalse(simulation.substitutePlayer(true,"H7","H8",0,true));
    }

    @Test public void substitutionsRejectWrongTeamDismissedOutgoingAndNonBenchIncoming() {
        java.util.List<OriginalProjectData.PlayerRecord> homeSquad=new java.util.ArrayList<>();
        java.util.List<OriginalProjectData.PlayerRecord> awaySquad=new java.util.ArrayList<>();
        for(int i=0;i<13;i++){
            String position=i==0?"POR":"MC";
            homeSquad.add(new OriginalProjectData.PlayerRecord("H"+i,"Home "+i,position,70,new float[]{70f}));
            awaySquad.add(new OriginalProjectData.PlayerRecord("A"+i,"Away "+i,position,70,new float[]{70f}));
        }
        MatchSimulation simulation=new MatchSimulation();
        assertTrue(simulation.start(
                new OriginalProjectData.TeamRoster("H","Home","L",1,homeSquad),
                new OriginalProjectData.TeamRoster("A","Away","L",1,awaySquad)));
        assertFalse(simulation.substitutePlayer(true,"A9","H12",1,false));
        assertFalse(simulation.substitutePlayer(true,"H9","A12",1,false));
        assertTrue(simulation.dismissPlayer("H9"));
        assertFalse(simulation.substitutePlayer(true,"H9","H12",1,false));
        assertFalse(simulation.substitutePlayer(true,"H10","H10",1,false));
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

    @Test public void attackingShapeSendsStrikerForwardAndKeepsWingersWide() {
        MatchSimulation simulation=new MatchSimulation();
        assertTrue(simulation.start("home","away"));
        MatchSimulation.PlayerState striker=simulation.getPlayers().get(9);
        MatchSimulation.PlayerState winger=simulation.getPlayers().get(8);
        MatchSimulation.TacticalTarget strikerTarget=MatchSimulation.calculateOffBallTarget(
                striker,0f,-30f,true,true);
        MatchSimulation.TacticalTarget wingerTarget=MatchSimulation.calculateOffBallTarget(
                winger,0f,-30f,true,true);
        assertTrue(strikerTarget.z<striker.homeZ);
        assertTrue(wingerTarget.z<winger.homeZ);
        assertTrue(Math.abs(wingerTarget.x)>Math.abs(strikerTarget.x)+10f);
    }

    @Test public void defensiveShapeCompactsTowardBallWithoutLeavingPitch() {
        MatchSimulation simulation=new MatchSimulation();
        assertTrue(simulation.start("home","away"));
        MatchSimulation.PlayerState centreBack=simulation.getPlayers().get(2);
        MatchSimulation.TacticalTarget target=MatchSimulation.calculateOffBallTarget(
                centreBack,20f,-35f,false,true);
        assertTrue(target.z<centreBack.homeZ);
        assertTrue(Math.abs(target.x)<=MatchSimulation.FIELD_HALF_WIDTH-2f);
        assertTrue(Math.abs(target.z)<=MatchSimulation.FIELD_HALF_LENGTH-3f);
    }

    @Test public void defensiveRoleTracksItsClosestAttackingThreatGoalSide() {
        MatchSimulation simulation=new MatchSimulation();
        assertTrue(simulation.start("home","away"));
        java.util.List<MatchSimulation.PlayerState> players=simulation.getPlayers();
        MatchSimulation.PlayerState centreBack=players.get(2);
        MatchSimulation.PlayerState striker=players.get(20);
        centreBack.x=-7f;centreBack.z=36f;
        players.get(1).x=-22f;players.get(1).z=32f;
        players.get(3).x=7f;players.get(3).z=36f;
        players.get(4).x=22f;players.get(4).z=32f;
        players.get(5).x=15f;players.get(5).z=40f;
        striker.x=-8f;striker.z=22f;
        MatchSimulation.TacticalTarget target=MatchSimulation.calculateDefensiveMarkTarget(
                centreBack,players,true);
        assertEquals(striker.x,target.x,0.001f);
        assertTrue("defender should position on the goal side of the attacker",target.z>striker.z);
        assertEquals(2.2f,target.z-striker.z,0.001f);
        assertTrue(Math.abs(target.x)<=MatchSimulation.FIELD_HALF_WIDTH-2f);
        assertTrue(Math.abs(target.z)<=MatchSimulation.FIELD_HALF_LENGTH-3f);
    }

    @Test public void unassignedDefenderDropsIntoGoalSideCoverLane() {
        MatchSimulation simulation=new MatchSimulation();
        assertTrue(simulation.start("home","away"));
        java.util.List<MatchSimulation.PlayerState> players=simulation.getPlayers();
        MatchSimulation.PlayerState cover=players.get(4);
        MatchSimulation.PlayerState leftBack=players.get(1);
        MatchSimulation.PlayerState centreBack=players.get(2);
        MatchSimulation.PlayerState rightCentreBack=players.get(3);
        MatchSimulation.PlayerState dm=players.get(5);
        // Keep the other defensive roles clustered around the ball so this fullback
        // is not the closest marker for any of the opposition's outfield players.
        leftBack.x=-5f;leftBack.z=18f;
        centreBack.x=-2f;centreBack.z=18f;
        rightCentreBack.x=2f;rightCentreBack.z=18f;
        dm.x=5f;dm.z=18f;
        cover.x=22f;cover.z=32f;
        MatchSimulation.TacticalTarget target=MatchSimulation.calculateDefensiveCoverTarget(
                cover,players,0f,10f,true);
        assertTrue("cover should shift toward the ball",Math.abs(target.x)<Math.abs(cover.homeX));
        assertTrue("cover should remain on the goal side",target.z>10f);
        assertTrue("cover movement must remain bounded",
                Math.hypot(target.x-cover.homeX,target.z-cover.homeZ)<=11.001);
        assertTrue(Math.abs(target.x)<=MatchSimulation.FIELD_HALF_WIDTH-2f);
        assertTrue(Math.abs(target.z)<=MatchSimulation.FIELD_HALF_LENGTH-3f);
    }

    @Test public void roleAwareShapeReversesAfterHalfTime() {
        MatchSimulation simulation=new MatchSimulation();
        assertTrue(simulation.start("home","away"));
        simulation.switchEnds();
        MatchSimulation.PlayerState striker=simulation.getPlayers().get(9);
        MatchSimulation.TacticalTarget target=MatchSimulation.calculateOffBallTarget(
                striker,0f,30f,true,false);
        assertTrue(target.z>striker.homeZ);
    }
}