package util;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class PasswordUtil
{
   public static final char INDICATOR_START = '{';
   public static final char INDICATOR_END = '}';

   public static final String ALGO_BCRYPT = "BCRYPT";

   private static final SecureRandom random;

   static
   {
      random = new SecureRandom();
   }

   public static String normalizePassword(final String password)
   {
      // Normalize password using NFKC form
      return Normalizer.normalize(password, Form.NFKC);
   }

   public static boolean comparePassword(String enteredPassword, final String storedPassword)
   {
      // Normalize password
      enteredPassword = normalizePassword(enteredPassword);

      final int idx1 = storedPassword.indexOf(INDICATOR_START);
      final int idx2 = storedPassword.indexOf(INDICATOR_END, idx1);

      if (idx1 != 0 || idx2 <= idx1 + 1 || idx2 == storedPassword.length() - 1)
         throw new IllegalArgumentException("Stored password is invalid syntax");

      final String algo = storedPassword.substring(idx1 + 1, idx2);
      final String hash = storedPassword.substring(idx2 + 1);

      if (algo.equals(ALGO_BCRYPT))
         return BCrypt.checkpw(enteredPassword, hash);

      throw new IllegalArgumentException("Unsupported algorithm indicator in password: " + algo);
   }

   public static String preparePassword(final String enteredPassword)
   {
      return preparePasswordBCrypt(enteredPassword, 10);
   }

   private static String preparePasswordBCrypt(final String enteredPassword, final int logRounds)
   {
      final String salt = BCrypt.gensalt(logRounds, random);
      return INDICATOR_START + ALGO_BCRYPT + INDICATOR_END + BCrypt.hashpw(normalizePassword(enteredPassword), salt);
   }
}
