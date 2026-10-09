package com.edirnehavadurumu.app;

import android.app.*;
import android.os.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.webkit.*;
import android.content.*;
import android.net.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.*;
import org.json.*;
import org.jsoup.*;

public class MainActivity extends Activity {
    static final String API="https://servis.mgm.gov.tr/web/";
    static final String VERSION_URL="https://hkndmrl22-bit.github.io/Edirnehavadurumu2/version.json";
    static final String HERO_URL="https://www.edirne.com.tr/img/assets/upload/place/meric-koprusu-5.jpg?h=800";
        final int NAVY=Color.rgb(4,28,50), CARD=Color.rgb(8,63,101), BLUE=Color.rgb(20,126,232);
    final int TEXT=Color.WHITE, MUTED=Color.rgb(175,198,220), GOLD=Color.rgb(255,194,55);
    ExecutorService ex=Executors.newSingleThreadExecutor();
    ExecutorService imgEx=Executors.newFixedThreadPool(4);
    int districtTab=0;
    int currentScreen=0;
    boolean autoRefreshEnabled=true;
    volatile boolean isLoading=false;
    String selectedDistrictName="Enez";
    String loadError="";
    Handler main=new Handler(Looper.getMainLooper()), timer=new Handler(Looper.getMainLooper());
    Runnable refresh5m;
    LinearLayout page,content,bottomNav;
    TextView pageTitle,status; TextView[] navButtons=new TextView[4];
    ArrayList<Loc> all=new ArrayList<>();
    Loc center;
    String lastUpdate="—";
    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    TextView tv(String s,float z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setTypeface(Typeface.DEFAULT,b?Typeface.BOLD:Typeface.NORMAL);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    GradientDrawable bg(int c,int r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
    GradientDrawable stroke(int c,int sc,int r){GradientDrawable g=bg(c,r);if(settingsPrefs().getBoolean("high_contrast",false))sc=Color.rgb(75,180,235);g.setStroke(dp(1),sc);return g;}
    LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    LinearLayout.LayoutParams mp(){return new LinearLayout.LayoutParams(-1,-2);}
    LinearLayout.LayoutParams w(int width){return new LinearLayout.LayoutParams(dp(width),-1);}
    @Override public void onCreate(Bundle b){super.onCreate(b); autoRefreshEnabled=settingsPrefs().getBoolean("auto_refresh",true); selectedDistrictName=settingsPrefs().getString("selected_district","Enez"); getWindow().setStatusBarColor(NAVY); getWindow().setNavigationBarColor(Color.rgb(5,20,34)); getWindow().getDecorView().setOnApplyWindowInsetsListener((v,insets)->{ int nav=0; if(Build.VERSION.SDK_INT>=30) nav=insets.getInsets(WindowInsets.Type.navigationBars()).bottom; else if(Build.VERSION.SDK_INT>=23) nav=insets.getSystemWindowInsetBottom(); if(bottomNav!=null){ LinearLayout.LayoutParams np=(LinearLayout.LayoutParams)bottomNav.getLayoutParams(); np.bottomMargin=nav; bottomNav.setLayoutParams(np); } return insets; }); refresh5m=()->{if(autoRefreshEnabled)load();timer.postDelayed(refresh5m,300000);};buildShell();showHome();load();timer.postDelayed(refresh5m,300000);checkForUpdate();}

    void buildShell(){
        page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setBackgroundColor(NAVY);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        content=col();content.setPadding(0,0,0,0);scroll.addView(content);
        page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        bottomNav=nav();
        page.addView(bottomNav,new LinearLayout.LayoutParams(-1,dp(52)));
        setContentView(page);
    }

    LinearLayout nav(){
        LinearLayout n=row();n.setGravity(Gravity.CENTER);n.setPadding(dp(6),dp(2),dp(6),dp(2));n.setBackground(bg(Color.rgb(8,38,68),0));
        String[] labels={"⌂\nAna Sayfa","●\nİlçeler","⚠\nUyarılar","⚙\nAyarlar"};
        for(int i=0;i<4;i++){final int k=i;TextView b=tv(labels[i],14,Color.rgb(225,238,250),true);b.setGravity(Gravity.CENTER);b.setClickable(true);b.setFocusable(true);b.setMinHeight(dp(46));b.setPadding(0,dp(1),0,dp(1));navButtons[i]=b;b.setOnClickListener(v->{if(k==0)showHome();else if(k==1)showDistricts();else if(k==2)showWarnings();else showSettings();});LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(46),1);p.setMargins(dp(3),0,dp(3),0);n.addView(b,p);} n.setClickable(true);n.bringToFront();return n;
    }
    void setNavActive(int active){
        for(int i=0;i<navButtons.length;i++) if(navButtons[i]!=null) navButtons[i].setBackground(i==active?bg(Color.rgb(18,122,235),18):null);
    }

    void header(String title,boolean back,boolean gear){
        LinearLayout h=row();h.setGravity(Gravity.CENTER_VERTICAL);h.setPadding(0,dp(14),0,dp(10));
        if(back){TextView b=tv("‹",34,TEXT,false);b.setGravity(Gravity.CENTER);b.setOnClickListener(v->showHome());h.addView(b,new LinearLayout.LayoutParams(dp(42),dp(46)));}
        LinearLayout tt=col();tt.addView(tv(title,21,TEXT,true));if(title.equals("EDİRNE"))tt.addView(tv("YEREL HAVA TAHMİN UYGULAMASI",10,MUTED,true));
        h.addView(tt,new LinearLayout.LayoutParams(0,-2,1));
        if(gear){TextView g=tv("⚙",24,TEXT,false);g.setGravity(Gravity.CENTER);g.setOnClickListener(v->showSettings());h.addView(g,new LinearLayout.LayoutParams(dp(44),dp(46)));}
        content.addView(h,mp());
    }

    View logoHero(){
        LinearLayout hero=col();hero.setPadding(dp(16),dp(12),dp(16),dp(14));
        GradientDrawable gd=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(25,86,128),Color.rgb(8,31,61)});gd.setCornerRadius(dp(24));hero.setBackground(gd);
        LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.edirne_logo_real);logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        top.addView(logo,new LinearLayout.LayoutParams(dp(92),dp(92)));
        LinearLayout tx=col();tx.setPadding(dp(12),0,0,0);tx.addView(tv("EDİRNE",29,TEXT,true));tx.addView(tv("YEREL HAVA TAHMİN UYGULAMASI",12,MUTED,true));top.addView(tx,new LinearLayout.LayoutParams(0,-2,1));
        hero.addView(top);
        TextView loc=tv("⌖  Edirne Merkez",16,TEXT,true);loc.setPadding(0,dp(8),0,dp(2));hero.addView(loc);
        hero.addView(tv("Son güncelleme: "+currentTime(),10,MUTED,false));
        return hero;
    }

    void showHome(){
        currentScreen=0;
        content.removeAllViews();setNavActive(0);content.setPadding(0,0,0,0);

        // MASTER ANA SAYFA — renk, oran, kart ve yazı hiyerarşisi 5223.png esas alınmıştır.
        FrameLayout hero=new FrameLayout(this);
        hero.setBackgroundColor(Color.rgb(3,28,48));
        ImageView photo=new ImageView(this);
        photo.setImageDrawable(null);
        photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        hero.addView(photo,new FrameLayout.LayoutParams(-1,dp(296)));
        loadHeroPhoto(photo);

        // Fotoğrafın üstündeki başlık alanı: masterdaki gibi fotoğrafın üzerinde.
        LinearLayout overlay=col();overlay.setPadding(dp(10),dp(18),dp(10),dp(8));
        GradientDrawable ov=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.argb(45,0,0,0),Color.argb(30,0,0,0)});
        overlay.setBackground(ov);
        FrameLayout.LayoutParams op=new FrameLayout.LayoutParams(-1,dp(296),Gravity.TOP);
        hero.addView(overlay,op);

        // Fotoğraf üzerindeki "EDİRNE HAVA DURUMU" başlığı kaldırıldı.
        SimpleDateFormat photoClock=new SimpleDateFormat("HH:mm",new Locale("tr","TR"));
        photoClock.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));
        TextView photoDate=tv(trDate()+"  •  "+photoClock.format(new Date()),12,Color.WHITE,true);
        photoDate.setGravity(Gravity.CENTER);
        photoDate.setShadowLayer(dp(3),0,dp(1),Color.BLACK);
        FrameLayout.LayoutParams datep=new FrameLayout.LayoutParams(-1,dp(30),Gravity.BOTTOM);
        datep.setMargins(dp(8),0,dp(8),dp(8));
        hero.addView(photoDate,datep);
        content.addView(hero,mp());

        if(center==null){
            TextView loading=tv(loadError.isEmpty()?"Veriler yükleniyor…":loadError,15,MUTED,true);
            loading.setPadding(dp(18),dp(18),dp(18),dp(18));
            loading.setOnClickListener(v->{loadError="";loading.setText("MGM verileri yeniden alınıyor…");load();});
            content.addView(loading,mp());return;
        }

        // Güncel durum kartı
        LinearLayout weather=col();weather.setPadding(dp(10),dp(6),dp(10),dp(6));
        weather.setBackground(stroke(Color.rgb(5,68,108),Color.rgb(25,113,174),18));
        LinearLayout wh=row();wh.setGravity(Gravity.CENTER_VERTICAL);
        wh.addView(tv("EDİRNE MERKEZ",16,Color.rgb(231,242,250),true),
                new LinearLayout.LayoutParams(0,dp(32),1));
        LinearLayout refresh=row();refresh.setGravity(Gravity.CENTER);refresh.setPadding(dp(8),0,dp(10),0);
        refresh.setBackground(stroke(BLUE,Color.rgb(45,153,255),18));
        TextView refreshIcon=tv("⟳",25,TEXT,true);refreshIcon.setGravity(Gravity.CENTER);
        refresh.addView(refreshIcon,new LinearLayout.LayoutParams(dp(30),dp(30)));
        TextView refreshLabel=tv("Verileri Yenile",11.5f,TEXT,true);refreshLabel.setSingleLine(true);
        refresh.addView(refreshLabel,new LinearLayout.LayoutParams(-2,dp(30)));
        refresh.setClickable(true);refresh.setFocusable(true);
        refresh.setOnClickListener(v->{
            if(isLoading){Toast.makeText(this,"Veriler zaten güncelleniyor.",Toast.LENGTH_SHORT).show();return;}
            refreshLabel.setText("Yenileniyor…");refreshIcon.setText("⟳");load();
        });
        wh.addView(refresh,new LinearLayout.LayoutParams(dp(158),dp(32)));
        weather.addView(wh);

        LinearLayout main=row();main.setGravity(Gravity.TOP);
        LinearLayout cur=col();cur.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        LinearLayout cr=row();cr.setGravity(Gravity.CENTER);
        cr.addView(weatherIconView(center.nowEvent,42),new LinearLayout.LayoutParams(dp(48),dp(48)));
        TextView bt=tv(tempC(center.now,"—"),34,GOLD,true);
        bt.setSingleLine(true);bt.setIncludeFontPadding(false);
        cr.addView(bt,new LinearLayout.LayoutParams(dp(126),dp(52)));
        cur.addView(cr,new LinearLayout.LayoutParams(-1,dp(54)));
        TextView cond=tv(val(center.nowEvent,"—"),13,TEXT,true);cond.setGravity(Gravity.CENTER);cond.setIncludeFontPadding(false);
        cur.addView(cond,new LinearLayout.LayoutParams(-1,dp(20)));
        TextView upd=tv("Son Güncelleme: "+lastUpdate,10.5f,TEXT,true);
        upd.setGravity(Gravity.CENTER);upd.setIncludeFontPadding(false);upd.setSingleLine(true);
        upd.setEllipsize(android.text.TextUtils.TruncateAt.END);
        cur.addView(upd,new LinearLayout.LayoutParams(-1,dp(25)));
        main.addView(cur,new LinearLayout.LayoutParams(0,dp(103),0.50f));

        LinearLayout met=col();met.setPadding(0,0,0,0);
        LinearLayout r1=row();r1.setGravity(Gravity.CENTER_VERTICAL);
        r1.addView(metricCompact("🌡 Hissedilen",tempC(center.feels,"—")),new LinearLayout.LayoutParams(0,dp(70),1));
        r1.addView(metricCompact("💧 Nem",unitValue(center.humidity,"%")),new LinearLayout.LayoutParams(0,dp(70),1));met.addView(r1);
        LinearLayout r2=row();r2.setGravity(Gravity.CENTER_VERTICAL);
        r2.addView(metricCompact("🌬 Rüzgâr",unitValue(center.wind," km/sa")+" "+windArrow(center.windDir)+" "+windDirection(center.windDir)),new LinearLayout.LayoutParams(0,dp(70),1));
        r2.addView(metricCompact("⏱ Basınç",unitValue(center.pressure," hPa")),new LinearLayout.LayoutParams(0,dp(70),1));met.addView(r2);
        main.addView(met,new LinearLayout.LayoutParams(0,dp(140),0.50f));
        weather.addView(main);

        LinearLayout.LayoutParams wp=mp();wp.setMargins(dp(8),dp(6),dp(8),0);content.addView(weather,wp);

        // Saatlik tahmin — 5 günlük tahminin üstünde.
        LinearLayout hourly=col();hourly.setPadding(dp(6),dp(5),dp(6),dp(5));
        hourly.setBackground(stroke(Color.rgb(5,68,108),Color.rgb(25,113,174),18));
        sectionLabel(hourly,"SAATLİK TAHMİNLER (EDİRNE MERKEZ)");
        LinearLayout hr=row();int hc=0;
        if(center.hours.isEmpty()){
            TextView unavailable=tv("Saatlik tahmin şu anda alınamıyor.",11,MUTED,false);
            unavailable.setGravity(Gravity.CENTER);
            hr.addView(unavailable,new LinearLayout.LayoutParams(-1,dp(82)));
        }
        for(Hour h:center.hours){
            hr.addView(hourCardFlex(h),new LinearLayout.LayoutParams(0,dp(82),1));
            if(++hc>=6)break;
        }
        hourly.addView(hr,new LinearLayout.LayoutParams(-1,dp(82)));
        LinearLayout.LayoutParams hp=mp();hp.setMargins(dp(8),dp(4),dp(8),0);content.addView(hourly,hp);


        // 5 günlük tahmin — saatliğin hemen altında.
        LinearLayout forecast5=col();
        forecast5.setPadding(dp(6),dp(5),dp(6),dp(6));
        forecast5.setBackground(stroke(Color.rgb(5,68,108),Color.rgb(25,113,174),18));
        sectionLabel(forecast5,"5 GÜNLÜK TAHMİN (EDİRNE MERKEZ)");
        LinearLayout days=row();int n=0;
        if(center.days.isEmpty()){
            TextView unavailable=tv("5 günlük tahmin şu anda alınamıyor.",11,MUTED,false);
            unavailable.setGravity(Gravity.CENTER);
            days.addView(unavailable,new LinearLayout.LayoutParams(-1,dp(118)));
        }
        for(Day d:center.days){
            if(d.observedOnly || isCurrentForecastDay(d.date)) continue;
            days.addView(dayCardFlex(d),new LinearLayout.LayoutParams(0,dp(126),1));
            if(++n>=5)break;
        }
        forecast5.setPadding(dp(6),dp(5),dp(6),dp(6));
        forecast5.addView(days,new LinearLayout.LayoutParams(-1,dp(126)));
        LinearLayout.LayoutParams dp5=mp();dp5.setMargins(dp(8),dp(4),dp(8),0);content.addView(forecast5,dp5);
    }

    View hourCardFlex(Hour h){
        LinearLayout c=col();c.setGravity(Gravity.CENTER_HORIZONTAL);c.setPadding(dp(2),dp(2),dp(2),dp(2));
        c.setBackground(stroke(Color.rgb(7,55,88),Color.rgb(16,91,137),14));
        TextView tm=tv(h.time,12,TEXT,true);tm.setGravity(Gravity.CENTER);tm.setIncludeFontPadding(false);
        c.addView(tm,new LinearLayout.LayoutParams(-1,dp(21)));
        c.addView(weatherIconView(h.event,18),new LinearLayout.LayoutParams(-1,dp(28)));
        TextView te=tv(h.temp+"°",17,TEXT,true);te.setGravity(Gravity.CENTER);te.setIncludeFontPadding(false);
        c.addView(te,new LinearLayout.LayoutParams(-1,dp(23)));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-1);p.setMargins(dp(2),0,dp(2),0);
        c.setLayoutParams(p);return c;
    }

    View dayCardFlex(Day d){
        LinearLayout c=col();c.setGravity(Gravity.CENTER_HORIZONTAL);c.setPadding(dp(2),dp(3),dp(2),dp(3));
        c.setBackground(stroke(Color.rgb(7,58,94),Color.rgb(24,111,171),13));
        TextView dl=tv(dayLabel(d.date),11.5f,TEXT,true);dl.setGravity(Gravity.CENTER);dl.setIncludeFontPadding(false);dl.setSingleLine(true);dl.setEllipsize(android.text.TextUtils.TruncateAt.END);
        c.addView(dl,new LinearLayout.LayoutParams(-1,dp(19)));
        TextView wd=tv(weekday(d.date),10.5f,MUTED,true);wd.setGravity(Gravity.CENTER);wd.setIncludeFontPadding(false);wd.setSingleLine(true);
        c.addView(wd,new LinearLayout.LayoutParams(-1,dp(18)));
        FrameLayout iconSlot=new FrameLayout(this);
        iconSlot.addView(weatherIconView(d.e,16),new FrameLayout.LayoutParams(dp(34),dp(28),Gravity.CENTER));
        c.addView(iconSlot,new LinearLayout.LayoutParams(-1,dp(29)));
        TextView ev=tv(d.e,7.2f,TEXT,true);ev.setGravity(Gravity.CENTER);ev.setIncludeFontPadding(false);ev.setMaxLines(1);ev.setSingleLine(true);ev.setEllipsize(android.text.TextUtils.TruncateAt.END);
        c.addView(ev,new LinearLayout.LayoutParams(-1,dp(18)));
        LinearLayout temps=row();temps.setGravity(Gravity.CENTER);
        TextView hi=tv(d.observedOnly?"Anlık":unitValue(d.ma,"°"),d.observedOnly?10.5f:12f,Color.rgb(255,120,100),true);hi.setGravity(Gravity.CENTER);hi.setIncludeFontPadding(false);hi.setSingleLine(true);
        TextView lo=tv(d.observedOnly?unitValue(d.ma,"°"):unitValue(d.mi,"°"),12f,Color.rgb(110,195,255),true);lo.setGravity(Gravity.CENTER);lo.setIncludeFontPadding(false);lo.setSingleLine(true);
        temps.addView(hi,new LinearLayout.LayoutParams(0,dp(19),1));
        temps.addView(lo,new LinearLayout.LayoutParams(0,dp(19),1));
        c.addView(temps,new LinearLayout.LayoutParams(-1,dp(19)));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-1);p.setMargins(dp(1),0,dp(1),0);c.setLayoutParams(p);return c;
    }

    void sectionLabel(LinearLayout parent,String s){
        TextView t=tv(s,13,Color.rgb(210,229,246),true);t.setPadding(0,dp(3),0,dp(4));parent.addView(t,mp());
    }

    String trDate(){
        String[] gun={"Pazar","Pazartesi","Salı","Çarşamba","Perşembe","Cuma","Cumartesi"};
        TimeZone zone=TimeZone.getTimeZone("Europe/Istanbul");
        Calendar c=Calendar.getInstance(zone);
        SimpleDateFormat format=new SimpleDateFormat("d MMMM yyyy",new Locale("tr","TR"));format.setTimeZone(zone);
        return format.format(c.getTime())+" "+gun[c.get(Calendar.DAY_OF_WEEK)-1];
    }
    String weekday(String d){
        try{
            Date x;
            try{x=new SimpleDateFormat("dd MMMM yyyy",new Locale("tr","TR")).parse(d);}
            catch(Exception e){
                x=new SimpleDateFormat("dd MMMM",new Locale("tr","TR")).parse(d);
                Calendar parsed=Calendar.getInstance();parsed.setTime(x);
                parsed.set(Calendar.YEAR,Calendar.getInstance().get(Calendar.YEAR));x=parsed.getTime();
            }
            Calendar cal=Calendar.getInstance();cal.setTime(x);
            String[] gun={"Pazar","Pazartesi","Salı","Çarşamba","Perşembe","Cuma","Cumartesi"};
            return gun[cal.get(Calendar.DAY_OF_WEEK)-1];
        }catch(Exception e){return "";}
    }
    String tempC(String s,String def){
        if(s==null||s.trim().isEmpty())return def;
        String x=s.trim().replace("°C","").replace("°","").trim();
        return x+"°C";
    }
    String unitValue(String value,String unit){
        if(value==null||value.trim().isEmpty())return "—";
        return value.trim()+unit;
    }
    void sectionPanel(String s){
        TextView t=tv(s,17,Color.rgb(210,229,246),true);t.setPadding(dp(19),dp(5),dp(19),dp(4));content.addView(t,mp());
    }

    View metric(String a,String b){
        String iconText=a, label="";
        int sp=a.indexOf(" ");
        if(sp>0){iconText=a.substring(0,sp);label=a.substring(sp+1);}
        LinearLayout card=row();card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(2),dp(3),dp(2),dp(3));
        card.setBackground(stroke(Color.rgb(10,63,98),Color.rgb(25,104,154),15));
        TextView ic=tv(iconText,22,TEXT,false);ic.setGravity(Gravity.CENTER);ic.setIncludeFontPadding(false);
        card.addView(ic,new LinearLayout.LayoutParams(dp(28),-1));
        LinearLayout info=col();info.setGravity(Gravity.CENTER_VERTICAL);
        TextView la=tv(label,9.5f,TEXT,true);la.setIncludeFontPadding(false);la.setSingleLine(true);la.setEllipsize(android.text.TextUtils.TruncateAt.END);
        TextView va=tv(b,12f,TEXT,true);va.setIncludeFontPadding(false);va.setMaxLines(2);va.setGravity(Gravity.CENTER_VERTICAL);
        info.addView(la,new LinearLayout.LayoutParams(-1,dp(18)));
        info.addView(va,new LinearLayout.LayoutParams(-1,dp(30)));
        card.addView(info,new LinearLayout.LayoutParams(0,-1,1));
        return card;
    }
         View metricCompact(String a,String b){
         String iconText=a,label="";
         int sp=a.indexOf(" ");
         if(sp>0){iconText=a.substring(0,sp);label=a.substring(sp+1);}
         LinearLayout card=row();card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(1),dp(2),dp(1),dp(2));
         card.setBackground(stroke(Color.rgb(10,63,98),Color.rgb(25,104,154),15));
         TextView ic=tv(iconText,18,TEXT,false);ic.setGravity(Gravity.CENTER);ic.setIncludeFontPadding(false);
         card.addView(ic,new LinearLayout.LayoutParams(dp(22),-1));
         LinearLayout info=col();info.setGravity(Gravity.CENTER_VERTICAL);
         TextView la=tv(label,10f,TEXT,true);la.setIncludeFontPadding(false);la.setSingleLine(true);la.setEllipsize(android.text.TextUtils.TruncateAt.END);
         TextView va=tv(b,11f,TEXT,true);va.setIncludeFontPadding(false);va.setSingleLine(true);va.setEllipsize(android.text.TextUtils.TruncateAt.END);va.setGravity(Gravity.CENTER_VERTICAL);
         info.addView(la,new LinearLayout.LayoutParams(-1,dp(20)));
         info.addView(va,new LinearLayout.LayoutParams(-1,dp(29)));
         card.addView(info,new LinearLayout.LayoutParams(0,-1,1));
         return card;
     }
View metricDetail(String a,String b){
        String iconText=a,label="";
        int sp=a.indexOf(" ");
        if(sp>0){iconText=a.substring(0,sp);label=a.substring(sp+1);}
        LinearLayout card=row();card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(4),dp(2),dp(4),dp(2));
        card.setBackground(stroke(Color.rgb(10,63,98),Color.rgb(25,104,154),15));
        TextView ic=tv(iconText,19,TEXT,false);ic.setGravity(Gravity.CENTER);ic.setIncludeFontPadding(false);
        card.addView(ic,new LinearLayout.LayoutParams(dp(25),-1));
        LinearLayout info=col();info.setGravity(Gravity.CENTER_VERTICAL);
        TextView la=tv(label,10.5f,TEXT,true);la.setIncludeFontPadding(false);la.setSingleLine(true);la.setEllipsize(android.text.TextUtils.TruncateAt.END);
        TextView va=tv(b,14.5f,TEXT,true);va.setIncludeFontPadding(false);va.setSingleLine(true);va.setEllipsize(android.text.TextUtils.TruncateAt.END);va.setGravity(Gravity.CENTER_VERTICAL);
        info.addView(la,new LinearLayout.LayoutParams(-1,dp(17)));
        info.addView(va,new LinearLayout.LayoutParams(-1,dp(24)));
        card.addView(info,new LinearLayout.LayoutParams(0,-1,1));
        return card;
    }
void section(String s){TextView t=tv(s,17,Color.rgb(205,224,244),true);t.setPadding(dp(2),dp(18),dp(2),dp(9));content.addView(t,mp());}

void showDistricts(){ showDistrictsTab(0); }

    void showDistrictsTab(int tab){
        currentScreen=1;
        districtTab=tab;
        content.removeAllViews();setNavActive(1);
        // Safe top inset for Android edge-to-edge status bar; keeps controls clear of clock/battery.
        content.setPadding(dp(12),dp(34),dp(12),dp(16));

        LinearLayout body=col();
        content.addView(body,mp());
        Loc selected=findSelectedDistrict();
        if(selected==null){
            TextView loading=tv(loadError.isEmpty()?"İlçe verileri yükleniyor…":loadError,14,MUTED,false);
            loading.setPadding(dp(12),dp(16),dp(12),dp(16));
            loading.setOnClickListener(v->{loadError="";loading.setText("MGM ilçe verileri yeniden alınıyor…");load();});
            body.addView(loading,mp());return;
        }
        if(tab==0)renderDistrictCurrent(body,selected);
        else renderDistrictForecast(body,selected);
    }

    Loc findSelectedDistrict(){
        for(int i=1;i<all.size();i++)if(all.get(i).name.equals(selectedDistrictName))return all.get(i);
        if(all.size()>1){selectedDistrictName=all.get(1).name;return all.get(1);}
        return null;
    }

    void selectDistrict(Loc l){
        if(l==null)return;
        selectedDistrictName=l.name;
        showDistrictsTab(districtTab);
    }

    View districtSegment(String label,boolean active,Runnable click){
        TextView t=tv(label,13,TEXT,true);t.setGravity(Gravity.CENTER);
        t.setPadding(dp(4),dp(11),dp(4),dp(11));
        t.setBackground(active?bg(BLUE,13):bg(Color.rgb(11,48,76),13));
        t.setOnClickListener(v->click.run());
        return t;
    }

    

void refreshDistricts(int tab,TextView button){
        if(isLoading){button.setText("⟳  Başka bir güncelleme sürüyor…");return;}
        isLoading=true;
        button.setText("⟳  Güncelleniyor…");
        ex.execute(()->{
            try{
                Loc cen=apiLocation("Edirne Merkez","merkez");
                String[] D={"Enez","Havsa","İpsala","Keşan","Lalapaşa","Meriç","Süloğlu","Uzunköprü"};
                String[] Q={"ENEZ","HAVSA","IPSALA","KESAN","LALAPASA","MERIC","SULOGLU","UZUNKOPRU"};
                ArrayList<Loc> tmp=new ArrayList<>();tmp.add(cen);
                for(int i=0;i<D.length;i++){
                    try{tmp.add(apiLocation(D[i],Q[i].toLowerCase(Locale.ROOT)));}
                    catch(Exception ignored){Loc missing=new Loc(D[i]);missing.nowEvent="Veri alınamadı";missing.lastUpdate="—";tmp.add(missing);}
                }
                main.post(()->{
                    center=cen;all=tmp;lastUpdate=cen.lastUpdate;
                    if(currentScreen==1)showDistrictsTab(tab);
                    else if(currentScreen==0)showHome();
                });
            }catch(Exception e){
                main.post(()->button.setText("⟳  Güncelleme başarısız — tekrar dene"));
            }finally{isLoading=false;}
        });
    }

    void renderDistrictCurrent(LinearLayout body,Loc selected){
        renderDistrictLayout(body,selected,false);
    }

    

void districtHeader(){
        LinearLayout collage=col();
        collage.setBackground(bg(Color.rgb(3,28,48),0));
        String[] names={"Enez","Havsa","İpsala","Keşan","Lalapaşa","Meriç","Süloğlu","Uzunköprü"};
        LinearLayout r1=row(),r2=row();
        for(int i=0;i<8;i++){
            LinearLayout cell=photoCollageCell(names[i]);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(70),1);
            cp.setMargins(dp(1),dp(1),dp(1),dp(1));
            if(i<4)r1.addView(cell,cp);else r2.addView(cell,cp);
        }
        collage.addView(r1,new LinearLayout.LayoutParams(-1,dp(72)));
        collage.addView(r2,new LinearLayout.LayoutParams(-1,dp(72)));
        content.addView(collage,new LinearLayout.LayoutParams(-1,dp(144)));
    }

    LinearLayout photoCollageCell(String name){
        LinearLayout cell=col();cell.setGravity(Gravity.BOTTOM);
        cell.setBackground(bg(Color.rgb(8,63,101),0));
        FrameLayout frame=new FrameLayout(this);
        ImageView img=new ImageView(this);img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        frame.addView(img,new FrameLayout.LayoutParams(-1,-1));
        loadRemoteImage(img,districtPhotoUrl(name));
        View sh=new View(this);
        GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{Color.argb(15,0,0,0),Color.argb(185,3,28,48)});
        sh.setBackground(g);frame.addView(sh,new FrameLayout.LayoutParams(-1,-1));
        TextView label=tv(name.toUpperCase(new Locale("tr","TR")),10,TEXT,true);
        label.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);label.setPadding(dp(2),0,dp(2),dp(5));
        frame.addView(label,new FrameLayout.LayoutParams(-1,-1,Gravity.BOTTOM));
        cell.addView(frame,new LinearLayout.LayoutParams(-1,-1));
        return cell;
    }

    View districtMiniCard(Loc l,boolean active){
        LinearLayout card=col();
        card.setPadding(dp(12),dp(6),dp(8),dp(5));
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(active?stroke(Color.rgb(18,122,235),Color.rgb(72,178,255),14):bg(CARD,14));
        TextView name=tv(l.name,20,TEXT,true);
        name.setSingleLine(true);
        card.addView(name,new LinearLayout.LayoutParams(-1,dp(28)));
        LinearLayout info=row();info.setGravity(Gravity.CENTER_VERTICAL);
        TextView temp=tv(tempC(l.now,"—"),15,GOLD,true);
        info.addView(temp,new LinearLayout.LayoutParams(dp(76),dp(31)));
        TextView ev=tv(val(l.nowEvent,"—"),10.5f,Color.rgb(225,240,250),true);
        ev.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);ev.setMaxLines(1);ev.setEllipsize(android.text.TextUtils.TruncateAt.END);
        info.addView(ev,new LinearLayout.LayoutParams(0,dp(31),1));
        card.addView(info,new LinearLayout.LayoutParams(-1,dp(31)));
        TextView upd=tv("Son güncelleme: "+val(l.lastUpdate,"—"),8.2f,MUTED,false);
        upd.setSingleLine(true);upd.setEllipsize(android.text.TextUtils.TruncateAt.END);
        card.addView(upd,new LinearLayout.LayoutParams(-1,dp(17)));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(89));p.setMargins(0,0,0,dp(7));card.setLayoutParams(p);
        return card;
    }

    String districtIcon(String name){
        if(name==null)return "●";
        String n=name.toLowerCase(new Locale("tr","TR"));
        if(n.equals("enez"))return "🏰";
        if(n.equals("havsa"))return "🕌";
        if(n.equals("i̇psala")||n.equals("ipsala"))return "🌾";
        if(n.equals("keşan"))return "🗼";
        if(n.equals("lalapaşa"))return "🏛";
        if(n.equals("meriç"))return "🌉";
        if(n.equals("süloğlu"))return "🌿";
        if(n.equals("uzunköprü"))return "🌉";
        return "📍";
    }

    void renderSelectedDistrict(LinearLayout right,Loc l,boolean forecastOnly){
        if(forecastOnly){
            LinearLayout panel=col();panel.setPadding(dp(10),dp(10),dp(10),dp(8));panel.setBackground(bg(Color.rgb(8,55,88),18));
            LinearLayout title=row();title.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout tt=col();tt.addView(tv(l.name,21,TEXT,true));tt.addView(tv("5 GÜNLÜK TAHMİN",10.5f,MUTED,true));tt.addView(tv("Son güncelleme: "+val(l.lastUpdate,"—"),10,MUTED,false));
            title.addView(tt,new LinearLayout.LayoutParams(-1,-2));
            
            panel.addView(title);
            for(Day d:l.days)panel.addView(dayCompact(d),mp());
            right.addView(panel,mp());return;
        }
        LinearLayout hero=col();hero.setPadding(dp(10),dp(10),dp(10),dp(8));hero.setBackground(bg(Color.rgb(10,59,94),18));
        LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout nm=col();nm.addView(tv(l.name,21,TEXT,true));nm.addView(tv("Son güncelleme: "+val(l.lastUpdate,"—"),10,MUTED,false));nm.addView(tv(val(l.nowEvent,"—"),11,Color.rgb(218,235,249),false));
        top.addView(nm,new LinearLayout.LayoutParams(0,-2,1));
        hero.addView(top);
        TextView temp=tv(tempC(l.now,"—"),31,GOLD,true);temp.setGravity(Gravity.CENTER_VERTICAL);temp.setPadding(dp(58),dp(3),0,dp(3));hero.addView(temp,new LinearLayout.LayoutParams(-1,dp(48)));
        LinearLayout mm=row();mm.setPadding(0,dp(4),0,0);mm.setGravity(Gravity.CENTER_VERTICAL);
        mm.addView(metricCompact("💧 Nem",val(l.humidity,"—")+"%"),new LinearLayout.LayoutParams(0,dp(64),1));
        mm.addView(metricCompact("≋ Rüzgâr",val(l.wind,"—")+" km/sa"),new LinearLayout.LayoutParams(0,dp(64),1));
        mm.addView(metricCompact("◉ Basınç",val(l.pressure,"—")+" hPa"),new LinearLayout.LayoutParams(0,dp(64),1));hero.addView(mm);
        right.addView(hero,mp());
        TextView refresh=tv("⟳  Son Durumu Yenile",13,TEXT,true);refresh.setGravity(Gravity.CENTER);refresh.setBackground(bg(BLUE,20));refresh.setPadding(0,dp(8),0,dp(8));refresh.setOnClickListener(v->refreshDistricts(districtTab,refresh));
        LinearLayout.LayoutParams rp=mp();rp.setMargins(0,dp(8),0,0);right.addView(refresh,rp);
    }

    View dayCompact(Day d){
        LinearLayout c=row();c.setGravity(Gravity.CENTER_VERTICAL);
        c.setPadding(dp(8),dp(5),dp(6),dp(5));c.setBackground(bg(CARD,12));
        LinearLayout a=col();
        a.addView(tv(dayLabel(d.date),12,TEXT,true));
        a.addView(tv(weekday(d.date),10,MUTED,true));
        a.addView(tv(d.e,8.5f,MUTED,false));
        c.addView(a,new LinearLayout.LayoutParams(0,dp(60),1));
        c.addView(weatherIconView(d.e,18),new LinearLayout.LayoutParams(dp(38),dp(60)));
        LinearLayout b=col();b.setGravity(Gravity.CENTER);
        b.addView(tv(d.ma+"°",13,Color.rgb(255,100,90),true));
        b.addView(tv(d.mi+"°",13,Color.rgb(90,190,255),true));
        c.addView(b,new LinearLayout.LayoutParams(dp(40),dp(60)));
        LinearLayout.LayoutParams p=mp();p.setMargins(0,0,0,dp(4));c.setLayoutParams(p);return c;
    }

    View hourCompact(Hour h){
        LinearLayout c=col();c.setGravity(Gravity.CENTER);c.setPadding(dp(3),dp(4),dp(3),dp(4));
        c.setBackground(bg(Color.rgb(19,59,91),12));
        c.addView(tv(h.time,9,TEXT,true));
        c.addView(weatherIconView(h.event,23),new LinearLayout.LayoutParams(-1,dp(34)));
        c.addView(tv(h.temp+"°",12,TEXT,true));
        return sized(c,dp(62),dp(80));
    }

    String districtPhotoUrl(String name){
        if(name==null)return "";
        String n=name.toLowerCase(new Locale("tr","TR"));
        if(n.equals("enez"))return "https://commons.wikimedia.org/wiki/Special:Redirect/file/Enez%20-%20panoramio%20%281%29.jpg";
        if(n.equals("havsa"))return "https://commons.wikimedia.org/wiki/Special:Redirect/file/Fatih%20Caddesi%2C%20Havsa.jpg";
        if(n.equals("i̇psala")||n.equals("ipsala"))return "https://commons.wikimedia.org/wiki/Special:Redirect/file/Pasakoy%20fields%2020230624.jpg";
        if(n.equals("keşan"))return "https://commons.wikimedia.org/wiki/Special:Redirect/file/Kesan%20Turkey.JPG";
        if(n.equals("lalapaşa"))return "https://commons.wikimedia.org/wiki/Special:Redirect/file/HamzabeyliKapisi.jpg";
        if(n.equals("meriç"))return "https://commons.wikimedia.org/wiki/Special:Redirect/file/Meri%C3%A7%20Nehri%20ve%20Meri%C3%A7%20K%C3%B6pr%C3%BCs%C3%BC%202015.jpg";
        if(n.equals("süloğlu"))return "https://foto.haberler.com/haber/2021/06/07/edirne-de-kanocular-normallesme-surecinin-ilk-14183804_amp.jpg";
        if(n.equals("uzunköprü"))return "https://commons.wikimedia.org/wiki/Special:Redirect/file/Uzunk%C3%B6pr%C3%BC%20%281%29.jpg";
        return "https://commons.wikimedia.org/wiki/Special:Redirect/file/Uzunk%C3%B6pr%C3%BC%20%281%29.jpg";
    }

    void loadRemoteImage(ImageView target,String url){
        if(url==null||url.isEmpty())return;
        imgEx.execute(()->{
            try{
                String key="district_"+Integer.toHexString(url.hashCode())+".img";
                java.io.File cache=new java.io.File(getCacheDir(),key);
                if(!cache.exists()){
                    java.net.URL u=new java.net.URL(url);
                    java.net.HttpURLConnection c=(java.net.HttpURLConnection)u.openConnection();
                    c.setConnectTimeout(8000);c.setReadTimeout(12000);c.setInstanceFollowRedirects(true);
                    c.setRequestProperty("User-Agent","EdirneHavaDurumu/10.73");
                    java.io.InputStream in=c.getInputStream();
                    java.io.FileOutputStream out=new java.io.FileOutputStream(cache);
                    byte[] buf=new byte[16384];int n;
                    while((n=in.read(buf))!=-1)out.write(buf,0,n);
                    out.close();in.close();c.disconnect();
                }
                final Bitmap b=BitmapFactory.decodeFile(cache.getAbsolutePath());
                if(b!=null)main.post(()->target.setImageBitmap(b));
            }catch(Exception ignored){}
        });
    }

    View sized(View v,int ww,int hh){v.setLayoutParams(new LinearLayout.LayoutParams(ww,hh));return v;}

    void renderDistrictForecast(LinearLayout body,Loc selected){
        renderDistrictLayout(body,selected,true);
    }

    void renderDistrictLayout(LinearLayout body,Loc selected,boolean forecast){
        if(selected==null){body.addView(tv("İlçe verileri yükleniyor…",14,MUTED,false));return;}

        LinearLayout hero=col();
        hero.setPadding(dp(14),dp(14),dp(14),dp(12));
        GradientDrawable heroBg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(27,87,128),Color.rgb(8,43,70)});
        heroBg.setCornerRadius(dp(22));
        heroBg.setStroke(dp(1),Color.rgb(38,111,159));
        hero.setBackground(heroBg);

        TextView districtTitle=tv(selected.name.toUpperCase(new Locale("tr","TR")),25,TEXT,true);
        districtTitle.setGravity(Gravity.CENTER);
        districtTitle.setSingleLine(true);
        hero.addView(districtTitle);

        LinearLayout summary=row();summary.setGravity(Gravity.CENTER_VERTICAL);
        View icon=weatherIconView(selected.nowEvent,52);
        summary.addView(icon,new LinearLayout.LayoutParams(dp(84),dp(86)));
        TextView temp=tv(tempC(selected.now,"—"),38,GOLD,true);
        temp.setGravity(Gravity.CENTER_VERTICAL);temp.setSingleLine(true);
        summary.addView(temp,new LinearLayout.LayoutParams(0,dp(76),1));
        hero.addView(summary);
        TextView condition=tv(val(selected.nowEvent,"Durum bilgisi yok"),17,TEXT,true);
        condition.setPadding(0,0,0,dp(3));hero.addView(condition);
        LinearLayout updateRow=row();updateRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView updated=tv("Son güncelleme: "+shortDateTime(val(selected.lastUpdate,"—")),10f,MUTED,false);
        updated.setSingleLine(true);updated.setEllipsize(android.text.TextUtils.TruncateAt.END);
        updateRow.addView(updated,new LinearLayout.LayoutParams(0,dp(32),1));
        TextView refresh=tv("⟳  Yenile",11.5f,TEXT,true);refresh.setGravity(Gravity.CENTER);
        refresh.setPadding(dp(9),dp(5),dp(9),dp(5));refresh.setBackground(bg(Color.rgb(15,99,153),12));
        refresh.setOnClickListener(v->refreshDistricts(districtTab,refresh));
        LinearLayout.LayoutParams refreshParams=new LinearLayout.LayoutParams(-2,dp(32));
        refreshParams.setMargins(dp(6),0,0,0);updateRow.addView(refresh,refreshParams);
        hero.addView(updateRow);

        LinearLayout segments=row();segments.setPadding(0,dp(12),0,dp(8));
        View currentTab=districtSegment("ANLIK DURUM",!forecast,()->showDistrictsTab(0));
        View forecastTab=districtSegment("5 GÜNLÜK TAHMİN",forecast,()->showDistrictsTab(1));
        LinearLayout.LayoutParams seg1=new LinearLayout.LayoutParams(0,-2,1);
        LinearLayout.LayoutParams seg2=new LinearLayout.LayoutParams(0,-2,1);
        seg1.setMargins(0,0,dp(4),0);seg2.setMargins(dp(4),0,0,0);
        segments.addView(currentTab,seg1);segments.addView(forecastTab,seg2);hero.addView(segments);

        if(forecast){
            if(selected.days.isEmpty()){
                TextView unavailable=tv("5 günlük MGM tahmini şu anda alınamıyor.",12,MUTED,false);
                unavailable.setGravity(Gravity.CENTER);
                unavailable.setPadding(0,dp(12),0,dp(12));
                hero.addView(unavailable,mp());
            }else{
                // Beş günün tamamı dikey listede görünür; yatay kaydırma gerekmez.
                int count=0;
                for(Day d:selected.days){
                    hero.addView(dayCompact(d),mp());
                    if(++count>=5)break;
                }
            }
        }else{
            LinearLayout grid=col();
            LinearLayout row1=row(),row2=row();
            row1.addView(metricDetail("💧 Nem",unitValue(selected.humidity,"%")),new LinearLayout.LayoutParams(0,dp(64),1));
            row1.addView(metricDetail("≋ Rüzgâr",unitValue(selected.wind," km/sa")),new LinearLayout.LayoutParams(0,dp(64),1));
            LinearLayout.LayoutParams gap=new LinearLayout.LayoutParams(0,dp(64),1);gap.setMargins(dp(5),0,0,0);row1.getChildAt(1).setLayoutParams(gap);
            row2.addView(metricDetail("◉ Basınç",unitValue(selected.pressure," hPa")),new LinearLayout.LayoutParams(0,dp(64),1));
            row2.addView(metricDetail("🌡 Hissedilen",tempC(selected.feels,"—")),new LinearLayout.LayoutParams(0,dp(64),1));
            LinearLayout.LayoutParams gap2=new LinearLayout.LayoutParams(0,dp(64),1);gap2.setMargins(dp(5),0,0,0);row2.getChildAt(1).setLayoutParams(gap2);
            LinearLayout.LayoutParams r1p=mp();r1p.setMargins(0,0,0,dp(5));grid.addView(row1,r1p);grid.addView(row2);
            hero.addView(grid);

            if(!selected.days.isEmpty()&&!selected.days.get(0).observedOnly){
                Day today=selected.days.get(0);
                LinearLayout minmax=row();minmax.setGravity(Gravity.CENTER_VERTICAL);
                minmax.setPadding(dp(10),dp(9),dp(10),dp(9));
                minmax.setBackground(bg(Color.rgb(10,51,81),13));
                TextView lo=tv("↓ En düşük  "+unitValue(today.mi,"°"),13,Color.rgb(117,202,255),true);
                TextView hi=tv("↑ En yüksek  "+unitValue(today.ma,"°"),13,Color.rgb(255,155,105),true);
                minmax.addView(lo,new LinearLayout.LayoutParams(0,-2,1));minmax.addView(hi,new LinearLayout.LayoutParams(0,-2,1));
                LinearLayout.LayoutParams mm=mp();mm.setMargins(0,dp(7),0,0);hero.addView(minmax,mm);
            }
            // MGM saatlik tahmini il merkezleri için sunulur; ilçe ekranında yer almıyor.
        }

                LinearLayout.LayoutParams heroParams=mp();heroParams.setMargins(0,0,0,dp(16));body.addView(hero,heroParams);

        TextView listTitle=tv("DİĞER İLÇELER",14,Color.rgb(210,229,246),true);
        listTitle.setPadding(dp(2),0,0,dp(8));body.addView(listTitle);
        for(int i=1;i<all.size();i++){
            Loc l=all.get(i);if(l.name.equals(selected.name))continue;
            LinearLayout.LayoutParams districtRowParams=mp();districtRowParams.setMargins(0,0,0,dp(8));
            body.addView(districtListRow(l),districtRowParams);
        }
    }

    View districtForecastCard(Day d){
        LinearLayout c=col();c.setGravity(Gravity.CENTER);c.setPadding(dp(4),dp(7),dp(4),dp(7));
        c.setBackground(stroke(Color.rgb(12,58,91),Color.rgb(35,108,153),15));
        TextView date=tv(dayLabel(d.date),10.5f,TEXT,true);date.setGravity(Gravity.CENTER);date.setSingleLine(true);date.setEllipsize(android.text.TextUtils.TruncateAt.END);
        c.addView(date,new LinearLayout.LayoutParams(-1,dp(20)));
        TextView week=tv(weekday(d.date),9.5f,MUTED,true);week.setGravity(Gravity.CENTER);week.setSingleLine(true);week.setEllipsize(android.text.TextUtils.TruncateAt.END);
        c.addView(week,new LinearLayout.LayoutParams(-1,dp(18)));
        c.addView(weatherIconView(d.e,22),new LinearLayout.LayoutParams(-1,dp(36)));
        TextView ev=tv(d.e,8.4f,TEXT,false);ev.setGravity(Gravity.CENTER);ev.setIncludeFontPadding(false);ev.setMaxLines(3);ev.setEllipsize(android.text.TextUtils.TruncateAt.END);
        c.addView(ev,new LinearLayout.LayoutParams(-1,dp(32)));
        TextView hi=tv(d.observedOnly?"Anlık":"↑ "+val(d.ma,"—")+"°",12.5f,Color.rgb(255,135,105),true);hi.setGravity(Gravity.CENTER);
        TextView lo=tv(d.observedOnly?val(d.ma,"—")+"°":"↓ "+val(d.mi,"—")+"°",12.5f,Color.rgb(110,195,255),true);lo.setGravity(Gravity.CENTER);
        c.addView(hi,new LinearLayout.LayoutParams(-1,dp(19)));c.addView(lo,new LinearLayout.LayoutParams(-1,dp(19)));
        return c;
    }

    View districtListRow(Loc l){
        LinearLayout card=row();card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16),dp(10),dp(14),dp(10));
        card.setBackground(bg(Color.rgb(8,55,88),15));
        // Keep district names visually dominant; omit decorative weather icons in the list.
        LinearLayout names=col();names.setGravity(Gravity.CENTER_VERTICAL);
        names.addView(tv(l.name,18,TEXT,true));
        TextView ev=tv(val(l.nowEvent,"Durum bilgisi yok"),11.5f,MUTED,false);
        ev.setSingleLine(true);ev.setEllipsize(android.text.TextUtils.TruncateAt.END);names.addView(ev);
        card.addView(names,new LinearLayout.LayoutParams(0,-2,1));
        TextView temp=tv(tempC(l.now,"—"),18,GOLD,true);temp.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(temp,new LinearLayout.LayoutParams(-2,dp(38)));
        card.setClickable(true);card.setFocusable(true);card.setOnClickListener(v->selectDistrict(l));
                return card;
    }

    

void showWarnings(){
        currentScreen=2;
        content.removeAllViews();setNavActive(2);
        content.setPadding(dp(12),dp(14),dp(12),dp(20));
        header("Uyarılar",true,false);

        LinearLayout panel=col();
        panel.setPadding(dp(14),dp(14),dp(14),dp(14));
        panel.setBackground(stroke(Color.rgb(10,63,98),Color.rgb(25,104,154),18));
        TextView state=tv("Uyarılar kontrol ediliyor…",14,TEXT,true);
        state.setGravity(Gravity.CENTER);
        state.setPadding(dp(8),dp(20),dp(8),dp(20));
        panel.addView(state,mp());
        content.addView(panel,mp());

        LinearLayout days=row();
        TextView today=tv("Bugün",13,TEXT,true);today.setGravity(Gravity.CENTER);
        today.setBackground(bg(BLUE,12));today.setPadding(dp(8),dp(10),dp(8),dp(10));
        TextView tomorrow=tv("Yarın",13,TEXT,true);tomorrow.setGravity(Gravity.CENTER);
        tomorrow.setBackground(bg(Color.rgb(11,48,76),12));tomorrow.setPadding(dp(8),dp(10),dp(8),dp(10));
        LinearLayout.LayoutParams p1=new LinearLayout.LayoutParams(0,dp(42),1);p1.setMargins(0,dp(10),dp(5),0);
        LinearLayout.LayoutParams p2=new LinearLayout.LayoutParams(0,dp(42),1);p2.setMargins(dp(5),dp(10),0,0);
        content.addView(days,mp());days.addView(today,p1);days.addView(tomorrow,p2);

        final int[] selectedDay={1};
        Runnable[] loadAlerts=new Runnable[1];
        loadAlerts[0]=()->{
            state.setText("Kontrol ediliyor…");
            ex.execute(()->{
                String message;
                try{
                    String url="https://www.mgm.gov.tr/Meteouyari/il.aspx?Gun="+selectedDay[0]+"&id=92201";
                    org.jsoup.nodes.Document doc=Jsoup.connect(url).timeout(15000).userAgent("Mozilla/5.0 (Android) EdirneHavaDurumu").get();
                    String pageText=doc.body()==null?"":doc.body().text();
                    String normalized=pageText.toLowerCase(new Locale("tr","TR"));
                    StringBuilder details=new StringBuilder();
                    org.jsoup.select.Elements rows=doc.select("table tr, .alert, .uyari, .warning, [class*=uyari], [class*=Uyari]");
                    for(org.jsoup.nodes.Element el:rows){
                        String t=el.text().trim();
                        if(t.length()>0 && t.length()<500 && !t.toLowerCase(new Locale("tr","TR")).contains("yeşil renkli ilçelerimizde")
                           && !t.equalsIgnoreCase("YEŞİL") && !t.equalsIgnoreCase("SARI")
                           && !t.equalsIgnoreCase("TURUNCU") && !t.equalsIgnoreCase("KIRMIZI")){
                            if(details.length()>0)details.append("\\n\\n");
                            details.append(t);
                        }
                    }
                    boolean noWarning=normalized.contains("herhangi bir meteorolojik uyarı bulunmamaktadır")
                        || (normalized.contains("yeşil renkli ilçelerimizde meteorolojik uyarı olmadığından")
                            && !normalized.contains("edirne için uyarı"));
                    if(noWarning || details.length()==0){
                        message="Şu anda güncel bir uyarı yok.";
                    }else{
                        message=details.toString();
                    }
                }catch(Exception e){message="Uyarı bilgisi şu anda alınamıyor. Tekrar deneyin.";}
                final String result=message;
                main.post(()->{if(currentScreen==2)state.setText(result);});
            });
        };
        today.setOnClickListener(v->{
            selectedDay[0]=1;today.setBackground(bg(BLUE,12));tomorrow.setBackground(bg(Color.rgb(11,48,76),12));loadAlerts[0].run();
        });
        tomorrow.setOnClickListener(v->{
            selectedDay[0]=2;tomorrow.setBackground(bg(BLUE,12));today.setBackground(bg(Color.rgb(11,48,76),12));loadAlerts[0].run();
        });
        loadAlerts[0].run();
    }

    android.content.SharedPreferences settingsPrefs(){return getSharedPreferences("app_settings",MODE_PRIVATE);}

    void showSettings(){
        currentScreen=3;
        content.removeAllViews();setNavActive(3);
        content.setPadding(dp(12),dp(10),dp(12),dp(20));
        header("Ayarlar",false,false);

        section("UYGULAMA TEMASI");
        LinearLayout themeRow=row();themeRow.setGravity(Gravity.CENTER_VERTICAL);
        themeRow.addView(settingsAction("☀","Tema Seçimi","Koyu veya açık tema tercihini yönet",()->showThemeOptions()),new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout.LayoutParams contrastP=new LinearLayout.LayoutParams(0,-2,1);contrastP.setMargins(dp(6),0,0,0);
        themeRow.addView(settingsAction("◉","Karanlık Mod Kontrastı","Koyu temanın okunabilirliği",()->showContrastOptions()),contrastP);
        content.addView(themeRow,mp());

        section("TERCİHLER");
        LinearLayout notifications=settingsGroup();
        notifications.addView(settingsSwitchRow("🔔","Hava Durumu Uyarıları","Şiddetli rüzgâr, yağmur veya kar için bildirim.", "weather_alerts",false));
        notifications.addView(settingsSwitchRow("▣","Günlük Hava Özeti","Her sabah 08:00'de günün hava özeti.", "daily_summary",false));
        content.addView(notifications,mp());

        LinearLayout location=settingsGroup();
        location.addView(settingsActionRow("⌖","Konum Değiştir","Edirne Merkez veya ilçelerini manuel seçin.",()->showLocationPicker()));
        location.addView(settingsSwitchRow("◎","Anlık Konum Kullan","Konumu GPS ile otomatik algıla.", "use_gps",false));
        content.addView(location,mp());

        content.addView(settingsSwitchRow("⟳","Otomatik yenileme (5 dk.)","Hava durumu verilerini otomatik yenile.", "auto_refresh",true));

        section("HAKKINDA");
        LinearLayout about=settingsGroup();
        about.addView(settingsActionRow("ⓘ","Hakkında","Edirne Yerel Hava Tahmin Uygulaması",()->showAboutDialog()));
        about.addView(settingsActionRow("🔒","Gizlilik","Gizlilik ve veri kullanımı hakkında",()->showPrivacyDialog()));
        about.addView(settingsActionRow("▤","Veri Kaynağı / Lisanslar","MGM ve uygulamada kullanılan kaynaklar",()->showSourcesDialog()));
        content.addView(about,mp());

        section("DESTEK VE GERİ BİLDİRİM");
        LinearLayout support=settingsGroup();
        support.addView(settingsActionRow("★","Uygulamayı Puanla","Uygulamayı geliştirmemize yardımcı olun.",()->showRateDialog()));
        support.addView(settingsActionRow("☏","Hata Bildir / Öneri Yap","Görüş ve önerilerinizi bizimle paylaşın.",()->showFeedback()));
        support.addView(settingsActionRow("?","Sıkça Sorulan Sorular","Veri doğruluğu ve güncelleme sıklığı.",()->showFaqDialog()));
        content.addView(support,mp());

        section("SOSYAL MEDYA VE PAYLAŞIM");
        LinearLayout socials=row();socials.setGravity(Gravity.CENTER);
        addSocial(socials,R.drawable.ic_facebook,"https://www.facebook.com/edirnehavadurumu");
        addSocial(socials,R.drawable.ic_instagram,"https://www.instagram.com/edirnehavadurumu/");
        addSocial(socials,R.drawable.ic_x,"https://x.com/edirnehavadurumu");
        addSocial(socials,R.drawable.ic_youtube,"https://www.youtube.com/@edirnehavadurumu");
        content.addView(socials,mp());
        content.addView(settingsActionRow("•","Uygulamayı Paylaş","Edirne Hava Durumu'nu arkadaşlarınızla paylaşın.",()->shareApp()),mp());

        TextView foot=tv("Edirne Yerel Hava Tahmin Uygulaması | Sürüm "+appVersion(),10,MUTED,false);
        foot.setGravity(Gravity.CENTER);foot.setPadding(0,dp(14),0,dp(8));content.addView(foot,mp());
    }

    LinearLayout settingsGroup(){
        LinearLayout group=col();group.setPadding(dp(10),dp(4),dp(10),dp(4));
        group.setBackground(stroke(Color.rgb(8,63,101),Color.rgb(25,104,154),18));
        LinearLayout.LayoutParams p=mp();p.setMargins(0,dp(4),0,dp(5));group.setLayoutParams(p);return group;
    }

    View settingsAction(String icon,String title,String subtitle,Runnable action){
        LinearLayout card=col();card.setPadding(dp(10),dp(10),dp(10),dp(10));
        card.setBackground(stroke(Color.rgb(8,63,101),Color.rgb(25,104,154),16));
        LinearLayout.LayoutParams p=mp();p.setMargins(0,dp(2),0,dp(2));card.setLayoutParams(p);
        TextView ic=tv(icon,22,TEXT,true);ic.setGravity(Gravity.CENTER);
        card.addView(ic,new LinearLayout.LayoutParams(-1,dp(28)));
        TextView titleView=tv(title,13.5f,TEXT,true);titleView.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(titleView,mp());
        TextView sub=tv(subtitle,10.5f,MUTED,false);sub.setMaxLines(2);
        card.addView(sub,mp());card.setClickable(true);card.setFocusable(true);card.setOnClickListener(v->action.run());return card;
    }

    View settingsActionRow(String icon,String title,String subtitle,Runnable action){
        LinearLayout row=row();row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(4),dp(7),dp(3),dp(7));
        TextView ic=tv(icon,23,TEXT,true);ic.setGravity(Gravity.CENTER);
        row.addView(ic,new LinearLayout.LayoutParams(dp(38),dp(48)));
        LinearLayout text=col();text.setGravity(Gravity.CENTER_VERTICAL);
        text.addView(tv(title,13.5f,TEXT,true),mp());
        TextView sub=tv(subtitle,10.5f,MUTED,false);sub.setMaxLines(2);sub.setEllipsize(android.text.TextUtils.TruncateAt.END);text.addView(sub,mp());
        row.addView(text,new LinearLayout.LayoutParams(0,-2,1));
        TextView chevron=tv("›",25,TEXT,false);chevron.setGravity(Gravity.CENTER);
        row.addView(chevron,new LinearLayout.LayoutParams(dp(25),dp(42)));
        row.setClickable(true);row.setFocusable(true);row.setOnClickListener(v->action.run());return row;
    }

    View settingsSwitchRow(String icon,String title,String subtitle,String key,boolean enabledFeature){
        LinearLayout row=row();row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(4),dp(6),dp(2),dp(6));
        TextView ic=tv(icon,22,key.equals("weather_alerts")||key.equals("daily_summary")?GOLD:TEXT,true);ic.setGravity(Gravity.CENTER);
        row.addView(ic,new LinearLayout.LayoutParams(dp(38),dp(54)));
        LinearLayout text=col();text.setGravity(Gravity.CENTER_VERTICAL);
        text.addView(tv(title,13.5f,TEXT,true),mp());
        TextView sub=tv(subtitle,10.5f,MUTED,false);sub.setMaxLines(2);text.addView(sub,mp());
        row.addView(text,new LinearLayout.LayoutParams(0,-2,1));
        Switch sw=new Switch(this);sw.setChecked(settingsPrefs().getBoolean(key,key.equals("auto_refresh")?autoRefreshEnabled:false));
        row.addView(sw,new LinearLayout.LayoutParams(-2,dp(48)));
        sw.setOnCheckedChangeListener((button,checked)->{
            settingsPrefs().edit().putBoolean(key,checked).apply();
            if(key.equals("auto_refresh")){
                autoRefreshEnabled=checked;
                if(checked)load();
            }else if(key.equals("weather_alerts")||key.equals("daily_summary")){
                new AlertDialog.Builder(this).setTitle("Bildirim ayarı")
                    .setMessage("Tercihin kaydedildi. Bu bildirim türü için otomatik bildirim altyapısı henüz tamamlanmadı; hazır olduğunda etkinleştirilecek.")
                    .setPositiveButton("Tamam",null).show();
            }else if(key.equals("use_gps")){
                if(checked){
                    sw.setChecked(false);
                    settingsPrefs().edit().putBoolean(key,false).apply();
                    new AlertDialog.Builder(this).setTitle("GPS konumu")
                        .setMessage("GPS konumunu etkinleştirmek için konum izni ve konum tabanlı MGM istasyon eşleştirmesi eklenmesi gerekiyor. Bu özellik henüz etkin değil.")
                        .setPositiveButton("Tamam",null).show();
                }
            }
        });
        return row;
    }

    void showThemeOptions(){
        new AlertDialog.Builder(this).setTitle("Tema Seçimi")
            .setMessage("Mevcut sürüm koyu mavi tasarımla çalışıyor. Açık tema için uygulama genelindeki renklerin birlikte dönüştürülmesi gerekiyor; henüz kaydetmiyorum.")
            .setPositiveButton("Tamam",null).show();
    }

    void showContrastOptions(){
        boolean high=settingsPrefs().getBoolean("high_contrast",true);
        new AlertDialog.Builder(this).setTitle("Karanlık Mod Kontrastı")
            .setSingleChoiceItems(new String[]{"Standart","Yüksek kontrast"},high?1:0,(d,which)->{
                settingsPrefs().edit().putBoolean("high_contrast",which==1).apply();
                d.dismiss();
                Toast.makeText(this,which==1?"Yüksek kontrast tercihi kaydedildi.":"Standart kontrast tercihi kaydedildi.",Toast.LENGTH_SHORT).show();
            }).setNegativeButton("İptal",null).show();
    }

    void showLocationPicker(){
        String[] names={"Edirne Merkez","Enez","Havsa","İpsala","Keşan","Lalapaşa","Meriç","Süloğlu","Uzunköprü"};
        String current=selectedDistrictName.equals("Edirne Merkez")?"Edirne Merkez":selectedDistrictName;
        int selected=0;for(int i=0;i<names.length;i++)if(names[i].equals(current))selected=i;
        final int[] choice={selected};
        new AlertDialog.Builder(this).setTitle("Konum Değiştir").setSingleChoiceItems(names,selected,(d,which)->choice[0]=which)
            .setNegativeButton("İptal",null).setPositiveButton("Seç", (d,w)->{
                selectedDistrictName=names[choice[0]];
                settingsPrefs().edit().putString("selected_district",selectedDistrictName).apply();
                Toast.makeText(this,selectedDistrictName+" seçildi.",Toast.LENGTH_SHORT).show();
                if(selectedDistrictName.equals("Edirne Merkez"))showHome();else showDistrictsTab(0);
            }).show();
    }

    void showAboutDialog(){
        new AlertDialog.Builder(this).setTitle("Hakkında")
            .setMessage("Edirne Yerel Hava Tahmin Uygulaması\nSürüm: "+appVersion()+"\nEdirne ve ilçeleri için hava durumu bilgileri.")
            .setPositiveButton("Tamam",null).show();
    }

    void showPrivacyDialog(){
        new AlertDialog.Builder(this).setTitle("Gizlilik")
            .setMessage("Uygulama hava durumu verilerini görüntülemek için internet bağlantısı kullanır. GPS ile otomatik konum bu sürümde etkin değildir. Bildirim tercihleri için gerekli altyapı tamamlanma aşamasındadır.")
            .setPositiveButton("Tamam",null).show();
    }

    void showSourcesDialog(){
        new AlertDialog.Builder(this).setTitle("Veri Kaynağı / Lisanslar")
            .setMessage("Hava durumu verileri Meteoroloji Genel Müdürlüğü (MGM) servislerinden alınır. Fotoğraf ve açık kaynak bileşenlerinin lisans bilgileri uygulama geliştirmesinde ayrıca belgelenmelidir.")
            .setPositiveButton("MGM sitesini aç",(d,w)->open("https://www.mgm.gov.tr/"))
            .setNegativeButton("Kapat",null).show();
    }

    void showRateDialog(){
        new AlertDialog.Builder(this).setTitle("Uygulamayı Puanla")
            .setMessage("Uygulama şu anda doğrudan mağazada yayımlanmıyor. İstersen geri bildirim göndererek destek olabilirsin.")
            .setPositiveButton("Geri bildirim gönder",(d,w)->showFeedback())
            .setNegativeButton("Kapat",null).show();
    }

    void showFeedback(){
        try{
            Intent intent=new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_SUBJECT,"Edirne Hava Durumu - Hata / Öneri");
            intent.putExtra(Intent.EXTRA_TEXT,"Uygulama sürümü: "+appVersion()+"\nCihaz: "+Build.MANUFACTURER+" "+Build.MODEL+"\n\nMesajım:\n");
            startActivity(Intent.createChooser(intent,"Geri bildirim gönder"));
        }catch(Exception e){
            new AlertDialog.Builder(this).setTitle("Geri Bildirim").setMessage("Cihazda e-posta uygulaması bulunamadı.").setPositiveButton("Tamam",null).show();
        }
    }

    void showFaqDialog(){
        new AlertDialog.Builder(this).setTitle("Sıkça Sorulan Sorular")
            .setMessage("• Veriler nereden geliyor? MGM servislerinden.\n\n• Ne sıklıkla yenileniyor? Otomatik yenileme açıkken uygulama çalışırken 5 dakikada bir kontrol edilir.\n\n• İlçeyi nasıl değiştiririm? Ayarlar > Konum Değiştir bölümünden seçebilirsin.\n\n• Uyarılar neden görünmeyebilir? MGM'de ilgili il için güncel uyarı yoksa uyarı gösterilmez.")
            .setPositiveButton("Tamam",null).show();
    }

    void shareApp(){
        Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT,"Edirne Hava Durumu");
        send.putExtra(Intent.EXTRA_TEXT,"Edirne Hava Durumu uygulamasını buradan indir: https://github.com/hkndmrl22-bit/Edirnehavadurumu2/releases/download/v10.99/app-release.apk");
        startActivity(Intent.createChooser(send,"Uygulamayı paylaş"));
    }

    void addSocial(LinearLayout p,int res,String url){ImageButton b=new ImageButton(this);b.setImageResource(res);b.setBackgroundColor(Color.TRANSPARENT);b.setOnClickListener(v->open(url));p.addView(b,new LinearLayout.LayoutParams(0,dp(55),1));}

    void load(){
        if(isLoading)return;
        isLoading=true;
        ex.execute(()->{
            try{
                final Loc cen=apiLocation("Edirne Merkez","merkez");
                main.post(()->{
                    center=cen;lastUpdate=cen.lastUpdate;loadError="";
                    if(currentScreen==0)showHome();
                    else if(currentScreen==1&&all.size()<=1)showDistrictsTab(districtTab);
                });
                String[] D={"Enez","Havsa","İpsala","Keşan","Lalapaşa","Meriç","Süloğlu","Uzunköprü"};
                String[] Q={"ENEZ","HAVSA","IPSALA","KESAN","LALAPASA","MERIC","SULOGLU","UZUNKOPRU"};
                ArrayList<Loc> tmp=new ArrayList<>();tmp.add(cen);
                for(int i=0;i<D.length;i++){
                    try{tmp.add(apiLocation(D[i],Q[i].toLowerCase(Locale.ROOT)));}
                    catch(Exception ignored){
                        Loc missing=new Loc(D[i]);missing.nowEvent="Veri alınamadı";missing.lastUpdate="—";tmp.add(missing);
                    }
                }
                main.post(()->{all=tmp;if(currentScreen==1)showDistrictsTab(districtTab);});
            }catch(Exception e){
                main.post(()->{
                    loadError="MGM verileri alınamadı. Yeniden denemek için dokunun.";
                    if(currentScreen==0)showHome();
                    else if(center==null&&currentScreen==1)showDistrictsTab(districtTab);
                    Toast.makeText(this,"MGM verileri alınamadı. Bağlantını kontrol edip tekrar dene.",Toast.LENGTH_SHORT).show();
                });
            }finally{isLoading=false;}
        });
    }

    Loc apiLocation(String name,String district)throws Exception{
        String q="il=edirne&ilce="+java.net.URLEncoder.encode(district,"UTF-8");
        JSONArray stations=new JSONArray(apiGet(API+"merkezler?"+q));if(stations.length()==0)throw new Exception("MGM istasyon yok");
        JSONObject st=stations.getJSONObject(0);int merkezId=st.optInt("merkezId",0),istNo=st.optInt("gunlukTahminIstNo",0),hourly=st.optInt("saatlikTahminIstNo",0);
        if(merkezId==0)merkezId=istNo;if(istNo==0)istNo=merkezId;Loc l=new Loc(name);
        try{
            JSONArray cur=new JSONArray(apiGet(API+"sondurumlar?merkezid="+merkezId));
            if(cur.length()>0){
                JSONObject c=cur.getJSONObject(0);l.now=num(c,"sicaklik");l.nowEvent=condition(c.optString("hadiseKodu",""));
                l.humidity=num(c,"nem");l.pressure=pressure(c);l.wind=num(c,"ruzgarHiz");l.feels=num(c,"hissedilenSicaklik");l.windDir=c.optString("ruzgarYon","");
                String observationTime=c.optString("veriZamani","");
                l.lastUpdate=observationTime.isEmpty()?"—":formatUtc(observationTime);
            }else l.nowEvent="Veri alınamadı";
        }catch(Exception ignored){l.nowEvent="Veri alınamadı";}
        try{
            JSONArray days=new JSONArray(apiGet(API+"tahminler/gunluk?istno="+istNo));
            if(days.length()>0){
                JSONObject j=days.getJSONObject(0);
                for(int i=1;i<=6;i++){
                    String date=formatDay(j.optString("tarihGun"+i,""));
                    String lo=num(j,"enDusukGun"+i),hi=num(j,"enYuksekGun"+i);
                    if(!date.isEmpty()&&isTodayOrFuture(date)&&(!lo.isEmpty()||!hi.isEmpty()||!j.optString("hadiseGun"+i,"").isEmpty()))
                        l.days.add(new Day(date,condition(j.optString("hadiseGun"+i,"")),lo,hi));
                }
            }
        }catch(Exception ignored){}
        try{
            int hno=hourly>0?hourly:istNo;
            JSONArray ha=new JSONArray(apiGet(API+"tahminler/saatlik?istno="+hno));
            if(ha.length()>0){
                JSONArray a=ha.getJSONObject(0).optJSONArray("tahmin");
                if(a!=null)for(int z=0;z<a.length()&&z<12;z++){
                    JSONObject h=a.getJSONObject(z);
                    l.hours.add(new Hour(timeOnly(formatUtc(h.optString("tarih",""))),num(h,"sicaklik"),condition(h.optString("hadise","")),num(h,"ruzgarHizi")));
                }
            }
        }catch(Exception ignored){}

        // MGM daily forecast stays authoritative. If it starts tomorrow, today's card
        // is clearly marked as a current observation instead of inventing daily min/max.
        SimpleDateFormat todayFormat=new SimpleDateFormat("dd MMMM yyyy",new Locale("tr","TR"));
        todayFormat.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));
        String today=todayFormat.format(new Date());
        if(l.nowEvent==null||l.nowEvent.isEmpty())l.nowEvent="Durum bilgisi yok";
        // Keep MGM daily dates intact: show five actual future forecast days, not four plus today.
        if(!l.days.isEmpty()&&!l.days.get(0).date.equals(today)){
            ArrayList<Day> next=new ArrayList<>();
            for(int i=0;i<l.days.size()&&next.size()<5;i++){
                if(!l.days.get(i).date.equals(today)) next.add(l.days.get(i));
            }
            l.days.clear();l.days.addAll(next);
        }
        return l;
    }
    boolean isCurrentForecastDay(String date){
        SimpleDateFormat fmt=new SimpleDateFormat("dd MMMM yyyy",new Locale("tr","TR"));
        fmt.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));
        return date!=null && date.equals(fmt.format(new Date()));
    }
    String apiGet(String u)throws Exception{return Jsoup.connect(u).ignoreContentType(true).timeout(20000).userAgent("Mozilla/5.0 (Android) EdirneHavaDurumu").header("Accept","application/json, text/plain, */*").header("Origin","https://www.mgm.gov.tr").header("Referer","https://www.mgm.gov.tr/").execute().body();}
    String num(JSONObject j,String k){if(!j.has(k)||j.isNull(k))return "";String s=String.valueOf(j.opt(k));try{double d=Double.parseDouble(s.replace(",","."));if(d==-9999d)return "";return d==Math.rint(d)?String.valueOf((int)d):String.format(Locale.US,"%.1f",d);}catch(Exception e){java.util.regex.Matcher m=java.util.regex.Pattern.compile("-?\\d+(?:[.,]\\d+)?").matcher(s);return m.find()?m.group().replace(",","."):"";}}
    String pressure(JSONObject j){
        String[] keys={"denizeIndirgenmisBasinc","denizSeviyesiBasinc","seaLevelPressure","aktuelBasinc","basinc","basincHpa","basincDegeri","istasyonBasinc","pressure","pressureHpa"};
        for(String key:keys){String v=num(j,key);if(!v.isEmpty())return v;}
        java.util.Iterator<String> it=j.keys();
        while(it.hasNext()){
            String key=it.next();
            String low=key.toLowerCase(Locale.ROOT);
            if(low.contains("basinc")||low.contains("pressure")){
                String v=num(j,key);if(!v.isEmpty())return v;
            }
        }
        return "";
    }
    String condition(String c){
        if(c==null||c.trim().isEmpty())return "Durum bilgisi yok";
        String code=c.trim().toUpperCase(Locale.ROOT);
        String[] k={"PB","GSY","HSY","SY","A","AB","CB","HY","Y","K","R","SIS","PUS","KY","KSY","YKY","KGY","KGSY","SNE","HSNE","KSNE"};
        String[] v={"Parçalı Bulutlu","Gökgürültülü Sağanak Yağışlı","Hafif Sağanak Yağışlı","Sağanak Yağışlı","Açık","Az Bulutlu","Çok Bulutlu","Hafif Yağmurlu","Yağmurlu","Kar Yağışlı","Rüzgarlı","Sis","Puslu","Kuvvetli Yağmurlu","Kuvvetli Sağanak Yağışlı","Yoğun Kar Yağışlı","Kuvvetli Gökgürültülü Sağanak Yağışlı","Kuvvetli Gökgürültülü Sağanak Yağışlı","Kar Yağışlı","Hafif Kar Yağışlı","Kuvvetli Kar Yağışlı"};
        for(int i=0;i<k.length;i++)if(k[i].equals(code))return v[i];
        if(code.matches("[A-Z0-9]{1,5}"))return "Hava durumu bilgisi yok";
        return c.trim();
    }
    String formatUtc(String s){try{SimpleDateFormat in=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);in.setTimeZone(TimeZone.getTimeZone("UTC"));Date d=in.parse(s);SimpleDateFormat o=new SimpleDateFormat("dd.MM.yyyy HH:mm",new Locale("tr","TR"));o.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));return o.format(d);}catch(Exception e){return s;}}
    String formatDay(String s){try{SimpleDateFormat in=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);in.setTimeZone(TimeZone.getTimeZone("UTC"));Date d=in.parse(s);Calendar c=Calendar.getInstance(TimeZone.getTimeZone("Europe/Istanbul"));c.setTime(d);String[] ay={"Ocak","Şubat","Mart","Nisan","Mayıs","Haziran","Temmuz","Ağustos","Eylül","Ekim","Kasım","Aralık"};return String.format(Locale.US,"%02d %s %04d",c.get(Calendar.DAY_OF_MONTH),ay[c.get(Calendar.MONTH)],c.get(Calendar.YEAR)).trim();}catch(Exception e){return s;}}
    boolean isTodayOrFuture(String value){
        try{
            TimeZone zone=TimeZone.getTimeZone("Europe/Istanbul");
            SimpleDateFormat format=new SimpleDateFormat("dd MMMM yyyy",new Locale("tr","TR"));
            format.setLenient(false);format.setTimeZone(zone);
            Date parsed=format.parse(value);
            Calendar forecast=Calendar.getInstance(zone);forecast.setTime(parsed);
            forecast.set(Calendar.HOUR_OF_DAY,0);forecast.set(Calendar.MINUTE,0);forecast.set(Calendar.SECOND,0);forecast.set(Calendar.MILLISECOND,0);
            Calendar today=Calendar.getInstance(zone);
            today.set(Calendar.HOUR_OF_DAY,0);today.set(Calendar.MINUTE,0);today.set(Calendar.SECOND,0);today.set(Calendar.MILLISECOND,0);
            return !forecast.before(today);
        }catch(Exception ignored){return false;}
    }
    String timeOnly(String s){if(s==null)return "";java.util.regex.Matcher m=java.util.regex.Pattern.compile("(\\d{2}:\\d{2})").matcher(s);return m.find()?m.group(1):"";}
    String currentTime(){
        SimpleDateFormat format=new SimpleDateFormat("d MMMM yyyy HH:mm",new Locale("tr","TR"));
        format.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));
        return format.format(new Date());
    }
    String windDirection(String value){
        if(value==null||value.trim().isEmpty())return "";
        try{
            double degrees=Double.parseDouble(value.trim());
            double d=((degrees%360)+360)%360;
            if(d>=315||d<45)return "K";
            if(d<135)return "D";
            if(d<225)return "G";
            return "B";
        }catch(Exception ignored){
            String v=value.trim().toUpperCase(new Locale("tr","TR"));
            if(v.contains("KUZEY"))return "K";
            if(v.contains("DOĞU")||v.contains("DOGU"))return "D";
            if(v.contains("GÜNEY")||v.contains("GUNEY"))return "G";
            if(v.contains("BATI"))return "B";
            return v;
        }
    }
    String windArrow(String value){
        String d=windDirection(value);
        if(d.equals("K"))return "↑";
        if(d.equals("D"))return "→";
        if(d.equals("G"))return "↓";
        if(d.equals("B"))return "←";
        return "↗";
    }
    String shortTime(String x){if(x==null||x.isEmpty()||x.equals("—"))return "—";int p=x.lastIndexOf(" ");return p>=0&&p+1<x.length()?x.substring(p+1):x;}
    String shortDateTime(String value){
        if(value==null||value.isEmpty()||value.equals("—"))return "—";
        try{
            SimpleDateFormat parser=new SimpleDateFormat("dd.MM.yyyy HH:mm",Locale.US);
            parser.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));
            Date date=parser.parse(value);
            SimpleDateFormat output=new SimpleDateFormat("dd MMM · HH:mm",new Locale("tr","TR"));
            output.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));
            return output.format(date);
        }catch(Exception ignored){return value;}
    }
    String dayLabel(String s){if(s==null||s.isEmpty())return "Bugün";return s.matches(".* \\d{4}$")?s.substring(0,s.length()-5):s;}
    String val(String x,String d){return x==null||x.isEmpty()?d:x;}
    View weatherIconView(String event,int sizeDp){
        final String e=val(event,"").toLowerCase(new Locale("tr"));
        return new View(this){
            Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override protected void onDraw(Canvas c){
                super.onDraw(c);
                float w=getWidth(),h=getHeight(),cx=w/2f,cy=h/2f;
                if(sizeDp<=18){ c.save(); c.scale(0.55f,0.55f,cx,cy); if(!(e.contains("gök")||e.contains("şimşek")||e.contains("sağanak")||e.contains("yağ")||e.contains("kar"))) c.translate(0,dp(5)); }
                if(e.contains("veri alınamadı")||e.contains("bilgisi yok")){
                    p.setColor(MUTED);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));
                    c.drawCircle(cx,cy,dp(12),p);p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(dp(17));
                    c.drawText("?",cx,cy+dp(6),p);
                    if(sizeDp<=18)c.restore();
                    return;
                }
                p.setStrokeWidth(Math.max(2,dp(2)));p.setStrokeCap(Paint.Cap.ROUND);
                if(!e.contains("kar")&&(e.contains("gök")||e.contains("şimşek")||e.contains("sağanak")||e.contains("yağ"))){
                    // güneş/yağışlı ikon
                    if(e.contains("sağanak")||e.contains("yağ")||e.contains("gök")){
                        p.setColor(Color.rgb(255,196,32));p.setStyle(Paint.Style.FILL);
                        c.drawCircle(cx-dp(9),cy-dp(7),dp(11),p);
                        p.setColor(Color.WHITE);p.setStyle(Paint.Style.FILL);
                        c.drawCircle(cx+dp(4),cy+dp(1),dp(12),p);c.drawCircle(cx-dp(8),cy+dp(3),dp(10),p);c.drawRoundRect(cx-dp(18),cy, cx+dp(18),cy+dp(11),dp(6),dp(6),p);
                        p.setColor(Color.rgb(40,155,235));p.setStrokeWidth(dp(3));p.setStyle(Paint.Style.STROKE);
                        for(int i=-1;i<=1;i++)c.drawLine(cx+dp(i*9),cy+dp(14),cx+dp(i*9-2),cy+dp(21),p);
                        if(e.contains("gök")){
                            p.setColor(Color.rgb(255,210,35));p.setStyle(Paint.Style.FILL);
                            Path bolt=new Path();bolt.moveTo(cx+dp(3),cy+dp(8));bolt.lineTo(cx-dp(2),cy+dp(17));
                            bolt.lineTo(cx+dp(2),cy+dp(17));bolt.lineTo(cx-dp(1),cy+dp(25));
                            bolt.lineTo(cx+dp(8),cy+dp(14));bolt.lineTo(cx+dp(4),cy+dp(14));bolt.close();c.drawPath(bolt,p);
                        }
                    }else{
                        p.setColor(Color.WHITE);p.setStyle(Paint.Style.FILL);
                        c.drawCircle(cx,cy,dp(15),p);
                    }
                    if(sizeDp<=18)c.restore();
                    return;
                }
                if(e.contains("kar")){
                    p.setColor(Color.WHITE);p.setStyle(Paint.Style.FILL);c.drawCircle(cx,cy-dp(3),dp(13),p);c.drawRoundRect(cx-dp(19),cy+dp(1),cx+dp(19),cy+dp(13),dp(7),dp(7),p);
                    p.setColor(Color.rgb(120,205,255));p.setStrokeWidth(dp(2));p.setStyle(Paint.Style.STROKE);
                    c.drawCircle(cx-dp(10),cy+dp(19),dp(2),p);c.drawCircle(cx,cy+dp(19),dp(2),p);c.drawCircle(cx+dp(10),cy+dp(19),dp(2),p);
                    if(sizeDp<=18)c.restore();
                    return;
                }
                if(e.contains("sis")||e.contains("pus")||e.contains("duman")){
                    p.setColor(Color.rgb(210,230,245));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(3));p.setStrokeCap(Paint.Cap.ROUND);
                    for(int i=-1;i<=1;i++){float yy=cy+dp(i*7);c.drawLine(cx-dp(18),yy,cx+dp(18),yy,p);}
                    if(sizeDp<=18)c.restore();
                    return;
                }
                if(e.contains("çok bulutlu")){
                    p.setColor(Color.WHITE);p.setStyle(Paint.Style.FILL);
                    c.drawCircle(cx-dp(7),cy+dp(1),dp(10),p);c.drawCircle(cx+dp(5),cy-dp(3),dp(12),p);
                    c.drawRoundRect(cx-dp(18),cy, cx+dp(18),cy+dp(12),dp(6),dp(6),p);
                    if(sizeDp<=18)c.restore();
                    return;
                }
                if(e.contains("bulut")||e.contains("parçalı")){
                    p.setColor(Color.rgb(255,195,35));p.setStyle(Paint.Style.FILL);c.drawCircle(cx-dp(8),cy-dp(8),dp(11),p);
                    p.setColor(Color.WHITE);c.drawCircle(cx+dp(5),cy+dp(3),dp(11),p);c.drawCircle(cx-dp(8),cy+dp(5),dp(9),p);c.drawRoundRect(cx-dp(18),cy+dp(2),cx+dp(18),cy+dp(12),dp(6),dp(6),p);
                    if(sizeDp<=18)c.restore();
                    return;
                }
                if(e.contains("rüzgar")||e.contains("rüzgâr")||e.contains("rüz")){
                    p.setColor(Color.rgb(210,230,245));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(3));p.setStrokeCap(Paint.Cap.ROUND);
                    c.drawLine(cx-dp(18),cy-dp(8),cx+dp(16),cy-dp(8),p);
                    c.drawLine(cx-dp(18),cy,cx+dp(10),cy,p);
                    c.drawLine(cx-dp(18),cy+dp(8),cx+dp(4),cy+dp(8),p);
                    if(sizeDp<=18)c.restore();
                    return;
                }
                // Açık hava: güneş + ışınlar
                p.setColor(Color.rgb(255,195,25));p.setStyle(Paint.Style.FILL);c.drawCircle(cx,cy,dp(14),p);
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(3));
                for(int i=0;i<8;i++){double a=i*Math.PI/4;float x1=cx+(float)Math.cos(a)*dp(20),y1=cy+(float)Math.sin(a)*dp(20);float x2=cx+(float)Math.cos(a)*dp(27),y2=cy+(float)Math.sin(a)*dp(27);c.drawLine(x1,y1,x2,y2,p);}
                if(sizeDp<=18) c.restore();
            }
        };
    }
    String icon(String e){
        String x=val(e,"").toLowerCase(new Locale("tr"));
        if(x.contains("gök")||x.contains("şimşek"))return "⛈";
        if(x.contains("kar"))return "❄";
        if(x.contains("sis")||x.contains("pus")||x.contains("duman"))return "≋";
        if(x.contains("yağ")||x.contains("sağanak"))return "☔";
        if(x.contains("sis"))return "≋";
        if(x.contains("rüz"))return "≋";
        if(x.contains("çok bulutlu"))return "☁";
        if(x.contains("parçalı"))return "◒";
        if(x.contains("az bulutlu"))return "◓";
        return "☀";
    }
    void open(String u){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}catch(Exception ignored){}}
    void loadHeroPhoto(ImageView target){
        imgEx.execute(()->{
            try{
                java.io.File cache=new java.io.File(getCacheDir(),"edirne_hero_high_v2.jpg");
                if(cache.exists()){
                    android.graphics.Bitmap cached=android.graphics.BitmapFactory.decodeFile(cache.getAbsolutePath());
                    if(cached!=null){main.post(()->target.setImageBitmap(cached));return;}
                    cache.delete();
                }
                java.io.File temp=new java.io.File(getCacheDir(),"edirne_hero_high_v2.jpg.part");
                java.net.HttpURLConnection c=(java.net.HttpURLConnection)new java.net.URL(HERO_URL).openConnection();
                c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setUseCaches(true);
                try{
                    try(java.io.InputStream in=c.getInputStream();java.io.FileOutputStream out=new java.io.FileOutputStream(temp)){
                        byte[] buf=new byte[8192];int n;
                        while((n=in.read(buf))!=-1)out.write(buf,0,n);
                    }
                }finally{c.disconnect();}
                if(!temp.renameTo(cache)){temp.delete();throw new java.io.IOException("Hero image cache save failed");}
                final android.graphics.Bitmap b=android.graphics.BitmapFactory.decodeFile(cache.getAbsolutePath());
                if(b!=null)main.post(()->target.setImageBitmap(b));
            }catch(Exception ignored){}
        });
    }

    String appVersion(){try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(Exception e){return "10.9";}}

    void checkForUpdate(){
        imgEx.execute(()->{
            try{
                String json=Jsoup.connect(VERSION_URL).ignoreContentType(true).timeout(8000).execute().body();
                JSONObject o=new JSONObject(json);
                final int latest=o.optInt("versionCode",0); final String name=o.optString("versionName",""); final String url=o.optString("apkUrl","https://github.com/hkndmrl22-bit/Edirnehavadurumu2/releases/latest/download/EdirneHavaDurumu.apk");
                int current=getPackageManager().getPackageInfo(getPackageName(),0).versionCode;
                if(latest>current) main.post(()->new AlertDialog.Builder(this).setTitle("Yeni sürüm var").setMessage("Yeni sürüm "+name+" yayınlandı. Şimdi indirmek ister misin?").setNegativeButton("Daha sonra",null).setPositiveButton("İndir", (d,w)->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)))).show());
            }catch(Exception ignored){}
        });
    }
    @Override protected void onDestroy(){timer.removeCallbacks(refresh5m);ex.shutdownNow();imgEx.shutdownNow();super.onDestroy();}
    static class Day{String date,e,mi,ma;boolean observedOnly;Day(String d,String e,String mi,String ma){this(d,e,mi,ma,false);}Day(String d,String e,String mi,String ma,boolean observedOnly){this.date=d;this.e=e;this.mi=mi;this.ma=ma;this.observedOnly=observedOnly;}}
    static class Hour{String time,temp,event,wind;Hour(String t,String v,String e,String w){time=t;temp=v;event=e;wind=w;}}
    static class Loc{String name,now="",nowEvent="",humidity="",pressure="",wind="",feels="",windDir="",lastUpdate="—";ArrayList<Day>days=new ArrayList<>();ArrayList<Hour>hours=new ArrayList<>();Loc(String n){name=n;}}

    View dayCard(Day d){
        LinearLayout c=row();c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(14),dp(10),dp(14),dp(10));c.setBackground(bg(CARD,17));
        LinearLayout left=col();left.addView(tv(dayLabel(d.date),14,TEXT,true));left.addView(tv(d.e,10,MUTED,false));c.addView(left,new LinearLayout.LayoutParams(0,-2,1));
        c.addView(tv(icon(d.e),27,TEXT,false),new LinearLayout.LayoutParams(dp(45),dp(55)));
        LinearLayout temp=col();temp.setGravity(Gravity.CENTER_VERTICAL);temp.addView(tv("↑ "+d.ma+"°",15,Color.rgb(255,145,90),true));temp.addView(tv("↓ "+d.mi+"°",15,Color.rgb(85,195,255),true));c.addView(temp);
        return c;
    }

    View hourCard(Hour h){
        LinearLayout c=col(); c.setGravity(Gravity.CENTER); c.setPadding(dp(4),dp(3),dp(4),dp(3)); c.setBackground(bg(Color.rgb(19,59,91),18));
        c.addView(tv(h.time,9,TEXT,true),new LinearLayout.LayoutParams(-1,dp(18)));
        TextView wi=tv(icon(h.event),23,TEXT,false);wi.setGravity(Gravity.CENTER);c.addView(wi,new LinearLayout.LayoutParams(-1,dp(38)));
        TextView temp=tv(h.temp+"°",16,TEXT,true);temp.setGravity(Gravity.CENTER);c.addView(temp,new LinearLayout.LayoutParams(-1,dp(24)));
        TextView ev=tv(h.event,8,TEXT,true);ev.setGravity(Gravity.CENTER);c.addView(ev,new LinearLayout.LayoutParams(-1,dp(16)));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(52),dp(70));p.setMargins(0,0,dp(4),0);c.setLayoutParams(p);return c;
    }
    void showForecast(){
        content.removeAllViews();header("5 Günlük Tahmin",true,false);
        if(center==null){content.addView(tv("Veriler yükleniyor…",14,MUTED,false));return;}
        for(Day d:center.days)content.addView(dayCard(d),mp());
        section(center.days.size()>1?center.days.get(1).date+"  •  DETAY":"DETAYLI TAHMİN");
        LinearLayout detail=col();detail.setPadding(dp(14),dp(14),dp(14),dp(14));detail.setBackground(bg(CARD,20));
        if(center.days.size()>1){Day d=center.days.get(1);detail.addView(tv(d.ma+"° / "+d.mi+"°",28,GOLD,true));detail.addView(tv(d.e,16,TEXT,true));}
        detail.addView(tv("Yağış ihtimali ve miktarı yayınlandığında burada gösterilir",11,MUTED,false));
        detail.addView(tv("Nem: "+val(center.humidity,"—")+"%     Rüzgâr: "+val(center.wind,"—")+" km/sa "+val(center.windDir,"")+"     Basınç: "+unitValue(center.pressure," hPa"),11,TEXT,false));
        content.addView(detail,mp());
        section("SAATLİK TAHMİN");
        HorizontalScrollView hs=new HorizontalScrollView(this);LinearLayout hr=row();
        if(center.hours.size()==0)hr.addView(tv("Saatlik tahmin şu anda alınamadı.",12,MUTED,false));
        for(Hour h:center.hours)hr.addView(hourCard(h));
        hs.addView(hr);content.addView(hs,mp());
    }


}