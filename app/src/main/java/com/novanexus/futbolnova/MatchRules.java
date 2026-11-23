package com.novanexus.futbolnova;
import java.util.List;
import com.badlogic.gdx.math.Vector3;
/** Deterministic rule geometry ported from the original Fútbol Nova match rules. */
public final class MatchRules{
 public enum Restart{IN_PLAY,GOAL,THROW_IN,CORNER,GOAL_KICK,FREE_KICK,PENALTY}
 private MatchRules(){}
 /**
  * Evaluates offside position at the instant a teammate plays the ball.
  * Coordinates are projected onto the attacking direction so the same rule
  * works before and after the teams switch ends. The offside line is the
  * farther-forward of the ball and the second-last active opponent.
  *
  * The active flag represents involvement in active play (for this game the
  * receiver is sanctioned only if they subsequently touch/interfere with play).
  */
 public static OffsideDecision evaluateOffsideAtPass(float attackerZ,float ballZ,List<Float> defenders,float attackSign,boolean active,String restart){
  if(isOffsideExemptRestart(restart))return new OffsideDecision(false,false,"restart_exemption",Float.NaN,Float.NaN);
  if(!Float.isFinite(attackerZ)||!Float.isFinite(ballZ)||!Float.isFinite(attackSign)
          ||Math.abs(attackSign)<0.000001f||defenders==null)
   return new OffsideDecision(false,false,"insufficient_data",Float.NaN,Float.NaN);
  float direction=Math.signum(attackSign),attacker=attackerZ*direction,ball=ballZ*direction;
  if(attacker<=0f)return new OffsideDecision(false,false,"own_half",Float.NaN,Float.NaN);
  float first=Float.NEGATIVE_INFINITY,second=Float.NEGATIVE_INFINITY;
  int validDefenders=0;
  for(Float raw:defenders){
   if(raw==null||!Float.isFinite(raw))continue;
   float progress=raw*direction;validDefenders++;
   if(progress>first){second=first;first=progress;}
   else if(progress>second)second=progress;
  }
  if(validDefenders<2)return new OffsideDecision(false,false,"insufficient_defenders",Float.NaN,Float.NaN);
  float line=Math.max(ball,second);
  boolean position=attacker>line+0.0001f;
  boolean offence=position&&active;
  String reason=offence?"active_offside":(position?"passive_position":"onside");
  return new OffsideDecision(position,offence,reason,second,line);
 }

 private static boolean isOffsideExemptRestart(String restart){
  return restart!=null&&(restart.equalsIgnoreCase("goal_kick")
          ||restart.equalsIgnoreCase("throw_in")||restart.equalsIgnoreCase("corner"));
 }
 /** Produces the initial ball velocity for every automatic restart, respecting the attacking direction. */
 public static Vector3 restartVelocity(Restart restart,boolean kickoff,boolean awardedHome,
         boolean homeAttacksNegativeZ,float ballX,float goalkeeperX){
  Vector3 velocity=new Vector3();
  if(restart==null||!Float.isFinite(ballX))return velocity;
  float attackSign=awardedHome?(homeAttacksNegativeZ?-1f:1f):(homeAttacksNegativeZ?1f:-1f);
  if(kickoff)return velocity.set(0f,1.2f,attackSign*5.5f);
  switch(restart){
   case THROW_IN:return velocity.set(ballX>=0f?-5.5f:5.5f,5.2f,attackSign*4.5f);
   case CORNER:return velocity.set(-ballX*0.22f,5.0f,attackSign*13f).nor().scl(16f);
   case GOAL_KICK:return velocity.set(0f,6.2f,attackSign*22f);
   case FREE_KICK:return velocity.set(0f,4.5f,attackSign*15f);
   case PENALTY:return MatchGameplay.penaltyShotVelocity(goalkeeperX,awardedHome,homeAttacksNegativeZ);
   default:return velocity;
  }
 }
 /** Classifies a direct free kick or penalty based on the defending team's own penalty area. */
 public static Restart classifyFoulRestart(float x,float z,boolean defendingHome,boolean homeDefendsPositiveZ){
  float ownGoalDirection=defendingHome?(homeDefendsPositiveZ?1f:-1f):(homeDefendsPositiveZ?-1f:1f);
  float goalProgress=z*ownGoalDirection;
  boolean insidePenaltyArea=Math.abs(x)<=20.16f&&goalProgress>=36f&&goalProgress<=52.5f;
  return insidePenaltyArea?Restart.PENALTY:Restart.FREE_KICK;
 }
 public static ExitDecision classifyBallExit(float x,float y,float z,boolean lastHome,float radius){
  return classifyBallExit(x,y,z,lastHome,34f,52.5f,3.66f,2.44f,radius);
 }
 public static ExitDecision classifyBallExit(float x,float y,float z,boolean lastHome,float hw,float hl,float gw,float bar,float r){
  return classifyBallExit(x,y,z,lastHome,hw,hl,gw,bar,r,true);
 }
 public static ExitDecision classifyBallExit(float x,float y,float z,boolean lastHome,float hw,float hl,float gw,float bar,float r,boolean homeDefendsPositiveZ){
  boolean touch=Math.abs(x)-r>hw,goal=Math.abs(z)-r>hl;
  if(!touch&&!goal)return new ExitDecision(Restart.IN_PLAY,false,x,y,z);
  if(goal){
   boolean defendingHome=homeDefendsPositiveZ?z>0f:z<0f,attackingHome=!defendingHome;
   if(Math.abs(x)+r<=gw&&y-r>=0f&&y+r<=bar)return new ExitDecision(Restart.GOAL,attackingHome,0f,0f,0f);
   if(lastHome==defendingHome)return new ExitDecision(Restart.CORNER,attackingHome,Math.signum(x)*hw,0f,Math.signum(z)*hl);
   return new ExitDecision(Restart.GOAL_KICK,defendingHome,0f,0f,hl*(defendingHome?1f:-1f)*0.88f);
  }
  return new ExitDecision(Restart.THROW_IN,!lastHome,clamp(x,-hw,hw),0f,clamp(z,-hl,hl));
 }
 public static final class OffsideDecision{
  public final boolean offsidePosition,offence;
  public final String reason;
  public final float secondLastDefenderProgress,offsideLineProgress;
  private OffsideDecision(boolean p,boolean o,String r,float d,float line){
   offsidePosition=p;offence=o;reason=r;secondLastDefenderProgress=d;offsideLineProgress=line;
  }
 }
 public static final class ExitDecision{
  public final Restart restart;public final boolean awardedHome;public final float x,y,z;
  private ExitDecision(Restart r,boolean h,float x,float y,float z){restart=r;awardedHome=h;this.x=x;this.y=y;this.z=z;}
 }
 private static float clamp(float v,float min,float max){return Math.max(min,Math.min(max,v));}
}
