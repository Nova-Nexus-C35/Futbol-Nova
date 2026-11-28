#include <jni.h>
#include <GLES3/gl3.h>
#include <android/log.h>
#include <cmath>
#include <vector>
#include <array>
#include <algorithm>
#include <utility>
#include <time.h>
#include <mutex>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,"FutbolNova",__VA_ARGS__)
namespace {
struct V3 { float x,y,z; };
struct Vertex { float x,y,z; };
struct Mesh { GLuint vao=0,vbo=0; GLsizei count=0; GLenum mode=GL_TRIANGLES; };
std::mutex stateMutex;
GLuint program=0; GLint mvpLoc=-1,colorLoc=-1;
int screenW=1,screenH=1; bool paused=false;
float stickX=0,stickY=0; bool shoot=false,pass=false,skill=false,pressure=false; int joystickPointer=-1,pressurePointer=-1;
float ballX=0,ballZ=.72f,ballY=.22f,ballVX=0,ballVY=0,ballVZ=0,ballReleaseTimer=0.0f;
float playerX=0,playerZ=0.0f,playerYaw=3.1415926f,matchTime=0,matchSeconds=0; int homeScore=0,awayScore=0; int controlledPlayer=9; float halfDurationSeconds=180.0f; int matchPhase=0; bool homeDefendsNegative=true;
int goalSequencePhase=0; bool goalScoringHome=true; float goalSequenceTimer=0.0f;
Mesh fieldMesh{},lineMesh{},sphereMesh{},standMesh{},roofMesh{},stadiumAccentMesh{},goalMesh{};
V3 stadiumSeatColor{.055f,.085f,.12f};
V3 stadiumAccentColor{.0f,.81f,.91f};
V3 stadiumRoofColor{.025f,.04f,.08f};
V3 stadiumSecondaryColor{.75f,.79f,.84f};
std::array<V3,22> players{};
std::array<float,22> playerHeight{};
std::array<float,22> playerPace{};
std::array<float,22> playerDribbling{};
std::array<float,22> playerDefending{};
std::array<float,22> playerGoalkeeperDiving{};
std::array<float,22> playerGoalkeeperHandling{};
std::array<float,22> playerGoalkeeperKicking{};
std::array<float,22> playerGoalkeeperReflexes{};
std::array<float,22> playerGoalkeeperSpeed{};
std::array<float,22> playerGoalkeeperPositioning{};
std::array<int,22> playerOverall{};
struct KeeperState { float diveTimer=0.0f; float diveX=0.0f; };
std::array<KeeperState,2> keepers{};

const char* VS="#version 300 es\nlayout(location=0) in vec3 aPos; uniform mat4 uMVP; void main(){gl_Position=uMVP*vec4(aPos,1.0);}";
const char* FS="#version 300 es\nprecision mediump float; uniform vec4 uColor; out vec4 frag; void main(){frag=uColor;}";

std::array<float,16> identity(){return {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1};}
std::array<float,16> mul(const std::array<float,16>&a,const std::array<float,16>&b){
 std::array<float,16> r{}; for(int c=0;c<4;c++)for(int row=0;row<4;row++)for(int k=0;k<4;k++)r[c*4+row]+=a[k*4+row]*b[c*4+k]; return r;
}
std::array<float,16> perspective(float f,float aspect,float n,float z){
 float q=1.0f/std::tan(f*.5f); std::array<float,16> m{};m[0]=q/aspect;m[5]=q;m[10]=(z+n)/(n-z);m[11]=-1;m[14]=(2*z*n)/(n-z);return m;
}
V3 norm(V3 v){float l=std::sqrt(v.x*v.x+v.y*v.y+v.z*v.z);if(l<.0001f)return {0,0,0};return {v.x/l,v.y/l,v.z/l};}
V3 sub(V3 a,V3 b){return {a.x-b.x,a.y-b.y,a.z-b.z};}
V3 cross(V3 a,V3 b){return {a.y*b.z-a.z*b.y,a.z*b.x-a.x*b.z,a.x*b.y-a.y*b.x};}
std::array<float,16> lookAt(V3 eye,V3 center,V3 up){
 V3 f=norm(sub(center,eye)),s=norm(cross(f,up)),u=cross(s,f);auto m=identity();
 m[0]=s.x;m[4]=s.y;m[8]=s.z;m[1]=u.x;m[5]=u.y;m[9]=u.z;m[2]=-f.x;m[6]=-f.y;m[10]=-f.z;
 m[12]=-(s.x*eye.x+s.y*eye.y+s.z*eye.z);m[13]=-(u.x*eye.x+u.y*eye.y+u.z*eye.z);m[14]=(f.x*eye.x+f.y*eye.y+f.z*eye.z);return m;
}
std::array<float,16> model(float x,float y,float z,float sx,float sy,float sz,float yaw=0){
 auto m=identity();float c=std::cos(yaw),s=std::sin(yaw);
 m[0]=c*sx;m[2]=-s*sx;m[5]=sy;m[8]=s*sz;m[10]=c*sz;m[12]=x;m[13]=y;m[14]=z;return m;
}
GLuint shader(GLenum type,const char*src){GLuint s=glCreateShader(type);glShaderSource(s,1,&src,nullptr);glCompileShader(s);GLint ok=0;glGetShaderiv(s,GL_COMPILE_STATUS,&ok);if(!ok){char log[1024];glGetShaderInfoLog(s,1024,nullptr,log);LOGE("shader: %s",log);}return s;}
void makeProgram(){GLuint v=shader(GL_VERTEX_SHADER,VS),f=shader(GL_FRAGMENT_SHADER,FS);program=glCreateProgram();glAttachShader(program,v);glAttachShader(program,f);glLinkProgram(program);glDeleteShader(v);glDeleteShader(f);mvpLoc=glGetUniformLocation(program,"uMVP");colorLoc=glGetUniformLocation(program,"uColor");}
Mesh mesh(const std::vector<Vertex>&v,GLenum mode=GL_TRIANGLES){Mesh m;m.mode=mode;m.count=(GLsizei)v.size();glGenVertexArrays(1,&m.vao);glGenBuffers(1,&m.vbo);glBindVertexArray(m.vao);glBindBuffer(GL_ARRAY_BUFFER,m.vbo);glBufferData(GL_ARRAY_BUFFER,v.size()*sizeof(Vertex),v.data(),GL_STATIC_DRAW);glEnableVertexAttribArray(0);glVertexAttribPointer(0,3,GL_FLOAT,GL_FALSE,sizeof(Vertex),(void*)0);glBindVertexArray(0);return m;}
Mesh makeSphere(){std::vector<Vertex>v;const int rings=12,segs=16;for(int i=0;i<rings;i++){float a1=-1.5707963f+3.1415926f*i/rings,a2=-1.5707963f+3.1415926f*(i+1)/rings;for(int j=0;j<segs;j++){float b1=6.2831853f*j/segs,b2=6.2831853f*(j+1)/segs;auto p=[](float a,float b){return Vertex{std::cos(a)*std::cos(b),std::sin(a),std::cos(a)*std::sin(b)};};Vertex p1=p(a1,b1),p2=p(a2,b1),p3=p(a2,b2),p4=p(a1,b2);v.insert(v.end(),{p1,p2,p3,p1,p3,p4});}}return mesh(v);}
void draw(const Mesh&m,const std::array<float,16>&vp,const std::array<float,16>&mod,float r,float g,float b,float a=1){auto mvp=mul(vp,mod);glUniformMatrix4fv(mvpLoc,1,GL_FALSE,mvp.data());glUniform4f(colorLoc,r,g,b,a);glBindVertexArray(m.vao);glDrawArrays(m.mode,0,m.count);}
void line(std::vector<Vertex>&v,float x1,float z1,float x2,float z2){v.push_back({x1,.025f,z1});v.push_back({x2,.025f,z2});}
void initField(){
 std::vector<Vertex>f={ {-34,0,-52},{34,0,-52},{34,0,52},{-34,0,-52},{34,0,52},{-34,0,52} };fieldMesh=mesh(f);
 std::vector<Vertex>l;line(l,-34,-52,34,-52);line(l,34,-52,34,52);line(l,34,52,-34,52);line(l,-34,52,-34,-52);line(l,-34,0,34,0);
 for(int i=0;i<48;i++){float a=6.2831853f*i/48;line(l,9*std::cos(a),9*std::sin(a),9*std::cos(a+6.2831853f/48),9*std::sin(a+6.2831853f/48));}
 line(l,-34,-20,-18,-20);line(l,-18,-20,-18,20);line(l,-18,20,-34,20);
 line(l,34,-20,18,-20);line(l,18,-20,18,20);line(l,18,20,34,20);
 lineMesh=mesh(l,GL_LINES);sphereMesh=makeSphere();
 std::vector<Vertex> stands={
 {-42,0,-58},{-36,0,-58},{-36,7,-58},{-42,7,-58},{-42,0,58},{-36,0,58},{-36,7,58},{-42,7,58},
 {36,0,-58},{42,0,-58},{42,7,-58},{36,7,-58},{36,0,58},{42,0,58},{42,7,58},{36,7,58},
 {-42,0,-58},{42,0,-58},{42,7,-58},{-42,7,-58},{-42,0,58},{42,0,58},{42,7,58},{-42,7,58}};
 const int boxIdx[]={0,1,2,0,2,3,4,5,6,4,6,7,8,9,10,8,10,11,12,13,14,12,14,15,16,17,18,16,18,19,20,21,22,20,22,23};
 std::vector<Vertex> sv;for(int idx:boxIdx)sv.push_back(stands[(size_t)idx]);standMesh=mesh(sv);
 std::vector<Vertex> roof;
 auto roofQuad=[&](float x1,float z1,float x2,float z2,float y){
  roof.push_back({x1,y,z1});roof.push_back({x2,y,z1});roof.push_back({x2,y,z2});
  roof.push_back({x1,y,z1});roof.push_back({x2,y,z2});roof.push_back({x1,y,z2});
 };
 roofQuad(-43,-59,-35,59,7.2f);roofQuad(35,-59,43,59,7.2f);
 roofQuad(-43,-59,43,-51,7.2f);roofQuad(-43,51,43,59,7.2f);
 roofMesh=mesh(roof);
 std::vector<Vertex> accents;
 auto stadiumLine=[&](float x1,float z1,float x2,float z2){accents.push_back({x1,6.85f,z1});accents.push_back({x2,6.85f,z2});};
 stadiumLine(-43,-59,-35,-59);stadiumLine(-43,59,-35,59);
 stadiumLine(35,-59,43,-59);stadiumLine(35,59,43,59);
 stadiumLine(-43,-59,43,-59);stadiumLine(-43,59,43,59);
 stadiumAccentMesh=mesh(accents,GL_LINES);
 std::vector<Vertex> g;
 auto seg=[&](float x1,float y1,float z1,float x2,float y2,float z2){g.push_back({x1,y1,z1});g.push_back({x2,y2,z2});};
 for(float z:{-53.0f,53.0f}){float sign=z<0?-1.0f:1.0f;seg(-3.66f,.1f,z, -3.66f,2.44f,z);seg(3.66f,.1f,z,3.66f,2.44f,z);seg(-3.66f,2.44f,z,3.66f,2.44f,z);seg(-3.66f,.1f,z,-3.66f,.1f,z+sign*2.0f);seg(3.66f,.1f,z,3.66f,.1f,z+sign*2.0f);seg(-3.66f,2.44f,z,-3.66f,2.44f,z+sign*2.0f);seg(3.66f,2.44f,z,3.66f,2.44f,z+sign*2.0f);}
 goalMesh=mesh(g,GL_LINES);
}
void setupTeams(){
 const V3 formation[11]={{0,0,-45},{22,0,-30},{8,0,-32},{-8,0,-32},{-22,0,-30},{0,0,-10},{15,0,-8},{-15,0,-8},{22,0,18},{0,0,24},{-22,0,18}};
 for(int i=0;i<22;i++){playerHeight[i]=175.0f;playerPace[i]=72.0f;playerDribbling[i]=70.0f;playerDefending[i]=70.0f;playerOverall[i]=70;}
 for(int i=0;i<11;i++){players[i]=formation[i];players[11+i]={formation[i].x,0,-formation[i].z};}
 players[0]={0,0,-49.5f}; players[11]={0,0,49.5f}; keepers={};
}
void resetKickoffPositions(){
 const V3 formation[11]={{0,0,-45},{22,0,-30},{8,0,-32},{-8,0,-32},{-22,0,-30},{0,0,-10},{15,0,-8},{-15,0,-8},{22,0,18},{0,0,24},{-22,0,18}};
 for(int i=0;i<11;i++){
  V3 home=formation[i],away={formation[i].x,0,-formation[i].z};
  if(!homeDefendsNegative){home.z=-home.z;away.z=-away.z;}
  players[(size_t)i]=home;
  players[(size_t)(11+i)]=away;
 }
 players[0]={0,0,homeDefendsNegative?-49.5f:49.5f};
 players[11]={0,0,homeDefendsNegative?49.5f:-49.5f};
 keepers={};
 playerX=players[(size_t)controlledPlayer].x;
 playerZ=players[(size_t)controlledPlayer].z;
 playerYaw=homeDefendsNegative?3.1415926f:0.0f;
 ballX=0.0f;ballZ=0.0f;ballY=.22f;
 ballVX=ballVY=ballVZ=0.0f;ballReleaseTimer=0.0f;
}

void movePlayerToward(int slot,float tx,float tz,float speed,float dt){
 V3 &p=players[(size_t)slot];float dx=tx-p.x,dz=tz-p.z,d=std::sqrt(dx*dx+dz*dz);
 if(d>.001f){float step=std::min(d,speed*dt);p.x+=dx/d*step;p.z+=dz/d*step;}
}
V3 kickoffTarget(int slot){
 const V3 formation[11]={{0,0,-45},{22,0,-30},{8,0,-32},{-8,0,-32},{-22,0,-30},{0,0,-10},{15,0,-8},{-15,0,-8},{22,0,18},{0,0,24},{-22,0,18}};
 int local=slot%11;V3 p=slot<11?formation[local]:V3{formation[local].x,0,-formation[local].z};
 if(!homeDefendsNegative)p.z=-p.z;
 if(local==0)p.z=slot<11?(homeDefendsNegative?-49.5f:49.5f):(homeDefendsNegative?49.5f:-49.5f);
 return p;
}
void beginGoalSequence(bool homeScored){
 goalScoringHome=homeScored;goalSequencePhase=1;goalSequenceTimer=0.0f;
 ballX=0.0f;ballY=.22f;ballZ=homeScored?(homeDefendsNegative?52.0f:-52.0f):(homeDefendsNegative?-52.0f:52.0f);
 ballVX=ballVY=ballVZ=0.0f;ballReleaseTimer=0.0f;
 stickX=stickY=0.0f;shoot=pass=skill=pressure=false;joystickPointer=pressurePointer=-1;
}
void updateGoalSequence(float dt){
 goalSequenceTimer+=dt;
 if(goalSequencePhase==1){
  float scoringAttack=goalScoringHome?(homeDefendsNegative?1.0f:-1.0f):(homeDefendsNegative?-1.0f:1.0f);
  for(int slot=1;slot<22;slot++){
   if(slot==11)continue;
   if((slot<11)==goalScoringHome){
    int rank=(slot%11)-5;
    movePlayerToward(slot,std::clamp(rank*1.05f,-5.0f,5.0f),scoringAttack*43.0f+std::abs(rank)*.22f,5.4f,dt);
   }else{V3 target=kickoffTarget(slot);movePlayerToward(slot,target.x,target.z,5.8f,dt);}
  }
  if(goalSequenceTimer>=3.4f){goalSequencePhase=2;goalSequenceTimer=0.0f;}
 }else if(goalSequencePhase==2){
  for(int slot=1;slot<22;slot++){if(slot==11)continue;V3 target=kickoffTarget(slot);movePlayerToward(slot,target.x,target.z,9.0f,dt);}
  if(goalSequenceTimer>=2.8f){goalSequencePhase=0;goalSequenceTimer=0.0f;resetKickoffPositions();}
 }
 playerX=players[(size_t)controlledPlayer].x;playerZ=players[(size_t)controlledPlayer].z;
}
void drawPlayer(const std::array<float,16>&vp,V3 p,bool home,float yaw,int slot){
 bool goalkeeper=(slot%11)==0;
 float r=goalkeeper?.92f:(home?.10f:.86f),g=goalkeeper?.72f:(home?.68f:.16f),b=goalkeeper?.12f:(home?.20f:.19f);
 float dive=(goalkeeper?keepers[(size_t)(slot/11)].diveTimer:0.0f);
 if(goalkeeper){p.x+=keepers[(size_t)(slot/11)].diveX*(dive>0?.48f:0.0f);p.y+=dive>0?std::sin((.48f-dive)/.48f*3.14159f)*.30f:0.0f;}
 float h=std::clamp(playerHeight[slot]/175.0f,.92f,1.08f);
 float athletic=std::clamp(.96f+playerOverall[slot]/1750.0f,.98f,1.06f);
 bool celebrating=goalSequencePhase==1&&((slot<11)==goalScoringHome)&&!goalkeeper;
 p.y+=celebrating?std::abs(std::sin(matchTime*7.5f+slot*.31f))*.22f:0.0f;
 float stride=std::sin(matchTime*7.2f+slot*1.31f)*(celebrating?.07f:.13f);
 float armSwing=std::sin(matchTime*7.2f+slot*1.31f+3.14159f)*.11f;
 // Jersey, shorts, head and a small contrasting chest stripe.
 draw(sphereMesh,vp,model(p.x,p.y+1.02f*h,p.z,.31f*athletic,.43f*h,.21f,yaw),r,g,b);
 draw(sphereMesh,vp,model(p.x,p.y+1.04f*h,p.z-.207f,.025f,.31f*h,.018f,yaw),.92f,.94f,.86f);
 draw(sphereMesh,vp,model(p.x,p.y+.66f*h,p.z,.24f*athletic,.19f*h,.19f,yaw),.055f,.065f,.075f);
 draw(sphereMesh,vp,model(p.x,p.y+1.57f*h,p.z,.17f*h,.18f*h,.17f*h),.88f,.72f,.58f);
 draw(sphereMesh,vp,model(p.x,p.y+1.70f*h,p.z,.17f*h,.055f*h,.17f*h),.10f,.075f,.055f);
 if(goalkeeper){
  draw(sphereMesh,vp,model(p.x-.39f,p.y+.91f*h,p.z,.105f,.095f,.11f,yaw),.94f,.94f,.90f);
  draw(sphereMesh,vp,model(p.x+.39f,p.y+.91f*h,p.z,.105f,.095f,.11f,yaw),.94f,.94f,.90f);
 }
 // Sleeves follow the shirt color; forearms remain skin-toned.
 float armY=celebrating?1.43f:1.17f,forearmY=celebrating?1.62f:.91f;
 draw(sphereMesh,vp,model(p.x-.29f*athletic,p.y+armY*h,p.z+armSwing,.105f,.18f*h,.105f,yaw),r,g,b);
 draw(sphereMesh,vp,model(p.x+.29f*athletic,p.y+armY*h,p.z-armSwing,.105f,.18f*h,.105f,yaw),r,g,b);
 draw(sphereMesh,vp,model(p.x-.31f*athletic,p.y+forearmY*h,p.z+armSwing*1.4f,.065f,.20f*h,.07f,yaw),.88f,.72f,.58f);
 draw(sphereMesh,vp,model(p.x+.31f*athletic,p.y+forearmY*h,p.z-armSwing*1.4f,.065f,.20f*h,.07f,yaw),.88f,.72f,.58f);
 // Animated legs, white socks and dark boots.
 draw(sphereMesh,vp,model(p.x-.12f,p.y+.40f*h,p.z+stride,.105f,.25f*h,.11f,yaw),.12f,.13f,.15f);
 draw(sphereMesh,vp,model(p.x+.12f,p.y+.40f*h,p.z-stride,.105f,.25f*h,.11f,yaw),.12f,.13f,.15f);
 draw(sphereMesh,vp,model(p.x-.12f,p.y+.17f*h,p.z+stride*1.45f,.075f,.18f*h,.085f,yaw),.92f,.93f,.90f);
 draw(sphereMesh,vp,model(p.x+.12f,p.y+.17f*h,p.z-stride*1.45f,.075f,.18f*h,.085f,yaw),.92f,.93f,.90f);
 draw(sphereMesh,vp,model(p.x-.12f,p.y+.065f*h,p.z+stride*1.6f-.045f,.09f,.065f*h,.15f,yaw),.055f,.06f,.065f);
 draw(sphereMesh,vp,model(p.x+.12f,p.y+.065f*h,p.z-stride*1.6f-.045f,.09f,.065f*h,.15f,yaw),.055f,.06f,.065f);
}
void updateKeeper(int team,float dt){
 int slot=team==0?0:11;
 size_t profile=(size_t)slot;
 float diving=std::clamp(playerGoalkeeperDiving[profile],0.0f,99.0f)/99.0f;
 float reflexes=std::clamp(playerGoalkeeperReflexes[profile],0.0f,99.0f)/99.0f;
 float keeperSpeed=std::clamp(playerGoalkeeperSpeed[profile],0.0f,99.0f)/99.0f;
 float positioning=std::clamp(playerGoalkeeperPositioning[profile],0.0f,99.0f)/99.0f;
 float direction=team==0?(homeDefendsNegative?-1.0f:1.0f):(homeDefendsNegative?1.0f:-1.0f);
 float goalZ=direction*52.5f;
 float homeLine=direction*49.5f;
 KeeperState &state=keepers[(size_t)team];
 state.diveTimer=std::max(0.0f,state.diveTimer-dt);
 float targetX=std::clamp(ballX*(.16f+.04f*positioning),-2.7f,2.7f);
 bool ballComing=(ballVZ*direction)>0.8f && (ballZ*direction)>34.0f;
 float interceptTime=-1.0f;
 if(ballComing){
  interceptTime=(goalZ-ballZ)/ballVZ;
  if(interceptTime>=0.0f&&interceptTime<1.25f)
   targetX=std::clamp(ballX+ballVX*interceptTime,-3.0f,3.0f);
  else interceptTime=-1.0f;
 }
 float speed=state.diveTimer>0?(6.4f+.8f*diving):(4.2f+1.3f*keeperSpeed);
 float dx=targetX-players[profile].x;
 players[profile].x+=std::clamp(dx,-speed*dt,speed*dt);
 float targetZ=homeLine;
 if((ballZ*direction)>38.0f && (ballZ*direction)<51.0f)
  targetZ=homeLine-direction*(.35f+.30f*positioning);
 float dz=targetZ-players[profile].z;
 players[profile].z+=std::clamp(dz,-3.2f*dt,speed*.62f*dt);
 players[profile].x=std::clamp(players[profile].x,-3.15f,3.15f);
 // The keeper's end changes at halftime; clamp against the current defended goal.
 float minZ,maxZ;
 if(team==0){
  minZ=homeDefendsNegative?-51.0f:48.8f;
  maxZ=homeDefendsNegative?-48.8f:51.0f;
 }else{
  minZ=homeDefendsNegative?48.8f:-51.0f;
  maxZ=homeDefendsNegative?51.0f:-48.8f;
 }
 players[profile].z=std::clamp(players[profile].z,minZ,maxZ);
 float reactionWindow=.32f+.30f*reflexes;
 float diveReach=.80f+.85f*diving;
 if(state.diveTimer<=0.0f && ballComing && interceptTime>=0.0f &&
    interceptTime<reactionWindow && ballY<2.15f &&
    std::abs((ballX+ballVX*interceptTime)-players[profile].x)<diveReach){
  state.diveTimer=.30f+.18f*diving;
  state.diveX=(ballX+ballVX*interceptTime)>=players[profile].x?1.0f:-1.0f;
 }
}
void collideGoalFrame(){
 for(float sign:{-1.0f,1.0f}){
  float goalZ=sign*53.0f;
  for(float postX:{-3.66f,3.66f}){
   float cy=std::clamp(ballY,.10f,2.44f);
   float dx=ballX-postX,dy=ballY-cy,dz=ballZ-goalZ;
   float d2=dx*dx+dy*dy+dz*dz;
   if(d2<.32f*.32f){
    float d=std::sqrt(std::max(d2,.0001f)),nx=dx/d,ny=dy/d,nz=dz/d;
    ballX=postX+nx*.32f;ballY=cy+ny*.32f;ballZ=goalZ+nz*.32f;
    float vn=ballVX*nx+ballVY*ny+ballVZ*nz;
    if(vn<0){ballVX-=1.65f*vn*nx;ballVY-=1.65f*vn*ny;ballVZ-=1.65f*vn*nz;}
   }
  }
  float cx=std::clamp(ballX,-3.66f,3.66f);
  float dx=ballX-cx,dy=ballY-2.44f,dz=ballZ-goalZ;
  float d2=dx*dx+dy*dy+dz*dz;
  if(d2<.32f*.32f){
   float d=std::sqrt(std::max(d2,.0001f)),nx=dx/d,ny=dy/d,nz=dz/d;
   ballX=cx+nx*.32f;ballY=2.44f+ny*.32f;ballZ=goalZ+nz*.32f;
   float vn=ballVX*nx+ballVY*ny+ballVZ*nz;
   if(vn<0){ballVX-=1.65f*vn*nx;ballVY-=1.65f*vn*ny;ballVZ-=1.65f*vn*nz;}
  }
 }
}
void update(float dt){
 matchTime+=dt;
 if(goalSequencePhase!=0){updateGoalSequence(dt);return;}
 ballReleaseTimer=std::max(0.0f,ballReleaseTimer-dt);
 float controlledPace=std::clamp(playerPace[(size_t)controlledPlayer]/75.0f,.75f,1.30f);
 playerX=std::clamp(playerX+stickX*dt*8.0f*controlledPace,-32.0f,32.0f);
 playerZ=std::clamp(playerZ-stickY*dt*8.0f*controlledPace,-50.0f,50.0f);
 if(std::abs(stickX)+std::abs(stickY)>.08f)playerYaw=std::atan2(stickX,stickY);
 players[(size_t)controlledPlayer]={playerX,0,playerZ};
 const V3 formation[11]={{0,0,-45},{22,0,-30},{8,0,-32},{-8,0,-32},{-22,0,-30},{0,0,-10},{15,0,-8},{-15,0,-8},{22,0,18},{0,0,24},{-22,0,18}};
 int nearestHome=-1,nearestAway=-1;float homeDistance=1.0e9f,awayDistance=1.0e9f;
 for(int i=1;i<=10;i++){if(i!=controlledPlayer){float dx=players[(size_t)i].x-ballX,dz=players[(size_t)i].z-ballZ;float d=dx*dx+dz*dz;if(d<homeDistance){homeDistance=d;nearestHome=i;}}}
 for(int i=12;i<=21;i++){float dx=players[(size_t)i].x-ballX,dz=players[(size_t)i].z-ballZ;float d=dx*dx+dz*dz;if(d<awayDistance){awayDistance=d;nearestAway=i;}}
 for(int i=0;i<22;i++){
  if(i==0||i==controlledPlayer||i==11)continue;
  int local=i%11;
  V3 base=formation[local];
  float support=std::clamp(ballZ/70.0f,-.22f,.22f);
  float tx=base.x+std::clamp(ballX*.18f,-7.0f,7.0f);
  float attackDirection=homeDefendsNegative?1.0f:-1.0f;
  float tz=(i<11?base.z:-base.z)*attackDirection+support*12.0f*attackDirection;
  if(i==nearestHome||i==nearestAway){tx=ballX;tz=ballZ+(i<11?-1.1f:1.1f);}
  float dx=tx-players[(size_t)i].x,dz=tz-players[(size_t)i].z;
  float d=std::sqrt(dx*dx+dz*dz);
  float speed=(2.0f+playerPace[(size_t)i]*.025f)*(pressure&&i<11?1.65f:1.0f);
  if(d>.08f){float step=std::min(d,speed*dt);players[(size_t)i].x+=dx/d*step;players[(size_t)i].z+=dz/d*step;}
 }
 updateKeeper(0,dt);
 updateKeeper(1,dt);
 // Resolve body-to-body contacts so teammates and opponents cannot occupy
 // the same point. Apply symmetric separation and keep every actor in bounds.
 for(int i=0;i<22;i++){
  for(int j=i+1;j<22;j++){
   float ox=players[(size_t)j].x-players[(size_t)i].x;
   float oz=players[(size_t)j].z-players[(size_t)i].z;
   float d2=ox*ox+oz*oz;
   constexpr float minGap=.78f;
   if(d2>=minGap*minGap)continue;
   float d=std::sqrt(std::max(d2,.0001f));
   float nx,nz;
   if(d<.01f){nx=((i+j)%2==0)?1.0f:0.0f;nz=((i+j)%2==0)?0.0f:1.0f;}
   else{nx=ox/d;nz=oz/d;}
   float push=(minGap-d)*.5f;
   players[(size_t)i].x-=nx*push;players[(size_t)i].z-=nz*push;
   players[(size_t)j].x+=nx*push;players[(size_t)j].z+=nz*push;
  }
 }
 for(int i=0;i<22;i++){
  players[(size_t)i].x=std::clamp(players[(size_t)i].x,-33.0f,33.0f);
  players[(size_t)i].z=std::clamp(players[(size_t)i].z,-51.0f,51.0f);
 }
 // Body contacts must not push either goalkeeper out of the goal mouth.
 // Keep each keeper inside the six-yard defensive corridor after separation.
 players[0].x=std::clamp(players[0].x,-3.15f,3.15f);
 players[11].x=std::clamp(players[11].x,-3.15f,3.15f);
 if(homeDefendsNegative){players[0].z=std::clamp(players[0].z,-51.0f,-48.8f);players[11].z=std::clamp(players[11].z,48.8f,51.0f);}
 else{players[0].z=std::clamp(players[0].z,48.8f,51.0f);players[11].z=std::clamp(players[11].z,-51.0f,-48.8f);}
 // Keep the controlled-player coordinates synchronized after contact resolution.
 playerX=players[(size_t)controlledPlayer].x;playerZ=players[(size_t)controlledPlayer].z;
 float dx=std::sin(playerYaw),dz=-std::cos(playerYaw);
 float ballDistance=std::sqrt((ballX-playerX)*(ballX-playerX)+(ballZ-playerZ)*(ballZ-playerZ));
 bool controlledPossession=ballDistance<1.45f&&ballY<1.1f&&ballReleaseTimer<=0.0f;
 bool kicked=false;
 // Give the closest AI player a basic first-touch decision when the user is not in possession.
 if(!controlledPossession&&ballReleaseTimer<=0.0f&&ballY<.95f&&std::sqrt(ballVX*ballVX+ballVZ*ballVZ)<4.0f){
  int closest=-1;float closestD=1.0e9f;
  for(int i=1;i<22;i++){
   if(i==11||i==controlledPlayer)continue;
   float ox=players[(size_t)i].x-ballX,oz=players[(size_t)i].z-ballZ,d=ox*ox+oz*oz;
   if(d<closestD){closestD=d;closest=i;}
  }
  if(closest>=0&&closestD<.95f*.95f){
   bool home=closest<11;
   float attackDirection=home?(homeDefendsNegative?1.0f:-1.0f):(homeDefendsNegative?-1.0f:1.0f);
   int first=home?1:12,last=home?10:21,target=-1;float best=1.0e9f;
   float tx=0.0f,tz=attackDirection*53.0f;
   bool takeShot=attackDirection*ballZ>31.0f;
   if(!takeShot){
    for(int i=first;i<=last;i++){
     if(i==closest)continue;
     float ox=players[(size_t)i].x-players[(size_t)closest].x;
     float oz=players[(size_t)i].z-players[(size_t)closest].z;
     float distance=std::sqrt(ox*ox+oz*oz);
     if(distance<2.5f||distance>27.0f)continue;
     float progress=oz*attackDirection;
     float score=distance-progress*.35f;
     if(score<best){best=score;target=i;}
    }
   }
   if(target>=0){tx=players[(size_t)target].x;tz=players[(size_t)target].z;}
   if(!takeShot&&target<0){
    ballX=players[(size_t)closest].x;ballZ=players[(size_t)closest].z+attackDirection*.78f;
    ballY=.24f;ballVX=ballVZ=ballVY=0;ballReleaseTimer=.18f;kicked=true;
   }else{
    float kx=tx-players[(size_t)closest].x,kz=tz-players[(size_t)closest].z;
    float length=std::sqrt(kx*kx+kz*kz);
    if(length>.001f){
     kx/=length;kz/=length;
     ballX=players[(size_t)closest].x+kx*.72f;ballZ=players[(size_t)closest].z+kz*.72f;ballY=.34f;
     float power=takeShot?22.0f:13.0f;
     ballVX=kx*power;ballVZ=kz*power;ballVY=takeShot?4.8f:1.4f;
     ballReleaseTimer=.52f;kicked=true;
    }
   }
  }
 }
 if((pass||shoot)&&controlledPossession){
  float kickX=dx,kickZ=dz;
  if(pass){
   int target=-1;float best=1.0e9f;
   for(int i=1;i<=10;i++){
    if(i==controlledPlayer)continue;
    float tx=players[(size_t)i].x-playerX,tz=players[(size_t)i].z-playerZ;
    float distance=std::sqrt(tx*tx+tz*tz);
    if(distance<2.5f||distance>28.0f)continue;
    float progress=(players[(size_t)i].z-playerZ)*(homeDefendsNegative?1.0f:-1.0f);
    float score=distance+std::max(0.0f,-progress)*1.8f-progress*.22f;
    bool blocked=false;
    for(int j=12;j<=21;j++){
     float sx=players[(size_t)j].x-playerX,sz=players[(size_t)j].z-playerZ;
     float t=std::clamp((sx*tx+sz*tz)/(distance*distance),0.0f,1.0f);
     float ox=sx-tx*t,oz=sz-tz*t;
     if(t>.08f&&t<.92f&&ox*ox+oz*oz<1.35f*1.35f){blocked=true;break;}
    }
    if(blocked)score+=18.0f;
    if(score<best){best=score;target=i;}
   }
   if(target>=0){kickX=players[(size_t)target].x-playerX;kickZ=players[(size_t)target].z-playerZ;float len=std::sqrt(kickX*kickX+kickZ*kickZ);if(len>.001f){kickX/=len;kickZ/=len;}}
  }
  float power=shoot?25.0f:15.0f;
  ballVX=kickX*power;ballVZ=kickZ*power;ballVY=shoot?7.0f:2.0f;
  ballX=playerX+kickX*.72f;ballZ=playerZ+kickZ*.72f;ballY=.35f;
  ballReleaseTimer=.52f;kicked=true;
 }
 if(skill&&controlledPossession){ballX=playerX+dx*.95f;ballZ=playerZ+dz*.95f;ballY=.24f;ballVX=dx*2.4f;ballVZ=dz*2.4f;ballVY=.15f;kicked=true;}
 shoot=false;pass=false;skill=false;
 if(controlledPossession&&!kicked&&!pressure&&ballDistance<1.1f){
  ballX=playerX+dx*.72f;ballZ=playerZ+dz*.72f;ballY=.24f;
  ballVX=stickX*5.0f;ballVZ=-stickY*5.0f;
 }
 float oldX=ballX,oldZ=ballZ;
 if(!controlledPossession||pressure||kicked){
  ballX+=ballVX*dt;ballY+=ballVY*dt;ballZ+=ballVZ*dt;
  ballVY-=9.81f*dt;
 }
 ballVX*=std::pow(.985f,dt*60);ballVZ*=std::pow(.985f,dt*60);
 if(ballY<.22f){ballY=.22f;ballVY=std::abs(ballVY)*.38f;if(std::abs(ballVY)<.7f)ballVY=0;}
 // Continuous horizontal collision check prevents fast shots tunnelling through players.
 float segX=ballX-oldX,segZ=ballZ-oldZ,segLen2=segX*segX+segZ*segZ;
 float ballSpeed=std::sqrt(ballVX*ballVX+ballVZ*ballVZ);
 if(ballSpeed>1.0f || !controlledPossession){
  for(int i=0;i<22;i++){
   if(i==controlledPlayer&&controlledPossession)continue;
   float px=players[(size_t)i].x,pz=players[(size_t)i].z;
   float t=segLen2>.0001f?std::clamp(((px-oldX)*segX+(pz-oldZ)*segZ)/segLen2,0.0f,1.0f):1.0f;
   float cx=oldX+segX*t,cz=oldZ+segZ*t;
   float ox=cx-px,oz=cz-pz;
   bool keeper=(i==0||i==11);
   float diving=keeper?std::clamp(playerGoalkeeperDiving[(size_t)i],0.0f,99.0f)/99.0f:0.0f;
   float handling=keeper?std::clamp(playerGoalkeeperHandling[(size_t)i],0.0f,99.0f)/99.0f:0.0f;
   float radius=keeper?(.90f+.04f*diving+(keepers[(size_t)(i/11)].diveTimer>0?.10f+.12f*diving:0.0f)):.57f;
   float verticalLimit=keeper?1.90f+.20f*diving:1.72f;
   float d2=ox*ox+oz*oz;
   if(d2<radius*radius && ballY<verticalLimit && ballY>.12f){
    float d=std::sqrt(std::max(d2,.0001f));
    float nx, nz;
    if(d<.01f){float vl=std::max(.01f,ballSpeed);nx=-ballVX/vl;nz=-ballVZ/vl;}
    else{nx=ox/d;nz=oz/d;}
    ballX=cx+nx*(radius+.02f);ballZ=cz+nz*(radius+.02f);
    float vn=ballVX*nx+ballVZ*nz;
    if(vn<0){
     float restitution=keeper?(.78f-.28f*handling):.48f;
     ballVX-=(1.0f+restitution)*vn*nx;ballVZ-=(1.0f+restitution)*vn*nz;
    }
    if(keeper){
     // Goalkeeper handling softens the parry; kicking controls the clearance power.
     float kicking=std::clamp(playerGoalkeeperKicking[(size_t)i],0.0f,99.0f)/99.0f;
     float clearanceDirection=i==0?(homeDefendsNegative?1.0f:-1.0f):(homeDefendsNegative?-1.0f:1.0f);
     ballVZ=clearanceDirection*std::max(4.0f+1.5f*kicking,std::abs(ballVZ)*(.55f+.17f*kicking));
     ballVX+=std::clamp((ballX-px)*(2.0f-.8f*handling),-4.5f,4.5f);
     ballVY=std::max(ballVY,1.0f+.8f*(1.0f-handling));
     keepers[(size_t)(i/11)].diveTimer=std::max(keepers[(size_t)(i/11)].diveTimer,.20f);
    }
    break;
   }
  }
 }
 collideGoalFrame();
 bool crossedPositive=oldZ<53.0f&&ballZ>=53.0f&&std::abs(ballX)<3.44f&&ballY<2.22f;
 bool crossedNegative=oldZ>-53.0f&&ballZ<=-53.0f&&std::abs(ballX)<3.44f&&ballY<2.22f;
 bool homeGoal=homeDefendsNegative?crossedPositive:crossedNegative;
 bool awayGoal=homeDefendsNegative?crossedNegative:crossedPositive;
 if(homeGoal){homeScore++;beginGoalSequence(true);}
 else if(awayGoal){awayScore++;beginGoalSequence(false);}
 if(std::abs(ballX)>34){ballX=std::copysign(34.0f,ballX);ballVX*=-.55f;}
 if(ballZ>52.0f && ballZ<53.0f){ballZ=52.0f;ballVZ=-std::abs(ballVZ)*.55f;}
 if(ballZ<-52.0f && ballZ>-53.0f){ballZ=-52.0f;ballVZ=std::abs(ballVZ)*.55f;}
 if(ballZ>55.0f){ballZ=55.0f;ballVZ=-std::abs(ballVZ)*.45f;}
 if(ballZ<-55.0f){ballZ=-55.0f;ballVZ=std::abs(ballVZ)*.45f;}
}
}
extern "C" JNIEXPORT void JNICALL Java_com_nova_futbolnova_MainActivity_nativeInit(JNIEnv*,jobject){
 std::lock_guard<std::mutex> lock(stateMutex);homeScore=awayScore=0;matchSeconds=0;matchTime=0;matchPhase=0;goalSequencePhase=0;goalSequenceTimer=0;homeDefendsNegative=true;controlledPlayer=9;ballReleaseTimer=0;stickX=stickY=0;shoot=pass=skill=pressure=false;joystickPointer=pressurePointer=-1;paused=false;makeProgram();initField();setupTeams();glEnable(GL_DEPTH_TEST);glClearColor(.015f,.045f,.075f,1);}
extern "C" JNIEXPORT void JNICALL Java_com_nova_futbolnova_MainActivity_nativeSetStadiumTheme(
    JNIEnv*,jclass,jint seating,jint accent,jint roof,jint secondary){
 std::lock_guard<std::mutex> lock(stateMutex);
 auto unpack=[](jint color)->V3{
  return {((color>>16)&0xff)/255.0f,((color>>8)&0xff)/255.0f,(color&0xff)/255.0f};
 };
 stadiumSeatColor=unpack(seating);
 stadiumAccentColor=unpack(accent);
 stadiumRoofColor=unpack(roof);
 stadiumSecondaryColor=unpack(secondary);
}
extern "C" JNIEXPORT void JNICALL Java_com_nova_futbolnova_MainActivity_nativeResize(JNIEnv*,jobject,jint w,jint h){screenW=std::max(1,(int)w);screenH=std::max(1,(int)h);glViewport(0,0,screenW,screenH);}
extern "C" JNIEXPORT void JNICALL Java_com_nova_futbolnova_MainActivity_nativeFrame(JNIEnv*,jobject){
 std::lock_guard<std::mutex> lock(stateMutex);
 static double last=0; timespec ts{};clock_gettime(CLOCK_MONOTONIC,&ts);double now=ts.tv_sec+ts.tv_nsec*1e-9;float dt=last==0?0.016f:(float)std::clamp(now-last,0.001,0.04);last=now;
 if(!paused){
  update(dt);matchSeconds+=dt;
  if(matchPhase==0&&matchSeconds>=halfDurationSeconds){matchSeconds=halfDurationSeconds;matchPhase=1;paused=true;}
  else if(matchPhase==2&&matchSeconds>=halfDurationSeconds){matchSeconds=halfDurationSeconds;matchPhase=3;paused=true;}
 }
 glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);glUseProgram(program);
 float aspect=(float)screenW/(float)screenH;auto proj=perspective(.92f,aspect,.1f,180.0f);
 V3 eye{playerX-std::sin(playerYaw)*18.0f,10.5f,playerZ+std::cos(playerYaw)*18.0f},center{ballX*.40f+playerX*.60f,1.0f,ballZ*.40f+playerZ*.60f};auto vp=mul(proj,lookAt(eye,center,{0,1,0}));
 draw(standMesh,vp,model(0,0,0,1,1,1),stadiumSeatColor.x,stadiumSeatColor.y,stadiumSeatColor.z);
 draw(roofMesh,vp,identity(),stadiumRoofColor.x,stadiumRoofColor.y,stadiumRoofColor.z);
 draw(stadiumAccentMesh,vp,identity(),stadiumAccentColor.x,stadiumAccentColor.y,stadiumAccentColor.z);
 draw(fieldMesh,vp,identity(),.035f,.34f,.13f);
 draw(lineMesh,vp,identity(),.88f,.94f,.85f);
 draw(goalMesh,vp,identity(),stadiumSecondaryColor.x,stadiumSecondaryColor.y,stadiumSecondaryColor.z);
 for(int i=0;i<22;i++){float yaw=i==controlledPlayer?playerYaw:(i<11?3.1415926f:0.0f);drawPlayer(vp,players[i],i<11,yaw,i);}
 draw(sphereMesh,vp,model(ballX,ballY,ballZ,.22f,.22f,.22f),.96f,.96f,.92f);
}
extern "C" JNIEXPORT void JNICALL Java_com_nova_futbolnova_MainActivity_nativeTouch(JNIEnv*,jclass,jfloat x,jfloat y,jint action,jint pointerId,jint w,jint h){
 std::lock_guard<std::mutex> lock(stateMutex);
 float nx=x/(float)std::max(1,w),ny=y/(float)std::max(1,h);
 bool joystickZone=nx<.38f&&ny>.55f;
 bool pressZone=nx>.78f&&ny>.86f;
 bool shootZone=nx>.78f&&ny>.68f&&ny<=.86f;
 bool passZone=nx>.65f&&nx<=.78f&&ny>.48f&&ny<=.68f;
 bool skillZone=nx>.78f&&ny<=.68f;
 if(action==0||action==5){
  if(joystickZone&&joystickPointer<0)joystickPointer=pointerId;
  if(joystickPointer==pointerId){float radius=.105f*(float)std::min(w,h);stickX=std::clamp((x-.17f*w)/radius,-1.0f,1.0f);stickY=std::clamp((y-.79f*h)/radius,-1.0f,1.0f);}
  if(pressZone&&pressurePointer<0){pressurePointer=pointerId;pressure=true;}
  if(shootZone)shoot=true;
  if(passZone)pass=true;
  if(skillZone)skill=true;
 }else if(action==2){
  if(joystickPointer==pointerId){float radius=.105f*(float)std::min(w,h);stickX=std::clamp((x-.17f*w)/radius,-1.0f,1.0f);stickY=std::clamp((y-.79f*h)/radius,-1.0f,1.0f);}
 }else if(action==1||action==6){
  if(joystickPointer==pointerId){joystickPointer=-1;stickX=0;stickY=0;}
  if(pressurePointer==pointerId){pressurePointer=-1;pressure=false;}
 }else if(action==3){
  joystickPointer=-1;pressurePointer=-1;stickX=stickY=0;pressure=false;
 }
}

extern "C" JNIEXPORT void JNICALL Java_com_nova_futbolnova_MainActivity_nativeSetPlayerProfile(
    JNIEnv*,jclass,jint slot,jfloat heightCm,jint overall,jfloat pace,jfloat dribbling,jfloat defending,
    jfloat goalkeeperDiving,jfloat goalkeeperHandling,jfloat goalkeeperKicking,jfloat goalkeeperReflexes,
    jfloat goalkeeperSpeed,jfloat goalkeeperPositioning){
 std::lock_guard<std::mutex> lock(stateMutex);
 if(slot<0||slot>=22)return;
 size_t index=(size_t)slot;
 playerHeight[index]=std::clamp((float)heightCm,150.0f,205.0f);
 playerOverall[index]=std::clamp((int)overall,1,99);
 playerPace[index]=std::clamp((float)pace,1.0f,99.0f);
 playerDribbling[index]=std::clamp((float)dribbling,1.0f,99.0f);
 playerDefending[index]=std::clamp((float)defending,1.0f,99.0f);
 playerGoalkeeperDiving[index]=std::clamp((float)goalkeeperDiving,0.0f,99.0f);
 playerGoalkeeperHandling[index]=std::clamp((float)goalkeeperHandling,0.0f,99.0f);
 playerGoalkeeperKicking[index]=std::clamp((float)goalkeeperKicking,0.0f,99.0f);
 playerGoalkeeperReflexes[index]=std::clamp((float)goalkeeperReflexes,0.0f,99.0f);
 playerGoalkeeperSpeed[index]=std::clamp((float)goalkeeperSpeed,0.0f,99.0f);
 playerGoalkeeperPositioning[index]=std::clamp((float)goalkeeperPositioning,0.0f,99.0f);
}

extern "C" JNIEXPORT void JNICALL Java_com_nova_futbolnova_MainActivity_nativeSwitchPlayer(JNIEnv*,jclass){
 std::lock_guard<std::mutex> lock(stateMutex);
 std::array<std::pair<float,int>,10> ranked{};
 for(int n=0;n<10;n++){int slot=n+1;float dx=players[(size_t)slot].x-ballX,dz=players[(size_t)slot].z-ballZ;ranked[(size_t)n]={dx*dx+dz*dz,slot};}
 std::sort(ranked.begin(),ranked.end(),[](const auto&a,const auto&b){return a.first<b.first;});
 int currentRank=0;for(int n=0;n<10;n++)if(ranked[(size_t)n].second==controlledPlayer)currentRank=n;
 controlledPlayer=ranked[(size_t)((currentRank+1)%10)].second;
 playerX=players[(size_t)controlledPlayer].x;playerZ=players[(size_t)controlledPlayer].z;
 playerYaw=std::atan2(ballX-playerX,-(ballZ-playerZ));
}
extern "C" JNIEXPORT void JNICALL Java_com_nova_futbolnova_MainActivity_nativeSetMatchDurationMinutes(JNIEnv*,jclass,jint minutes){
 std::lock_guard<std::mutex> lock(stateMutex);halfDurationSeconds=(float)std::clamp((int)minutes,3,20)*60.0f;}
extern "C" JNIEXPORT void JNICALL Java_com_nova_futbolnova_MainActivity_nativeStartSecondHalf(JNIEnv*,jclass){
 std::lock_guard<std::mutex> lock(stateMutex);
 if(matchPhase!=1)return;
 homeDefendsNegative=false;
 for(auto &player:players)player.z=-player.z;
 playerX=players[(size_t)controlledPlayer].x;playerZ=players[(size_t)controlledPlayer].z;
 playerYaw=3.1415926f-playerYaw;
 ballX=0;ballY=.22f;ballZ=0;ballVX=ballVY=ballVZ=0;ballReleaseTimer=0;keepers={};
 matchSeconds=0;matchPhase=2;paused=false;
}
extern "C" JNIEXPORT jint JNICALL Java_com_nova_futbolnova_MainActivity_nativeGetMatchPhase(JNIEnv*,jclass){std::lock_guard<std::mutex> lock(stateMutex);return matchPhase;}
extern "C" JNIEXPORT void JNICALL Java_com_nova_futbolnova_MainActivity_nativePause(JNIEnv*,jclass,jboolean value){std::lock_guard<std::mutex> lock(stateMutex);paused=value;}

extern "C" JNIEXPORT jint JNICALL Java_com_nova_futbolnova_MainActivity_nativeGetHomeScore(JNIEnv*,jclass){std::lock_guard<std::mutex> lock(stateMutex);return homeScore;}
extern "C" JNIEXPORT jint JNICALL Java_com_nova_futbolnova_MainActivity_nativeGetAwayScore(JNIEnv*,jclass){std::lock_guard<std::mutex> lock(stateMutex);return awayScore;}
extern "C" JNIEXPORT jfloat JNICALL Java_com_nova_futbolnova_MainActivity_nativeGetMatchSeconds(JNIEnv*,jclass){std::lock_guard<std::mutex> lock(stateMutex);return matchSeconds;}
extern "C" JNIEXPORT jint JNICALL Java_com_nova_futbolnova_MainActivity_nativeGetGoalSequencePhase(JNIEnv*,jclass){std::lock_guard<std::mutex> lock(stateMutex);return goalSequencePhase;}
extern "C" JNIEXPORT void JNICALL Java_com_nova_futbolnova_MainActivity_nativeSkipGoalSequence(JNIEnv*,jclass){std::lock_guard<std::mutex> lock(stateMutex);if(goalSequencePhase!=0){goalSequencePhase=0;goalSequenceTimer=0.0f;resetKickoffPositions();}}
