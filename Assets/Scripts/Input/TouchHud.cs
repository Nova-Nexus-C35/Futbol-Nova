using UnityEngine;

namespace FutbolNova.Input
{
    public sealed class TouchHud : MonoBehaviour
    {
        bool sprint;
        void OnGUI()
        {
            float w = Screen.width;
            float h = Screen.height;
            float size = Mathf.Clamp(w * 0.12f, 64f, 110f);
            Rect up = new Rect(30f,h-size*2.1f,size,size);
            Rect down = new Rect(30f,h-size*0.95f,size,size);
            Rect left = new Rect(30f+size*0.0f,h-size*1.5f,size,size);
            Rect right = new Rect(30f+size*1.1f,h-size*1.5f,size,size);
            GUI.Box(new Rect(20f,h-size*2.3f,size*2.25f,size*2.3f),"MOVIMIENTO");
            if (GUI.RepeatButton(up,"▲")) MatchInput.SetTouchMove(new Vector2(0,1));
            else if (GUI.RepeatButton(down,"▼")) MatchInput.SetTouchMove(new Vector2(0,-1));
            else if (GUI.RepeatButton(left,"◀")) MatchInput.SetTouchMove(new Vector2(-1,0));
            else if (GUI.RepeatButton(right,"▶")) MatchInput.SetTouchMove(new Vector2(1,0));
            else MatchInput.SetTouchMove(Vector2.zero);

            Rect shoot = new Rect(w-size*1.2f,h-size*1.2f,size,size);
            Rect pass = new Rect(w-size*2.35f,h-size*0.85f,size,size);
            Rect sprintRect = new Rect(w-size*2.35f,h-size*2.0f,size,size*0.7f);
            if (GUI.Button(shoot,"TIRO")) MatchInput.PressShoot();
            if (GUI.Button(pass,"PASE")) MatchInput.PressPass();
            sprint = GUI.Toggle(sprintRect,sprint,"SPRINT");
            MatchInput.SetTouchSprint(sprint);
        }
    }
}
