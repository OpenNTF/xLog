package org.openntf.logging.config;


public class Configuration {
	
	private String applicationName;
	
	private String context;
	private String userName;
	private String roles;
	private String accessLevel;
	private Integer counter = 0;
	private boolean error = false;
	
	public String getApplicationName() {
		return applicationName;
	}
	
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
	public String getContext() {
		return context;
	}
	
	public void setContext(String context) {
		this.context = context;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getRoles() {
		return roles;
	}
	
	public void setRoles(String roles) {
		this.roles = roles;
	}
	
	public String getAccessLevel() {
		return accessLevel;
	}
	
	public void setAccessLevel(String accessLevel) {
		this.accessLevel = accessLevel;
	}
	
	public Integer getCounter() {
		return counter;
	}
	
	public void setCounter(Integer counter) {
		this.counter = counter;
	}
	
	public boolean isError() {
		return error;
	}
	
	public void setError(boolean error) {
		this.error = error;
	}
}
