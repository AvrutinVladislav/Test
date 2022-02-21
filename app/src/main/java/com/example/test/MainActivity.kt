package com.example.test

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.databinding.ActivityMainBinding
import com.example.test.databinding.ItemReviewBinding
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val reviews: MutableLiveData<ReviewsResponseDto> = MutableLiveData<ReviewsResponseDto>()
    val adapter by lazy { ReviewsListAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding.rvReviewsList) {
            adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        getReviews()
        reviews.observe(this, {
            adapter.reviewsList.addAll(it.reviews!!)
            adapter.notifyDataSetChanged()
        })
    }

    fun getReviews() {
        val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.nytimes.com/svc/movies/v2/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val service = (retrofit.create(ReviewsService::class.java))

        GlobalScope.launch(Dispatchers.IO) {
            val response = service.getReviews()
            reviews.postValue(response)
            Log.d("My", "$response")
        }

    }


//    val adapter: JsonAdapter<ReviewsService> = moshi.adapter(ReviewsService::class.java)


}

interface ReviewsService {
    @GET("reviews/all.json?api-key=cqmVWmORvGkA3iKgGX7URNzutvC3RrQS")
    suspend fun getReviews(): ReviewsResponseDto
}

@JsonClass(generateAdapter = false)
data class ReviewsResponseDto(
    @Json(name = "status")
    val status: String,
    @Json(name = "copyright")
    val copyright: String,
    @Json(name = "has_more")
    val hasMore: Boolean,
    @Json(name = "num_results")
    val numResults: Int,
    @Json(name = "results")
    val reviews: List<ReviewDto>?
)

@JsonClass(generateAdapter = false)
data class ReviewDto(
    @Json(name = "display_title")
    val displayTitle: String,
    @Json(name = "byline")
    val byline: String,
    @Json(name = "summary_short")
    val summaryShort: String,
    @Json(name = "publication_date")
    val publicationDate: String,
    @Json(name = "date_updated")
    val dateUpdated: String,
    val link: Link,
    val multimedia: Multimedia?
)

@JsonClass(generateAdapter = false)
data class Link(
    val url: String
)

@JsonClass(generateAdapter = false)
data class Multimedia(
    val src: String?
)

class ReviewsListAdapter : RecyclerView.Adapter<ReviewsListAdapter.ViewHolder>() {
    var reviewsList: MutableList<ReviewDto> = emptyList<ReviewDto>().toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemBinding: ItemReviewBinding = ItemReviewBinding.bind(view)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviewsList[position]
        with(holder.itemBinding) {
            review.multimedia?.src?.let {
                Glide.with(holder.itemView).load(it).into(post)
            }
        header.text = review.displayTitle
            textPreview.text = review.summaryShort
            criticName.text = review.byline
            calendarData.text = review.dateUpdated

        }
    }

    override fun getItemCount(): Int {
        return reviewsList.size
    }
}


