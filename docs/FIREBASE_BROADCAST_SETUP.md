# Genel Bildirim Sistemi — Kurulum ve Uygulama Planı

## Mevcut proje incelemesi

- Android uygulaması Java tabanlıdır.
- Uygulama kimliği: `com.edirnehavadurumu.app`
- `minSdk 23`, `targetSdk 35`, Android Gradle Plugin `8.7.3`
- `app/build.gradle` içinde şu anda yalnızca Jsoup bağımlılığı vardır.
- `AndroidManifest.xml` içinde yalnızca internet izni ve `MainActivity` tanımlı; Firebase Messaging servisi ve bildirim izni henüz yok.
- Depoda `google-services.json` bulunmuyor; dolayısıyla bir Firebase projesiyle bağlantı henüz doğrulanamıyor.
- GitHub Actions, ana dala gelen değişikliklerde APK oluşturup yayımlayacak şekilde ayarlanmış.

## Hedef

Yöneticinin tek bir mesaj başlığı ve metni girerek tüm uygulama kurulumlarına duyuru gönderebilmesi. Mesaj, uygulama kapalıyken Android sistem bildirimi olarak da gösterilebilmeli.

## Önerilen güvenli mimari

1. Firebase Cloud Messaging (FCM) kullanılacak.
2. Uygulama, ilk açılışta `all_users` adlı FCM konusuna abone olacak.
3. Yönetici mesajları Firebase Console içindeki Messaging ekranından bu konuya gönderilecek. Bu, ayrı bir özel sunucu gerektirmeyen ilk sürüm yönetici panelidir.
4. Bildirim izni Android 13 ve üstünde uygun zamanda istenecek; kullanıcı reddederse zorlanmayacak.
5. İsteğe bağlı olarak uygulama açıkken gelen mesajlar için `FirebaseMessagingService` ile uygulama içi gösterim eklenecek.
6. Mesaj gönderimi yalnızca yetkili Firebase Console hesabından yapılmalı. FCM sunucu anahtarı veya servis hesabı kimlik bilgileri APK içine konulmamalı.

## Uygulamaya geçiş adımları

1. Firebase Console'da bir proje oluşturun veya mevcut projeyi seçin.
2. Android uygulamasını tam kimlik `com.edirnehavadurumu.app` ile kaydedin.
3. Firebase tarafından verilen `google-services.json` dosyasını `app/google-services.json` konumuna ekleyin. Bu dosya uygulama yapılandırmasıdır; özel sunucu anahtarı değildir.
4. Proje düzeyindeki Gradle dosyasına Google Services eklentisini, uygulama modülüne Firebase BoM ve `firebase-messaging` bağımlılığını ekleyin.
5. Manifest'e FCM servisini ve Android 13+ bildirim iznini ekleyin.
6. Uygulama başlatıldığında `all_users` konusuna aboneliği ekleyin.
7. Bildirim kanalını oluşturun ve bildirime dokunulduğunda uygulamanın açılmasını sağlayın.
8. Test cihazında izin vererek test mesajı gönderin; ardından yeni APK'yı derleyip doğrulayın.
9. Firebase Console > Messaging üzerinden hedef olarak `all_users` konusunu seçerek genel duyuru gönderin.

## Önemli sınırlar

- Firebase projesi ve `google-services.json` olmadan gerçek cihazlara bildirim gönderimi test edilemez.
- Bu depo incelemesinde mevcut Firebase bağlantısı görülmedi; bu nedenle bildirim sistemi şu anda çalışıyor kabul edilmemelidir.
- Bu dalda henüz uygulama kodu değiştirilmedi. Önce Firebase proje yapılandırması gerekir; yapılandırma geldikten sonra kod ve APK derlemesi güvenli biçimde tamamlanabilir.
- İlk aşamada Firebase Console yönetim ekranı kullanılır. Uygulamanın içine ayrı bir yönetici ekranı eklemek istenirse kimlik doğrulama ve sunucu tarafı yetkilendirme gerekir; herkese açık uygulamada yalnızca gizli bir butonla yönetici yetkisi vermek güvenli değildir.

## Resmî belgeler

- Firebase Android kurulumu: https://firebase.google.com/docs/android/setup
- Firebase Cloud Messaging Android kurulumu: https://firebase.google.com/docs/cloud-messaging/android/get-started
