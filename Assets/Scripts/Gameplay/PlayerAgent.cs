using UnityEngine;
using FutbolNova.Core;

namespace FutbolNova.Gameplay
{
    [RequireComponent(typeof(CharacterController))]
    public sealed class PlayerAgent : MonoBehaviour
    {
        public TeamSide Side { get; private set; }
        public int ShirtNumber { get; private set; }
        public bool IsUserControlled { get; private set; }
        public float MoveSpeed = 5.2f;
        public float SprintMultiplier = 1.55f;
        public Transform Ball { get; set; }
        public Transform FormationAnchor { get; set; }
        public PlayerAgent Target { get; set; }

        CharacterController controller;
        Vector3 inputDirection;
        bool sprinting;
        float gravity = -18f;
        float verticalVelocity;

        public void Initialize(TeamSide side, int number, bool userControlled)
        {
            Side = side;
            ShirtNumber = number;
            IsUserControlled = userControlled;
            controller = GetComponent<CharacterController>();
        }

        public void SetMoveInput(Vector2 input, bool sprint)
        {
            inputDirection = new Vector3(input.x, 0f, input.y);
            if (inputDirection.sqrMagnitude > 1f) inputDirection.Normalize();
            sprinting = sprint;
        }

        void Update()
        {
            if (controller == null) return;
            Vector3 desired = inputDirection;
            if (!IsUserControlled)
                desired = CalculateAI();

            float speed = MoveSpeed * (sprinting ? SprintMultiplier : 1f);
            Vector3 planar = desired * speed;
            if (desired.sqrMagnitude > 0.02f)
                transform.rotation = Quaternion.Slerp(transform.rotation, Quaternion.LookRotation(desired), 12f * Time.deltaTime);

            if (controller.isGrounded && verticalVelocity < 0f) verticalVelocity = -1f;
            verticalVelocity += gravity * Time.deltaTime;
            controller.Move((planar + Vector3.up * verticalVelocity) * Time.deltaTime);
        }

        Vector3 CalculateAI()
        {
            if (Ball == null || FormationAnchor == null) return Vector3.zero;
            Vector3 ballPos = Ball.position;
            Vector3 anchor = FormationAnchor.position;
            bool nearest = Target != null;
            Vector3 destination = nearest ? ballPos : Vector3.Lerp(anchor, ballPos, 0.18f);
            if (Side == TeamSide.Away) destination.x = Mathf.Clamp(destination.x, -33f, 33f);
            Vector3 delta = destination - transform.position;
            delta.y = 0f;
            if (delta.magnitude < 0.6f) return Vector3.zero;
            return delta.normalized;
        }

        public void TeleportTo(Vector3 position)
        {
            if (controller == null) controller = GetComponent<CharacterController>();
            controller.enabled = false;
            transform.position = position;
            controller.enabled = true;
        }
    }
}
