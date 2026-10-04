package com.edirnehavadurumu.app;

import android.app.*;import android.os.*;import android.graphics.*;import android.graphics.drawable.*;import android.view.*;import android.content.*;import android.net.*;import android.widget.*;import java.util.*;import java.util.concurrent.*;import org.jsoup.*;import org.jsoup.nodes.*;import org.jsoup.select.*;

public class MainActivity extends Activity{
 static final String HOURLY="https://www.mgm.gov.tr/tahmin/saatlik.aspx?m=EDIRNE";
 static final String DETAIL="https://www.mgm.gov.tr/tahmin/il-ve-ilceler.aspx?il=Edirne&ilce=";
 ExecutorService ex=Executors.newSingleThreadExecutor(); Handler main=new Handler();
 LinearLayout root,current,five,dist; TextView status,updated; ProgressBar progress;
 String[] D={"Edirne","Enez","Havsa","İpsala","Keşan","Lalapaşa","Meriç","Süloğlu","Uzunköprü"};
 String[] Q={"","Enez","Havsa","İpsala","Keşan","Lalapaşa","Meriç","Süloğlu","Uzunköprü"};
 int dp(float x){return(int)(x*getResources().getDisplayMetrics().density+.5f);}
 TextView tv(String s,float z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setTypeface(Typeface.DEFAULT,b?1:0);t.setPadding(dp(5),dp(3),dp(5),dp(3));return t;}
 GradientDrawable bg(int c,int r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
 void title(String s){TextView t=tv(s,19,-1,true);t.setPadding(dp(2),dp(15),dp(2),dp(7));root.addView(t);}
 @Override public void onCreate(Bundle b){super.onCreate(b);ui();load();}
 void ui(){
  ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(8),dp(12),dp(22));root.setBackgroundColor(Color.rgb(7,24,45));sc.addView(root);setContentView(sc);
  LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);
  ImageView im=new ImageView(this);im.setImageResource(R.drawable.edirne_logo_real);im.setScaleType(ImageView.ScaleType.CENTER_INSIDE);im.setAdjustViewBounds(true);h.addView(im,new LinearLayout.LayoutParams(dp(105),dp(105)));
  LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.addView(tv("Edirne Hava Durumu",22,-1,true));tx.addView(tv("/ edirnehavadurumu",14,Color.LTGRAY,false));tx.addView(tv("MGM verileri • Güncel tahminler",12,Color.LTGRAY,false));h.addView(tx,new LinearLayout.LayoutParams(0,-2,1));
  Button r=new Button(this);r.setText("↻ Yenile");r.setOnClickListener(v->load());h.addView(r);root.addView(h);
  status=tv("MGM verileri yükleniyor…",14,Color.LTGRAY,false);root.addView(status);progress=new ProgressBar(this);progress.setIndeterminate(true);root.addView(progress);
  title("🌡️ Edirne Merkez ve İlçeler • Son Durum");
  HorizontalScrollView hs=new HorizontalScrollView(this);current=new LinearLayout(this);current.setOrientation(LinearLayout.HORIZONTAL);hs.addView(current);root.addView(hs);
  title("📅 Edirne Merkez • 5 Günlük Tahmin");five=new LinearLayout(this);five.setOrientation(LinearLayout.VERTICAL);root.addView(five);
  title("📍 İlçeler • 5 Günlük Tahmin");
  TextView hint=tv("Bir ilçeye dokunun, 5 günlük tahminini açın.",12,Color.LTGRAY,false);root.addView(hint);
  dist=new LinearLayout(this);dist.setOrientation(LinearLayout.VERTICAL);root.addView(dist);
  updated=tv("",12,Color.LTGRAY,false);root.addView(updated);root.addView(tv("Veri kaynağı: Meteoroloji Genel Müdürlüğü (MGM)",12,Color.LTGRAY,false));
  TextView f=tv("Bizi takip edin",15,-1,true);f.setGravity(17);root.addView(f);LinearLayout s=new LinearLayout(this);s.setGravity(17);
  ImageButton fb=new ImageButton(this);fb.setImageResource(R.drawable.ic_facebook);fb.setBackgroundColor(Color.TRANSPARENT);fb.setOnClickListener(v->open("https://www.facebook.com/edirnehavadurumu"));s.addView(fb,new LinearLayout.LayoutParams(dp(58),dp(58)));
  ImageButton ig=new ImageButton(this);ig.setImageResource(R.drawable.ic_instagram);ig.setBackgroundColor(Color.TRANSPARENT);ig.setOnClickListener(v->open("https://www.instagram.com/edirnehavadurumu/"));s.addView(ig,new LinearLayout.LayoutParams(dp(58),dp(58)));root.addView(s);
 }
 void load(){status.setText("MGM verileri alınıyor…");progress.setVisibility(View.VISIBLE);ex.execute(()->{try{
   Document hd=Jsoup.connect(HOURLY).userAgent("Mozilla/5.0 EdirneHavaDurumu").timeout(20000).get();
   List<String> hs=row(hd,"Saat"),ev=row(hd,"Beklenen Hadise"),ts=row(hd,"Sıcaklık"),ns=row(hd,"Nem");
   final Loc center=detail(hd,"Edirne",true);
   if(center.days.size()<5) center.days=daysFromHourly(hd);
   ArrayList<Loc> all=new ArrayList<>();all.add(center);
   for(int i=1;i<D.length;i++){Loc l=detail(Jsoup.connect(DETAIL+java.net.URLEncoder.encode(Q[i],"UTF-8")).userAgent("Mozilla/5.0 EdirneHavaDurumu").timeout(20000).get(),D[i],false);all.add(l);}
   main.post(()->{progress.setVisibility(View.GONE);renderCurrent(all);renderCenter(center);renderDistricts(all);status.setText("MGM verileri başarıyla güncellendi.");updated.setText("Kaynak: MGM • Son durum ve 5 günlük tahminler");});
  }catch(Exception e){main.post(()->{progress.setVisibility(View.GONE);status.setText("MGM verisi alınamadı. Yenile'ye basın.");Toast.makeText(this,"MGM bağlantısı başarısız",0).show();});}});
 }
 Loc detail(Document d,String name,boolean center){
   Loc l=new Loc(name); ArrayList<String> dates=new ArrayList<>(); ArrayList<String> events=new ArrayList<>();ArrayList<String> mins=new ArrayList<>();ArrayList<String> maxs=new ArrayList<>();
   for(Element tr:d.select("tr")){
     Elements c=tr.select("th,td");String row=tr.text().trim();String low=row.toLowerCase(new Locale("tr"));
     if(low.contains("anlık durum")||low.contains("son durum")){
       String x=row.replaceAll("(?i).*?(anlık durum|son durum)","").trim();if(x.length()>0)l.now=x;
     }
     if(low.equals("tarih")||low.startsWith("tarih ")){for(int i=1;i<c.size();i++)dates.add(c.get(i).text().trim());}
     if(low.startsWith("hadise")){for(int i=1;i<c.size();i++){String x=c.get(i).text().trim();if(x.length()>0)events.add(x);}}
     if(low.startsWith("en düşük")){for(int i=1;i<c.size();i++)mins.add(c.get(i).text().trim());}
     if(low.startsWith("en yüksek")){for(int i=1;i<c.size();i++)maxs.add(c.get(i).text().trim());}
   }
   int n=Math.min(5,Math.min(dates.size(),Math.min(mins.size(),maxs.size())));
   for(int i=0;i<n;i++)l.days.add(new Day(dates.get(i),i<events.size()?events.get(i):"",mins.get(i),maxs.get(i)));
   if(l.now.isEmpty()){
     String temp=firstNumber(d.select("body").text(),"Sıcaklık");
     l.now=temp.isEmpty()?"":temp+"°C";
   }
   return l;
 }
 String firstNumber(String s,String label){int p=s.toLowerCase(new Locale("tr")).indexOf(label.toLowerCase(new Locale("tr")));if(p<0)return"";String x=s.substring(p);java.util.regex.Matcher m=java.util.regex.Pattern.compile("-?\\d+").matcher(x);return m.find()?m.group():"";}
 ArrayList<Day> daysFromHourly(Document d){ArrayList<Day>a=new ArrayList<>();ArrayList<String> t=row(d,"Sıcaklık");if(t.size()>0){Calendar q=Calendar.getInstance();String[]m={"Ocak","Şubat","Mart","Nisan","Mayıs","Haziran","Temmuz","Ağustos","Eylül","Ekim","Kasım","Aralık"};for(int i=0;i<5;i++){String x=i<t.size()?t.get(i):"-";a.add(new Day(q.get(Calendar.DAY_OF_MONTH)+" "+m[q.get(Calendar.MONTH)],"","",x));q.add(Calendar.DAY_OF_MONTH,1);}}return a;}
 List<String> row(Document d,String label){ArrayList<String>o=new ArrayList<>();for(Element tr:d.select("tr")){Elements c=tr.select(">th,>td");if(c.size()>0&&c.get(0).text().toLowerCase(new Locale("tr")).contains(label.toLowerCase(new Locale("tr")))){for(int i=1;i<c.size();i++)o.add(c.get(i).text().trim());break;}}return o;}
 void renderCurrent(ArrayList<Loc>a){current.removeAllViews();for(Loc l:a){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(17);c.setPadding(dp(8),dp(9),dp(8),dp(9));c.setBackground(bg(Color.rgb(20,48,78),14));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(122),dp(120));p.setMargins(0,0,dp(7),0);c.setLayoutParams(p);c.addView(tv(l.name,14,-1,true));String e=l.days.size()>0?l.days.get(0).e:"";c.addView(tv(icon(e),27,-1,false));String n=l.now;if(n.isEmpty()&&l.days.size()>0)n=l.days.get(0).ma+"°C";c.addView(tv(n,18,Color.rgb(255,193,7),true));c.addView(tv(e.isEmpty()?"MGM":e,10,Color.LTGRAY,false));current.addView(c);}}
 void renderCenter(Loc l){five.removeAllViews();if(l.days.size()==0){five.addView(tv("Edirne Merkez 5 günlük tahmin okunamadı.",13,Color.LTGRAY,false));return;}for(Day x:l.days)five.addView(card(x,false));}
 void renderDistricts(ArrayList<Loc>a){dist.removeAllViews();for(int i=1;i<a.size();i++){Loc l=a.get(i);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);TextView z=tv("▶ "+l.name+"   •   5 günlük tahmini aç",16,-1,true);z.setPadding(dp(10),dp(12),dp(10),dp(12));z.setBackground(bg(Color.rgb(20,48,78),12));box.addView(z);LinearLayout days=new LinearLayout(this);days.setOrientation(LinearLayout.VERTICAL);days.setVisibility(View.GONE);for(Day x:l.days)days.addView(card(x,true));box.addView(days);z.setOnClickListener(v->{days.setVisibility(days.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);z.setText((days.getVisibility()==View.VISIBLE?"▼ ":"▶ ")+l.name+"   •   5 günlük tahmini "+(days.getVisibility()==View.VISIBLE?"kapat":"aç"));});LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.setMargins(0,dp(3),0,dp(3));box.setLayoutParams(bp);dist.addView(box);}}
 View card(Day x,boolean small){LinearLayout c=new LinearLayout(this);c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(8),dp(7),dp(8),dp(7));c.setBackground(bg(Color.rgb(20,48,78),12));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,dp(2),0,dp(2));c.setLayoutParams(cp);c.addView(tv(x.date,small?12:13,-1,true),new LinearLayout.LayoutParams(dp(small?98:112),-2));c.addView(tv(icon(x.e),small?21:24,-1,false),new LinearLayout.LayoutParams(dp(38),-2));c.addView(tv(x.e,small?11:12,Color.LTGRAY,false),new LinearLayout.LayoutParams(0,-2,1));LinearLayout tt=new LinearLayout(this);tt.addView(tv("↓ "+x.mi+"°",small?15:16,Color.rgb(80,190,255),true));tt.addView(tv(" ↑ "+x.ma+"°",small?15:16,Color.rgb(255,130,70),true));c.addView(tt);return c;}
 String icon(String e){String x=e.toLowerCase(new Locale("tr"));if(x.contains("gök")||x.contains("şimşek"))return"⛈️";if(x.contains("kar"))return"🌨️";if(x.contains("sağanak")||x.contains("yağış")||x.contains("yağmur"))return"🌧️";if(x.contains("sis"))return"🌫️";if(x.contains("rüzgar"))return"🌬️";if(x.contains("çok bulutlu")||x.contains("kapalı"))return"☁️";if(x.contains("parçalı"))return"⛅";if(x.contains("az bulutlu"))return"🌤️";return"☀️";}
 void open(String u){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}catch(Exception e){}}
 @Override protected void onDestroy(){ex.shutdownNow();super.onDestroy();}
 static class Day{String date,e,mi,ma;Day(String d,String x,String a,String b){date=d;e=x;mi=a;ma=b;}}
 static class Loc{String name,now="";ArrayList<Day>days=new ArrayList<>();Loc(String n){name=n;}}
}