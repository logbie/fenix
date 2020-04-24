/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ui

import androidx.test.espresso.NoMatchingViewException
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.Rule
import org.mozilla.fenix.helpers.AndroidAssetDispatcher
import org.mozilla.fenix.helpers.HomeActivityTestRule
import org.mozilla.fenix.helpers.TestAssetHelper
import org.mozilla.fenix.ui.robots.homeScreen
import org.mozilla.fenix.ui.robots.navigationToolbar

/**
 *  Tests for verifying basic functionality of tab collection
 *
 */

class CollectionTest {
    /* ktlint-disable no-blank-line-before-rbrace */ // This imposes unreadable grouping.

    private val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    val activityTestRule = HomeActivityTestRule()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply {
            setDispatcher(AndroidAssetDispatcher())
            start()
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    // open a webpage, and add currently opened tab to existing collection
    fun addTabToCollectionTest() {
        val secondWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 2)

        createCollectionFromTab("testcollection_1")

        homeScreen {
            verifyExistingTabList()
            closeTab()
        }.openNavigationToolbar {
        }.enterURLAndEnterToBrowser(secondWebPage.url) {
            verifyPageContent(secondWebPage.content)
        }.openThreeDotMenu {
            clickBrowserViewSaveCollectionButton()
        }.selectExistingCollection("testcollection_1") {
            verifySnackBarText("Tab saved!")
        }.openHomeScreen {
            verifyExistingTabList()
            closeTab()
            // On homeview, expand the collection and open the first saved page
            expandCollection("testcollection_1")
            verifyItemInCollectionExists("Test_Page_1")
            verifyItemInCollectionExists("Test_Page_2")
        }
    }

    @Test
    fun renameCollectionTest() {
        createCollectionFromTab("testcollection_1")

        homeScreen {
            // On homeview, tap the 3-dot button to expand, select rename, rename collection
            clickCollectionThreeDotButton()
            selectRenameCollection()
            typeCollectionName("renamed_collection")
            verifyCollectionIsDisplayed("renamed_collection")
        }
    }

    @Test
    fun deleteCollectionTest() {
        createCollectionFromTab("testcollection_1")

        homeScreen {
            // Choose delete collection from homeview, and confirm
            clickCollectionThreeDotButton()
            selectDeleteCollection()
            confirmDeleteCollection()
            verifyNoCollectionsHeader()
        }
    }

    @Test
    fun createCollectionFromTabTest() {
        createCollectionFromTab("testcollection_1")
        createCollectionFromTab("testcollection_2", false)

        homeScreen {
            // swipe to bottom until the collections are shown
            verifyHomeScreen()
            try {
                verifyCollectionIsDisplayed("testcollection_1")
            } catch (e: NoMatchingViewException) {
                scrollToElementByText("testcollection_1")
            }
            verifyCollectionIsDisplayed("testcollection_2")
        }
    }

    private fun createCollectionFromTab(collectionName: String, firstCollection: Boolean = true) {
        val firstWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        // Open a webpage and save to collection "testcollection_1"
        navigationToolbar {
        }.enterURLAndEnterToBrowser(firstWebPage.url) {
            verifyPageContent(firstWebPage.content)
        }.openThreeDotMenu {
            // click save to collection menu item, type collection name
            clickBrowserViewSaveCollectionButton()
            if (!firstCollection)
                clickAddNewCollection()

        }.typeCollectionName(collectionName) {
            verifySnackBarText("Tab saved!")
        }.openHomeScreen {
            mDevice.wait(
                Until.findObject(By.text(collectionName)),
                TestAssetHelper.waitingTime
            )
        }
    }
}
