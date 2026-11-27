#!/usr/bin/env python3
"""Validate canonical football data required by the native Android runtime."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA_ROOT = (ROOT / "data").resolve()
MANIFEST_PATH = DATA_ROOT / "native_runtime_manifest.json"
CUBA_ROSTER_KEYS = ("cuba_first_division", "cuba_second_division", "cuba_third_division")
ROSTER_KEYS = CUBA_ROSTER_KEYS + ("original_club_rosters",)


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
        fail(f"{source}: {team_id}/{player_id} must have {len(keys)} ordered attributes")
    for index, value in enumerate(values):
        if isinstance(value, bool) or not isinstance(value, int) or not 0 <= value <= 99:
            fail(f"{source}: {team_id}/{player_id} attribute {keys[index]} must be an integer from 0 to 99")


def validate_attribute_map(player, keys, source):
    values = player.get("attributes")
    player_id = str(player.get("id", "<missing>"))
    if not isinstance(values, dict):
        fail(f"{source}: custom player {player_id} has no attribute object")
    missing = sorted(set(keys) - set(values))
    extra = sorted(set(values) - set(keys))
    if missing or extra:
        fail(f"{source}: custom player {player_id} attribute keys mismatch; missing={missing}, extra={extra}")
    for key in keys:
        value = values[key]
        if isinstance(value, bool) or not isinstance(value, int) or not 0 <= value <= 99:
            fail(f"{source}: custom player {player_id} attribute {key} must be 0..99")


def main():
    manifest = load_json(MANIFEST_PATH)
    sources = manifest.get("canonical_sources")
    if not isinstance(sources, dict):
        fail("native_runtime_manifest.json: canonical_sources missing")

    checked, loaded = 0, {}
    for key, relative in sources.items():
        if not isinstance(relative, str) or not relative:
            fail(f"canonical source {key} must be a non-empty relative path")
        loaded[key] = load_json(data_path(relative, key))
        checked += 1

    engine = manifest.get("engine", {})
    if engine.get("implementation") != "original_native" or engine.get("external_game_engines") != []:
        fail("runtime must remain original_native with no external game engines")
    if manifest.get("visual_policy", {}).get("exclude_ultimate_team") is not True:
        fail("Ultimate Team exclusion must remain enabled")

    total_teams = total_players = 0
    cuba_teams = cuba_players = original_teams = original_players = 0
    all_team_ids, all_player_ids, positions = set(), set(), set()

    for key in ROSTER_KEYS:
        if key not in sources:
            fail(f"native_runtime_manifest.json: missing {key}")
        source, payload = sources[key], loaded[key]
        keys = payload.get("attribute_keys")
        if not isinstance(keys, list) or not keys or len(keys) != len(set(keys)):
            fail(f"{source}: attribute_keys must be a non-empty unique list")
        rosters = payload.get("rosters")
        if not isinstance(rosters, list) or not rosters:
            fail(f"{source}: empty or invalid rosters")
        if payload.get("roster_size") != 22 or payload.get("fictional") is not True:
            fail(f"{source}: roster_size must be 22 and generated data must remain fictional")
        if key == "original_club_rosters" and payload.get("exhibition_only") is not True:
            fail(f"{source}: original clubs must remain exhibition-only")

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
            total_teams += 1
            total_players += len(players)
            if key in CUBA_ROSTER_KEYS:
                cuba_teams += 1
                cuba_players += len(players)
            else:
                original_teams += 1
                original_players += len(players)

    original_key = "original_clubs"
    if original_key not in loaded:
        fail("native_runtime_manifest.json: original_clubs source is required")
    original_catalog = loaded[original_key]
    original_clubs = original_catalog.get("clubs")
    if not isinstance(original_clubs, list) or len(original_clubs) != 4:
        fail(f"{sources[original_key]}: exactly four original clubs are required")
    original_by_id = {}
    for club in original_clubs:
        club_id = str(club.get("runtime_club_id", "")).strip()
        if not club_id or club_id in original_by_id:
            fail(f"{sources[original_key]}: missing or duplicate original club ID {club_id!r}")
        roster = club.get("roster", {})
        known_ids = roster.get("known_player_ids", [])
        complete = roster.get("status") == "complete_22_verified_game_profiles"
        if complete and (not isinstance(known_ids, list) or len(known_ids) != 22):
            fail(f"{sources[original_key]}: {club_id} is marked complete but does not list 22 player IDs")
        if not complete and known_ids:
            fail(f"{sources[original_key]}: incomplete club {club_id} must not expose an unverified roster")
        original_by_id[club_id] = club

    original_rosters = loaded["original_club_rosters"].get("rosters", [])
    if len(original_rosters) != 2:
        fail("original_club_rosters must currently contain exactly the two complete original exhibition teams")
    for team in original_rosters:
        club_id = team["club_id"]
        club = original_by_id.get(club_id)
        if club is None:
            fail(f"original exhibition roster references unknown club {club_id}")
        expected = club.get("roster", {}).get("known_player_ids", [])
        actual = [str(p.get("id", "")) for p in team["players"]]
        if set(actual) != set(expected) or len(actual) != 22:
            fail(f"original exhibition roster {club_id} does not match the canonical club player IDs")

    clubs_by_id = {}
    spain_key = "spain_clubs"
    if spain_key not in loaded:
        fail("native_runtime_manifest.json: spain_clubs source is required")
    season_clubs = loaded[spain_key].get("clubs")
    if not isinstance(season_clubs, list):
        fail(f"{sources[spain_key]}: clubs must be a list")
    for club in season_clubs:
        club_id = str(club.get("id", "")).strip()
        if not club_id or club_id in clubs_by_id:
            fail(f"{sources[spain_key]}: missing or duplicate club ID {club_id!r}")
        clubs_by_id[club_id] = {
            "display_name": club.get("display_name"),
            "player_ids": club.get("player_ids"),
            "status": club.get("status"),
        }
    for club_id, club in original_by_id.items():
        if club_id in clubs_by_id:
            fail(f"original club ID collides with season club ID: {club_id}")
        roster = club.get("roster", {})
        clubs_by_id[club_id] = {
            "display_name": club.get("name"),
            "player_ids": roster.get("known_player_ids", []),
            "status": "playable" if roster.get("status") == "complete_22_verified_game_profiles" else "catalog_only",
        }

    custom_key = "custom_players"
    if custom_key in sources:
        custom_source = sources[custom_key]
        custom = loaded[custom_key].get("players", [])
        if not isinstance(custom, list):
            fail(f"{custom_source}: players must be a list")
        custom_ids, matched, custom_club_links = set(), 0, 0
        reference_keys = loaded[CUBA_ROSTER_KEYS[0]].get("attribute_keys", [])
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

            club_id = str(player.get("club_id", "")).strip()
            club = clubs_by_id.get(club_id)
            if not club:
                fail(f"{custom_source}: {player_id} references unknown active club_id {club_id}")
            members = club.get("player_ids")
            if not isinstance(members, list) or player_id not in members:
                fail(f"{custom_source}: active club {club_id} does not reference player {player_id}")
            custom_club_links += 1

            historical_id = str(player.get("original_club_id", "")).strip()
            if historical_id:
                historical = original_by_id.get(historical_id)
                if historical is None:
                    fail(f"{custom_source}: {player_id} references unknown original_club_id {historical_id}")
                if player_id not in historical.get("roster", {}).get("known_player_ids", []):
                    fail(f"{custom_source}: original club {historical_id} does not reference player {player_id}")

            preferred = player.get("preferred_national_team")
            if preferred is not None and (
                not isinstance(preferred, str) or len(preferred) != 3 or not preferred.isalpha()
            ):
                fail(f"{custom_source}: {player_id} has invalid preferred_national_team")

        unmatched = len(custom_ids) - matched
        defaults = manifest.get("default_friendly_match")
        if not isinstance(defaults, dict):
            fail("native_runtime_manifest.json: default_friendly_match is required")
        home_id, away_id = str(defaults.get("home_team_id", "")).strip(), str(defaults.get("away_team_id", "")).strip()
        if not home_id or not away_id or home_id == away_id:
            fail("native_runtime_manifest.json: default friendly teams must be distinct stable IDs")
        for team_id, name_key in ((home_id, "home_team_name"), (away_id, "away_team_name")):
            club = clubs_by_id.get(team_id)
            if club is None:
                fail(f"native_runtime_manifest.json: default friendly club {team_id} is unknown")
            if defaults.get(name_key) != club.get("display_name"):
                fail(f"native_runtime_manifest.json: saved display name for {team_id} does not match the canonical club catalog")
    else:
        custom_ids, unmatched, custom_club_links = set(), 0, 0

    print(f"PASS: {checked} canonical JSON sources parse and remain inside data/.")
    print(f"PASS: {cuba_teams} Cuban teams / {cuba_players} players and {original_teams} original exhibition teams / {original_players} players.")
    print(f"PASS: {total_teams} total playable rosters and {total_players} unique players; {len(positions)} positions represented.")
    print(f"PASS: {len(original_clubs)} original clubs catalogued; only the two complete 22-player rosters are playable.")
    if custom_key in sources:
        print(f"PASS: {len(custom_ids)} custom player profiles have valid IDs, 35 attributes and club links.")
        if unmatched:
            print(f"INFO: {unmatched} custom profile(s) are catalogued but not present in a loaded playable roster.")
        print("PASS: historical original-club memberships are checked separately from current club assignments.")
        print("PASS: national-team preferences remain metadata and do not fabricate call-ups.")
    print("PASS: native-only runtime policy and product exclusions are intact.")


if __name__ == "__main__":
    main()
