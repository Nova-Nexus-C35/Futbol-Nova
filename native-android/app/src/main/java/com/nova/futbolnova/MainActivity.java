package com.nova.futbolnova;

import android.app.Activity;
import android.app.AlertDialog;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.MotionEvent;
import android.widget.*;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends Activity {
    static { System.loadLibrary("futbolnova"); }
    private GameSurface game;
    private FrameLayout root;
    private LinearLayout content, navigation;
    private CanonicalDataRepository data;
    private CanonicalDataRepository.Team homeTeam, awayTeam;
    private String currentScreen="INICIO";
    private boolean matchRunning=false;
    private int matchMinutes=3;
    private final Handler matchUiHandler=new Handler();
    private TextView scoreboard;
    private Button skipGoalButton;
    private Runnable scoreRefresh;

    private static native void nativeTouch(float x,float y,int action,int pointerId,int width,int height);
    private static native void nativePause(boolean paused);
    private static native void nativeSwitchPlayer();
    private static native int nativeGetHomeScore();
    private static native int nativeGetAwayScore();
    private static native float nativeGetMatchSeconds();
    private static native int nativeGetGoalSequencePhase();
    private static native void nativeSkipGoalSequence();
    private static native void nativeSetMatchDurationMinutes(int minutes);
    private static native void nativeStartSecondHalf();
    private static native int nativeGetMatchPhase();
    static native void nativeSetPlayerProfile(int slot,float heightCm,int overall,float pace,float dribbling,float defending);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        hideSystemBars();
        data=new CanonicalDataRepository(this);
        org.json.JSONObject defaults=data.runtimeManifest==null?null:data.runtimeManifest.optJSONObject("default_friendly_match");
        if(defaults!=null){
            homeTeam=data.findTeamById(defaults.optString("home_team_id",""));
            awayTeam=data.findTeamById(defaults.optString("away_team_id",""));
            if(homeTeam==null||awayTeam==null){
                data.warnings.add("El amistoso predeterminado guardado es "+defaults.optString("home_team_name","equipo local")+
                    " vs "+defaults.optString("away_team_name","equipo visitante")+
                    ", pero sus plantillas aún no están habilitadas en el runtime nativo. No se sustituirán por clubes aleatorios.");
            }
        }
        buildShell();
        showScreen("INICIO");
    }
    private void hideSystemBars(){getWindow().getDecorView().setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);}
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int color,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(14));if(stroke!=0)d.setStroke(dp(1.5f),stroke);return d;}
    private TextView text(String value,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private Button button(String value,Runnable action){Button b=new Button(this);b.setText(value);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setBackground(bg(0xFF183B38,0xFFB8E85A));b.setOnClickListener(v->action.run());return b;}
    private void buildShell(){
        root=new FrameLayout(this);root.setBackgroundColor(0xFF07131C);
        LinearLayout shell=new LinearLayout(this);shell.setOrientation(LinearLayout.VERTICAL);
        TextView brand=text("FÚTBOL NOVA",24,0xFFF3F7F8,true);brand.setPadding(dp(20),dp(12),dp(20),dp(8));shell.addView(brand);
        navigation=new LinearLayout(this);navigation.setOrientation(LinearLayout.HORIZONTAL);navigation.setPadding(dp(8),0,dp(8),dp(8));
        for(String tab:new String[]{"INICIO","ONLINE","MODOS","CONFIGURACIÓN"}){
            Button b=new Button(this);b.setText(tab);b.setTextSize(10);b.setTextColor(0xFFE8F2F2);b.setAllCaps(false);b.setPadding(0,0,0,0);b.setBackground(bg(0xFF10252D,0xFF335A55));
            b.setOnClickListener(v->showScreen(tab));navigation.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));
        }
        shell.addView(navigation);
        ScrollView scroll=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(18),dp(8),dp(18),dp(24));scroll.addView(content);
        shell.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(shell);setContentView(root);
    }
    private void showScreen(String screen){
        currentScreen=screen;content.removeAllViews();
        switch(screen){
            case "INICIO": homeScreen();break;
            case "ONLINE": onlineScreen();break;
            case "MODOS": modesScreen();break;
            default: settingsScreen();break;
        }
    }
    private void heading(String title,String subtitle){
        TextView h=text(title,25,0xFFF2F6F7,true);content.addView(h);
        TextView s=text(subtitle,13,0xFF9EB3B9,false);s.setPadding(0,dp(4),0,dp(14));content.addView(s);
    }
    private LinearLayout card(){
        LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(14),dp(16),dp(14));c.setBackground(bg(0xFF10242B,0xFF45645A));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);content.addView(c,lp);return c;
    }
    private void homeScreen(){
        heading("INICIO","Fútbol masculino · juego local · funcionamiento sin conexión");
        LinearLayout match=card();
        match.addView(text("PARTIDO AMISTOSO",20,0xFFB8E85A,true));
        match.addView(text("Partido local 11 contra 11. Selecciona los equipos, prepara el encuentro y entra al estadio 3D.",14,0xFFE4ECEE,false));
        org.json.JSONObject defaults=data.runtimeManifest==null?null:data.runtimeManifest.optJSONObject("default_friendly_match");
        String homeLabel=homeTeam==null?(defaults==null?"Equipo local pendiente":defaults.optString("home_team_name","Equipo local pendiente")):homeTeam.name;
        String awayLabel=awayTeam==null?(defaults==null?"Equipo visitante pendiente":defaults.optString("away_team_name","Equipo visitante pendiente")):awayTeam.name;
        TextView teams=text(homeLabel+"   VS   "+awayLabel,18,Color.WHITE,true);teams.setPadding(0,dp(14),0,dp(10));match.addView(teams);
        if(homeTeam==null||awayTeam==null)match.addView(text("La pareja predeterminada está guardada, pero sus plantillas completas todavía no están habilitadas. Puedes elegir equipos entre las plantillas verificadas disponibles.",12,0xFFFFD18A,false));
        match.addView(button("ELEGIR EQUIPO LOCAL",()->chooseTeam(true)));
        match.addView(button("ELEGIR EQUIPO VISITANTE",()->chooseTeam(false)));
        match.addView(button("DURACIÓN: "+matchMinutes+" MIN POR TIEMPO",()->chooseMatchDuration()));
        Button play=button("JUGAR AMISTOSO 3D",()->startMatch());play.setEnabled(homeTeam!=null&&awayTeam!=null&&homeTeam.isPlayable()&&awayTeam.isPlayable());
        match.addView(play);
        match.addView(text(data.playableTeams.size()+" plantillas completas verificadas para el motor nativo · 22 jugadores por equipo.",12,0xFFB6C7CB,false));
        LinearLayout info=card();info.addView(text("BASE DE DATOS OFFLINE",17,0xFFB8E85A,true));
        info.addView(text(data.baseClubCount+" clubes catalogados · "+data.generatedClubCount+" clubes de temporada en el lote generado · "+data.customPlayerCount+" futbolistas personalizados · "+data.baseLeagueCount+" ligas base configuradas.",13,0xFFE4ECEE,false));
        info.addView(text(data.firstDivisionTeams+" plantillas de primera + "+data.secondDivisionTeams+" de segunda + "+data.thirdDivisionTeams+" de tercera cargadas · "+data.skillCount+" habilidades registradas · "+data.techniqueCount+" técnicas · "+data.officialBallCount+" balones.",12,0xFFB6C7CB,false));
        info.addView(text("Solo se habilitan para jugar las plantillas verificadas con 22 futbolistas e identificadores únicos.",12,0xFFFFD18A,false));
        for(String warning:data.warnings)info.addView(text("Aviso: "+warning,11,0xFFFFB0A0,false));
        info.addView(text("Versión de datos: "+data.dataVersion,11,0xFF9EB3B9,false));
        LinearLayout originals=card();originals.addView(text("CLUBES ORIGINALES",17,0xFFB8E85A,true));
        for(org.json.JSONObject club:data.originalClubs){
            org.json.JSONObject roster=club.optJSONObject("roster");
            org.json.JSONArray ids=roster==null?null:roster.optJSONArray("known_player_ids");
            int count=ids==null?0:ids.length();
            boolean complete=roster!=null&&"complete_22_verified_game_profiles".equals(roster.optString("status"))&&count==22;
            org.json.JSONObject stadium=club.optJSONObject("stadium");
            String venue=stadium==null?"Estadio pendiente":stadium.optString("name","Estadio pendiente");
            originals.addView(text(club.optString("name","Club")+" · "+venue,14,Color.WHITE,true));
            originals.addView(text(complete?"22/22 · Plantilla disponible para amistosos":"Catálogo · "+count+"/22 jugadores registrados; no seleccionable todavía",12,complete?0xFFB8E85A:0xFFFFD18A,false));
        }
        LinearLayout custom=card();custom.addView(text("JUGADORES PERSONALIZADOS",17,0xFFB8E85A,true));
        for(org.json.JSONObject p:data.customPlayers){
            custom.addView(text(p.optString("name","Jugador")+" · "+p.optString("position","")+" · GRL "+p.optInt("overall",0)+" / POT "+p.optInt("potential",0),14,Color.WHITE,true));
            custom.addView(text(p.optString("id","")+" · "+p.optString("club_id","Club pendiente"),11,0xFFB6C7CB,false));
            if(p.has("original_club_name"))custom.addView(text("Club original: "+p.optString("original_club_name",""),11,0xFF9EB3B9,false));
            String nationalTeam=p.optString("national_team_name",p.optString("preferred_national_team","Selección pendiente"));
            custom.addView(text("Selección: "+nationalTeam,12,0xFFB8E85A,true));
        }
        LinearLayout help=card();help.addView(text("IDENTIDAD NOVA",17,0xFFB8E85A,true));
        help.addView(text("Interfaz original inspirada en la claridad de FIFA 23 y la presentación y simulación moderna de FC 26. Sin recursos copiados ni motores externos.",13,0xFFE4ECEE,false));
    }
    private void chooseMatchDuration(){
        String[] choices={"3","5","6","8","10","15","20"};
        new AlertDialog.Builder(this).setTitle("Minutos por tiempo").setItems(choices,(d,which)->{
            matchMinutes=Integer.parseInt(choices[which]);showScreen("INICIO");
        }).setNegativeButton("Cancelar",null).show();
    }
    private void chooseTeam(boolean home){
        if(data.playableTeams.isEmpty()){toast("No hay plantillas verificadas disponibles.");return;}
        String[] names=new String[data.playableTeams.size()];for(int i=0;i<names.length;i++){CanonicalDataRepository.Team t=data.playableTeams.get(i);names[i]=t.name+" · "+t.division;}
        new AlertDialog.Builder(this).setTitle(home?"Equipo local":"Equipo visitante").setItems(names,(d,which)->{
            if(home)homeTeam=data.playableTeams.get(which);else awayTeam=data.playableTeams.get(which);
            if(homeTeam==awayTeam&&data.playableTeams.size()>1){toast("Elige dos equipos diferentes.");if(home)homeTeam=data.playableTeams.get(0);else awayTeam=data.playableTeams.get(1);}
            showScreen("INICIO");
        }).setNegativeButton("Cancelar",null).show();
    }
    private void onlineScreen(){
        heading("ONLINE","Conexiones y modos multijugador");
        LinearLayout c=card();c.addView(text("MÓDULO MULTIJUGADOR",19,0xFFB8E85A,true));
        c.addView(text("La modalidad online todavía no está instalada. No se simula una conexión activa.",14,0xFFE4ECEE,false));
        c.addView(text("Internet y conexión local por Wi‑Fi se gestionarán como opciones independientes cuando el módulo esté construido y validado.",13,0xFFB6C7CB,false));
    }
    private void modesScreen(){
        heading("MODOS","Catálogo conectado al manifiesto oficial de módulos");
        LinearLayout base=card();base.addView(text("DISPONIBLE EN EL JUEGO BASE",17,0xFFB8E85A,true));
        base.addView(text("Partido amistoso local 11v11 · Ayuda y controles.",14,0xFFE4ECEE,false));
        for(CanonicalDataRepository.Module m:data.modules){
            LinearLayout c=card();c.addView(text(m.name,17,Color.WHITE,true));
            c.addView(text(m.category+" · "+statusLabel(m.status),12,0xFFB8E85A,true));
            if(!m.description.isEmpty())c.addView(text(m.description,13,0xFFCFDADD,false));
            if(m.downloadable)c.addView(text("Paquete marcado como descargable por el catálogo; requiere verificación de integridad e instalación antes de activarse.",11,0xFFB6C7CB,false));
        }
    }
    private String statusLabel(String s){switch(s){case "installed":return "Instalado";case "available":return "Disponible para descargar";case "ready":return "Listo para integrar";case "testing":return "En pruebas";case "in_development":return "En desarrollo";default:return "Planificado";}}
    private void settingsScreen(){
        heading("CONFIGURACIÓN","Preferencias del motor y del partido");
        String[] groups={"Edición","Módulos","Música","Juego","Datos","Controles","Audio","Gráficos","Accesibilidad","Administración","Información"};
        for(String name:groups){LinearLayout c=card();c.addView(text(name.toUpperCase(),15,0xFFB8E85A,true));
            if(name.equals("Juego"))c.addView(text("Perfiles de partido: Auténtico y Competitivo. Duración amistoso: 3, 5, 6, 8, 10, 15 o 20 minutos por tiempo.",13,Color.WHITE,false));
            else if(name.equals("Gráficos"))c.addView(text("Renderizado nativo OpenGL ES 3.0. Ajustes de cámara y calidad avanzada se habilitarán a medida que se integren.",13,Color.WHITE,false));
            else if(name.equals("Datos"))c.addView(text("Catálogos JSON originales cargados desde el paquete offline. IDs estables y estados de verificación preservados.",13,Color.WHITE,false));
            else c.addView(text("Sección registrada; controles específicos pendientes de integración.",13,0xFFB6C7CB,false));
        }
    }
    private void startMatch(){
        if(homeTeam==null||awayTeam==null||homeTeam.id.equals(awayTeam.id)){toast("Selecciona dos equipos diferentes con 22 jugadores únicos.");return;}
        if(game!=null)game.onPause();
        matchRunning=true;nativeSetMatchDurationMinutes(matchMinutes);nativePause(false);
        root.removeAllViews();
        FrameLayout gameRoot=new FrameLayout(this);game=new GameSurface();
        gameRoot.addView(game);
        scoreboard=text("",14,Color.WHITE,true);
        scoreboard.setGravity(Gravity.CENTER);scoreboard.setBackgroundColor(0x9907131C);FrameLayout.LayoutParams top=new FrameLayout.LayoutParams(-1,dp(42),Gravity.TOP);gameRoot.addView(scoreboard,top);
        skipGoalButton=button("SALTAR",()->nativeSkipGoalSequence());
        skipGoalButton.setVisibility(View.GONE);
        FrameLayout.LayoutParams skipLp=new FrameLayout.LayoutParams(dp(132),dp(46),Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        skipLp.topMargin=dp(50);gameRoot.addView(skipGoalButton,skipLp);
        scoreRefresh=new Runnable(){@Override public void run(){
            if(!matchRunning||scoreboard==null)return;
            int phase=nativeGetMatchPhase();
            if(skipGoalButton!=null)skipGoalButton.setVisibility(nativeGetGoalSequencePhase()!=0?View.VISIBLE:View.GONE);
            if(phase==1){matchUiHandler.removeCallbacks(this);showHalfTimeDialog();return;}
            if(phase==3){matchUiHandler.removeCallbacks(this);showFullTimeDialog();return;}
            int seconds=(int)nativeGetMatchSeconds();
            if(phase==2)seconds+=matchMinutes*60;
            scoreboard.setText(homeTeam.name+"   "+nativeGetHomeScore()+" - "+nativeGetAwayScore()+"   "+awayTeam.name+"     "+String.format(Locale.getDefault(),"%02d:%02d",seconds/60,seconds%60));
            matchUiHandler.postDelayed(this,500);
        }};
        matchUiHandler.post(scoreRefresh);
        gameRoot.addView(new TouchHud());
        Button pause=button("PAUSA",()->{nativePause(true);new AlertDialog.Builder(this).setTitle("Partido pausado").setItems(new String[]{"Reanudar","Salir del partido"},(d,w)->{if(w==0)nativePause(false);else returnToMenu();}).setOnCancelListener(d->nativePause(false)).show();});
        FrameLayout.LayoutParams pp=new FrameLayout.LayoutParams(dp(112),dp(48),Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);gameRoot.addView(pause,pp);
        root.addView(gameRoot);
    }
    private void showHalfTimeDialog(){
        new AlertDialog.Builder(this).setTitle("DESCANSO")
            .setMessage("Finalizó el primer tiempo. El marcador es "+nativeGetHomeScore()+" - "+nativeGetAwayScore()+".")
            .setCancelable(false)
            .setPositiveButton("INICIAR SEGUNDO TIEMPO",(d,w)->{
                if(game!=null)game.queueEvent(()->{
                    nativeStartSecondHalf();
                    runOnUiThread(()->{if(matchRunning&&scoreRefresh!=null)matchUiHandler.post(scoreRefresh);});
                });
            })
            .setNegativeButton("SALIR DEL PARTIDO",(d,w)->returnToMenu()).show();
    }
    private void showFullTimeDialog(){
        new AlertDialog.Builder(this).setTitle("FINAL DEL PARTIDO")
            .setMessage(homeTeam.name+"  "+nativeGetHomeScore()+" - "+nativeGetAwayScore()+"  "+awayTeam.name)
            .setCancelable(false)
            .setPositiveButton("REVANCHA",(d,w)->startMatch())
            .setNegativeButton("VOLVER AL MENÚ",(d,w)->returnToMenu()).show();
    }
    private void returnToMenu(){
        matchRunning=false;
        if(scoreRefresh!=null)matchUiHandler.removeCallbacks(scoreRefresh);
        scoreboard=null;skipGoalButton=null;nativePause(true);
        if(game!=null){game.onPause();game=null;}
        buildShell();showScreen("INICIO");
    }
    @Override public void onBackPressed(){if(matchRunning)returnToMenu();else super.onBackPressed();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

    private final class GameSurface extends GLSurfaceView {
        GameSurface(){super(MainActivity.this);setEGLContextClientVersion(3);setPreserveEGLContextOnPause(true);
            setRenderer(new Renderer(){
                @Override public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl,javax.microedition.khronos.egl.EGLConfig cfg){
                    nativeInit();data.applyTeam(0,homeTeam);data.applyTeam(11,awayTeam);
                }
                @Override public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl,int w,int h){nativeResize(w,h);}
                @Override public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl){nativeFrame();}
            });setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);}
    }
    @Override protected void onPause(){if(game!=null)game.onPause();super.onPause();}
    @Override protected void onResume(){super.onResume();if(game!=null)game.onResume();}
    private static native void nativeInit();
    private static native void nativeResize(int width,int height);
    private static native void nativeFrame();

    private final class TouchHud extends View {
        private final Paint p=new Paint(3);
        TouchHud(){super(MainActivity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){
            float w=getWidth(),h=getHeight(),s=Math.min(w,h);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(0xB8C9F45A);
            c.drawCircle(w*.17f,h*.79f,s*.105f,p);p.setStyle(Paint.Style.FILL);p.setColor(0xB8C9F45A);c.drawCircle(w*.17f,h*.79f,s*.035f,p);
            drawButton(c,w*.17f,h*.17f,s*.045f,"CAMBIAR");
            drawButton(c,w*.90f,h*.77f,s*.07f,"TIRO");drawButton(c,w*.69f,h*.60f,s*.055f,"PASE");
            drawButton(c,w*.90f,h*.58f,s*.055f,"REGATE");drawButton(c,w*.90f,h*.92f,s*.055f,"PRESIÓN");
        }
        private void drawButton(Canvas c,float x,float y,float r,String label){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(0xD8C9F45A);c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(Math.max(12,r*.34f));c.drawText(label,x,y+p.getTextSize()*.35f,p);}
        @Override public boolean onTouchEvent(MotionEvent e){
            int index=e.getActionIndex();
            int action=e.getActionMasked();
            if((action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_POINTER_DOWN)&&
               e.getX(index)<getWidth()*.30f&&e.getY(index)<getHeight()*.27f){nativeSwitchPlayer();return true;}
            if(action==MotionEvent.ACTION_MOVE){
                for(int i=0;i<e.getPointerCount();i++)
                    nativeTouch(e.getX(i),e.getY(i),action,e.getPointerId(i),getWidth(),getHeight());
            }else{
                nativeTouch(e.getX(index),e.getY(index),action,e.getPointerId(index),getWidth(),getHeight());
            }
            return true;
        }
    }
}
