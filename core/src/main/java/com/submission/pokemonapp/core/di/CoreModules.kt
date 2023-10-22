package com.submission.pokemonapp.core.di

import androidx.room.Room
import com.submission.pokemonapp.core.BuildConfig
import com.submission.pokemonapp.core.data.PokemonRepository
import com.submission.pokemonapp.core.data.source.local.PokemonLocalData
import com.submission.pokemonapp.core.data.source.local.room.PokemonDatabase
import com.submission.pokemonapp.core.data.source.remote.PokemonRemoteData
import com.submission.pokemonapp.core.data.source.remote.network.PokemonApiService
import com.submission.pokemonapp.core.domain.repository.IPokemonRepo
import com.submission.pokemonapp.core.utils.AppExecutors
import com.submission.pokemonapp.core.utils.Constant
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.math.sin

val databaseModule = module {
    factory { get<PokemonDatabase>().pokemonDao() }
    single {
        val passPhrase = SQLiteDatabase.getBytes("pokemon".toCharArray())
        val factory = SupportFactory(passPhrase)
        Room.databaseBuilder(
            androidContext(),
            PokemonDatabase::class.java, "Pokemon.db"
        )
            .fallbackToDestructiveMigration()
            .openHelperFactory(factory)
            .build()
    }
}

val networkModule = module {
    single {
        val hostName = Constant.POKEMON_DOMAIN
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    }
    single {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()

        retrofit.create(PokemonApiService::class.java)
    }
}

val repoModule = module {
    single { PokemonLocalData(get()) }
    single { PokemonRemoteData(get()) }
    factory { AppExecutors() }
    single<IPokemonRepo> { PokemonRepository(get(), get(), get()) }
}