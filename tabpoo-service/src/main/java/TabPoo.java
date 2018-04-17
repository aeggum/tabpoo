import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.post;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import model.JSONWebToken;
import model.JSONWebTokenHeader;
import request.LogRequest;
import request.UserRequest;
import model.WebToken;
import spark.Request;
import spark.Response;
import util.PasswordUtil;

public class TabPoo {
   private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
   private static Logger logger = LogManager.getLogger("main");
   private static SecureRandom random = new SecureRandom();

   private static KeyPair signingKeyPair;
   private static String kid;

   private static final String ALG = "RS256";
   private final static String SIGNATURE_ALGORITHM = "SHA256withRSA";
   private static final String RSA = "RSA";

   private static final MongoDatabase database;
   private static final MongoClient mongoClient;

   static {
      mongoClient = new MongoClient("localhost", 27017);
      database = mongoClient.getDatabase("tabpoo");
   }

   public static void main(String[] args) throws Exception {
      setup();

      post("/authenticate", TabPoo::authenticate, gson::toJson);
      post("/poolog", TabPoo::logpoo, gson::toJson);
      post("/users", TabPoo::createUser, gson::toJson);
      get("/poolog", TabPoo::getLogsForUser, gson::toJson);

      before((request, response) -> {
         response.header("Content-Type", "application/json");
      });

      Runtime.getRuntime().addShutdownHook(new ShutdownHook());
   }

   private static void setup() throws Exception {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA);
      keyGen.initialize(512, random);
      signingKeyPair = keyGen.generateKeyPair();
      kid = DigestUtils.sha1Hex("tabpoo-app");

      enableCORS();
   }

   static class ShutdownHook extends Thread {
      public void run() {
         mongoClient.close();
      }
   }

   @SuppressWarnings("unchecked")
   public static Object createUser(Request request, Response response) {
      UserRequest userRequest = gson.fromJson(request.body(), UserRequest.class);

      if (!userRequest.isValid()) {
         response.status(400);
         return Collections.singletonMap("error", "Invalid input");
      }
      else if (!userRequest.isPasswordValid()) {
         response.status(400);
         return Collections.singletonMap("error", "Invalid password or passwords do not match");
      }

      MongoCollection<Document> users = database.getCollection("user");

      Document matchingUser = users.find(Filters.eq("username", userRequest.username)).first();
      if (matchingUser != null) {
         response.status(409);
         return Collections.singletonMap("error", "Cannot create user");
      }

      Document newUser = new Document("uuid", UUID.randomUUID())
            .append("username", userRequest.username)
            .append("password", PasswordUtil.preparePassword(userRequest.password))
            .append("first", userRequest.givenname)
            .append("last", userRequest.surname);
      users.insertOne(newUser);

      response.status(201);
      return Collections.singletonMap("status", "Success");
   }

   public static Object getLogsForUser(Request request, Response response) {
      JSONWebToken token = getValidAuthorizationToken(request, response);

      if (token == null) {
         response.status(403);
         return Collections.singletonMap("error", "Forbidden");
      }

      MongoCollection<Document> users = database.getCollection("user");

      Document matchingUser = users.find(Filters.eq("uuid", token.uuid)).first();
      if (matchingUser == null) {
         response.status(500);
         return Collections.singletonMap("error", "Unexpected Error");
      }

      MongoCollection<Document> poolog = database.getCollection("poolog");
      List<LogRequest> pooHistory = new ArrayList<>();
      for (Document d : poolog.find()) {
         pooHistory.add(new LogRequest((Long) d.get("timestamp"), (Integer) d.get("bristolLevel")));
      }

      return pooHistory;
   }

   @SuppressWarnings("unchecked")
   public static Object authenticate(Request request, Response response) {
      Map<String, String> requestMap = gson.fromJson(request.body(), Map.class);

      String username = requestMap.get("username");
      String password = requestMap.get("password");

      MongoCollection<Document> users = database.getCollection("user");

      Document matchingUser = users.find(Filters.eq("username", username)).first();
      if (matchingUser == null) {
         response.status(401);
         return Collections.singletonMap("error", "Unauthorized");
      }

      String userPwd = matchingUser.getString("password");
      if (!PasswordUtil.comparePassword(password, userPwd)) {
         response.status(401);
         return Collections.singletonMap("error", "Unauthorized");
      }

      JSONWebToken token = new JSONWebToken();

      token.uuid = (UUID) matchingUser.get("uuid");

      Long exp = 3600L;
      token.iat = System.currentTimeMillis() / 1000;  // iat should be seconds from the epoch
      token.exp = token.iat + exp;

      String encodedTokenString = getEncodedToken(token);

      Map<String, Object> responseDataMap = new HashMap<>();
      responseDataMap.put("access_token", encodedTokenString);
      responseDataMap.put("token_type", "Bearer");
      responseDataMap.put("expires_in", exp);
      responseDataMap.put("expiration", token.exp);
      return responseDataMap;
   }

   public static Object logpoo(Request request, Response response) {
      JSONWebToken token = getValidAuthorizationToken(request, response);

      if (token == null) {
         response.status(403);
         return Collections.singletonMap("error", "Forbidden");
      }

      MongoCollection<Document> users = database.getCollection("user");

      Document matchingUser = users.find(Filters.eq("uuid", token.uuid)).first();
      if (matchingUser == null) {
         response.status(500);
         return Collections.singletonMap("error", "Unexpected Error");
      }

      LogRequest logRequest = gson.fromJson(request.body(), LogRequest.class);

      MongoCollection<Document> poolog = database.getCollection("poolog");
      Document newLog = new Document("uuid", token.uuid)
            .append("timestamp", logRequest.timestamp)
            .append("bristol", logRequest.bristolLevel);
      poolog.insertOne(newLog);

      response.status(201);
      return Collections.singletonMap("status", "Success");
   }

   private static JSONWebTokenHeader getTokenHeader() {
      JSONWebTokenHeader header = new JSONWebTokenHeader();
      header.kid = kid;
      header.alg = ALG;
      return header;
   }

   private static String getEncodedToken(WebToken token) {
      try {
         JSONWebTokenHeader header = getTokenHeader();
         String part1 = Base64.getUrlEncoder().encodeToString(gson.toJson(header).getBytes());
         String part2 = Base64.getUrlEncoder().encodeToString(gson.toJson(token).getBytes());
         String signingString = part1 + "." + part2;

         Signature rsa = Signature.getInstance(SIGNATURE_ALGORITHM);
         rsa.initSign(signingKeyPair.getPrivate());
         rsa.update(signingString.getBytes());
         byte[] signature = rsa.sign();
         String base64Signature = Base64.getUrlEncoder().encodeToString(signature);

         return signingString + "." + base64Signature;
      } catch (Exception ex) {
         logger.error("Could not sign JWT {}", token, ex);
         throw new RuntimeException("Error signing JWT");
      }
   }

   private static JSONWebToken getValidAuthorizationToken(final Request request, final Response response) {
      String givenTokenString = request.headers("Authorization");

      if (givenTokenString != null && givenTokenString.split("\\.").length == 3) {
         String[] tokenParts = givenTokenString.split("\\.");
         JSONWebToken jwt = gson.fromJson(new String(Base64.getUrlDecoder().decode(tokenParts[1])), JSONWebToken.class);

         logger.info("jwt from authorization header: {}", jwt);

         if (jwt.exp < (System.currentTimeMillis() / 1000)) {
            logger.info("given token is expired: {}", jwt);
            return null;
         }

         return jwt;
      } else {
         logger.info("Invalid JWT: was null or did not contain 3 parts.  Authorization header was {}", givenTokenString);
         return null;
      }
   }

   private static void enableCORS() {
      options("/*", (request, response) -> {

         String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
         if (accessControlRequestHeaders != null) {
            response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
         }

         String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
         if (accessControlRequestMethod != null) {
            response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
         }

         return "OK";
      });

      before((request, response) -> {
         response.header("Access-Control-Allow-Origin", "*");
         response.header("Access-Control-Request-Method", "GET, POST, PUT, DELETE");
         response.header("Access-Control-Allow-Headers", "Origin, Accept,  X-Auth-Token, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization");
      });
   }
}
