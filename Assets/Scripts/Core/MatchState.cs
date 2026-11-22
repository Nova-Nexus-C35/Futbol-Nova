using System;

namespace FutbolNova.Core
{
    public enum TeamSide { Home, Away }

    [Serializable]
    public sealed class MatchState
    {
        public int HomeGoals { get; private set; }
        public int AwayGoals { get; private set; }
        public float ElapsedSeconds { get; private set; }
        public bool IsPaused { get; private set; }
        public bool IsFinished { get; private set; }

        public void Tick(float deltaTime)
        {
            if (!IsPaused && !IsFinished && deltaTime > 0f)
                ElapsedSeconds += deltaTime;
        }

        public void SetPaused(bool paused) => IsPaused = paused;

        public void AddGoal(TeamSide side)
        {
            if (IsFinished) return;
            if (side == TeamSide.Home) HomeGoals++;
            else AwayGoals++;
        }

        public void Finish() => IsFinished = true;

        public string ClockText
        {
            get
            {
                int total = (int)ElapsedSeconds;
                return $"{total / 60:00}:{total % 60:00}";
            }
        }
    }
}
