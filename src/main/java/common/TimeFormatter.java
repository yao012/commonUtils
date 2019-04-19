package common;
/*
 * Created by Qin Meijie on 2017/11/27.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 */
public class TimeFormatter {
	private final static Logger L = LogManager.getLogger(TimeFormatter.class);

	enum Language {
		CHINESE{
			@Override
			public String getDay() {
				return "天";
			}

			@Override
			public String getHour() {
				return "小时";
			}

			@Override
			public String getMinute() {
				return "分钟";
			}

			@Override
			public String getSecond() {
				return "秒";
			}

			@Override
			public String getMilli() {
				return "毫秒";
			}

			@Override
			public String getMicro() {
				return "微秒";
			}

			@Override
			public String getNano() {
				return "纳秒";
			}
		}, ENGLISH {
			@Override
			public String getDay() {
				return " days";
			}

			@Override
			public String getHour() {
				return " hours";
			}

			@Override
			public String getMinute() {
				return " minutes";
			}

			@Override
			public String getSecond() {
				return " seconds";
			}

			@Override
			public String getMilli() {
				return " millis";
			}

			@Override
			public String getMicro() {
				return " micros";
			}

			@Override
			public String getNano() {
				return " nanos";
			}
		};
		public abstract String getDay();
		public abstract String getHour();
		public abstract String getMinute();
		public abstract String getSecond();
		public abstract String getMilli();
		public abstract String getMicro();
		public abstract String getNano();
	}

	// 将时间差转为可读的时长： xx 天 xx 小时  xx 分钟
	public static String toMin(long from, long to) {
		return format(from, to, TimeUnit.MINUTES, Language.CHINESE, true);
	}

	public static String toMin(Date from, Date to) {
		return format(from.getTime(), to.getTime(), TimeUnit.SECONDS, Language.CHINESE, false);
	}

	/**
	 * 将 {@code from} 到 {@code to} 这段时间，转成可读的字符串，精度为 {@code precision}，语言为 {@code language}
	 *
	 * @param from 开始时间戳
	 * @param to 结束时间戳
	 * @param precision 精度
	 * @param language 语言
	 *
	 * @return
	 */
	public static String format(long from, long to, TimeUnit precision, Language language, boolean accumulated) {
		int ordinal = precision.ordinal();  // 精度比较，越大精度越低
		long rangeInPrecision = precision.convert(Math.abs(from - to), TimeUnit.MILLISECONDS);
		StringBuilder builder = new StringBuilder();

		long n = precision.toDays(rangeInPrecision);
		if(n > 0) {
			builder.append(n).append(language.getDay()).append(' ');
		}

		if( ordinal < TimeUnit.DAYS.ordinal()) {
			rangeInPrecision = rangeInPrecision - precision.convert(n, TimeUnit.DAYS);
			n = precision.toHours(rangeInPrecision);

			if(n > 0) {
				if(!accumulated) {
					builder.setLength(0);
				}
				builder.append(n).append(language.getHour()).append(' ');
			}
			if( ordinal < TimeUnit.HOURS.ordinal()) {
				rangeInPrecision = rangeInPrecision - precision.convert(n, TimeUnit.HOURS);
				n = precision.toMinutes(rangeInPrecision);
				if(n > 0) {
					if(!accumulated) {
						builder.setLength(0);
					}
					builder.append(n).append(language.getMinute()).append(' ');
				}
				if( ordinal < TimeUnit.MINUTES.ordinal()) {
					rangeInPrecision = rangeInPrecision - precision.convert(n, TimeUnit.MINUTES);
					n = precision.toSeconds(rangeInPrecision);
					if(n > 0) {
						if(!accumulated) {
							builder.setLength(0);
						}
						builder.append(n).append(language.getSecond()).append(' ');
					}
					if( ordinal < TimeUnit.SECONDS.ordinal()) {
						rangeInPrecision = rangeInPrecision - precision.convert(n, TimeUnit.SECONDS);
						n = precision.toMillis(rangeInPrecision);

						if(n > 0) {
							if(!accumulated) {
								builder.setLength(0);
							}
							builder.append(n).append(language.getMilli()).append(' ');
						}
						if( ordinal < TimeUnit.MILLISECONDS.ordinal()) {
							rangeInPrecision = rangeInPrecision - precision.convert(n, TimeUnit.MILLISECONDS);
							n = precision.toMicros(rangeInPrecision);

							if(n > 0) {
								if(!accumulated) {
									builder.setLength(0);
								}
								builder.append(n).append(language.getMicro()).append(' ');
							}
							if( ordinal < TimeUnit.MICROSECONDS.ordinal()) {
								rangeInPrecision = rangeInPrecision - precision.convert(n, TimeUnit.MICROSECONDS);
								n = precision.toNanos(rangeInPrecision);

								if(n > 0) {
									if(!accumulated) {
										builder.setLength(0);
									}
									builder.append(n).append(language.getNano()).append(' ');
								}
							}
						}

					}
				}
			}
		}
		return builder.toString();
	}
}

