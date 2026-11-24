package com.novanexus.futbolnova;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

/** Atomic local-file storage for automatic match recovery on Android. */
public final class MatchSaveStore {
    private static final String SAVE_PATH="FutbolNova/saves/current-match.bin";
    private static final String TEMP_PATH="FutbolNova/saves/current-match.json.tmp";
    private MatchSaveStore() {}

    public static boolean save(MatchRuntimeSnapshot snapshot){
        if(Gdx.files==null||snapshot==null||!snapshot.isValid())return false;
        try{
            FileHandle target=Gdx.files.local(SAVE_PATH),temp=Gdx.files.local(TEMP_PATH);
            target.parent().mkdirs();
            try(ObjectOutputStream output=new ObjectOutputStream(temp.write(false))){
                output.writeObject(snapshot);output.flush();
            }
            temp.moveTo(target);
            return target.exists()&&target.length()>0;
        }catch(RuntimeException error){
            if(Gdx.app!=null)Gdx.app.error("FutbolNova","Could not persist match checkpoint",error);
            return false;
        }
    }

    public static MatchRuntimeSnapshot load(){
        if(Gdx.files==null)return null;
        FileHandle file=Gdx.files.local(SAVE_PATH);
        if(!file.exists())return null;
        try(ObjectInputStream input=new ObjectInputStream(file.read())){
            Object value=input.readObject();
            if(!(value instanceof MatchRuntimeSnapshot))return null;
            MatchRuntimeSnapshot snapshot=(MatchRuntimeSnapshot)value;
            return snapshot.isValid()?snapshot:null;
        }catch(IOException|ClassNotFoundException|RuntimeException error){
            if(Gdx.app!=null)Gdx.app.error("FutbolNova","Saved match is unreadable; leaving current match untouched",error);
            return null;
        }
    }

    public static void clear(){
        if(Gdx.files==null)return;
        FileHandle file=Gdx.files.local(SAVE_PATH),temp=Gdx.files.local(TEMP_PATH);
        if(file.exists())file.delete();
        if(temp.exists())temp.delete();
    }
}
