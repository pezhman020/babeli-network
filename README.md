# Babeli Network Monitor

یک اپ اندروید مینیمال و مدرن برای مانیتورینگ ترافیک اینترنت.

## ویژگی‌ها

| صفحه | قابلیت‌ها |
|------|----------|
| **Dashboard** | سرعت لحظه‌ای، گراف زنده، اطلاعات شبکه فعلی، آمار session |
| **Speed** | گیج سرعت متحرک، تاریخچه سرعت، آمار کل دانلود/آپلود |
| **Apps** | مصرف اینترنت هر اپ (امروز / هفته / ماه)، بار دانلود و آپلود |
| **Networks** | شبکه متصل + لیست تمام شبکه‌های اطراف با سیگنال |

## نصب و اجرا

### پیش‌نیازها
- Android Studio Hedgehog یا جدیدتر
- JDK 17
- Android SDK 26+

### مراحل
```bash
cd c:\projects\babeli-network
# باز کن در Android Studio
# یا build مستقیم:
.\gradlew assembleDebug
```

### مجوزهای لازم
اپ به صورت خودکار درخواست می‌دهد:
- **Location** — برای scan شبکه‌های WiFi (الزامی Android 6+)
- **Usage Access** — برای آمار مصرف هر اپ (باید دستی از Settings فعال شود)
- **Notifications** — برای نمایش سرعت در notification bar

## معماری

```
MVVM + Hilt DI
│
├── data/
│   ├── NetworkMonitor.kt     ← سرعت لحظه‌ای با TrafficStats
│   ├── AppUsageManager.kt    ← مصرف هر اپ با NetworkStatsManager
│   └── WifiScanner.kt        ← اطلاعات WiFi و scan شبکه‌های اطراف
│
├── viewmodel/
│   └── NetworkViewModel.kt   ← StateFlow برای UI
│
├── ui/
│   ├── screens/              ← 4 صفحه اصلی
│   ├── components/           ← SpeedGauge، Cards، SignalBars
│   └── theme/                ← Dark theme با رنگ Cyan/Purple
│
└── service/
    └── NetworkMonitorService ← Foreground service برای notification
```

## طراحی

- **رنگ پس‌زمینه:** `#080C18` (تاریک آبی)
- **رنگ اصلی:** `#00D4FF` (Cyan)
- **رنگ ثانوی:** `#7C4DFF` (Purple)
- **فونت:** Default system با وزن‌های مختلف
- **Navigation:** Bottom nav با 4 تب
