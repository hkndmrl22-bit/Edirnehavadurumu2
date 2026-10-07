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
    final int NAVY=Color.rgb(7,25,48), CARD=Color.rgb(15,48,79), BLUE=Color.rgb(34,112,170);
    final int TEXT=Color.WHITE, MUTED=Color.rgb(175,198,220), GOLD=Color.rgb(255,194,55);
    ExecutorService ex=Executors.newSingleThreadExecutor();
    Handler main=new Handler(Looper.getMainLooper()), timer=new Handler(Looper.getMainLooper());
    Runnable refresh5m;
    LinearLayout page,content,bottomNav;
    TextView pageTitle,status;
    ArrayList<Loc> all=new ArrayList<>();
    Loc center;
    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    TextView tv(String s,float z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setTypeface(Typeface.DEFAULT,b?Typeface.BOLD:Typeface.NORMAL);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    GradientDrawable bg(int c,int r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
    GradientDrawable stroke(int c,int sc,int r){GradientDrawable g=bg(c,r);g.setStroke(dp(1),sc);return g;}
    LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    LinearLayout.LayoutParams mp(){return new LinearLayout.LayoutParams(-1,-2);}
    LinearLayout.LayoutParams w(int width){return new LinearLayout.LayoutParams(dp(width),-1);}
    @Override public void onCreate(Bundle b){super.onCreate(b);refresh5m=()->{load();timer.postDelayed(refresh5m,300000);};buildShell();showHome();load();timer.postDelayed(refresh5m,300000);}

    void buildShell(){
        page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setBackgroundColor(NAVY);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        content=col();content.setPadding(dp(16),dp(10),dp(16),dp(28));scroll.addView(content);
        page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        bottomNav=nav();
        page.addView(bottomNav,new LinearLayout.LayoutParams(-1,dp(92)));
        setContentView(page);
    }

    LinearLayout nav(){
        LinearLayout n=row();n.setGravity(Gravity.CENTER);n.setPadding(dp(8),dp(7),dp(8),dp(7));n.setBackground(bg(Color.rgb(8,38,68),0));
        String[] labels={"⌂\nAna Sayfa","●\nİlçeler","▥\nTahmin","⚠\nUyarılar"};
        for(int i=0;i<4;i++){final int k=i;TextView b=tv(labels[i],15,k==0?Color.WHITE:Color.rgb(210,228,245),true);b.setGravity(Gravity.CENTER);b.setClickable(true);b.setFocusable(true);b.setMinHeight(dp(84));b.setPadding(0,dp(6),0,dp(6));if(k==0)b.setBackground(bg(Color.rgb(18,122,235),18));b.setOnClickListener(v->{if(k==0)showHome();else if(k==1)showDistricts();else if(k==2)showForecast();else showWarnings();});LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(84),1);p.setMargins(dp(3),0,dp(3),0);n.addView(b,p);} n.setClickable(true);n.bringToFront();
        return n;
    }

    void header(String title,boolean back,boolean gear){
        LinearLayout h=row();h.setGravity(Gravity.CENTER_VERTICAL);h.setPadding(0,dp(5),0,dp(10));
        if(back){TextView b=tv("‹",34,TEXT,false);b.setGravity(Gravity.CENTER);b.setOnClickListener(v->showHome());h.addView(b,new LinearLayout.LayoutParams(dp(42),dp(46)));}
        LinearLayout tt=col();tt.addView(tv(title,21,TEXT,true));if(title.equals("EDİRNE"))tt.addView(tv("YEREL HAVA TAHMİN UYGULAMASI",10,MUTED,true));
        h.addView(tt,new LinearLayout.LayoutParams(0,-2,1));
        if(gear){TextView g=tv("⚙",24,TEXT,false);g.setGravity(Gravity.CENTER);g.setOnClickListener(v->showSettings());h.addView(g,new LinearLayout.LayoutParams(dp(44),dp(46)));}
        content.addView(h,mp());
    }

    View logoHero(){
        LinearLayout hero=col();hero.setPadding(dp(16),dp(14),dp(16),dp(16));
        GradientDrawable gd=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(25,86,128),Color.rgb(8,31,61)});gd.setCornerRadius(dp(24));hero.setBackground(gd);
        LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.edirne_logo_app);logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        top.addView(logo,new LinearLayout.LayoutParams(dp(76),dp(76)));
        LinearLayout tx=col();tx.setPadding(dp(12),0,0,0);tx.addView(tv("EDİRNE",27,TEXT,true));tx.addView(tv("YEREL HAVA TAHMİN UYGULAMASI",11,MUTED,true));top.addView(tx,new LinearLayout.LayoutParams(0,-2,1));
        hero.addView(top);
        TextView loc=tv("⌖  Edirne Merkez",14,TEXT,true);loc.setPadding(0,dp(13),0,dp(2));hero.addView(loc);
        hero.addView(tv("Son güncelleme: "+currentTime(),10,MUTED,false));
        return hero;
    }

    void showHome(){
        content.removeAllViews();header("EDİRNE",false,true);content.addView(logoHero(),mp());
        LinearLayout weather=col();weather.setPadding(dp(18),dp(14),dp(18),dp(16));weather.setBackground(bg(Color.rgb(8,56,91),22));
        if(center==null){weather.addView(tv("Veriler yükleniyor…",17,MUTED,true));}
        else{
            LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout left=col();left.addView(tv("⌖  Edirne Merkez",18,TEXT,true));left.addView(tv(new SimpleDateFormat("d MMM yyyy EEE HH:mm",new Locale("tr","TR")).format(new Date()),12,MUTED,false));left.addView(tv("Son güncelleme: "+currentTime()",11,MUTED,false));top.addView(left,new LinearLayout.LayoutParams(0,-2,1));
            LinearLayout right=col();right.setGravity(Gravity.CENTER);right.addView(tv(icon(center.nowEvent),48,TEXT,false));right.addView(tv(val(center.now,"—"),38,Color.WHITE,true));right.addView(tv(val(center.nowEvent,"—"),14,TEXT,true));right.addView(tv("Hissedilen: "+val(center.feels,"—")+"°C",11,MUTED,false));top.addView(right,new LinearLayout.LayoutParams(dp(125),-2));weather.addView(top);
            LinearLayout metrics=row();metrics.setPadding(0,dp(12),0,0);metrics.addView(metric("💧 Nem",val(center.humidity,"—")+"%"),new LinearLayout.LayoutParams(0,-2,1));metrics.addView(metric("≋ Rüzgâr",val(center.wind,"—")+" km/sa "+val(center.windDir,"")),new LinearLayout.LayoutParams(0,-2,1));metrics.addView(metric("◉ Basınç",val(center.pressure,"—")+" hPa"),new LinearLayout.LayoutParams(0,-2,1));weather.addView(metrics);
        }
        LinearLayout.LayoutParams wp=mp();wp.setMargins(0,dp(10),0,0);content.addView(weather,wp);
        section("GÜN DOĞUMU / GÜN BATIMI");
        LinearLayout sun=row();sun.addView(infoCard("☀","Gün doğumu","07:08"),new LinearLayout.LayoutParams(0,-2,1));sun.addView(infoCard("◐","Gün batımı","18:54"),new LinearLayout.LayoutParams(0,-2,1));content.addView(sun,mp());
        section("SAATLİK TAHMİN");
        HorizontalScrollView hs=new HorizontalScrollView(this);LinearLayout hr=row();
        if(center==null||center.hours.size()==0)hr.addView(tv("Saatlik tahmin yükleniyor…",12,MUTED,false));else for(Hour h:center.hours)hr.addView(hourCard(h));
        hs.addView(hr);content.addView(hs,mp());
        section("5 GÜNLÜK HAVA TAHMİNİ");
        if(center!=null&&!center.days.isEmpty()){LinearLayout days=row();HorizontalScrollView ds=new HorizontalScrollView(this);for(Day d:center.days){TextView card=tv(dayLabel(d.date)+"\n"+icon(d.e)+"\n"+d.ma+"°  "+d.mi+"°\n"+d.e,11,TEXT,true);card.setGravity(Gravity.CENTER);card.setPadding(dp(10),dp(10),dp(10),dp(10));card.setBackground(bg(Color.rgb(7,48,82),17));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(112),dp(150));p.setMargins(0,0,dp(8),0);days.addView(card,p);}ds.addView(days);content.addView(ds,mp());}
    }

    TextView metric(String a,String b){TextView t=tv(a+"\n"+b,10,TEXT,true);t.setPadding(dp(7),dp(9),dp(7),dp(9));t.setGravity(Gravity.CENTER);t.setBackground(bg(Color.rgb(20,69,105),14));return t;}
    TextView infoCard(String icon,String a,String b){TextView t=tv(icon+"  "+a+"\n      "+b,13,TEXT,true);t.setPadding(dp(12),dp(12),dp(12),dp(12));t.setBackground(bg(Color.rgb(12,58,94),17));return t;}
    void section(String s){TextView t=tv(s,15,Color.rgb(188,213,239),true);t.setPadding(dp(2),dp(18),dp(2),dp(9));content.addView(t,mp());}

void showDistricts(){
        content.removeAllViews();header("Edirne İlçeleri",false,false);
        section("İLÇE HAVA DURUMU");
        for(int i=1;i<all.size();i++)districtCard(all.get(i));
    }

    void districtCard(Loc l){
        LinearLayout c=row();c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(12),dp(10),dp(12),dp(10));c.setBackground(bg(CARD,18));
        c.addView(tv(icon(l.nowEvent),30,TEXT,false),new LinearLayout.LayoutParams(dp(48),dp(58)));
        LinearLayout tx=col();tx.addView(tv(l.name,15,TEXT,true));tx.addView(tv(val(l.nowEvent,"—"),11,MUTED,false));c.addView(tx,new LinearLayout.LayoutParams(0,-2,1));
        c.addView(tv(val(l.now,"—"),22,GOLD,true));c.setOnClickListener(v->showDistrictDetail(l));
        LinearLayout.LayoutParams p=mp();p.setMargins(0,0,0,dp(8));content.addView(c,p);
    }

    void showDistrictDetail(Loc l){
        content.removeAllViews();header(l.name+" Hava Durumu",true,false);
        LinearLayout hero=col();hero.setPadding(dp(18),dp(16),dp(18),dp(16));hero.setBackground(bg(CARD,22));hero.setGravity(Gravity.CENTER);
        hero.addView(tv(icon(l.nowEvent),52,TEXT,false));hero.addView(tv(val(l.now,"—"),44,GOLD,true));hero.addView(tv(val(l.nowEvent,"—"),17,TEXT,true));hero.addView(tv("Hissedilen: "+val(l.feels,"—")+"°C",12,MUTED,false));hero.addView(tv("Son güncelleme: "+currentTime(),10,MUTED,false));content.addView(hero,mp());
        section("ANLIK VERİLER");LinearLayout m=row();m.addView(metric("Nem",val(l.humidity,"—")+"%"),new LinearLayout.LayoutParams(0,-2,1));m.addView(metric("Rüzgâr",val(l.wind,"—")+" km/sa "+val(l.windDir,"")),new LinearLayout.LayoutParams(0,-2,1));m.addView(metric("Basınç",val(l.pressure,"—")+" hPa"),new LinearLayout.LayoutParams(0,-2,1));content.addView(m,mp());
        section("5 GÜNLÜK TAHMİN");for(Day d:l.days)content.addView(dayCard(d),mp());
        section("SAATLİK TAHMİN");for(Hour h:l.hours)content.addView(hourCard(h));
    }

    void showWarnings(){
        content.removeAllViews();header("Meteorolojik Uyarılar",true,false);
        LinearLayout card=col();card.setPadding(dp(15),dp(15),dp(15),dp(15));card.setBackground(stroke(Color.rgb(72,57,15),Color.rgb(255,193,7),20));
        card.addView(tv("METEOROLOJİK UYARI",12,Color.rgb(255,205,65),true));card.addView(tv("SARI KODLU UYARI",20,TEXT,true));card.addView(tv("Güncel meteorolojik uyarılar bu alanda gösterilecektir.",12,MUTED,false));card.addView(tv("Güncel uyarılar burada yayınlanır.",10,MUTED,false));content.addView(card,mp());
        TextView note=tv("⚠  Bu deneme sürümünde uyarı ekranının tasarımı hazırlandı. Aktif uyarılar aktif uyarılar burada otomatik listelenecek.",12,TEXT,false);note.setPadding(dp(14),dp(14),dp(14),dp(14));note.setBackground(bg(CARD,18));LinearLayout.LayoutParams p=mp();p.setMargins(0,dp(12),0,0);content.addView(note,p);
    }

    void showSettings(){
        content.removeAllViews();header("Ayarlar",true,false);
        section("UYGULAMA TEMASI");content.addView(setting("◐","Açık / Koyu / Sistem","Koyu tema (deneme)"),mp());
        section("TERCİHLER");content.addView(toggleSetting("Bildirimler",true));content.addView(toggleSetting("Konum",false));content.addView(toggleSetting("Anlık Güncelleme",true));
        section("HAKKINDA");content.addView(setting("ⓘ","Hakkında","Edirne Yerel Hava Tahmin Uygulaması"),mp());content.addView(setting("🔒","Gizlilik Politikası","Yerel uygulama"),mp());
        section("BİZİ TAKİP EDİN");LinearLayout socials=row();addSocial(socials,R.drawable.ic_facebook,"https://www.facebook.com/edirnehavadurumu");addSocial(socials,R.drawable.ic_instagram,"https://www.instagram.com/edirnehavadurumu/");addSocial(socials,R.drawable.ic_x,"https://x.com/edirnehavadurumu");addSocial(socials,R.drawable.ic_youtube,"https://www.youtube.com/@edirnehavadurumu");content.addView(socials,mp());
        TextView foot=tv("Edirne Yerel Hava Tahmin Uygulaması\nSürüm "+appVersion(),11,MUTED,false);foot.setGravity(Gravity.CENTER);foot.setPadding(0,dp(25),0,dp(15));content.addView(foot,mp());
    }

    TextView setting(String i,String a,String b){TextView t=tv(i+"   "+a+"\n        "+b,13,TEXT,true);t.setPadding(dp(13),dp(12),dp(13),dp(12));t.setBackground(bg(CARD,16));return t;}
    View toggleSetting(String name,boolean checked){Switch s=new Switch(this);s.setText(name);s.setTextColor(TEXT);s.setTextSize(14);s.setChecked(checked);s.setPadding(dp(10),dp(8),dp(10),dp(8));s.setBackground(bg(CARD,16));LinearLayout.LayoutParams p=mp();p.setMargins(0,dp(5),0,0);s.setLayoutParams(p);return s;}
    void addSocial(LinearLayout p,int res,String url){ImageButton b=new ImageButton(this);b.setImageResource(res);b.setBackgroundColor(Color.TRANSPARENT);b.setOnClickListener(v->open(url));p.addView(b,new LinearLayout.LayoutParams(0,dp(55),1));}

    void load(){
        if(status!=null)status.setText("Veriler güncelleniyor…");
        ex.execute(()->{
            try{
                final Loc cen=apiLocation("Edirne Merkez","merkez");
                main.post(()->{center=cen;all=new ArrayList<>();all.add(cen);if(status!=null)status.setText("Veriler güncellendi.");showHome();});
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
        if(cur.length()>0){JSONObject c=cur.getJSONObject(0);l.now=num(c,"sicaklik");if(!l.now.isEmpty())l.now+="°C";l.nowEvent=condition(c.optString("hadiseKodu",""));l.humidity=num(c,"nem");l.pressure=pressure(c);l.wind=num(c,"ruzgarHiz");l.feels=num(c,"hissedilenSicaklik");l.windDir=c.optString("ruzgarYon","");}
        JSONArray days=new JSONArray(apiGet(API+"tahminler/gunluk?istno="+istNo));
        if(days.length()>0){JSONObject j=days.getJSONObject(0);for(int i=1;i<=5;i++){String lo=num(j,"enDusukGun"+i),hi=num(j,"enYuksekGun"+i);if(!lo.isEmpty()&&!hi.isEmpty())l.days.add(new Day(formatDay(j.optString("tarihGun"+i,"")),condition(j.optString("hadiseGun"+i,"")),lo,hi));}}
        try{int hno=hourly>0?hourly:istNo;JSONArray ha=new JSONArray(apiGet(API+"tahminler/saatlik?istno="+hno));if(ha.length()>0){JSONArray a=ha.getJSONObject(0).optJSONArray("tahmin");if(a!=null)for(int z=0;z<a.length()&&z<12;z++){JSONObject h=a.getJSONObject(z);l.hours.add(new Hour(timeOnly(formatUtc(h.optString("tarih",""))),num(h,"sicaklik"),condition(h.optString("hadise","")),num(h,"ruzgarHizi")));}}}catch(Exception ignored){}
        return l;
    }
    String apiGet(String u)throws Exception{return Jsoup.connect(u).ignoreContentType(true).timeout(20000).userAgent("Mozilla/5.0 (Android) EdirneHavaDurumu").header("Accept","application/json, text/plain, */*").header("Origin","https://www.mgm.gov.tr").header("Referer","https://www.mgm.gov.tr/").execute().body();}
    String num(JSONObject j,String k){if(!j.has(k)||j.isNull(k))return "";String s=String.valueOf(j.opt(k));if(s.equals("-9999"))return "";try{double d=Double.parseDouble(s.replace(",","."));return d==Math.rint(d)?String.valueOf((int)d):String.format(Locale.US,"%.1f",d);}catch(Exception e){java.util.regex.Matcher m=java.util.regex.Pattern.compile("-?\\d+(?:[.,]\\d+)?").matcher(s);return m.find()?m.group().replace(",","."):"";}}
    String pressure(JSONObject j){String v=num(j,"basinc");if(!v.isEmpty())return v;java.util.Iterator<String> it=j.keys();while(it.hasNext()){String k=it.next().toLowerCase(Locale.ROOT);if(k.contains("basinc")){v=num(j,k);if(!v.isEmpty())return v;}}return "";}
    String condition(String c){String[] k={"PB","GSY","HSY","SY","A","AB","CB","HY","Y","K","R","SIS","KY","KSY","YKY","KGY"};String[] v={"Parçalı Bulutlu","Gökgürültülü Sağanak Yağışlı","Hafif Sağanak Yağışlı","Sağanak Yağışlı","Açık","Az Bulutlu","Çok Bulutlu","Hafif Yağmurlu","Yağmurlu","Kar Yağışlı","Rüzgarlı","Sis","Kuvvetli Yağmurlu","Kuvvetli Sağanak Yağışlı","Yoğun Kar Yağışlı","Kuvvetli Gökgürültülü Sağanak Yağışlı"};for(int i=0;i<k.length;i++)if(k[i].equalsIgnoreCase(c))return v[i];return c;}
    String formatUtc(String s){try{SimpleDateFormat in=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);in.setTimeZone(TimeZone.getTimeZone("UTC"));Date d=in.parse(s);SimpleDateFormat o=new SimpleDateFormat("dd.MM.yyyy HH:mm",new Locale("tr","TR"));o.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));return o.format(d);}catch(Exception e){return s;}}
    String formatDay(String s){try{SimpleDateFormat in=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);in.setTimeZone(TimeZone.getTimeZone("UTC"));Date d=in.parse(s);SimpleDateFormat o=new SimpleDateFormat("dd MMM",new Locale("tr","TR"));o.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));return o.format(d);}catch(Exception e){return s;}}
    String timeOnly(String s){if(s==null)return "";java.util.regex.Matcher m=java.util.regex.Pattern.compile("(\\d{2}:\\d{2})").matcher(s);return m.find()?m.group(1):"";}
    String currentTime(){return new SimpleDateFormat("d MMM yyyy HH:mm",new Locale("tr","TR")).format(new Date());}
    String dayLabel(String s){if(s==null||s.isEmpty())return "Bugün";return s;}
    String val(String x,String d){return x==null||x.isEmpty()?d:x;}
    String icon(String e){String x=val(e,"").toLowerCase(new Locale("tr"));if(x.contains("gök")||x.contains("şimşek"))return "⛈️";if(x.contains("kar"))return "🌨️";if(x.contains("yağ")||x.contains("sağanak"))return "🌧️";if(x.contains("sis"))return "🌫️";if(x.contains("rüz"))return "🌬️";if(x.contains("çok bulutlu"))return "☁️";if(x.contains("parçalı"))return "⛅";if(x.contains("az bulutlu"))return "🌤️";return "☀️";}
    void open(String u){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}catch(Exception ignored){}}
    String appVersion(){try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(Exception e){return "10.5";}}
    @Override protected void onDestroy(){timer.removeCallbacks(refresh5m);ex.shutdownNow();super.onDestroy();}
    static class Day{String date,e,mi,ma;Day(String d,String e,String mi,String ma){this.date=d;this.e=e;this.mi=mi;this.ma=ma;}}
    static class Hour{String time,temp,event,wind;Hour(String t,String v,String e,String w){time=t;temp=v;event=e;wind=w;}}
    static class Loc{String name,now="",nowEvent="",humidity="",pressure="",wind="",feels="",windDir="";ArrayList<Day>days=new ArrayList<>();ArrayList<Hour>hours=new ArrayList<>();Loc(String n){name=n;}}

    View dayCard(Day d){
        LinearLayout c=row();c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(12),dp(10),dp(12),dp(10));c.setBackground(bg(CARD,17));
        LinearLayout left=col();left.addView(tv(dayLabel(d.date),14,TEXT,true));left.addView(tv(d.e,10,MUTED,false));c.addView(left,new LinearLayout.LayoutParams(0,-2,1));
        c.addView(tv(icon(d.e),27,TEXT,false),new LinearLayout.LayoutParams(dp(45),dp(55)));
        LinearLayout temp=col();temp.setGravity(Gravity.CENTER_VERTICAL);temp.addView(tv("↑ "+d.ma+"°",15,Color.rgb(255,145,90),true));temp.addView(tv("↓ "+d.mi+"°",15,Color.rgb(85,195,255),true));c.addView(temp);
        return c;
    }

    View hourCard(Hour h){
        TextView t=tv(h.time+"\n"+icon(h.event)+"\n"+h.temp+"°\n"+h.event,13,TEXT,true);t.setGravity(Gravity.CENTER);t.setPadding(dp(7),dp(9),dp(7),dp(9));t.setBackground(bg(Color.rgb(19,59,91),16));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(105),dp(150));p.setMargins(0,0,dp(8),0);t.setLayoutParams(p);return t;
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