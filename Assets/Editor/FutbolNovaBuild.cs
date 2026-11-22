using System.IO;
using UnityEditor;
using UnityEditor.Build;
using UnityEditor.Build.Reporting;
using UnityEditor.SceneManagement;
using UnityEngine;
using UnityEngine.SceneManagement;
using FutbolNova.Bootstrap;

namespace FutbolNova.Editor
{
    public static class FutbolNovaBuild
    {
        const string ScenePath = "Assets/Scenes/Main.unity";
        const string AndroidOutput = "Build/FutbolNova.apk";

        [MenuItem("Futbol Nova/Generate Bootstrap Scene")]
        public static void GenerateBootstrapScene()
        {
            Directory.CreateDirectory("Assets/Scenes");
            Scene scene = EditorSceneManager.NewScene(NewSceneSetup.EmptyScene, NewSceneMode.Single);
            GameObject bootstrap = new GameObject("FutbolNovaBootstrap");
            bootstrap.AddComponent<FutbolNovaBootstrap>();
            EditorSceneManager.SaveScene(scene, ScenePath);
            EditorBuildSettings.scenes = new[]
            {
                new EditorBuildSettingsScene(ScenePath, true)
            };
            Debug.Log("Fútbol Nova bootstrap scene generated at " + ScenePath);
        }

        public static void BuildAndroid()
        {
            GenerateBootstrapScene();
            Directory.CreateDirectory("Build");
            PlayerSettings.Android.targetArchitectures = AndroidArchitecture.ARM64;

            BuildPlayerOptions options = new BuildPlayerOptions
            {
                scenes = new[] { ScenePath },
                locationPathName = AndroidOutput,
                target = BuildTarget.Android,
                options = BuildOptions.None
            };

            BuildReport report = BuildPipeline.BuildPlayer(options);
            if (report.summary.result != BuildResult.Succeeded)
            {
                throw new BuildFailedException(
                    "Fútbol Nova Android build failed: " + report.summary.result +
                    ". Errors: " + report.summary.totalErrors);
            }

            Debug.Log("Fútbol Nova Android APK created: " + AndroidOutput);
        }
    }
}
