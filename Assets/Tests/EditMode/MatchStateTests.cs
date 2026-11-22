using NUnit.Framework;
using FutbolNova.Core;
using FutbolNova.Gameplay;

namespace FutbolNova.Tests
{
    public sealed class MatchStateTests
    {
        [Test]
        public void Tick_AdvancesClockAndFormatsMinutes()
        {
            var state = new MatchState();

            state.Tick(65.9f);

            Assert.That(state.ElapsedSeconds, Is.EqualTo(65.9f).Within(0.001f));
            Assert.That(state.ClockText, Is.EqualTo("01:05"));
        }

        [Test]
        public void Tick_DoesNotAdvanceWhenPausedOrFinished()
        {
            var state = new MatchState();
            state.SetPaused(true);
            state.Tick(10f);
            Assert.That(state.ElapsedSeconds, Is.Zero);

            state.SetPaused(false);
            state.Finish();
            state.Tick(10f);
            Assert.That(state.ElapsedSeconds, Is.Zero);
        }

        [Test]
        public void Tick_IgnoresNegativeAndNonFiniteValues()
        {
            var state = new MatchState();

            state.Tick(-1f);
            state.Tick(float.NaN);
            state.Tick(float.PositiveInfinity);

            Assert.That(state.ElapsedSeconds, Is.Zero);
        }

        [Test]
        public void AddGoal_UpdatesOnlyTheScoringTeam()
        {
            var state = new MatchState();

            state.AddGoal(TeamSide.Home);
            state.AddGoal(TeamSide.Away);
            state.AddGoal(TeamSide.Home);

            Assert.That(state.HomeGoals, Is.EqualTo(2));
            Assert.That(state.AwayGoals, Is.EqualTo(1));
        }

        [Test]
        public void Finish_PreventsFurtherGoalsAndPauseChanges()
        {
            var state = new MatchState();
            state.Finish();

            state.AddGoal(TeamSide.Home);
            state.SetPaused(true);

            Assert.That(state.HomeGoals, Is.Zero);
            Assert.That(state.IsFinished, Is.True);
            Assert.That(state.IsPaused, Is.False);
        }
        [Test]
        public void GoalEnd_AssignsGoalsToTheTeamAttackingThatEnd()
        {
            Assert.That(MatchSceneFactory.GetScoringSideForGoalEnd(-1), Is.EqualTo(TeamSide.Home));
            Assert.That(MatchSceneFactory.GetScoringSideForGoalEnd(1), Is.EqualTo(TeamSide.Away));
        }

    }
}
