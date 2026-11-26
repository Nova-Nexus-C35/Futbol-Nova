package com.novanexus.futbolnova;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;

/**
 * Optional GLB-backed two-team renderer. Home and away assets are loaded once,
 * then shared by independent Scene instances. Match coordinates remain authoritative.
 * Any asset/load/render failure is handled by FutbolNovaGame's procedural fallback.
 */
public final class GltfPlayerSquad implements Disposable {
    public static final String HOME_ASSET = "models/players/home.glb";
    public static final String AWAY_ASSET = "models/players/away.glb";

    private final SceneAsset homeAsset;
    private final SceneAsset awayAsset;
    private final SceneManager sceneManager;
    private final Array<Scene> players = new Array<>();
    private final String[] activeClips;
    private final float[] actionLockSeconds;
    private final float modelScale;

    public GltfPlayerSquad(String homePath, String awayPath, PerspectiveCamera camera,
                           int playersPerTeam, float modelScale) {
        if (homePath == null || homePath.trim().isEmpty()
                || awayPath == null || awayPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Both home and away GLB asset paths are required");
        }
        if (camera == null || playersPerTeam < 1 || !Float.isFinite(modelScale) || modelScale <= 0f) {
            throw new IllegalArgumentException("Invalid GLB squad configuration");
        }
        if (!Gdx.files.internal(homePath).exists() || !Gdx.files.internal(awayPath).exists()) {
            throw new IllegalStateException("Both home and away GLB assets must be bundled");
        }

        SceneAsset loadedHome = new GLTFLoader().load(Gdx.files.internal(homePath));
        SceneAsset loadedAway = null;
        try {
            if (loadedHome == null || loadedHome.scene == null || loadedHome.scene.model == null) {
                throw new IllegalStateException("Home GLB has no renderable default scene");
            }
            loadedAway = new GLTFLoader().load(Gdx.files.internal(awayPath));
            if (loadedAway == null || loadedAway.scene == null || loadedAway.scene.model == null) {
                throw new IllegalStateException("Away GLB has no renderable default scene");
            }
        } catch (RuntimeException error) {
            if (loadedAway != null) loadedAway.dispose();
            if (loadedHome != null) loadedHome.dispose();
            throw error;
        }
        homeAsset = loadedHome;
        awayAsset = loadedAway;

        sceneManager = new SceneManager(Math.max(24, Math.max(homeAsset.maxBones, awayAsset.maxBones)));
        sceneManager.setCamera(camera);
        sceneManager.setAmbientLight(0.72f);
        DirectionalLightEx keyLight = new DirectionalLightEx();
        keyLight.direction.set(-0.45f, -1f, -0.35f).nor();
        keyLight.color.set(Color.WHITE);
        sceneManager.environment.add(keyLight);

        this.modelScale = modelScale;
        int totalPlayers = playersPerTeam * 2;
        activeClips = new String[totalPlayers];
        actionLockSeconds = new float[totalPlayers];
        for (int i = 0; i < totalPlayers; i++) {
            SceneAsset teamAsset = i < playersPerTeam ? homeAsset : awayAsset;
            Scene scene = new Scene(teamAsset.scene);
            players.add(scene);
            sceneManager.addScene(scene);
            String idle = findClip(scene.modelInstance.animations, "idle", "stand");
            if (idle == null) idle = firstClip(scene.modelInstance.animations);
            play(scene, i, idle);
        }
    }

    public int size() {
        return players.size;
    }

    /** Updates one player from authoritative match coordinates and velocity. */
    public void updatePlayer(int index, float x, float z, float yawRadians, float movementSpeed) {
        if (index < 0 || index >= players.size) return;
        Scene scene = players.get(index);
        scene.modelInstance.transform.setToTranslation(x, 0f, z)
                .rotate(Vector3.Y, yawRadians * MathUtils.radiansToDegrees + 180f)
                .scale(modelScale, modelScale, modelScale);

        if (actionLockSeconds[index] > 0f) return;
        String desired = movementSpeed > 2.8f
                ? findClip(scene.modelInstance.animations, "sprint", "run")
                : movementSpeed > 0.25f
                ? findClip(scene.modelInstance.animations, "walk", "jog", "run")
                : findClip(scene.modelInstance.animations, "idle", "stand");
        if (desired != null && !desired.equals(activeClips[index])) play(scene, index, desired);
    }

    /** Hides dismissed or substituted players without removing their stable lineup slot. */
    public void setPlayerActive(int index, boolean active) {
        if (index < 0 || index >= players.size) return;
        if (!active) players.get(index).modelInstance.transform.setToTranslation(0f, -10000f, 0f);
    }

    /** Starts a football action clip if it exists in this player's GLB. */
    public boolean playAction(int index, String clipHint) {
        if (index < 0 || index >= players.size || clipHint == null || clipHint.trim().isEmpty()) return false;
        Scene scene = players.get(index);
        String clip = findClip(scene.modelInstance.animations, clipHint);
        if (clip == null) return false;
        play(scene, index, clip);
        String hint = clipHint.toLowerCase(java.util.Locale.ROOT);
        actionLockSeconds[index] = hint.contains("dive") ? 1.7f
                : hint.contains("shoot") ? 0.95f
                : hint.contains("pass") ? 0.8f
                : hint.contains("tackle") ? 1.25f : 1.0f;
        return true;
    }

    public void render(float delta) {
        float safeDelta = Math.max(0f, Math.min(delta, 0.05f));
        for (int i = 0; i < actionLockSeconds.length; i++)
            actionLockSeconds[i] = Math.max(0f, actionLockSeconds[i] - safeDelta);
        sceneManager.update(safeDelta);
        sceneManager.render();
    }

    private void play(Scene scene, int index, String clipId) {
        if (clipId == null || scene.animationController == null) return;
        scene.animationController.setAnimation(clipId, -1);
        activeClips[index] = clipId;
    }

    private static String firstClip(Array<Animation> animations) {
        return animations == null || animations.size == 0 ? null : animations.first().id;
    }

    private static String findClip(Array<Animation> animations, String... hints) {
        if (animations == null) return null;
        for (Animation animation : animations) {
            if (animation.id == null) continue;
            String id = animation.id.toLowerCase(java.util.Locale.ROOT);
            for (String hint : hints) {
                if (id.contains(hint.toLowerCase(java.util.Locale.ROOT))) return animation.id;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        sceneManager.dispose();
        homeAsset.dispose();
        awayAsset.dispose();
    }
}
