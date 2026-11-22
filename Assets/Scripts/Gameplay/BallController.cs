using UnityEngine;

namespace FutbolNova.Gameplay
{
    [RequireComponent(typeof(Rigidbody))]
    public sealed class BallController : MonoBehaviour
    {
        Rigidbody body;
        public Rigidbody Body => body;

        void Awake()
        {
            body = GetComponent<Rigidbody>();
            body.mass = 0.43f;
            body.drag = 0.12f;
            body.angularDrag = 0.08f;
            body.interpolation = RigidbodyInterpolation.Interpolate;
            body.collisionDetectionMode = CollisionDetectionMode.ContinuousDynamic;
        }

        public void Kick(Vector3 direction, float force, float lift)
        {
            direction.y = 0f;
            if (direction.sqrMagnitude < 0.001f) direction = transform.forward;
            Vector3 impulse = direction.normalized * force + Vector3.up * lift;
            body.AddForce(impulse, ForceMode.Impulse);
        }

        public void ResetBall(Vector3 position)
        {
            body.velocity = Vector3.zero;
            body.angularVelocity = Vector3.zero;
            body.position = position;
        }
    }
}
