package com.example.data.auth

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * Permanent representation of a registered user account in non-volatile storage.
 */
data class StoredUserAccount(
    val uid: String,
    val email: String,             // strictly normalized: lowercase and trimmed
    val passwordHash: String,
    val username: String,
    val displayName: String = username,
    val role: String = "CUSTOMER / PIONEER",
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val authProvider: String = "PASSWORD",  // "PASSWORD" or "GOOGLE"
    val photoUrl: String? = null
)

/**
 * Android SQLite helper providing ACID non-volatile local database persistence
 * for user accounts across app reloads, preview sessions, and logouts.
 */
class UserAccountDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS users (
                uid TEXT PRIMARY KEY,
                email TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                username TEXT NOT NULL,
                display_name TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'CUSTOMER / PIONEER',
                is_verified INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                last_login_at INTEGER NOT NULL,
                auth_provider TEXT NOT NULL DEFAULT 'PASSWORD',
                photo_url TEXT
            );
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);")
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // Ensure new columns exist on older tables without data loss
        try {
            db.execSQL("ALTER TABLE users ADD COLUMN auth_provider TEXT NOT NULL DEFAULT 'PASSWORD';")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE users ADD COLUMN photo_url TEXT;")
        } catch (_: Exception) {}
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Safe migration if schema upgrades in future
    }

    companion object {
        const val DATABASE_NAME = "brain_battle_accounts.db"
        const val DATABASE_VERSION = 1
    }
}

/**
 * Robust non-volatile storage manager for Brain Battle user accounts.
 *
 * Implements triple redundancy:
 * 1. Native Android SQLite database for transactional durability
 * 2. Synchronous file backup in context.filesDir ("bb_accounts_store.json")
 * 3. SharedPreferences with immediate .commit()
 *
 * Guarantees that registered accounts and the seeded demo account:
 * - Persist across app reloads and process restarts
 * - Are NOT deleted or cleared when a user logs out
 * - Normalize emails (lowercase and trimmed) uniformly for both registration and login
 */
class PersistentUserAccountStore(private val context: Context) {

    private val tag = "PersistentUserStore"
    private val dbHelper = UserAccountDatabaseHelper(context.applicationContext)
    private val prefs = context.applicationContext.getSharedPreferences("brain_battle_auth", Context.MODE_PRIVATE)
    private val backupFile = File(context.applicationContext.filesDir, "bb_accounts_store.json")

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val userListType = Types.newParameterizedType(List::class.java, StoredUserAccount::class.java)
    private val userListAdapter = moshi.adapter<List<StoredUserAccount>>(userListType)

    // Fast in-memory lookup cache: email (lowercase) -> StoredUserAccount
    private val emailCache = ConcurrentHashMap<String, StoredUserAccount>()
    private val uidCache = ConcurrentHashMap<String, StoredUserAccount>()

    init {
        loadAllFromDisk()
    }

    /**
     * Wipes all local SQLite accounts, cached file backups, and SharedPreferences.
     */
    @Synchronized
    fun clearAllAccounts() {
        try {
            emailCache.clear()
            uidCache.clear()
            val db = dbHelper.writableDatabase
            db.delete("users", null, null)
            if (backupFile.exists()) backupFile.delete()
            prefs.edit().clear().commit()
            Log.i(tag, "All local mock/cached user accounts wiped clean.")
        } catch (e: Exception) {
            Log.e(tag, "Error clearing local accounts", e)
        }
    }

    companion object {
        const val DEMO_EMAIL = "kingzotalker@gmail.com"
        const val DEMO_PASSWORD = "kingzley@1A"
        const val DEMO_ROLE = "CUSTOMER / PIONEER"
        const val DEMO_USERNAME = "Kingzo"
        const val DEMO_DISPLAY_NAME = "Kingzo Pioneer"

        fun hashSha256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun normalizeEmail(email: String): String {
            return email.trim().lowercase()
        }

        fun generateDeterministicUid(email: String): String {
            val hash = hashSha256(normalizeEmail(email))
            return "bb_usr_${hash.take(16)}"
        }
    }

    /**
     * Seeds the persistent storage with the default test account so login always
     * works out of the box even in sandbox or fresh preview environments:
     * - Email: kingzotalker@gmail.com
     * - Password: kingzley@1A
     * - Role: CUSTOMER / PIONEER
     */
    fun seedDefaultAccounts() {
        val normalizedDemoEmail = normalizeEmail(DEMO_EMAIL)
        val existing = getUserByEmail(normalizedDemoEmail)
        if (existing == null) {
            val demoAccount = StoredUserAccount(
                uid = generateDeterministicUid(normalizedDemoEmail),
                email = normalizedDemoEmail,
                passwordHash = hashSha256(DEMO_PASSWORD),
                username = DEMO_USERNAME,
                displayName = DEMO_DISPLAY_NAME,
                role = DEMO_ROLE,
                isEmailVerified = true,
                createdAt = 1700000000000L,
                lastLoginAt = System.currentTimeMillis()
            )
            saveUser(demoAccount)
            Log.i(tag, "Successfully seeded default demo account: $normalizedDemoEmail (Role: $DEMO_ROLE)")
        }
    }

    /**
     * Saves a user account into SQLite, atomic file backup, and SharedPreferences.
     */
    @Synchronized
    fun saveUser(account: StoredUserAccount) {
        val normalizedEmail = normalizeEmail(account.email)
        val cleanAccount = account.copy(email = normalizedEmail)

        // 1. Write to SQLite
        try {
            val db = dbHelper.writableDatabase
            val cv = ContentValues().apply {
                put("uid", cleanAccount.uid)
                put("email", cleanAccount.email)
                put("password_hash", cleanAccount.passwordHash)
                put("username", cleanAccount.username)
                put("display_name", cleanAccount.displayName)
                put("role", cleanAccount.role)
                put("is_verified", if (cleanAccount.isEmailVerified) 1 else 0)
                put("created_at", cleanAccount.createdAt)
                put("last_login_at", cleanAccount.lastLoginAt)
                put("auth_provider", cleanAccount.authProvider)
                put("photo_url", cleanAccount.photoUrl)
            }
            db.insertWithOnConflict("users", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        } catch (e: Exception) {
            Log.e(tag, "Error saving user to SQLite: ${e.message}", e)
        }

        // 2. Update memory cache
        emailCache[normalizedEmail] = cleanAccount
        uidCache[cleanAccount.uid] = cleanAccount

        // 3. Mirror to SharedPreferences with immediate commit()
        try {
            prefs.edit().apply {
                putString("email_to_uid_$normalizedEmail", cleanAccount.uid)
                putString("account_${cleanAccount.uid}_email", normalizedEmail)
                putString("account_${cleanAccount.uid}_passhash", cleanAccount.passwordHash)
                putString("account_${cleanAccount.uid}_username", cleanAccount.username)
                putString("account_${cleanAccount.uid}_display_name", cleanAccount.displayName)
                putString("account_${cleanAccount.uid}_role", cleanAccount.role)
                putBoolean("account_${cleanAccount.uid}_verified", cleanAccount.isEmailVerified)
                putLong("account_${cleanAccount.uid}_created_at", cleanAccount.createdAt)
                putLong("account_${cleanAccount.uid}_last_login_at", cleanAccount.lastLoginAt)
                putString("account_${cleanAccount.uid}_auth_provider", cleanAccount.authProvider)
                putString("account_${cleanAccount.uid}_photo_url", cleanAccount.photoUrl)
                putString("username_reservation_${cleanAccount.username.trim().lowercase()}", cleanAccount.uid)
                commit()
            }
        } catch (e: Exception) {
            Log.w(tag, "Prefs mirror error: ${e.message}")
        }

        // 4. Synchronous atomic JSON file backup
        flushJsonBackup()
    }

    /**
     * Looks up user by normalized email.
     */
    fun getUserByEmail(email: String): StoredUserAccount? {
        val normalized = normalizeEmail(email)
        if (normalized.isBlank()) return null

        emailCache[normalized]?.let { return it }

        // Query SQLite
        try {
            val db = dbHelper.readableDatabase
            db.query(
                "users",
                null,
                "email = ?",
                arrayOf(normalized),
                null,
                null,
                null
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val account = cursorToAccount(cursor)
                    emailCache[normalized] = account
                    uidCache[account.uid] = account
                    return account
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "SQLite lookup by email failed: ${e.message}")
        }

        // Fallback from SharedPreferences
        val uidFromPrefs = prefs.getString("email_to_uid_$normalized", null)
        if (!uidFromPrefs.isNullOrBlank()) {
            val passHash = prefs.getString("account_${uidFromPrefs}_passhash", null)
            if (!passHash.isNullOrBlank()) {
                val username = prefs.getString("account_${uidFromPrefs}_username", "Player") ?: "Player"
                val displayName = prefs.getString("account_${uidFromPrefs}_display_name", username) ?: username
                val role = prefs.getString("account_${uidFromPrefs}_role", "CUSTOMER / PIONEER") ?: "CUSTOMER / PIONEER"
                val verified = prefs.getBoolean("account_${uidFromPrefs}_verified", false)
                val createdAt = prefs.getLong("account_${uidFromPrefs}_created_at", System.currentTimeMillis())
                val lastLogin = prefs.getLong("account_${uidFromPrefs}_last_login_at", System.currentTimeMillis())
                val authProvider = prefs.getString("account_${uidFromPrefs}_auth_provider", "PASSWORD") ?: "PASSWORD"
                val photoUrl = prefs.getString("account_${uidFromPrefs}_photo_url", null)
                val recovered = StoredUserAccount(
                    uid = uidFromPrefs,
                    email = normalized,
                    passwordHash = passHash,
                    username = username,
                    displayName = displayName,
                    role = role,
                    isEmailVerified = verified,
                    createdAt = createdAt,
                    lastLoginAt = lastLogin,
                    authProvider = authProvider,
                    photoUrl = photoUrl
                )
                saveUser(recovered)
                return recovered
            }
        }

        return null
    }

    /**
     * Looks up user by unique UID.
     */
    fun getUserByUid(uid: String): StoredUserAccount? {
        if (uid.isBlank()) return null

        uidCache[uid]?.let { return it }

        // Query SQLite
        try {
            val db = dbHelper.readableDatabase
            db.query(
                "users",
                null,
                "uid = ?",
                arrayOf(uid),
                null,
                null,
                null
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val account = cursorToAccount(cursor)
                    emailCache[account.email] = account
                    uidCache[account.uid] = account
                    return account
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "SQLite lookup by uid failed: ${e.message}")
        }

        // Fallback from SharedPreferences
        val email = prefs.getString("account_${uid}_email", null)
        if (!email.isNullOrBlank()) {
            return getUserByEmail(email)
        }

        return null
    }

    fun isEmailRegistered(email: String): Boolean {
        return getUserByEmail(email) != null
    }

    fun isUsernameTaken(username: String, excludeUid: String? = null): Boolean {
        val trimmed = username.trim().lowercase()
        if (trimmed.isBlank()) return false

        // Check cache
        val inCache = emailCache.values.firstOrNull { it.username.trim().lowercase() == trimmed }
        if (inCache != null && inCache.uid != excludeUid) return true

        // Check SQLite
        try {
            val db = dbHelper.readableDatabase
            db.query(
                "users",
                arrayOf("uid"),
                "LOWER(username) = ?",
                arrayOf(trimmed),
                null,
                null,
                null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val foundUid = cursor.getString(0)
                    if (foundUid != excludeUid) return true
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "SQLite username check error: ${e.message}")
        }

        // Check prefs reservation
        val reservedUid = prefs.getString("username_reservation_$trimmed", null)
        if (reservedUid != null && reservedUid != excludeUid) return true

        return false
    }

    /**
     * Verifies password against stored password hash or fallback.
     */
    fun verifyPassword(account: StoredUserAccount, rawPassword: String): Boolean {
        val inputHash = hashSha256(rawPassword)
        return account.passwordHash == inputHash || account.passwordHash == rawPassword
    }

    @Synchronized
    fun updateLastLogin(uid: String, timestamp: Long = System.currentTimeMillis()) {
        val existing = getUserByUid(uid) ?: return
        val updated = existing.copy(lastLoginAt = timestamp)
        saveUser(updated)
    }

    @Synchronized
    fun updateEmailVerified(uid: String, verified: Boolean) {
        val existing = getUserByUid(uid) ?: return
        val updated = existing.copy(isEmailVerified = verified)
        saveUser(updated)
    }

    /**
     * Permanently deletes a single user when account deletion is explicitly requested.
     */
    @Synchronized
    fun deleteUser(uid: String): Boolean {
        val existing = getUserByUid(uid)
        val email = existing?.email ?: prefs.getString("account_${uid}_email", "") ?: ""

        try {
            val db = dbHelper.writableDatabase
            db.delete("users", "uid = ?", arrayOf(uid))
        } catch (e: Exception) {
            Log.e(tag, "SQLite delete error: ${e.message}")
        }

        emailCache.remove(email)
        uidCache.remove(uid)

        prefs.edit().apply {
            remove("account_${uid}_email")
            remove("account_${uid}_passhash")
            remove("account_${uid}_username")
            remove("account_${uid}_display_name")
            remove("account_${uid}_role")
            remove("account_${uid}_verified")
            remove("account_${uid}_created_at")
            remove("account_${uid}_last_login_at")
            remove("account_${uid}_auth_provider")
            remove("account_${uid}_photo_url")
            if (email.isNotBlank()) {
                remove("email_to_uid_$email")
            }
            if (existing != null) {
                remove("username_reservation_${existing.username.trim().lowercase()}")
            }
            commit()
        }

        flushJsonBackup()
        return true
    }

    fun getAllUsers(): List<StoredUserAccount> {
        return emailCache.values.toList()
    }

    fun getGoogleAccounts(): List<StoredUserAccount> {
        return emailCache.values.filter { it.authProvider.equals("GOOGLE", ignoreCase = true) }
    }

    private fun cursorToAccount(cursor: Cursor): StoredUserAccount {
        val authProviderCol = cursor.getColumnIndex("auth_provider")
        val authProvider = if (authProviderCol != -1 && !cursor.isNull(authProviderCol)) {
            cursor.getString(authProviderCol)
        } else "PASSWORD"

        val photoUrlCol = cursor.getColumnIndex("photo_url")
        val photoUrl = if (photoUrlCol != -1 && !cursor.isNull(photoUrlCol)) {
            cursor.getString(photoUrlCol)
        } else null

        return StoredUserAccount(
            uid = cursor.getString(cursor.getColumnIndexOrThrow("uid")),
            email = normalizeEmail(cursor.getString(cursor.getColumnIndexOrThrow("email"))),
            passwordHash = cursor.getString(cursor.getColumnIndexOrThrow("password_hash")),
            username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
            displayName = cursor.getString(cursor.getColumnIndexOrThrow("display_name")),
            role = cursor.getString(cursor.getColumnIndexOrThrow("role")),
            isEmailVerified = cursor.getInt(cursor.getColumnIndexOrThrow("is_verified")) == 1,
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
            lastLoginAt = cursor.getLong(cursor.getColumnIndexOrThrow("last_login_at")),
            authProvider = authProvider,
            photoUrl = photoUrl
        )
    }

    @Synchronized
    private fun loadAllFromDisk() {
        emailCache.clear()
        uidCache.clear()

        // 1. Read SQLite
        try {
            val db = dbHelper.readableDatabase
            db.query("users", null, null, null, null, null, null).use { cursor ->
                while (cursor.moveToNext()) {
                    val account = cursorToAccount(cursor)
                    emailCache[account.email] = account
                    uidCache[account.uid] = account
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "SQLite load all error: ${e.message}")
        }

        // 2. If SQLite was empty, check JSON backup
        if (emailCache.isEmpty() && backupFile.exists()) {
            try {
                val json = backupFile.readText()
                val list = userListAdapter.fromJson(json)
                list?.forEach { acc ->
                    saveUser(acc)
                }
            } catch (e: Exception) {
                Log.w(tag, "JSON backup restore error: ${e.message}")
            }
        }
    }

    @Synchronized
    private fun flushJsonBackup() {
        try {
            val list = emailCache.values.toList()
            val json = userListAdapter.toJson(list)
            backupFile.writeText(json)
        } catch (e: Exception) {
            Log.w(tag, "Failed to write json backup: ${e.message}")
        }
    }
}
