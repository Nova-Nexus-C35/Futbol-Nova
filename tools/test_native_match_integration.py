#!/usr/bin/env python3
"""Static integration contract for the native 11v11 friendly-match path.

This verifies that canonical match data, Java selection, JNI stadium setup,
OpenGL rendering, controls, and existing basic match phases remain connected.
It is not a substitute for an on-device gameplay test.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "data"
JAVA = ROOT / "native-android/app/src/main/java/com/nova/futbolnova"
CPP = ROOT / "native-android/app/src/main/cpp/native_engine.cpp"


def require(condition, message):
    if not condition:
        raise SystemExit(f"FAIL: {message}")


manifest = json.loads((DATA / "native_runtime_manifest.json").read_text(encoding="utf-8"))
clubs_data = json.loads((DATA / "original_clubs_2026.json").read_text(encoding="utf-8"))
rosters_data = json.loads((DATA / "players/nova_original_club_rosters_2026.json").read_text(encoding="utf-8"))
clubs = {c["runtime_club_id"]: c for c in clubs_data["clubs"]}
rosters = {r["club_id"]: r for r in rosters_data["rosters"]}
defaults = manifest["default_friendly_match"]
attribute_contract = manifest.get("attribute_contract", {})
goalkeeper_keys = ("dive", "hand", "kick", "ref", "gkspd", "gkpos")
require(tuple(attribute_contract.get("goalkeeper_keys", [])) == goalkeeper_keys,
        "runtime manifest must define the six goalkeeper attribute keys in canonical order")
require(attribute_contract.get("range") == {"minimum": 0, "maximum": 99},
        "runtime manifest must define the canonical 0..99 player attribute range")

require(defaults["home_team_id"] == "FN-NOVA-C35", "Nova C35 must be the default home club")
require(defaults["away_team_id"] == "FN-NOVA-RDA", "FC Río de Argenti must be the default away club")
require(defaults["home_team_id"] != defaults["away_team_id"], "home and away clubs must differ")
for team_id in (defaults["home_team_id"], defaults["away_team_id"]):
    require(team_id in rosters, f"{team_id} must have a loaded exhibition roster")
    players = rosters[team_id]["players"]
    ids = [p["id"] for p in players]
    require(len(players) == 22 and len(set(ids)) == 22, f"{team_id} must have 22 unique players")

home_stadium = clubs[defaults["home_team_id"]]["stadium"]
require(defaults["home_stadium_id"] == home_stadium["id"], "manifest home stadium ID must match Nova C35")
require(defaults["home_stadium_name"] == home_stadium["name"] == "Estadio del Fénix",
        "Nova C35 must automatically select Estadio del Fénix")
for key in ("seating", "accent", "roof", "secondary_accent"):
    require(home_stadium.get(key, "").startswith("#"), f"stadium palette {key} must be a hex color")

repository = (JAVA / "CanonicalDataRepository.java").read_text(encoding="utf-8")
activity = (JAVA / "MainActivity.java").read_text(encoding="utf-8")
engine = CPP.read_text(encoding="utf-8")

require("originalClubsById.put(id,club)" in repository, "original club metadata must be indexed by stable ID")
require("stadiumSeatingColor" in repository and "stadiumRoofColor" in repository,
        "team records must carry their stadium palette")
require("homeTeam.stadiumName" in activity, "pre-match UI must display the selected home stadium")

require("TeamSelectionPolicy.excludingId(" in activity,
        "pre-match UI must use the unit-tested team selection policy")
require((ROOT / "native-android/app/src/test/java/com/nova/futbolnova/TeamSelectionPolicyTest.java").is_file(),
        "JVM unit tests for distinct team selection must be present")
require("List<CanonicalDataRepository.Team> candidates" in activity,
        "team picker must build a verified candidate list")
require("TeamSelectionPolicy.excludingId(" in activity,
        "team picker must exclude the team already selected on the opposite side")
require("CanonicalDataRepository.Team selected=candidates.get(which)" in activity,
        "team picker must apply the selected filtered candidate, not an unfiltered index")

require("private boolean userPausedMatch=false;" in activity and "private boolean lifecyclePausedMatch=false;" in activity,
        "Android activity must distinguish user pause from lifecycle pause")
require("if(matchRunning){lifecyclePausedMatch=true;nativePause(true);}" in activity,
        "leaving the foreground must pause the native simulation")
require("if(matchRunning&&!userPausedMatch)nativePause(false);" in activity,
        "returning to foreground must not override a user-requested pause")

require("nativeSetStadiumTheme(homeTeam.stadiumSeatingColor" in activity,
        "selected home stadium palette must be passed to the native renderer")
require("Java_com_nova_futbolnova_MainActivity_nativeSetStadiumTheme" in engine, "native stadium theme JNI entry point is required")
for render in ("draw(standMesh", "draw(roofMesh", "draw(stadiumAccentMesh", "stadiumSecondaryColor"):
    require(render in engine, f"OpenGL renderer must use stadium component: {render}")

require("data.applyTeam(0,homeTeam)" in activity and "data.applyTeam(11,awayTeam)" in activity,
        "both verified 11-player sides must be applied to native player slots")
require('customPlayersById.get(p.optString("id",""))' in repository,
        "custom profiles must override roster data by stable player ID")
require('attribute(values,String key' in repository and 'nativeAttributeKeys.indexOf(key)' in repository,
        "Java must resolve player attributes by canonical JSON keys, not positional magic numbers")
goalkeeper_keys = ("dive", "hand", "kick", "ref", "gkspd", "gkpos")
for key in goalkeeper_keys:
    require(f'attribute(values,"{key}",25)' in repository,
            f"Java profile mapping must load goalkeeper attribute {key}")
for parameter in ("goalkeeperDiving", "goalkeeperHandling", "goalkeeperKicking",
                  "goalkeeperReflexes", "goalkeeperSpeed", "goalkeeperPositioning"):
    require(parameter in activity and parameter in engine,
            f"JNI/C++ player profile contract missing {parameter}")
for native_stat in ("playerGoalkeeperDiving", "playerGoalkeeperHandling",
                    "playerGoalkeeperKicking", "playerGoalkeeperReflexes",
                    "playerGoalkeeperSpeed", "playerGoalkeeperPositioning"):
    require(native_stat in engine, f"native goalkeeper simulation does not use {native_stat}")
require("reactionWindow" in engine and "diveReach" in engine,
        "goalkeeper reflexes and diving must affect keeper reactions")
require("clearanceMinimum" in engine and "handling" in engine,
        "goalkeeper kicking and handling must affect keeper parry/clearance")
require("std::array<V3,22> players" in engine, "native match must retain 22 player actors")
require("void updateKeeper" in engine and "takeShot" in engine,
        "basic goalkeeper and opponent decision logic must remain connected")
for control in ("joystickZone", "shootZone", "passZone", "skillZone", "pressZone"):
    require(control in engine, f"native touch mapping missing {control}")
for rule in ("crossedPositive", "crossedNegative", "matchPhase==0", "matchPhase=1", "matchPhase==2"):
    require(rule in engine, f"basic match phase/goal transition missing {rule}")
require("nativeStartSecondHalf" in activity and "showFullTimeDialog" in activity,
        "halftime and full-time UI transitions must remain connected")

print("PASS: Nova C35 vs FC Río de Argenti uses two verified 22-player rosters.")
print("PASS: Estadio del Fénix and its canonical palette are connected to the native OpenGL renderer.")
print("PASS: six goalkeeper attributes flow from canonical JSON through Java/JNI into native keeper behavior.")
print("PASS: 22-player simulation, goalkeeper/opponent AI, touch controls, goals and match phases are wired.")
print("INFO: static integration checks only; full laws of football and on-device gameplay remain pending.")
