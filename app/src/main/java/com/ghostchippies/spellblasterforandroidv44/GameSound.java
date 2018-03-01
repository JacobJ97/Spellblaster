package com.ghostchippies.spellblasterforandroidv44;

import android.content.Context;
import android.media.SoundPool;

class GameSound {

    private SoundPool pool;
    private SoundPool poolKitKat;
    private Context context;

    GameSound(Context context) {
        this.context = context;


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder builder;
            builder = new SoundPool.Builder();
            builder.setMaxStreams(10);
            pool = builder.build();
        }
        else {
            poolKitKat = new SoundPool(10, 10, 10);
        }
    }

    int addSound(int resourceID) {
        return pool.load(context, resourceID, 1);
    }

    int addSoundKitKat(int resourceID) {return poolKitKat.load(context, resourceID, 1); }

    void play(int soundID) {
        pool.play(soundID, 1, 1, 1, 0, 1);
    }

    void playKitKat(int soundID) {poolKitKat.play(soundID, 1, 1, 1, 0, 1); }
}
