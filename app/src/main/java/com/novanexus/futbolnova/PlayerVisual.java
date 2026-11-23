package com.novanexus.futbolnova;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.math.Vector3;

/** Low-poly articulated humanoid built from shared meshes for mobile rendering. */
public final class PlayerVisual {
    private final ModelInstance torso,shorts,head,hair,leftArm,rightArm,leftLeg,rightLeg,leftBoot,rightBoot;
    private final float phaseOffset;
    private static final Vector3 UP=new Vector3(0,1,0);

    public PlayerVisual(Model torsoModel,Model shortsModel,Model headModel,Model hairModel,Model armModel,
            Model legModel,Model bootModel,int shirtNumber) {
        torso=new ModelInstance(torsoModel); shorts=new ModelInstance(shortsModel); head=new ModelInstance(headModel); hair=new ModelInstance(hairModel);
        leftArm=new ModelInstance(armModel); rightArm=new ModelInstance(armModel);
        leftLeg=new ModelInstance(legModel); rightLeg=new ModelInstance(legModel);
        leftBoot=new ModelInstance(bootModel); rightBoot=new ModelInstance(bootModel);
        phaseOffset=shirtNumber*0.31f;
    }

    public void update(float x,float z,float yaw,float time,float speed) {
        float swing=PlayerAnimation.gaitSwing(speed,time,phaseOffset);
        float bob=PlayerAnimation.bodyBob(speed,time,phaseOffset);
        place(torso,x,1.15f+bob,z,yaw);
        place(shorts,x,0.77f+bob*0.55f,z,yaw);
        place(head,x,1.64f+bob,z,yaw);
        place(hair,x,1.77f+bob,z,yaw);
        // Arms and legs rotate around shoulder/hip pivots instead of their own centers.
        placeLimb(leftArm,x-0.27f,1.42f,z,yaw,-swing*0.82f,0.44f);
        placeLimb(rightArm,x+0.27f,1.42f,z,yaw,swing*0.82f,0.44f);
        placeLimb(leftLeg,x-0.14f,0.66f,z,yaw,swing,0.47f);
        placeLimb(rightLeg,x+0.14f,0.66f,z,yaw,-swing,0.47f);
        placeLimb(leftBoot,x-0.14f,0.22f,z-0.045f,yaw,swing*0.55f,0.30f);
        placeLimb(rightBoot,x+0.14f,0.22f,z-0.045f,yaw,-swing*0.55f,0.30f);
    }

    private static void place(ModelInstance instance,float x,float y,float z,float yaw) {
        instance.transform.setToTranslation(x,y,z);
        instance.transform.rotate(UP,yaw);
    }

    private static void placeLimb(ModelInstance instance,float pivotX,float pivotY,float pivotZ,
            float yaw,float angle,float length) {
        float half=length*0.5f;
        float centerY=pivotY-half*(float)Math.cos(angle);
        float centerZ=pivotZ-half*(float)Math.sin(angle);
        instance.transform.setToTranslation(pivotX,centerY,centerZ);
        instance.transform.rotate(UP,yaw);
        instance.transform.rotate(Vector3.X,angle*57.29578f);
    }

    /** Applies a short lateral dive pose after the normal procedural gait update. */
    public void setGoalkeeperDive(float direction,float intensity) {
        float side=direction<0f?-1f:1f;
        float amount=Math.max(0f,Math.min(1f,intensity));
        float roll=side*32f*amount;
        torso.transform.rotate(Vector3.Z,roll);
        shorts.transform.rotate(Vector3.Z,roll*0.55f);
        head.transform.rotate(Vector3.Z,roll*0.35f);
        leftArm.transform.rotate(Vector3.Z,-side*24f*amount);
        rightArm.transform.rotate(Vector3.Z,-side*24f*amount);
        leftLeg.transform.rotate(Vector3.Z,side*12f*amount);
        rightLeg.transform.rotate(Vector3.Z,side*12f*amount);
    }

    public void render(ModelBatch batch,Environment environment) {
        batch.render(torso,environment);batch.render(shorts,environment);batch.render(head,environment);batch.render(hair,environment);
        batch.render(leftArm,environment);batch.render(rightArm,environment);
        batch.render(leftLeg,environment);batch.render(rightLeg,environment);
        batch.render(leftBoot,environment);batch.render(rightBoot,environment);
    }
}
