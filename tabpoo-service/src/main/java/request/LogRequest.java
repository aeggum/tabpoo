package request;

public class LogRequest
{
   public LogRequest()
   {
   }

   public LogRequest(final Long timestamp, final Integer bristolLevel)
   {
      this.timestamp = timestamp;
      this.bristolLevel = bristolLevel;
   }

   public Long timestamp;
   public Integer bristolLevel;

//   public Integer volumeLevel;
//   public Integer wipeLevel;
//   public Integer satisfactionLevel;
}
