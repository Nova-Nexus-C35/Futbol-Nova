using System.Collections.Generic;
using UnityEngine;
using FutbolNova.Core;
using FutbolNova.Input;

namespace FutbolNova.Gameplay
{
    // Owns construction of the procedural match scene; MatchManager remains the match coordinator.
    public sealed class MatchSceneFactory
    {
        readonly MatchManager owner;
        readonly Transform parent;

        public MatchSceneFactory(MatchManager owner, Transform parent)
        {
            this.owner = owner;
            this.parent = parent;
        }

        public void BuildPitch()
        {
            CreatePrimitive("Pitch", PrimitiveType.Cube, new Vector3(0f, -0.35f, 0f), new Vector3(68f, 0.5f, 105f), new Color(0.12f, 0.42f, 0.18f), parent);
            for (int i = -3; i <= 3; i++)
                CreatePrimitive("Pitch stripe", PrimitiveType.Cube, new Vector3(0f, -0.08f, i * 15f), new Vector3(67.7f, 0.015f, 7.5f), i % 2 == 0 ? new Color(0.14f,0.48f,0.2f) : new Color(0.11f,0.4f,0.16f), parent);
            CreatePrimitive("Halfway line", PrimitiveType.Cube, new Vector3(0f, -0.07f, 0f), new Vector3(68f, 0.02f, 0.12f), Color.white, parent);
            CreatePrimitive("Center mark", PrimitiveType.Cylinder, new Vector3(0f, -0.045f, 0f), new Vector3(9.15f, 0.02f, 9.15f), Color.white, parent);
            CreatePrimitive("Center spot", PrimitiveType.Cylinder, new Vector3(0f, -0.01f, 0f), new Vector3(0.35f, 0.025f, 0.35f), Color.white, parent);
            CreateGoal(-1);
            CreateGoal(1);
            CreatePrimitive("North penalty area", PrimitiveType.Cube, new Vector3(0f, -0.05f, 43f), new Vector3(40.3f, 0.04f, 16.5f), Color.white, parent);
            CreatePrimitive("South penalty area", PrimitiveType.Cube, new Vector3(0f, -0.05f, -43f), new Vector3(40.3f, 0.04f, 16.5f), Color.white, parent);
        }

        void CreateGoal(int end)
        {
            float z = end * 51.5f;
            float sign = end;
            Color white = Color.white;
            CreatePrimitive("Goal post", PrimitiveType.Cube, new Vector3(-3.66f,1.22f,z), new Vector3(0.15f,2.44f,0.15f),white,parent);
            CreatePrimitive("Goal post", PrimitiveType.Cube, new Vector3(3.66f,1.22f,z), new Vector3(0.15f,2.44f,0.15f),white,parent);
            CreatePrimitive("Goal crossbar", PrimitiveType.Cube, new Vector3(0f,2.44f,z), new Vector3(7.32f,0.15f,0.15f),white,parent);
            GameObject trigger = new GameObject("Goal trigger");
            trigger.transform.SetParent(parent);
            trigger.transform.position = new Vector3(0f,1.2f,z + sign * 1.2f);
            BoxCollider box = trigger.AddComponent<BoxCollider>();
            box.isTrigger = true;
            box.size = new Vector3(7.1f,2.4f,2.4f);
            GoalTrigger goal = trigger.AddComponent<GoalTrigger>();
            goal.Initialize(owner, end > 0 ? TeamSide.Home : TeamSide.Away);
        }

        public List<PlayerAgent> BuildTeam(TeamSide side, Color color)
        {
            var list = new List<PlayerAgent>();
            float direction = side == TeamSide.Home ? -1f : 1f;
            Vector3[] positions = {

                new Vector3(0f,0f,-47f), new Vector3(-25f,0f,-30f), new Vector3(-9f,0f,-32f),
                new Vector3(9f,0f,-32f), new Vector3(25f,0f,-30f), new Vector3(-22f,0f,-10f),
                new Vector3(-7f,0f,-12f), new Vector3(7f,0f,-12f), new Vector3(22f,0f,-10f),
                new Vector3(-10f,0f,12f), new Vector3(10f,0f,12f)
            };
            for (int i=0;i<11;i++)
            {
                Vector3 pos = positions[i];
                pos.z *= direction;
                GameObject player = GameObject.CreatePrimitive(PrimitiveType.Capsule);
                player.name = (side == TeamSide.Home ? "Home " : "Away ") + (i+1);
                player.transform.SetParent(parent);
                player.transform.position = pos;
                player.transform.localScale = new Vector3(0.85f,0.95f,0.85f);
                player.GetComponent<Renderer>().material.color = color;
                Collider old = player.GetComponent<Collider>();
                UnityEngine.Object.Destroy(old);
                CharacterController cc = player.AddComponent<CharacterController>();
                cc.height = 1.9f; cc.radius = 0.42f; cc.center = new Vector3(0f,0.95f,0f);
                PlayerAgent agent = player.AddComponent<PlayerAgent>();
                agent.Initialize(side,i+1,side == TeamSide.Home && i == 6);
                GameObject anchor = new GameObject("Formation anchor");
                anchor.transform.SetParent(parent);
                anchor.transform.position = pos;
                agent.FormationAnchor = anchor.transform;
                list.Add(agent);
            }
            return list;
        }

        public BallController CreateBall()
        {
            GameObject ball = GameObject.CreatePrimitive(PrimitiveType.Sphere);
            ball.name = "Match ball";
            ball.transform.SetParent(parent);
            ball.transform.localScale = Vector3.one * 0.42f;
            ball.transform.position = new Vector3(0f,0.35f,0f);
            ball.GetComponent<Renderer>().material.color = Color.white;
            Rigidbody rb = ball.AddComponent<Rigidbody>();
            rb.collisionDetectionMode = CollisionDetectionMode.ContinuousDynamic;
            return ball.AddComponent<BallController>();
        }

        public Camera BuildCamera()
        {
            GameObject cam = new GameObject("Match Camera");
            Camera matchCamera = cam.AddComponent<Camera>();
            cam.tag = "MainCamera";
            matchCamera.fieldOfView = 58f;
            matchCamera.transform.position = new Vector3(0f,28f,-35f);
            matchCamera.transform.LookAt(new Vector3(0f,0f,5f));
            return matchCamera;
        }

        public void CreateTouchHUD()
        {
            GameObject hud = new GameObject("Touch HUD");
            hud.AddComponent<TouchHud>();
        }

        GameObject CreatePrimitive(string name, PrimitiveType type, Vector3 position, Vector3 scale, Color color, Transform parent)
        {
            GameObject obj = GameObject.CreatePrimitive(type);
            obj.name = name; obj.transform.SetParent(parent); obj.transform.position = position; obj.transform.localScale = scale;
            obj.GetComponent<Renderer>().material.color = color;
            Collider collider = obj.GetComponent<Collider>();
            if (name != "Pitch" && collider != null) UnityEngine.Object.Destroy(collider);
            return obj;
        }
    }
}
