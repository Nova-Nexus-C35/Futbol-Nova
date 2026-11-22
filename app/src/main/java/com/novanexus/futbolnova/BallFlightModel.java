package com.novanexus.futbolnova;
import com.badlogic.gdx.math.Vector3;
/** Aerodynamic ball flight model ported from the original Fútbol Nova model. */
public final class BallFlightModel {
 public static final float DEFAULT_GRAVITY=9.81f, DEFAULT_DRAG=0.0013f, DEFAULT_MAGNUS=0.0018f, DEFAULT_SPIN_DECAY=0.22f, MAX_SPIN=110f;
 private BallFlightModel(){}
 public static Vector3 aerodynamicForce(Vector3 velocity,Vector3 spin,float drag,float magnus,float maxForce){
  Vector3 d=velocity.cpy().scl(-velocity.len()*Math.max(0f,drag));
  Vector3 m=spin.cpy().crs(velocity).scl(Math.max(0f,magnus)).limit(Math.max(0f,maxForce));
  return d.add(m);
 }
 public static Vector3 decaySpin(Vector3 spin,float delta,float decay){
  return spin.cpy().limit(MAX_SPIN).scl(1f/(1f+Math.max(0f,decay)*Math.max(0f,delta)));
 }
 public static Step integrateStep(Vector3 p,Vector3 v,Vector3 s,float dt){
  return integrateStep(p,v,s,dt,0.43f,DEFAULT_GRAVITY,DEFAULT_DRAG,DEFAULT_MAGNUS,DEFAULT_SPIN_DECAY);
 }
 public static Step integrateStep(Vector3 p,Vector3 v,Vector3 s,float dt,float mass,float gravity,float drag,float magnus,float decay){
  float t=Math.max(0f,Math.min(dt,0.05f)), safeMass=Math.max(0.01f,mass);
  Vector3 force=aerodynamicForce(v,s,drag,magnus,4.8f);
  Vector3 acceleration=force.scl(1f/safeMass).add(0f,-gravity,0f);
  Vector3 nv=v.cpy().mulAdd(acceleration,t);
  Vector3 np=p.cpy().add(v.cpy().add(nv).scl(0.5f*t));
  return new Step(np,nv,decaySpin(s,t,decay));
 }
 public static final class Step{
  public final Vector3 position,velocity,spin;
  private Step(Vector3 p,Vector3 v,Vector3 s){position=p;velocity=v;spin=s;}
 }
}
