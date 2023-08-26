package io.arsha.api.data.rest;

import java.util.LinkedHashSet;

public class Ids extends LinkedHashSet<Long> {
    public static Ids collect(long... ids) {
        var set = new Ids();
        for (var id : ids) {
            set.add(id);
        }
        return set;
    }
}
