package com.novanexus.futbolnova;

import com.novanexus.futbolnova.MatchClock.Phase;

/** Complete, versioned, validated snapshot of a live 3D match runtime. */
public final class MatchRuntimeSnapshot {
    public int schemaVersion=1;
    public MatchSimulation.Snapshot simulation;
    public MatchClock.Snapshot clock;
    public MatchState.Snapshot matchState;
    public MatchStatistics.Snapshot statistics;
    public MatchDiscipline.Snapshot discipline;
    public MatchAdvantage.Snapshot advantage;
    public float[] ballPosition=new float[3],ballVelocity=new float[3],ballSpin=new float[3],controlledPosition=new float[3];
    public float[] aiTackleCooldowns=new float[22],goalkeeperRecoveryTimers=new float[22],goalkeeperDiveDirections=new float[22];
    public int possessionOwnerIndex=-1,pendingOffsideReceiver=-1,pendingPassReceiver=-1;
    public String pendingPasserPlayerId;
    public boolean pendingPasserHomeTeam;
    public float pendingPassClock,aiKickClock,animationClock,tackleCooldown,restartClock,ballRotation;
    public MatchRules.Restart pendingRestart=MatchRules.Restart.IN_PLAY;
    public boolean restartAwardedHome,kickoffRestart,lastTouchHome,matchReportSaved;
    public boolean substitutionMode,substitutionHomeTeam;
    public int substitutionOutgoingIndex=-1,substitutionIncomingIndex,substitutionWindow=1;

    public MatchRuntimeSnapshot() {}

    /** Checks every nested subsystem using temporary instances before any live state is changed. */
    public boolean isValid() {
        if(schemaVersion!=1||simulation==null||clock==null||matchState==null||statistics==null
                ||discipline==null||advantage==null||pendingRestart==null
                ||!validVector(ballPosition)||!validVector(ballVelocity)||!validVector(ballSpin)
                ||!validVector(controlledPosition)||!validArray(aiTackleCooldowns,22)
                ||!validArray(goalkeeperRecoveryTimers,22)||!validArray(goalkeeperDiveDirections,22)
                ||!finite(pendingPassClock,aiKickClock,animationClock,tackleCooldown,restartClock,ballRotation)
                ||pendingPassClock<0f||aiKickClock<0f||animationClock<0f||tackleCooldown<0f||restartClock<0f
                ||possessionOwnerIndex< -1||possessionOwnerIndex>21||pendingOffsideReceiver< -1||pendingOffsideReceiver>21
                ||pendingPassReceiver< -1||pendingPassReceiver>21||substitutionOutgoingIndex< -1
                ||substitutionOutgoingIndex>21||substitutionIncomingIndex<0||substitutionWindow<1||substitutionWindow>3)
            return false;
        if(clock.phase==null||clock.phase==Phase.PRE_MATCH||clock.phase==Phase.FULL_TIME)return false;
        MatchSimulation sim=new MatchSimulation();
        if(!sim.restore(simulation))return false;
        MatchClock testClock;
        try{testClock=new MatchClock(clock.halfDurationSeconds);}catch(RuntimeException invalid){return false;}
        if(!testClock.restore(clock))return false;
        MatchState testState=new MatchState();if(!testState.restore(matchState))return false;
        MatchStatistics testStats=new MatchStatistics();if(!testStats.restore(statistics))return false;
        MatchDiscipline testDiscipline=new MatchDiscipline();if(!testDiscipline.restore(discipline))return false;
        MatchAdvantage testAdvantage=new MatchAdvantage();if(!testAdvantage.restore(advantage))return false;
        if(pendingPassReceiver<0&&pendingPassClock>0.0001f)return false;
        if(pendingPassReceiver>=0&&(pendingPasserPlayerId==null||pendingPasserPlayerId.trim().isEmpty()))return false;
        if(possessionOwnerIndex>=0&&!simulation.players.get(possessionOwnerIndex).active)return false;
        return true;
    }

    private static boolean validVector(float[] values){return validArray(values,3);}
    private static boolean validArray(float[] values,int size){
        if(values==null||values.length!=size)return false;
        for(float value:values)if(!Float.isFinite(value))return false;
        return true;
    }
    private static boolean finite(float... values){for(float value:values)if(!Float.isFinite(value))return false;return true;}
}
