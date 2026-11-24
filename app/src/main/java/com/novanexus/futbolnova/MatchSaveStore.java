package com.novanexus.futbolnova;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

/** Atomic local-file storage for automatic match recovery on Android. */
public final class MatchSaveStore {
    private static final String SAVE_PATH="FutbolNova/saves/current-match.json";
    private static final String TEMP_PATH="FutbolNova/saves/current-match.json.tmp";
    private MatchSaveStore() {}

    public static boolean save(MatchRuntimeSnapshot snapshot){
        if(Gdx.files==null||snapshot==null||!snapshot.isValid())return false;
        try{
            Json json=new Json();json.setOutputType(JsonWriter.OutputType.json);
            String content=json.toJson(snapshot);
            FileHandle target=Gdx.files.local(SAVE_PATH),temp=Gdx.files.local(TEMP_PATH);
            target.parent().mkdirs();
            temp.writeString(content,false,"UTF-8");
            if(target.exists())target.delete();
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
        try{
            Json json=new Json();json.setIgnoreUnknownFields(false);
            MatchRuntimeSnapshot snapshot=json.fromJson(MatchRuntimeSnapshot.class,file);
            return snapshot!=null&&snapshot.isValid()?snapshot:null;
        }catch(RuntimeException error){
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
