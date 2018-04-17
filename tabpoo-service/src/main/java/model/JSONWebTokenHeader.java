package model;

public class JSONWebTokenHeader {
   public String kid = "";
   public String alg = "";

   @Override
   public String toString() {
      return "model.JSONWebTokenHeader{" + "kid='" + kid + '\'' + ", alg='" + alg + '\'' + '}';
   }
}
