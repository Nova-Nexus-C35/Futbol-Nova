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

    private final MatchState matchState = new MatchState();
    private final List<Model> ownedModels = new ArrayList<>();
    private final List<ModelInstance> players = new ArrayList<>();
    private final Vector3 ballPosition = new Vector3(0f, BALL_RADIUS, 0f);
    private final Vector3 ballVelocity = new Vector3();
    private final Vector3 controlledPosition = new Vector3();

    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private Environment environment;
    private ModelInstance fieldInstance;
    private ModelInstance ballInstance;
    private ModelInstance controlledPlayer;
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

        Model field = builder.createBox(68f, 0.35f, 105f,
                new Material(ColorAttribute.createDiffuse(new Color(0.10f, 0.42f, 0.19f, 1f))), attributes);
        own(field);
        fieldInstance = new ModelInstance(field);
        fieldInstance.transform.setToTranslation(0f, -0.22f, 0f);

        Model homeModel = builder.createBox(1.15f, 1.85f, 0.9f,
                new Material(ColorAttribute.createDiffuse(new Color(0.10f, 0.35f, 0.95f, 1f))), attributes);
        Model awayModel = builder.createBox(1.15f, 1.85f, 0.9f,
                new Material(ColorAttribute.createDiffuse(new Color(0.92f, 0.16f, 0.12f, 1f))), attributes);
        own(homeModel);
        own(awayModel);

        float[][] formation = {
                {0f, 45f},
                {-23f, 31f}, {-8f, 34f}, {8f, 34f}, {23f, 31f},
                {-18f, 10f}, {0f, 13f}, {18f, 10f},
                {-19f, -20f}, {0f, -27f}, {19f, -20f}
        };

        for (int i = 0; i < formation.length; i++) {
            ModelInstance home = new ModelInstance(homeModel);
            home.transform.setToTranslation(formation[i][0], 0.93f, formation[i][1]);
            players.add(home);
            if (i == 6) {
                controlledPlayer = home;
                controlledPosition.set(formation[i][0], 0.93f, formation[i][1]);
            }

            ModelInstance away = new ModelInstance(awayModel);
            away.transform.setToTranslation(formation[i][0], 0.93f, -formation[i][1]);
            players.add(away);
        }

        Model ball = builder.createSphere(1.1f, 1.1f, 1.1f, 16, 16,
                new Material(ColorAttribute.createDiffuse(Color.WHITE)), attributes);
        own(ball);
        ballInstance = new ModelInstance(ball);
    }

    private void own(Model model) {
        ownedModels.add(model);
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);
        matchState.update(delta);
        updateTouchControls(delta);
        updateBall(delta);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.035f, 0.07f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        camera.update();
        ballInstance.transform.setToTranslation(ballPosition);
        modelBatch.begin(camera);
        modelBatch.render(fieldInstance, environment);
        for (ModelInstance player : players) modelBatch.render(player, environment);
        modelBatch.render(ballInstance, environment);
        modelBatch.end();

        drawPitchMarkings();
        drawHud();
    }

    private void updateTouchControls(float delta) {
        if (Gdx.input.isTouched()) {
            float x = Gdx.input.getX();
            float y = Gdx.input.getY();
            float width = Gdx.graphics.getWidth();
            float height = Gdx.graphics.getHeight();
            if (x < width * 0.58f) {
                float horizontal = (x - width * 0.27f) / (width * 0.27f);
                float vertical = (height * 0.50f - y) / (height * 0.50f);
                Vector3 movement = new Vector3(horizontal, 0f, -vertical);
                if (movement.len2() > 1f) movement.nor();
                controlledPosition.mulAdd(movement, 10f * delta);
                controlledPosition.x = Math.max(-FIELD_HALF_WIDTH + 2f, Math.min(FIELD_HALF_WIDTH - 2f, controlledPosition.x));
                controlledPosition.z = Math.max(-FIELD_HALF_LENGTH + 2f, Math.min(FIELD_HALF_LENGTH - 2f, controlledPosition.z));
                controlledPlayer.transform.setToTranslation(controlledPosition);
            }
        }

        if (Gdx.input.justTouched() && Gdx.input.getX() > Gdx.graphics.getWidth() * 0.60f) {
            float y = Gdx.input.getY();
            ballPosition.set(controlledPosition.x, BALL_RADIUS, controlledPosition.z - 1.3f);
            if (y < Gdx.graphics.getHeight() * 0.55f) {
                ballVelocity.set((controlledPosition.x * -0.08f), 8f, -25f);
            } else {
                ballVelocity.set((controlledPosition.x * -0.04f), 2.5f, -13f);
            }
        }
    }

    private void updateBall(float delta) {
        ballVelocity.y -= 18f * delta;
        ballPosition.mulAdd(ballVelocity, delta);
        if (ballPosition.y < BALL_RADIUS) {
            ballPosition.y = BALL_RADIUS;
            if (ballVelocity.y < 0f) ballVelocity.y *= -0.38f;
            ballVelocity.x *= 0.91f;
            ballVelocity.z *= 0.91f;
        }

        if (Math.abs(ballPosition.x) > FIELD_HALF_WIDTH - BALL_RADIUS) {
            ballPosition.x = Math.signum(ballPosition.x) * (FIELD_HALF_WIDTH - BALL_RADIUS);
            ballVelocity.x *= -0.55f;
        }

        if (Math.abs(ballPosition.z) > FIELD_HALF_LENGTH) {
            boolean insideGoal = Math.abs(ballPosition.x) < 3.66f && ballPosition.y < 2.44f;
            if (insideGoal) {
                matchState.addGoal(ballPosition.z < 0f);
                ballPosition.set(0f, BALL_RADIUS, 0f);
                ballVelocity.setZero();
            } else {
                ballPosition.z = Math.signum(ballPosition.z) * FIELD_HALF_LENGTH;
                ballVelocity.z *= -0.55f;
            }
        }

        if (ballVelocity.len2() < 0.08f && ballPosition.y <= BALL_RADIUS + 0.02f) {
            ballVelocity.setZero();
        }
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
        line(-20.16f, y, -52.5f, 20.16f, y, -52.5f);
        line(-20.16f, y, -52.5f, -20.16f, y, -35f);
        line(20.16f, y, -52.5f, 20.16f, y, -35f);
        line(-20.16f, y, 52.5f, 20.16f, y, 52.5f);
        line(-20.16f, y, 52.5f, -20.16f, y, 35f);
        line(20.16f, y, 52.5f, 20.16f, y, 35f);
        shapeRenderer.end();
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
        String clock = String.format(java.util.Locale.US, "%02d:%02d",
                (int) (matchState.getElapsedSeconds() / 60f),
                (int) (matchState.getElapsedSeconds() % 60f));
        font.draw(spriteBatch, "FUTBOL NOVA  |  " + matchState.getHomeGoals() + " - "
                + matchState.getAwayGoals() + "  |  " + clock, 18f, Gdx.graphics.getHeight() - 18f);
        font.draw(spriteBatch, "LADO IZQUIERDO: MOVER     DERECHA ARRIBA: TIRO     DERECHA ABAJO: PASE",
                18f, 28f);
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
