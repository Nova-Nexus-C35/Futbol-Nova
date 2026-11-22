# Fútbol Nova — 3D Game Vision

## Confirmed product direction

The target is a genuine 3D association-football game with a presentation and match experience inspired by the FIFA / EA SPORTS FC series. The current flat-looking prototype and simple colored player blocks are not the desired final visual style and must not be treated as acceptance criteria.

This is a direction and quality target, not a claim of feature parity with a commercial FIFA title. Fútbol Nova must use original branding, original or properly licensed assets, and its own implementation.

## Technical constraints to preserve

- Native Android game, Java 17 and libGDX.
- Do not migrate the project to Unity or Godot.
- Offline-first operation.
- ARM64 Android target, with Android 9 compatibility and mobile performance as constraints.
- Keep development on `rebuild/libgdx-android-2026-11-22`; do not merge into `main` without explicit authorization.
- Preserve existing code and history. Read files before editing and verify every GitHub write by reading the changed file back.

## Required visual and gameplay direction

1. **Fully 3D pitch and stadium:** three-dimensional grass surface, markings, goals, stadium structure, stands, lighting, shadows and atmosphere.
2. **Full 3D footballers:** complete humanoid models, team kits, boots and readable player silhouettes; colored rectangular placeholders are temporary only.
3. **Character animation:** locomotion, acceleration, deceleration, turning, dribbling, passing, shooting, jumping, tackling, goalkeeper actions and goal celebrations.
4. **3D ball simulation:** rotation, velocity, bounce, friction, collision response, flight and interaction with players and goal frames.
5. **Broadcast-style match camera:** elevated perspective similar to a televised football match, dynamically tracking play; additional camera modes may be added later.
6. **Complete match structure:** two teams, goalkeepers, match clock, score, goals, boundaries, restarts, fouls and core football rules.
7. **Mobile controls:** touch joystick and contextual buttons for passing, shooting, sprinting and player switching, with responsive layout for landscape Android screens.
8. **Football intelligence:** team positioning, formations, off-ball movement, opponent behavior, goalkeeper decision-making and player selection.
9. **Audio and presentation:** crowd ambience, whistle, ball impacts, commentary or optional sound packs, scoreboard and match UI, introduced without compromising offline play or performance.

## Implementation order

- **Phase A — 3D foundation:** verify the perspective camera and actual 3D depth; build a recognizable pitch, goals, stadium environment, lighting and shadows.
- **Phase B — playable 3D actors:** replace placeholders with optimized animated 3D player models and a 3D ball; implement movement, collision and ball control.
- **Phase C — match gameplay:** implement touch controls, player switching, passing, shooting, goalkeepers, team AI, scoring and match rules.
- **Phase D — broadcast presentation:** camera tracking, stadium atmosphere, match HUD, audio and polished transitions.
- **Phase E — optimization and validation:** profile on the target Android 9 ARM64 device, reduce draw calls and model complexity where needed, and verify sustained frame rate, thermal behavior and memory use.

## Acceptance principle

A screenshot showing only a flat-looking pitch and colored rectangular player markers does not satisfy the intended visual direction. Each phase must be demonstrated in an actual running Android build before being marked complete. Do not claim FIFA-level realism, successful device testing, or completed gameplay features without corresponding evidence.

## Status

- Confirmed: 3D football game direction inspired by FIFA.
- Confirmed: Java + libGDX remains the engine and implementation stack.
- Confirmed: Android 9 / ARM64 and offline-first remain constraints.
- Pending: detailed visual reference selection, asset strategy, animation pipeline, performance budget and device validation.
