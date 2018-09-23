package com.spiritflightapps.memverse.utils

const val RATINGS_INFO_TEXT = """

Please rate how you do on the verse with the number buttons

5. Remembered - Perfect
4. Remembered - Hesitation
3. Remembered - Difficult
2. Sorry - had to tap Show
1. Sorry - No Idea

Don't stress about it though
the system is flexible
have fun!

"""
var youversionToMemverseMap: HashMap<String, String> = hashMapOf(
        "KJV" to "KJV",
        "NIV" to "NNV",
        "ERV" to "ERV",
        "NLT" to "NLT",
        "AMP" to "AMP",
        "CEV" to "CEV",
        "ERV" to "ERV",
        "ESV" to "ESV",
        "GW" to "GW",
        "GNT" to "GNT",
        "HCSB" to "HCS",
        "NASB" to "NAS",
        "NCV" to "NCV",
        "NIRV" to "IRV",
        "NKV" to "NKJ",
        "NRSV" to "NRS",
        "MSG" to "MSG",
        "ESV" to "ESV07",
        "DARBY" to "DTL"
)
// yay, google search showed https://maustsontoast.com/2010/comma-separated-list-of-bible-books
const val BOOKS_OF_BIBLE = """
    Genesis, Exodus, Leviticus, Numbers, Deuteronomy, Joshua, Judges, Ruth, 1 Samuel, 2 Samuel, 1 Kings, 2 Kings, 1 Chronicles, 2 Chronicles, Ezra, Nehemiah, Esther, Job, Psalms, Proverbs, Ecclesiastes, Song of Solomon, Isaiah, Jeremiah, Lamentations, Ezekiel, Daniel, Hosea, Joel, Amos, Obadiah, Jonah, Micah, Nahum, Habakkuk, Zephaniah, Haggai, Zechariah, Malachi, Matthew, Mark, Luke, John, Acts, Romans, 1 Corinthians, 2 Corinthians, Galatians, Ephesians, Philippians, Colossians, 1 Thessalonians, 2 Thessalonians, 1 Timothy, 2 Timothy, Titus, Philemon, Hebrew, James, 1 Peter, 2 Peter, 1 John, 2 John, 3 John, Jude, Revelation
"""

const val TRANSLATIONS_ABBREVIATIONS_TEXT = """
New International Version (1984) - NIV,
King James Version (Modernized/1987) - KJV,
New International Version (2011) - NNV,
New Living Translation (2007) - NLT,
Amplified Bible (Classic Edition) (1987) - AMP,
Contemporary English Version (1995) - CEV,
Easy-to-Read Version (2006) - ERV,
English Standard Version (2011) - ESV,
God's Word Translation (1995) - GW,
Good News Translation (1992) - GNT,
Holman Christian Standard Bible (2009) - HCS,
New American Standard Bible (1995) - NAS,
New Century Version (2005) - NCV,
New International Reader's Version (1998) - IRV,
New King James Version (1982) - NKJ,
New Revised Standard Version (1989) - NRS,
The Message (2002) - MSG,
Authorized King James Version (1769) - AKJ,
New Living Translation (2015) - NLT15,
English Standard Version (2007) - ESV07,
Darby Translation - DTL
"""