package com.novanexus.futbolnova;

import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public class GltfPlayerFixtureTest {
    @Test
    public void bundledGlbFixtureContainsRigAndFootballRelevantLocomotionClips() throws Exception {
        byte[] bytes;
        try (InputStream input = getClass().getResourceAsStream("/gltf/fixtures/quaternius-player.glb")) {
            assertNotNull("CC0 GLB fixture must be present on the test classpath", input);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            bytes = output.toByteArray();
        }

        assertTrue("GLB must contain its 12-byte header and first chunk", bytes.length > 20);
        ByteBuffer header = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals("glTF magic", 0x46546C67, header.getInt(0));
        assertEquals("glTF 2.0", 2, header.getInt(4));
        assertEquals("declared GLB length", bytes.length, header.getInt(8));

        int jsonLength = header.getInt(12);
        assertTrue("JSON chunk must fit inside GLB", jsonLength > 0 && 20 + jsonLength <= bytes.length);
        assertEquals("JSON chunk type", 0x4E4F534A, header.getInt(16));
        String json = new String(bytes, 20, jsonLength, StandardCharsets.UTF_8);
        assertTrue("fixture must contain a skin", json.contains("\"skins\""));
        assertTrue("fixture must contain skeletal animations", json.contains("\"animations\""));
        assertTrue("fixture must include a running clip", json.contains("Man_Run"));
        assertTrue("fixture must include an idle clip", json.contains("Man_Idle"));
    }

    @Test
    public void bundledFixtureIncludesCc0LicenseNotice() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/gltf/fixtures/QUATERNIUS-LICENSE.txt")) {
            assertNotNull("license notice must accompany the fixture", input);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            String notice = new String(output.toByteArray(), StandardCharsets.UTF_8);
            assertTrue(notice.contains("CC0 1.0"));
            assertTrue(notice.contains("kendrekaran/striker-3d"));
        }
    }
}
