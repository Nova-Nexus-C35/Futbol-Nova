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
import com.badlogic.gdx.math.Matrix4;
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
    private final MatchDiscipline matchDiscipline = new MatchDiscipline();
    private final MatchAdvantage matchAdvantage = new MatchAdvantage();
    private final MatchClock matchClock = new MatchClock();
    private boolean matchReportSaved;
    private final MatchSimulation matchSimulation = new MatchSimulation();
    private List<OriginalProjectData.TeamRoster> playableRosters = new ArrayList<>();
    private TeamSelection teamSelection;
    private final List<Model> ownedModels = new ArrayList<>();
    private final List<PlayerVisual> playerVisuals = new ArrayList<>();
    private GltfPlayerSquad gltfPlayerSquad;
    private final List<ModelInstance> environmentModels = new ArrayList<>();
    private final Vector3 ballPosition = new Vector3(0f, BALL_RADIUS, 0f);
    private final Vector3 ballVelocity = new Vector3();
    private final Vector3 ballSpin = new Vector3();
    private final Vector3 controlledPosition = new Vector3();
    private final Vector3 cameraFollowPosition = new Vector3();
    private final Vector3 cameraLookTarget = new Vector3();
    private final Vector3 touchMovement = new Vector3();
    private final Matrix4 hudProjection = new Matrix4();
    private final float[] aiTackleCooldowns = new float[22];
    private final float[] goalkeeperRecoveryTimers = new float[22];
    private final float[] goalkeeperDiveDirections = new float[22];

    private PerspectiveCamera camera;
    private final BroadcastCamera broadcastCamera = new BroadcastCamera();
    private ModelBatch modelBatch;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private Environment environment;
    private ModelInstance fieldInstance;
    private ModelInstance ballInstance;
    private int possessionOwnerIndex = MatchGameplay.NO_OWNER;
    private int pendingOffsideReceiver = MatchGameplay.NO_OWNER;
    private int pendingPassReceiver = MatchGameplay.NO_OWNER;
    private String pendingPasserPlayerId;
    private boolean pendingPasserHomeTeam;
    private float pendingPassClock;
    private float aiKickClock = 0.9f;
    private float animationClock;
    private float tackleCooldown;
    private float restartClock;
    private float ballRotation;
    private MatchRules.Restart pendingRestart = MatchRules.Restart.IN_PLAY;
    private boolean restartAwardedHome;
    private boolean kickoffRestart;
    private boolean lastTouchHome = true;
    private boolean substitutionMode;
    private boolean substitutionHomeTeam = true;
    private int substitutionOutgoingIndex = -1;
    private int substitutionIncomingIndex;
    private int substitutionWindow = 1;
    private boolean disposed;
    private float checkpointClock;

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
        camera.far = 260f;
        camera.update();
        broadcastCamera.reset(camera, ballPosition);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.78f, 0.78f, 0.78f, 1f));
        environment.add(new DirectionalLight().set(0.85f, 0.85f, 0.85f, -0.4f, -1f, -0.25f));

        createProceduralPitchAndTeams();
        tryLoadGltfPlayers();
        restoreSavedMatch();
        try {
            OriginalProjectData.Summary summary = OriginalProjectData.loadBundledSummary();
            Gdx.app.log("FutbolNova", "Original project data loaded: " + summary);
        } catch (RuntimeException error) {
            Gdx.app.error("FutbolNova", "Original project data could not be loaded", error);
        }
    }

    private void tryLoadGltfPlayers() {
        if (!Gdx.files.internal(GltfPlayerSquad.HOME_ASSET).exists()
                || !Gdx.files.internal(GltfPlayerSquad.AWAY_ASSET).exists()) {
            Gdx.app.log("FutbolNova", "Home/away GLB models are not both available; keeping procedural fallback");
            return;
        }
        try {
            gltfPlayerSquad = new GltfPlayerSquad(
                    GltfPlayerSquad.HOME_ASSET, GltfPlayerSquad.AWAY_ASSET,
                    camera, MatchSimulation.PLAYERS_PER_TEAM, 1f);
            Gdx.app.log("FutbolNova", "Loaded home/away GLB player squads: " + gltfPlayerSquad.size());
        } catch (RuntimeException error) {
            gltfPlayerSquad = null;
            Gdx.app.error("FutbolNova", "GLB player asset failed; keeping procedural fallback", error);
        }
    }

    private void createProceduralPitchAndTeams() {
        ModelBuilder builder = new ModelBuilder();
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        Model field = builder.createBox(68f,0.35f,105f,new Material(ColorAttribute.createDiffuse(new Color(0.10f,0.42f,0.19f,1f))),attributes);
        own(field); fieldInstance=new ModelInstance(field); fieldInstance.transform.setToTranslation(0f,-0.22f,0f);
        Material turfLight=new Material(ColorAttribute.createDiffuse(new Color(0.115f,0.46f,0.21f,1f)));
        Material turfDark=new Material(ColorAttribute.createDiffuse(new Color(0.09f,0.39f,0.17f,1f)));
        Model turfStripeLight=builder.createBox(67.8f,0.03f,9.55f,turfLight,attributes);
        Model turfStripeDark=builder.createBox(67.8f,0.03f,9.55f,turfDark,attributes);
        own(turfStripeLight);own(turfStripeDark);
        for(int stripe=0;stripe<11;stripe++){
            Model stripeModel=(stripe%2==0)?turfStripeLight:turfStripeDark;
            addEnvironment(stripeModel,0f,-0.0275f,-47.5f+stripe*9.5f);
        }

        Material standMat=new Material(ColorAttribute.createDiffuse(new Color(0.045f,0.065f,0.085f,1f)));
        Material standAccent=new Material(ColorAttribute.createDiffuse(new Color(0.075f,0.095f,0.12f,1f)));
        Model longStand=builder.createBox(1.45f,0.9f,112f,standMat,attributes);
        Model longStandAccent=builder.createBox(1.45f,0.9f,112f,standAccent,attributes);
        Model endStand=builder.createBox(88f,0.9f,1.45f,standMat,attributes);
        Model endStandAccent=builder.createBox(88f,0.9f,1.45f,standAccent,attributes);
        own(longStand);own(longStandAccent);own(endStand);own(endStandAccent);
        for(int tier=0;tier<6;tier++){
            float offset=tier*1.25f;
            float elevation=0.45f+tier*0.78f;
            Model sideModel=(tier%2==0)?longStand:longStandAccent;
            Model endModel=(tier%2==0)?endStand:endStandAccent;
            addEnvironment(sideModel,-36.8f-offset,elevation,0f);
            addEnvironment(sideModel,36.8f+offset,elevation,0f);
            addEnvironment(endModel,0f,elevation,-54.8f-offset);
            addEnvironment(endModel,0f,elevation,54.8f+offset);
        }

        // Broadcast presentation: perimeter LED boards, corner floodlight pylons and roof beams.
        Material boardDark=new Material(ColorAttribute.createDiffuse(new Color(0.018f,0.035f,0.052f,1f)));
        Material boardCyan=new Material(ColorAttribute.createDiffuse(new Color(0.02f,0.72f,0.88f,1f)));
        Material boardViolet=new Material(ColorAttribute.createDiffuse(new Color(0.36f,0.12f,0.82f,1f)));
        Model sideBoard=builder.createBox(0.28f,0.82f,96f,boardDark,attributes);
        Model sideBoardGlow=builder.createBox(0.08f,0.12f,96f,boardCyan,attributes);
        Model endBoard=builder.createBox(58f,0.82f,0.28f,boardDark,attributes);
        Model endBoardGlow=builder.createBox(58f,0.12f,0.08f,boardViolet,attributes);
        own(sideBoard);own(sideBoardGlow);own(endBoard);own(endBoardGlow);
        for(float side:new float[]{-1f,1f}){
            addEnvironment(sideBoard,side*34.65f,0.48f,0f);
            addEnvironment(sideBoardGlow,side*34.82f,0.76f,0f);
            addEnvironment(endBoard,0f,0.48f,side*53.15f);
            addEnvironment(endBoardGlow,0f,0.76f,side*53.32f);
        }

        Material towerMat=new Material(ColorAttribute.createDiffuse(new Color(0.055f,0.07f,0.09f,1f)));
        Material lampMat=new Material(ColorAttribute.createDiffuse(new Color(0.92f,0.96f,1f,1f)));
        Model tower=builder.createBox(0.48f,17f,0.48f,towerMat,attributes);
        Model lampBar=builder.createBox(5.8f,0.42f,0.62f,lampMat,attributes);
        Model lampWing=builder.createBox(0.42f,0.42f,4.4f,lampMat,attributes);
        own(tower);own(lampBar);own(lampWing);
        for(float sx:new float[]{-1f,1f})for(float sz:new float[]{-1f,1f}){
            float tx=sx*42.5f,tz=sz*57f;
            addEnvironment(tower,tx,8.5f,tz);
            addEnvironment(lampBar,tx,17.2f,tz);
            addEnvironment(lampWing,tx,17.2f,tz);
        }

        Material roofMat=new Material(ColorAttribute.createDiffuse(new Color(0.035f,0.055f,0.075f,1f)));
        Model roofBeam=builder.createBox(2.4f,0.55f,112f,roofMat,attributes);
        Model roofRib=builder.createBox(78f,0.38f,1.1f,roofMat,attributes);
        own(roofBeam);own(roofRib);
        addEnvironment(roofBeam,-44f,10.5f,0f);
        addEnvironment(roofBeam,44f,10.5f,0f);
        addEnvironment(roofRib,0f,10.5f,-61f);
        addEnvironment(roofRib,0f,10.5f,61f);

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
        saveCompletedMatchReport();
        if(matchClock.getPhase()!=MatchClock.Phase.PRE_MATCH&&matchClock.getPhase()!=MatchClock.Phase.FULL_TIME){
            checkpointClock+=delta;
            if(checkpointClock>=3f){saveMatchCheckpoint();checkpointClock=0f;}
        }
        List<MatchSimulation.PlayerState> lineup = matchSimulation.getPlayers();
        animationClock+=delta;
        for(int i=0;i<lineup.size()&&i<playerVisuals.size();i++){
            MatchSimulation.PlayerState state=lineup.get(i);
            float yaw=(float)Math.atan2(-state.forwardX,-state.forwardZ);
            if(state.active){
                // Keep the procedural representation current so a GLB render failure can
                // fall back immediately without a frame of stale player transforms.
                playerVisuals.get(i).update(state.x,state.z,yaw,animationClock,state.movementSpeed);
                if((i==0||i==11)&&goalkeeperRecoveryTimers[i]>0f)
                    playerVisuals.get(i).setGoalkeeperDive(goalkeeperDiveDirections[i],
                            Math.min(1f,goalkeeperRecoveryTimers[i]/0.78f));
                if(gltfPlayerSquad!=null)
                    gltfPlayerSquad.updatePlayer(i,state.x,state.z,yaw,state.movementSpeed);
            }else if(gltfPlayerSquad!=null){
                gltfPlayerSquad.setPlayerActive(i,false);
            }
        }

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.035f, 0.07f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        broadcastCamera.update(camera, ballPosition, ballVelocity.len(), delta);
        ballRotation += ballVelocity.len()*delta*35f;
        ballInstance.transform.setToTranslation(ballPosition);
        ballInstance.transform.rotate(Vector3.X,ballRotation);
        ballInstance.transform.rotate(Vector3.Y,ballRotation*0.7f);
        modelBatch.begin(camera);
        modelBatch.render(fieldInstance,environment);
        for(ModelInstance structure:environmentModels)modelBatch.render(structure,environment);
        if(gltfPlayerSquad==null){
            for(int i=0;i<playerVisuals.size()&&i<matchSimulation.getPlayers().size();i++)
                if(matchSimulation.getPlayers().get(i).active)playerVisuals.get(i).render(modelBatch,environment);
        }
        modelBatch.render(ballInstance,environment);
        modelBatch.end();
        renderGltfPlayersOrFallback(delta);

        drawPitchMarkings();
        drawTouchControls();
        drawHud();
    }

    private void renderGltfPlayersOrFallback(float delta) {
        if (gltfPlayerSquad == null) return;
        try {
            gltfPlayerSquad.render(delta);
        } catch (RuntimeException error) {
            Gdx.app.error("FutbolNova", "GLB renderer failed at runtime; switching to procedural players", error);
            try {
                gltfPlayerSquad.dispose();
            } catch (RuntimeException disposeError) {
                Gdx.app.error("FutbolNova", "Could not fully dispose failed GLB renderer", disposeError);
            }
            gltfPlayerSquad = null;
            modelBatch.begin(camera);
            for (int i = 0; i < playerVisuals.size() && i < matchSimulation.getPlayers().size(); i++) {
                if (matchSimulation.getPlayers().get(i).active) {
                    playerVisuals.get(i).render(modelBatch, environment);
                }
            }
            modelBatch.end();
        }
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
            }else if(substitutionMode&&!matchClock.canAcceptGameplayInput()){
                if(y>height*0.78f&&x<width*0.30f&&!pointerWasTouched[pointer]){
                    toggleSubstitutionTeam();
                }else if(x<width*0.58f)substitutionMode=false;
                else{
                    substitutionMode=false;
                    if(matchClock.getPhase()==MatchClock.Phase.HALF_TIME)startSecondHalf();
                    else if(matchClock.isPlaying()&&matchClock.isPaused()){matchClock.resume();matchState.setPaused(false);}
                }
                pointerWasTouched[pointer]=true;
                continue;
            }else if(!matchClock.canAcceptGameplayInput()){
                // Do not let movement or action touches mutate a paused/pre/post-match.
                pointerWasTouched[pointer]=true;
                continue;
            }else if(x<width*0.58f){
                if(movementPointer<0||movementPointer==pointer){
                    movementPointer=pointer;
                    movementStillDown=true;
                    float radius=Math.max(1f,Math.min(width*0.22f,height*0.38f));
                    float horizontal=(x-width*0.28f)/radius;
                    float vertical=(height*0.50f-y)/radius;
                    touchMovement.set(horizontal,0f,-vertical);
                    if(touchMovement.len2()>1f)touchMovement.nor();
                    controlledPosition.mulAdd(touchMovement,10f*delta);
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
        if(!movementStillDown){
            movementPointer=-1;
            touchMovement.setZero();
        }
        matchActionPointerDown=matchActionDown;
    }

    /** Top HUD strip also exposes the home team's bench while play is stopped. */
    private void handleMatchAction(float x,float y,int width,int height){
        MatchClock.Phase phase=matchClock.getPhase();
        if(phase==MatchClock.Phase.PRE_MATCH){
            if(x<width*0.32f){cycleHomeRoster();return;}
            if(x<width*0.72f){cycleAwayRoster();return;}
            if(matchClock.start()){
                matchState.setPaused(false);
                matchReportSaved=false;
                ballPosition.set(0f,BALL_RADIUS,0f);ballVelocity.setZero();ballSpin.setZero();
                possessionOwnerIndex=MatchGameplay.NO_OWNER;pendingOffsideReceiver=MatchGameplay.NO_OWNER;clearPendingPass();restartAwardedHome=true;
                kickoffRestart=true;pendingRestart=MatchRules.Restart.IN_PLAY;
                restartClock=1.4f;lastTouchHome=true;
            }
            return;
        }
        if(substitutionMode){
            if(x<width*0.32f){cycleSubstitutionOutgoing();return;}
            if(x<width*0.72f){cycleSubstitutionIncoming();return;}
            applySelectedSubstitution();
            return;
        }
        if(x>=width*0.72f){
            if(matchClock.isPlaying()){
                if(matchClock.isPaused())matchClock.resume(); else matchClock.pause();
                matchState.setPaused(matchClock.isPaused());
                if(matchClock.isPaused())saveMatchCheckpoint();
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
            if(matchSimulation.getBenchPlayers(true).isEmpty()
                    &&matchSimulation.getBenchPlayers(false).isEmpty())startSecondHalf();
            else openSubstitutionMode();
        }else if(phase==MatchClock.Phase.FULL_TIME){
            resetMatch();
        }else if(matchClock.isPlaying()&&matchClock.isPaused()){
            openSubstitutionMode();
        }
    }

    private void startSecondHalf(){
        if(!matchClock.startSecondHalf())return;
        matchState.setPaused(false);
        substitutionMode=false;
        matchSimulation.switchEnds();
        MatchSimulation.PlayerState selectedAfterSwitch=matchSimulation.getControlledPlayer();
        if(selectedAfterSwitch!=null)controlledPosition.set(selectedAfterSwitch.x,0f,selectedAfterSwitch.z);
        ballPosition.set(0f,BALL_RADIUS,0f);ballVelocity.setZero();ballSpin.setZero();
        possessionOwnerIndex=MatchGameplay.NO_OWNER;pendingOffsideReceiver=MatchGameplay.NO_OWNER;clearPendingPass();restartAwardedHome=false;
        kickoffRestart=true;pendingRestart=MatchRules.Restart.IN_PLAY;restartClock=1.4f;
    }

    private void openSubstitutionMode(){
        if(matchClock.getPhase()!=MatchClock.Phase.HALF_TIME&&!matchClock.isPaused())return;
        substitutionHomeTeam=true;
        if(matchSimulation.getBenchPlayers(true).isEmpty()
                &&!matchSimulation.getBenchPlayers(false).isEmpty())substitutionHomeTeam=false;
        if(matchSimulation.getBenchPlayers(substitutionHomeTeam).isEmpty())return;
        if(matchClock.getPhase()!=MatchClock.Phase.HALF_TIME
                &&matchSimulation.getSubstitutionWindowsUsed(substitutionHomeTeam)>=3)return;
        substitutionMode=true;
        substitutionWindow=Math.min(3,matchSimulation.getSubstitutionWindowsUsed(substitutionHomeTeam)+1);
        substitutionOutgoingIndex=findNextActivePlayer(substitutionHomeTeam,-1);
        substitutionIncomingIndex=0;
    }

    private int findNextActivePlayer(boolean homeTeam,int fromIndex){
        List<MatchSimulation.PlayerState> players=matchSimulation.getPlayers();
        int first=homeTeam?1:12;
        int last=homeTeam?10:21;
        int base=fromIndex>=first&&fromIndex<=last?fromIndex:first-1;
        for(int offset=1;offset<=last-first+1;offset++){
            int candidate=first+((base-first+offset)%(last-first+1));
            if(players.get(candidate).active)return candidate;
        }
        return -1;
    }

    private void toggleSubstitutionTeam(){
        boolean otherTeam=!substitutionHomeTeam;
        if(matchSimulation.getBenchPlayers(otherTeam).isEmpty())return;
        if(matchClock.getPhase()!=MatchClock.Phase.HALF_TIME
                &&matchSimulation.getSubstitutionWindowsUsed(otherTeam)>=3)return;
        substitutionHomeTeam=otherTeam;
        substitutionWindow=Math.min(3,matchSimulation.getSubstitutionWindowsUsed(otherTeam)+1);
        substitutionOutgoingIndex=findNextActivePlayer(substitutionHomeTeam,-1);
        substitutionIncomingIndex=0;
    }

    private void cycleSubstitutionOutgoing(){
        substitutionOutgoingIndex=findNextActivePlayer(substitutionHomeTeam,substitutionOutgoingIndex);
    }

    private void cycleSubstitutionIncoming(){
        List<OriginalProjectData.PlayerRecord> bench=matchSimulation.getBenchPlayers(substitutionHomeTeam);
        if(!bench.isEmpty())substitutionIncomingIndex=(substitutionIncomingIndex+1)%bench.size();
    }

    private void applySelectedSubstitution(){
        List<OriginalProjectData.PlayerRecord> bench=matchSimulation.getBenchPlayers(substitutionHomeTeam);
        List<MatchSimulation.PlayerState> players=matchSimulation.getPlayers();
        int first=substitutionHomeTeam?1:12;
        int last=substitutionHomeTeam?10:21;
        if(substitutionOutgoingIndex<first||substitutionOutgoingIndex>last
                ||substitutionIncomingIndex<0||substitutionIncomingIndex>=bench.size())return;
        MatchSimulation.PlayerState outgoing=players.get(substitutionOutgoingIndex);
        String incomingId=bench.get(substitutionIncomingIndex).id;
        int window=matchClock.getPhase()==MatchClock.Phase.HALF_TIME?0:substitutionWindow;
        boolean halfTime=matchClock.getPhase()==MatchClock.Phase.HALF_TIME;
        if(!matchSimulation.substitutePlayer(substitutionHomeTeam,outgoing.playerId,incomingId,window,halfTime))return;
        if(pendingPassReceiver==substitutionOutgoingIndex)clearPendingPass();
        if(possessionOwnerIndex==substitutionOutgoingIndex){
            possessionOwnerIndex=MatchGameplay.NO_OWNER;
            ballVelocity.setZero();ballSpin.setZero();
        }
        if(substitutionHomeTeam&&matchSimulation.getControlledPlayerIndex()==substitutionOutgoingIndex){
            MatchSimulation.PlayerState replacement=matchSimulation.getControlledPlayer();
            if(replacement!=null)controlledPosition.set(replacement.x,0f,replacement.z);
        }
        substitutionOutgoingIndex=findNextActivePlayer(substitutionHomeTeam,substitutionOutgoingIndex);
        substitutionIncomingIndex=0;
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
        MatchSaveStore.clear();
        saveCompletedMatchReport();
        matchReportSaved=false;
        matchClock.reset();matchState.reset();matchStatistics.reset();matchDiscipline.reset();matchAdvantage.clear();
        if(teamSelection!=null)rebuildSelectedLineups();
        ballPosition.set(0f,BALL_RADIUS,0f);ballVelocity.setZero();ballSpin.setZero();
        possessionOwnerIndex=MatchGameplay.NO_OWNER;pendingOffsideReceiver=MatchGameplay.NO_OWNER;clearPendingPass();aiKickClock=0.9f;tackleCooldown=0f;
        restartClock=0f;pendingRestart=MatchRules.Restart.IN_PLAY;kickoffRestart=false;
        for(int i=0;i<aiTackleCooldowns.length;i++)aiTackleCooldowns[i]=0f;
    }

    public MatchDiscipline.Outcome issueYellowCard(String playerId){
        return issueYellowCard(playerId,MatchDiscipline.Reason.REFEREE_DECISION);
    }

    public MatchDiscipline.Outcome issueYellowCard(String playerId,MatchDiscipline.Reason reason){
        MatchDiscipline.Outcome outcome=matchDiscipline.issueYellow(
                playerId,reason,matchClock.getTotalElapsedSeconds());
        if(outcome==MatchDiscipline.Outcome.DISMISSED)applyDismissal(playerId);
        return outcome;
    }

    public MatchDiscipline.Outcome issueRedCard(String playerId){
        return issueRedCard(playerId,MatchDiscipline.Reason.REFEREE_DECISION);
    }

    public MatchDiscipline.Outcome issueRedCard(String playerId,MatchDiscipline.Reason reason){
        MatchDiscipline.Outcome outcome=matchDiscipline.issueRed(
                playerId,reason,matchClock.getTotalElapsedSeconds());
        if(outcome==MatchDiscipline.Outcome.DISMISSED)applyDismissal(playerId);
        return outcome;
    }

    private void applyDismissal(String playerId){
        for(int i=0;i<matchSimulation.getPlayers().size();i++){
            MatchSimulation.PlayerState player=matchSimulation.getPlayers().get(i);
            if(player.playerId.equals(playerId)){
                boolean wasControlled=i==matchSimulation.getControlledPlayerIndex();
                if(!matchSimulation.dismissPlayer(playerId))return;
                if(possessionOwnerIndex==i)possessionOwnerIndex=MatchGameplay.NO_OWNER;
                if(pendingOffsideReceiver==i)pendingOffsideReceiver=MatchGameplay.NO_OWNER;
                if(pendingPassReceiver==i)clearPendingPass();
                if(wasControlled){
                    MatchSimulation.PlayerState replacement=matchSimulation.getControlledPlayer();
                    if(replacement!=null)controlledPosition.set(replacement.x,0f,replacement.z);
                }
                return;
            }
        }
    }

    private void saveCompletedMatchReport(){
        if(matchReportSaved||matchClock.getPhase()!=MatchClock.Phase.FULL_TIME||Gdx.app==null)return;
        MatchReport report=MatchReport.capture(matchSimulation.getHomeTeamName(),
                matchSimulation.getAwayTeamName(),matchState.getHomeGoals(),matchState.getAwayGoals(),
                matchClock.getTotalElapsedSeconds(),matchStatistics,matchDiscipline);
        MatchReportStore.save(Gdx.app.getPreferences("futbol_nova_match_reports"),report);
        try {
            String exportDirectory="FutbolNova/Actas";
            Gdx.files.external(exportDirectory).mkdirs();
            String baseName="partido_"+report.matchId;
            Gdx.files.external(exportDirectory+"/"+baseName+".json").writeString(report.toJson(),false,"UTF-8");
            Gdx.files.external(exportDirectory+"/"+baseName+".csv").writeString(MatchReportExporter.toCsv(report),false,"UTF-8");
            Gdx.app.log("FutbolNova","Exported match report to external app files: "+baseName);
        } catch(RuntimeException exportError) {
            Gdx.app.error("FutbolNova","Local report saved, but external CSV/JSON export failed",exportError);
        }
        matchReportSaved=true;
        MatchSaveStore.clear();
        Gdx.app.log("FutbolNova","Saved completed match report: "+report.matchId);
    }

    private void recordFoulAndApplyDiscipline(MatchSimulation.PlayerState offender){
        if(offender==null||!offender.active)return;
        matchStatistics.recordFoul(offender.playerId,offender.homeTeam,matchClock.getTotalElapsedSeconds());
        MatchDiscipline.Outcome outcome=matchDiscipline.recordFoul(offender.playerId,matchClock.getTotalElapsedSeconds());
        if(outcome==MatchDiscipline.Outcome.DISMISSED)applyDismissal(offender.playerId);
    }

    public MatchDiscipline getMatchDiscipline(){return matchDiscipline;}

    private void performShot(){
        if(possessionOwnerIndex!=matchSimulation.getControlledPlayerIndex())return;
        if(gltfPlayerSquad!=null)
            gltfPlayerSquad.playAction(matchSimulation.getControlledPlayerIndex(),"Shoot (football)");
        clearPendingPass();
        pendingOffsideReceiver=MatchGameplay.NO_OWNER;
        float attackSign=matchSimulation.homeAttacksNegativeZ()?-1f:1f;
        ballPosition.set(controlledPosition.x,BALL_RADIUS,controlledPosition.z+attackSign*0.62f);
        ballVelocity.set(MatchGameplay.shotVelocity(true,-ballPosition.x*0.22f,matchSimulation.homeAttacksNegativeZ()));ballSpin.set(0f,18f,0f);
        matchStatistics.recordShot(matchSimulation.getControlledPlayer().playerId,true,matchClock.getTotalElapsedSeconds());
        possessionOwnerIndex=MatchGameplay.NO_OWNER;lastTouchHome=true;
    }

    private void performPass(){
        if(possessionOwnerIndex!=matchSimulation.getControlledPlayerIndex())return;
        if(gltfPlayerSquad!=null)
            gltfPlayerSquad.playAction(matchSimulation.getControlledPlayerIndex(),"Pass (straight)");
        List<MatchSimulation.PlayerState> lineup=matchSimulation.getPlayers();
        MatchGameplay.PassTargetDecision passDecision=MatchGameplay.findForwardPassDecision(
                lineup,matchSimulation.getControlledPlayerIndex(),controlledPosition.x,controlledPosition.z,
                true,matchSimulation.homeAttacksNegativeZ());
        int receiver=passDecision.receiverIndex;
        clearPendingPass();
        pendingOffsideReceiver=passDecision.offside?receiver:MatchGameplay.NO_OWNER;
        float attackSign=matchSimulation.homeAttacksNegativeZ()?-1f:1f;
        ballPosition.set(controlledPosition.x,BALL_RADIUS,controlledPosition.z+attackSign*0.62f);
        if(receiver==MatchGameplay.NO_OWNER){
            ballVelocity.set(MatchGameplay.clearanceVelocity(true,controlledPosition.x,matchSimulation.homeAttacksNegativeZ()));
        }else{
            MatchSimulation.PlayerState passer=matchSimulation.getControlledPlayer();
            MatchSimulation.PlayerState target=lineup.get(receiver);
            matchStatistics.recordPassAttempt(passer.playerId,true,matchClock.getTotalElapsedSeconds());
            beginPendingPass(passer,receiver);
            ballVelocity.set(MatchGameplay.passVelocity(controlledPosition.x,controlledPosition.z,target.x,target.z));
        }
        ballSpin.setZero();possessionOwnerIndex=MatchGameplay.NO_OWNER;lastTouchHome=true;
    }

    private void beginPendingPass(MatchSimulation.PlayerState passer,int receiver){
        if(passer==null||receiver<0||receiver>=matchSimulation.getPlayers().size())return;
        pendingPasserPlayerId=passer.playerId;
        pendingPasserHomeTeam=passer.homeTeam;
        pendingPassReceiver=receiver;
        pendingPassClock=4f;
    }

    private void clearPendingPass(){
        pendingPassReceiver=MatchGameplay.NO_OWNER;
        pendingPasserPlayerId=null;
        pendingPassClock=0f;
    }

    private void performTackle(){
        if(tackleCooldown>0f||possessionOwnerIndex<0||possessionOwnerIndex==matchSimulation.getControlledPlayerIndex())return;
        List<MatchSimulation.PlayerState> lineup=matchSimulation.getPlayers();
        MatchSimulation.PlayerState attacker=lineup.get(matchSimulation.getControlledPlayerIndex()),target=lineup.get(possessionOwnerIndex);
        if(target.homeTeam)return;
        if(gltfPlayerSquad!=null)
            gltfPlayerSquad.playAction(matchSimulation.getControlledPlayerIndex(),
                    PlayerAnimationPolicy.tackleHint(attacker.movementSpeed));
        MatchSimulation.TackleResult result=MatchGameplay.tackle(attacker,target,1.7f);tackleCooldown=0.75f;
        if(result!=null&&result.success){matchStatistics.recordTackle(attacker.playerId,true,matchClock.getTotalElapsedSeconds());possessionOwnerIndex=matchSimulation.getControlledPlayerIndex();lastTouchHome=true;}
        else if(result!=null&&result.foul){
            recordFoulAndApplyDiscipline(attacker);
            if(!beginAdvantage(target,attacker.homeTeam))awardFoulRestart(target.x,target.z,attacker.homeTeam,target.homeTeam);
        }
    }

    private void updateBall(float delta) {
        BallFlightModel.Step flight=BallFlightModel.integrateStep(ballPosition,ballVelocity,ballSpin,delta);
        ballPosition.set(flight.position);ballVelocity.set(flight.velocity);ballSpin.set(flight.spin);
        if(ballPosition.y<BALL_RADIUS){
            ballPosition.y=BALL_RADIUS;
            if(ballVelocity.y<0f)ballVelocity.y*=-0.38f;
            ballVelocity.x*=0.91f;ballVelocity.z*=0.91f;
        }
        if(tryGoalkeeperSave(delta))return;
        BallCollisionModel.resolveGoalFrame(ballPosition,ballVelocity,3.66f,2.44f,52.5f,BALL_RADIUS);
        BallCollisionModel.resolveGoalFrame(ballPosition,ballVelocity,3.66f,2.44f,-52.5f,BALL_RADIUS);
        BallCollisionModel.resolvePlayerContact(ballPosition,ballVelocity,matchSimulation.getPlayers());
        if(Math.abs(ballPosition.x)-BALL_RADIUS>FIELD_HALF_WIDTH
                ||Math.abs(ballPosition.z)-BALL_RADIUS>FIELD_HALF_LENGTH){
            MatchRules.ExitDecision exit=MatchRules.classifyBallExit(
                    ballPosition.x,ballPosition.y,ballPosition.z,lastTouchHome,FIELD_HALF_WIDTH,FIELD_HALF_LENGTH,3.66f,2.44f,BALL_RADIUS,matchSimulation.homeAttacksNegativeZ());
            possessionOwnerIndex=MatchGameplay.NO_OWNER;pendingOffsideReceiver=MatchGameplay.NO_OWNER;
            clearPendingPass();
            ballVelocity.setZero();ballSpin.setZero();
            if(exit.restart==MatchRules.Restart.GOAL){
                matchAdvantage.clear();
                matchState.addGoal(exit.awardedHome);
                matchStatistics.recordGoal(null,exit.awardedHome,matchClock.getTotalElapsedSeconds());
                playGoalCelebration(exit.awardedHome);
                ballPosition.set(0f,BALL_RADIUS,0f);
                restartAwardedHome=!exit.awardedHome;
                kickoffRestart=true;
                pendingRestart=MatchRules.Restart.IN_PLAY;
                restartClock=1.4f;
            }else if(matchAdvantage.isActive()){
                resolveAdvantageRestart();
                return;
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

    private void awardOffsideRestart(MatchSimulation.PlayerState offender){
        if(offender==null)return;
        clearPendingPass();
        matchStatistics.recordOffside(offender.playerId,offender.homeTeam,matchClock.getTotalElapsedSeconds());
        matchAdvantage.clear();
        pendingRestart=MatchRules.Restart.FREE_KICK;
        restartAwardedHome=!offender.homeTeam;kickoffRestart=false;
        possessionOwnerIndex=MatchGameplay.NO_OWNER;pendingOffsideReceiver=MatchGameplay.NO_OWNER;
        ballPosition.set(offender.x,BALL_RADIUS,offender.z);
        ballVelocity.setZero();ballSpin.setZero();lastTouchHome=offender.homeTeam;
        restartClock=1.0f;
    }

    private boolean beginAdvantage(MatchSimulation.PlayerState fouledPlayer,boolean defendingHome){
        List<MatchSimulation.PlayerState> players=matchSimulation.getPlayers();
        if(fouledPlayer==null||!fouledPlayer.active||possessionOwnerIndex<0||possessionOwnerIndex>=players.size())return false;
        MatchSimulation.PlayerState owner=players.get(possessionOwnerIndex);
        if(owner.homeTeam!=fouledPlayer.homeTeam)return false;
        return matchAdvantage.begin(owner.homeTeam,fouledPlayer.x,fouledPlayer.z,defendingHome);
    }

    private void resolveAdvantageRestart(){
        if(!matchAdvantage.isActive())return;
        float foulX=matchAdvantage.getFoulX(),foulZ=matchAdvantage.getFoulZ();
        boolean defendingHome=matchAdvantage.isDefendingHome();
        boolean awardedHome=matchAdvantage.isAwardedHome();
        awardFoulRestart(foulX,foulZ,defendingHome,awardedHome);
    }

    private void awardFoulRestart(float foulX,float foulZ,boolean defendingHome,boolean awardedHome){
        clearPendingPass();
        matchAdvantage.clear();
        boolean homeDefendsPositiveZ=matchSimulation.homeAttacksNegativeZ();
        pendingRestart=MatchRules.classifyFoulRestart(foulX,foulZ,defendingHome,homeDefendsPositiveZ);
        restartAwardedHome=awardedHome;kickoffRestart=false;
        possessionOwnerIndex=MatchGameplay.NO_OWNER;pendingOffsideReceiver=MatchGameplay.NO_OWNER;ballVelocity.setZero();ballSpin.setZero();
        if(pendingRestart==MatchRules.Restart.PENALTY){
            float defendingGoalDirection=defendingHome
                    ?(homeDefendsPositiveZ?1f:-1f):(homeDefendsPositiveZ?-1f:1f);
            ballPosition.set(0f,BALL_RADIUS,defendingGoalDirection*(FIELD_HALF_LENGTH-11f));
        }else{
            ballPosition.set(Math.max(-FIELD_HALF_WIDTH+1f,Math.min(FIELD_HALF_WIDTH-1f,foulX)),
                    BALL_RADIUS,Math.max(-FIELD_HALF_LENGTH+1f,Math.min(FIELD_HALF_LENGTH-1f,foulZ)));
        }
        restartClock=1.2f;
    }

    private boolean tryGoalkeeperSave(float delta){
        List<MatchSimulation.PlayerState> lineup=matchSimulation.getPlayers();
        if(lineup.size()!=22)return false;
        for(int keeperIndex:new int[]{0,11}){
            MatchSimulation.PlayerState keeper=lineup.get(keeperIndex);
            goalkeeperRecoveryTimers[keeperIndex]=Math.max(0f,
                    goalkeeperRecoveryTimers[keeperIndex]-delta);
            if(!keeper.active||goalkeeperRecoveryTimers[keeperIndex]>0f)continue;
            float ownGoalDirection=keeper.homeTeam
                    ?(matchSimulation.homeAttacksNegativeZ()?1f:-1f)
                    :(matchSimulation.homeAttacksNegativeZ()?-1f:1f);
            GoalkeeperSaveModel.Plan plan=GoalkeeperSaveModel.evaluate(keeper.x,keeper.z,
                    ballPosition.x,ballPosition.y,ballPosition.z,ballVelocity.x,
                    ballVelocity.y,ballVelocity.z,ownGoalDirection);
            if(plan==null)continue;

            // The goalkeeper starts moving toward the predicted contact point before the shot arrives.
            keeper.targetX=plan.targetX;
            keeper.targetZ=plan.targetZ;
            float timeToContact=plan.time;
            float lateralError=Math.abs(keeper.x-plan.interceptX);
            float depthError=Math.abs(keeper.z-plan.interceptZ);
            if(timeToContact>Math.max(0.09f,delta*1.5f)
                    ||lateralError>1.45f||depthError>2.35f)continue;

            ballPosition.set(plan.interceptX,Math.max(BALL_RADIUS,plan.interceptY),plan.interceptZ);
            ballSpin.setZero();
            pendingOffsideReceiver=MatchGameplay.NO_OWNER;
            lastTouchHome=keeper.homeTeam;
            matchStatistics.recordSave(keeper.playerId,keeper.homeTeam,matchClock.getTotalElapsedSeconds());
            goalkeeperDiveDirections[keeperIndex]=plan.diveDirection;
            if(gltfPlayerSquad!=null) {
                String keeperClip=PlayerAnimationPolicy.goalkeeperSaveHint(
                        plan.action==GoalkeeperSaveModel.Action.CATCH,plan.diveDirection);
                if(!gltfPlayerSquad.playAction(keeperIndex,keeperClip)
                        && !"keeper_punch".equals(keeperClip))
                    gltfPlayerSquad.playAction(keeperIndex,"keeper_punch");
            }
            if(plan.action==GoalkeeperSaveModel.Action.CATCH){
                possessionOwnerIndex=keeperIndex;
                ballVelocity.setZero();
                goalkeeperRecoveryTimers[keeperIndex]=0.42f;
            }else{
                possessionOwnerIndex=MatchGameplay.NO_OWNER;
                ballVelocity.set(
                        ballVelocity.x*0.28f+plan.diveDirection*Math.max(1.8f,plan.speed*0.16f),
                        Math.max(3f,Math.abs(ballVelocity.y)*0.25f+2.4f),
                        -ownGoalDirection*Math.max(5f,plan.speed*0.45f));
                goalkeeperRecoveryTimers[keeperIndex]=0.78f;
            }
            return true;
        }
        return false;
    }

    private void playGoalCelebration(boolean scoringHome) {
        if (gltfPlayerSquad == null) return;
        List<MatchSimulation.PlayerState> lineup = matchSimulation.getPlayers();
        int played = 0;
        for (int i = 0; i < lineup.size() && played < 6; i++) {
            MatchSimulation.PlayerState player = lineup.get(i);
            if (!player.active || player.homeTeam != scoringHome || "GK".equals(player.role)) continue;
            if (gltfPlayerSquad.playAction(i, "siu_celebration")) played++;
        }
    }

    private void playRestartAnimation(List<MatchSimulation.PlayerState> lineup,
                                      MatchRules.Restart restart, boolean awardedHome) {
        if (gltfPlayerSquad == null || restart != MatchRules.Restart.THROW_IN) return;
        int thrower = MatchGameplay.NO_OWNER;
        float nearestDistance2 = Float.MAX_VALUE;
        for (int i = 0; i < lineup.size(); i++) {
            MatchSimulation.PlayerState player = lineup.get(i);
            if (!player.active || player.homeTeam != awardedHome || "GK".equals(player.role)) continue;
            float dx = player.x - ballPosition.x;
            float dz = player.z - ballPosition.z;
            float distance2 = dx * dx + dz * dz;
            if (distance2 < nearestDistance2) {
                nearestDistance2 = distance2;
                thrower = i;
            }
        }
        if (thrower != MatchGameplay.NO_OWNER) gltfPlayerSquad.playAction(thrower, "throw_in");
    }
    private void updateGameplay(float delta){
        tackleCooldown=Math.max(0f,tackleCooldown-delta);
        for(int i=0;i<aiTackleCooldowns.length;i++)aiTackleCooldowns[i]=Math.max(0f,aiTackleCooldowns[i]-delta);
        if(restartClock>0f){
            restartClock-=delta;
            if(restartClock<=0f){
                List<MatchSimulation.PlayerState> penaltyLineup=matchSimulation.getPlayers();
                playRestartAnimation(penaltyLineup,pendingRestart,restartAwardedHome);
                int goalkeeperIndex=restartAwardedHome?11:0;
                float goalkeeperX=penaltyLineup.size()>goalkeeperIndex
                        ?penaltyLineup.get(goalkeeperIndex).x:Float.NaN;
                ballVelocity.set(MatchRules.restartVelocity(pendingRestart,kickoffRestart,
                        restartAwardedHome,matchSimulation.homeAttacksNegativeZ(),
                        ballPosition.x,goalkeeperX));
                lastTouchHome=restartAwardedHome;
                pendingRestart=MatchRules.Restart.IN_PLAY;
                kickoffRestart=false;
            }
            return;
        }
        List<MatchSimulation.PlayerState> lineup=matchSimulation.getPlayers();
        boolean possessionWasFree=possessionOwnerIndex<0;
        boolean expectedPassReception=pendingPassReceiver>=0;
        if(pendingPassReceiver>=0){
            pendingPassClock-=delta;
            if(pendingPassClock<=0f)clearPendingPass();
        }
        if(possessionOwnerIndex<0)
            possessionOwnerIndex=MatchGameplay.acquirePossession(lineup,ballPosition.x,ballPosition.y,ballPosition.z,ballVelocity.len());
        if(pendingOffsideReceiver>=0&&possessionOwnerIndex>=0){
            if(possessionOwnerIndex==pendingOffsideReceiver){
                MatchSimulation.PlayerState offender=lineup.get(pendingOffsideReceiver);
                awardOffsideRestart(offender);
                return;
            }
            pendingOffsideReceiver=MatchGameplay.NO_OWNER;
        }
        if(pendingPassReceiver>=0&&possessionOwnerIndex>=0){
            if(possessionOwnerIndex==pendingPassReceiver&&pendingPasserPlayerId!=null) {
                matchStatistics.recordPassCompleted(pendingPasserPlayerId,pendingPasserHomeTeam,matchClock.getTotalElapsedSeconds());
                if(gltfPlayerSquad!=null) {
                    String aerialClip=PlayerAnimationPolicy.aerialControlHint(ballPosition.y);
                    gltfPlayerSquad.playAction(pendingPassReceiver,
                            aerialClip==null?"soccer_receive":aerialClip);
                }
            }
            clearPendingPass();
        }
        if(possessionWasFree&&!expectedPassReception&&possessionOwnerIndex>=0&&gltfPlayerSquad!=null) {
            String aerialClip=PlayerAnimationPolicy.aerialControlHint(ballPosition.y);
            if(aerialClip!=null)gltfPlayerSquad.playAction(possessionOwnerIndex,aerialClip);
        }
        int possessionTeam=possessionOwnerIndex>=0&&possessionOwnerIndex<lineup.size()
                ?(lineup.get(possessionOwnerIndex).homeTeam?1:0):MatchAdvantage.NO_TEAM;
        MatchAdvantage.Outcome advantageOutcome=matchAdvantage.update(delta,possessionTeam);
        if(advantageOutcome==MatchAdvantage.Outcome.AWARD_FREE_KICK){resolveAdvantageRestart();return;}
        if(possessionOwnerIndex<0||possessionOwnerIndex>=lineup.size()){
            possessionOwnerIndex=MatchGameplay.NO_OWNER;return;
        }

        // Nearest opponent may win the ball with a correctly oriented tackle; cooldown prevents repeated instant steals.
        MatchSimulation.PlayerState currentOwner=lineup.get(possessionOwnerIndex);
        int nearestDefender=MatchGameplay.NO_OWNER;float nearestDistance=1.7f;
        for(int i=0;i<lineup.size();i++){
            if(i==possessionOwnerIndex||!lineup.get(i).active||lineup.get(i).homeTeam==currentOwner.homeTeam||aiTackleCooldowns[i]>0f)continue;
            MatchSimulation.PlayerState defender=lineup.get(i);
            float dx=defender.x-currentOwner.x,dz=defender.z-currentOwner.z;
            float d=(float)Math.sqrt(dx*dx+dz*dz);
            if(d<nearestDistance){nearestDistance=d;nearestDefender=i;}
        }
        if(nearestDefender!=MatchGameplay.NO_OWNER){
            MatchSimulation.TackleResult tackle=MatchGameplay.tackle(lineup.get(nearestDefender),currentOwner,1.7f);
            aiTackleCooldowns[nearestDefender]=1.35f;
            if(tackle!=null&&tackle.success){
                matchStatistics.recordTackle(lineup.get(nearestDefender).playerId,nearestDefender<11,matchClock.getTotalElapsedSeconds());
                if(gltfPlayerSquad!=null)
                    gltfPlayerSquad.playAction(nearestDefender,
                            PlayerAnimationPolicy.tackleHint(lineup.get(nearestDefender).movementSpeed));
                possessionOwnerIndex=nearestDefender;
                currentOwner=lineup.get(possessionOwnerIndex);
                aiKickClock=1.0f;
            }else if(tackle!=null&&tackle.foul){
                MatchSimulation.PlayerState offender=lineup.get(nearestDefender);
                recordFoulAndApplyDiscipline(offender);
                if(!beginAdvantage(currentOwner,offender.homeTeam))
                    awardFoulRestart(currentOwner.x,currentOwner.z,offender.homeTeam,currentOwner.homeTeam);
                return;
            }
        }

        MatchSimulation.PlayerState owner=lineup.get(possessionOwnerIndex);
        float ownerAttackSign=owner.homeTeam?(matchSimulation.homeAttacksNegativeZ()?-1f:1f):(matchSimulation.homeAttacksNegativeZ()?1f:-1f);
        ballPosition.set(owner.x,BALL_RADIUS,owner.z+ownerAttackSign*0.62f);
        ballVelocity.setZero();ballSpin.setZero();lastTouchHome=owner.homeTeam;
        matchStatistics.recordPossessionSeconds(delta*MATCH_TIME_SCALE,owner.homeTeam);
        if(possessionOwnerIndex==matchSimulation.getControlledPlayerIndex())return;
        float goalProgress=owner.z*ownerAttackSign;
        float nearestOpponentDistance=Float.MAX_VALUE;
        float goalkeeperX=0f;
        for(MatchSimulation.PlayerState candidate:lineup){
            if(!candidate.active||candidate.homeTeam==owner.homeTeam)continue;
            float dx=candidate.x-owner.x,dz=candidate.z-owner.z;
            nearestOpponentDistance=Math.min(nearestOpponentDistance,(float)Math.sqrt(dx*dx+dz*dz));
            if("GK".equals(candidate.role))goalkeeperX=candidate.x;
        }
        if(MatchGameplay.shouldAiShoot(goalProgress,owner.x)){
            if(gltfPlayerSquad!=null)
                gltfPlayerSquad.playAction(possessionOwnerIndex,"soccer_shoot");
            float aimX=MatchGameplay.chooseAiShotAimX(owner.x,goalkeeperX,nearestOpponentDistance);
            ballVelocity.set(MatchGameplay.shotVelocity(owner.homeTeam,aimX,
                    matchSimulation.homeAttacksNegativeZ()));
            ballSpin.set(0f,owner.homeTeam?18f:-18f,0f);
            matchStatistics.recordShot(owner.playerId,owner.homeTeam,matchClock.getTotalElapsedSeconds());
            possessionOwnerIndex=MatchGameplay.NO_OWNER;aiKickClock=1.3f;
            return;
        }
        aiKickClock=Math.max(0f,aiKickClock-delta);
        if(MatchGameplay.shouldAiCarryBall(goalProgress,nearestOpponentDistance)&&aiKickClock>0f){
            // Keep updating a safe forward target so the simulation moves the carrier with the ball.
            Vector3 carryTarget=MatchGameplay.chooseAiCarryTarget(
                    lineup,possessionOwnerIndex,matchSimulation.homeAttacksNegativeZ());
            owner.targetX=carryTarget.x;
            owner.targetZ=carryTarget.z;
            return;
        }
        if(aiKickClock>0f)return;
        MatchGameplay.PassTargetDecision passDecision=MatchGameplay.findForwardPassDecision(
                lineup,possessionOwnerIndex,owner.x,owner.z,owner.homeTeam,matchSimulation.homeAttacksNegativeZ());
        int receiver=passDecision.receiverIndex;
        clearPendingPass();
        pendingOffsideReceiver=passDecision.offside?receiver:MatchGameplay.NO_OWNER;
        if(receiver!=MatchGameplay.NO_OWNER){
            if(gltfPlayerSquad!=null)
                gltfPlayerSquad.playAction(possessionOwnerIndex,"soccer_pass");
            MatchSimulation.PlayerState target=lineup.get(receiver);
            matchStatistics.recordPassAttempt(owner.playerId,owner.homeTeam,matchClock.getTotalElapsedSeconds());
            beginPendingPass(owner,receiver);
            ballVelocity.set(MatchGameplay.passVelocity(owner.x,owner.z,target.x,target.z));
        }else{
            pendingOffsideReceiver=MatchGameplay.NO_OWNER;
            ballVelocity.set(MatchGameplay.clearanceVelocity(owner.homeTeam,owner.x,matchSimulation.homeAttacksNegativeZ()));
        }
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
        // Penalty areas and six-yard goal areas.
        line(-20.16f, y, -52.5f, 20.16f, y, -52.5f);
        line(-20.16f, y, -52.5f, -20.16f, y, -35f);
        line(20.16f, y, -52.5f, 20.16f, y, -35f);
        line(-20.16f, y, 52.5f, 20.16f, y, 52.5f);
        line(-20.16f, y, 52.5f, -20.16f, y, 35f);
        line(20.16f, y, 52.5f, 20.16f, y, 35f);
        line(-9.16f,y,-52.5f,9.16f,y,-52.5f);
        line(-9.16f,y,-52.5f,-9.16f,y,-47f);
        line(9.16f,y,-52.5f,9.16f,y,-47f);
        line(-9.16f,y,52.5f,9.16f,y,52.5f);
        line(-9.16f,y,52.5f,-9.16f,y,47f);
        line(9.16f,y,52.5f,9.16f,y,47f);
        // Centre and penalty spots, plus corner arcs.
        circleOnPitch(0f,0f,0.18f,y);
        circleOnPitch(0f,-41.5f,0.18f,y);
        circleOnPitch(0f,41.5f,0.18f,y);
        drawCornerArc(-34f,-52.5f,1f,y,0f);
        drawCornerArc(34f,-52.5f,1f,y,90f);
        drawCornerArc(34f,52.5f,1f,y,180f);
        drawCornerArc(-34f,52.5f,1f,y,270f);
        shapeRenderer.end();
    }

    private void drawGoalNet(float z,float y){
        float back=z+Math.signum(z)*2.2f;
        line(-3.66f,y,z,-3.66f,2.44f,z);line(3.66f,y,z,3.66f,2.44f,z);
        line(-3.66f,2.44f,z,3.66f,2.44f,z);
        line(-3.66f,y,z,-3.66f,y,back);line(3.66f,y,z,3.66f,y,back);
        line(-3.66f,2.44f,z,-3.66f,2.44f,back);line(3.66f,2.44f,z,3.66f,2.44f,back);
        line(-3.66f,y,back,3.66f,y,back);line(-3.66f,2.44f,back,3.66f,2.44f,back);
        // Fine mesh grid makes the goal read as a net rather than a wireframe frame.
        for(int row=1;row<6;row++){
            float netY=row*0.4f;
            line(-3.66f,netY,z,3.66f,netY,z);
            line(-3.66f,netY,back,3.66f,netY,back);
            line(-3.66f,netY,z,-3.66f,netY,back);
            line(3.66f,netY,z,3.66f,netY,back);
        }
        for(int column=1;column<12;column++){
            float netX=-3.66f+column*(7.32f/12f);
            line(netX,y,z,netX,2.44f,z);
            line(netX,y,back,netX,2.44f,back);
            line(netX,y,z,netX,y,back);
            line(netX,2.44f,z,netX,2.44f,back);
        }
    }

    private void drawCornerArc(float x,float z,float radius,float y,float startDegrees){
        for(int i=0;i<8;i++){
            float a1=(float)Math.toRadians(startDegrees+i*90f/8f);
            float a2=(float)Math.toRadians(startDegrees+(i+1)*90f/8f);
            line(x+(float)Math.cos(a1)*radius,y,z+(float)Math.sin(a1)*radius,
                    x+(float)Math.cos(a2)*radius,y,z+(float)Math.sin(a2)*radius);
        }
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
        int width=Gdx.graphics.getWidth(),height=Gdx.graphics.getHeight();
        if(width<=0||height<=0)return;
        spriteBatch.begin();
        float originalScale=font.getData().scaleX;
        float hudScale=Math.max(0.68f,Math.min(1.05f,Math.min(width/1180f,height/720f)));
        font.getData().setScale(hudScale);

        float matchSeconds=matchClock.getTotalElapsedSeconds();
        int matchMinute=(int)(matchSeconds/60f);
        String clock=String.format(java.util.Locale.US,"%02d:%02d",matchMinute,(int)(matchSeconds%60f));
        String scoreLine=matchSimulation.getHomeTeamName()+"  "+matchState.getHomeGoals()
                +" - "+matchState.getAwayGoals()+"  "+matchSimulation.getAwayTeamName()+"  |  "+clock;
        font.draw(spriteBatch,scoreLine,18f,height-18f);

        MatchSimulation.PlayerState activePlayer=matchSimulation.getControlledPlayer();
        if(activePlayer!=null)font.draw(spriteBatch,"JUGADOR: "+activePlayer.playerName
                +"  #"+activePlayer.shirtNumber+"  "+activePlayer.role,18f,height-42f);

        String phaseText;
        switch(matchClock.getPhase()){
            case PRE_MATCH: phaseText="AMISTOSO | ARRIBA IZQ: EQUIPO LOCAL | CENTRO: VISITANTE | DERECHA: INICIAR"; break;
            case HALF_TIME: phaseText="DESCANSO | ARRIBA IZQ: BANQUILLO | DERECHA: COMENZAR 2º TIEMPO"; break;
            case FULL_TIME: phaseText="FINAL | ARRIBA IZQ: NUEVO PARTIDO"; break;
            default: phaseText=matchClock.isPaused()
                    ?"PARTIDO PAUSADO | ARRIBA DERECHA: CONTINUAR"
                    :"CENTRO ARRIBA: CAMBIAR JUGADOR | DERECHA ARRIBA: PAUSA";
        }
        font.draw(spriteBatch,phaseText,18f,height-66f);
        if(matchAdvantage.isActive())font.draw(spriteBatch,"LEY DE LA VENTAJA",18f,height-90f);

        String discipline="TARJETAS "+countTeamCards(true,false)+"-"+countTeamCards(false,false)
                +" | ROJAS "+countTeamCards(true,true)+"-"+countTeamCards(false,true)
                +" | JUGADORES "+matchSimulation.getActivePlayerCount(true)+"-"+matchSimulation.getActivePlayerCount(false);
        String attacking="TIROS "+matchStatistics.getShots(true)+"-"+matchStatistics.getShots(false)
                +" | A PUERTA "+matchStatistics.getShotsOnTarget(true)+"-"+matchStatistics.getShotsOnTarget(false)
                +" | PARADAS "+matchStatistics.getSaves(true)+"-"+matchStatistics.getSaves(false)
                +" | ENTRADAS "+matchStatistics.getTackles(true)+"-"+matchStatistics.getTackles(false);
        String passing="PASES "+matchStatistics.getCompletedPasses(true)+"/"+matchStatistics.getPasses(true)
                +"-"+matchStatistics.getCompletedPasses(false)+"/"+matchStatistics.getPasses(false)
                +" | FUERAS "+matchStatistics.getOffsides(true)+"-"+matchStatistics.getOffsides(false)
                +" | POSESIÓN "+String.format(java.util.Locale.US,"%.0f-%.0f",
                matchStatistics.getPossessionPercent(true),matchStatistics.getPossessionPercent(false))+"%";
        font.draw(spriteBatch,discipline,18f,76f);
        font.draw(spriteBatch,attacking,18f,52f);
        font.draw(spriteBatch,passing,18f,28f);

        if(substitutionMode){
            List<OriginalProjectData.PlayerRecord> bench=matchSimulation.getBenchPlayers(substitutionHomeTeam);
            int firstOutgoing=substitutionHomeTeam?1:12;
            int lastOutgoing=substitutionHomeTeam?10:21;
            String teamLabel=substitutionHomeTeam?"LOCAL":"VISITANTE";
            String outgoingName=substitutionOutgoingIndex>=firstOutgoing&&substitutionOutgoingIndex<=lastOutgoing
                    ?matchSimulation.getPlayers().get(substitutionOutgoingIndex).playerName:"—";
            String incomingName=substitutionIncomingIndex>=0&&substitutionIncomingIndex<bench.size()
                    ?bench.get(substitutionIncomingIndex).name:"—";
            font.draw(spriteBatch,"CAMBIOS "+teamLabel+" | TITULAR: "+outgoingName+" | SUPLENTE: "+incomingName+" | CONFIRMAR A LA DERECHA",
                    18f,height*0.48f);
            font.draw(spriteBatch,"CAMBIOS USADOS: "+matchSimulation.getSubstitutionsUsed(substitutionHomeTeam)+"/5 | ESQUINA INFERIOR IZQ: CAMBIAR EQUIPO",
                    18f,height*0.44f);
        }else if(matchClock.isPaused()||matchClock.getPhase()==MatchClock.Phase.HALF_TIME){
            font.draw(spriteBatch,"ARRIBA IZQ: BANQUILLO / CAMBIOS",18f,height*0.48f);
        }
        if(matchClock.getPhase()==MatchClock.Phase.FULL_TIME&&Gdx.app!=null)
            font.draw(spriteBatch,"ACTAS GUARDADAS: "+MatchReportStore.count(Gdx.app.getPreferences("futbol_nova_match_reports")),
                    18f,100f);

        font.getData().setScale(originalScale);
        spriteBatch.end();
    }

    /** Draws the same touch zones consumed by updateTouchControls so controls are discoverable. */
    private void drawTouchControls(){
        int width=Gdx.graphics.getWidth(),height=Gdx.graphics.getHeight();
        if(width<=0||height<=0||!matchClock.canAcceptGameplayInput())return;

        float stickX=width*0.28f,stickY=height*0.50f;
        float stickRadius=Math.max(1f,Math.min(width*0.22f,height*0.38f));
        float knobX=stickX+touchMovement.x*stickRadius*0.58f;
        float knobY=stickY-touchMovement.z*stickRadius*0.58f;
        float actionX=width*0.82f;
        float actionRadius=Math.max(24f,Math.min(width*0.07f,height*0.105f));

        hudProjection.setToOrtho2D(0f,0f,width,height);
        shapeRenderer.setProjectionMatrix(hudProjection);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.04f,0.08f,0.12f,0.34f);
        shapeRenderer.circle(stickX,stickY,stickRadius);
        shapeRenderer.setColor(0.12f,0.72f,0.88f,0.62f);
        shapeRenderer.circle(knobX,knobY,stickRadius*0.30f);
        float[] actionY={height*0.83f,height*0.50f,height*0.17f};
        float[][] actionColors={{0.92f,0.30f,0.20f},{0.08f,0.62f,0.86f},{0.92f,0.66f,0.16f}};
        for(int i=0;i<actionY.length;i++){
            float[] color=actionColors[i];
            shapeRenderer.setColor(color[0],color[1],color[2],0.52f);
            shapeRenderer.circle(actionX,actionY[i],actionRadius);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f,1f,1f,0.78f);
        shapeRenderer.circle(stickX,stickY,stickRadius);
        shapeRenderer.circle(knobX,knobY,stickRadius*0.30f);
        for(float y:actionY)shapeRenderer.circle(actionX,y,actionRadius);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        spriteBatch.begin();
        float labelScale=font.getData().scaleX;
        font.getData().setScale(Math.min(1.05f,Math.max(0.72f,width/900f)));
        font.draw(spriteBatch,"MOVER",stickX-stickRadius*0.34f,stickY-stickRadius-12f);
        font.draw(spriteBatch,"TIRO",actionX-actionRadius*0.45f,actionY[0]+5f);
        font.draw(spriteBatch,"PASE",actionX-actionRadius*0.45f,actionY[1]+5f);
        font.draw(spriteBatch,"ROBO",actionX-actionRadius*0.45f,actionY[2]+5f);
        font.getData().setScale(labelScale);
        spriteBatch.end();
    }

    private int countTeamCards(boolean homeTeam,boolean redCards){
        int count=0;
        for(MatchSimulation.PlayerState player:matchSimulation.getPlayers()){
            if(player.homeTeam!=homeTeam)continue;
            if(redCards?matchDiscipline.isSentOff(player.playerId):matchDiscipline.getYellowCards(player.playerId)>0)count++;
        }
        return count;
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null && width > 0 && height > 0) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }
    }


    /** Android lifecycle pause freezes the match and writes a recovery checkpoint. */
    @Override
    public void pause(){
        if(matchClock.isPlaying())matchClock.pause();
        matchState.setPaused(true);
        saveMatchCheckpoint();
    }

    /** The saved match stays paused until the player explicitly resumes it in the HUD. */
    @Override
    public void resume(){
        if(camera!=null)camera.update();
    }

    private void saveMatchCheckpoint(){
        if(Gdx.app==null||matchClock.getPhase()==MatchClock.Phase.PRE_MATCH
                ||matchClock.getPhase()==MatchClock.Phase.FULL_TIME)return;
        MatchRuntimeSnapshot s=new MatchRuntimeSnapshot();
        s.simulation=matchSimulation.snapshot();s.clock=matchClock.snapshot();s.matchState=matchState.snapshot();
        s.statistics=matchStatistics.snapshot();s.discipline=matchDiscipline.snapshotState();s.advantage=matchAdvantage.snapshot();
        s.ballPosition=new float[]{ballPosition.x,ballPosition.y,ballPosition.z};
        s.ballVelocity=new float[]{ballVelocity.x,ballVelocity.y,ballVelocity.z};
        s.ballSpin=new float[]{ballSpin.x,ballSpin.y,ballSpin.z};
        s.controlledPosition=new float[]{controlledPosition.x,controlledPosition.y,controlledPosition.z};
        s.aiTackleCooldowns=aiTackleCooldowns.clone();s.goalkeeperRecoveryTimers=goalkeeperRecoveryTimers.clone();
        s.goalkeeperDiveDirections=goalkeeperDiveDirections.clone();
        s.possessionOwnerIndex=possessionOwnerIndex;s.pendingOffsideReceiver=pendingOffsideReceiver;
        s.pendingPassReceiver=pendingPassReceiver;s.pendingPasserPlayerId=pendingPasserPlayerId;
        s.pendingPasserHomeTeam=pendingPasserHomeTeam;s.pendingPassClock=pendingPassClock;
        s.aiKickClock=aiKickClock;s.animationClock=animationClock;s.tackleCooldown=tackleCooldown;
        s.restartClock=restartClock;s.ballRotation=ballRotation;s.pendingRestart=pendingRestart;
        s.restartAwardedHome=restartAwardedHome;s.kickoffRestart=kickoffRestart;s.lastTouchHome=lastTouchHome;
        s.matchReportSaved=matchReportSaved;s.substitutionMode=substitutionMode;
        s.substitutionHomeTeam=substitutionHomeTeam;s.substitutionOutgoingIndex=substitutionOutgoingIndex;
        s.substitutionIncomingIndex=substitutionIncomingIndex;s.substitutionWindow=substitutionWindow;
        if(!MatchSaveStore.save(s))Gdx.app.error("FutbolNova","Match checkpoint was not saved");
    }

    private void restoreSavedMatch(){
        MatchRuntimeSnapshot s=MatchSaveStore.load();
        if(s==null)return;
        if(!s.isValid()){Gdx.app.error("FutbolNova","Saved match failed validation; fresh match retained");return;}
        if(!matchSimulation.restore(s.simulation)||!matchClock.restore(s.clock)
                ||!matchState.restore(s.matchState)||!matchStatistics.restore(s.statistics)
                ||!matchDiscipline.restore(s.discipline)||!matchAdvantage.restore(s.advantage)){
            Gdx.app.error("FutbolNova","Saved match could not be restored; fresh match retained");return;
        }
        ballPosition.set(s.ballPosition[0],s.ballPosition[1],s.ballPosition[2]);
        ballVelocity.set(s.ballVelocity[0],s.ballVelocity[1],s.ballVelocity[2]);
        ballSpin.set(s.ballSpin[0],s.ballSpin[1],s.ballSpin[2]);
        controlledPosition.set(s.controlledPosition[0],s.controlledPosition[1],s.controlledPosition[2]);
        System.arraycopy(s.aiTackleCooldowns,0,aiTackleCooldowns,0,22);
        System.arraycopy(s.goalkeeperRecoveryTimers,0,goalkeeperRecoveryTimers,0,22);
        System.arraycopy(s.goalkeeperDiveDirections,0,goalkeeperDiveDirections,0,22);
        possessionOwnerIndex=s.possessionOwnerIndex;pendingOffsideReceiver=s.pendingOffsideReceiver;
        pendingPassReceiver=s.pendingPassReceiver;pendingPasserPlayerId=s.pendingPasserPlayerId;
        pendingPasserHomeTeam=s.pendingPasserHomeTeam;pendingPassClock=s.pendingPassClock;
        aiKickClock=s.aiKickClock;animationClock=s.animationClock;tackleCooldown=s.tackleCooldown;
        restartClock=s.restartClock;ballRotation=s.ballRotation;pendingRestart=s.pendingRestart;
        restartAwardedHome=s.restartAwardedHome;kickoffRestart=s.kickoffRestart;lastTouchHome=s.lastTouchHome;
        matchReportSaved=s.matchReportSaved;substitutionMode=s.substitutionMode;
        substitutionHomeTeam=s.substitutionHomeTeam;substitutionOutgoingIndex=s.substitutionOutgoingIndex;
        substitutionIncomingIndex=s.substitutionIncomingIndex;substitutionWindow=s.substitutionWindow;
        broadcastCamera.reset(camera,ballPosition);checkpointClock=0f;
        Gdx.app.log("FutbolNova","Recovered saved match: "+matchSimulation.getHomeTeamName()+" vs "+matchSimulation.getAwayTeamName());
    }

    @Override
    public void dispose() {
        saveMatchCheckpoint();
        saveCompletedMatchReport();
        if (disposed) return;
        disposed = true;
        if (gltfPlayerSquad != null) gltfPlayerSquad.dispose();
        if (modelBatch != null) modelBatch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (spriteBatch != null) spriteBatch.dispose();
        if (font != null) font.dispose();
        for (Model model : ownedModels) model.dispose();
        ownedModels.clear();
    }
}
