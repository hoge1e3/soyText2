package jp.tonyu.util;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;


public class TDate {
	long ticks;
	String template;
	TimeZone timeZone=TimeZone.getDefault();
	Locale locale=Locale.getDefault();

	public String getTemplate() {
		return template;
	}
	public TDate setTemplate(String template) {
		this.template = template;
		return this;

	}
	public TDate() {
		ticks=new java.util.Date().getTime();	
	}
	public TDate(long ticks) {
		this.ticks=ticks;	
	}
	public TDate(String src, String template, Locale locale) throws ParseException {
		DateFormat fmt=new SimpleDateFormat(template, locale);
		java.util.Date d=fmt.parse(src);
		ticks=d.getTime();
		//timeZone=fmt.getTimeZone();
	}
	public TimeZone getTimeZone() {
		return timeZone;
	}
	public TDate setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
		return this;
	}
	public Locale getLocale() {
		return locale;
	}
	public TDate setLocale(Locale locale) {
		this.locale = locale;
		return this;
	}
	public long ticks() {
		return ticks;
	}
	public String toString(DateFormat fmt) {
		fmt.setTimeZone(timeZone);
		java.util.Date d=new java.util.Date(ticks);
		return fmt.format(d);	
	}
	public String toString() {
		if (template!=null) return toString(template);
		DateFormat fmt=new SimpleDateFormat();
		return toString(fmt);
	}
	public String toString(String template) {
		DateFormat fmt=new SimpleDateFormat(template,locale);
		return toString(fmt);
	}
	public long sub(TDate other) {
		return ticks-other.ticks;
	}
	public TDate after(long duration) {
		return new TDate(ticks+duration).copyFrom(this);
	}
	
	private TDate copyFrom(TDate date) {
		return setLocale(date.getLocale()).setTemplate(date.getTemplate()).setTimeZone(date.getTimeZone());
	}
	public String toRFC2822() {
		//Thu, 03 May 2007 17:13:21 GMT
		setLocale(Locale.ENGLISH);
		return toString(rfc2822tmpl);
	}
	public static TDate fromRFC2822(String src) throws ParseException {
		return new TDate(src,rfc2822tmpl, Locale.ENGLISH);
	}
	public static final String rfc2822tmpl="EEE, dd MMM yyyy hh:mm:ss z";
	public static final long oneSecond=1000;
}
