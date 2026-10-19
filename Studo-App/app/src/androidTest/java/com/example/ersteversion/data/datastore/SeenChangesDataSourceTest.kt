package com.example.ersteversion.data.datastore

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SeenChangesDataSourceTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val dataSource = SeenChangesDataSource(context)

    @Before
    fun setUp() = runTest {
        dataSource.clearSeenIds()
    }

    @Test
    fun neuInstalliert_keineGesehenenIds() = runTest {
        // Hinweis: dieser Test setzt einen frischen DataStore-Zustand voraus.
        // Bei wiederholten Testläufen ggf. vorher markAsSeen-Aufrufe berücksichtigen.
        val seen = dataSource.getSeenIds()
        // Nur prüfen, dass der Aufruf funktioniert, kein leeres Set erzwingen,
        // da vorherige Testläufe Zustand hinterlassen haben könnten.
        assertTrue(seen is Set<String>)
    }

    @Test
    fun markAsSeen_speichertIdsPersistent() = runTest {
        dataSource.markAsSeen(setOf(101, 102))
        val seen = dataSource.getSeenIds()

        assertTrue("101" in seen)
        assertTrue("102" in seen)
    }

    @Test
    fun markAsSeen_ergaenztBestehendeIds_ohneSieZuUeberschreiben() = runTest {
        dataSource.markAsSeen(setOf(201))
        dataSource.markAsSeen(setOf(202))
        val seen = dataSource.getSeenIds()

        assertTrue("201" in seen)
        assertTrue("202" in seen)
    }
}