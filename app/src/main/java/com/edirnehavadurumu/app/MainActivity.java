    View metric(String a,String b){
        String iconText=a, label="";
        int sp=a.indexOf(" ");
        if(sp>0){iconText=a.substring(0,sp);label=a.substring(sp+1);}
        LinearLayout card=col();
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(2),dp(3),dp(2),dp(3));
        card.setBackground(stroke(Color.rgb(10,63,98),Color.rgb(25,104,154),15));

        TextView la=tv(label,8.5f,Color.rgb(215,236,250),true);
        la.setGravity(Gravity.CENTER);
        la.setSingleLine(true);
        card.addView(la,new LinearLayout.LayoutParams(-1,dp(18)));

        TextView ic=tv(iconText,19,TEXT,false);
        ic.setGravity(Gravity.CENTER);
        ic.setIncludeFontPadding(false);
        card.addView(ic,new LinearLayout.LayoutParams(-1,dp(24)));

        TextView va=tv(b,11.5f,TEXT,true);
        va.setGravity(Gravity.CENTER);
        va.setIncludeFontPadding(false);
        va.setMaxLines(2);
        card.addView(va,new LinearLayout.LayoutParams(-1,dp(30)));
        return card;
    }
    void section(String s){TextView t=tv(s,17,Color.rgb(205,224,244),true);t.setPadding(dp(2),dp(18),dp(2),dp(9));content.addView(t,mp());}

void showDistricts(){ showDistrictsTab(0); }

    void showDistrictsTab(int tab){
        districtTab=tab;
        content.removeAllViews();setNavActive(1);content.setPadding(0,0,0,0);

        LinearLayout tabs=row();tabs.setPadding(0,0,0,dp(2));
        TextView instant=tv("◉  ANLIK DURUM",14,TEXT,true);
        TextView five=tv("▦  5 GÜNLÜK TAHMİN",14,TEXT,true);
        instant.setGravity(Gravity.CENTER);five.setGravity(Gravity.CENTER);
        instant.setBackground(tab==0?bg(Color.rgb(18,122,235),16):stroke(Color.rgb(20,69,105),Color.rgb(35,125,190),16));
        five.setBackground(tab==1?bg(Color.rgb(18,122,235),16):stroke(Color.rgb(20,69,105),Color.rgb(35,125,190),16));
        LinearLayout.LayoutParams tp1=new LinearLayout.LayoutParams(0,dp(58),1);
        LinearLayout.LayoutParams tp2=new LinearLayout.LayoutParams(0,dp(58),1);
        tp1.setMargins(0,0,dp(2),0);tp2.setMargins(dp(2),0,0,0);
        tabs.addView(instant,tp1);tabs.addView(five,tp2);content.addView(tabs,mp());

        LinearLayout body=col();content.addView(body,mp());
        if(tab==0)renderDistrictCurrent(body,all.size()>1?all.get(1):null);
        else renderDistrictForecast(body,all.size()>1?all.get(1):null);

        instant.setOnClickListener(v->{if(districtTab!=0)showDistrictsTab(0);});
        five.setOnClickListener(v->{if(districtTab!=1)showDistrictsTab(1);});
    }

    void refreshDistricts(int tab,TextView button){
        button.setText("⟳  Güncelleniyor…");
        ex.execute(()->{
            try{
                Loc cen=apiLocation("Edirne Merkez","merkez");
                String[] D={"Enez","Havsa","İpsala","Keşan","Lalapaşa","Meriç","Süloğlu","Uzunköprü"};
                String[] Q={"ENEZ","HAVSA","IPSALA","KESAN","LALAPASA","MERIC","SULOGLU","UZUNKOPRU"};
                ArrayList<Loc> tmp=new ArrayList<>();tmp.add(cen);
                for(int i=0;i<D.length;i++){try{tmp.add(apiLocation(D[i],Q[i].toLowerCase(Locale.ROOT)));}catch(Exception ignored){}}
                main.post(()->{center=cen;all=tmp;lastUpdate=currentTime();showDistrictsTab(tab);});
            }catch(Exception e){
                main.post(()->button.setText("⟳  Güncelleme başarısız — tekrar dene"));
            }
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
            final View card=districtMiniCard(l,l==selected);
            card.setOnClickListener(v->{body.removeAllViews();renderDistrictCurrent(body,l);});
            left.addView(card);
        }
        renderSelectedDistrict(right,selected,false);
        body.addView(split,mp());
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
        card.setPadding(dp(12),dp(7),dp(8),dp(7));
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(active?stroke(Color.rgb(18,122,235),Color.rgb(72,178,255),14):bg(CARD,14));

        card.addView(tv(l.name,18,TEXT,true),new LinearLayout.LayoutParams(-1,dp(27)));

        LinearLayout info=row();
        info.setGravity(Gravity.CENTER_VERTICAL);

        TextView temp=tv(tempC(l.now,"—"),15,GOLD,true);
        temp.setGravity(Gravity.CENTER_VERTICAL);
        info.addView(temp,new LinearLayout.LayoutParams(dp(68),dp(34)));

        info.addView(weatherIconView(l.nowEvent,20),new LinearLayout.LayoutParams(dp(36),dp(34)));

        TextView ev=tv(val(l.nowEvent,"—"),9.5f,Color.rgb(225,240,250),true);
        ev.setGravity(Gravity.CENTER_VERTICAL);
        ev.setMaxLines(2);
        ev.setEllipsize(android.text.TextUtils.TruncateAt.END);
        info.addView(ev,new LinearLayout.LayoutParams(0,dp(34),1));

        card.addView(info,new LinearLayout.LayoutParams(-1,dp(34)));

        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(82));
        p.setMargins(0,0,0,dp(7));
        card.setLayoutParams(p);
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
            LinearLayout panel=col();
            panel.setPadding(dp(10),dp(10),dp(10),dp(8));
            panel.setBackground(bg(Color.rgb(8,55,88),18));

            panel.addView(tv(l.name,21,TEXT,true),new LinearLayout.LayoutParams(-1,dp(30)));
            panel.addView(tv("5 GÜNLÜK TAHMİN",10,MUTED,true),new LinearLayout.LayoutParams(-1,dp(20)));
            panel.addView(tv("Son güncelleme: "+l.lastUpdate,9.5f,MUTED,false),new LinearLayout.LayoutParams(-1,dp(24)));

            for(Day d:l.days)panel.addView(dayCompact(d),mp());
            right.addView(panel,mp());
            return;
        }

        LinearLayout hero=col();
        hero.setPadding(dp(10),dp(10),dp(10),dp(9));
        hero.setBackground(bg(Color.rgb(10,59,94),18));

        hero.addView(tv(l.name,21,TEXT,true),new LinearLayout.LayoutParams(-1,dp(29)));
        hero.addView(tv("Son güncelleme: "+l.lastUpdate,9.5f,MUTED,false),new LinearLayout.LayoutParams(-1,dp(23)));

        LinearLayout condition=row();
        condition.setGravity(Gravity.CENTER_VERTICAL);
        condition.addView(weatherIconView(l.nowEvent,32),new LinearLayout.LayoutParams(dp(50),dp(50)));
        TextView ce=tv(val(l.nowEvent,"—"),11,TEXT,true);
        ce.setGravity(Gravity.CENTER_VERTICAL);
        ce.setMaxLines(2);
        condition.addView(ce,new LinearLayout.LayoutParams(0,dp(50),1));
        hero.addView(condition,new LinearLayout.LayoutParams(-1,dp(54)));

        TextView temp=tv(tempC(l.now,"—"),31,GOLD,true);
        temp.setGravity(Gravity.CENTER);
        temp.setIncludeFontPadding(false);
        hero.addView(temp,new LinearLayout.LayoutParams(-1,dp(54)));

        LinearLayout mm=row();
        mm.setPadding(0,dp(4),0,0);
        mm.setGravity(Gravity.CENTER);
        mm.addView(metric("💧 Nem",val(l.humidity,"—")+"%"),new LinearLayout.LayoutParams(0,dp(76),1));
        mm.addView(metric("≋ Rüzgâr",val(l.wind,"—")+" km/sa"),new LinearLayout.LayoutParams(0,dp(76),1));
        mm.addView(metric("◉ Basınç",val(l.pressure,"—")+" hPa"),new LinearLayout.LayoutParams(0,dp(76),1));
        hero.addView(mm,new LinearLayout.LayoutParams(-1,dp(80)));

        right.addView(hero,mp());
    }

    View dayCompact(Day d){
        LinearLayout c=row();
        c.setGravity(Gravity.CENTER_VERTICAL);
        c.setPadding(dp(9),dp(7),dp(7),dp(7));
        c.setBackground(stroke(Color.rgb(8,63,101),Color.rgb(17,104,160),14));

        LinearLayout a=col();
        a.setGravity(Gravity.CENTER_VERTICAL);
        a.addView(tv(dayLabel(d.date),12,TEXT,true),new LinearLayout.LayoutParams(-1,dp(21)));
        a.addView(tv(weekday(d.date),9,MUTED,false),new LinearLayout.LayoutParams(-1,dp(17)));
        TextView ev=tv(d.e,9.5f,MUTED,false);
        ev.setMaxLines(2);
        ev.setEllipsize(android.text.TextUtils.TruncateAt.END);
        a.addView(ev,new LinearLayout.LayoutParams(-1,dp(30)));
        c.addView(a,new LinearLayout.LayoutParams(0,dp(66),1));

        c.addView(weatherIconView(d.e,25),new LinearLayout.LayoutParams(dp(48),dp(66)));

        LinearLayout b=col();
        b.setGravity(Gravity.CENTER);
        b.addView(tv(d.ma+"°",14,Color.rgb(255,100,90),true),new LinearLayout.LayoutParams(dp(38),dp(27)));
        b.addView(tv(d.mi+"°",14,Color.rgb(90,190,255),true),new LinearLayout.LayoutParams(dp(38),dp(27)));
        c.addView(b,new LinearLayout.LayoutParams(dp(42),dp(66)));

        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(78));
        p.setMargins(0,0,0,dp(6));
        c.setLayoutParams(p);
        return c;
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
        if(selected==null){body.addView(tv("İlçe verileri yükleniyor…",14,MUTED,false));return;}
        LinearLayout split=row();split.setGravity(Gravity.TOP);
        LinearLayout left=col();left.setPadding(0,dp(8),dp(4),0);
        LinearLayout right=col();right.setPadding(dp(4),dp(8),0,0);
        split.addView(left,new LinearLayout.LayoutParams(0,-2,0.49f));
        split.addView(right,new LinearLayout.LayoutParams(0,-2,0.51f));
        for(int i=1;i<all.size();i++){
            final Loc l=all.get(i);
            View card=districtMiniCard(l,l==selected);
            card.setOnClickListener(v->{body.removeAllViews();renderDistrictForecast(body,l);});
            left.addView(card);
        }
        renderSelectedDistrict(right,selected,true);
        body.addView(split,mp());
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
        if(merkezId==0)merkezId=istNo;if(istNo==0)istNo=merkezId;Loc l=new Loc(name); l.lastUpdate=currentTime();
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
        String today=new SimpleDateFormat("dd",new Locale("tr","TR")).format(new Date())+" "+new SimpleDateFormat("MMMM",new Locale("tr","TR")).format(new Date());
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
    String formatDay(String s){try{SimpleDateFormat in=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);in.setTimeZone(TimeZone.getTimeZone("UTC"));Date d=in.parse(s);Calendar c=Calendar.getInstance(TimeZone.getTimeZone("Europe/Istanbul"));c.setTime(d);String[] ay={"Ocak","Şubat","Mart","Nisan","Mayıs","Haziran","Temmuz","Ağustos","Eylül","Ekim","Kasım","Aralık"};return String.format(Locale.US,"%02d %s",c.get(Calendar.DAY_OF_MONTH),ay[c.get(Calendar.MONTH)]).trim();}catch(Exception e){return s;}}
    String timeOnly(String s){if(s==null)return "";java.util.regex.Matcher m=java.util.regex.Pattern.compile("(\\d{2}:\\d{2})").matcher(s);return m.find()?m.group(1):"";}
    String currentTime(){return new SimpleDateFormat("d MMMM yyyy HH:mm",new Locale("tr","TR")).format(new Date());}
    String shortTime(String x){if(x==null||x.isEmpty()||x.equals("—"))return "—";int p=x.lastIndexOf(" ");return p>=0&&p+1<x.length()?x.substring(p+1):x;}
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
    @Override protected void onDestroy(){timer.removeCallbacks(refresh5m);ex.shutdownNow();imgEx.shutdownNow();super.onDestroy();}
    static class Day{String date,e,mi,ma;Day(String d,String e,String mi,String ma){this.date=d;this.e=e;this.mi=mi;this.ma=ma;}}
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
        detail.addView(tv("Nem: "+val(center.humidity,"—")+"%     Rüzgâr: "+val(center.wind,"—")+" km/sa "+val(center.windDir,"")+"     Basınç: "+val(center.pressure,"—")+" hPa",11,TEXT,false));
        content.addView(detail,mp());
        section("SAATLİK TAHMİN");
        HorizontalScrollView hs=new HorizontalScrollView(this);LinearLayout hr=row();
        if(center.hours.size()==0)hr.addView(tv("Saatlik tahmin şu anda alınamadı.",12,MUTED,false));
        for(Hour h:center.hours)hr.addView(hourCard(h));
        hs.addView(hr);content.addView(hs,mp());
    }


}