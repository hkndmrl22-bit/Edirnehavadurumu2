package com.edirnehavadurumu.app;

import android.app.*;import android.os.*;import android.graphics.*;import android.util.Base64;import android.graphics.drawable.*;import android.view.*;import android.content.*;import android.net.*;import android.widget.*;import java.util.*;import java.util.concurrent.*;import java.text.*;import org.json.*;import org.jsoup.*;

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
 @Override public void onCreate(Bundle b){super.onCreate(b);ui();load();timer.postDelayed(refresh5m,300000);}
 void ui(){
  ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
  root.setPadding(dp(14),dp(8),dp(14),dp(26));root.setBackgroundColor(Color.rgb(5,20,38));sc.setFillViewport(true);sc.addView(root);setContentView(sc);
  LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.HORIZONTAL);hero.setGravity(Gravity.CENTER_VERTICAL);hero.setPadding(dp(4),dp(8),dp(4),dp(10));
  ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.edirne_logo_app);logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);hero.addView(logo,new LinearLayout.LayoutParams(dp(88),dp(88)));
  LinearLayout ht=new LinearLayout(this);ht.setOrientation(LinearLayout.VERTICAL);ht.setPadding(dp(10),0,dp(6),0);
  ht.addView(tv("EDİRNE HAVA DURUMU",20,Color.WHITE,true));ht.addView(tv("EDİRNE YEREL HAVA TAHMİN UYGULAMASI",12,Color.rgb(175,205,230),false));
  
  hero.addView(ht,new LinearLayout.LayoutParams(0,-2,1));root.addView(hero);
  LinearLayout refresh=new LinearLayout(this);refresh.setGravity(Gravity.CENTER_VERTICAL);refresh.setPadding(dp(12),dp(9),dp(12),dp(9));refresh.setBackground(bg(Color.rgb(16,43,70),14));
  LinearLayout rt=new LinearLayout(this);rt.setOrientation(LinearLayout.VERTICAL);rt.addView(tv("Verileri yenile",14,Color.WHITE,true));rt.addView(tv("Anlık hava durumunu güncelle",11,Color.rgb(175,195,215),false));refresh.addView(rt,new LinearLayout.LayoutParams(0,-2,1));
  TextView rb=tv("↻  YENİLE",13,Color.WHITE,true);rb.setGravity(17);rb.setPadding(dp(12),dp(9),dp(12),dp(9));rb.setBackground(bg(Color.rgb(28,105,155),18));refresh.addView(rb,new LinearLayout.LayoutParams(dp(105),-2));rb.setOnClickListener(v->load());root.addView(refresh);
  title("🌡️  SON DURUMLAR");
  status=tv("Veriler güncelleniyor…",12,Color.rgb(170,195,215),false);status.setPadding(dp(3),dp(8),dp(3),dp(2));root.addView(status);
  progress=new ProgressBar(this);progress.setIndeterminate(true);progress.setVisibility(View.VISIBLE);root.addView(progress,new LinearLayout.LayoutParams(-1,dp(3)));
  HorizontalScrollView hs=new HorizontalScrollView(this);hs.setHorizontalScrollBarEnabled(false);current=new LinearLayout(this);current.setOrientation(LinearLayout.HORIZONTAL);hs.addView(current);root.addView(hs);
  title("🕒  SAATLİK TAHMİN • EDİRNE MERKEZ");
  HorizontalScrollView hscroll=new HorizontalScrollView(this);hscroll.setHorizontalScrollBarEnabled(false);hourly=new LinearLayout(this);hourly.setOrientation(LinearLayout.HORIZONTAL);hscroll.addView(hourly);root.addView(hscroll);
  title("📅  EDİRNE MERKEZ • 5 GÜNLÜK");five=new LinearLayout(this);five.setOrientation(LinearLayout.VERTICAL);root.addView(five);
  title("📍  İLÇELER");TextView hint=tv("İlçeye dokunarak 5 günlük tahmini açıp kapatabilirsiniz.",12,Color.rgb(165,190,210),false);hint.setPadding(dp(3),0,dp(3),dp(8));root.addView(hint);dist=new LinearLayout(this);dist.setOrientation(LinearLayout.VERTICAL);root.addView(dist);
  updated=tv("",11,Color.rgb(135,160,185),false);updated.setPadding(dp(3),dp(10),dp(3),dp(4));root.addView(updated);
  TextView ftr=tv("TAKİPTE KAL, HAVADAN HABERDAR OL!",13,Color.WHITE,true);ftr.setGravity(17);ftr.setPadding(0,dp(12),0,dp(4));root.addView(ftr);
  LinearLayout s=new LinearLayout(this);s.setGravity(17);s.setPadding(0,dp(4),0,dp(4));
  ImageButton fb=new ImageButton(this);fb.setImageResource(R.drawable.ic_facebook);fb.setBackgroundColor(Color.TRANSPARENT);fb.setScaleType(ImageView.ScaleType.CENTER_INSIDE);fb.setOnClickListener(v->open("https://www.facebook.com/edirnehavadurumu"));s.addView(fb,new LinearLayout.LayoutParams(dp(58),dp(58)));
  ImageButton ig=new ImageButton(this);ig.setImageResource(R.drawable.ic_instagram);ig.setBackgroundColor(Color.TRANSPARENT);ig.setScaleType(ImageView.ScaleType.CENTER_INSIDE);ig.setOnClickListener(v->open("https://www.instagram.com/edirnehavadurumu/"));LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(58),dp(58));ip.setMargins(dp(12),0,0,0);s.addView(ig,ip); TextView xb=tv("𝕏",21,Color.WHITE,true);xb.setGravity(17);xb.setOnClickListener(v->open("https://x.com/edirnehavadurumu"));LinearLayout.LayoutParams xp=new LinearLayout.LayoutParams(dp(58),dp(58));xp.setMargins(dp(12),0,0,0);s.addView(xb,xp); TextView yt=tv("▶",20,Color.WHITE,true);yt.setGravity(17);yt.setOnClickListener(v->open("https://www.youtube.com/@edirnehavadurumu"));LinearLayout.LayoutParams yp=new LinearLayout.LayoutParams(dp(58),dp(58));yp.setMargins(dp(12),0,0,0);s.addView(yt,yp);root.addView(s);
  TextView design=tv("Design by Edirnehavadurumugroup • 2026",10,Color.rgb(115,140,165),false);design.setGravity(17);design.setPadding(0,dp(3),0,dp(10));root.addView(design);
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