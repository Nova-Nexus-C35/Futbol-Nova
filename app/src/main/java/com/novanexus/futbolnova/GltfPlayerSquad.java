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
 * Optional GLB-backed squad renderer. A single source SceneAsset is shared by
 * per-player Scene instances; match state remains owned by MatchSimulation.
 * If the asset is absent or fails to load, FutbolNovaGame keeps PlayerVisual.
 */
public final class GltfPlayerSquad implements Disposable {
    public static final String DEFAULT_ASSET = "models/players/active.glb";

    private final SceneAsset asset;
    private final SceneManager sceneManager;
    private final Array<Scene> players = new Array<>();
    private final String[] activeClips;
    private final float modelScale;

    public GltfPlayerSquad(String assetPath, PerspectiveCamera camera, int playerCount, float modelScale) {
        if (assetPath == null || assetPath.trim().isEmpty()) {
            throw new IllegalArgumentException("GLB asset path is required");
        }
        if (camera == null || playerCount < 1 || !Float.isFinite(modelScale) || modelScale <= 0f) {
            throw new IllegalArgumentException("Invalid GLB squad configuration");
        }
        if (!Gdx.files.internal(assetPath).exists()) {
            throw new IllegalStateException("GLB player asset not found: " + assetPath);
        }

        asset = new GLTFLoader().load(Gdx.files.internal(assetPath));
        if (asset == null || asset.scene == null || asset.scene.model == null) {
            if (asset != null) asset.dispose();
            throw new IllegalStateException("GLB has no renderable default scene: " + assetPath);
        }

        sceneManager = new SceneManager();
        sceneManager.setCamera(camera);
        sceneManager.setAmbientLight(0.72f);
        DirectionalLightEx keyLight = new DirectionalLightEx();
        keyLight.direction.set(-0.45f, -1f, -0.35f).nor();
        keyLight.color.set(Color.WHITE);
        sceneManager.environment.add(keyLight);

        this.modelScale = modelScale;
        activeClips = new String[playerCount];
        for (int i = 0; i < playerCount; i++) {
            Scene scene = new Scene(asset.scene);
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
                .rotate(Vector3.Y, yawRadians * MathUtils.radiansToDegrees)
                .scale(modelScale, modelScale, modelScale);

        String desired = movementSpeed > 2.8f
                ? findClip(scene.modelInstance.animations, "sprint", "run")
                : movementSpeed > 0.25f
                ? findClip(scene.modelInstance.animations, "walk", "jog", "run")
                : findClip(scene.modelInstance.animations, "idle", "stand");
        if (desired != null && !desired.equals(activeClips[index])) {
            play(scene, index, desired);
        }
    }

    /** Advances skeletal animation and renders the complete GLB-backed squad. */
    public void render(float delta) {
        sceneManager.update(Math.max(0f, Math.min(delta, 0.05f)));
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
                if (id.contains(hint)) return animation.id;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        sceneManager.dispose();
        asset.dispose();
    }
}
