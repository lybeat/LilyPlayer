package com.lybeat.lilyplayer.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Author: lybeat
 * Date: 2015/12/20
 */
public class DateUtil {

	private DateUtil() {
		throw new UnsupportedOperationException("Cannot be instantiated");
	}
	
	private final static ThreadLocal<SimpleDateFormat> dateFormater = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};

	private final static ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};
	
	public static boolean isInEasternEightZones() {
		boolean defaultVaule;
        defaultVaule = TimeZone.getDefault() == TimeZone.getTimeZone("GMT+08");
		return defaultVaule;
	}

	/**
	 * 转换不同时区的日期
	 * @param date
	 * @param oldZone
	 * @param newZone
	 * @return
	 */
	public static Date transformTimeWithTimeZone(Date date, TimeZone oldZone,
												 TimeZone newZone) {
		Date finalDate = null;
		if (date != null) {
			int timeOffset = oldZone.getOffset(date.getTime())
					- newZone.getOffset(date.getTime());
			finalDate = new Date(date.getTime() - timeOffset);
		}
		return finalDate;

	}

	public static String getCurrentTime(String format) {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
		return sdf.format(date);
	}

	/**
	 *
	 * @param date
	 * @return
	 */
	public static String getWeekWithDate(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int day = c.get(Calendar.DAY_OF_WEEK);
		String str = null;
		if (day == 1) {
			str = "星期日";
		} else if (day == 2) {
			str = "星期一";
		} else if (day == 3) {
			str = "星期二";
		} else if (day == 4) {
			str = "星期三";
		} else if (day == 5) {
			str = "星期四";
		} else if (day == 6) {
			str = "星期五";
		} else if (day == 7) {
			str = "星期六";
		}
		return str;
	}

	/**
	 *
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean isSameDate(Date date1, Date date2) {
		Calendar c1 = Calendar.getInstance();
		c1.setTime(date1);
		Calendar c2 = Calendar.getInstance();
		c2.setTime(date2);
		
		if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) {
			if (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)) {
				if (c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * String to date
	 * 
	 * @param sdate
	 * @return
	 */
	public static Date toDate(String sdate) {
		try {
			return dateFormater.get().parse(sdate);
		} catch (ParseException e) {
			return null;
		}
	}

	public static String stampToString(long stamp) {
        stamp = stamp * 1000;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(Long.parseLong(String.valueOf(stamp)));
	}
	
	public static boolean isToday(String sdate) {
		boolean b = false;
		Date time = toDate(sdate);
		Date today = new Date();
		if (time != null) {
			String nowDate = dateFormater2.get().format(today);
			String timeDate = dateFormater2.get().format(time);
			if (nowDate.equals(timeDate)) {
				b = true;
			}
		}
		return b;
	}

	public static String msToString(long ms) {
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
		return sdf.format(ms);
	}

	public static String stringForTime(long timeMs) {
		int totalSeconds = (int) (timeMs / 1000);

		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours   = totalSeconds / 3600;

		StringBuilder formatBuilder = new StringBuilder();
		Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
		formatBuilder.setLength(0);
		if (hours > 0) {
			return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
		} else {
			return formatter.format("%02d:%02d", minutes, seconds).toString();
		}
	}

	/**
	 * 得到友好的时间格式
	 * @param sdate  yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String friendly_time(String sdate) {
		Date time;
		if (isInEasternEightZones()) {
			time = toDate(sdate);
		} else {
			time = transformTimeWithTimeZone(toDate(sdate),
					TimeZone.getTimeZone("GMT+08"), TimeZone.getDefault());
		}
		if (time == null) {
			return "Unknown";
		}
		String ftime = "";
		Calendar cal = Calendar.getInstance();

		String curDate = dateFormater2.get().format(cal.getTime());
		String paramDate = dateFormater2.get().format(time);
		if (curDate.equals(paramDate)) {
			int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
			if (hour == 0)
				ftime = Math.max(
						(cal.getTimeInMillis() - time.getTime()) / 60000, 1)
						+ "分钟前";
			else
				ftime = hour + "小时前";
			return ftime;
		}

		long lt = time.getTime() / 86400000;
		long ct = cal.getTimeInMillis() / 86400000;
		int days = (int) (ct - lt);
		if (days == 0) {
			int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
			if (hour == 0)
				ftime = Math.max(
						(cal.getTimeInMillis() - time.getTime()) / 60000, 1)
						+ "分钟前";
			else
				ftime = hour + "小时前";
		} else if (days == 1) {
			ftime = "昨天";
		} else if (days == 2) {
			ftime = "前天";
		} else if (days > 2 && days <= 10) {
			ftime = days + "天前";
		} else if (days > 10) {
			ftime = dateFormater2.get().format(time);
		}
		return ftime;
	}
}
