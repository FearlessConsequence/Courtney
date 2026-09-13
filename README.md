[README.md](https://github.com/user-attachments/files/32155559/README.md)
# Support Client — Android-клиент службы поддержки

Android-приложение для работы с обращениями в службу поддержки: просмотр списка, переписка с оператором в реальном времени, работа офлайн.


## Стек

| Технология | Назначение |
|------------|-----------|
| Kotlin 2.2.10 | Язык |
| Jetpack Compose + Material 3 | UI |
| Navigation Compose | Навигация |
| Ktor (REST + WebSocket) + OkHttp | Сеть |
| kotlinx.serialization | JSON |
| Room + KSP | Локальная БД |
| Coroutines / Flow | Асинхронность |



## 🌐 Настройка base URL

**Файл:** `app/src/main/java/com/example/supportform/data/api/NetworkModule.kt`

```kotlin
private const val BASE_URL = "http://10.0.2.2:8080"
```

| Сценарий | Адрес |
|----------|-------|
| Android Emulator | `http://10.0.2.2:8080` |
| Телефон по USB | `adb reverse tcp:8080 tcp:8080` → `http://127.0.0.1:8080` |
| Телефон по Wi-Fi | `http://<LAN-IP-компьютера>:8080` |

**WebSocket URL:** `ws://10.0.2.2:8080/ws` (без `/api`)

---

## 🚀 Команды сборки и запуска

### Backend (обязательно)

```sh
cd ../test-work
docker compose up -d --build --wait
```

### Android

```sh
./gradlew assembleDebug       # сборка APK
./gradlew installDebug        # установка на устройство
```

Или запустить из Android Studio: **Run → app**.

---

## 🔑 Тестовые учётные данные

| Логин | Пароль | Данные |
|-------|--------|--------|
| `client.one` | `Support123!` | 6 обращений |
| `client.two` | `Support123!` | 2 обращения |
| `client.empty` | `Support123!` | Пустой аккаунт |

---

## 🏗️ Архитектура

```
┌─────────────────────────────────┐
│         UI (Compose)            │
└───────────────┬─────────────────┘
                │ Flow
                ▼
┌─────────────────────────────────┐
│      Room (single source)       │
└───────────────▲─────────────────┘
                │ upsert
┌───────────────┴─────────────────┐
│  Repository (REST + WebSocket)  │
└───────────────┬─────────────────┘
                │
                ▼
┌─────────────────────────────────┐
│            Ktor                 │
└─────────────────────────────────┘
```

**Ключевая идея:** UI **никогда не ходит в сеть напрямую**.  
Все данные сохраняются в **Room**, экран читает из **Room через Flow**.  
REST-ответы и WebSocket-события **пишут в Room** — UI обновляется автоматически.

### Слои

| Слой | Назначение |
|------|-----------|
| `data/api/` | Ktor клиенты (REST + WS), `NetworkModule` |
| `data/database/` | Room: `AppDatabase`, `TicketDao`, `CommentDao` |
| `data/repository/` | `TicketRepository` — REST → Room, WS → Room |
| `data/model/` | DTO (Ticket, Comment, Author, Login) |
| `ui/` | Compose экраны и ViewModel |
| `utils/` | `TokenManager`, `RefreshManager` |

### Экраны

| № | Экран | Что делает |
|---|-------|-----------|
| 1 | **Login** | Валидация, загрузка, ошибки |
| 2 | **Tickets** | Список, статус, дата, logout |
| 3 | **Detail** | История комментариев, отправка |

---

## 🔐 Хранение сессии

- Токены (access + refresh) хранятся в `TokenManager` (SharedPreferences)
- **Пароль не сохраняется**
- Access TTL — **120 секунд**, refresh — **7 дней**
- При старте приложения:
  - токен **есть** → сразу на `tickets`
  - токена **нет** → на `login`

### Обновление токенов

`RefreshManager` — единая точка refresh:

| Ситуация | Поведение |
|----------|-----------|
| Параллельные 401 | **Один** refresh через `Mutex` |
| Успешный refresh | Заменяет **оба** токена атомарно |
| Сетевая ошибка / timeout / 5xx | Refresh **не запускается**, токен остаётся |
| Окончательный 401 на refresh | Очистка токенов → на `login` |

---

## 🔄 Синхронизация

### REST → Room
- `refreshTickets` — загрузка списка → в Room
- `refreshTicketDetail` — тикет и комментарии → в Room
- `sendComment` — отправка → сохранение в Room

### WebSocket → Room
- Подключение **один раз** при старте приложения
- Заголовок `Authorization: Bearer <access>`
- События `comment.created` и `ticket.status_changed` → перезагрузка тикета через REST → в Room
- Переподключение с увеличивающейся задержкой (**1с → 30с**)
- При смене access-токена WS подключается заново с новым

### Конфликты
- Upsert по `id` (`OnConflictStrategy.REPLACE`)
- Сортировка тикетов: `updatedAt DESC, id ASC`
- Сортировка комментариев: `sequence ASC`

---

## ⚠️ Обработка ошибок

| Ситуация | Поведение |
|----------|-----------|
| 401 | Refresh токенов → повтор запроса |
| Сеть / timeout / 5xx | **Не выкидывает** из сессии, данные из Room остаются |
| Ошибка отправки комментария | Текст **сохраняется**, показывается ошибка |
| Refresh не удался | Очистка сессии → экран входа |

---

## 📴 Офлайн

- Ранее загруженные тикеты и комментарии **доступны без интернета** (из Room)
- Ошибка обновления **не стирает** уже загруженные данные
- При восстановлении сети данные подтягиваются автоматически

---

## ✅ Проведённые проверки

- ✅ Вход с корректными и некорректными данными
- ✅ Восстановление сессии после перезапуска приложения
- ✅ Автоматический refresh access-токена (120 секунд)
- ✅ Обработка параллельных 401 (один refresh через Mutex)
- ✅ Отправка комментария с сохранением текста при ошибке
- ✅ Получение комментариев оператора через WebSocket в реальном времени
- ✅ Переподключение WebSocket с увеличивающейся задержкой
- ✅ Работа офлайн: список и тикеты из Room
- ✅ Logout с очисткой сессии и отключением WebSocket
- ✅ Отсутствие утечки данных между аккаунтами

---

## ⚠️ Ограничения

- ❌ **Koin** не подключён — зависимости создаются вручную в `AppNavigation`
- ❌ **Автотесты** отсутствуют — проверка вручную
- ❌ Пагинация не реализована (по ТЗ не требуется)
- ❌ Push-уведомления не реализованы (по ТЗ не требуются)
- ❌ Офлайн-очередь на отправку не реализована (по ТЗ не требуется)

---

## ⏱️ Затраченное время

| Этап | Часов |
|------|-------|
| Настройка окружения (Docker, Android Studio, эмулятор) | 4 |
| Вёрстка экранов (Login, Tickets, Detail) | 8 |
| REST (Ktor), логин, список, детали | 6 |
| Отправка комментариев, идемпотентность | 4 |
| Refresh токенов, Mutex | 3 |
| Room (сущности, DAO, Repository, мапперы) | 6 |
| WebSocket (подключение, события, переподключение) | 4 |
| Отладка и ручное тестирование | 8 |
| Документация | 2 |
| **Итого** | **~45 часов** |

---

## 📂 Структура проекта

```
mobile/
├── app/
│   └── src/main/
│       ├── java/com/example/supportform/
│       │   ├── data/
│       │   │   ├── api/          # Ktor (REST + WS), NetworkModule
│       │   │   ├── database/     # Room (Entity, DAO, AppDatabase)
│       │   │   ├── model/        # DTO
│       │   │   └── repository/   # TicketRepository
│       │   ├── ui/
│       │   │   ├── login/        # LoginForm, LoginViewModel
│       │   │   ├── tickets/      # TicketsForm
│       │   │   ├── detail/       # DetailForm
│       │   │   └── AppNavigation.kt
│       │   └── utils/            # TokenManager, RefreshManager
│       └── AndroidManifest.xml
└── build.gradle.kts
```
