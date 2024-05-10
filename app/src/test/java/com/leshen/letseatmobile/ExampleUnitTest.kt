package com.leshen.letseatmobile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.leshen.letseatmobile.restaurantPanel.RestaurantPanelModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import retrofit2.Response
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class RestaurantPanelViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var apiService: ApiService

    @InjectMocks
    lateinit var viewModel: RestaurantPanelViewModel

    @Before
    fun setup() {
    }
    @Test
    fun `fetchDataFromApi should update errorMessage on HTTP 404 error`() = runBlocking {

        val restaurantId = 1
        `when`(apiService.getRestaurantPanelData(restaurantId)).thenThrow(
            HttpException(
                Response.error<RestaurantPanelModel>(
                    404,
                    "not found".toResponseBody("text/plain".toMediaTypeOrNull())
                )
            )
        )


        viewModel.fetchDataFromApi(restaurantId)


        assertNull(viewModel.restaurantData.value)
        assertEquals("Failed to connect to the server", viewModel.errorMessage.value)
    }

    @Test
    fun `fetchDataFromApi should update errorMessage on generic exception`() = runBlocking {

        val restaurantId = 1
        `when`(apiService.getRestaurantPanelData(restaurantId)).thenThrow(RuntimeException("Some generic exception"))

        viewModel.fetchDataFromApi(restaurantId)

        assertNull(viewModel.restaurantData.value)
        assertEquals("Failed to connect to the server", viewModel.errorMessage.value)
    }
}

@RunWith(MockitoJUnitRunner::class)
class FavouritesTest {

    @Mock
    lateinit var apiService: ApiService

    @Mock
    lateinit var favourites: Favourites

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        favourites = Favourites()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `addToFavorites_success`() = runBlockingTest {
        val restaurantId = 1
        `when`(apiService.addToFavorites(restaurantId.toLong())).thenReturn(Response.success(Unit))

        favourites.addToFavorites(restaurantId)

        advanceUntilIdle()

        assertFalse(favourites.showToastCalled)
        assertEquals("Restaurant added to favorites", favourites.showToastMessage)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `removeFromFavorites_success`() = runBlockingTest {
        val restaurantId = 1
        `when`(apiService.removeFromFavorites(restaurantId.toLong())).thenReturn(Response.success(Unit))

        favourites.removeFromFavorites(restaurantId)

        assertTrue(favourites.showToastCalled)
        assertEquals("Restaurant removed from favorites", favourites.showToastMessage)
    }
}
