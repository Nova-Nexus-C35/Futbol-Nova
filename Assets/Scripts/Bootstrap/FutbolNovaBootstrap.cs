using UnityEngine;
using FutbolNova.Gameplay;

namespace FutbolNova.Bootstrap
{
    public sealed class FutbolNovaBootstrap : MonoBehaviour
    {
        void Start()
        {
            Application.targetFrameRate = 60;
            Screen.orientation = ScreenOrientation.LandscapeLeft;
            QualitySettings.vSyncCount = 0;
            GameObject world = new GameObject("Futbol Nova Match World");
            MatchManager match = world.AddComponent<MatchManager>();
            match.BuildMatch();
        }
    }
}
