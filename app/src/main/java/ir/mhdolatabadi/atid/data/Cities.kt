package ir.mhdolatabadi.atid.data

/** Iran's 31 provincial capitals. A 1:1 mirror of web/src/data/cities.ts. */
data class City(val slug: String, val name: String, val latitude: Double, val longitude: Double)

object Cities {
    val all: List<City> = listOf(
        City("tehran", "تهران", 35.6892, 51.389),
        City("mashhad", "مشهد", 36.2972, 59.6067),
        City("isfahan", "اصفهان", 32.6525, 51.6746),
        City("shiraz", "شیراز", 29.5918, 52.5837),
        City("tabriz", "تبریز", 38.08, 46.2919),
        City("karaj", "کرج", 35.84, 50.9391),
        City("qom", "قم", 34.6399, 50.8759),
        City("ahvaz", "اهواز", 31.3183, 48.6706),
        City("kermanshah", "کرمانشاه", 34.3142, 47.065),
        City("urmia", "ارومیه", 37.5527, 45.0761),
        City("rasht", "رشت", 37.2808, 49.5832),
        City("zahedan", "زاهدان", 29.4963, 60.8629),
        City("hamedan", "همدان", 34.7983, 48.5148),
        City("kerman", "کرمان", 30.2839, 57.0834),
        City("yazd", "یزد", 31.8974, 54.3569),
        City("ardabil", "اردبیل", 38.2498, 48.2933),
        City("bandar-abbas", "بندرعباس", 27.1832, 56.2666),
        City("arak", "اراک", 34.0954, 49.7013),
        City("zanjan", "زنجان", 36.6765, 48.4963),
        City("sanandaj", "سنندج", 35.3219, 46.9862),
        City("qazvin", "قزوین", 36.2797, 50.0049),
        City("khorramabad", "خرم‌آباد", 33.4878, 48.3558),
        City("gorgan", "گرگان", 36.8456, 54.4393),
        City("sari", "ساری", 36.5633, 53.0601),
        City("bushehr", "بوشهر", 28.9234, 50.8203),
        City("birjand", "بیرجند", 32.8663, 59.2211),
        City("ilam", "ایلام", 33.6374, 46.4227),
        City("bojnurd", "بجنورد", 37.4747, 57.329),
        City("shahrekord", "شهرکرد", 32.3256, 50.8644),
        City("semnan", "سمنان", 35.5729, 53.3971),
        City("yasuj", "یاسوج", 30.6682, 51.588),
    )

    val default: City = all.first()

    fun bySlug(slug: String?): City? = all.firstOrNull { it.slug == slug }
}
