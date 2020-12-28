package com.giftinapp.merchant;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;

public class FontTypeFace extends Activity {

    private Context context;

    public FontTypeFace(Context ctx) {
        // TODO Auto-generated constructor stub
        context = ctx;
    }

    public Typeface roboto_thin(){
        Typeface thin= Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        return thin;
    }

    public Typeface roboto_light(){
        Typeface light= Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        return light;
    }

    public Typeface splashfont(){
        Typeface sfont= Typeface.createFromAsset(context.getAssets(), "Roboto-Light.TTF");
        return sfont;
    }


}
