package com.novanexus.futbolnova;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Elevated broadcast-style 3D camera with damped ball tracking and transition zoom.
 */
public final class BroadcastCamera {
    private static final float SIDE_OFFSET = 56f;
    private static final float BASE_HEIGHT = 32f;
    private static final float BASE_TRAILING_DISTANCE = 23f;
    private static final float FOLLOW_RESPONSE = 3.8f;
    private final Vector3 smoothedFocus = new Vector3();
    private final Vector3 desiredPosition = new Vector3();
    private final Vector3 requestedFocus = new Vector3();
    private final Vector3 desiredTarget = new Vector3();
    private boolean initialized;
    private float currentFieldOfView = 49f;

    public void reset(PerspectiveCamera camera, Vector3 focus) {
        smoothedFocus.set(focus.x, 0f, focus.z);
        initialized = true;
        currentFieldOfView = 49f;
        camera.fieldOfView = currentFieldOfView;
        apply(camera, 0f);
    }

    public void update(PerspectiveCamera camera, Vector3 focus, float ballSpeed, float delta) {
        if (!initialized) {
            reset(camera, focus);
            return;
        }
        float safeDelta = Float.isFinite(delta) ? MathUtils.clamp(delta, 0f, 0.1f) : 0f;
        float safeSpeed = Float.isFinite(ballSpeed) ? MathUtils.clamp(ballSpeed, 0f, 35f) : 0f;
        float alpha = 1f - (float)Math.exp(-FOLLOW_RESPONSE * safeDelta);
        requestedFocus.set(
                MathUtils.clamp(focus.x, -34f, 34f), 0f,
                MathUtils.clamp(focus.z, -52.5f, 52.5f));
        smoothedFocus.lerp(requestedFocus, alpha);
        float targetFieldOfView = 49f + Math.min(8f, safeSpeed * 0.22f);
        currentFieldOfView += (targetFieldOfView - currentFieldOfView) * alpha;
        camera.fieldOfView = currentFieldOfView;
        apply(camera, safeSpeed);
    }

    private void apply(PerspectiveCamera camera, float ballSpeed) {
        float transitionLift = Math.min(4f, ballSpeed * 0.10f);
        desiredPosition.set(
                MathUtils.clamp(SIDE_OFFSET + smoothedFocus.x * 0.08f, 50f, 63f),
                BASE_HEIGHT + transitionLift,
                smoothedFocus.z + BASE_TRAILING_DISTANCE);
        desiredTarget.set(smoothedFocus.x * 0.32f, 0.65f, smoothedFocus.z * 0.62f);
        camera.position.set(desiredPosition);
        camera.lookAt(desiredTarget);
        camera.up.set(Vector3.Y);
        camera.near = 0.1f;
        camera.far = 260f;
        camera.update();
    }

    public Vector3 getSmoothedFocus() {
        return new Vector3(smoothedFocus);
    }
}
