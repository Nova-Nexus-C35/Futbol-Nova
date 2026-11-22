using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using FutbolNova.Core;
using FutbolNova.Input;

namespace FutbolNova.Gameplay
{
    public sealed class MatchManager : MonoBehaviour
    {
        public MatchState State { get; } = new MatchState();
        public BallController Ball { get; private set; }
        public PlayerAgent UserPlayer { get; private set; }
        readonly List<PlayerAgent> home = new List<PlayerAgent>();
        readonly List<PlayerAgent> away = new List<PlayerAgent>();
        bool goalLock;
        Camera matchCamera;
        MatchSceneFactory sceneFactory;

        public void BuildMatch()
        {
            sceneFactory = new MatchSceneFactory(this, transform);
            sceneFactory.BuildPitch();
            Ball = sceneFactory.CreateBall();
            home.AddRange(sceneFactory.BuildTeam(TeamSide.Home, new Color(0.05f, 0.25f, 0.9f)));
            away.AddRange(sceneFactory.BuildTeam(TeamSide.Away, new Color(0.9f, 0.12f, 0.08f)));
            UserPlayer = home[6];
            foreach (var p in home) p.Ball = Ball.transform;
            foreach (var p in away) p.Ball = Ball.transform;
            AssignChasers();
            matchCamera = sceneFactory.BuildCamera();
            sceneFactory.CreateTouchHUD();
            ResetKickoff();
        }

        void Update()
        {
            if (Ball == null) return;
            MatchInput.ReadFrame();
            State.Tick(Time.deltaTime);
            UserPlayer.SetMoveInput(MatchInput.Move, MatchInput.Sprint);
            if (MatchInput.PassPressed) Pass();
            if (MatchInput.ShootPressed) Shoot();
            UpdateCamera();
        }









        void AssignChasers()
        {
            AssignNearest(home);
            AssignNearest(away);
        }

        void AssignNearest(List<PlayerAgent> players)
        {
            PlayerAgent nearest = null;
            float best = float.MaxValue;
            foreach (var p in players)
            {
                if (p.IsUserControlled) continue;
                float d = (p.transform.position - Ball.transform.position).sqrMagnitude;
                if (d < best) { best = d; nearest = p; }
            }
            foreach (var p in players) p.Target = p == nearest ? nearest : null;
        }

        void Pass()
        {
            PlayerAgent receiver = null;
            float best = float.MaxValue;
            foreach (var p in home)
            {
                if (p == UserPlayer) continue;
                float d = (p.transform.position - UserPlayer.transform.position).sqrMagnitude;
                if (d < best) { best = d; receiver = p; }
            }
            if (receiver == null) return;
            Vector3 direction = receiver.transform.position - Ball.transform.position;
            Ball.Kick(direction, 7.5f, 1.2f);
        }

        void Shoot()
        {
            Vector3 target = new Vector3(0f,1f,UserPlayer.transform.position.z < 0f ? 53f : -53f);
            Ball.Kick(target - Ball.transform.position, 17f, 3.2f);
        }

        public void RegisterGoal(TeamSide side)
        {
            if (goalLock) return;
            goalLock = true;
            State.AddGoal(side);
            StartCoroutine(KickoffAfterDelay());
        }

        IEnumerator KickoffAfterDelay()
        {
            yield return new WaitForSeconds(2f);
            ResetKickoff();
            goalLock = false;
        }

        void ResetKickoff()
        {
            Ball.ResetBall(new Vector3(0f,0.5f,0f));
            ResetTeam(home);
            ResetTeam(away);
            AssignChasers();
        }

        void ResetTeam(List<PlayerAgent> team)
        {
            for (int i=0;i<team.Count;i++)
            {
                Vector3 anchor = team[i].FormationAnchor.position;
                team[i].TeleportTo(anchor);
            }
        }



        void UpdateCamera()
        {
            if (matchCamera == null || UserPlayer == null) return;
            Vector3 target = UserPlayer.transform.position + new Vector3(0f,24f,-25f);
            matchCamera.transform.position = Vector3.Lerp(matchCamera.transform.position,target,4f*Time.deltaTime);
            matchCamera.transform.LookAt(UserPlayer.transform.position + Vector3.forward*8f);
        }





        void OnGUI()
        {
            GUI.color = Color.white;
            GUI.Box(new Rect(12,12,180,62), "FÚTBOL NOVA\n" + State.HomeGoals + "  -  " + State.AwayGoals + "     " + State.ClockText);
        }
    }
}
