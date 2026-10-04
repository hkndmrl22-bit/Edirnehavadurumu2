package com.edirnehavadurumu.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String MGM_URL = "https://www.mgm.gov.tr/tahmin/saatlik.aspx?m=EDIRNE";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler();
    private LinearLayout root, hourlyContainer;
    private TextView status, updated;
    private ProgressBar progress;

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
        t.setPadding(dp(6), dp(4), dp(6), dp(4));
        return t;
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(12), dp(16), dp(20));
        root.setBackgroundColor(Color.rgb(8,24,45));
        scroll.addView(root);
        setContentView(scroll);

        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView logo = tv("EDİRNE\nHAVA DURUMU", 11, Color.WHITE, true);
        logo.setGravity(Gravity.CENTER);
        GradientDrawable logoBg = new GradientDrawable();
        logoBg.setShape(GradientDrawable.OVAL);
        logoBg.setColor(Color.rgb(19,61,96));
        logoBg.setStroke(dp(2), Color.rgb(255,193,7));
        logo.setBackground(logoBg);
        head.addView(logo, new LinearLayout.LayoutParams(dp(78), dp(78)));

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(10),0,0,0);
        titles.addView(tv("Edirne Hava Durumu", 24, Color.WHITE, true));
        titles.addView(tv("/ edirnehavadurumu", 14, Color.LTGRAY, false));
        titles.addView(tv("MGM verileri • Güncel tahminler", 12, Color.LTGRAY, false));
        head.addView(titles, new LinearLayout.LayoutParams(0, -2, 1));

        Button refresh = new Button(this);
        refresh.setText("Yenile");
        refresh.setOnClickListener(v -> loadMgm());
        head.addView(refresh);
        root.addView(head);

        status = tv("MGM verileri yükleniyor…", 14, Color.LTGRAY, false);
        root.addView(status);

        progress = new ProgressBar(this);
        progress.setIndeterminate(true);
        root.addView(progress);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14),dp(12),dp(14),dp(12));
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.rgb(20,48,78));
        cardBg.setCornerRadius(dp(16));
        card.setBackground(cardBg);

        TextView source = tv("EDİRNE MERKEZ  •  MGM SAATLİK TAHMİN", 15, Color.WHITE, true);
        card.addView(source);

        hourlyContainer = new LinearLayout(this);
        hourlyContainer.setOrientation(LinearLayout.VERTICAL);
        card.addView(hourlyContainer);

        root.addView(card, new LinearLayout.LayoutParams(-1, -2));
        updated = tv("", 12, Color.LTGRAY, false);
        root.addView(updated);
        root.addView(tv("Veri kaynağı: Meteoroloji Genel Müdürlüğü (MGM)", 12, Color.LTGRAY, false));
    }

    private void loadMgm() {
        status.setText("MGM verileri alınıyor…");
        progress.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            try {
                Document doc = Jsoup.connect(MGM_URL)
                        .userAgent("Mozilla/5.0 EdirneHavaDurumu/3.0")
                        .timeout(20000)
                        .get();

                List<String> hours = row(doc, "Saat");
                List<String> temps = row(doc, "Sıcaklık");
                List<String> feels = row(doc, "Hissedilen Sıcaklık");
                List<String> humidity = row(doc, "Nem");
                List<String> wind = row(doc, "Rüzgar Yön ve Hızı");
                List<String> gust = row(doc, "Rüzgar Hamlesi");

                final List<WeatherItem> items = new ArrayList<>();
                int n = Math.min(hours.size(), temps.size());

                for (int i=0; i<n; i++) {
                    items.add(new WeatherItem(
                        val(hours,i), val(temps,i), val(feels,i),
                        val(humidity,i), val(wind,i), val(gust,i)
                    ));
                }

                main.post(() -> render(items));
            } catch (Exception e) {
                main.post(() -> {
                    progress.setVisibility(View.GONE);
                    status.setText("MGM verisi alınamadı. İnternet bağlantınızı kontrol edip Yenile'ye basın.");
                    Toast.makeText(this, "MGM bağlantısı başarısız", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private List<String> row(Document doc, String label) {
        List<String> out = new ArrayList<>();
        for (Element tr : doc.select("tr")) {
            Elements cells = tr.select("th,td");
            if (cells.size() == 0) continue;

            String first = cells.get(0).text().trim();
            if (first.toLowerCase(Locale.ROOT).contains(label.toLowerCase(Locale.ROOT))) {
                for (int i=1; i<cells.size(); i++)
                    out.add(cells.get(i).text().trim());
                break;
            }
        }
        return out;
    }

    private String val(List<String> l, int i) {
        return i < l.size() ? l.get(i) : "-";
    }

    private void render(List<WeatherItem> items) {
        progress.setVisibility(View.GONE);
        hourlyContainer.removeAllViews();

        if (items.isEmpty()) {
            status.setText("MGM sayfasından veri bulunamadı.");
            return;
        }

        status.setText("MGM'den güncel saatlik tahmin alındı.");

        for (WeatherItem w : items) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0,dp(5),0,dp(5));

            TextView h = tv(w.hour,15,Color.WHITE,true);
            row.addView(h,new LinearLayout.LayoutParams(dp(58),-2));

            TextView t = tv(w.temp+"°C",22,Color.rgb(255,193,7),true);
            row.addView(t,new LinearLayout.LayoutParams(dp(80),-2));

            LinearLayout details = new LinearLayout(this);
            details.setOrientation(LinearLayout.VERTICAL);
            details.addView(tv("Hissedilen: "+w.feel+"°C",12,Color.LTGRAY,false));
            details.addView(tv("Nem: %"+w.humidity+"  •  Rüzgar: "+w.wind,12,Color.LTGRAY,false));
            details.addView(tv("Hamle: "+w.gust,12,Color.LTGRAY,false));

            row.addView(details,new LinearLayout.LayoutParams(0,-2,1));
            hourlyContainer.addView(row);

            View line = new View(this);
            line.setBackgroundColor(Color.rgb(55,78,104));
            hourlyContainer.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
        }

        updated.setText("Kaynak: MGM • Edirne saatlik tahmin");
    }

    @Override protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private static class WeatherItem {
        final String hour,temp,feel,humidity,wind,gust;
        WeatherItem(String h,String t,String f,String hu,String w,String g) {
            hour=h; temp=t; feel=f; humidity=hu; wind=w; gust=g;
        }
    }
}
