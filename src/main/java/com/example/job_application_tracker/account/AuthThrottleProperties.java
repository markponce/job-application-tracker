package com.example.job_application_tracker.account;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;

@ConfigurationProperties("app.auth.throttling")
@Validated
public class AuthThrottleProperties {

	private boolean enabled = true;
	@Min(1)
	private int loginPerIp = 20;
	@Min(1)
	private int loginPerAccount = 10;
	@Min(1)
	private int registrationPerIp = 5;
	@Min(1)
	private int verificationPerIp = 20;
	@Min(1)
	private int verificationPerAccount = 10;
	@Min(1)
	private int resendPerIp = 5;
	@Min(1)
	private int resendPerAccount = 3;
	@Min(1)
	private long loginWindowSeconds = 900;
	@Min(1)
	private long registrationWindowSeconds = 3600;
	@Min(1)
	private long verificationWindowSeconds = 900;
	@Min(1)
	private long resendWindowSeconds = 3600;

	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean value) { enabled = value; }
	public int getLoginPerIp() { return loginPerIp; }
	public void setLoginPerIp(int value) { loginPerIp = value; }
	public int getLoginPerAccount() { return loginPerAccount; }
	public void setLoginPerAccount(int value) { loginPerAccount = value; }
	public int getRegistrationPerIp() { return registrationPerIp; }
	public void setRegistrationPerIp(int value) { registrationPerIp = value; }
	public int getVerificationPerIp() { return verificationPerIp; }
	public void setVerificationPerIp(int value) { verificationPerIp = value; }
	public int getVerificationPerAccount() { return verificationPerAccount; }
	public void setVerificationPerAccount(int value) { verificationPerAccount = value; }
	public int getResendPerIp() { return resendPerIp; }
	public void setResendPerIp(int value) { resendPerIp = value; }
	public int getResendPerAccount() { return resendPerAccount; }
	public void setResendPerAccount(int value) { resendPerAccount = value; }
	public long getLoginWindowSeconds() { return loginWindowSeconds; }
	public void setLoginWindowSeconds(long value) { loginWindowSeconds = value; }
	public long getRegistrationWindowSeconds() { return registrationWindowSeconds; }
	public void setRegistrationWindowSeconds(long value) { registrationWindowSeconds = value; }
	public long getVerificationWindowSeconds() { return verificationWindowSeconds; }
	public void setVerificationWindowSeconds(long value) { verificationWindowSeconds = value; }
	public long getResendWindowSeconds() { return resendWindowSeconds; }
	public void setResendWindowSeconds(long value) { resendWindowSeconds = value; }
}
