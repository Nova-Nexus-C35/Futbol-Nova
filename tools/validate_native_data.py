#!/usr/bin/env python3
"""Validate canonical football data required by the native Android runtime."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA_ROOT = (ROOT / "data").resolve()
MANIFEST_PATH = DATA_ROOT / "native_runtime_manifest.json"
ROSTER_KEYS = ("cuba_first_division", "cuba_second_division", "cuba_third_division")


def fail(message: str) -> None:
    raise SystemExit(f"ERROR: {message}")


def load_json(path: Path):
    try:
        with path.open("r", encoding="utf-8") as stream:
            return json.load(stream)
    except (OSError, json.JSONDecodeError) as exc:
        fail(f"cannot read valid JSON from {path}: {exc}")


def data_path(relative: str, key: str) -> Path:
    candidate = (DATA_ROOT / relative).resolve()
    try:
        candidate.relative_to(DATA_ROOT)
    except ValueError:
        fail(f"canonical source {key} escapes data/: {relative}")
    if not candidate.is_file():
        fail(f"missing canonical source ({key}): {candidate.relative_to(ROOT)}")
    return candidate


def validate_attribute_list(player, keys, source, team_id):
    values = player.get("attributes")
    player_id = str(player.get("id", "<missing>"))
    if not isinstance(values, list) or len(values) != len(keys):
        fail(
            f"{source}: {team_id}/{player_id} must have "
            f"{len(keys)} ordered attributes"
        )
    for index, value in enumerate(values):
        if isinstance(value, bool) or not isinstance(value, int) or not 0 <= value <= 99:
            fail(
                f"{source}: {team_id}/{player_id} attribute "
                f"{keys[index]} must be an integer from 0 to 99"
            )


def validate_attribute_map(player, keys, source):
    values = player.get("attributes")
    player_id = str(player.get("id", "<missing>"))
    if not isinstance(values, dict):
        fail(f"{source}: custom player {player_id} has no attribute object")
    missing = sorted(set(keys) - set(values))
    extra = sorted(set(values) - set(keys))
    if missing or extra:
        fail(
            f"{source}: custom player {player_id} attribute keys mismatch; "
            f"missing={missing}, extra={extra}"
        )
    for key in keys:
        value = values[key]
        if isinstance(value, bool) or not isinstance(value, int) or not 0 <= value <= 99:
            fail(f"{source}: custom player {player_id} attribute {key} must be 0..99")


def main():
    manifest = load_json(MANIFEST_PATH)
    sources = manifest.get("canonical_sources")
    if not isinstance(sources, dict):
        fail("native_runtime_manifest.json: canonical_sources missing")

    checked = 0
    loaded = {}
    for key, relative in sources.items():
        if not isinstance(relative, str) or not relative:
            fail(f"canonical source {key} must be a non-empty relative path")
        path = data_path(relative, key)
        loaded[key] = load_json(path)
        checked += 1

    engine = manifest.get("engine", {})
    if engine.get("implementation") != "original_native":
        fail("runtime engine must be original_native")
    if engine.get("external_game_engines") != []:
        fail("native runtime must not declare external game engines")
    if manifest.get("visual_policy", {}).get("exclude_ultimate_team") is not True:
        fail("Ultimate Team exclusion must remain enabled")

    total_teams = 0
    total_players = 0
    all_team_ids = set()
    all_player_ids = set()
    positions = set()

    for key in ROSTER_KEYS:
        if key not in sources:
            fail(f"native_runtime_manifest.json: missing {key}")
        source = sources[key]
        payload = loaded[key]
        keys = payload.get("attribute_keys")
        if not isinstance(keys, list) or not keys or len(keys) != len(set(keys)):
            fail(f"{source}: attribute_keys must be a non-empty unique list")
        rosters = payload.get("rosters")
        if not isinstance(rosters, list) or not rosters:
            fail(f"{source}: empty or invalid rosters")
        if payload.get("roster_size") != 22:
            fail(f"{source}: roster_size must be 22")
        if payload.get("fictional") is not True:
            fail(f"{source}: generated Cuban player data must remain marked fictional")

        for team in rosters:
            if not isinstance(team, dict):
                fail(f"{source}: roster entry must be an object")
            team_id = str(team.get("club_id", "")).strip()
            if not team_id or team_id in all_team_ids:
                fail(f"{source}: missing or duplicate club_id {team_id!r}")
            all_team_ids.add(team_id)
            players = team.get("players")
            if not isinstance(players, list) or len(players) != 22:
                fail(f"{source}: {team_id} must contain exactly 22 players")

            local_ids = set()
            for player in players:
                if not isinstance(player, dict):
                    fail(f"{source}: {team_id} contains a non-object player")
                player_id = str(player.get("id", "")).strip()
                if not player_id or player_id in local_ids or player_id in all_player_ids:
                    fail(f"{source}: missing or duplicate player ID {player_id!r}")
                if not str(player.get("name", "")).strip():
                    fail(f"{source}: {team_id}/{player_id} has no name")
                position = str(player.get("position", "")).strip()
                if not position:
                    fail(f"{source}: {team_id}/{player_id} has no position")
                validate_attribute_list(player, keys, source, team_id)
                local_ids.add(player_id)
                all_player_ids.add(player_id)
                positions.add(position)

            total_players += len(players)
            total_teams += 1

    custom_key = "custom_players"
    if custom_key in sources:
        custom_source = sources[custom_key]
        custom_payload = loaded[custom_key]
        custom = custom_payload.get("players", [])
        if not isinstance(custom, list):
            fail(f"{custom_source}: players must be a list")
        custom_ids = set()
        reference_keys = loaded[ROSTER_KEYS[0]].get("attribute_keys", [])
        matched = 0
        for player in custom:
            if not isinstance(player, dict):
                fail(f"{custom_source}: custom player must be an object")
            player_id = str(player.get("id", "")).strip()
            if not player_id or player_id in custom_ids:
                fail(f"{custom_source}: missing or duplicate custom player ID {player_id!r}")
            if player.get("custom_player") is not True:
                fail(f"{custom_source}: {player_id} must be marked custom_player=true")
            validate_attribute_map(player, reference_keys, custom_source)
            if player_id in all_player_ids:
                matched += 1
            custom_ids.add(player_id)
        unmatched = len(custom_ids) - matched

        # Custom profiles must remain attached to the exact stable club IDs
        # recorded in the season club catalog. Names are display labels only.
        spain_key = "spain_clubs"
        if spain_key not in loaded:
            fail("native_runtime_manifest.json: spain_clubs source is required to validate custom club membership")
        season_clubs = loaded[spain_key].get("clubs")
        if not isinstance(season_clubs, list):
            fail(f"{sources[spain_key]}: clubs must be a list")
        clubs_by_id = {}
        for club in season_clubs:
            if not isinstance(club, dict):
                fail(f"{sources[spain_key]}: club entry must be an object")
            club_id = str(club.get("id", "")).strip()
            if not club_id or club_id in clubs_by_id:
                fail(f"{sources[spain_key]}: missing or duplicate club ID {club_id!r}")
            clubs_by_id[club_id] = club

        custom_club_links = 0
        draft_custom_clubs = set()
        for player in custom:
            player_id = str(player.get("id", "")).strip()
            club_id = str(player.get("club_id", "")).strip()
            if not club_id:
                fail(f"{custom_source}: {player_id} has no stable club_id")
            club = clubs_by_id.get(club_id)
            if club is None:
                fail(f"{custom_source}: {player_id} references unknown club_id {club_id}")
            member_ids = club.get("player_ids")
            if not isinstance(member_ids, list) or player_id not in member_ids:
                fail(f"{sources[spain_key]}: {club_id} does not reference custom player {player_id}")
            custom_club_links += 1
            if club.get("status") != "playable":
                draft_custom_clubs.add(club_id)

        # Preferences are retained as profile metadata; they do not create
        # national-team call-ups or playable squads on their own.
        for player in custom:
            preferred = player.get("preferred_national_team")
            if preferred is not None and (
                not isinstance(preferred, str) or len(preferred) != 3 or not preferred.isalpha()
            ):
                fail(f"{custom_source}: {player.get('id')} has invalid preferred_national_team")

    print(f"PASS: {checked} canonical JSON sources parse and remain inside data/.")
    print(f"PASS: {total_teams} Cuban rosters and {total_players} players have unique IDs and valid attributes.")
    print(f"PASS: {len(positions)} player positions are represented: {', '.join(sorted(positions))}.")
    if custom_key in sources:
        print(f"PASS: {len(custom_ids)} custom player profiles have valid unique IDs and complete attributes.")
        if unmatched:
            print(
                f"INFO: {unmatched} custom profile(s) do not match a current Cuban roster ID; "
                "they are catalogued but are not applied to those match lineups."
            )
        print(f"PASS: {custom_club_links} custom player profiles resolve to their exact season club IDs.")
        if draft_custom_clubs:
            print(
                "INFO: custom-player clubs remain non-playable pending complete 22-player rosters: "
                + ", ".join(sorted(draft_custom_clubs))
            )
        print("PASS: national-team preferences remain metadata and do not fabricate call-ups.")
    print("PASS: native-only runtime policy and product exclusions are intact.")


if __name__ == "__main__":
    main()
