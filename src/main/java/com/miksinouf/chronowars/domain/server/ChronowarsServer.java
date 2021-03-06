package com.miksinouf.chronowars.domain.server;

import static spark.Spark.*;
import static spark.SparkBase.staticFileLocation;

import java.lang.management.ManagementFactory;

import javax.management.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Response;

import com.miksinouf.chronowars.domain.administration.GamesAdminitrator;
import com.miksinouf.chronowars.domain.board.IllegalMoveException;
import com.miksinouf.chronowars.domain.games.GamesQueueSingleton;
import com.miksinouf.chronowars.domain.player.TooManyTokensException;
import com.miksinouf.chronowars.domain.player.UnknownPlayerException;

public class ChronowarsServer {

    private final Logger logger = LoggerFactory.getLogger(ChronowarsServer.class);

    public static void main(String[] args) {
        final ChronowarsServer chronowarsServer = new ChronowarsServer();
        chronowarsServer.startChronowarsServer();
        chronowarsServer.startJmxServer();
    }

    public void startChronowarsServer() {
        logger.info("Chronowars server startup !");
        final ChronowarsAdapter chronowarsAdapter = new ChronowarsAdapter();
        staticFileLocation("/web-resources");

        // permet d'inscrire le joueur à la liste d'attente des parties et
        // renvoie l'identifiant unique
        get("/register/:nickname",
                (request, response) -> GamesQueueSingleton.INSTANCE
                        .register(request.params("nickname")));

        // boolean
        get("/have_i_running_game/:identifier",
                (request, response) -> GamesQueueSingleton.INSTANCE
                        .gamesQueue
                        .players
                        .hasPlayerAGame(request.params("identifier")));

        /**
         * renvoie une réponse json de la forme {
         * "currentColorToPlay": "BLACK",
         * "status": "running",
         * "lastRoundPoints": "12",
         * "blackTokens": {
         *   "(2,2),
         *   (1,3)"
         *   }
         *  "whiteTokens": {
         *  "(1,2),
         *  (2,5),
         *  (3,2)"
         *  }
         *
         * ou :
         *
         * { "currentColorToPlay": "WHITE", "status": "finished", "winner":
         * "BLACK", "blackTokens": "(2,2),(1,3)", "whiteTokens":
         * "(1,2),(2,5),(3,2)" }
         */
        get("/get_game/:playerIdentifier",
                (request, response) -> {
                	try {
                		return chronowarsAdapter.adaptGame(GamesQueueSingleton.INSTANCE
                                .gamesQueue
                                .players
                                .getGame(
                                        request.params("playerIdentifier")));
                	} catch (UnknownPlayerException e) {
                		return badRequest(response,
                                e.toString());
                	}
                });


        /**
         * permet de placer un pion
         */
        put("/set_token/:playerIdentifier/:x/:y",
                (request, response) -> {
                    final String x = request.params("x");
                    final String y = request.params("y");
                    try {
                        return GamesQueueSingleton.INSTANCE.gamesQueue.players.setToken(
                                request.params("playerIdentifier"),
                                parseInt(x), parseInt(y));
                    } catch (UnknownPlayerException | IllegalMoveException | TooManyTokensException | ChronowarsNumberFormatException e) {
                        return badRequest(response,
                                e.toString());
                    }
                });

        /**
         * Permet de déplacer un pion moveDirection = UP | UP_RIGHT | RIGHT |
         * DOWN_RIGHT | DOWN | DOWN_LEFT | LEFT | UP_LEFT
         */
        patch("/move_token/:playerIdentifier/:x/:y/:moveDirection",
                (request, response) -> {
                    final String x = request.params("x");
                    final String y = request.params("y");
                    try {
                        return GamesQueueSingleton.INSTANCE.gamesQueue.players.moveToken(
                                request.params("playerIdentifier"),
                                parseInt(x), parseInt(y),
                                request.params("moveDirection"));
                    } catch (UnknownPlayerException | IllegalMoveException e) {
                        return badRequest(response,
                                e.toString());
                    } catch (ChronowarsNumberFormatException e) {
                        return badRequest(response,
                                "One of the coordinates is not an integer.");
                    }
                });
    }

    public void startJmxServer() {
        final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            final ObjectName gamesAdministrator =
                    new ObjectName("com.miksinouf.chronowars.domain.administration:type=GamesAdministratorMBean");
            mBeanServer.registerMBean(new GamesAdminitrator(), gamesAdministrator);

        } catch (MalformedObjectNameException
                | NotCompliantMBeanException
                | InstanceAlreadyExistsException
                | MBeanRegistrationException e) {
            e.printStackTrace();
        }
    }

    private static String badRequest(Response response, String message) {
        response.status(400);
        return message;
    }

    private static int parseInt(String intToParse)
            throws ChronowarsNumberFormatException {
        try {
            return Integer.parseInt(intToParse);
        } catch (NumberFormatException e) {
            throw new ChronowarsNumberFormatException();
        }
    }

}
