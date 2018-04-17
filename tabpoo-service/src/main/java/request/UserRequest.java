package request;

import org.apache.commons.lang3.StringUtils;

public class UserRequest
{
   public String username;
   public String password;
   public String confirmPassword;
   public String givenname;
   public String surname;

   public boolean isPasswordValid() {
      return StringUtils.equals(password, confirmPassword);
   }

   public boolean isValid() {
      if (StringUtils.isBlank(username)
            || StringUtils.isBlank(password)
            || StringUtils.isBlank(confirmPassword)
            || StringUtils.isBlank(givenname)
            || StringUtils.isBlank(surname))
         return false;

      return true;
   }
}
