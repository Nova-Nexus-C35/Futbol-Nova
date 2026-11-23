package com.novanexus.futbolnova;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxNativesLoader;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public final class BroadcastCameraTest {
    @BeforeClass public static void loadLibGdxDesktopNatives() {
        GdxNativesLoader.load();
    }

    @Test public void resetCreatesElevatedThreeQuarterBroadcastView() {
        BroadcastCamera controller = new BroadcastCamera();
        PerspectiveCamera camera = new PerspectiveCamera(55f, 1920f, 1080f);
        controller.reset(camera, new Vector3(0f, 0.55f, 0f));
        assertTrue(camera.position.y >= 32f);
        assertTrue(camera.position.x > 45f);
        assertTrue(camera.position.z > 20f);
        assertEquals(49f, camera.fieldOfView, 0.001f);
    }

    @Test public void followSmoothsFocusAndClampsItToThePitch() {
        BroadcastCamera controller = new BroadcastCamera();
        PerspectiveCamera camera = new PerspectiveCamera(55f, 1920f, 1080f);
        controller.reset(camera, new Vector3());
        controller.update(camera, new Vector3(100f, 0.55f, 100f), 12f, 0.016f);
        Vector3 focus = controller.getSmoothedFocus();
        assertTrue(focus.x > 0f && focus.x < 34f);
        assertTrue(focus.z > 0f && focus.z < 52.5f);
        assertTrue(camera.position.y > 32f);
        assertTrue(camera.direction.y < 0f);
    }

    @Test public void invalidDeltaCannotCauseCameraToJump() {
        BroadcastCamera controller = new BroadcastCamera();
        PerspectiveCamera camera = new PerspectiveCamera(55f, 1920f, 1080f);
        controller.reset(camera, new Vector3());
        Vector3 before = controller.getSmoothedFocus();
        controller.update(camera, new Vector3(20f, 0f, 20f), 0f, Float.NaN);
        assertEquals(before.x, controller.getSmoothedFocus().x, 0.0001f);
        assertEquals(before.z, controller.getSmoothedFocus().z, 0.0001f);
    }

    @Test public void fastTransitionsWidenTheBroadcastViewSmoothly() {
        BroadcastCamera controller = new BroadcastCamera();
        PerspectiveCamera camera = new PerspectiveCamera(55f, 1920f, 1080f);
        controller.reset(camera, new Vector3());
        float initialFov = camera.fieldOfView;
        controller.update(camera, new Vector3(), 30f, 0.05f);
        assertTrue(camera.fieldOfView > initialFov);
        assertTrue(camera.fieldOfView < 57f);
        for (int i = 0; i < 40; i++) {
            controller.update(camera, new Vector3(), 30f, 0.05f);
        }
        assertTrue(camera.fieldOfView > 54f);
        assertTrue(camera.fieldOfView <= 57f);
    }
}
