package ru.spbau.shawanation.database;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.ArrayList;
import java.util.List;

/**
 * AggregatedPost is a class which aggregates posts about same restaurants
 */
@Entity("AggregatedPost")
public class AggregatedPost {
    @Reference
    private List<Post> posts = new ArrayList<>();

    public AggregatedPost() {}
}
