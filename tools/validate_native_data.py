#!/usr/bin/env python3
"""Validate canonical data required by the native Android runtime."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
manifest_path = ROOT / "data/native_runtime_manifest.json"

def load_json(path: Path):
    with path.open("r", encoding="utf-8") as stream:
        return json.load(stream)

def main():
    manifest = load_json(manifest_path)
    sources = manifest.get("canonical_sources")
    if not isinstance(sources, dict):
        raise SystemExit("native_runtime_manifest.json: canonical_sources missing")

    checked = 0
    for key, relative in sources.items():
        path = ROOT / "data" / relative
        if not path.is_file():
            raise SystemExit(f"Missing canonical source ({key}): {path.relative_to(ROOT)}")
        load_json(path)
        checked += 1

    total_teams = 0
    total_players = 0
    all_team_ids = set()
    for key in ("cuba_first_division", "cuba_second_division"):
        relative = sources[key]
        payload = load_json(ROOT / "data" / relative)
        rosters = payload.get("rosters")
        if not isinstance(rosters, list) or not rosters:
            raise SystemExit(f"{relative}: empty or invalid rosters")
        for team in rosters:
            team_id = str(team.get("club_id", "")).strip()
            players = team.get("players")
            if not team_id or team_id in all_team_ids:
                raise SystemExit(f"{relative}: missing or duplicate club_id {team_id!r}")
            all_team_ids.add(team_id)
            if not isinstance(players, list) or len(players) != 22:
                raise SystemExit(f"{relative}: {team_id} must contain exactly 22 players")
            ids = [str(p.get("id", "")).strip() for p in players]
            if any(not player_id for player_id in ids) or len(set(ids)) != 22:
                raise SystemExit(f"{relative}: {team_id} has missing or duplicate player IDs")
            total_players += len(players)
            total_teams += 1

    if manifest.get("engine", {}).get("external_game_engines") != []:
        raise SystemExit("Native runtime must not declare external game engines")
    if manifest.get("visual_policy", {}).get("exclude_ultimate_team") is not True:
        raise SystemExit("Ultimate Team exclusion must remain enabled")

    print(f"PASS: {checked} canonical JSON sources parse.")
    print(f"PASS: {total_teams} Cuban rosters and {total_players} players have valid IDs.")
    print("PASS: native-only runtime policy and product exclusions are intact.")

if __name__ == "__main__":
    main()
