# 🏫 Campus Navigation System

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![NFC](https://img.shields.io/badge/NFC-002E5B?style=for-the-badge&logo=nfc&logoColor=white)

Система навигации по учебному заведению с использованием NFC меток для автоматического определения текущего положения и построения оптимального маршрута.

## 📌 Основные возможности

- **Автоматическое определение местоположения** через NFC метки
- **Визуализация маршрута** на интерактивной карте этажа
- **Расчет времени пути** с учетом расстояния
- **Простой интерфейс** с минимальными действиями пользователя
- **Адаптивный алгоритм** поиска кратчайшего пути (BFS)

## Скриншоты

<div align="center">
  <img src="screenshots/Screenshot_1.jpg" width="30%" alt="Построение маршрута между 214 и 206 кабинетом"/>
  <img src="screenshots/Screenshot_2.jpg" width="30%" alt="Построение маршрута между 214 и 207 кабинетом"/> 
  <img src="screenshots/Screenshot_3.jpg" width="30%" alt="Построение маршрута между 214 и 222 кабинетом"/>
</div>

## Технологии

- **Язык**: Kotlin 100%
- **Архитектура**: Single Activity
- **NFC**: Android NFC Reader API
- **Алгоритм**: Поиск в ширину (BFS)
- **Визуализация**: Custom View с анимацией
- **Минимальная версия Android**: 8.0 (API 26)

## Структура проекта

CampusNavigator/
├── app/
│ ├── src/
│ │ ├── main/
│ │ │ ├── java/com/example/navigator/
│ │ │ │ ├── MainActivity.kt # Основная логика
│ │ │ │ └── LineView.kt # Отрисовка маршрута
│ │ │ ├── res/
│ │ │ │ ├── layout/ # Макеты
│ │ │ │ │ └── activity_main.xml # Макет экрана
│ │ │ │ ├── drawable/ # Изображения
│ │ │ │ │ └── second_floor_png.png # План второго этажа
│ │ │ │ └── values/ # Ресурсы
│ │ │ └── AndroidManifest.xml
│ ├── build.gradle # Конфигурация модуля
├── build.gradle # Конфигурация проекта
├── README.md
└── .gitignore # Игнорируемые файлы


## Быстрый старт

### Требования
- Android устройство с NFC (API 26+)
- Android Studio Electric Eel или новее
- NFC метки с записанными идентификаторами

### Установка
1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/your-username/CampusNavigator.git```

- Откройте проект в Android Studio
- Настройте NFC метки (измените nfcIdMap в MainActivity.kt)
- Запустите на устройстве через Run 'app'
- Добавление новых комнат:



