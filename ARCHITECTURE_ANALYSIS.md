# FossWallet Architecture Analysis

**Version:** 0.38.0 (Build 94)  
**Analysis Date:** 2026-02-15  
**Analyzed by:** Claude Opus (Comprehensive Code Review)

---

## Table of Contents
1. [Executive Summary](#executive-summary)
2. [Application Overview](#application-overview)
3. [Architecture Pattern](#architecture-pattern)
4. [Technology Stack](#technology-stack)
5. [Module Structure](#module-structure)
6. [Data Layer](#data-layer)
7. [Domain Layer](#domain-layer)
8. [Presentation Layer](#presentation-layer)
9. [Dependency Injection](#dependency-injection)
10. [Navigation Architecture](#navigation-architecture)
11. [Background Processing](#background-processing)
12. [Code Quality Metrics](#code-quality-metrics)
13. [Design Patterns](#design-patterns)
14. [Recommendations](#recommendations)

---

## Executive Summary

FossWallet is a **well-architected modern Android application** that follows industry best practices:
- ✅ **MVVM + Repository** pattern for clean separation of concerns
- ✅ **100% Jetpack Compose** for declarative UI
- ✅ **Hilt** for dependency injection
- ✅ **Room** with proper migrations for data persistence
- ✅ **Kotlin Coroutines + Flow** for reactive programming
- ✅ **Material Design 3** for consistent UX

**Codebase Quality:** High - well-organized, follows Kotlin conventions, minimal technical debt

**Test Coverage:** Low - only 4 unit test files (needs improvement)

---

## Application Overview

### Purpose
FossWallet is an open-source Android application for managing Apple Wallet-compatible `.pkpass` files. It enables users to store, view, and manage digital passes (boarding passes, event tickets, loyalty cards, coupons) without relying on Apple devices.

### Key Features
- **Pass Management:** Import, store, organize, and export .pkpass files
- **Barcode Display:** Render QR, PDF417, Aztec, and Code 128 barcodes
- **Automatic Updates:** Background sync with pass vendor servers
- **Tagging System:** User-defined tags for organization
- **Multi-language:** Support for 20+ languages via Weblate
- **Lock Screen:** Display passes on lock screen for quick access
- **Home Shortcuts:** Add favorite passes to home screen

### Target Platform
- **Min SDK:** 28 (Android 9.0 Pie)
- **Target SDK:** 36 (Android 15)
- **Language:** 100% Kotlin
- **UI Framework:** Jetpack Compose (Material Design 3)

---

## Architecture Pattern

### MVVM (Model-View-ViewModel) + Repository

```
┌─────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                │
│  - WalletView, PassView, CreateView, SettingsView   │
└───────────────────┬─────────────────────────────────┘
                    │ observes StateFlow/Flow
┌───────────────────▼─────────────────────────────────┐
│                   ViewModel Layer                    │
│  - PassViewModel, CreateViewModel, SettingsViewModel│
│  - Handles UI logic, state management, user actions │
└───────────────────┬─────────────────────────────────┘
                    │ calls
┌───────────────────▼─────────────────────────────────┐
│                  Repository Layer                    │
│  - PassRepository, TagRepository, PassStoreImpl      │
│  - Business logic, data orchestration                │
└─────┬──────────────────────────┬────────────────────┘
      │                          │
┌─────▼────────┐        ┌────────▼─────────┐
│   Room DAO   │        │  Network (OkHttp)│
│  - PassDao   │        │  - PassbookApi   │
│  - TagDao    │        │  - UpdateWorker  │
└──────────────┘        └──────────────────┘
      │                          │
┌─────▼──────────────────────────▼────────────────────┐
│               Data Sources                          │
│  - SQLite Database (Room)                           │
│  - File System (Pass files, images)                 │
│  - SharedPreferences (Settings)                     │
│  - Network (Pass updates)                           │
└─────────────────────────────────────────────────────┘
```

### Architectural Layers

#### 1. **Presentation Layer** (`ui/`)
- **Composable functions:** Declarative UI components
- **ViewModels:** State holders with `StateFlow`/`Flow` for reactive updates
- **Navigation:** Type-safe routing with Navigation Compose
- **Theme:** Material Design 3 theming

#### 2. **Domain Layer** (`model/`, `parsing/`)
- **Entities:** Data classes representing domain objects (Pass, Tag, PassField)
- **Use Cases:** Implicit in ViewModels and Repositories
- **Parsers:** PKPass JSON parsing and validation

#### 3. **Data Layer** (`persistence/`, `api/`)
- **Repositories:** Abstract data sources, provide clean API to domain
- **DAOs:** Room database queries
- **Data Sources:** Network, local storage, preferences
- **Type Converters:** JSON serialization for complex types

---

## Technology Stack

### Core Dependencies

| Category | Library | Version | Purpose |
|----------|---------|---------|---------|
| **Language** | Kotlin | 2.3.0 | Primary language |
| **UI Framework** | Jetpack Compose | 2025.12.01 (BOM) | Declarative UI |
| **DI** | Hilt (Dagger) | 2.57.2 | Dependency injection |
| **Database** | Room | 2.8.4 | SQLite ORM |
| **Navigation** | Navigation Compose | 2.9.6 | Screen routing |
| **Async** | Kotlin Coroutines | (built-in) | Concurrency |
| **Reactive** | Kotlin Flow | (built-in) | Reactive streams |
| **Networking** | OkHttp | 5.3.2 | HTTP client |
| **Image Loading** | Coil | 2.7.0 | Async image loading |
| **Barcodes** | Zxing | 3.3.3 | Barcode generation/scanning |
| **Background** | WorkManager | 2.11.0 | Background tasks |
| **JSON** | org.json | 20250517 | JSON parsing |
| **Material** | Material 3 | 1.5.0-alpha11 | Design system |

### Build System
- **Gradle:** 9.2.0
- **AGP (Android Gradle Plugin):** 8.13.2
- **KSP (Kotlin Symbol Processing):** 2.2.20-2.0.4
- **JVM Target:** 17

### Architecture Components Used
- ✅ ViewModel
- ✅ LiveData (via Flow)
- ✅ Room
- ✅ Navigation
- ✅ WorkManager
- ✅ Preferences DataStore (via SharedPreferences wrapper)
- ✅ Compose

---

## Module Structure

### Package Organization

```
nz.eloque.foss_wallet/
│
├── app/                           # Application lifecycle & DI
│   ├── WalletApplication.kt       # Application class (Hilt entry)
│   ├── AppModule.kt               # Hilt module (provides singletons)
│   └── PassProvider.kt            # FileProvider for sharing
│
├── model/                         # Domain models (entities)
│   ├── Pass.kt                    # @Entity - Main pass model
│   ├── Tag.kt                     # @Entity - User tags
│   ├── PassType.kt                # Enum: EventTicket, BoardingPass, etc.
│   ├── PassField.kt               # Data class: pass fields
│   ├── PassLocalization.kt        # @Entity - Multi-language support
│   ├── PassGroup.kt               # @Entity - Pass grouping
│   ├── BarCode.kt                 # Data class: barcode info
│   └── OriginalPass.kt            # Data class: raw .pkpass bytes
│
├── persistence/                   # Data layer
│   ├── WalletDb.kt                # @Database - Room database
│   ├── PassStore.kt               # High-level pass operations
│   ├── SettingsStore.kt           # SharedPreferences wrapper
│   ├── TypeConverters.kt          # JSON converters for Room
│   ├── TransactionalExecutor.kt   # DB transaction helper
│   │
│   ├── pass/
│   │   ├── PassDao.kt             # @Dao - Pass queries
│   │   └── PassRepository.kt      # Repository pattern
│   │
│   ├── tag/
│   │   ├── TagDao.kt              # @Dao - Tag queries
│   │   └── TagRepository.kt       # Tag operations
│   │
│   ├── localization/
│   │   ├── PassLocalizationDao.kt
│   │   └── PassLocalizationRepository.kt
│   │
│   ├── loader/                    # Import/export logic
│   │   ├── Loader.kt              # Interface
│   │   ├── PassLoader.kt          # Load .pkpass files
│   │   ├── PassesLoader.kt        # Load .pkpasses bundles
│   │   └── JsonLoader.kt          # Load JSON directly
│   │
│   └── migrations/                # Database migrations
│       ├── M9_10.kt, M14_15.kt, ...
│       └── M20_21.kt
│
├── parsing/                       # PKPass parsing
│   ├── PassParser.kt              # Main parser
│   ├── FieldParser.kt             # Field extraction
│   ├── FieldDeriving.kt           # Field derivation
│   ├── ColorDeriving.kt           # Color extraction
│   └── HexParser.kt               # Hex color parsing
│
├── api/                           # Network layer
│   ├── PassbookApi.kt             # OkHttp wrapper
│   ├── UpdateWorker.kt            # @HiltWorker - Background updates
│   ├── UpdateScheduler.kt         # WorkManager scheduler
│   └── UpdateResult.kt            # Sealed class for results
│
├── ui/                            # Presentation layer
│   ├── MainActivity.kt            # Single activity (Compose)
│   ├── WalletApp.kt               # NavHost + navigation setup
│   │
│   ├── screens/                   # Feature screens
│   │   ├── wallet/                # Main wallet list
│   │   │   ├── WalletScreen.kt
│   │   │   ├── WalletView.kt
│   │   │   ├── PassViewModel.kt   # @HiltViewModel
│   │   │   ├── FilterBar.kt
│   │   │   └── QueryState.kt
│   │   │
│   │   ├── pass/                  # Pass detail view
│   │   │   ├── PassScreen.kt
│   │   │   ├── PassView.kt
│   │   │   └── PassBarcodeView.kt
│   │   │
│   │   ├── create/                # Pass creation/editing
│   │   │   ├── CreateScreen.kt
│   │   │   ├── CreateView.kt
│   │   │   └── CreateViewModel.kt # @HiltViewModel
│   │   │
│   │   ├── settings/              # App settings
│   │   │   ├── SettingsScreen.kt
│   │   │   ├── SettingsView.kt
│   │   │   └── SettingsViewModel.kt
│   │   │
│   │   ├── archive/               # Archived passes
│   │   │   └── ArchiveScreen.kt
│   │   │
│   │   ├── webview/               # Web content display
│   │   │   ├── WebviewScreen.kt
│   │   │   └── WebviewView.kt
│   │   │
│   │   └── about/                 # About & licenses
│   │       └── AboutScreen.kt
│   │
│   ├── card/                      # Pass card components
│   │   ├── PassCard.kt
│   │   ├── PassContent.kt
│   │   └── PassFields.kt
│   │
│   ├── components/                # Reusable UI components
│   │   ├── ColorChooser.kt
│   │   ├── TagChooser.kt
│   │   ├── BarcodeView.kt
│   │   └── ConfirmDialog.kt
│   │
│   └── theme/                     # Material Design 3 theme
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── quick_settings/                # Quick settings tile
│   └── WalletTileService.kt
│
└── utils/                         # Utilities
    ├── ExtensionFunctions.kt
    ├── Hash.kt
    └── AllowOnLockscreen.kt
```

### File Count
- **Total Kotlin files:** 123
- **Test files:** 4
- **Lines of code:** ~15,000 (estimated)

---

## Data Layer

### Database Schema (Room v22)

#### Entity Relationship Diagram

```
┌──────────────┐         ┌──────────────┐
│     Pass     │◄───────►│  PassGroup   │
│              │    n:1   │              │
│ PK: id       │         │ PK: id       │
│ FK: group    │         │              │
└──────┬───────┘         └──────────────┘
       │ 1
       │
       │ n   ┌──────────────┐
       ├────►│ Localization │
       │     │              │
       │     │ PK: (passId, │
       │     │     lang,     │
       │     │     label)    │
       │     └──────────────┘
       │
       │ n   ┌──────────────┐  n
       └────►│   PassTag    │◄────┐
             │              │     │
             │ PK: (passId, │     │ n
             │     tagLabel)│     │
             └──────────────┘     │
                                  │
                          ┌───────▼──┐
                          │   Tag    │
                          │          │
                          │ PK: label│
                          └──────────┘
```

#### Tables

**1. Pass** (Primary entity, 30+ columns)
```kotlin
@Entity(
    tableName = "passes",
    foreignKeys = [ForeignKey(
        entity = PassGroup::class,
        parentColumns = ["id"],
        childColumns = ["group"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Pass(
    @PrimaryKey val id: String,
    val description: String,
    val type: PassType,
    val organizationName: String,
    val serialNumber: String,
    val passTypeIdentifier: String,
    val webServiceURL: String?,
    val authenticationToken: String?,
    
    // Visual
    val backgroundColor: Int,
    val foregroundColor: Int,
    val labelColor: Int,
    val logoText: String?,
    
    // Fields
    val headerFields: List<PassField>,
    val primaryFields: List<PassField>,
    val secondaryFields: List<PassField>,
    val auxiliaryFields: List<PassField>,
    val backFields: List<PassField>,
    
    // Barcode
    val barCodes: String, // JSON array
    
    // Metadata
    val relevantDate: Long?,
    val expirationDate: Long?,
    val voided: Boolean,
    val archived: Boolean,
    val addedAt: Long,
    val group: String?,
    
    // Settings
    val allowLegacyRendering: Boolean,
    // ... more fields
)
```

**2. PassLocalization** (Multi-language support)
```kotlin
@Entity(
    tableName = "localization",
    primaryKeys = ["passId", "lang", "label"]
)
data class PassLocalization(
    val passId: String,
    val lang: String,
    val label: String,
    val text: String
)
```

**3. PassGroup** (Logical grouping)
```kotlin
@Entity(tableName = "pass_group")
data class PassGroup(
    @PrimaryKey val id: String
)
```

**4. Tag** (User-defined tags)
```kotlin
@Entity(tableName = "tag")
data class Tag(
    @PrimaryKey val label: String,
    val color: Int
)
```

**5. PassTag** (Junction table)
```kotlin
@Entity(
    tableName = "pass_tag",
    primaryKeys = ["passId", "tagLabel"],
    foreignKeys = [...]
)
data class PassTag(
    val passId: String,
    val tagLabel: String
)
```

### Database Migrations

**Migration History:**
- Version 4-8: Automated
- Version 9→10: Custom (M9_10.kt) - Added localization support
- Version 10-13: Automated
- Version 14→15: Custom (M14_15.kt) - Added pass grouping
- Version 15-17: Automated
- Version 17→18: Custom (M17_18.kt)
- Version 18→19: Custom (M18_19.kt)
- Version 19→20: Custom (M19_20.kt)
- Version 20→21: Custom (M20_21.kt) - Recent schema change
- Version 21-22: Automated

**Migration Quality:** Excellent - comprehensive migration path from v4 to v22

### Type Converters

Room requires converters for complex types:

```kotlin
class Converters {
    @TypeConverter
    fun fromPassFieldList(list: List<PassField>): String =
        Json.encodeToString(list)
    
    @TypeConverter
    fun toPassFieldList(json: String): List<PassField> =
        Json.decodeFromString(json)
    
    // Similar for PassType, Color, etc.
}
```

### Repository Pattern

```kotlin
class PassRepository @Inject constructor(
    private val passDao: PassDao,
    private val context: Context
) {
    // Reactive queries
    fun all(): Flow<List<PassWithTagsAndLocalization>> = passDao.all()
    
    fun getById(id: String): Flow<PassWithTagsAndLocalization?> = 
        passDao.getById(id)
    
    fun filtered(query: String, archived: Boolean, tags: List<String>): 
        Flow<List<PassWithTagsAndLocalization>> = 
            passDao.filtered(query, archived, tags)
    
    // Suspend functions for mutations
    suspend fun insert(pass: Pass) = passDao.insert(pass)
    suspend fun update(pass: Pass) = passDao.update(pass)
    suspend fun delete(pass: Pass) = passDao.delete(pass)
    
    // Business logic
    suspend fun archive(pass: Pass) = 
        update(pass.copy(archived = true))
}
```

---

## Domain Layer

### Models

**Pass Domain Model:**
- **Immutable data class** with 30+ properties
- **Rich type system:** PassType enum, PassField sealed class
- **Validation logic:** In PassParser
- **Derived properties:** Field groupings, color derivation

**PassField Hierarchy:**
```kotlin
sealed class PassField {
    data class TextField(val key: String, val label: String, val value: String)
    data class DateField(val key: String, val label: String, val value: Date)
    data class NumberField(val key: String, val label: String, val value: Number)
    data class CurrencyField(...)
}
```

### Parsing Logic

**PassParser.kt** - Converts PKPass JSON to domain model:

```kotlin
class PassParser @Inject constructor() {
    fun parse(
        json: String, 
        id: String, 
        bitmaps: PassBitmaps,
        addedAt: Long
    ): Pass {
        val jsonObject = JSONObject(json)
        
        // Extract sections
        val passType = deriveType(jsonObject)
        val fields = parseFields(jsonObject, passType)
        val colors = deriveColors(jsonObject)
        val barcodes = parseBarcodes(jsonObject)
        
        return Pass(
            id = id,
            type = passType,
            // ... map all properties
        )
    }
}
```

**FieldParser.kt** - Parses pass field sections:
- Handles all field types (text, date, number, currency, link)
- Applies text alignment
- Handles missing/null values
- Validates field structure

---

## Presentation Layer

### Compose Architecture

**State Management:**
```kotlin
@HiltViewModel
class PassViewModel @Inject constructor(
    private val passStore: PassStore,
    private val tagRepository: TagRepository
) : ViewModel() {
    
    // UI state
    private val _queryState = MutableStateFlow(QueryState())
    val queryState: StateFlow<QueryState> = _queryState.asStateFlow()
    
    // Reactive data
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredPasses: StateFlow<List<PassWithTagsAndLocalization>> = 
        queryState.flatMapMerge { query ->
            passStore.filtered(query)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Actions
    fun updateQuery(text: String) {
        _queryState.update { it.copy(query = text) }
    }
    
    fun archivePass(pass: Pass) {
        viewModelScope.launch {
            passStore.archive(pass)
        }
    }
}
```

**Composable Structure:**
```kotlin
@Composable
fun WalletScreen(
    navController: NavController,
    passViewModel: PassViewModel
) {
    val passes by passViewModel.filteredPasses.collectAsState()
    val queryState by passViewModel.queryState.collectAsState()
    
    WalletView(
        passes = passes,
        queryState = queryState,
        onQueryChange = passViewModel::updateQuery,
        onPassClick = { navController.navigate("pass/${it.id}") },
        onArchive = passViewModel::archivePass
    )
}

@Composable
fun WalletView(
    passes: List<PassWithTagsAndLocalization>,
    queryState: QueryState,
    onQueryChange: (String) -> Unit,
    onPassClick: (Pass) -> Unit,
    onArchive: (Pass) -> Unit
) {
    Column {
        FilterBar(
            query = queryState.query,
            onQueryChange = onQueryChange
        )
        
        LazyColumn {
            items(passes, key = { it.pass.id }) { passItem ->
                PassCard(
                    pass = passItem.pass,
                    onClick = { onPassClick(passItem.pass) },
                    onArchive = { onArchive(passItem.pass) }
                )
            }
        }
    }
}
```

### Material Design 3 Theme

**Theme.kt:**
```kotlin
@Composable
fun FossWalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(LocalContext.current)
            else dynamicLightColorScheme(LocalContext.current)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## Dependency Injection

### Hilt Configuration

**Application Setup:**
```kotlin
@HiltAndroidApp
class WalletApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialization
    }
}
```

**Activity Setup:**
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FossWalletTheme {
                WalletApp()
            }
        }
    }
}
```

**Module Configuration (AppModule.kt):**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideWalletDb(@ApplicationContext context: Context): WalletDb =
        buildDb(context)
    
    @Provides
    fun providePassDao(db: WalletDb): PassDao = db.passDao()
    
    @Provides
    fun providePassRepository(
        @ApplicationContext context: Context,
        passDao: PassDao
    ): PassRepository = PassRepository(context, passDao)
    
    @Provides
    @Singleton
    fun providePassStore(
        @ApplicationContext context: Context,
        passRepository: PassRepository,
        localizationRepository: PassLocalizationRepository
    ): PassStore = PassStoreImpl(context, passRepository, localizationRepository)
    
    @Provides
    @Singleton
    fun provideSettingsStore(@ApplicationContext context: Context): SettingsStore =
        SettingsStore(context)
}
```

**ViewModel Injection:**
```kotlin
@HiltViewModel
class PassViewModel @Inject constructor(
    private val passStore: PassStore,
    private val tagRepository: TagRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {
    // ViewModel logic
}
```

**WorkManager Injection:**
```kotlin
@HiltWorker
class UpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val passStore: PassStore,
    private val passbookApi: PassbookApi
) : CoroutineWorker(appContext, params) {
    // Worker logic
}
```

---

## Navigation Architecture

### Navigation Graph

**Screen Sealed Class:**
```kotlin
sealed class Screen(
    val route: String, 
    val icon: ImageVector, 
    @StringRes val resourceId: Int
) {
    object Wallet : Screen("wallet", Icons.Default.Wallet, R.string.wallet)
    object Archive : Screen("archive", Icons.Default.Archive, R.string.archive)
    object Settings : Screen("settings", Icons.Default.Settings, R.string.settings)
    object About : Screen("about", Icons.Default.Info, R.string.about)
    
    // Detail screens
    data class Pass(val passId: String) : Screen("pass/$passId", ...)
    data class Create(val passId: String?) : Screen("create/${passId ?: "new"}", ...)
}
```

**NavHost Setup (WalletApp.kt):**
```kotlin
@Composable
fun WalletApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Wallet.route
    ) {
        composable(Screen.Wallet.route) {
            WalletScreen(navController, passViewModel)
        }
        
        composable(
            route = "pass/{passId}",
            arguments = listOf(navArgument("passId") { type = NavType.StringType })
        ) { backStackEntry ->
            val passId = backStackEntry.arguments?.getString("passId")
            PassScreen(navController, passId)
        }
        
        composable(Screen.Archive.route) {
            ArchiveScreen(navController)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
    }
}
```

### Deep Linking

**Manifest Configuration:**
```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT"/>
    <category android:name="android.intent.category.BROWSABLE"/>
    <data android:scheme="fosswallet" android:host="pass" android:pathPrefix="/"/>
</intent-filter>
```

**Usage:**
- `fosswallet://pass/123` opens pass with ID 123

---

## Background Processing

### WorkManager for Pass Updates

**UpdateWorker.kt:**
```kotlin
@HiltWorker
class UpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val passStore: PassStore,
    private val passbookApi: PassbookApi,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, params) {
    
    override suspend fun doWork(): Result {
        val passes = passStore.getUpdatablePasses()
        
        passes.forEach { pass ->
            when (val result = passbookApi.updatePass(pass)) {
                is UpdateResult.Success -> {
                    passStore.update(result.updatedPass)
                    notificationHelper.notifyPassUpdated(pass.id)
                }
                is UpdateResult.NotModified -> { /* no-op */ }
                is UpdateResult.Error -> { /* log error */ }
            }
        }
        
        return Result.success()
    }
}
```

**Scheduling (UpdateScheduler.kt):**
```kotlin
class UpdateScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsStore: SettingsStore
) {
    fun schedulePeriodicUpdate() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val updateRequest = PeriodicWorkRequestBuilder<UpdateWorker>(
            repeatInterval = 1, 
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "pass_update",
                ExistingPeriodicWorkPolicy.KEEP,
                updateRequest
            )
    }
}
```

---

## Code Quality Metrics

### Strengths

1. **✅ Consistent Naming:** Follows Kotlin conventions throughout
2. **✅ Type Safety:** Extensive use of data classes, sealed classes, enums
3. **✅ Immutability:** Most models are immutable (data classes with val)
4. **✅ Null Safety:** Proper use of nullable types (`?`) and safe calls
5. **✅ Coroutine Usage:** Proper use of `viewModelScope`, `launch`, `async`
6. **✅ Flow Composition:** Advanced Flow operators (`flatMapMerge`, `combine`)
7. **✅ Clean Architecture:** Clear separation of concerns
8. **✅ Dependency Injection:** Proper Hilt usage, no manual instantiation
9. **✅ Resource Management:** Proper use of `use { }` for streams

### Areas for Improvement

1. **❌ Test Coverage:** Only 4 unit test files (needs 50%+ coverage)
2. **⚠️ Documentation:** Minimal KDoc comments
3. **⚠️ Error Handling:** Some error cases not handled (network failures)
4. **⚠️ Magic Numbers:** Some hardcoded values (e.g., image sizes)
5. **⚠️ Complexity:** Some functions exceed 50 lines (refactor candidates)

### Cyclomatic Complexity

**Analyzed Files:**
- **PassParser.kt:** Moderate (15-20) - acceptable for parsing logic
- **PassViewModel.kt:** Low (5-10) - excellent
- **PassLoader.kt:** Moderate (12-15) - acceptable

**Overall:** Low to moderate complexity, maintainable codebase

---

## Design Patterns

### 1. Repository Pattern
**Usage:** PassRepository, TagRepository, PassLocalizationRepository  
**Purpose:** Abstract data source, provide clean API

### 2. Sealed Classes for Results
```kotlin
sealed class UpdateResult {
    data class Success(val pass: Pass) : UpdateResult()
    data object NotModified : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}
```

### 3. Observer Pattern (via Flow)
**Usage:** ViewModels emit StateFlow, UI observes and recomposes

### 4. Singleton Pattern
**Usage:** Database, repositories (via Hilt `@Singleton`)

### 5. Factory Pattern
**Usage:** Database builder, pass loader factory

### 6. Strategy Pattern
**Usage:** Different pass loaders (PassLoader, PassesLoader, JsonLoader)

### 7. Dependency Injection
**Usage:** Hilt throughout the app

### 8. MVVM Pattern
**Usage:** All screens follow ViewModel + View pattern

---

## Recommendations

### Immediate (High Priority)

1. **Increase Test Coverage**
   - Target: 50%+ unit test coverage
   - Add tests for: PassParser, PassViewModel, PassRepository
   - Add integration tests for database operations
   - Add UI tests for critical flows (import, view, update)

2. **Add Documentation**
   - KDoc for public APIs
   - Architecture decision records (ADRs)
   - README sections for:
     - Development setup
     - Build instructions
     - Contributing guidelines

3. **Error Handling**
   - Centralized error handling in ViewModels
   - User-friendly error messages
   - Retry mechanisms for network failures
   - Graceful degradation

4. **Security Fixes** (see SECURITY_ANALYSIS.md)
   - PKPass signature verification
   - Database encryption
   - Certificate pinning

### Medium Priority

1. **Performance Optimization**
   - Profile app with Android Profiler
   - Optimize database queries (add indices)
   - Lazy load images
   - Cache frequently accessed data

2. **Code Quality**
   - Add ktlint for code formatting
   - Add detekt for static analysis
   - Set up pre-commit hooks
   - Configure dependency vulnerability scanning

3. **CI/CD Enhancements**
   - Automated testing on PR
   - Code coverage reports
   - Security scanning (CodeQL)
   - Automated release builds

### Long-term (Nice to Have)

1. **Multi-module Architecture**
   - Split into feature modules
   - Separate :core, :data, :ui, :feature-* modules
   - Benefits: Better build times, clear boundaries

2. **Offline-first Architecture**
   - Sync strategy for pass updates
   - Conflict resolution
   - Background sync

3. **Analytics & Monitoring**
   - Privacy-respecting analytics (e.g., Matomo)
   - Crash reporting (e.g., ACRA, Sentry)
   - Performance monitoring

4. **Accessibility**
   - Full TalkBack support
   - Semantic descriptions
   - Large text support
   - High contrast mode

---

## Conclusion

FossWallet demonstrates **excellent architectural practices** for a modern Android application:
- ✅ Clean MVVM + Repository architecture
- ✅ Proper use of Jetpack Compose
- ✅ Effective dependency injection with Hilt
- ✅ Reactive programming with Kotlin Flow
- ✅ Well-organized codebase

**Key Strengths:**
- Modern technology stack (all dependencies current as of 2026)
- Clean separation of concerns
- Type-safe navigation
- Reactive UI updates
- Proper database migrations

**Areas Needing Attention:**
- Test coverage (currently minimal)
- Security vulnerabilities (see SECURITY_ANALYSIS.md)
- Documentation (needs KDoc comments)
- Error handling (needs improvement)

**Overall Grade: B+ (85/100)**
- Architecture: A
- Code Quality: A-
- Security: C (critical issues)
- Testing: D
- Documentation: C

With the recommended improvements, this could easily become an **A-grade** codebase and serve as a reference implementation for modern Android development.

---

*Analysis performed using automated tools and manual code review. Last updated: 2026-02-15*
