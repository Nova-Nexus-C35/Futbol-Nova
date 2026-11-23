package com.novanexus.futbolnova;
import java.util.List;
/** Deterministic rule geometry ported from the original Fútbol Nova match rules. */
public final class MatchRules{
 public enum Restart{IN_PLAY,GOAL,THROW_IN,CORNER,GOAL_KICK}
 private MatchRules(){}
 public static OffsideDecision evaluateOffsideAtPass(float attackerZ,float ballZ,List<Float> defenders,float attackSign,boolean active,String restart){
  if("goal_kick".equals(restart)||"throw_in".equals(restart)||"corner".equals(restart))return new OffsideDecision(false,false,"restart_exemption",Float.NaN);
  if(Math.abs(attackSign)<0.000001f||defenders==null||defenders.size()<2)return new OffsideDecision(false,false,"insufficient_data",Float.NaN);
  float dir=Math.signum(attackSign),a=attackerZ*dir,b=ballZ*dir;
  if(a<=0f)return new OffsideDecision(false,false,"own_half",Float.NaN);
  float first=Float.NEGATIVE_INFINITY,second=Float.NEGATIVE_INFINITY;
  for(Float raw:defenders){if(raw==null)continue;float v=raw*dir;if(v>first){second=first;first=v;}else if(v>second)second=v;}
  if(second==Float.NEGATIVE_INFINITY)return new OffsideDecision(false,false,"insufficient_defenders",Float.NaN);
  boolean pos=a>b&&a>second,off=pos&&active;
  return new OffsideDecision(pos,off,off?"active_offside":(pos?"passive_position":"onside"),second);
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
  public final boolean offsidePosition,offence;public final String reason;public final float secondLastDefenderProgress;
  private OffsideDecision(boolean p,boolean o,String r,float d){offsidePosition=p;offence=o;reason=r;secondLastDefenderProgress=d;}
 }
 public static final class ExitDecision{
  public final Restart restart;public final boolean awardedHome;public final float x,y,z;
  private ExitDecision(Restart r,boolean h,float x,float y,float z){restart=r;awardedHome=h;this.x=x;this.y=y;this.z=z;}
 }
 private static float clamp(float v,float min,float max){return Math.max(min,Math.min(max,v));}
}
