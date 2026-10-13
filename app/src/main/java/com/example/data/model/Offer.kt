package com.example.data.model

import com.example.data.local.Deal
import java.text.NumberFormat
import java.util.Locale

enum class OfferCategory(val displayName: String, val iconName: String) {
    ALL("All Offers", "local_offer"),
    CREDIT_CARD("Credit Cards", "credit_card"),
    TRAVEL("Travel & Flights", "flight"),
    SUPERMARKET("Supermarkets", "shopping_cart"),
    DINING("Dining & Cafes", "restaurant"),
    ELECTRONICS("Tech & Gadgets", "devices"),
    FASHION("Fashion & Retail", "checkroom")
}

enum class DealSortOption(val displayName: String, val shortLabel: String) {
    RECOMMENDED("Recommended Deals", "Featured"),
    DISCOUNT_DESC("Highest Discount %", "Highest %"),
    DISCOUNT_ASC("Lowest Discount %", "Lowest %"),
    EXPIRY_SOONEST("Expiring Soonest", "Expiring Soon"),
    EXPIRY_LATEST("Latest Expiry Date", "Valid Longer")
}

data class CountryInfo(
    val country: String,
    val region: String,
    val currencySymbol: String,
    val currencyCode: String,
    val flag: String,
    val cities: List<String>
)

object SupportedLocations {
    const val GLOBAL_CITY = "All Cities / Nationwide"

    val LOCATIONS = listOf(
        // North America
        CountryInfo(
            country = "United States",
            region = "North America",
            currencySymbol = "$",
            currencyCode = "USD",
            flag = "🇺🇸",
            cities = listOf(GLOBAL_CITY, "New York", "San Francisco", "Chicago", "Los Angeles", "Miami", "Austin", "Seattle", "Boston", "Dallas", "Denver", "Las Vegas", "Atlanta")
        ),
        CountryInfo(
            country = "Canada",
            region = "North America",
            currencySymbol = "CA$",
            currencyCode = "CAD",
            flag = "🇨🇦",
            cities = listOf(GLOBAL_CITY, "Toronto", "Vancouver", "Montreal", "Calgary", "Ottawa", "Edmonton", "Quebec City", "Winnipeg")
        ),
        CountryInfo(
            country = "Mexico",
            region = "North America",
            currencySymbol = "Mex$",
            currencyCode = "MXN",
            flag = "🇲🇽",
            cities = listOf(GLOBAL_CITY, "Mexico City", "Guadalajara", "Monterrey", "Cancun", "Puebla", "Tijuana")
        ),

        // Europe
        CountryInfo(
            country = "United Kingdom",
            region = "Europe",
            currencySymbol = "£",
            currencyCode = "GBP",
            flag = "🇬🇧",
            cities = listOf(GLOBAL_CITY, "London", "Manchester", "Edinburgh", "Birmingham", "Glasgow", "Liverpool", "Bristol", "Leeds", "Belfast")
        ),
        CountryInfo(
            country = "Germany",
            region = "Europe",
            currencySymbol = "€",
            currencyCode = "EUR",
            flag = "🇩🇪",
            cities = listOf(GLOBAL_CITY, "Berlin", "Munich", "Frankfurt", "Hamburg", "Cologne", "Stuttgart", "Düsseldorf", "Leipzig")
        ),
        CountryInfo(
            country = "France",
            region = "Europe",
            currencySymbol = "€",
            currencyCode = "EUR",
            flag = "🇫🇷",
            cities = listOf(GLOBAL_CITY, "Paris", "Lyon", "Marseille", "Nice", "Toulouse", "Bordeaux", "Strasbourg", "Lille")
        ),
        CountryInfo(
            country = "Italy",
            region = "Europe",
            currencySymbol = "€",
            currencyCode = "EUR",
            flag = "🇮🇹",
            cities = listOf(GLOBAL_CITY, "Rome", "Milan", "Florence", "Venice", "Naples", "Turin", "Bologna", "Palermo")
        ),
        CountryInfo(
            country = "Spain",
            region = "Europe",
            currencySymbol = "€",
            currencyCode = "EUR",
            flag = "🇪🇸",
            cities = listOf(GLOBAL_CITY, "Madrid", "Barcelona", "Valencia", "Seville", "Malaga", "Bilbao", "Zaragoza")
        ),
        CountryInfo(
            country = "Netherlands",
            region = "Europe",
            currencySymbol = "€",
            currencyCode = "EUR",
            flag = "🇳🇱",
            cities = listOf(GLOBAL_CITY, "Amsterdam", "Rotterdam", "The Hague", "Utrecht", "Eindhoven")
        ),
        CountryInfo(
            country = "Switzerland",
            region = "Europe",
            currencySymbol = "CHF ",
            currencyCode = "CHF",
            flag = "🇨🇭",
            cities = listOf(GLOBAL_CITY, "Zurich", "Geneva", "Basel", "Bern", "Lausanne")
        ),
        CountryInfo(
            country = "Ireland",
            region = "Europe",
            currencySymbol = "€",
            currencyCode = "EUR",
            flag = "🇮🇪",
            cities = listOf(GLOBAL_CITY, "Dublin", "Cork", "Galway", "Limerick")
        ),
        CountryInfo(
            country = "Sweden",
            region = "Europe",
            currencySymbol = "kr ",
            currencyCode = "SEK",
            flag = "🇸🇪",
            cities = listOf(GLOBAL_CITY, "Stockholm", "Gothenburg", "Malmö", "Uppsala")
        ),
        CountryInfo(
            country = "Poland",
            region = "Europe",
            currencySymbol = "zł ",
            currencyCode = "PLN",
            flag = "🇵🇱",
            cities = listOf(GLOBAL_CITY, "Warsaw", "Krakow", "Wroclaw", "Gdansk", "Poznan")
        ),

        // Middle East
        CountryInfo(
            country = "Saudi Arabia",
            region = "Middle East",
            currencySymbol = "SAR ",
            currencyCode = "SAR",
            flag = "🇸🇦",
            cities = listOf(GLOBAL_CITY, "Riyadh", "Jeddah", "Dammam", "Mecca", "Medina", "Khobar", "Tabuk", "Taif", "Abha", "Buraidah", "Jubail", "Yanbu", "Al Ahsa", "Hail", "Najran", "Jizan")
        ),
        CountryInfo(
            country = "United Arab Emirates",
            region = "Middle East",
            currencySymbol = "AED ",
            currencyCode = "AED",
            flag = "🇦🇪",
            cities = listOf(GLOBAL_CITY, "Dubai", "Abu Dhabi", "Sharjah", "Ajman", "Ras Al Khaimah", "Fujairah", "Al Ain")
        ),
        CountryInfo(
            country = "Qatar",
            region = "Middle East",
            currencySymbol = "QAR ",
            currencyCode = "QAR",
            flag = "🇶🇦",
            cities = listOf(GLOBAL_CITY, "Doha", "Al Rayyan", "Al Wakrah", "Al Khor")
        ),
        CountryInfo(
            country = "Kuwait",
            region = "Middle East",
            currencySymbol = "KWD ",
            currencyCode = "KWD",
            flag = "🇰🇼",
            cities = listOf(GLOBAL_CITY, "Kuwait City", "Hawally", "Salmiya", "Al Ahmadi", "Farwaniya")
        ),
        CountryInfo(
            country = "Bahrain",
            region = "Middle East",
            currencySymbol = "BHD ",
            currencyCode = "BHD",
            flag = "🇧🇭",
            cities = listOf(GLOBAL_CITY, "Manama", "Riffa", "Muharraq", "Hamad Town")
        ),
        CountryInfo(
            country = "Oman",
            region = "Middle East",
            currencySymbol = "OMR ",
            currencyCode = "OMR",
            flag = "🇴🇲",
            cities = listOf(GLOBAL_CITY, "Muscat", "Salalah", "Sohar", "Nizwa")
        ),
        CountryInfo(
            country = "Egypt",
            region = "Middle East",
            currencySymbol = "EGP ",
            currencyCode = "EGP",
            flag = "🇪🇬",
            cities = listOf(GLOBAL_CITY, "Cairo", "Alexandria", "Giza", "Sharm El Sheikh", "Hurghada", "Mansoura")
        ),
        CountryInfo(
            country = "Turkey",
            region = "Middle East",
            currencySymbol = "₺",
            currencyCode = "TRY",
            flag = "🇹🇷",
            cities = listOf(GLOBAL_CITY, "Istanbul", "Ankara", "Izmir", "Antalya", "Bursa")
        ),

        // Asia Pacific
        CountryInfo(
            country = "India",
            region = "Asia Pacific",
            currencySymbol = "₹",
            currencyCode = "INR",
            flag = "🇮🇳",
            cities = listOf(GLOBAL_CITY, "Mumbai", "Delhi", "Bengaluru", "Hyderabad", "Chennai", "Kolkata", "Pune", "Ahmedabad", "Jaipur", "Kochi")
        ),
        CountryInfo(
            country = "Singapore",
            region = "Asia Pacific",
            currencySymbol = "S$",
            currencyCode = "SGD",
            flag = "🇸🇬",
            cities = listOf(GLOBAL_CITY, "Central Business District", "Marina Bay", "Orchard", "Jurong", "Tampines", "Sentosa")
        ),
        CountryInfo(
            country = "Australia",
            region = "Asia Pacific",
            currencySymbol = "A$",
            currencyCode = "AUD",
            flag = "🇦🇺",
            cities = listOf(GLOBAL_CITY, "Sydney", "Melbourne", "Brisbane", "Perth", "Adelaide", "Gold Coast", "Canberra")
        ),
        CountryInfo(
            country = "New Zealand",
            region = "Asia Pacific",
            currencySymbol = "NZ$",
            currencyCode = "NZD",
            flag = "🇳🇿",
            cities = listOf(GLOBAL_CITY, "Auckland", "Wellington", "Christchurch", "Hamilton", "Queenstown")
        ),
        CountryInfo(
            country = "Japan",
            region = "Asia Pacific",
            currencySymbol = "¥",
            currencyCode = "JPY",
            flag = "🇯🇵",
            cities = listOf(GLOBAL_CITY, "Tokyo", "Osaka", "Kyoto", "Yokohama", "Nagoya", "Sapporo", "Fukuoka")
        ),
        CountryInfo(
            country = "South Korea",
            region = "Asia Pacific",
            currencySymbol = "₩",
            currencyCode = "KRW",
            flag = "🇰🇷",
            cities = listOf(GLOBAL_CITY, "Seoul", "Busan", "Incheon", "Daegu", "Daejeon")
        ),
        CountryInfo(
            country = "Hong Kong",
            region = "Asia Pacific",
            currencySymbol = "HK$",
            currencyCode = "HKD",
            flag = "🇭🇰",
            cities = listOf(GLOBAL_CITY, "Central", "Kowloon", "Mong Kok", "Tsim Sha Tsui", "Causeway Bay")
        ),
        CountryInfo(
            country = "Malaysia",
            region = "Asia Pacific",
            currencySymbol = "RM ",
            currencyCode = "MYR",
            flag = "🇲🇾",
            cities = listOf(GLOBAL_CITY, "Kuala Lumpur", "Penang", "Johor Bahru", "Kota Kinabalu")
        ),
        CountryInfo(
            country = "Thailand",
            region = "Asia Pacific",
            currencySymbol = "฿",
            currencyCode = "THB",
            flag = "🇹🇭",
            cities = listOf(GLOBAL_CITY, "Bangkok", "Phuket", "Chiang Mai", "Pattaya")
        ),

        // Latin America & Africa
        CountryInfo(
            country = "Brazil",
            region = "Latin America",
            currencySymbol = "R$",
            currencyCode = "BRL",
            flag = "🇧🇷",
            cities = listOf(GLOBAL_CITY, "São Paulo", "Rio de Janeiro", "Brasília", "Salvador", "Fortaleza")
        ),
        CountryInfo(
            country = "South Africa",
            region = "Africa",
            currencySymbol = "R ",
            currencyCode = "ZAR",
            flag = "🇿🇦",
            cities = listOf(GLOBAL_CITY, "Johannesburg", "Cape Town", "Durban", "Pretoria")
        )
    )

    const val DEFAULT_COUNTRY = "United States"
    const val DEFAULT_CITY = GLOBAL_CITY

    fun getCountryInfo(country: String): CountryInfo {
        return LOCATIONS.find { it.country.equals(country, ignoreCase = true) }
            ?: LOCATIONS.first()
    }

    fun getCurrencySymbol(country: String): String {
        return getCountryInfo(country).currencySymbol
    }

    fun getCurrencyCode(country: String): String {
        return getCountryInfo(country).currencyCode
    }

    fun formatPrice(amount: Double, country: String): String {
        val info = getCountryInfo(country)
        return when {
            info.currencyCode == "JPY" || info.currencyCode == "KRW" -> {
                "${info.currencySymbol}${NumberFormat.getIntegerInstance().format(amount.toLong())}"
            }
            amount == amount.toLong().toDouble() -> {
                "${info.currencySymbol}${NumberFormat.getIntegerInstance().format(amount.toLong())}"
            }
            else -> {
                "${info.currencySymbol}${String.format(Locale.US, "%.2f", amount)}"
            }
        }
    }
}

data class Offer(
    val id: String,
    val title: String,
    val merchant: String,
    val source: String,
    val category: OfferCategory,
    val country: String,
    val city: String,
    val discountPercentage: Double = 0.0,
    val originalPrice: Double = 0.0,
    val discountHighlight: String,
    val description: String,
    val promoCode: String? = null,
    val linkUrl: String,
    val expiryDate: String,
    val daysUntilExpiry: Int,
    val verifiedRating: Int = 96,
    val minimumSpend: String? = null,
    val terms: String,
    val tags: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val isAiDiscovered: Boolean = false
) {
    val discountedPrice: Double
        get() = if (originalPrice > 0) {
            originalPrice * (1.0 - (discountPercentage / 100.0).coerceIn(0.0, 1.0))
        } else 0.0

    val isExpiringSoon: Boolean
        get() = daysUntilExpiry <= 3

    fun toDealEntity(): Deal = Deal(
        id = id,
        title = title,
        category = category.name,
        merchant = merchant,
        discountPercentage = discountPercentage,
        expiryDate = expiryDate,
        originalPrice = originalPrice,
        country = country,
        city = city,
        source = source,
        discountHighlight = discountHighlight,
        description = description,
        promoCode = promoCode,
        linkUrl = linkUrl,
        daysUntilExpiry = daysUntilExpiry,
        verifiedRating = verifiedRating,
        minimumSpend = minimumSpend,
        terms = terms,
        tags = tags.joinToString(","),
        isFeatured = isFeatured,
        isAiDiscovered = isAiDiscovered
    )
}
