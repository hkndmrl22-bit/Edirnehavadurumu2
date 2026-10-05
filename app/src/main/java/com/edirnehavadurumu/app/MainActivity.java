package com.edirnehavadurumu.app;

import android.app.*;import android.os.*;import android.graphics.*;import android.util.Base64;import android.graphics.drawable.*;import android.view.*;import android.content.*;import android.net.*;import android.widget.*;import java.util.*;import java.util.concurrent.*;import java.text.*;import org.json.*;import org.jsoup.*;

public class MainActivity extends Activity{
 static final String API="https://servis.mgm.gov.tr/web/";
 ExecutorService ex=Executors.newSingleThreadExecutor(); Handler main=new Handler();
 LinearLayout root,current,five,dist; TextView status,updated; ProgressBar progress;
 String[] D={"Edirne Merkez","Enez","Havsa","İpsala","Keşan","Lalapaşa","Meriç","Süloğlu","Uzunköprü"};
 String[] Q={"","ENEZ","HAVSA","IPSALA","KESAN","LALAPASA","MERIC","SULOGLU","UZUNKOPRU"};
 int dp(float x){return(int)(x*getResources().getDisplayMetrics().density+.5f);}
 TextView tv(String s,float z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setTypeface(Typeface.DEFAULT,b?1:0);t.setPadding(dp(5),dp(3),dp(5),dp(3));return t;}
 GradientDrawable bg(int c,int r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
 void title(String s){TextView t=tv(s,19,-1,true);t.setPadding(dp(2),dp(15),dp(2),dp(7));root.addView(t);}
 @Override public void onCreate(Bundle b){super.onCreate(b);ui();load();}
 void ui(){
  ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(8),dp(12),dp(22));root.setBackgroundColor(Color.rgb(7,24,45));sc.addView(root);setContentView(sc);
  LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);h.setPadding(0,dp(8),0,dp(8));
  LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.setPadding(dp(2),0,dp(8),0);
  ImageView logo=new ImageView(this);
  logo.setImageBitmap(BitmapFactory.decodeByteArray(Base64.decode("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", Base64.DEFAULT),0,Base64.decode("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", Base64.DEFAULT).length));
  logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
  logo.setAdjustViewBounds(true);
  tx.addView(logo,new LinearLayout.LayoutParams(dp(92),dp(92)));
  tx.addView(tv("Yerel Hava Tahmin Uygulaması",17,Color.rgb(180,210,235),true));
  h.addView(tx,new LinearLayout.LayoutParams(0,-2,1));
  Button r=new Button(this);r.setText("VERİLERİ ANLIK YENİLE");r.setOnClickListener(v->load());h.addView(r);root.addView(h);
  status=tv("Veriler güncelleniyor…",14,Color.LTGRAY,false);root.addView(status);progress=new ProgressBar(this);progress.setIndeterminate(true);root.addView(progress);
  title("🌡️ Edirne Merkez ve İlçeler • Son Durum");
  HorizontalScrollView hs=new HorizontalScrollView(this);current=new LinearLayout(this);current.setOrientation(LinearLayout.HORIZONTAL);hs.addView(current);root.addView(hs);
  title("📅 Edirne Merkez • 5 Günlük Tahmin");five=new LinearLayout(this);five.setOrientation(LinearLayout.VERTICAL);root.addView(five);
  title("📍 İlçeler • 5 Günlük Tahmin");
  TextView hint=tv("Bir ilçeye dokunun, 5 günlük tahminini açın.",12,Color.LTGRAY,false);root.addView(hint);
  dist=new LinearLayout(this);dist.setOrientation(LinearLayout.VERTICAL);root.addView(dist);
  updated=tv("",12,Color.LTGRAY,false);root.addView(updated);
  TextView f=tv("Bizi takip edin",16,-1,true);f.setGravity(17);f.setPadding(0,dp(12),0,dp(4));root.addView(f);
  LinearLayout s=new LinearLayout(this);s.setGravity(17);s.setPadding(0,dp(4),0,dp(8));
  ImageButton fb=new ImageButton(this);fb.setImageResource(R.drawable.ic_facebook);fb.setBackgroundColor(Color.TRANSPARENT);fb.setPadding(dp(2),dp(2),dp(2),dp(2));fb.setScaleType(ImageView.ScaleType.CENTER_INSIDE);fb.setOnClickListener(v->open("https://www.facebook.com/edirnehavadurumu"));s.addView(fb,new LinearLayout.LayoutParams(dp(76),dp(76)));
  ImageButton ig=new ImageButton(this);ig.setImageResource(R.drawable.ic_instagram);ig.setBackgroundColor(Color.TRANSPARENT);ig.setPadding(dp(2),dp(2),dp(2),dp(2));ig.setScaleType(ImageView.ScaleType.CENTER_INSIDE);ig.setOnClickListener(v->open("https://www.instagram.com/edirnehavadurumu/"));LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(76),dp(76));ip.setMargins(dp(14),0,0,0);s.addView(ig,ip);root.addView(s);
  TextView design=tv("Design by Edirnehavadurumugroup 2026",12,Color.rgb(155,175,195),false);design.setGravity(17);design.setPadding(0,dp(4),0,dp(12));root.addView(design);
 }
 void load(){status.setText("Veriler güncelleniyor…");progress.setVisibility(View.VISIBLE);ex.execute(()->{try{
   ArrayList<Loc> all=new ArrayList<>();
   for(int i=0;i<D.length;i++) all.add(apiLocation(D[i],i==0?"merkez":Q[i].toLowerCase(Locale.ROOT)));
   Loc center=all.get(0);
   main.post(()->{progress.setVisibility(View.GONE);renderCurrent(all);renderCenter(center);renderDistricts(all);status.setText("Veriler güncellendi. • "+currentTime());});
  }catch(Exception e){main.post(()->{progress.setVisibility(View.GONE);status.setText("Veriler alınamadı. Yenile'ye basın.");Toast.makeText(this,"Bağlantı başarısız",0).show();});}});
 }
 Loc apiLocation(String name,String district)throws Exception{
   String q="il=edirne&ilce="+java.net.URLEncoder.encode(district,"UTF-8");
   JSONArray stations=new JSONArray(apiGet(API+"merkezler?"+q));
   if(stations.length()==0) throw new Exception("MGM istasyon bulunamadı: "+name);
   JSONObject st=stations.getJSONObject(0);
   int merkezId=st.optInt("merkezId",0);
   int istNo=st.optInt("gunlukTahminIstNo",0);
   if(merkezId==0) merkezId=istNo;
   if(istNo==0) istNo=merkezId;
   Loc l=new Loc(name);
   JSONArray curA=new JSONArray(apiGet(API+"sondurumlar?merkezid="+merkezId));
   if(curA.length()>0){
     JSONObject c=curA.getJSONObject(0);
     String temp=num(c,"sicaklik"), code=c.optString("hadiseKodu","");
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
     LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(17);c.setPadding(dp(8),dp(8),dp(8),dp(8));c.setBackground(bg(Color.rgb(20,48,78),14));
     LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(142),dp(160));p.setMargins(0,0,dp(7),0);c.setLayoutParams(p);
     c.addView(tv(l.name,14,-1,true));String e=l.nowEvent;c.addView(tv(icon(e,l.nowTime),26,-1,false));c.addView(tv(l.now.isEmpty()?"—":l.now,20,Color.rgb(255,193,7),true));c.addView(tv(e.isEmpty()?"—":e,10,Color.LTGRAY,false));c.addView(tv(l.nowTime.isEmpty()?"Ölçüm: —":"Ölçüm: "+timeOnly(l.nowTime),11,Color.LTGRAY,false));current.addView(c);
   }
 }
 void renderCenter(Loc l){five.removeAllViews();if(l.days.size()==0){five.addView(tv("Edirne Merkez 5 günlük tahmin okunamadı.",13,Color.LTGRAY,false));return;}for(Day x:l.days)five.addView(card(x,false));}
 void renderDistricts(ArrayList<Loc>a){
   dist.removeAllViews();
   for(int i=1;i<a.size();i++){
     Loc l=a.get(i);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);
     TextView z=tv("▶ "+l.name+"   •   5 günlük tahmini aç",16,-1,true);z.setPadding(dp(10),dp(12),dp(10),dp(12));z.setBackground(bg(Color.rgb(20,48,78),12));box.addView(z);
     LinearLayout days=new LinearLayout(this);days.setOrientation(LinearLayout.VERTICAL);days.setVisibility(View.GONE);for(Day x:l.days)days.addView(card(x,true));box.addView(days);
     z.setOnClickListener(v->{boolean open=days.getVisibility()!=View.VISIBLE;days.setVisibility(open?View.VISIBLE:View.GONE);z.setText((open?"▼ ":"▶ ")+l.name+"   •   5 günlük tahmini "+(open?"kapat":"aç"));});
     LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.setMargins(0,dp(3),0,dp(3));box.setLayoutParams(bp);dist.addView(box);
   }
 }
 View card(Day x,boolean small){LinearLayout c=new LinearLayout(this);c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(8),dp(7),dp(8),dp(7));c.setBackground(bg(Color.rgb(20,48,78),12));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,dp(2),0,dp(2));c.setLayoutParams(cp);c.addView(tv(x.date,small?12:13,-1,true),new LinearLayout.LayoutParams(dp(small?118:125),-2));c.addView(tv(icon(x.e),small?21:24,-1,false),new LinearLayout.LayoutParams(dp(38),-2));c.addView(tv(x.e,small?11:12,Color.LTGRAY,false),new LinearLayout.LayoutParams(0,-2,1));LinearLayout tt=new LinearLayout(this);tt.setOrientation(LinearLayout.VERTICAL);tt.addView(tv("↓ "+x.mi+"°",small?15:16,Color.rgb(80,190,255),true));tt.addView(tv("↑ "+x.ma+"°",small?15:16,Color.rgb(255,130,70),true));c.addView(tt);return c;}
 String icon(String e){return icon(e,"");}
 String icon(String e,String time){String x=e.toLowerCase(new Locale("tr"));if(x.contains("gök")||x.contains("şimşek"))return"⛈️";if(x.contains("kar"))return"🌨️";if(x.contains("sağanak")||x.contains("yağış")||x.contains("yağmur"))return"🌧️";if(x.contains("sis"))return"🌫️";if(x.contains("rüzgar"))return"🌬️";if(x.contains("çok bulutlu")||x.contains("kapalı"))return"☁️";if(x.contains("parçalı"))return"⛅";if(x.contains("az bulutlu"))return"🌤️";if(x.contains("açık")){int h=-1;try{if(time!=null&&time.length()>=13)h=Integer.parseInt(time.substring(11,13));}catch(Exception z){}if(h>=0&&(h>=20||h<6))return"🌙";return"☀️";}return"☀️";}
 void open(String u){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}catch(Exception e){}}
 @Override protected void onDestroy(){ex.shutdownNow();super.onDestroy();}
 static class Day{String date,e,mi,ma;Day(String d,String x,String a,String b){date=d;e=x;mi=a;ma=b;}}
 static class Loc{String name,now="",nowTime="",nowEvent="";ArrayList<Day>days=new ArrayList<>();Loc(String n){name=n;}}
 static class Current{String time,temp,event;Current(String t,String v,String e){time=t;temp=v;event=e;}}
}