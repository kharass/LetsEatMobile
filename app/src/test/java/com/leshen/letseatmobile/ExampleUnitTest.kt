package com.leshen.letseatmobile

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.leshen.letseatmobile.restaurantList.Table
import com.leshen.letseatmobile.restaurantList.TablesAdapter
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
import org.robolectric.annotation.Config
import android.app.Application
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import okhttp3.ResponseBody
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.shadows.ShadowLog

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


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ReservationTest {

    @Test
    fun `Shows all tables for specific restaurant`() = runBlocking {

        val context = ApplicationProvider.getApplicationContext<Context>()
        val tables = listOf(
            Table(tableId = 1, restaurantId = 1, token = "a", size = 4),
            Table(tableId = 2, restaurantId = 1, token = "a", size = 2),
            Table(tableId = 3, restaurantId = 1, token = "a", size = 6)
        )
        var clickedTable: Table? = null
        val adapter = TablesAdapter(tables) { table ->
            clickedTable = table
        }

        val recyclerView = RecyclerView(context)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

        val parent = LayoutInflater.from(context).inflate(R.layout.table_item, null) as ViewGroup
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(viewHolder, 0)

        assertEquals(3, adapter.itemCount)
        val buttonTable = viewHolder.itemView.findViewById<TextView>(R.id.buttonTable)
        assertEquals("Stolik nr 1 (4 os.)", buttonTable.text.toString())

        buttonTable.performClick()
        assertEquals(tables[0], clickedTable)
    }
}


@ExperimentalCoroutinesApi
class ReservationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Mock
    private lateinit var apiService: ApiService

    @Mock
    private lateinit var application: Application

    private lateinit var reservationViewModel: ReservationViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(mainThreadSurrogate)
        reservationViewModel = ReservationViewModel(application)
        reservationViewModel.apiService = apiService
        ShadowLog.setupLogging()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun cancelReservation_shouldShowError() = runBlockingTest {

        val reservationId = 1L
        val table = Table(1, 3, 67, "12345" )
        val errorResponse = Response.error<Void>(400, ResponseBody.create(null, "Bad Request"))

        `when`(apiService.deleteReservation(reservationId)).thenReturn(errorResponse)

        reservationViewModel.cancelReservation(reservationId, table)

        val logItems = ShadowLog.getLogs()
        val log = logItems.find { it.tag == "HTTP_ERROR" && it.msg.contains("Failed to cancel reservation: 400") }

        assert(log != null)
    }
}