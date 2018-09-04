package com.craiovadata.sightsandsounds.util;


import com.craiovadata.sightsandsounds.model.Rating;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Utilities for Ratings.
 */
public class RatingUtil {

    private static final String[][] REVIEW_CONTENTS = {
            { // 0-1 stars
                    "the song is ok but photo is a bit dim",
                    "could have been clearer",
                    "instruments didn't wake up that morning",
                    "the image is a bit fade",
                    "music from back times"
            },

            {  // 1-2 stars
                    "The photo is a little too dark",
                    "After a semester of thinking about music, I am still unsure as to what constitutes \"good\" or \"bad\" music.",
                    "But what about when music is created to be intentionally terrible? That might sound like a wild thing to attempt",
                    "popular artist with Music from the past",
                    "To live is to be musical, starting with the blood dancing in your veins..."
            },

            {  //2-3 stars
                    "Interesting sight and sound",
                    "average song but I like the picture",
                    "Classical 90's song",
                    "this intro is short but sparki",
                    "not bad. Good old music"
            },

            {  // 3-4 stars
                    "This is a nice photo",
                    "Music is great",
                    "Good old hit",
                    "I listen on my way to work",
                    "Some people can dig up great music like magic"
            },

            {  // 4-5 stars
                    "This is fantastic! Best ever!",
                    "So great music. The photo is nice to.",
                    "I play it every day",
                    "best songs of all time",
                    "Masive!!! This is my favourite"
            }

    };


    /**
     * Get a list of random Rating POJOs.
     */
    public static List<Rating> getRandomList(int length) {
        List<Rating> result = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            result.add(getRandom());
        }

        return result;
    }

    /**
     * Get the average rating of a List.
     */
    public static double getAverageRating(List<Rating> ratings) {
        double sum = 0.0;

        for (Rating rating : ratings) {
            sum += rating.getRating();
        }

        return sum / ratings.size();
    }

    public static double getTotalRating(List<Rating> ratings) {
        double sum = 0.0;

        for (Rating rating : ratings) {
            sum += rating.getRating();
        }

        return sum;
    }

    /**
     * Create a random Rating POJO.
     */
    public static Rating getRandom() {
        Rating rating = new Rating();

        Random random = new Random();

        double score = random.nextDouble() * 5.0;
        String text = REVIEW_CONTENTS[(int) Math.floor(score)][random.nextInt(5)];

        rating.setUserId(UUID.randomUUID().toString());
        rating.setUserName(getRandomName(random));
        rating.setRating(score);
        rating.setText(text);

        return rating;
    }

    private static final String[] NAME_FIRST_WORDS = {
            "Herbert",
            "Goldie",
            "Jerold",
            "Sherman",
            "Jenifffer",
            "Sam",
            "Salina",
            "Rosia",
            "Lavone",
            "Yan",
            "Gilma",
            "Gregory",
            "Marcelle",
            "Hope",
            "Nancie",
            "Sherryl"
    };

    private static final String[] NAME_SECOND_WORDS = {
            "Kildow",
            "Boomer",
            "Fiala",
            "Vandegrift",
            "Petrarca",
            "Thornley",
            "Mantle",
            "Kuchera",
            "Cha",
            "Duplessis",
            "Mcfarling",
            "Vandeventer",
            "Chute",
            "Liao",
            "Fernald",
            "Turnage",
            "Pembleton",
            "Peavey",
            "Lalor",
            "Leaf"
    };

    private static String getRandomName(Random random) {
        return getRandomString(NAME_FIRST_WORDS, random) + " "
                + getRandomString(NAME_SECOND_WORDS, random);
    }

    private static String getRandomString(String[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }

    private static int getRandomInt(int[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }

}
