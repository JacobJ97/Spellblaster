package com.ghostchippies.spellblasterforandroidv44;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


class Shuffler {

    private String shuffledWord;

    Shuffler(String wordToShuffle) {
        List<String> letters = Arrays.asList(wordToShuffle.split(""));
        Collections.shuffle(letters);
        shuffledWord = "";
        for (String letter : letters) {
            shuffledWord += letter;
        }
    }

    String getShuffledWord() {
        return shuffledWord;
    }

}
