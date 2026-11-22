using UnityEngine;

namespace FutbolNova.Input
{
    public static class MatchInput
    {
        public static Vector2 Move { get; private set; }
        public static bool Sprint { get; private set; }
        public static bool PassPressed { get; private set; }
        public static bool ShootPressed { get; private set; }

        static Vector2 touchMove;
        static bool touchSprint;
        static bool touchPass;
        static bool touchShoot;

        public static void SetTouchMove(Vector2 value) => touchMove = Vector2.ClampMagnitude(value, 1f);
        public static void SetTouchSprint(bool value) => touchSprint = value;
        public static void PressPass() => touchPass = true;
        public static void PressShoot() => touchShoot = true;

        public static void ReadFrame()
        {
            Vector2 keyboard = new Vector2(UnityEngine.Input.GetAxisRaw("Horizontal"), UnityEngine.Input.GetAxisRaw("Vertical"));
            Move = keyboard.sqrMagnitude > 0.01f ? Vector2.ClampMagnitude(keyboard, 1f) : touchMove;
            Sprint = UnityEngine.Input.GetKey(KeyCode.LeftShift) || touchSprint;
            PassPressed = UnityEngine.Input.GetKeyDown(KeyCode.J) || touchPass;
            ShootPressed = UnityEngine.Input.GetKeyDown(KeyCode.K) || touchShoot;
            touchPass = false;
            touchShoot = false;
        }
    }
}
