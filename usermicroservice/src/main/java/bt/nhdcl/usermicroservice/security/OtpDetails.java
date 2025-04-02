package bt.nhdcl.usermicroservice.security;

public class OtpDetails {
    private final String otp;
    private final long expiryTime;

    public OtpDetails(String otp, long expiryTime) {
        this.otp = otp;
        this.expiryTime = expiryTime;
    }

    public String getOtp() {
        return otp;
    }

    public long getExpiryTime() {
        return expiryTime;
    }
}
