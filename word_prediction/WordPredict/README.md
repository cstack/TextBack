Current usage:
java WordPredict dictionary max_choices

Where dictionary is the dictionary file (i.e. word-freq.dict) and max_choices is the maximum number of choices that should be returned

To-do:
* Dictionary file contains duplicate words (because some words have multiple parts of speech with different frequencies); these duplicates should perhaps be added together
* The dictionary is limited to the top 5,000 words in the English language (as compiled by the Corpus of Contemporary American English (COCA) from BYU [http://corpus.byu.edu/coca/], and therefore there should either be more words added (with lower weights), or the Android app could maybe fall back to a built-in Android dictionary
