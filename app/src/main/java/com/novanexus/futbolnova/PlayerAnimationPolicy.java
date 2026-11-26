package com.novanexus.futbolnova;

/** Pure gameplay-to-animation decisions; kept independent of rendering for deterministic tests. */
public final class PlayerAnimationPolicy {
    private static final float SPRINT_SPEED = 2.8f;
    private static final float MOVING_SPEED = 0.25f;
    private static final float HEADER_HEIGHT = 0.98f;
    private static final float CHEST_CONTROL_HEIGHT = 0.65f;
    private static final float MAX_CONTROL_HEIGHT = 1.1f;
    private static final float SLIDE_TACKLE_SPEED = 3.0f;

    private PlayerAnimationPolicy() {
    }

    public static String locomotionHint(float speed) {
        if (!Float.isFinite(speed) || speed <= MOVING_SPEED) return "idle";
        return speed > SPRINT_SPEED ? "sprint" : "walk";
    }

    public static String tackleHint(float speed) {
        return Float.isFinite(speed) && speed >= SLIDE_TACKLE_SPEED
                ? "slide_tackle" : "standing_tackle";
    }

    public static String aerialControlHint(float ballHeight) {
        if (!Float.isFinite(ballHeight) || ballHeight < CHEST_CONTROL_HEIGHT
                || ballHeight > MAX_CONTROL_HEIGHT) return null;
        return ballHeight >= HEADER_HEIGHT ? "soccer_header" : "soccer_chest_control";
    }

    /** Matches Ara display labels and exported IDs such as "Shoot (football)" and "soccer_shoot". */
    static boolean clipNameMatches(String animationId, String hint) {
        if (animationId == null || hint == null) return false;
        String id = normalizeClipName(animationId);
        String rawHint = hint.trim();
        int descriptor = rawHint.indexOf('(');
        String baseHint = descriptor > 0 ? rawHint.substring(0, descriptor).trim() : rawHint;
        String full = normalizeClipName(rawHint);
        String base = normalizeClipName(baseHint);
        if (id.isEmpty()) return false;
        if (containsClipToken(id, full) || containsClipToken(id, base)) return true;
        if (full.startsWith("soccer")) return containsClipToken(id, full.substring(6));
        if (full.startsWith("goalkeeper")) return containsClipToken(id, full.substring(10));
        if (full.startsWith("keeper")) return containsClipToken(id, full.substring(6));
        return false;
    }

    private static boolean containsClipToken(String id, String token) {
        if (token == null || token.length() < 3) return false;
        // "stand" is a prefix of "standing tackle", not an idle-pose synonym.
        if ("stand".equals(token))
            return id.equals("stand") || id.startsWith("standidle") || id.startsWith("standloop");
        return id.contains(token);
    }

    private static String normalizeClipName(String value) {
        StringBuilder normalized = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = Character.toLowerCase(value.charAt(i));
            if (Character.isLetterOrDigit(ch)) normalized.append(ch);
        }
        return normalized.toString();
    }
    public static String goalkeeperSaveHint(boolean caught, float diveDirection) {
        if (caught) return "keeper_catch";
        if (!Float.isFinite(diveDirection) || diveDirection == 0f) return "keeper_punch";
        return diveDirection < 0f ? "keeper_dive_left" : "keeper_dive_right";
    }
}