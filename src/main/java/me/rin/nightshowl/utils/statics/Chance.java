package me.rin.nightshowl.utils.statics;

public class Chance {

    public static Boolean fromPercent(int chance) {

        return Math.random() <= (double) chance / 100;
    }

}
