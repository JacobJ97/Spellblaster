package com.ghostchippies.spellblasterforandroidv44;

class Splitter {

    private String word;
    private String description;

    Splitter(String stringToSplit) {
        String[] parts = stringToSplit.split(",");
        word = parts[0];
        description = parts[1];
    }

    String getWord() {
        return word;
    }

    String getDescription() {
        return description;
    }

}
