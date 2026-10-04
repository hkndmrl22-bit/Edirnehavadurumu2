package com.edirnehavadurumu.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.content.Intent;
import android.net.Uri;
import android.widget.*;
import android.graphics.drawable.Drawable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String MGM_HOURLY = "https://www.mgm.gov.tr/tahmin/saatlik.aspx?m=EDIRNE";
    private static final String MGM_5DAY = "https://www.mgm.gov.tr/tahmin/il-ve-ilceler.aspx?il=EDIRNE";
    private static final String MGM_DAILY = "https://www.mgm.gov.tr/tahmin/gunluk-tahmin.aspx?b=1";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler();
    private LinearLayout root, hourlyContainer, fiveContainer, districtContainer;
    private TextView status, updated;
    private ProgressBar progress;

    private final String[] DISTRICTS = {"Edirne","Enez","Havsa","İpsala","Keşan","Lalapaşa","Meriç","Süloğlu","Uzunköprü"};

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();
        loadMgm();
    }

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + .5f);
    }

    private TextView tv(String text, float sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setPadding(dp(5), dp(4), dp(5), dp(4));
        return t;
    }

    private GradientDrawable bg(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private void sectionTitle(LinearLayout parent, String text) {
        TextView h = tv(text, 19, Color.WHITE, true);
        h.setPadding(dp(2),dp(16),dp(2),dp(8));
        parent.addView(h);
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14),dp(10),dp(14),dp(22));
        root.setBackgroundColor(Color.rgb(7,24,45));
        scroll.addView(root);
        setContentView(scroll);

        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);

        ImageView logo = new ImageView(this);
        logo.setImageResource(getResources().getIdentifier("edirne_logo","drawable",getPackageName()));
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        head.addView(logo,new LinearLayout.LayoutParams(dp(78),dp(78)));

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(10),0,dp(4),0);
        titles.addView(tv("Edirne Hava Durumu",23,Color.WHITE,true));
        titles.addView(tv("/ edirnehavadurumu",14,Color.LTGRAY,false));
        titles.addView(tv("MGM verileri • Güncel tahminler",12,Color.LTGRAY,false));
        head.addView(titles,new LinearLayout.LayoutParams(0,-2,1));

        Button refresh = new Button(this);
        refresh.setText("↻ Yenile");
        refresh.setOnClickListener(v -> loadMgm());
        head.addView(refresh);
        root.addView(head);

        status = tv("MGM verileri yükleniyor…",14,Color.LTGRAY,false);
        root.addView(status);
        progress = new ProgressBar(this);
        progress.setIndeterminate(true);
        root.addView(progress);

        sectionTitle(root,"📅 5 Günlük Tahmin");
        fiveContainer = new LinearLayout(this);
        fiveContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(fiveContainer);

        sectionTitle(root,"📍 Edirne İlçeleri");
        districtContainer = new LinearLayout(this);
        districtContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(districtContainer);

        sectionTitle(root,"🕒 Saatlik Tahmin • Edirne Merkez");
        hourlyContainer = new LinearLayout(this);
        hourlyContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(hourlyContainer);

        updated = tv("",12,Color.LTGRAY,false);
        root.addView(updated);
        root.addView(tv("Veri kaynağı: Meteoroloji Genel Müdürlüğü (MGM)",12,Color.LTGRAY,false));
        root.addView(tv("Bizi takip edin",14,Color.WHITE,true));
        LinearLayout socials = new LinearLayout(this);
        socials.setGravity(Gravity.CENTER);
        socials.setPadding(0,dp(6),0,dp(6));

        ImageButton facebook = new ImageButton(this);
        facebook.setImageResource(com.edirnehavadurumu.app.R.drawable.ic_facebook);
        facebook.setBackgroundColor(Color.TRANSPARENT);
        facebook.setContentDescription("Facebook - edirnehavadurumu");
        facebook.setPadding(dp(6),dp(6),dp(6),dp(6));
        facebook.setOnClickListener(v -> openSocial("https://www.facebook.com/edirnehavadurumu"));
        socials.addView(facebook,new LinearLayout.LayoutParams(dp(62),dp(62)));

        ImageButton instagram = new ImageButton(this);
        instagram.setImageResource(com.edirnehavadurumu.app.R.drawable.ic_instagram);
        instagram.setBackgroundColor(Color.TRANSPARENT);
        instagram.setContentDescription("Instagram - edirnehavadurumu");
        instagram.setPadding(dp(6),dp(6),dp(6),dp(6));
        instagram.setOnClickListener(v -> openSocial("https://www.instagram.com/edirnehavadurumu/"));
        socials.addView(instagram,new LinearLayout.LayoutParams(dp(62),dp(62)));

        root.addView(socials,new LinearLayout.LayoutParams(-1,-2));
        root.addView(tv("Facebook  •  Instagram",12,Color.LTGRAY,false));
    }

    private void loadMgm() {
        status.setText("MGM verileri alınıyor…");
        progress.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            try {
                Document hourly = null, five = null, daily = null;
                try { hourly = Jsoup.connect(MGM_HOURLY).userAgent("Mozilla/5.0 EdirneHavaDurumu").timeout(20000).get(); } catch(Exception ignored) {}
                try { five = Jsoup.connect(MGM_5DAY).userAgent("Mozilla/5.0 EdirneHavaDurumu").timeout(20000).get(); } catch(Exception ignored) {}
                try { daily = Jsoup.connect(MGM_DAILY).userAgent("Mozilla/5.0 EdirneHavaDurumu").timeout(20000).get(); } catch(Exception ignored) {}
                if(hourly == null && five == null && daily == null) throw new Exception("MGM kaynaklarına erişilemedi");

                List<String> hours=hourly==null?new ArrayList<>():row(hourly,"Saat");
                List<String> temps=hourly==null?new ArrayList<>():row(hourly,"Sıcaklık");
                List<String> feels=hourly==null?new ArrayList<>():row(hourly,"Hissedilen Sıcaklık");
                List<String> humidity=hourly==null?new ArrayList<>():row(hourly,"Nem");
                List<String> wind=hourly==null?new ArrayList<>():row(hourly,"Rüzgar Yön ve Hızı");
                List<String> gust=hourly==null?new ArrayList<>():row(hourly,"Rüzgar Hamlesi");
                List<WeatherItem> items=new ArrayList<>();
                for(int i=0;i<Math.min(hours.size(),temps.size());i++)
                    items.add(new WeatherItem(val(hours,i),val(temps,i),val(feels,i),val(humidity,i),val(wind,i),val(gust,i)));

                List<FiveDay> days=five==null?new ArrayList<>():parseFiveDay(five);
                List<District> districts=daily==null?new ArrayList<>():parseDistricts(daily);

                main.post(() -> {
                    progress.setVisibility(View.GONE);
                    renderHourly(items);
                    renderFive(days);
                    renderDistricts(districts);
                    status.setText("MGM verileri başarıyla güncellendi.");
                    updated.setText("Kaynak: MGM • Edirne il ve ilçe tahminleri");
                });
            } catch(Exception e) {
                main.post(() -> {
                    progress.setVisibility(View.GONE);
                    status.setText("MGM verisi alınamadı. İnternet bağlantınızı kontrol edip Yenile'ye basın.");
                    Toast.makeText(this,"MGM bağlantısı başarısız",Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private List<String> row(Document doc,String label) {
        List<String> out=new ArrayList<>();
        for(Element tr:doc.select("tr")) {
            Elements cells=tr.select("th,td");
            if(cells.size()==0) continue;
            String first=cells.get(0).text().trim();
            if(first.toLowerCase(Locale.ROOT).contains(label.toLowerCase(Locale.ROOT))) {
                for(int i=1;i<cells.size();i++) out.add(cells.get(i).text().trim());
                break;
            }
        }
        return out;
    }

    private List<FiveDay> parseFiveDay(Document doc) {
        List<FiveDay> out=new ArrayList<>();
        for(Element tr:doc.select("tr")) {
            Elements c=tr.select("th,td");
            if(c.size()<4) continue;
            String date=c.get(0).text().trim();
            if(date.matches(".*\\d{1,2}.*")) {
                String event=c.size()>1?c.get(1).text().trim():"-";
                String min="",max="";
                for(int i=2;i<c.size();i++) {
                    String x=c.get(i).text().trim();
                    if(x.matches("-?\\d+.*")) {
                        if(min.isEmpty()) min=x; else if(max.isEmpty()) {max=x; break;}
                    }
                }
                if(!min.isEmpty() && !max.isEmpty() && out.size()<5) out.add(new FiveDay(date,event,min,max));
            }
        }
        return out;
    }

    private List<District> parseDistricts(Document doc) {
        List<District> out=new ArrayList<>();
        Set<String> wanted=new HashSet<>(Arrays.asList(DISTRICTS));
        for(Element tr:doc.select("tr")) {
            Elements c=tr.select("th,td");
            if(c.size()<3) continue;
            String name=c.get(0).text().trim();
            String matched=null;
            for(String d:DISTRICTS) if(name.equalsIgnoreCase(d) || name.toLowerCase(Locale.ROOT).startsWith(d.toLowerCase(Locale.ROOT)+" ")) matched=d;
            if(matched==null || containsDistrict(out,matched)) continue;
            String event=c.get(1).text().trim();
            String min="",max="";
            for(int i=2;i<c.size();i++) {
                String x=c.get(i).text().trim();
                if(x.matches("-?\\d+")) {
                    if(min.isEmpty()) min=x; else {max=x; break;}
                }
            }
            if(!min.isEmpty() && !max.isEmpty()) out.add(new District(matched,event,min,max));
        }
        return out;
    }

    private boolean containsDistrict(List<District> list,String name) {
        for(District d:list) if(d.name.equals(name)) return true;
        return false;
    }

    private String val(List<String> l,int i){return i<l.size()?l.get(i):"-";}

    private void renderFive(List<FiveDay> days) {
        fiveContainer.removeAllViews();
        if(days.isEmpty()) {
            fiveContainer.addView(tv("5 günlük MGM tahmini şu anda okunamadı. Yenile ile tekrar deneyin.",13,Color.LTGRAY,false));
            return;
        }
        for(FiveDay d:days) {
            LinearLayout card=new LinearLayout(this);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(10),dp(8),dp(10),dp(8));
            card.setBackground(bg(Color.rgb(20,48,78),14));
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2); cp.setMargins(0,dp(3),0,dp(3));
            card.addView(tv(d.date,14,Color.WHITE,true),new LinearLayout.LayoutParams(dp(105),-2));
            TextView ev=tv(d.event,13,Color.LTGRAY,false);
            card.addView(ev,new LinearLayout.LayoutParams(0,-2,1));
            card.addView(tv(d.min+"° / "+d.max+"°",17,Color.rgb(255,193,7),true));
            fiveContainer.addView(card,cp);
        }
    }

    private void renderDistricts(List<District> ds) {
        districtContainer.removeAllViews();
        if(ds.isEmpty()) {
            districtContainer.addView(tv("Edirne ilçeleri için MGM verisi şu anda okunamadı. Yenile ile tekrar deneyin.",13,Color.LTGRAY,false));
            return;
        }
        for(District d:ds) {
            LinearLayout card=new LinearLayout(this);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(10),dp(7),dp(10),dp(7));
            card.setBackground(bg(Color.rgb(17,42,69),12));
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2); cp.setMargins(0,dp(2),0,dp(2));
            card.addView(tv(d.name,15,Color.WHITE,true),new LinearLayout.LayoutParams(dp(105),-2));
            card.addView(tv(d.event,12,Color.LTGRAY,false),new LinearLayout.LayoutParams(0,-2,1));
            card.addView(tv(d.min+"°  –  "+d.max+"°",15,Color.rgb(255,193,7),true));
            districtContainer.addView(card,cp);
        }
    }

    private void renderHourly(List<WeatherItem> items) {
        hourlyContainer.removeAllViews();
        if(items.isEmpty()) {
            hourlyContainer.addView(tv("Saatlik MGM verisi şu anda okunamadı. Yenile ile tekrar deneyin.",13,Color.LTGRAY,false));
            return;
        }
        for(WeatherItem w:items) {
            LinearLayout r=new LinearLayout(this);
            r.setGravity(Gravity.CENTER_VERTICAL);
            r.setPadding(0,dp(5),0,dp(5));
            r.addView(tv(w.hour,14,Color.WHITE,true),new LinearLayout.LayoutParams(dp(55),-2));
            r.addView(tv(w.temp+"°C",21,Color.rgb(255,193,7),true),new LinearLayout.LayoutParams(dp(78),-2));
            LinearLayout details=new LinearLayout(this); details.setOrientation(LinearLayout.VERTICAL);
            details.addView(tv("Hissedilen: "+w.feel+"°C",12,Color.LTGRAY,false));
            details.addView(tv("Nem: %"+w.humidity+" • Rüzgar: "+w.wind,12,Color.LTGRAY,false));
            details.addView(tv("Hamle: "+w.gust,12,Color.LTGRAY,false));
            r.addView(details,new LinearLayout.LayoutParams(0,-2,1));
            hourlyContainer.addView(r);
            View line=new View(this); line.setBackgroundColor(Color.rgb(55,78,104));
            hourlyContainer.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
        }
    }

    private void openSocial(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch(Exception e) {
            Toast.makeText(this, "Bağlantı açılamadı", Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onDestroy(){executor.shutdownNow();super.onDestroy();}

    private static class WeatherItem {
        final String hour,temp,feel,humidity,wind,gust;
        WeatherItem(String h,String t,String f,String hu,String w,String g){hour=h;temp=t;feel=f;humidity=hu;wind=w;gust=g;}
    }
    private static class FiveDay {
        final String date,event,min,max;
        FiveDay(String d,String e,String mi,String ma){date=d;event=e;min=mi;max=ma;}
    }
    private static class District {
        final String name,event,min,max;
        District(String n,String e,String mi,String ma){name=n;event=e;min=mi;max=ma;}
    }
}
