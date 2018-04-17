package model;

import java.util.UUID;

public class JSONWebToken extends WebToken {
   public UUID uuid;

   @Override
   public String toString() {
      return "JSONWebToken{" + "uuid=" + uuid + ", iat=" + iat + ", exp=" + exp + '}';
   }
}
