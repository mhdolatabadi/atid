import { useEffect } from 'react';
import { skyAt } from '../lib/sky';

/** The living backdrop: its colours follow the current prayer period and glide when it changes. */
export function SkyBackground() {
  useEffect(() => {
    const apply = () => {
      document.documentElement.dataset.sky = skyAt(new Date());
    };
    apply();
    const id = setInterval(apply, 60_000);
    return () => clearInterval(id);
  }, []);

  return (
    <div className="sky" aria-hidden="true">
      <div className="sky__orb sky__orb--1" />
      <div className="sky__orb sky__orb--2" />
      <div className="sky__orb sky__orb--3" />
    </div>
  );
}
