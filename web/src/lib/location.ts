export interface Coordinates {
  latitude: number;
  longitude: number;
  isDeviceLocation: boolean;
}

export const TEHRAN: Coordinates = {
  latitude: 35.6892,
  longitude: 51.389,
  isDeviceLocation: false,
};

export function requestDeviceLocation(): Promise<Coordinates> {
  return new Promise((resolve, reject) => {
    if (!('geolocation' in navigator)) {
      reject(new Error('Geolocation is not supported'));
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          isDeviceLocation: true,
        });
      },
      (error) => reject(error),
      { enableHighAccuracy: false, timeout: 10000, maximumAge: 10 * 60 * 1000 },
    );
  });
}
