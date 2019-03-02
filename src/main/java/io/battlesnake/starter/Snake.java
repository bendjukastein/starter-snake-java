package io.battlesnake.starter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.get;

/**
 * Snake server that deals with requests from the snake engine.
 * Just boiler plate code.  See the readme to get started.
 * It follows the spec here: https://github.com/battlesnakeio/docs/tree/master/apis/snake
 */
public class Snake {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final Handler HANDLER = new Handler();
    private static final Logger LOG = LoggerFactory.getLogger(Snake.class);

    /**
     * Main entry point.
     *
     * @param args are ignored.
     */
    public static void main(String[] args) {
        String port = System.getProperty("PORT");
        if (port != null) {
            LOG.info("Found system provided port: {}", port);
        } else {
            LOG.info("Using default port: {}", port);
            port = "8080";
        }
        port(Integer.parseInt(port));
        get("/", (req, res) -> "Battlesnake documentation can be found at " + 
            "<a href=\"https://docs.battlesnake.io\">https://docs.battlesnake.io</a>.");
        post("/start", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/ping", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/move", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/end", HANDLER::process, JSON_MAPPER::writeValueAsString);
    }

    /**
     * Handler class for dealing with the routes set up in the main method.
     */
    public static class Handler {

        /**
         * For the ping request
         */
        private static final Map<String, String> EMPTY = new HashMap<>();

        /**
         * Generic processor that prints out the request and response from the methods.
         *
         * @param req
         * @param res
         * @return
         */
        public Map<String, String> process(Request req, Response res) {
            try {
                JsonNode parsedRequest = JSON_MAPPER.readTree(req.body());
                String uri = req.uri();
                LOG.info("{} called with: {}", uri, req.body());
                Map<String, String> snakeResponse;
                if (uri.equals("/start")) {
                    snakeResponse = start(parsedRequest);
                } else if (uri.equals("/ping")) {
                    snakeResponse = ping();
                } else if (uri.equals("/move")) {
                    snakeResponse = move(parsedRequest);
                } else if (uri.equals("/end")) {
                    snakeResponse = end(parsedRequest);
                } else {
                    throw new IllegalAccessError("Strange call made to the snake: " + uri);
                }
                LOG.info("Responding with: {}", JSON_MAPPER.writeValueAsString(snakeResponse));
                return snakeResponse;
            } catch (Exception e) {
                LOG.warn("Something went wrong!", e);
                return null;
            }
        }

        /**
         * /ping is called by the play application during the tournament or on play.battlesnake.io to make sure your
         * snake is still alive.
         *
         * @param pingRequest a map containing the JSON sent to this snake. See the spec for details of what this contains.
         * @return an empty response.
         */
        public Map<String, String> ping() {
            return EMPTY;
        }

        /**
         * /start is called by the engine when a game is first run.
         *
         * @param startRequest a map containing the JSON sent to this snake. See the spec for details of what this contains.
         * @return a response back to the engine containing the snake setup values.
         */
        public Map<String, String> start(JsonNode startRequest) {
            Map<String, String> response = new HashMap<>();
            response.put("color", "#000000");
            return response;
        }

        /**
         * /move is called by the engine for each turn the snake has.
         *
         * @param moveRequest a map containing the JSON sent to this snake. See the spec for details of what this contains.
         * @return a response back to the engine containing snake movement values.
         */
        public Map<String, String> move(JsonNode moveRequest) {//store as battledata for start and move.
            Map<String, String> response = new HashMap<>();

            final int FREE = 0;
            final int SNAKE = 1;
            final int HEAD = 2;
            final int FOOD = 3;




            int food_x = moveRequest.get("board").get("food").elements().next().get("x").asInt(-1); //gets x cord of first food
            int food_y = moveRequest.get("board").get("food").elements().next().get("y").asInt(-1); //gets y cord of first food

            int head_x = moveRequest.get("you").get("body").elements().next().get("x").asInt(); //get x cord of snake head
            int head_y = moveRequest.get("you").get("body").elements().next().get("y").asInt(); //get y cord of snake head

            int width = moveRequest.get("board").get("width").asInt(); //gets width of board
            int height = moveRequest.get("board").get("height").asInt(); //gets width of board

            int[][] board = new int[height][width];

            // Board values; 0 means empty, 1 means DEATH, 2 means FOOD


            for(JsonNode snake : moveRequest.get("board").get("snakes"))
            {
                for (JsonNode snakeBody : snake.get("body"))
                {
                    board[snakeBody.get("x").asInt()][snakeBody.get("y").asInt()] = SNAKE;
                }
                int snakeX = snake.get("body").elements().next().get("x").asInt();
                int snakeY = snake.get("body").elements().next().get("y").asInt();
                board[snakeX][snakeY] = SNAKE;
            }



            for (JsonNode food : moveRequest.get("board").get("food"))
            {
                board[food.get("x").asInt()][food.get("y").asInt()] = FOOD;
            }


if(food_x != -1 && food_y != -1  ){
    if (head_x < width-1 && head_x < food_x && board[head_x+1][head_y] != SNAKE){
                response.put("move", "right");
                return response;
            }
            else if (head_y < height-1 && head_y < food_y&& board[head_x][head_y+1] != SNAKE){
                response.put("move", "down");
                return response;
            }
            else if (head_y > 0 && head_y > food_y&& board[head_x][head_y-1] != SNAKE){
                response.put("move", "up");
                return response;
            }
            else if (head_x > 0 && head_x > food_x&& board[head_x-1][head_y] != SNAKE){
                response.put("move", "left");
                return response;
            }
}
            
            /*
            Response Output Options:  up down left right
            */
            else if (head_x < width-1 && board[head_x+1][head_y] != SNAKE){
            response.put("move", "right");
            return response;
            }
            else if (head_y < height-1 && board[head_x][head_y+1] != SNAKE){
            response.put("move", "down");
            return response;
            }
            else if (head_y > 0 &&board[head_x][head_y-1] != SNAKE){
            response.put("move", "up");
            return response;
            }
            else if (head_x > 0 &&board[head_x-1][head_y] != SNAKE){
            response.put("move", "left");
            return response;
            }


            else response.put("move", "left");
            return response;
        }

        /**
         * /end is called by the engine when a game is complete.
         *
         * @param endRequest a map containing the JSON sent to this snake. See the spec for details of what this contains.
         * @return responses back to the engine are ignored.
         */
        public Map<String, String> end(JsonNode endRequest) {
            Map<String, String> response = new HashMap<>();
            return response;
        }
    }

}
