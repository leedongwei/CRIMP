package com.nusclimb.live.crimp;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Index;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;
import de.greenrobot.daogenerator.ToOne;

/**
 * Generates entities and DAOs for the example project DaoExample.
 *
 * Run it as a Java application (not Android).
 *
 * @author Markus
 */
public class CrimpDaoGenerator {

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1, "com.nusclimb.live.crimp.schema");
        addEntities(schema);

        new DaoGenerator().generateAll(schema, "app/src/main/java");
    }

    private static void addEntities(Schema schema){
        // User
        Entity user = schema.addEntity("UserDb");
        user.addLongProperty("fb_user_id").primaryKey().index();
        user.addStringProperty("fb_access_token").notNull();
        user.addStringProperty("user_name").notNull();
        user.addLongProperty("sequential_token");
        user.addIntProperty("flag").notNull();

        // Climber
        Entity climber = schema.addEntity("ClimberDb");
        climber.addLongProperty("climber_id").primaryKey().index();
        climber.addStringProperty("climber_name");
        climber.addIntProperty("flag").notNull();

        // Route
        Entity route = schema.addEntity("RouteDb");
        Property routeId = route.addLongProperty("route_id").primaryKey().index().getProperty();
        route.addStringProperty("route_name").notNull();
        route.addStringProperty("score_type").notNull();
        route.addDateProperty("time_start");
        route.addDateProperty("time_end");
        route.addIntProperty("flag").notNull();

        // Relation between Climber and Route
        Property routeForClimber = climber.addLongProperty("active_on").getProperty();
        climber.addToOne(route, routeForClimber);

        // Relation between User and Route
        Property userForRoute = route.addLongProperty("judge").getProperty();
        route.addToOne(user, userForRoute);
        Property routeForUser = route.addLongProperty("judging").getProperty();
        ToOne judging = user.addToOne(route, routeForUser);
        judging.setName("judging");
        Property routeForUser2 = route.addLongProperty("help_me").getProperty();
        ToOne helpMe = user.addToOne(route, routeForUser2);
        helpMe.setName("help_me");

        // Category
        Entity category = schema.addEntity("CategoryDb");
        category.addLongProperty("category_id").primaryKey().index();
        category.addStringProperty("category_name").notNull();
        category.addStringProperty("acronym").notNull();
        category.addIntProperty("flag").notNull();

        // Relation between Route and Category
        Property categoryForRoute = route.addLongProperty("category_id").notNull().getProperty();
        route.addToOne(category, categoryForRoute);
        ToMany categoryToRoutes = category.addToMany(route, categoryForRoute);
        categoryToRoutes.setName("routes"); // Optional
        categoryToRoutes.orderAsc(routeId); // Optional

        // Marker
        Entity marker = schema.addEntity("MarkerDb");
        marker.addStringProperty("marker_id").notNull();
        marker.addIntProperty("flag").notNull();

        // Relation between MarkerId, Route and Category
        Property climberForMarker = marker.addLongProperty("climber_id").notNull().getProperty();
        Property categoryForMarker = marker.addLongProperty("category_id").notNull().getProperty();
        marker.addToOne(climber, climberForMarker);
        marker.addToOne(category, categoryForMarker);
        Index markerIndexUnique = new Index();
        markerIndexUnique.addProperty(climberForMarker);
        markerIndexUnique.addProperty(categoryForMarker);
        marker.addIndex(markerIndexUnique);

        // Score
        Entity score = schema.addEntity("ScoreDb");
        score.addStringProperty("score");
        score.addStringProperty("append_score");
        score.addIntProperty("flag").notNull();

        // Relation between Score, Climber, Category and Route
        Property routeForScore = score.addLongProperty("route_id").notNull().getProperty();
        score.addToOne(route, routeForScore);
        Property climberForScore = score.addLongProperty("climber_id").notNull().getProperty();
        score.addToOne(climber, climberForScore);
        Index scoreIndexUnique = new Index();
        scoreIndexUnique.addProperty(routeForScore);
        scoreIndexUnique.addProperty(climberForScore);
        scoreIndexUnique.makeUnique();
        score.addIndex(scoreIndexUnique);

        Entity transaction = schema.addEntity("TransactionDb");
        transaction.addIdProperty().primaryKey();
        Property msb = transaction.addLongProperty("msb").notNull().getProperty();
        Property lsb = transaction.addLongProperty("lsb").notNull().getProperty();
        Index transactionIndexUnique = new Index();
        transactionIndexUnique.addProperty(msb);
        transactionIndexUnique.addProperty(lsb);
        transactionIndexUnique.makeUnique();
        transaction.addIndex(transactionIndexUnique);
        transaction.addStringProperty("request");
        transaction.addIntProperty("flag").notNull();
    }
}
