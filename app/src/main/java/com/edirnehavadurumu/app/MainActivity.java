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
    GradientDrawable stroke(int c,int sc,int r){GradientDrawable g=bg(c,r);g.setStroke(dp(1),sc);return g;}
    LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    LinearLayout.LayoutParams mp(){return new LinearLayout.LayoutParams(-1,-2);}
    LinearLayout.LayoutParams w(int width){return new LinearLayout.LayoutParams(dp(width),-1);}
    @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(NAVY); getWindow().setNavigationBarColor(Color.rgb(5,20,34)); getWindow().getDecorView().setOnApplyWindowInsetsListener((v,insets)->{ int nav=0; if(Build.VERSION.SDK_INT>=30) nav=insets.getInsets(WindowInsets.Type.navigationBars()).bottom; else if(Build.VERSION.SDK_INT>=23) nav=insets.getSystemWindowInsetBottom(); if(bottomNav!=null){ LinearLayout.LayoutParams np=(LinearLayout.LayoutParams)bottomNav.getLayoutParams(); np.bottomMargin=nav; bottomNav.setLayoutParams(np); } return insets; }); refresh5m=()->{load();timer.postDelayed(refresh5m,300000);};buildShell();showHome();load();timer.postDelayed(refresh5m,300000);checkForUpdate();}

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
        TextView photoDate=tv(trDate()+"  •  "+new SimpleDateFormat("HH:mm",new Locale("tr","TR")).format(new Date()),12,Color.WHITE,true);
        photoDate.setGravity(Gravity.CENTER);
        photoDate.setShadowLayer(dp(3),0,dp(1),Color.BLACK);
        FrameLayout.LayoutParams datep=new FrameLayout.LayoutParams(-1,dp(30),Gravity.BOTTOM);
        datep.setMargins(dp(8),0,dp(8),dp(8));
        hero.addView(photoDate,datep);
        content.addView(hero,mp());

        if(center==null){content.addView(tv("Veriler yükleniyor…",15,MUTED,true),mp());return;}

        // Güncel durum kartı
        LinearLayout weather=col();weather.setPadding(dp(10),dp(6),dp(10),dp(6));
        weather.setBackground(stroke(Color.rgb(5,68,108),Color.rgb(25,113,174),18));
        LinearLayout wh=row();wh.setGravity(Gravity.CENTER_VERTICAL);
        wh.addView(tv("EDİRNE MERKEZ",16,Color.rgb(231,242,250),true),
                new LinearLayout.LayoutParams(0,dp(24),1));
        TextView upd=tv("⟳  Son Güncelleme: "+lastUpdate,10,TEXT,true);
        upd.setGravity(Gravity.CENTER);upd.setSingleLine(true);
        upd.setBackground(bg(Color.rgb(18,75,115),14));
        upd.setClickable(true);
        upd.setOnClickListener(v->{ lastUpdate=currentTime(); upd.setText("⟳  Güncelleniyor…"); load(); });
        wh.addView(upd,new LinearLayout.LayoutParams(dp(174),dp(24)));
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
        main.addView(cur,new LinearLayout.LayoutParams(0,dp(78),0.50f));

        LinearLayout met=col();met.setPadding(0,0,0,0);
        LinearLayout r1=row();r1.setGravity(Gravity.CENTER_VERTICAL);
        r1.addView(metric("🌡 Hissedilen",tempC(center.feels,"—")),new LinearLayout.LayoutParams(0,dp(58),1));
        r1.addView(metric("💧 Nem",val(center.humidity,"—")+"%"),new LinearLayout.LayoutParams(0,dp(58),1));met.addView(r1);
        LinearLayout r2=row();r2.setGravity(Gravity.CENTER_VERTICAL);
        r2.addView(metric("≋ Rüzgâr",val(center.wind,"—")+" km/sa\n"+val(center.windDir,"")),new LinearLayout.LayoutParams(0,dp(58),1));
        r2.addView(metric("◉ Basınç",val(center.pressure,"—")+" hPa"),new LinearLayout.LayoutParams(0,dp(58),1));met.addView(r2);
        main.addView(met,new LinearLayout.LayoutParams(0,dp(116),0.50f));
        weather.addView(main);

        LinearLayout.LayoutParams wp=mp();wp.setMargins(dp(8),dp(6),dp(8),0);content.addView(weather,wp);

        // Saatlik tahmin — 5 günlük tahminin üstünde.
        LinearLayout hourly=col();hourly.setPadding(dp(6),dp(5),dp(6),dp(5));
        hourly.setBackground(stroke(Color.rgb(5,68,108),Color.rgb(25,113,174),18));
        sectionLabel(hourly,"SAATLİK TAHMİNLER ( EDİRNE MERKEZ )");
        LinearLayout hr=row();int hc=0;
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
        for(Day d:center.days){
            days.addView(dayCardFlex(d),new LinearLayout.LayoutParams(0,dp(122),1));
            if(++n>=5)break;
        }
        forecast5.addView(days,new LinearLayout.LayoutParams(-1,dp(122)));
        LinearLayout.LayoutParams dp5=mp();dp5.setMargins(dp(8),dp(4),dp(8),0);content.addView(forecast5,dp5);
    }

    View hourCardFlex(Hour h){
        LinearLayout c=col();c.setGravity(Gravity.CENTER_HORIZONTAL);c.setPadding(dp(2),dp(2),dp(2),dp(2));
        c.setBackground(stroke(Color.rgb(7,55,88),Color.rgb(16,91,137),14));
        TextView tm=tv(h.time,10,TEXT,true);tm.setGravity(Gravity.CENTER);tm.setIncludeFontPadding(false);
        c.addView(tm,new LinearLayout.LayoutParams(-1,dp(18)));
        c.addView(weatherIconView(h.event,25),new LinearLayout.LayoutParams(-1,dp(34)));
        TextView te=tv(h.temp+"°",18,TEXT,true);te.setGravity(Gravity.CENTER);te.setIncludeFontPadding(false);
        c.addView(te,new LinearLayout.LayoutParams(-1,dp(23)));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-1);p.setMargins(dp(2),0,dp(2),0);
        c.setLayoutParams(p);return c;
    }

    View dayCardFlex(Day d){
        LinearLayout c=col();c.setGravity(Gravity.CENTER_HORIZONTAL);c.setPadding(dp(2),dp(2),dp(2),dp(2));
        c.setBackground(stroke(Color.rgb(7,58,94),Color.rgb(24,111,171),13));
        TextView dl=tv(dayLabel(d.date),9,TEXT,true);dl.setGravity(Gravity.CENTER);dl.setIncludeFontPadding(false);
        c.addView(dl,new LinearLayout.LayoutParams(-1,dp(15)));
        TextView wd=tv(weekday(d.date),8,MUTED,false);wd.setGravity(Gravity.CENTER);wd.setIncludeFontPadding(false);
        c.addView(wd,new LinearLayout.LayoutParams(-1,dp(13)));
        c.addView(weatherIconView(d.e,21),new LinearLayout.LayoutParams(-1,dp(28)));
        TextView ev=tv(d.e,7,TEXT,true);ev.setGravity(Gravity.CENTER);ev.setIncludeFontPadding(false);ev.setMaxLines(2);
        c.addView(ev,new LinearLayout.LayoutParams(-1,dp(30)));
        LinearLayout temps=row();temps.setGravity(Gravity.CENTER);
        TextView hi=tv(d.ma+"°",12,Color.rgb(255,45,45),true);hi.setGravity(Gravity.CENTER);hi.setIncludeFontPadding(false);
        TextView lo=tv(d.mi+"°",12,Color.rgb(45,150,255),true);lo.setGravity(Gravity.CENTER);lo.setIncludeFontPadding(false);
        temps.addView(hi,new LinearLayout.LayoutParams(0,dp(20),1));
        temps.addView(lo,new LinearLayout.LayoutParams(0,dp(20),1));
        c.addView(temps);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-1);p.setMargins(dp(1),0,dp(1),0);c.setLayoutParams(p);return c;
    }

    void sectionLabel(LinearLayout parent,String s){
        TextView t=tv(s,13,Color.rgb(210,229,246),true);t.setPadding(0,dp(3),0,dp(4));parent.addView(t,mp());
    }

    String trDate(){
        String[] gun={"Pazar","Pazartesi","Salı","Çarşamba","Perşembe","Cuma","Cumartesi"};
        Calendar c=Calendar.getInstance();return new SimpleDateFormat("d MMMM yyyy",new Locale("tr","TR")).format(c.getTime())+" "+gun[c.get(Calendar.DAY_OF_WEEK)-1];
    }
    String weekday(String d){
        try{
            Date x;
            try{x=new SimpleDateFormat("dd MMM",new Locale("tr","TR")).parse(d);}
            catch(Exception e){x=new SimpleDateFormat("dd.MM.yyyy",new Locale("tr","TR")).parse(d);}
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
        TextView la=tv(label,8.5f,TEXT,true);la.setIncludeFontPadding(false);la.setSingleLine(true);la.setEllipsize(android.text.TextUtils.TruncateAt.END);
        TextView va=tv(b,10.5f,TEXT,true);va.setIncludeFontPadding(false);va.setMaxLines(2);va.setGravity(Gravity.CENTER_VERTICAL);
        info.addView(la,new LinearLayout.LayoutParams(-1,dp(18)));
        info.addView(va,new LinearLayout.LayoutParams(-1,dp(30)));
        card.addView(info,new LinearLayout.LayoutParams(0,-1,1));
        return card;
    }
    void section(String s){TextView t=tv(s,17,Color.rgb(205,224,244),true);t.setPadding(dp(2),dp(18),dp(2),dp(9));content.addView(t,mp());}

void showDistricts(){
        content.removeAllViews();setNavActive(1);content.setPadding(0,0,0,0);

        header("Edirne İlçeleri",false,false);
        section("İLÇELERDE ANLIK SON DURUM");
        TextView upd=tv("Son güncelleme: "+currentTime(),12,MUTED,false);
        upd.setPadding(dp(2),0,dp(2),dp(8));content.addView(upd,mp());

        LinearLayout tabs=row();tabs.setPadding(0,0,0,dp(2));
        TextView instant=tv("◉  ANLIK DURUM",14,TEXT,true);
        TextView five=tv("▦  5 GÜNLÜK TAHMİN",14,TEXT,true);
        instant.setGravity(Gravity.CENTER);five.setGravity(Gravity.CENTER);
        instant.setBackground(bg(Color.rgb(18,122,235),16));
        five.setBackground(stroke(Color.rgb(20,69,105),Color.rgb(35,125,190),16));
        LinearLayout.LayoutParams tp1=new LinearLayout.LayoutParams(0,dp(58),1);
        LinearLayout.LayoutParams tp2=new LinearLayout.LayoutParams(0,dp(58),1);
        tp1.setMargins(0,0,dp(2),0);tp2.setMargins(dp(2),0,0,0);
        tabs.addView(instant,tp1);tabs.addView(five,tp2);content.addView(tabs,mp());

        LinearLayout body=col();content.addView(body,mp());
        renderDistrictCurrent(body,all.size()>1?all.get(1):null);

        instant.setOnClickListener(v->{
            instant.setBackground(bg(Color.rgb(18,122,235),16));
            five.setBackground(stroke(Color.rgb(20,69,105),Color.rgb(35,125,190),16));
            body.removeAllViews();renderDistrictCurrent(body,all.size()>1?all.get(1):null);
        });
        five.setOnClickListener(v->{
            five.setBackground(bg(Color.rgb(18,122,235),16));
            instant.setBackground(stroke(Color.rgb(20,69,105),Color.rgb(35,125,190),16));
            body.removeAllViews();renderDistrictForecast(body,all.size()>1?all.get(1):null);
        });
    }

    void renderDistrictCurrent(LinearLayout body,Loc selected){
        if(selected==null){body.addView(tv("İlçe verileri yükleniyor…",14,MUTED,false));return;}
        LinearLayout split=row();split.setGravity(Gravity.TOP);
        LinearLayout left=col();left.setPadding(0,dp(8),dp(4),0);
        LinearLayout right=col();right.setPadding(dp(4),dp(8),0,0);
        split.addView(left,new LinearLayout.LayoutParams(0,-2,0.49f));
        split.addView(right,new LinearLayout.LayoutParams(0,-2,0.51f));

        for(int i=1;i<all.size();i++){
            final Loc l=all.get(i);
            final TextView card=districtMiniCard(l,l==selected);
            card.setOnClickListener(v->{body.removeAllViews();renderDistrictCurrent(body,l);});
            left.addView(card);
        }
        renderSelectedDistrict(right,selected,false);
        body.addView(split,mp());
    }

    TextView districtMiniCard(Loc l,boolean active){
        TextView c=tv(icon(l.nowEvent)+"  "+l.name+"\n"+val(l.nowEvent,"—")+"   "+tempC(l.now,"—"),10,TEXT,true);
        c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(8),dp(6),dp(5),dp(6));
        c.setBackground(active?bg(Color.rgb(18,122,235),14):bg(CARD,14));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(76));
        p.setMargins(0,0,0,dp(6));c.setLayoutParams(p);return c;
    }

    void renderSelectedDistrict(LinearLayout right,Loc l,boolean forecastOnly){
        LinearLayout hero=col();hero.setPadding(dp(10),dp(9),dp(10),dp(9));
        hero.setBackground(bg(Color.rgb(10,59,94),18));

        LinearLayout rt=row();rt.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout names=col();
        names.addView(tv(l.name,20,TEXT,true));
        names.addView(tv(val(l.nowEvent,"—"),11,MUTED,false));
        rt.addView(names,new LinearLayout.LayoutParams(0,-2,1));
        rt.addView(weatherIconView(l.nowEvent,42),new LinearLayout.LayoutParams(dp(58),dp(58)));
        hero.addView(rt);

        TextView temp=tv(tempC(l.now,"—"),30,GOLD,true);
        temp.setPadding(0,dp(2),0,dp(2));hero.addView(temp);

        LinearLayout mm=row();mm.setGravity(Gravity.CENTER_VERTICAL);
        mm.addView(metric("💧 Nem",val(l.humidity,"—")+"%"),new LinearLayout.LayoutParams(0,dp(64),1));
        mm.addView(metric("≋ Rüzgâr",val(l.wind,"—")+" km/sa"),new LinearLayout.LayoutParams(0,dp(64),1));
        mm.addView(metric("◉ Basınç",val(l.pressure,"—")+" hPa"),new LinearLayout.LayoutParams(0,dp(64),1));
        hero.addView(mm);right.addView(hero,mp());

        TextView h=tv("5 GÜNLÜK HAVA TAHMİNİ",14,Color.rgb(205,224,244),true);
        h.setPadding(0,dp(10),0,dp(6));right.addView(h,mp());
        for(Day d:l.days)right.addView(dayCompact(d),mp());

        TextView sh=tv("SAATLİK TAHMİN",14,Color.rgb(205,224,244),true);
        sh.setPadding(0,dp(10),0,dp(6));right.addView(sh,mp());
        LinearLayout hours=row();
        for(Hour h2:l.hours){hours.addView(hourCompact(h2));if(hours.getChildCount()>=4)break;}
        right.addView(hours,mp());
    }

    View dayCompact(Day d){
        LinearLayout c=row();c.setGravity(Gravity.CENTER_VERTICAL);
        c.setPadding(dp(7),dp(6),dp(5),dp(6));c.setBackground(bg(CARD,12));
        LinearLayout a=col();
        a.addView(tv(dayLabel(d.date),11,TEXT,true));
        a.addView(tv(d.e,8.5f,MUTED,false));
        c.addView(a,new LinearLayout.LayoutParams(0,dp(51),1));
        c.addView(weatherIconView(d.e,24),new LinearLayout.LayoutParams(dp(34),dp(51)));
        LinearLayout b=col();b.setGravity(Gravity.CENTER);
        b.addView(tv(d.ma+"°",12,Color.rgb(255,100,90),true));
        b.addView(tv(d.mi+"°",12,Color.rgb(90,190,255),true));
        c.addView(b,new LinearLayout.LayoutParams(dp(43),dp(51)));
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

    View sized(View v,int ww,int hh){v.setLayoutParams(new LinearLayout.LayoutParams(ww,hh));return v;}

    void renderDistrictForecast(LinearLayout body,Loc selected){
        if(selected==null){body.addView(tv("İlçe verileri yükleniyor…",14,MUTED,false));return;}
        LinearLayout split=row();split.setGravity(Gravity.TOP);
        LinearLayout left=col();left.setPadding(0,dp(8),dp(4),0);
        LinearLayout right=col();right.setPadding(dp(4),dp(8),0,0);
        split.addView(left,new LinearLayout.LayoutParams(0,-2,0.49f));
        split.addView(right,new LinearLayout.LayoutParams(0,-2,0.51f));
        for(int i=1;i<all.size();i++){
            final Loc l=all.get(i);
            TextView card=districtMiniCard(l,l==selected);
            card.setOnClickListener(v->{body.removeAllViews();renderDistrictForecast(body,l);});
            left.addView(card);
        }
        renderSelectedDistrict(right,selected,true);body.addView(split,mp());
    }

    void showWarnings(){
        content.removeAllViews();setNavActive(2);content.setPadding(dp(16),dp(18),dp(16),dp(28));header("Meteorolojik Uyarılar",true,false);
        LinearLayout card=col();card.setPadding(dp(15),dp(15),dp(15),dp(15));card.setBackground(stroke(Color.rgb(72,57,15),Color.rgb(255,193,7),20));
        card.addView(tv("METEOROLOJİK UYARI",12,Color.rgb(255,205,65),true));card.addView(tv("SARI KODLU UYARI",20,TEXT,true));card.addView(tv("Güncel meteorolojik uyarılar bu alanda gösterilecektir.",12,MUTED,false));card.addView(tv("Güncel uyarılar burada yayınlanır.",10,MUTED,false));content.addView(card,mp());
        TextView note=tv("⚠  Bu deneme sürümünde uyarı ekranının tasarımı hazırlandı. Aktif uyarılar aktif uyarılar burada otomatik listelenecek.",12,TEXT,false);note.setPadding(dp(14),dp(14),dp(14),dp(14));note.setBackground(bg(CARD,18));LinearLayout.LayoutParams p=mp();p.setMargins(0,dp(12),0,0);content.addView(note,p);
    }

    void showSettings(){
        content.removeAllViews();setNavActive(3);content.setPadding(dp(16),dp(18),dp(16),dp(28));header("Ayarlar",true,false);
        section("UYGULAMA TEMASI");content.addView(setting("◐","Açık / Koyu / Sistem","Koyu tema (deneme)"),mp());
        section("TERCİHLER");content.addView(toggleSetting("Bildirimler",true));content.addView(toggleSetting("Konum",false));content.addView(toggleSetting("Anlık Güncelleme",true));
        section("HAKKINDA");content.addView(setting("ⓘ","Hakkında","Edirne Yerel Hava Tahmin Uygulaması"),mp());content.addView(setting("🔒","Gizlilik Politikası","Yerel uygulama"),mp());
        section("BİZİ TAKİP EDİN");LinearLayout socials=row();addSocial(socials,R.drawable.ic_facebook,"https://www.facebook.com/edirnehavadurumu");addSocial(socials,R.drawable.ic_instagram,"https://www.instagram.com/edirnehavadurumu/");addSocial(socials,R.drawable.ic_x,"https://x.com/edirnehavadurumu");addSocial(socials,R.drawable.ic_youtube,"https://www.youtube.com/@edirnehavadurumu");content.addView(socials,mp());
        TextView foot=tv("Edirne Yerel Hava Tahmin Uygulaması\nSürüm "+appVersion()+"",10,MUTED,false);foot.setGravity(Gravity.CENTER);foot.setPadding(0,dp(25),0,dp(15));content.addView(foot,mp());
    }

    TextView setting(String i,String a,String b){TextView t=tv(i+"   "+a+"\n        "+b,13,TEXT,true);t.setPadding(dp(13),dp(12),dp(13),dp(12));t.setBackground(bg(CARD,16));return t;}
    View toggleSetting(String name,boolean checked){Switch s=new Switch(this);s.setText(name);s.setTextColor(TEXT);s.setTextSize(14);s.setChecked(checked);s.setPadding(dp(10),dp(8),dp(10),dp(8));s.setBackground(bg(CARD,16));LinearLayout.LayoutParams p=mp();p.setMargins(0,dp(5),0,0);s.setLayoutParams(p);return s;}
    void addSocial(LinearLayout p,int res,String url){ImageButton b=new ImageButton(this);b.setImageResource(res);b.setBackgroundColor(Color.TRANSPARENT);b.setOnClickListener(v->open(url));p.addView(b,new LinearLayout.LayoutParams(0,dp(55),1));}

    void load(){
        if(status!=null)status.setText("Veriler güncelleniyor…");
        ex.execute(()->{
            try{
                final Loc cen=apiLocation("Edirne Merkez","merkez");
                main.post(()->{center=cen;all=new ArrayList<>();all.add(cen);lastUpdate=currentTime();if(status!=null)status.setText("Veriler güncellendi.");showHome();});
                String[] D={"Enez","Havsa","İpsala","Keşan","Lalapaşa","Meriç","Süloğlu","Uzunköprü"};
                String[] Q={"ENEZ","HAVSA","IPSALA","KESAN","LALAPASA","MERIC","SULOGLU","UZUNKOPRU"};
                ArrayList<Loc> tmp=new ArrayList<>();tmp.add(cen);
                for(int i=0;i<D.length;i++){try{tmp.add(apiLocation(D[i],Q[i].toLowerCase(Locale.ROOT)));}catch(Exception ignored){}}
                main.post(()->{all=tmp;if(center==null)center=tmp.get(0);});
            }catch(Exception e){main.post(()->{if(status!=null)status.setText("Veriler alınamadı.");showHome();});}
        });
    }

    Loc apiLocation(String name,String district)throws Exception{
        String q="il=edirne&ilce="+java.net.URLEncoder.encode(district,"UTF-8");
        JSONArray stations=new JSONArray(apiGet(API+"merkezler?"+q));if(stations.length()==0)throw new Exception("MGM istasyon yok");
        JSONObject st=stations.getJSONObject(0);int merkezId=st.optInt("merkezId",0),istNo=st.optInt("gunlukTahminIstNo",0),hourly=st.optInt("saatlikTahminIstNo",0);
        if(merkezId==0)merkezId=istNo;if(istNo==0)istNo=merkezId;Loc l=new Loc(name);
        JSONArray cur=new JSONArray(apiGet(API+"sondurumlar?merkezid="+merkezId));
        if(cur.length()>0){JSONObject c=cur.getJSONObject(0);l.now=num(c,"sicaklik");l.nowEvent=condition(c.optString("hadiseKodu",""));l.humidity=num(c,"nem");l.pressure=pressure(c);l.wind=num(c,"ruzgarHiz");l.feels=num(c,"hissedilenSicaklik");l.windDir=c.optString("ruzgarYon","");}
        JSONArray days=new JSONArray(apiGet(API+"tahminler/gunluk?istno="+istNo));
        if(days.length()>0){
            JSONObject j=days.getJSONObject(0);
            for(int i=1;i<=5;i++){
                String lo=num(j,"enDusukGun"+i),hi=num(j,"enYuksekGun"+i);
                if(!lo.isEmpty()&&!hi.isEmpty())l.days.add(new Day(formatDay(j.optString("tarihGun"+i,"")),condition(j.optString("hadiseGun"+i,"")),lo,hi));
            }
        }
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

        // First card is always today. Current observation is also MGM data.
        String today=new SimpleDateFormat("dd MMM",new Locale("tr","TR")).format(new Date());
        String todayTemp=val(l.now,"—");
        String todayMin=todayTemp, todayMax=todayTemp;
        try{
            double mn=Double.parseDouble(todayTemp.replace(",","."));
            double mx=mn;
            for(Hour hh:l.hours){
                if(hh.time.startsWith("00:")||hh.time.startsWith("01:")||hh.time.startsWith("02:")||hh.time.startsWith("03:")||hh.time.startsWith("04:")||hh.time.startsWith("05:")||hh.time.startsWith("06:")||hh.time.startsWith("07:")||hh.time.startsWith("08:")||hh.time.startsWith("09:")||hh.time.startsWith("10:")||hh.time.startsWith("11:")||hh.time.startsWith("12:")||hh.time.startsWith("13:")||hh.time.startsWith("14:")||hh.time.startsWith("15:")||hh.time.startsWith("16:")||hh.time.startsWith("17:")||hh.time.startsWith("18:")||hh.time.startsWith("19:")||hh.time.startsWith("20:")||hh.time.startsWith("21:")||hh.time.startsWith("22:")||hh.time.startsWith("23:")){
                    try{double v=Double.parseDouble(hh.temp.replace(",","."));mn=Math.min(mn,v);mx=Math.max(mx,v);}catch(Exception ignored){}
                }
            }
            todayMin=String.format(Locale.US,"%.0f",mn);
            todayMax=String.format(Locale.US,"%.0f",mx);
        }catch(Exception ignored){}
        ArrayList<Day> next=new ArrayList<>();
        next.add(new Day(today,condition(l.nowEvent),todayMin,todayMax));
        for(int i=0;i<l.days.size()&&next.size()<5;i++)next.add(l.days.get(i));
        l.days.clear();l.days.addAll(next);
        return l;
    }
    String apiGet(String u)throws Exception{return Jsoup.connect(u).ignoreContentType(true).timeout(20000).userAgent("Mozilla/5.0 (Android) EdirneHavaDurumu").header("Accept","application/json, text/plain, */*").header("Origin","https://www.mgm.gov.tr").header("Referer","https://www.mgm.gov.tr/").execute().body();}
    String num(JSONObject j,String k){if(!j.has(k)||j.isNull(k))return "";String s=String.valueOf(j.opt(k));if(s.equals("-9999"))return "";try{double d=Double.parseDouble(s.replace(",","."));return d==Math.rint(d)?String.valueOf((int)d):String.format(Locale.US,"%.1f",d);}catch(Exception e){java.util.regex.Matcher m=java.util.regex.Pattern.compile("-?\\d+(?:[.,]\\d+)?").matcher(s);return m.find()?m.group().replace(",","."):"";}}
    String pressure(JSONObject j){
        String[] keys={"basinc","basincHpa","basincDegeri","istasyonBasinc","denizSeviyesiBasinc","pressure","pressureHpa"};
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
    String condition(String c){String[] k={"PB","GSY","HSY","SY","A","AB","CB","HY","Y","K","R","SIS","KY","KSY","YKY","KGY"};String[] v={"Parçalı Bulutlu","Gökgürültülü Sağanak Yağışlı","Hafif Sağanak Yağışlı","Sağanak Yağışlı","Açık","Az Bulutlu","Çok Bulutlu","Hafif Yağmurlu","Yağmurlu","Kar Yağışlı","Rüzgarlı","Sis","Kuvvetli Yağmurlu","Kuvvetli Sağanak Yağışlı","Yoğun Kar Yağışlı","Kuvvetli Gökgürültülü Sağanak Yağışlı"};for(int i=0;i<k.length;i++)if(k[i].equalsIgnoreCase(c))return v[i];return c;}
    String formatUtc(String s){try{SimpleDateFormat in=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);in.setTimeZone(TimeZone.getTimeZone("UTC"));Date d=in.parse(s);SimpleDateFormat o=new SimpleDateFormat("dd.MM.yyyy HH:mm",new Locale("tr","TR"));o.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));return o.format(d);}catch(Exception e){return s;}}
    String formatDay(String s){try{SimpleDateFormat in=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);in.setTimeZone(TimeZone.getTimeZone("UTC"));Date d=in.parse(s);SimpleDateFormat o=new SimpleDateFormat("dd MMM",new Locale("tr","TR"));o.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));return o.format(d);}catch(Exception e){return s;}}
    String timeOnly(String s){if(s==null)return "";java.util.regex.Matcher m=java.util.regex.Pattern.compile("(\\d{2}:\\d{2})").matcher(s);return m.find()?m.group(1):"";}
    String currentTime(){return new SimpleDateFormat("d MMM yyyy HH:mm",new Locale("tr","TR")).format(new Date());}
    String dayLabel(String s){if(s==null||s.isEmpty())return "Bugün";return s;}
    String val(String x,String d){return x==null||x.isEmpty()?d:x;}
    View weatherIconView(String event,int sizeDp){
        final String e=val(event,"").toLowerCase(new Locale("tr"));
        return new View(this){
            Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override protected void onDraw(Canvas c){
                super.onDraw(c);
                float w=getWidth(),h=getHeight(),cx=w/2f,cy=h/2f;
                if(sizeDp<=18){ c.save(); c.scale(0.55f,0.55f,cx,cy); c.translate(0,-dp(8)); }
                p.setStrokeWidth(Math.max(2,dp(2)));p.setStrokeCap(Paint.Cap.ROUND);
                if(e.contains("gök")||e.contains("şimşek")||e.contains("sağanak")||e.contains("yağ")){
                    // güneş/yağışlı ikon
                    if(e.contains("sağanak")||e.contains("yağ")||e.contains("gök")){
                        p.setColor(Color.rgb(255,196,32));p.setStyle(Paint.Style.FILL);
                        c.drawCircle(cx-dp(9),cy-dp(7),dp(11),p);
                        p.setColor(Color.WHITE);p.setStyle(Paint.Style.FILL);
                        c.drawCircle(cx+dp(4),cy+dp(1),dp(12),p);c.drawCircle(cx-dp(8),cy+dp(3),dp(10),p);c.drawRoundRect(cx-dp(18),cy, cx+dp(18),cy+dp(11),dp(6),dp(6),p);
                        p.setColor(Color.rgb(40,155,235));p.setStrokeWidth(dp(3));p.setStyle(Paint.Style.STROKE);
                        for(int i=-1;i<=1;i++)c.drawLine(cx+dp(i*9),cy+dp(14),cx+dp(i*9-2),cy+dp(21),p);
                    }else{
                        p.setColor(Color.WHITE);p.setStyle(Paint.Style.FILL);
                        c.drawCircle(cx,cy,dp(15),p);
                    }
                    return;
                }
                if(e.contains("kar")){
                    p.setColor(Color.WHITE);p.setStyle(Paint.Style.FILL);c.drawCircle(cx,cy-dp(3),dp(13),p);c.drawRoundRect(cx-dp(19),cy+dp(1),cx+dp(19),cy+dp(13),dp(7),dp(7),p);
                    p.setColor(Color.rgb(120,205,255));p.setStrokeWidth(dp(2));p.setStyle(Paint.Style.STROKE);
                    c.drawCircle(cx-dp(10),cy+dp(19),dp(2),p);c.drawCircle(cx,cy+dp(19),dp(2),p);c.drawCircle(cx+dp(10),cy+dp(19),dp(2),p);return;
                }
                if(e.contains("bulut")||e.contains("parçalı")){
                    p.setColor(Color.rgb(255,195,35));p.setStyle(Paint.Style.FILL);c.drawCircle(cx-dp(8),cy-dp(8),dp(11),p);
                    p.setColor(Color.WHITE);c.drawCircle(cx+dp(5),cy+dp(3),dp(11),p);c.drawCircle(cx-dp(8),cy+dp(5),dp(9),p);c.drawRoundRect(cx-dp(18),cy+dp(2),cx+dp(18),cy+dp(12),dp(6),dp(6),p);return;
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
        ex.execute(()->{
            try{
                java.io.File cache=new java.io.File(getCacheDir(),"edirne_hero_high_v2.jpg");
                if(!cache.exists()){
                    java.net.URL u=new java.net.URL(HERO_URL);
                    java.net.HttpURLConnection c=(java.net.HttpURLConnection)u.openConnection();
                    c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setUseCaches(true);
                    java.io.InputStream in=c.getInputStream();
                    java.io.FileOutputStream out=new java.io.FileOutputStream(cache);
                    byte[] buf=new byte[8192];int n;
                    while((n=in.read(buf))!=-1)out.write(buf,0,n);
                    out.close();in.close();c.disconnect();
                }
                final android.graphics.Bitmap b=android.graphics.BitmapFactory.decodeFile(cache.getAbsolutePath());
                if(b!=null)main.post(()->target.setImageBitmap(b));
            }catch(Exception ignored){}
        });
    }

    String appVersion(){try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(Exception e){return "10.9";}}

    void checkForUpdate(){
        ex.execute(()->{
            try{
                String json=Jsoup.connect(VERSION_URL).ignoreContentType(true).timeout(8000).execute().body();
                JSONObject o=new JSONObject(json);
                final int latest=o.optInt("versionCode",0); final String name=o.optString("versionName",""); final String url=o.optString("apkUrl","https://github.com/hkndmrl22-bit/Edirnehavadurumu2/releases/latest/download/EdirneHavaDurumu.apk");
                int current=getPackageManager().getPackageInfo(getPackageName(),0).versionCode;
                if(latest>current) main.post(()->new AlertDialog.Builder(this).setTitle("Yeni sürüm var").setMessage("Yeni sürüm "+name+" yayınlandı. Şimdi indirmek ister misin?").setNegativeButton("Daha sonra",null).setPositiveButton("İndir", (d,w)->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)))).show());
            }catch(Exception ignored){}
        });
    }
    @Override protected void onDestroy(){timer.removeCallbacks(refresh5m);ex.shutdownNow();super.onDestroy();}
    static class Day{String date,e,mi,ma;Day(String d,String e,String mi,String ma){this.date=d;this.e=e;this.mi=mi;this.ma=ma;}}
    static class Hour{String time,temp,event,wind;Hour(String t,String v,String e,String w){time=t;temp=v;event=e;wind=w;}}
    static class Loc{String name,now="",nowEvent="",humidity="",pressure="",wind="",feels="",windDir="";ArrayList<Day>days=new ArrayList<>();ArrayList<Hour>hours=new ArrayList<>();Loc(String n){name=n;}}

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
        detail.addView(tv("Nem: "+val(center.humidity,"—")+"%     Rüzgâr: "+val(center.wind,"—")+" km/sa "+val(center.windDir,"")+"     Basınç: "+val(center.pressure,"—")+" hPa",11,TEXT,false));
        content.addView(detail,mp());
        section("SAATLİK TAHMİN");
        HorizontalScrollView hs=new HorizontalScrollView(this);LinearLayout hr=row();
        if(center.hours.size()==0)hr.addView(tv("Saatlik tahmin şu anda alınamadı.",12,MUTED,false));
        for(Hour h:center.hours)hr.addView(hourCard(h));
        hs.addView(hr);content.addView(hs,mp());
    }


}