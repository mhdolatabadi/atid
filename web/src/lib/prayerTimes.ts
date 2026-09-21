// Approximate shar'i prayer-time calculator based on standard low-precision solar-position
// formulas (NOAA/Meeus equation of time and declination). Uses the "Tehran" angle convention
// (Fajr 17.7°, Maghrib 4.5°, Isha 14°) with a Jafari/Shafii (shadow factor = 1) Asr rule, which
// is the common convention for Iran. Results are approximate, typically within about a minute
// of official published tables. Ported 1:1 from the Android app's PrayerTimesCalculator.kt.

export interface PrayerTimes {
  fajr: string;
  sunrise: string;
  dhuhr: string;
  asr: string;
  sunset: string;
  maghrib: string;
  isha: string;
}

const FAJR_ANGLE = 17.7;
const MAGHRIB_ANGLE = 4.5;
const ISHA_ANGLE = 14.0;
const SUN_EDGE_ANGLE = 0.833; // sunrise/sunset: refraction + solar radius
const ASR_SHADOW_FACTOR = 1.0;

function toRadians(deg: number): number {
  return (deg * Math.PI) / 180;
}

function toDegrees(rad: number): number {
  return (rad * 180) / Math.PI;
}

export function calculateForToday(latitude: number, longitude: number): PrayerTimes {
  const now = new Date();
  const timeZoneOffsetHours = -now.getTimezoneOffset() / 60;
  return calculate(
    now.getFullYear(),
    now.getMonth() + 1,
    now.getDate(),
    latitude,
    longitude,
    timeZoneOffsetHours,
  );
}

export function calculate(
  year: number,
  month: number,
  day: number,
  latitude: number,
  longitude: number,
  timeZoneOffsetHours: number,
): PrayerTimes {
  const julianDate = julianDay(year, month, day) + 0.5;
  const julianCenturies = (julianDate - 2451545.0) / 36525.0;
  const { declination, equationOfTimeMinutes } = sunPosition(julianCenturies);

  const dhuhr = 12.0 - longitude / 15.0 - equationOfTimeMinutes / 60.0 + timeZoneOffsetHours;
  const fajr = dhuhr - hourAngle(-FAJR_ANGLE, latitude, declination) / 15.0;
  const sunrise = dhuhr - hourAngle(-SUN_EDGE_ANGLE, latitude, declination) / 15.0;
  const asrAltitude = toDegrees(
    Math.atan(1.0 / (ASR_SHADOW_FACTOR + Math.tan(toRadians(Math.abs(latitude - declination))))),
  );
  const asr = dhuhr + hourAngle(asrAltitude, latitude, declination) / 15.0;
  const sunset = dhuhr + hourAngle(-SUN_EDGE_ANGLE, latitude, declination) / 15.0;
  const maghrib = dhuhr + hourAngle(-MAGHRIB_ANGLE, latitude, declination) / 15.0;
  const isha = dhuhr + hourAngle(-ISHA_ANGLE, latitude, declination) / 15.0;

  return {
    fajr: formatHours(fajr),
    sunrise: formatHours(sunrise),
    dhuhr: formatHours(dhuhr),
    asr: formatHours(asr),
    sunset: formatHours(sunset),
    maghrib: formatHours(maghrib),
    isha: formatHours(isha),
  };
}

/** Hour angle (degrees) at which the sun reaches `angleDeg` altitude for the given location. */
function hourAngle(angleDeg: number, latitudeDeg: number, declinationDeg: number): number {
  const lat = toRadians(latitudeDeg);
  const decl = toRadians(declinationDeg);
  const angle = toRadians(angleDeg);
  const cosH = (Math.sin(angle) - Math.sin(lat) * Math.sin(decl)) / (Math.cos(lat) * Math.cos(decl));
  return toDegrees(Math.acos(Math.min(1.0, Math.max(-1.0, cosH))));
}

/** Returns { declination, equationOfTimeMinutes } for Julian century `t`. */
function sunPosition(t: number): { declination: number; equationOfTimeMinutes: number } {
  const l0 = normalizeDegrees(280.46646 + t * (36000.76983 + t * 0.0003032));
  const m = normalizeDegrees(357.52911 + t * (35999.05029 - 0.0001537 * t));
  const e = 0.016708634 - t * (0.000042037 + 0.0000001267 * t);
  const mRad = toRadians(m);

  const centerCorrection = (1.914602 - t * (0.004817 + 0.000014 * t)) * Math.sin(mRad) +
    (0.019993 - 0.000101 * t) * Math.sin(2 * mRad) +
    0.000289 * Math.sin(3 * mRad);

  const trueLongitude = l0 + centerCorrection;
  const omega = 125.04 - 1934.136 * t;
  const apparentLongitude = trueLongitude - 0.00569 - 0.00478 * Math.sin(toRadians(omega));

  const meanObliquity = 23.0 +
    (26.0 + (21.448 - t * (46.815 + t * (0.00059 - t * 0.001813))) / 60.0) / 60.0;
  const obliquity = meanObliquity + 0.00256 * Math.cos(toRadians(omega));

  const declination = toDegrees(
    Math.asin(Math.sin(toRadians(obliquity)) * Math.sin(toRadians(apparentLongitude))),
  );

  const y = Math.tan(toRadians(obliquity / 2.0)) ** 2;
  const l0Rad = toRadians(l0);
  const equationOfTimeMinutes = 4.0 * toDegrees(
    y * Math.sin(2 * l0Rad) - 2 * e * Math.sin(mRad) + 4 * e * y * Math.sin(mRad) * Math.cos(2 * l0Rad) -
      0.5 * y * y * Math.sin(4 * l0Rad) - 1.25 * e * e * Math.sin(2 * mRad),
  );

  return { declination, equationOfTimeMinutes };
}

function normalizeDegrees(value: number): number {
  const result = value % 360.0;
  return result < 0 ? result + 360.0 : result;
}

function julianDay(year: number, month: number, day: number): number {
  let y = year;
  let m = month;
  if (m <= 2) {
    y -= 1;
    m += 12;
  }
  const a = Math.trunc(y / 100);
  const b = 2 - a + Math.trunc(a / 4);
  return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + day + b - 1524.5;
}

function formatHours(hours: number): string {
  let h = hours % 24.0;
  if (h < 0) h += 24.0;
  const totalMinutes = Math.round(h * 60.0);
  const hh = Math.trunc(totalMinutes / 60) % 24;
  const mm = totalMinutes % 60;
  return `${String(hh).padStart(2, '0')}:${String(mm).padStart(2, '0')}`;
}
