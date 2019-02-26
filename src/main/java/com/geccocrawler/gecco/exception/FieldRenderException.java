package com.geccocrawler.gecco.exception;

import com.geccocrawler.gecco.spider.SpiderThreadLocal;
import lombok.extern.apachecommons.CommonsLog;

import java.lang.reflect.Field;

/**
 * 注入某个属性异常
 * 
 * @author huchengyi
 *
 */
@CommonsLog
public class FieldRenderException extends Exception {
	private static final long serialVersionUID = 5698150653455275921L;

	private Field field;

	public FieldRenderException(Field field, String message) {
		super(message);
		this.field = field;
	}

	public FieldRenderException(Field field, String message, Throwable cause) {
		super(message, cause);
		this.field = field;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public static void log(Field field, String message, Throwable cause) {
		boolean debug = SpiderThreadLocal.get().getEngine().isDebug();
		log.error(field.getName() + " render error : " + message);
		if(debug && cause != null) {
			log.error(message, cause);
		}
	}

	public static void log(Field field, String message) {
		log(field, message, null);
	}
}
