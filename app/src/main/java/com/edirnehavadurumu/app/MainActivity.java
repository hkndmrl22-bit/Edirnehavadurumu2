package com.edirnehavadurumu.app;

import android.app.*;import android.os.*;import android.graphics.*;import android.graphics.drawable.*;import android.view.*;import android.content.*;import android.net.*;import android.widget.*;import java.util.*;import java.util.concurrent.*;import org.jsoup.*;import org.jsoup.nodes.*;import org.jsoup.select.*;

public class MainActivity extends Activity{
 static final String H="https://www.mgm.gov.tr/tahmin/saatlik.aspx?m=EDIRNE", F="https://www.mgm.gov.tr/kurumici/5gunalfabetik.aspx";
 ExecutorService ex=Executors.newSingleThreadExecutor(); Handler main=new Handler(); LinearLayout root,five,dist,hour; TextView status,updated;
 String[] D={"Edirne","Enez","Havsa","İpsala","Keşan","Lalapaşa","Meriç","Süloğlu","Uzunköprü"};
 int dp(float x){return(int)(x*getResources().getDisplayMetrics().density+.5f);}
 TextView tv(String s,float z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setTypeface(Typeface.DEFAULT,b?1:0);t.setPadding(dp(5),dp(3),dp(5),dp(3));return t;}
 GradientDrawable bg(int c,int r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
 void title(String s){TextView t=tv(s,19,-1,true);t.setPadding(dp(2),dp(15),dp(2),dp(7));root.addView(t);}
 @Override public void onCreate(Bundle b){super.onCreate(b);ui();load();}
 void ui(){
  ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(8),dp(12),dp(22));root.setBackgroundColor(Color.rgb(7,24,45));sc.addView(root);setContentView(sc);
  LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);ImageView im=new ImageView(this);im.setImageResource(R.drawable.edirne_logo_real);im.setScaleType(ImageView.ScaleType.CENTER_INSIDE);h.addView(im,new LinearLayout.LayoutParams(dp(82),dp(82)));
  LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.addView(tv("Edirne Hava Durumu",22,-1,true));tx.addView(tv("/ edirnehavadurumu",14,Color.LTGRAY,false));tx.addView(tv("MGM verileri • Güncel tahminler",12,Color.LTGRAY,false));h.addView(tx,new LinearLayout.LayoutParams(0,-2,1));
  Button r=new Button(this);r.setText("↻ Yenile");r.setOnClickListener(v->load());h.addView(r);root.addView(h);
  status=tv("MGM verileri yükleniyor…",14,Color.LTGRAY,false);root.addView(status);ProgressBar p=new ProgressBar(this);p.setIndeterminate(true);root.addView(p);progress=p;
  title("📅 Edirne Merkez • 5 Günlük Tahmin");five=new LinearLayout(this);five.setOrientation(LinearLayout.VERTICAL);root.addView(five);
  title("📍 Edirne İlçeleri • 5 Günlük Tahmin");dist=new LinearLayout(this);dist.setOrientation(LinearLayout.VERTICAL);root.addView(dist);
  title("🕒 Saatlik Tahmin • Edirne Merkez");hour=new LinearLayout(this);hour.setOrientation(LinearLayout.VERTICAL);root.addView(hour);
  updated=tv("",12,Color.LTGRAY,false);root.addView(updated);root.addView(tv("Veri kaynağı: Meteoroloji Genel Müdürlüğü (MGM)",12,Color.LTGRAY,false));
  TextView f=tv("Bizi takip edin",15,-1,true);f.setGravity(17);root.addView(f);LinearLayout s=new LinearLayout(this);s.setGravity(17);
  ImageButton fb=new ImageButton(this);fb.setImageResource(R.drawable.ic_facebook);fb.setBackgroundColor(Color.TRANSPARENT);fb.setOnClickListener(v->open("https://www.facebook.com/edirnehavadurumu"));s.addView(fb,new LinearLayout.LayoutParams(dp(58),dp(58)));
  ImageButton ig=new ImageButton(this);ig.setImageResource(R.drawable.ic_instagram);ig.setBackgroundColor(Color.TRANSPARENT);ig.setOnClickListener(v->open("https://www.instagram.com/edirnehavadurumu/"));s.addView(ig,new LinearLayout.LayoutParams(dp(58),dp(58)));root.addView(s);TextView sh=tv("Facebook ve Instagram: @edirnehavadurumu",12,Color.LTGRAY,false);sh.setGravity(17);root.addView(sh);
 }
 ProgressBar progress;
 void load(){status.setText("MGM verileri alınıyor…");progress.setVisibility(View.VISIBLE);ex.execute(()->{try{
   Document hd=Jsoup.connect(H).userAgent("Mozilla/5.0 EdirneHavaDurumu").timeout(20000).get(),fd=Jsoup.connect(F).userAgent("Mozilla/5.0 EdirneHavaDurumu").timeout(20000).get();
   List<String> hs=row(hd,"Saat"),ts=row(hd,"Sıcaklık"),fs=row(hd,"Hissedilen Sıcaklık"),ns=row(hd,"Nem"),ws=row(hd,"Rüzgar Yön ve Hızı"),gs=row(hd,"Rüzgar Hamlesi");
   ArrayList<W> wl=new ArrayList<>();for(int i=0;i<Math.min(hs.size(),ts.size());i++)wl.add(new W(v(hs,i),v(ts,i),v(fs,i),v(ns,i),v(ws,i),v(gs,i)));
   Data data=parse(fd);main.post(()->{progress.setVisibility(View.GONE);renderCenter(data.get("EDIRNE"));renderDistricts(data);renderHour(wl);status.setText("MGM verileri başarıyla güncellendi.");updated.setText("Kaynak: MGM • 5 günlük ve saatlik tahminler");});
  }catch(Exception e){main.post(()->{progress.setVisibility(View.GONE);status.setText("MGM verisi alınamadı. Yenile'ye basın.");Toast.makeText(this,"MGM bağlantısı başarısız",0).show();});}});}
 List<String> row(Document d,String label){ArrayList<String> o=new ArrayList<>();for(Element tr:d.select("tr")){Elements c=tr.select(">th,>td");if(c.size()>0&&c.get(0).text().toLowerCase(new Locale("tr")).contains(label.toLowerCase(new Locale("tr")))){for(int i=1;i<c.size();i++)o.add(c.get(i).text().trim());break;}}return o;}
 String v(List<String>x,int i){return i<x.size()?x.get(i):"-";}
 Data parse(Document d){Data a=new Data();ArrayList<String> dates=new ArrayList<>();for(Element tr:d.select("tr")){Elements c=tr.select(">th,>td");if(c.size()>=6&&c.get(0).text().trim().equalsIgnoreCase("Merkez")){for(int i=1;i<c.size();i++)dates.add(c.get(i).text().trim());break;}}
  if(dates.size()<5){Calendar q=Calendar.getInstance();String[]m={"Ocak","Şubat","Mart","Nisan","Mayıs","Haziran","Temmuz","Ağustos","Eylül","Ekim","Kasım","Aralık"};for(int i=0;i<5;i++){dates.add(q.get(Calendar.DAY_OF_MONTH)+" "+m[q.get(Calendar.MONTH)]);q.add(Calendar.DAY_OF_MONTH,1);}}
  HashSet<String>w=new HashSet<>();for(String x:D)w.add(n(x));
  for(Element tr:d.select("tr")){Elements c=tr.select(">th,>td");if(c.size()<11)continue;String raw=c.get(0).text().trim(),name=clean(raw);if(!w.contains(n(name)))continue;Loc l=new Loc(name);for(int k=0;k<5;k++){int b=1+k*3;if(b+2>=c.size())break;String e=c.get(b).select("img").attr("alt").trim();if(e.isEmpty())e=c.get(b).text().trim();String mi=c.get(b+1).text().trim(),ma=c.get(b+2).text().trim();if(mi.matches("-?\\d+.*")&&ma.matches("-?\\d+.*"))l.a.add(new Day(dates.get(k),e,mi,ma));}if(l.a.size()==5)a.put(n(name),l);}return a;}
 String clean(String s){s=s.replace('_',' ');int p=s.indexOf(" (");if(p>0)s=s.substring(0,p);for(String d:D)if(s.equalsIgnoreCase(d))return d;return s;}
 String n(String s){return s.toLowerCase(new Locale("tr")).replace("ı","i").replace("ş","s").replace("ğ","g").replace("ü","u").replace("ö","o").replace("ç","c").trim();}
 void renderCenter(Loc l){five.removeAllViews();if(l==null){five.addView(tv("Edirne Merkez 5 günlük tahmin okunamadı.",13,Color.LTGRAY,false));return;}for(Day x:l.a)five.addView(card(x,false));}
 void renderDistricts(Data a){dist.removeAllViews();for(String name:D){Loc l=a.get(n(name));if(l==null)continue;TextView z=tv("▾ "+name,16,-1,true);z.setPadding(dp(5),dp(10),dp(5),dp(3));dist.addView(z);for(Day x:l.a)dist.addView(card(x,true));}}
 View card(Day x,boolean small){LinearLayout c=new LinearLayout(this);c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(8),dp(7),dp(8),dp(7));c.setBackground(bg(Color.rgb(20,48,78),12));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,dp(2),0,dp(2));c.setLayoutParams(cp);
  LinearLayout da=new LinearLayout(this);da.setOrientation(LinearLayout.VERTICAL);da.addView(tv(x.date,small?12:13,-1,true));c.addView(da,new LinearLayout.LayoutParams(dp(small?98:112),-2));
  TextView ic=tv(icon(x.e),small?21:24,-1,false);ic.setGravity(17);c.addView(ic,new LinearLayout.LayoutParams(dp(38),-2));
  TextView ev=tv(x.e,small?11:12,Color.LTGRAY,false);c.addView(ev,new LinearLayout.LayoutParams(0,-2,1));
  LinearLayout tt=new LinearLayout(this);tt.addView(tv("↓ "+x.mi+"°",small?15:16,Color.rgb(80,190,255),true));tt.addView(tv(" ↑ "+x.ma+"°",small?15:16,Color.rgb(255,130,70),true));c.addView(tt);return c;}
 String icon(String e){String x=e.toLowerCase(new Locale("tr"));if(x.contains("gök")||x.contains("şimşek"))return"⛈️";if(x.contains("kar"))return"🌨️";if(x.contains("sağanak")||x.contains("yağış"))return"🌧️";if(x.contains("sis"))return"🌫️";if(x.contains("rüzgar"))return"🌬️";if(x.contains("çok bulutlu")||x.contains("kapalı"))return"☁️";if(x.contains("parçalı"))return"⛅";if(x.contains("az bulutlu"))return"🌤️";return"☀️";}
 void renderHour(List<W>a){hour.removeAllViews();for(W x:a){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.addView(tv(x.h,13,-1,true),new LinearLayout.LayoutParams(dp(62),-2));r.addView(tv(icon(x.event)+" "+x.t+"°C",18,Color.rgb(255,193,7),true),new LinearLayout.LayoutParams(dp(118),-2));LinearLayout q=new LinearLayout(this);q.setOrientation(LinearLayout.VERTICAL);q.addView(tv(x.event,11,Color.LTGRAY,false));q.addView(tv("Hissedilen: "+x.f+"°C • Nem: %"+x.n,11,Color.LTGRAY,false));q.addView(tv("Rüzgar: "+x.w+" • Hamle: "+x.g,11,Color.LTGRAY,false));r.addView(q,new LinearLayout.LayoutParams(0,-2,1));hour.addView(r);View line=new View(this);line.setBackgroundColor(Color.rgb(55,78,104));hour.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));}}
 void open(String u){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}catch(Exception e){}}
 @Override protected void onDestroy(){ex.shutdownNow();super.onDestroy();}
 static class W{String h,t,f,n,w,g,event="-";W(String a,String b,String c,String d,String e,String z){h=a;t=b;f=c;n=d;w=e;g=z;}}
 static class Day{String date,e,mi,ma;Day(String d,String x,String a,String b){date=d;e=x;mi=a;ma=b;}}
 static class Loc{String name;ArrayList<Day>a=new ArrayList<>();Loc(String n){name=n;}}
 static class Data extends HashMap<String,Loc>{}
}