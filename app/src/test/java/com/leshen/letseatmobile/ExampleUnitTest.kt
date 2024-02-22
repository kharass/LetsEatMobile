package com.leshen.letseatmobile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.leshen.letseatmobile.restaurantPanel.RestaurantPanelModel
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
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
import org.mockito.junit.MockitoRule
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import retrofit2.Response

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
