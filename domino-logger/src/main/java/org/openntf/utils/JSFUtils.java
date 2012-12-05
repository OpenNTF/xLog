package org.openntf.utils;

import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import lotus.domino.ACL;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.commons.lang.StringUtils;

import com.ibm.xsp.designer.context.ServletXSPContext;

/**
 * Utility-class for getting JSF variables
 * 
 * @author Olle Thalén
 *
 */
public class JSFUtils {
	@SuppressWarnings("deprecation")
	public static Object getVariable(String name) {
		return FacesContext.getCurrentInstance().getApplication()
				.getVariableResolver()
				.resolveVariable(FacesContext.getCurrentInstance(), name);
	}
	
	public static Session getCurrentSession() {
		return (Session) getVariable("session");
	}
	
	public static Database getCurrentDatabase() {
		return (Database) getVariable("database");
	}
	
	public static String getSessionID() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		ExternalContext exCon = ctx.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) exCon
				.getRequest();
		return request.getRequestedSessionId();
	}
	
	public static String getRequestParameterValue(String parameter) {
		return (String) FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get(parameter);
	}
	
	public static String getUsername() {
		ServletXSPContext xspContext = (ServletXSPContext) JSFUtils
				.getVariable("context");
		String fullName = xspContext.getUser().getDistinguishedName();
		return fullName;
	}
	
	public static String getAccessLevel() {
		ServletXSPContext xspContext = (ServletXSPContext) JSFUtils
				.getVariable("context");
		try {
			int aclLevel = getCurrentDatabase().queryAccess(xspContext.getUser().getDistinguishedName());
			switch (aclLevel) {
			case ACL.LEVEL_AUTHOR:
				return "Author";
			case ACL.LEVEL_DEPOSITOR:
				return "Depositor";
			case ACL.LEVEL_DESIGNER:
				return "Designer";
			case ACL.LEVEL_EDITOR:
				return "Editor";
			case ACL.LEVEL_MANAGER:
				return "Manager";
			case ACL.LEVEL_NOACCESS:
				return "No access";
			case ACL.LEVEL_READER:
				return "Reader";
				default:
					return StringUtils.EMPTY;
			}
		} catch (NotesException e) {
			e.printStackTrace();
			return StringUtils.EMPTY;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> getRoles() {
		ServletXSPContext xspContext = (ServletXSPContext) JSFUtils
				.getVariable("context");
		return xspContext.getUser().getRoles();
	}
}