using UnityEngine;
using FutbolNova.Core;

namespace FutbolNova.Gameplay
{
    public sealed class GoalTrigger : MonoBehaviour
    {
        public TeamSide ScoringSide;
        MatchManager manager;

        public void Initialize(MatchManager matchManager, TeamSide scoringSide)
        {
            manager = matchManager;
            ScoringSide = scoringSide;
        }

        void OnTriggerEnter(Collider other)
        {
            if (other.GetComponent<BallController>() != null && manager != null)
                manager.RegisterGoal(ScoringSide);
        }
    }
}
