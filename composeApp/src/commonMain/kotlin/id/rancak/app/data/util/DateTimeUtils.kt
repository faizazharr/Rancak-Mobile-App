package id.rancak.app.data.util

/** Convert "YYYY-MM-DD" to "YYYY-MM-DDT00:00:00Z" for backend date-time fields. */
fun String.toDateTimeString(): String =
    if (length == 10 && matches(Regex("""\d{4}-\d{2}-\d{2}"""))) "${this}T00:00:00Z" else this
