package com.edirnehavadurumu.app;

import android.app.*;import android.os.*;import android.graphics.*;import android.webkit.*;import android.util.Base64;import android.graphics.drawable.*;import android.view.*;import android.content.*;import android.net.*;import android.widget.*;import java.util.*;import java.util.concurrent.*;import java.text.*;import java.net.*;import java.io.*;import org.json.*;import org.jsoup.*;

public class MainActivity extends Activity{
 static final String API="https://servis.mgm.gov.tr/web/";
 ExecutorService ex=Executors.newSingleThreadExecutor(); Handler main=new Handler();
 LinearLayout root,current,hourly,five,dist,details; TextView status,updated; ProgressBar progress; Handler timer=new Handler();
Runnable refresh5m=new Runnable(){public void run(){load();timer.postDelayed(this,300000);}};
 String[] D={"Edirne Merkez","Enez","Havsa","İpsala","Keşan","Lalapaşa","Meriç","Süloğlu","Uzunköprü"};
 String[] Q={"","ENEZ","HAVSA","IPSALA","KESAN","LALAPASA","MERIC","SULOGLU","UZUNKOPRU"};
 int dp(float x){return(int)(x*getResources().getDisplayMetrics().density+.5f);}
 TextView tv(String s,float z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setTypeface(Typeface.DEFAULT,b?1:0);t.setPadding(dp(5),dp(3),dp(5),dp(3));return t;}
 GradientDrawable bg(int c,int r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
 void title(String s){TextView t=tv(s,19,-1,true);t.setPadding(dp(2),dp(15),dp(2),dp(7));root.addView(t);}
 @Override public void onCreate(Bundle b){super.onCreate(b);ui();load();main.postDelayed(()->checkForUpdate(false),1500);timer.postDelayed(refresh5m,300000);}
 void ui(){
  ScrollView sc=new ScrollView(this);
  root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
  root.setPadding(dp(12),dp(8),dp(12),dp(24));
  root.setBackgroundColor(Color.rgb(4,20,38));
  sc.setFillViewport(true); sc.addView(root); setContentView(sc);

  LinearLayout head=new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL);
  head.setPadding(dp(4),dp(8),dp(4),dp(12));
  ImageView logo=new ImageView(this); logo.setImageResource(R.drawable.edirne_logo_app);
  logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
  head.addView(logo,new LinearLayout.LayoutParams(dp(64),dp(64)));
  LinearLayout ht=new LinearLayout(this); ht.setOrientation(LinearLayout.VERTICAL); ht.setPadding(dp(10),0,0,0);
  ht.addView(tv("EDİRNE",24,Color.WHITE,true));
  ht.addView(tv("YEREL HAVA TAHMİN UYGULAMASI",12,Color.rgb(91,190,255),true));
  ht.addView(tv("MGM verileri • 5 dakikada bir güncellenir",10,Color.rgb(150,180,205),false));
  head.addView(ht,new LinearLayout.LayoutParams(0,-2,1));
  TextView gear=tv("⚙",25,Color.WHITE,false); gear.setGravity(17); head.addView(gear,new LinearLayout.LayoutParams(dp(42),dp(50)));
  root.addView(head);

  LinearLayout hero=new LinearLayout(this); hero.setOrientation(LinearLayout.VERTICAL);
  hero.setPadding(dp(16),dp(16),dp(16),dp(16)); hero.setBackground(bg(Color.rgb(12,57,91),24));
  TextView place=tv("📍 EDİRNE MERKEZ",15,Color.WHITE,true); hero.addView(place);
  TextView live=tv("ANLIK HAVA DURUMU",10,Color.rgb(145,210,250),true); live.setPadding(0,dp(2),0,dp(4)); hero.addView(live);
  status=tv("Veriler güncelleniyor…",11,Color.rgb(175,205,225),false); hero.addView(status);
  LinearLayout heroRow=new LinearLayout(this); heroRow.setGravity(Gravity.CENTER_VERTICAL);
  TextView heroIcon=tv("☀️",54,Color.WHITE,false); heroIcon.setGravity(17); heroRow.addView(heroIcon,new LinearLayout.LayoutParams(dp(78),dp(78)));
  LinearLayout heroText=new LinearLayout(this); heroText.setOrientation(LinearLayout.VERTICAL);
  TextView heroTemp=tv("—",42,Color.WHITE,true); heroText.addView(heroTemp);
  TextView heroEvent=tv("—",15,Color.rgb(215,230,240),false); heroText.addView(heroEvent);
  TextView heroFeel=tv("Hissedilen: —",11,Color.rgb(160,195,220),false); heroText.addView(heroFeel);
  heroRow.addView(heroText,new LinearLayout.LayoutParams(0,-2,1)); hero.addView(heroRow);
  LinearLayout metrics=new LinearLayout(this); metrics.setPadding(0,dp(10),0,0);
  TextView m1=tv("💧 Nem\n—%",12,Color.WHITE,true); m1.setGravity(17); m1.setBackground(bg(Color.rgb(8,39,66),14));
  TextView m2=tv("💨 Rüzgâr\n— km/sa",12,Color.WHITE,true); m2.setGravity(17); m2.setBackground(bg(Color.rgb(8,39,66),14));
  TextView m3=tv("📈 Basınç\n— hPa",12,Color.WHITE,true); m3.setGravity(17); m3.setBackground(bg(Color.rgb(8,39,66),14));
  LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,dp(62),1); mp.setMargins(dp(3),0,dp(3),0);
  metrics.addView(m1,mp); metrics.addView(m2,new LinearLayout.LayoutParams(0,dp(62),1)); metrics.addView(m3,new LinearLayout.LayoutParams(0,dp(62),1));
  hero.addView(metrics); root.addView(hero);
  root.addView(tv(" ",1,Color.TRANSPARENT,false));

  LinearLayout refresh=new LinearLayout(this); refresh.setGravity(Gravity.CENTER_VERTICAL); refresh.setPadding(dp(14),dp(10),dp(10),dp(10));
  refresh.setBackground(bg(Color.rgb(10,39,64),16));
  LinearLayout rt=new LinearLayout(this); rt.setOrientation(LinearLayout.VERTICAL);
  rt.addView(tv("Son güncelleme",13,Color.WHITE,true)); rt.addView(tv("MGM verilerini şimdi yenile",10,Color.rgb(155,185,210),false));
  refresh.addView(rt,new LinearLayout.LayoutParams(0,-2,1));
  TextView rb=tv("↻  YENİLE",13,Color.WHITE,true); rb.setGravity(17); rb.setPadding(dp(13),dp(10),dp(13),dp(10)); rb.setBackground(bg(Color.rgb(20,125,190),18));
  refresh.addView(rb,new LinearLayout.LayoutParams(dp(110),-2)); rb.setOnClickListener(v->load()); root.addView(refresh);

  title("🕒  SAATLİK TAHMİN");
  HorizontalScrollView hscroll=new HorizontalScrollView(this); hscroll.setHorizontalScrollBarEnabled(false);
  hourly=new LinearLayout(this); hourly.setOrientation(LinearLayout.HORIZONTAL); hscroll.addView(hourly); root.addView(hscroll);

  title("📅  5 GÜNLÜK TAHMİN");
  five=new LinearLayout(this); five.setOrientation(LinearLayout.VERTICAL); root.addView(five);

  title("📍  EDİRNE İLÇELERİ");
  TextView hint=tv("İlçeye dokunarak tahmini açıp kapatabilirsiniz.",11,Color.rgb(145,175,200),false); hint.setPadding(dp(3),0,dp(3),dp(7)); root.addView(hint);
  current=new LinearLayout(this); current.setOrientation(LinearLayout.HORIZONTAL);
  HorizontalScrollView cs=new HorizontalScrollView(this); cs.setHorizontalScrollBarEnabled(false); cs.addView(current); root.addView(cs);
  dist=new LinearLayout(this); dist.setOrientation(LinearLayout.VERTICAL); root.addView(dist);

  progress=new ProgressBar(this); progress.setIndeterminate(true); progress.setVisibility(View.VISIBLE);
  root.addView(progress,new LinearLayout.LayoutParams(-1,dp(3)));
  updated=tv("",10,Color.rgb(120,150,175),false); updated.setGravity(17); updated.setPadding(0,dp(12),0,dp(5)); root.addView(updated);
  TextView ftr=tv("TAKİPTE KAL, HAVADAN HABERDAR OL!",12,Color.WHITE,true); ftr.setGravity(17); ftr.setPadding(0,dp(12),0,dp(5)); root.addView(ftr);
  LinearLayout social=new LinearLayout(this); social.setGravity(17);
  int[] icons={R.drawable.ic_facebook,R.drawable.ic_instagram,R.drawable.ic_x,R.drawable.ic_youtube};
  String[] names={"Facebook","Instagram","X","YouTube"};
  String[] urls={"https://www.facebook.com/edirnehavadurumu","https://www.instagram.com/edirnehavadurumu/","https://x.com/edirnehavadurumu","https://www.youtube.com/@edirnehavadurumu"};
  for(int i=0;i<4;i++){ ImageButton b=socialIcon(icons[i],names[i],urls[i]); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(48),dp(48)); if(i>0)p.setMargins(dp(7),0,0,0); social.addView(b,p); }
  root.addView(social);
  TextView design=tv("Design by Edirnehavadurumugroup • 2026",9,Color.rgb(105,135,160),false); design.setGravity(17); root.addView(design);

  final TextView[] heroViews={heroIcon,heroTemp,heroEvent,heroFeel,m1,m2,m3};
  hero.setTag(heroViews);
 }
 }
 ImageButton socialIcon(int res,String desc,String url){ImageButton b=new ImageButton(this);b.setImageResource(res);b.setBackgroundColor(Color.TRANSPARENT);b.setPadding(dp(3),dp(3),dp(3),dp(3));b.setScaleType(ImageView.ScaleType.CENTER_INSIDE);b.setContentDescription(desc);b.setOnClickListener(v->open(url));return b;}
 String appVersion(){try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(Exception e){return "8.0";}}
 void checkForUpdate(boolean manual){
  ex.execute(()->{
   try{
    String url="https://raw.githubusercontent.com/hkndmrl22-bit/Edirnehavadurumu2/main/docs/version.json?ts="+System.currentTimeMillis();
    String body=Jsoup.connect(url).ignoreContentType(true).timeout(15000)
      .userAgent("EdirneHavaDurumu/10.1 Android")
      .header("Accept","application/json")
      .execute().body();
    JSONObject j=new JSONObject(body);
    android.content.pm.PackageInfo pi=getPackageManager().getPackageInfo(getPackageName(),0);
    int localCode=pi.versionCode; String localName=pi.versionName;
    int remoteCode=j.optInt("versionCode",localCode); String remoteName=j.optString("versionName","");
    String notes=j.optString("notes","Yeni sürüm yayınlandı.");
    String apk=j.optString("apkUrl","https://github.com/hkndmrl22-bit/Edirnehavadurumu2/releases/latest/download/EdirneHavaDurumu.apk");
    main.post(()->{
     if(remoteCode>localCode) new AlertDialog.Builder(MainActivity.this).setTitle("🎉 Yeni sürüm mevcut")
       .setMessage("Edirne Hava Durumu uygulamasının yeni sürümü ("+remoteName+") yayınlandı.\\n\\n"+notes)
       .setNegativeButton("Daha sonra",null)
       .setPositiveButton("GÜNCELLE",(d,w)->open(apk)).show();
     else if(manual) Toast.makeText(MainActivity.this,"Uygulamanız güncel. • Sürüm "+localName,Toast.LENGTH_LONG).show();
    });
   }catch(Exception e){
    if(manual) main.post(()->Toast.makeText(MainActivity.this,"Güncelleme kontrolü başarısız: "+e.getMessage(),Toast.LENGTH_LONG).show());
   }
  });
 }
 void load(){status.setText("Veriler güncelleniyor…");progress.setVisibility(View.VISIBLE);ex.execute(()->{try{
   ArrayList<Loc> all=new ArrayList<>();
   for(int i=0;i<D.length;i++) all.add(apiLocation(D[i],i==0?"merkez":Q[i].toLowerCase(Locale.ROOT)));
   Loc center=all.get(0);
   main.post(()->{progress.setVisibility(View.GONE);renderCurrent(all);renderHourly(center);renderCenter(center);renderDistricts(all);status.setText("Veriler güncellendi. • "+currentTime());});
  }catch(Exception e){main.post(()->{progress.setVisibility(View.GONE);status.setText("Veriler alınamadı. Yenile'ye basın.");Toast.makeText(this,"Bağlantı başarısız",0).show();});}});
 }
 Loc apiLocation(String name,String district)throws Exception{
   String q="il=edirne&ilce="+java.net.URLEncoder.encode(district,"UTF-8");
   JSONArray stations=new JSONArray(apiGet(API+"merkezler?"+q));
   if(stations.length()==0) throw new Exception("MGM istasyon bulunamadı: "+name);
   JSONObject st=stations.getJSONObject(0);
   int merkezId=st.optInt("merkezId",0);
   int istNo=st.optInt("gunlukTahminIstNo",0);
   int hourlyIstNo=st.optInt("saatlikTahminIstNo",0);
   if(merkezId==0) merkezId=istNo;
   if(istNo==0) istNo=merkezId;
   Loc l=new Loc(name);
   JSONArray curA=new JSONArray(apiGet(API+"sondurumlar?merkezid="+merkezId));
   if(curA.length()>0){
     JSONObject c=curA.getJSONObject(0);
     String temp=num(c,"sicaklik"), code=c.optString("hadiseKodu",""); l.humidity=num(c,"nem"); l.pressure=pressure(c); l.wind=num(c,"ruzgarHiz"); l.gust=num(c,"ruzgarHamle"); l.feels=num(c,"hissedilenSicaklik"); l.windDir=c.optString("ruzgarYon","");
     l.now=temp.isEmpty()?"":temp+"°C";
     l.nowEvent=condition(code);
     l.nowTime=measurementTime(c);
   }
   JSONArray dayA=new JSONArray(apiGet(API+"tahminler/gunluk?istno="+istNo));
   if(dayA.length()>0){
     JSONObject j=dayA.getJSONObject(0);
     for(int i=1;i<=5;i++){
       String lo=num(j,"enDusukGun"+i), hi=num(j,"enYuksekGun"+i);
       String code=j.optString("hadiseGun"+i,"");
       String date=formatDay(j.optString("tarihGun"+i,""));
       if(!lo.isEmpty()&&!hi.isEmpty()) l.days.add(new Day(date,condition(code),lo,hi));
     }
   }
   try{
     l.hours.clear();
     int hno=hourlyIstNo>0?hourlyIstNo:istNo;
     JSONArray ha=new JSONArray(apiGet(API+"tahminler/saatlik?istno="+hno));
     if(ha.length()>0){
       JSONArray ta=ha.getJSONObject(0).optJSONArray("tahmin");
       if(ta!=null) for(int z=0;z<ta.length()&&z<12;z++){
         JSONObject h=ta.getJSONObject(z);
         String ht=timeOnly(formatUtc(h.optString("tarih","")));
         String hv=num(h,"sicaklik");
         if(!ht.isEmpty()||!hv.isEmpty()) l.hours.add(new Hour(ht,hv,condition(h.optString("hadise","")),num(h,"ruzgarHizi")));
       }
     }
   }catch(Exception ignored){}
   return l;
 }
 String apiGet(String u)throws Exception{
   return Jsoup.connect(u).ignoreContentType(true).timeout(20000)
     .userAgent("Mozilla/5.0 (Android) EdirneHavaDurumu")
     .header("Accept","application/json, text/plain, */*")
     .header("Origin","https://www.mgm.gov.tr")
     .header("Referer","https://www.mgm.gov.tr/")
     .execute().body();
 }
 String num(JSONObject j,String k){
   if(!j.has(k)||j.isNull(k))return"";
   String s=String.valueOf(j.opt(k)); if(s.equals("-9999"))return"";
   try{double d=Double.parseDouble(s.replace(",","."));if(d==Math.rint(d))return String.valueOf((int)d);return String.format(Locale.US,"%.1f",d).replace(".0","");}catch(Exception e){java.util.regex.Matcher m=java.util.regex.Pattern.compile("-?\\d+(?:[.,]\\d+)?").matcher(s);return m.find()?m.group().replace(",","." ): "";}
 }
 String pressure(JSONObject j){
   String v=num(j,"basinc"); if(!v.isEmpty())return v;
   String[] keys={"basınç","pressure","basincHpa","basincDegeri","istasyonBasinc","denizSeviyesineIndirgenmisBasinc"};
   for(String k:keys){v=num(j,k);if(!v.isEmpty())return v;}
   java.util.Iterator<String> it=j.keys(); while(it.hasNext()){String k=it.next();String n=k.toLowerCase(Locale.ROOT); if(n.contains("basinc")||n.contains("basınc")){v=num(j,k);if(!v.isEmpty())return v;}}
   return "";
 }
 String condition(String c){
   if(c==null)c="";c=c.toUpperCase(Locale.ROOT);
   String[] k={"PB","GSY","HSY","SY","A","AB","CB","D","HY","HKY","MSY","KKY","GKR","SCK","PUS","Y","K","DY","R","KKR","SGK","SIS","KY","KSY","YKY","KF","KGY"};
   String[] v={"Parçalı Bulutlu","Gökgürültülü Sağanak Yağışlı","Hafif Sağanak Yağışlı","Sağanak Yağışlı","Açık","Az Bulutlu","Çok Bulutlu","Duman","Hafif Yağmurlu","Hafif Kar Yağışlı","Yer Yer Sağanak Yağışlı","Karla Karışık Yağmurlu","Güneyli Kuvvetli Rüzgar","Sıcak","PUS","Yağmurlu","Kar Yağışlı","Dolu","Rüzgarlı","Kuzeyli Kuvvetli Rüzgar","Soğuk","Sis","Kuvvetli Yağmurlu","Kuvvetli Sağanak Yağışlı","Yoğun Kar Yağışlı","Toz veya Kum Fırtınası","Kuvvetli Gökgürültülü Sağanak Yağışlı"};
   for(int i=0;i<k.length;i++)if(k[i].equals(c))return v[i];return c;
 }
 String currentTime(){SimpleDateFormat f=new SimpleDateFormat("HH:mm",new Locale("tr","TR"));f.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));return f.format(new Date());}
 String formatUtc(String s){
   if(s==null||s.isEmpty())return"";
   try{SimpleDateFormat in=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);in.setTimeZone(TimeZone.getTimeZone("UTC"));Date d=in.parse(s);SimpleDateFormat out=new SimpleDateFormat("dd.MM.yyyy HH:mm",new Locale("tr","TR"));out.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));return out.format(d);}catch(Exception e){return s;}
 }
 String measurementTime(JSONObject c){
   String[] keys={"veriZamani","sonVeriZamani","olcumZamani","olcmeZamani","tarihSaat","dateTime","denizVeriZamani"};
   for(String k:keys){String s=c.optString(k,"");if(s!=null&&!s.isEmpty()&&!s.equals("-9999")){String t=formatUtc(s);String h=timeOnly(t);if(h.matches("\\d{2}:\\d{2}"))return h;}}
   return "";
 }
 String timeOnly(String s){if(s==null||s.isEmpty())return"";java.util.regex.Matcher m=java.util.regex.Pattern.compile("(\\d{2}:\\d{2})(?::\\d{2})?").matcher(s);return m.find()?m.group(1):"";}
 String formatDay(String s){
   if(s==null||s.isEmpty())return"";
   try{SimpleDateFormat in=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);in.setTimeZone(TimeZone.getTimeZone("UTC"));Date d=in.parse(s);SimpleDateFormat out=new SimpleDateFormat("dd MMM",new Locale("tr","TR"));out.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));return out.format(d);}catch(Exception e){return s.length()>=10?s.substring(8,10)+"."+s.substring(5,7):s;}
 }
 void renderCurrent(ArrayList<Loc>a){
   current.removeAllViews();
   for(Loc l:a){
     LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(9),dp(9),dp(9),dp(9));card.setBackground(bg(Color.rgb(15,48,79),16));
     LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(190),dp(250));p.setMargins(0,0,dp(8),0);card.setLayoutParams(p);
     TextView nm=tv(l.name,14,Color.WHITE,true);nm.setGravity(17);card.addView(nm);card.addView(tv(icon(l.nowEvent,l.nowTime),30,Color.WHITE,false));
     TextView temp=tv(l.now.isEmpty()?"—":l.now,25,Color.rgb(255,196,55),true);temp.setGravity(17);card.addView(temp);
     TextView ev=tv(l.nowEvent.isEmpty()?"—":l.nowEvent,10,Color.rgb(205,220,235),false);ev.setGravity(17);card.addView(ev);
     TextView mt=tv(l.nowTime.isEmpty()?"Saat: —":"Saat: "+timeOnly(l.nowTime),10,Color.rgb(145,175,200),false);mt.setGravity(17);card.addView(mt);
     TextView wx=tv("💨 "+val(l.wind,"—")+" km/sa",10,Color.rgb(180,205,225),false);wx.setGravity(17);card.addView(wx);
     TextView wd=tv("🧭 "+val(l.windDir,"—"),10,Color.rgb(180,205,225),false);wd.setGravity(17);card.addView(wd);
     TextView hx=tv("💧 Nem: "+val(l.humidity,"—")+"%",11,Color.rgb(190,215,235),false);hx.setGravity(17);card.addView(hx);
     TextView px=tv("📈 Basınç: "+val(l.pressure,"—")+" hPa",11,Color.rgb(190,215,235),true);px.setGravity(17);card.addView(px);
     TextView fx=tv("🌡️ Hissedilen: "+val(l.feels,"—")+"°C",11,Color.rgb(190,215,235),false);fx.setGravity(17);card.addView(fx);current.addView(card);
   }
 }
 void renderHourly(Loc l){
   hourly.removeAllViews();
   if(l.hours.size()==0){TextView t=tv("Saatlik MGM tahmini şu anda alınamadı.",13,Color.LTGRAY,false);t.setPadding(dp(10),dp(12),dp(10),dp(12));hourly.addView(t);return;}
   for(Hour x:l.hours){
     LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(9),dp(10),dp(9),dp(10));card.setBackground(bg(Color.rgb(15,48,79),14));
     LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(105),dp(145));p.setMargins(0,0,dp(8),0);card.setLayoutParams(p);
     card.addView(tv(x.time.isEmpty()?"—":x.time,13,Color.WHITE,true));
     TextView ic=tv(icon(x.event,x.time),28,Color.WHITE,false);ic.setGravity(17);card.addView(ic);
     TextView temp=tv(x.temp.isEmpty()?"—":x.temp+"°",23,Color.rgb(255,196,55),true);temp.setGravity(17);card.addView(temp);
     TextView ev=tv(x.event.isEmpty()?"—":x.event,9,Color.rgb(205,220,235),false);ev.setGravity(17);card.addView(ev);
     TextView wi=tv(x.wind.isEmpty()?"":"💨 "+x.wind+" km/sa",9,Color.rgb(150,180,205),false);wi.setGravity(17);card.addView(wi);
     hourly.addView(card);
   }
 }
 void renderDetails(Loc l){details.removeAllViews();renderHourly(l);String[] x={"💧 Nem: "+val(l.humidity,"—")+"%","🌡️ Hissedilen: "+val(l.feels,"—")+"°C","💨 Rüzgâr: "+val(l.wind,"—")+" km/sa  •  "+val(l.windDir,"—"),"💨 Rüzgâr hamlesi: "+val(l.gust,"—")+" km/sa","📈 Basınç: "+val(l.pressure,"—")+" hPa"};for(String q:x){TextView t=tv(q,13,Color.WHITE,false);t.setPadding(dp(12),dp(9),dp(12),dp(9));t.setBackground(bg(Color.rgb(15,48,79),12));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(2),0,dp(2));details.addView(t,p);}}
 String val(String x,String d){return x==null||x.isEmpty()?d:x;}
 String windDirection(String x){if(x==null||x.isEmpty()||x.equals("-9999"))return "—"; try{double d=Double.parseDouble(x.replace(",",".")); int i=(int)Math.round(d/22.5)%16; String[] sixteen={"K","KKD","KD","DKD","D","DGD","GD","GBD","BGB","B","KB","KGB","K"}; if(i>=0&&i<sixteen.length)return sixteen[i];}catch(Exception e){} return x;}
 void renderCenter(Loc l){five.removeAllViews();if(l.days.size()==0){five.addView(tv("Edirne Merkez 5 günlük tahmin okunamadı.",13,Color.LTGRAY,false));return;}for(Day x:l.days)five.addView(card(x,false));}
 void renderDistricts(ArrayList<Loc>a){
   dist.removeAllViews();for(int i=1;i<a.size();i++){Loc l=a.get(i);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);
     TextView z=tv("▶  "+l.name+"   •   Tahmini aç",15,Color.WHITE,true);z.setPadding(dp(12),dp(13),dp(10),dp(13));z.setBackground(bg(Color.rgb(15,48,79),13));box.addView(z);
     LinearLayout days=new LinearLayout(this);days.setOrientation(LinearLayout.VERTICAL);days.setPadding(dp(4),dp(2),dp(4),dp(3));days.setVisibility(View.GONE);for(Day x:l.days)days.addView(card(x,true));box.addView(days);
     z.setOnClickListener(v->{boolean open=days.getVisibility()!=View.VISIBLE;days.setVisibility(open?View.VISIBLE:View.GONE);z.setText((open?"▼  ":"▶  ")+l.name+"   •   Tahmini "+(open?"kapat":"aç"));});
     LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.setMargins(0,dp(4),0,dp(4));box.setLayoutParams(bp);dist.addView(box);
   }
 }
 View card(Day x,boolean small){
   LinearLayout c=new LinearLayout(this);c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(10),dp(8),dp(10),dp(8));c.setBackground(bg(Color.rgb(15,48,79),12));
   LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,dp(2),0,dp(2));c.setLayoutParams(cp);
   LinearLayout left=new LinearLayout(this);left.setOrientation(LinearLayout.VERTICAL);left.addView(tv(x.date,small?12:13,Color.WHITE,true));left.addView(tv(dayName(x.date),10,Color.rgb(150,180,205),false));c.addView(left,new LinearLayout.LayoutParams(dp(small?100:112),-2));
   c.addView(tv(icon(x.e),small?23:26,Color.WHITE,false),new LinearLayout.LayoutParams(dp(40),-2));c.addView(tv(x.e,small?10:11,Color.rgb(205,220,235),false),new LinearLayout.LayoutParams(0,-2,1));
   LinearLayout tt=new LinearLayout(this);tt.setOrientation(LinearLayout.VERTICAL);tt.setGravity(Gravity.CENTER_VERTICAL);tt.addView(tv("↓ "+x.mi+"°",small?15:16,Color.rgb(85,195,255),true));tt.addView(tv("↑ "+x.ma+"°",small?15:16,Color.rgb(255,135,75),true));c.addView(tt);return c;
 }
 String dayName(String s){if(s==null||s.isEmpty())return"";try{String[] p=s.split(" ");if(p.length>0){int d=Integer.parseInt(p[0]);Calendar cal=Calendar.getInstance();cal.set(Calendar.DAY_OF_MONTH,d);String m=p.length>1?p[1]:"";String[] tr={"Oca","Şub","Mar","Nis","May","Haz","Tem","Ağu","Eyl","Eki","Kas","Ara"};for(int i=0;i<tr.length;i++)if(tr[i].equals(m)){cal.set(Calendar.MONTH,i);break;}return new SimpleDateFormat("EEEE",new Locale("tr","TR")).format(cal.getTime());}}catch(Exception e){}return"";}
 String icon(String e){return icon(e,"");}
 String icon(String e,String time){String x=e.toLowerCase(new Locale("tr"));if(x.contains("gök")||x.contains("şimşek"))return"⛈️";if(x.contains("kar"))return"🌨️";if(x.contains("sağanak")||x.contains("yağış")||x.contains("yağmur"))return"🌧️";if(x.contains("sis"))return"🌫️";if(x.contains("rüzgar"))return"🌬️";if(x.contains("çok bulutlu")||x.contains("kapalı"))return"☁️";if(x.contains("parçalı"))return"⛅";if(x.contains("az bulutlu"))return"🌤️";if(x.contains("açık")){int h=-1;try{if(time!=null&&time.length()>=13)h=Integer.parseInt(time.substring(11,13));}catch(Exception z){}if(h>=0&&(h>=20||h<6))return"🌙";return"☀️";}return"☀️";}
 void open(String u){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}catch(Exception e){}}
 @Override protected void onDestroy(){timer.removeCallbacks(refresh5m);ex.shutdownNow();super.onDestroy();}
 static class Day{String date,e,mi,ma;Day(String d,String x,String a,String b){date=d;e=x;mi=a;ma=b;}}
 static class Loc{String name,now="",nowTime="",nowEvent="",humidity="",pressure="",wind="",gust="",feels="",windDir="";ArrayList<Day>days=new ArrayList<>();ArrayList<Hour>hours=new ArrayList<>();Loc(String n){name=n;}}
 static class Hour{String time,temp,event,wind;Hour(String a,String b,String c,String d){time=a;temp=b;event=c;wind=d;}}
 static class Current{String time,temp,event;Current(String t,String v,String e){time=t;temp=v;event=e;}}
}