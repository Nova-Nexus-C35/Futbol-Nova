package com.novanexus.futbolnova;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.List;

/**
 * First native Android 3D vertical slice. All geometry is procedural and uses
 * original placeholder shapes; no third-party game assets are bundled.
 */
public final class FutbolNovaGame extends ApplicationAdapter {
    private static final float FIELD_HALF_WIDTH = 34f;
    private static final float FIELD_HALF_LENGTH = 52.5f;
    private static final float BALL_RADIUS = 0.55f;
    /** 30 match-seconds per real second: a complete 90-minute match lasts about 3 minutes. */
    private static final float MATCH_TIME_SCALE = 30f;

    private final MatchState matchState = new MatchState();
    private final MatchStatistics matchStatistics = new MatchStatistics();
    private final MatchClock matchClock = new MatchClock();
    private final MatchSimulation matchSimulation = new MatchSimulation();
    private List<OriginalProjectData.TeamRoster> playableRosters = new ArrayList<>();
    private TeamSelection teamSelection;
    private final List<Model> ownedModels = new ArrayList<>();
    private final List<PlayerVisual> playerVisuals = new ArrayList<>();
    private final List<ModelInstance> environmentModels = new ArrayList<>();
    private final Vector3 ballPosition = new Vector3(0f, BALL_RADIUS, 0f);
    private final Vector3 ballVelocity = new Vector3();
    private final Vector3 ballSpin = new Vector3();
    private final Vector3 controlledPosition = new Vector3();
    private final Vector3 cameraFollowPosition = new Vector3();
    private final Vector3 cameraLookTarget = new Vector3();
    private final float[] aiTackleCooldowns = new float[22];

    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private Environment environment;
    private ModelInstance fieldInstance;
    private ModelInstance ballInstance;
    private int possessionOwnerIndex = MatchGameplay.NO_OWNER;
    private float aiKickClock = 0.9f;
    private float animationClock;
    private float tackleCooldown;
    private float restartClock;
    private float ballRotation;
    private MatchRules.Restart pendingRestart = MatchRules.Restart.IN_PLAY;
    private boolean restartAwardedHome;
    private boolean kickoffRestart;
    private boolean lastTouchHome = true;
    private boolean disposed;

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.15f);

        camera = new PerspectiveCamera(55f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 78f, 86f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 0.1f;
        camera.far = 240f;
        camera.update();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.78f, 0.78f, 0.78f, 1f));
        environment.add(new DirectionalLight().set(0.85f, 0.85f, 0.85f, -0.4f, -1f, -0.25f));

        createProceduralPitchAndTeams();
        try {
            OriginalProjectData.Summary summary = OriginalProjectData.loadBundledSummary();
            Gdx.app.log("FutbolNova", "Original project data loaded: " + summary);
        } catch (RuntimeException error) {
            Gdx.app.error("FutbolNova", "Original project data could not be loaded", error);
        }
    }

    private void createProceduralPitchAndTeams() {
        ModelBuilder builder = new ModelBuilder();
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        Model field = builder.createBox(68f,0.35f,105f,new Material(ColorAttribute.createDiffuse(new Color(0.10f,0.42f,0.19f,1f))),attributes);
        own(field); fieldInstance=new ModelInstance(field); fieldInstance.transform.setToTranslation(0f,-0.22f,0f);

        Material standMat=new Material(ColorAttribute.createDiffuse(new Color(0.055f,0.075f,0.095f,1f)));
        Model longStand=builder.createBox(9f,8f,112f,standMat,attributes);
        Model endStand=builder.createBox(88f,8f,9f,standMat,attributes);
        own(longStand);own(endStand);
        addEnvironment(longStand,-41.5f,3.8f,0f);addEnvironment(longStand,41.5f,3.8f,0f);
        addEnvironment(endStand,0f,3.8f,-58f);addEnvironment(endStand,0f,3.8f,58f);

        Material white=new Material(ColorAttribute.createDiffuse(Color.WHITE));
        Model post=builder.createBox(0.14f,2.44f,0.14f,white,attributes);
        Model bar=builder.createBox(7.32f,0.14f,0.14f,white,attributes);
        own(post);own(bar);
        for(float z:new float[]{-53.1f,53.1f}){addEnvironment(post,-3.66f,1.22f,z);addEnvironment(post,3.66f,1.22f,z);addEnvironment(bar,0f,2.44f,z);}

        Material homeShirt=new Material(ColorAttribute.createDiffuse(new Color(0.10f,0.35f,0.95f,1f)));
        Material awayShirt=new Material(ColorAttribute.createDiffuse(new Color(0.92f,0.16f,0.12f,1f)));
        Material homeShorts=new Material(ColorAttribute.createDiffuse(new Color(0.055f,0.09f,0.42f,1f)));
        Material awayShorts=new Material(ColorAttribute.createDiffuse(new Color(0.12f,0.12f,0.15f,1f)));
        Material skin=new Material(ColorAttribute.createDiffuse(new Color(0.55f,0.34f,0.23f,1f)));
        Material hair=new Material(ColorAttribute.createDiffuse(new Color(0.055f,0.045f,0.04f,1f)));
        Material socks=new Material(ColorAttribute.createDiffuse(new Color(0.92f,0.92f,0.92f,1f)));
        Material boots=new Material(ColorAttribute.createDiffuse(new Color(0.72f,0.08f,0.08f,1f)));
        Model homeTorso=builder.createCylinder(0.58f,0.68f,0.32f,8,homeShirt,attributes);
        Model awayTorso=builder.createCylinder(0.58f,0.68f,0.32f,8,awayShirt,attributes);
        Model homeShortModel=builder.createCylinder(0.44f,0.25f,0.29f,8,homeShorts,attributes);
        Model awayShortModel=builder.createCylinder(0.44f,0.25f,0.29f,8,awayShorts,attributes);
        Model headModel=builder.createSphere(0.32f,0.34f,0.30f,10,8,skin,attributes);
        Model hairModel=builder.createSphere(0.32f,0.12f,0.30f,10,6,hair,attributes);
        Model homeArm=builder.createCylinder(0.14f,0.44f,0.14f,6,homeShirt,attributes);
        Model awayArm=builder.createCylinder(0.14f,0.44f,0.14f,6,awayShirt,attributes);
        Model legModel=builder.createCylinder(0.19f,0.47f,0.19f,6,socks,attributes);
        Model bootModel=builder.createBox(0.18f,0.12f,0.34f,boots,attributes);
        for(Model model:new Model[]{homeTorso,awayTorso,homeShortModel,awayShortModel,headModel,hairModel,homeArm,awayArm,legModel,bootModel})own(model);

        boolean canonicalLineupsLoaded=false;
        try {
            playableRosters=OriginalProjectData.loadPlayableRosters();
            if(playableRosters.size()>=2){
                teamSelection=new TeamSelection(playableRosters);
                canonicalLineupsLoaded=matchSimulation.start(teamSelection.getHome(),teamSelection.getAway());
            }
            if(canonicalLineupsLoaded)Gdx.app.log("FutbolNova","Playable squads: "+matchSimulation.getHomeTeamName()+" vs "+matchSimulation.getAwayTeamName());
        } catch(RuntimeException error) {
            Gdx.app.error("FutbolNova","Canonical player rosters unavailable; using placeholder teams",error);
        }
        if(!canonicalLineupsLoaded&&!matchSimulation.start("nova_home","nova_away"))
            throw new IllegalStateException("Could not initialize match lineups");
        List<MatchSimulation.PlayerState> lineup=matchSimulation.getPlayers();
        for(int i=0;i<lineup.size();i++){
            MatchSimulation.PlayerState state=lineup.get(i);
            playerVisuals.add(new PlayerVisual(state.homeTeam?homeTorso:awayTorso,
                    state.homeTeam?homeShortModel:awayShortModel,headModel,hairModel,
                    state.homeTeam?homeArm:awayArm,legModel,bootModel,state.shirtNumber));
            if(i==matchSimulation.getControlledPlayerIndex())controlledPosition.set(state.x,0f,state.z);
        }
        Model ball=builder.createSphere(1.1f,1.1f,1.1f,16,16,new Material(ColorAttribute.createDiffuse(Color.WHITE)),attributes);
        own(ball);ballInstance=new ModelInstance(ball);
    }

    private void addEnvironment(Model model,float x,float y,float z){
        ModelInstance instance=new ModelInstance(model);instance.transform.setToTranslation(x,y,z);environmentModels.add(instance);
    }

    private void own(Model model) {
        ownedModels.add(model);
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);
        updateTouchControls(delta);
        boolean matchRunning = matchClock.isPlaying() && !matchClock.isPaused();
        if (matchRunning) {
            matchClock.update(delta * MATCH_TIME_SCALE);
            matchState.update(delta);
            updateBall(delta);
            matchSimulation.update(delta, ballPosition.x, ballPosition.z,
                    controlledPosition.x, controlledPosition.z);
            updateGameplay(delta);
        }
        List<MatchSimulation.PlayerState> lineup = matchSimulation.getPlayers();
        animationClock+=delta;
        for(int i=0;i<lineup.size()&&i<playerVisuals.size();i++){
            MatchSimulation.PlayerState state=lineup.get(i);
            float yaw=(float)Math.atan2(-state.forwardX,-state.forwardZ);
            playerVisuals.get(i).update(state.x,state.z,yaw,animationClock,state.movementSpeed);
        }

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.035f, 0.07f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        camera.position.lerp(cameraFollowPosition.set(ballPosition.x*0.20f,78f,86f+ballPosition.z*0.18f),Math.min(1f,delta*0.55f));
        camera.lookAt(cameraLookTarget.set(ballPosition.x*0.18f,0f,ballPosition.z*0.16f));
        camera.update();
        ballRotation += ballVelocity.len()*delta*35f;
        ballInstance.transform.setToTranslation(ballPosition);
        ballInstance.transform.rotate(Vector3.X,ballRotation);
        ballInstance.transform.rotate(Vector3.Y,ballRotation*0.7f);
        modelBatch.begin(camera);
        modelBatch.render(fieldInstance,environment);
        for(ModelInstance structure:environmentModels)modelBatch.render(structure,environment);
        for(PlayerVisual player:playerVisuals)player.render(modelBatch,environment);
        modelBatch.render(ballInstance,environment);
        modelBatch.end();

        drawPitchMarkings();
        drawHud();
    }

    private final boolean[] pointerWasTouched = new boolean[20];
    private int movementPointer = -1;
    private boolean matchActionPointerDown;

    /**
     * Independent pointer tracking allows the left movement stick and a right-side
     * action button to be used at the same time on Android multi-touch screens.
     */
    private void updateTouchControls(float delta){
        int width=Gdx.graphics.getWidth(),height=Gdx.graphics.getHeight();
        if(width<=0||height<=0)return;

        boolean movementStillDown=false;
        boolean matchActionDown=false;
        for(int pointer=0;pointer<pointerWasTouched.length;pointer++){
            boolean touched=Gdx.input.isTouched(pointer);
            if(!touched){
                pointerWasTouched[pointer]=false;
                continue;
            }
            float x=Gdx.input.getX(pointer),y=Gdx.input.getY(pointer);
            if(y<height*0.16f){
                matchActionDown=true;
                if(!matchActionPointerDown)handleMatchAction(x,y,width,height);
            }else if(x<width*0.58f){
                if(movementPointer<0||movementPointer==pointer){
                    movementPointer=pointer;
                    movementStillDown=true;
                    float radius=Math.max(1f,width*0.22f);
                    float horizontal=(x-width*0.28f)/radius;
                    float vertical=(height*0.50f-y)/radius;
                    Vector3 movement=new Vector3(horizontal,0f,-vertical);
                    if(movement.len2()>1f)movement.nor();
                    controlledPosition.mulAdd(movement,10f*delta);
                    controlledPosition.x=Math.max(-FIELD_HALF_WIDTH+2f,Math.min(FIELD_HALF_WIDTH-2f,controlledPosition.x));
                    controlledPosition.z=Math.max(-FIELD_HALF_LENGTH+2f,Math.min(FIELD_HALF_LENGTH-2f,controlledPosition.z));
                }
            }else if(!pointerWasTouched[pointer]){
                if(y<height*0.34f)performShot();
                else if(y<height*0.68f)performPass();
                else performTackle();
            }
            pointerWasTouched[pointer]=true;
        }
        if(!movementStillDown)movementPointer=-1;
        matchActionPointerDown=matchActionDown;
    }

    /** Top HUD strip: kickoff/continue at left, pause/resume at right. */
    private void handleMatchAction(float x,float y,int width,int height){
        MatchClock.Phase phase=matchClock.getPhase();
        if(phase==MatchClock.Phase.PRE_MATCH){
            if(x<width*0.32f){cycleHomeRoster();return;}
            if(x<width*0.72f){cycleAwayRoster();return;}
            matchClock.start();
            return;
        }
        if(x>=width*0.72f){
            if(matchClock.isPlaying()){
                if(matchClock.isPaused())matchClock.resume(); else matchClock.pause();
            }
            return;
        }
        if(x>=width*0.32f){
            int selected=matchSimulation.cycleControlledPlayer();
            MatchSimulation.PlayerState player=matchSimulation.getControlledPlayer();
            if(selected>=0&&player!=null)controlledPosition.set(player.x,0f,player.z);
            return;
        }
        if(phase==MatchClock.Phase.HALF_TIME){
            matchClock.startSecondHalf();
            matchSimulation.switchEnds();
            MatchSimulation.PlayerState selectedAfterSwitch=matchSimulation.getControlledPlayer();
            if(selectedAfterSwitch!=null)controlledPosition.set(selectedAfterSwitch.x,0f,selectedAfterSwitch.z);
            ballPosition.set(0f,BALL_RADIUS,0f);ballVelocity.setZero();ballSpin.setZero();
            possessionOwnerIndex=MatchGameplay.NO_OWNER;restartAwardedHome=false;
            kickoffRestart=true;pendingRestart=MatchRules.Restart.IN_PLAY;restartClock=1.4f;
        }else if(phase==MatchClock.Phase.FULL_TIME){
            resetMatch();
        }
    }

    private void cycleHomeRoster(){
        if(teamSelection!=null&&teamSelection.cycleHome())rebuildSelectedLineups();
    }

    private void cycleAwayRoster(){
        if(teamSelection!=null&&teamSelection.cycleAway())rebuildSelectedLineups();
    }

    private void rebuildSelectedLineups(){
        if(teamSelection!=null&&matchSimulation.start(teamSelection.getHome(),teamSelection.getAway())){
            MatchSimulation.PlayerState selected=matchSimulation.getControlledPlayer();
            if(selected!=null)controlledPosition.set(selected.x,0f,selected.z);
        }
    }

    private void resetMatch(){
        matchClock.reset();matchState.reset();matchStatistics.reset();
        if(teamSelection!=null)rebuildSelectedLineups();
        ballPosition.set(0f,BALL_RADIUS,0f);ballVelocity.setZero();ballSpin.setZero();
        possessionOwnerIndex=MatchGameplay.NO_OWNER;aiKickClock=0.9f;tackleCooldown=0f;
        restartClock=0f;pendingRestart=MatchRules.Restart.IN_PLAY;kickoffRestart=false;
        for(int i=0;i<aiTackleCooldowns.length;i++)aiTackleCooldowns[i]=0f;
    }

    private void performShot(){
        if(possessionOwnerIndex!=matchSimulation.getControlledPlayerIndex())return;
        float attackSign=matchSimulation.homeAttacksNegativeZ()?-1f:1f;
        ballPosition.set(controlledPosition.x,BALL_RADIUS,controlledPosition.z+attackSign*0.62f);
        ballVelocity.set(MatchGameplay.shotVelocity(true,-ballPosition.x*0.22f,matchSimulation.homeAttacksNegativeZ()));ballSpin.set(0f,18f,0f);
        matchStatistics.recordShot(true);
        possessionOwnerIndex=MatchGameplay.NO_OWNER;lastTouchHome=true;
    }

    private void performPass(){
        if(possessionOwnerIndex!=matchSimulation.getControlledPlayerIndex())return;
        List<MatchSimulation.PlayerState> lineup=matchSimulation.getPlayers();
        int receiver=MatchGameplay.findForwardPassTarget(lineup,matchSimulation.getControlledPlayerIndex(),controlledPosition.x,controlledPosition.z,true,matchSimulation.homeAttacksNegativeZ());
        float attackSign=matchSimulation.homeAttacksNegativeZ()?-1f:1f;
        ballPosition.set(controlledPosition.x,BALL_RADIUS,controlledPosition.z+attackSign*0.62f);
        matchStatistics.recordPassAttempt(true);
        if(receiver==MatchGameplay.NO_OWNER)ballVelocity.set(MatchGameplay.clearanceVelocity(true,controlledPosition.x,matchSimulation.homeAttacksNegativeZ()));
        else{MatchSimulation.PlayerState target=lineup.get(receiver);ballVelocity.set(MatchGameplay.passVelocity(controlledPosition.x,controlledPosition.z,target.x,target.z));}
        ballSpin.setZero();possessionOwnerIndex=MatchGameplay.NO_OWNER;lastTouchHome=true;
    }

    private void performTackle(){
        if(tackleCooldown>0f||possessionOwnerIndex<0||possessionOwnerIndex==matchSimulation.getControlledPlayerIndex())return;
        List<MatchSimulation.PlayerState> lineup=matchSimulation.getPlayers();
        MatchSimulation.PlayerState attacker=lineup.get(matchSimulation.getControlledPlayerIndex()),target=lineup.get(possessionOwnerIndex);
        if(target.homeTeam)return;
        MatchSimulation.TackleResult result=MatchGameplay.tackle(attacker,target,1.7f);tackleCooldown=0.75f;
        if(result!=null&&result.success){matchStatistics.recordTackle(true);possessionOwnerIndex=matchSimulation.getControlledPlayerIndex();lastTouchHome=true;}
        else if(result!=null&&result.foul){matchStatistics.recordFoul(true);possessionOwnerIndex=MatchGameplay.NO_OWNER;ballVelocity.set(0f,2f,3f);lastTouchHome=true;}
    }

    private void updateBall(float delta) {
        BallFlightModel.Step flight=BallFlightModel.integrateStep(ballPosition,ballVelocity,ballSpin,delta);
        ballPosition.set(flight.position);ballVelocity.set(flight.velocity);ballSpin.set(flight.spin);
        if(ballPosition.y<BALL_RADIUS){
            ballPosition.y=BALL_RADIUS;
            if(ballVelocity.y<0f)ballVelocity.y*=-0.38f;
            ballVelocity.x*=0.91f;ballVelocity.z*=0.91f;
        }
        if(tryGoalkeeperSave())return;
        if(Math.abs(ballPosition.x)-BALL_RADIUS>FIELD_HALF_WIDTH
                ||Math.abs(ballPosition.z)-BALL_RADIUS>FIELD_HALF_LENGTH){
            MatchRules.ExitDecision exit=MatchRules.classifyBallExit(
                    ballPosition.x,ballPosition.y,ballPosition.z,lastTouchHome,FIELD_HALF_WIDTH,FIELD_HALF_LENGTH,3.66f,2.44f,BALL_RADIUS,matchSimulation.homeAttacksNegativeZ());
            possessionOwnerIndex=MatchGameplay.NO_OWNER;
            ballVelocity.setZero();ballSpin.setZero();
            if(exit.restart==MatchRules.Restart.GOAL){
                matchState.addGoal(exit.awardedHome);
                matchStatistics.recordGoal(exit.awardedHome);
                ballPosition.set(0f,BALL_RADIUS,0f);
                restartAwardedHome=!exit.awardedHome;
                kickoffRestart=true;
                pendingRestart=MatchRules.Restart.IN_PLAY;
                restartClock=1.4f;
            }else{
                ballPosition.set(exit.x,exit.restart==MatchRules.Restart.THROW_IN?1.75f:BALL_RADIUS,exit.z);
                pendingRestart=exit.restart;
                restartAwardedHome=exit.awardedHome;
                kickoffRestart=false;
                restartClock=1.2f;
            }
        }
        if(ballVelocity.len2()<0.08f&&ballPosition.y<=BALL_RADIUS+0.02f)ballVelocity.setZero();
    }

    private boolean tryGoalkeeperSave(){
        List<MatchSimulation.PlayerState> lineup=matchSimulation.getPlayers();
        if(lineup.size()!=22)return false;
        for(int keeperIndex:new int[]{0,11}){
            MatchSimulation.PlayerState keeper=lineup.get(keeperIndex);
            float ownGoalDirection=keeper.homeTeam
                    ?(matchSimulation.homeAttacksNegativeZ()?1f:-1f)
                    :(matchSimulation.homeAttacksNegativeZ()?-1f:1f);
            if(!MatchGameplay.canGoalkeeperAttemptSave(keeper.x,keeper.z,ballPosition.x,
                    ballPosition.y,ballPosition.z,ballVelocity.z,ownGoalDirection))continue;
            float speed=ballVelocity.len();
            ballPosition.set(keeper.x,Math.min(1.25f,Math.max(BALL_RADIUS,ballPosition.y)),
                    keeper.z-ownGoalDirection*1.05f);
            ballSpin.setZero();
            lastTouchHome=keeper.homeTeam;
            matchStatistics.recordSave(keeper.homeTeam);
            if(speed<9f){
                possessionOwnerIndex=keeperIndex;
                ballVelocity.setZero();
            }else{
                possessionOwnerIndex=MatchGameplay.NO_OWNER;
                ballVelocity.set(ballVelocity.x*0.35f,
                        Math.max(2.5f,Math.abs(ballVelocity.y)*0.35f),
                        -ownGoalDirection*Math.max(5f,speed*0.48f));
            }
            return true;
        }
        return false;
    }

    private void updateGameplay(float delta){
        tackleCooldown=Math.max(0f,tackleCooldown-delta);
        for(int i=0;i<aiTackleCooldowns.length;i++)aiTackleCooldowns[i]=Math.max(0f,aiTackleCooldowns[i]-delta);
        if(restartClock>0f){
            restartClock-=delta;
            if(restartClock<=0f){
                float attackSign=restartAwardedHome?(matchSimulation.homeAttacksNegativeZ()?-1f:1f):(matchSimulation.homeAttacksNegativeZ()?1f:-1f);
                if(kickoffRestart)ballVelocity.set(0f,1.2f,attackSign*5.5f);
                else if(pendingRestart==MatchRules.Restart.THROW_IN)
                    ballVelocity.set(ballPosition.x>=0f?-5.5f:5.5f,5.2f,attackSign*4.5f);
                else if(pendingRestart==MatchRules.Restart.CORNER)
                    ballVelocity.set(-ballPosition.x*0.22f,5.0f,attackSign*13f).nor().scl(16f);
                else if(pendingRestart==MatchRules.Restart.GOAL_KICK)
                    ballVelocity.set(0f,6.2f,attackSign*22f);
                lastTouchHome=restartAwardedHome;
                pendingRestart=MatchRules.Restart.IN_PLAY;
                kickoffRestart=false;
            }
            return;
        }
        List<MatchSimulation.PlayerState> lineup=matchSimulation.getPlayers();
        if(possessionOwnerIndex<0)
            possessionOwnerIndex=MatchGameplay.acquirePossession(lineup,ballPosition.x,ballPosition.y,ballPosition.z,ballVelocity.len());
        if(possessionOwnerIndex<0||possessionOwnerIndex>=lineup.size()){
            possessionOwnerIndex=MatchGameplay.NO_OWNER;return;
        }

        // Nearest opponent may win the ball with a correctly oriented tackle; cooldown prevents repeated instant steals.
        MatchSimulation.PlayerState currentOwner=lineup.get(possessionOwnerIndex);
        int nearestDefender=MatchGameplay.NO_OWNER;float nearestDistance=1.7f;
        for(int i=0;i<lineup.size();i++){
            if(i==possessionOwnerIndex||lineup.get(i).homeTeam==currentOwner.homeTeam||aiTackleCooldowns[i]>0f)continue;
            MatchSimulation.PlayerState defender=lineup.get(i);
            float dx=defender.x-currentOwner.x,dz=defender.z-currentOwner.z;
            float d=(float)Math.sqrt(dx*dx+dz*dz);
            if(d<nearestDistance){nearestDistance=d;nearestDefender=i;}
        }
        if(nearestDefender!=MatchGameplay.NO_OWNER){
            MatchSimulation.TackleResult tackle=MatchGameplay.tackle(lineup.get(nearestDefender),currentOwner,1.7f);
            aiTackleCooldowns[nearestDefender]=1.35f;
            if(tackle!=null&&tackle.success){
                possessionOwnerIndex=nearestDefender;
                currentOwner=lineup.get(possessionOwnerIndex);
                aiKickClock=1.0f;
            }else if(tackle!=null&&tackle.foul){
                possessionOwnerIndex=MatchGameplay.NO_OWNER;
                ballVelocity.set(0f,1.8f,2.5f);
                lastTouchHome=!currentOwner.homeTeam;
                return;
            }
        }

        MatchSimulation.PlayerState owner=lineup.get(possessionOwnerIndex);
        float ownerAttackSign=owner.homeTeam?(matchSimulation.homeAttacksNegativeZ()?-1f:1f):(matchSimulation.homeAttacksNegativeZ()?1f:-1f);
        ballPosition.set(owner.x,BALL_RADIUS,owner.z+ownerAttackSign*0.62f);
        ballVelocity.setZero();ballSpin.setZero();lastTouchHome=owner.homeTeam;
        if(possessionOwnerIndex==matchSimulation.getControlledPlayerIndex())return;
        aiKickClock-=delta;if(aiKickClock>0f)return;
        int receiver=MatchGameplay.findForwardPassTarget(lineup,possessionOwnerIndex,owner.x,owner.z,owner.homeTeam,matchSimulation.homeAttacksNegativeZ());
        if(receiver!=MatchGameplay.NO_OWNER){
            MatchSimulation.PlayerState target=lineup.get(receiver);
            ballVelocity.set(MatchGameplay.passVelocity(owner.x,owner.z,target.x,target.z));
        }else ballVelocity.set(MatchGameplay.clearanceVelocity(owner.homeTeam,owner.x,matchSimulation.homeAttacksNegativeZ()));
        possessionOwnerIndex=MatchGameplay.NO_OWNER;aiKickClock=1.15f;
    }

    private void drawPitchMarkings() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        float y = 0.015f;
        line(-34f, y, -52.5f, 34f, y, -52.5f);
        line(-34f, y, 52.5f, 34f, y, 52.5f);
        line(-34f, y, -52.5f, -34f, y, 52.5f);
        line(34f, y, -52.5f, 34f, y, 52.5f);
        line(-34f, y, 0f, 34f, y, 0f);
        circleOnPitch(0f, 0f, 9.15f, y);
        drawGoalNet(-53.2f,y); drawGoalNet(53.2f,y);
        line(-20.16f, y, -52.5f, 20.16f, y, -52.5f);
        line(-20.16f, y, -52.5f, -20.16f, y, -35f);
        line(20.16f, y, -52.5f, 20.16f, y, -35f);
        line(-20.16f, y, 52.5f, 20.16f, y, 52.5f);
        line(-20.16f, y, 52.5f, -20.16f, y, 35f);
        line(20.16f, y, 52.5f, 20.16f, y, 35f);
        shapeRenderer.end();
    }

    private void drawGoalNet(float z,float y){
        float back=z+Math.signum(z)*2.2f;
        line(-3.66f,y,z,-3.66f,2.44f,z);line(3.66f,y,z,3.66f,2.44f,z);
        line(-3.66f,2.44f,z,3.66f,2.44f,z);
        line(-3.66f,y,z,-3.66f,y,back);line(3.66f,y,z,3.66f,y,back);
        line(-3.66f,2.44f,z,-3.66f,2.44f,back);line(3.66f,2.44f,z,3.66f,2.44f,back);
        line(-3.66f,y,back,3.66f,y,back);line(-3.66f,2.44f,back,3.66f,2.44f,back);
    }

    private void circleOnPitch(float centerX, float centerZ, float radius, float y) {
        int segments = 64;
        for (int i = 0; i < segments; i++) {
            float a1 = (float) (Math.PI * 2.0 * i / segments);
            float a2 = (float) (Math.PI * 2.0 * (i + 1) / segments);
            line(centerX + (float) Math.cos(a1) * radius, y, centerZ + (float) Math.sin(a1) * radius,
                    centerX + (float) Math.cos(a2) * radius, y, centerZ + (float) Math.sin(a2) * radius);
        }
    }

    private void line(float x1, float y1, float z1, float x2, float y2, float z2) {
        shapeRenderer.line(x1, y1, z1, x2, y2, z2);
    }

    private void drawHud() {
        spriteBatch.begin();
        float matchSeconds=matchClock.getTotalElapsedSeconds();
        int matchMinute=(int)(matchSeconds/60f);
        String clock=String.format(java.util.Locale.US,"%02d:%02d",matchMinute,(int)(matchSeconds%60f));
        font.draw(spriteBatch, matchSimulation.getHomeTeamName()+"  "+matchState.getHomeGoals()
                +" - "+matchState.getAwayGoals()+"  "+matchSimulation.getAwayTeamName()+"  |  "+clock,
                18f,Gdx.graphics.getHeight()-18f);
        MatchSimulation.PlayerState activePlayer=matchSimulation.getControlledPlayer();
        if(activePlayer!=null)font.draw(spriteBatch,"JUGADOR: "+activePlayer.playerName
                +"  #"+activePlayer.shirtNumber+"  "+activePlayer.role,
                18f,Gdx.graphics.getHeight()-70f);
        font.draw(spriteBatch, "IZQUIERDA: MOVER   DERECHA: ARRIBA TIRO | CENTRO PASE | ABAJO ROBO", 18f, 28f);
        font.draw(spriteBatch,"TIROS "+matchStatistics.getShots(true)+"-"+matchStatistics.getShots(false)
                +"  PASES "+matchStatistics.getPasses(true)+"-"+matchStatistics.getPasses(false)
                +"  ENTRADAS "+matchStatistics.getTackles(true)+"-"+matchStatistics.getTackles(false)
                +"  PARADAS "+matchStatistics.getSaves(true)+"-"+matchStatistics.getSaves(false),
                18f,52f);
        String phaseText;
        switch(matchClock.getPhase()){
            case PRE_MATCH: phaseText="ARRIBA IZQ: EQUIPO LOCAL | CENTRO: VISITANTE | DERECHA: INICIAR"; break;
            case HALF_TIME: phaseText="DESCANSO - ARRIBA IZQUIERDA: COMENZAR 2º TIEMPO"; break;
            case FULL_TIME: phaseText="FINAL - ARRIBA IZQUIERDA: NUEVO PARTIDO"; break;
            default: phaseText=matchClock.isPaused()?"PARTIDO PAUSADO - ARRIBA DERECHA PARA CONTINUAR":"ARRIBA CENTRO: CAMBIAR FUTBOLISTA | DERECHA: PAUSA";
        }
        if(!phaseText.isEmpty())font.draw(spriteBatch,phaseText,18f,Gdx.graphics.getHeight()*0.56f);
        if(matchClock.getPhase()==MatchClock.Phase.PRE_MATCH)
            font.draw(spriteBatch,"LOCAL: "+matchSimulation.getHomeTeamName()+"   VISITANTE: "+matchSimulation.getAwayTeamName(),18f,Gdx.graphics.getHeight()-48f);
        else font.draw(spriteBatch,"ARRIBA CENTRO: CAMBIAR JUGADOR   ARRIBA DERECHA: PAUSA / CONTINUAR",18f,Gdx.graphics.getHeight()-48f);
        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null && width > 0 && height > 0) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }
    }

    @Override
    public void dispose() {
        if (disposed) return;
        disposed = true;
        if (modelBatch != null) modelBatch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (spriteBatch != null) spriteBatch.dispose();
        if (font != null) font.dispose();
        for (Model model : ownedModels) model.dispose();
        ownedModels.clear();
    }
}
