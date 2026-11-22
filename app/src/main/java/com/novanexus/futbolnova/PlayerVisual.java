package com.novanexus.futbolnova;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.math.Vector3;

/** Low-poly articulated humanoid built from shared meshes for mobile rendering. */
public final class PlayerVisual {
    private final ModelInstance torso,head,hair,leftArm,rightArm,leftLeg,rightLeg,leftBoot,rightBoot;
    private final float phaseOffset;
    private static final Vector3 UP=new Vector3(0,1,0);

    public PlayerVisual(Model torsoModel,Model headModel,Model hairModel,Model armModel,
            Model legModel,Model bootModel,int shirtNumber) {
        torso=new ModelInstance(torsoModel); head=new ModelInstance(headModel); hair=new ModelInstance(hairModel);
        leftArm=new ModelInstance(armModel); rightArm=new ModelInstance(armModel);
        leftLeg=new ModelInstance(legModel); rightLeg=new ModelInstance(legModel);
        leftBoot=new ModelInstance(bootModel); rightBoot=new ModelInstance(bootModel);
        phaseOffset=shirtNumber*0.31f;
    }

    public void update(float x,float z,float yaw,float time,float speed) {
        float swing=(float)Math.sin(time*9f+phaseOffset)*Math.min(0.48f,speed*0.075f);
        place(torso,x,1.12f,z,yaw,0f);
        place(head,x,1.64f,z,yaw,0f);
        place(hair,x,1.77f,z,yaw,0f);
        place(leftArm,x-0.39f,1.15f,z,yaw,-swing*0.85f);
        place(rightArm,x+0.39f,1.15f,z,yaw,swing*0.85f);
        place(leftLeg,x-0.14f,0.43f,z,yaw,swing);
        place(rightLeg,x+0.14f,0.43f,z,yaw,-swing);
        place(leftBoot,x-0.14f,0.10f,z-0.045f,yaw,swing);
        place(rightBoot,x+0.14f,0.10f,z-0.045f,yaw,-swing);
    }

    private static void place(ModelInstance instance,float x,float y,float z,float yaw,float limbSwing) {
        instance.transform.setToTranslation(x,y,z);
        instance.transform.rotate(UP,yaw);
        if(limbSwing!=0f) instance.transform.rotate(Vector3.X,limbSwing*57.29578f);
    }

    public void render(ModelBatch batch,Environment environment) {
        batch.render(torso,environment);batch.render(head,environment);batch.render(hair,environment);
        batch.render(leftArm,environment);batch.render(rightArm,environment);
        batch.render(leftLeg,environment);batch.render(rightLeg,environment);
        batch.render(leftBoot,environment);batch.render(rightBoot,environment);
    }
}
