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
            if (IsPaused || IsFinished || deltaTime <= 0f ||
                float.IsNaN(deltaTime) || float.IsInfinity(deltaTime))
                return;

            ElapsedSeconds += deltaTime;
        }

        public void SetPaused(bool paused)
        {
            if (!IsFinished)
                IsPaused = paused;
        }

        public void AddGoal(TeamSide side)
        {
            if (IsFinished)
                return;

            if (side == TeamSide.Home)
                HomeGoals++;
            else if (side == TeamSide.Away)
                AwayGoals++;
        }

        public void Finish()
        {
            IsFinished = true;
            IsPaused = false;
        }

        public string ClockText
        {
            get
            {
                int total = (int)Math.Min(ElapsedSeconds, int.MaxValue);
                return string.Format("{0:00}:{1:00}", total / 60, total % 60);
            }
        }
    }
}
